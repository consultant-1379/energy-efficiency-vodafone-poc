/*
 * Copyright (c) 2017 Ericsson, AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.basic.network.functions;

import com.ericsson.basic.network.functions.inventory.InventoryBandwidth;
import com.ericsson.basic.network.functions.inventory.InventoryHandler;
import com.ericsson.basic.network.functions.inventory.MiniLinkBandwidthCoordinates;
import com.ericsson.basic.network.functions.statistics.MonitoredDataSample;
import com.ericsson.basic.network.functions.statistics.RemoteStatisticsHandler;
import com.ericsson.basic.network.functions.statistics.StatisticsHistoryHandler;
import com.ericsson.basic.network.functions.statistics.StatisticsObserver;
import com.ericsson.basic.network.functions.statistics.StatisticsRunnable;
import com.ericsson.basic.network.functions.statistics.StatisticsSample;
import com.ericsson.basic.network.functions.statistics.ISO8601;
import com.ericsson.equipment.minilink.MiniLink_ACM;
import com.ericsson.equipment.minilink.MiniLink_ACM_Flavour;
import com.ericsson.equipment.minilink.MiniLink_ChannelSpacing;
import com.ericsson.sb.communication.SouthboundCommunicationService;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfacesStateBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.Interfaces;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfacesState;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.interfaces.Interface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.interfaces.state._interface.StatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.rev161116.NetworkId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.topology.rev161116.networks.network.Link;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.topology.rev161021.TeBandwidth;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.topology.rev161021.te.link.config.attributes.TeLinkAttributes;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter64;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.interfaces.network.topology.rev170714.IfRefTpRef;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.interfaces.network.topology.rev170714.IfRefTpRefBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ericsson
 */
public class InterfaceHandler implements StatisticsObserver, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(InterfaceHandler.class);
    private final Long ZERO_LONG = new Long(0);
    private DataBroker dataBroker;
    private TopologyHandler topologyHandler;
    private SouthboundCommunicationService southboundCommunicationService;
    private StatisticsHistoryHandler statisticsHistoryHandler;
    private volatile Map<String, String> ifRef2NodeId = Collections.synchronizedMap(new HashMap<>());
    private volatile Map<String, LinkGroupInfo> linkGroupMap = Collections.synchronizedMap(new HashMap<>());
    private Map<String, Thread> ifRefThreadMap = new HashMap<>();
    private Thread statisticsHistoryThread;
    private InventoryBandwidth inventoryBandwidth;

    private class CapacityInfo {

        private Long currentCapacity;
        private Long nominalCapacity;
        private Long requiredCapacity;
        private DateAndTime requestDateAndTime;

        public CapacityInfo() {

        }

        Long getCurrentCapacity() {
            return currentCapacity;
        }

        Long getRequiredCapacity() {
            return requiredCapacity;
        }

        Long getNominalCapacity() {
            return nominalCapacity;
        }

        DateAndTime getRequestDateAndTime() {
            return requestDateAndTime;
        }

        void setCurrentCapacity(final Long currentCapacity) {
            this.currentCapacity = currentCapacity;
        }

        void setRequiredCapacity(final Long requiredCapacity) {
            this.requiredCapacity = requiredCapacity;
        }

        void setNominalCapacity(final Long nominalCapacity) {
            this.nominalCapacity = nominalCapacity;
        }

        void setRequestDateAndTime(final DateAndTime requestDateAndTime) {
            this.requestDateAndTime = requestDateAndTime;
        }
    }

    private Map<String, CapacityInfo> ifRefCapacityInfo = Collections.synchronizedMap(new HashMap<>());

   /**
     * Returns the dataBroker
     *
     * @return DataBroker
     */
    public DataBroker getDataBroker() {
        return dataBroker;
    }

   /**
     * Returns the dataBroker
     *
     * @return DataBroker
     */
    public TopologyHandler getTopologyHandler() {
        return topologyHandler;
    }

    public void init(final DataBroker dataBroker,
            final SouthboundCommunicationService southboundCommunicationService,
            final TopologyHandler topologyHandler) {
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
        this.southboundCommunicationService = Preconditions.checkNotNull(southboundCommunicationService);
        this.topologyHandler = Preconditions.checkNotNull(topologyHandler);

        createInterfacesData();
        inventoryBandwidth = new InventoryBandwidth();
        inventoryBandwidth.init();
        statisticsHistoryHandler = new StatisticsHistoryHandler();
        statisticsHistoryHandler.init(dataBroker, topologyHandler.getTopologyUpdater(), this);
        statisticsHistoryThread = new Thread(statisticsHistoryHandler);
        statisticsHistoryThread.setDaemon(true);
        statisticsHistoryThread.setName("StatHist:");
        statisticsHistoryThread.start();
    }

    @Override
    public void close() throws InterruptedException {
        if (statisticsHistoryThread != null) {
            statisticsHistoryThread.interrupt();
            statisticsHistoryThread.join();
            statisticsHistoryThread = null;
        }
    }

    public void createInterfacesData() {
        final InterfacesBuilder interfacesBuilder = new InterfacesBuilder();
        interfacesBuilder.setInterface(Collections.emptyList());
        InstanceIdentifier<Interfaces> interfaceIid = InstanceIdentifier.create(Interfaces.class);

        WriteTransaction wt = dataBroker.newWriteOnlyTransaction();
        wt.put(LogicalDatastoreType.OPERATIONAL, interfaceIid, interfacesBuilder.build());

        try {
            wt.submit().get();
        } catch (final Exception e) {
            LOG.error("InterfaceHandler.createInterfacesData: ", e);
        }

        final InterfacesStateBuilder interfacesStateBuilder = new InterfacesStateBuilder();
        interfacesStateBuilder.setInterface(Collections.emptyList());
        InstanceIdentifier<InterfacesState> interfaceIid2 = InstanceIdentifier.create(InterfacesState.class);

        wt = dataBroker.newWriteOnlyTransaction();
        wt.put(LogicalDatastoreType.OPERATIONAL, interfaceIid2, interfacesStateBuilder.build());

        try {
            wt.submit().get();
        } catch (final Exception e) {
            LOG.error("InterfaceHandler.createInterfacesData: ", e);
        }
    }

    public void loadInterfaceDataFromNodes(final String nodeId, final IpAddress nodeIpAddress) {
        southboundCommunicationService.loadInterfaceDataFromNodes(nodeId, nodeIpAddress,
                InventoryHandler.getNodeProductName(nodeId));
    }

    public void createInterface(final String networkName, final String nodeId, final String tpId,
            final String ifRef) {
        final InterfaceBuilder interfaceBuilder = new InterfaceBuilder();
        final InterfaceKey interfaceKey = new InterfaceKey(ifRef);
        interfaceBuilder.setKey(interfaceKey);
        interfaceBuilder.setName(ifRef);
        interfaceBuilder.setEnabled(true);

        final IfRefTpRefBuilder ifRefTpRefBuilder = new IfRefTpRefBuilder();
        ifRefTpRefBuilder.setNetworkRef(new NetworkId(networkName));
        ifRefTpRefBuilder.setNodeRef(nodeId);
        ifRefTpRefBuilder.setTpRef(tpId);

        interfaceBuilder.addAugmentation(IfRefTpRef.class, ifRefTpRefBuilder.build());

        final InstanceIdentifier<Interface> interfaceIid = InstanceIdentifier.create(Interfaces.class)
                .child(Interface.class, interfaceKey);

        WriteTransaction wt = dataBroker.newWriteOnlyTransaction();
        wt.put(LogicalDatastoreType.OPERATIONAL, interfaceIid, interfaceBuilder.build());

        try {
            wt.submit().get();
        } catch (final Exception e) {
            LOG.error("InterfaceHandler.createInterface: ", e);
        }

        final StatisticsBuilder statisticsBuilder = new StatisticsBuilder();
        statisticsBuilder.setInOctets(Counter64.getDefaultInstance("0"));
        statisticsBuilder.setOutOctets(Counter64.getDefaultInstance("0"));

        final org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508
                .interfaces.state.InterfaceBuilder interfaceBuilder2 = new org.opendaylight.yang
                .gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.interfaces.state
                .InterfaceBuilder();
        interfaceBuilder2.setStatistics(statisticsBuilder.build());

        org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.interfaces
                 .state.InterfaceKey interfaceKey2 = new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns
                 .yang.ietf.interfaces.rev140508.interfaces.state.InterfaceKey(ifRef);
        interfaceBuilder2.setKey(interfaceKey2);
        interfaceBuilder2.setName(ifRef);

        final InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508
                .interfaces.state.Interface> interfaceIid2 = InstanceIdentifier.create(InterfacesState.class)
                .child(org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508
                .interfaces.state.Interface.class, interfaceKey2);

        wt = dataBroker.newWriteOnlyTransaction();
        wt.put(LogicalDatastoreType.OPERATIONAL, interfaceIid2, interfaceBuilder2.build());

        try {
            wt.submit().get();
        } catch (final Exception e) {
            LOG.error("InterfaceHandler.createInterface: ", e);
        }

        ifRef2NodeId.put(ifRef, nodeId);
    }

    public Interface readInterface(final String ifRef) {
        final InterfaceKey interfaceKey = new InterfaceKey(ifRef);

        final InstanceIdentifier<Interface> interfaceIid = InstanceIdentifier.create(Interfaces.class)
                .child(Interface.class, interfaceKey);

        Optional<Interface> resultOptional = null;
        final ReadOnlyTransaction rt = dataBroker.newReadOnlyTransaction();
        CheckedFuture<Optional<Interface>, ReadFailedException> result =
                rt.read(LogicalDatastoreType.OPERATIONAL, interfaceIid);
        try {
            resultOptional = result.get();
            if (resultOptional == null || !resultOptional.isPresent()) {
                return null;
            }
        } catch (final InterruptedException e) {
            LOG.error("InterfaceHandler.readInterface: InterruptedException", e);
            return null;
        } catch (final ExecutionException e) {
            LOG.error("InterfaceHandler.readInterface: ExecutionException", e);
            return null;
        }

        return resultOptional.get();
    }

    public Integer getBridgePortId(final String ifRef) {
        return southboundCommunicationService.getBridgePortId(ifRef);
    }

    private boolean isPeerLinkGroup(final LinkGroupInfo linkGroupInfo1, final LinkGroupInfo linkGroupInfo2) {
        return linkGroupInfo1.getSourceNodeId().equals(linkGroupInfo2.getDestNodeId()) &&
               linkGroupInfo1.getDestNodeId().equals(linkGroupInfo2.getSourceNodeId());
    }

    /*
     * returns the lag identifier of the lag peer of the given lag interface name
     */
    private String findLagPeer(final String ifRef) {
        final String linkGroupName = LagNameHandler.getLagName(ifRef);
        if (linkGroupName == null) {
            LOG.error("InterfaceHandler.findLagPeer: missing lag name for ifRef {}", ifRef);
            return null;
        }

        return findLinkGroupPeer(linkGroupName);
    }

    /*
     * returns the lag identifier of the lag peer of the given lagInfo
     */
    private String findLinkGroupPeer(final LinkGroupInfo linkGroupInfo) {
        final Set linkGroupIdSet = linkGroupMap.keySet();
        if (linkGroupIdSet == null) {
            return null;
        }
        String linkGroupPeerFound = null;
        Iterator<String> iter = linkGroupIdSet.iterator();
        while (iter.hasNext()) {
            String linkGroupId = iter.next();
            LinkGroupInfo linkGroupInfoSearch = linkGroupMap.get(linkGroupId);
            if (linkGroupInfoSearch != null && isPeerLinkGroup(linkGroupInfo, linkGroupInfoSearch)) {
                // a lag id or a bonding id (link-id) in the case
                linkGroupPeerFound = linkGroupInfoSearch.getLinkGroupId();
                break;
            }
        }

        LOG.info("InterfaceHandler.findLinkGroupPeer: peer lagId {}", linkGroupPeerFound);

        return linkGroupPeerFound;
    }

    private String findLinkGroupPeer(final String linkGroupName) {
        /* links members of the lag including member having both oper status up or down */
        final LinkGroupInfo linkGroupInfo = linkGroupMap.get(linkGroupName);
        if (linkGroupInfo == null) {
            LOG.error("InterfaceHandler.findLinkGroupPeer: missing lag info {}", linkGroupName);
            return null;
        }

        return findLinkGroupPeer(linkGroupInfo);
    }

    public void addLinkGroupConfig(final LinkGroupInfo linkGroupInfo) {
       /*
        * finding peer lag and updating peerLagId on both
        */
        final String linkGroupId = linkGroupInfo.getLinkGroupId();

        final String peerLinkGroupId = findLinkGroupPeer(linkGroupInfo);
        if (peerLinkGroupId == null) {
            LOG.info("InterfaceHandler.addLinkGroupConfig: no link group peer found for {}", linkGroupId);
            if (linkGroupMap.get(linkGroupId) == null) {
                linkGroupMap.put(linkGroupId, linkGroupInfo);
            }
            return;
        }
        LinkGroupInfo linkGroupInfo1 = linkGroupMap.get(linkGroupId);
        LinkGroupInfo linkGroupInfoPeer1 = linkGroupMap.get(peerLinkGroupId);
        if (linkGroupInfo1 != null && linkGroupInfoPeer1 != null &&
            linkGroupInfo1.getPeerLinkGroupId() != null && linkGroupInfoPeer1.getPeerLinkGroupId() != null) {
            LOG.info("InterfaceHandler.addLinkGroupConfig: data already set for {} {}", linkGroupId, peerLinkGroupId);
            return;
        }

        linkGroupInfo.setPeerLinkGroupId(peerLinkGroupId);
        linkGroupMap.put(linkGroupId, linkGroupInfo);

        LinkGroupInfo linkGroupInfoPeer = linkGroupMap.get(peerLinkGroupId);
        linkGroupInfoPeer.setPeerLinkGroupId(linkGroupId);
        linkGroupMap.put(peerLinkGroupId, linkGroupInfoPeer);

        LOG.info("InterfaceHandler.addLinkGroupConfig: linkGroupId {} peerLinkGroupId {}",
                linkGroupId, peerLinkGroupId);
    }

    public synchronized IpAddress getNodeIpAddress(final String ifRef) {
        final String sNodeId = ifRef2NodeId.get(ifRef);
        if (sNodeId == null) {
            return null;
        }

        return topologyHandler.getNodeIpAddress(sNodeId);
    }

    private Long getIfRefCurrentCapacity(final String ifRef) {
        CapacityInfo capacityInfo = ifRefCapacityInfo.get(ifRef);
        if (capacityInfo != null) {
            return capacityInfo.getCurrentCapacity();
        }

        return ZERO_LONG;
    }

    public Long getIfRefRequiredCapacity(final String ifRef) {
        CapacityInfo capacityInfo = ifRefCapacityInfo.get(ifRef);
        if (capacityInfo != null) {
            return capacityInfo.getRequiredCapacity();
        }

        return ZERO_LONG;
    }

    public DateAndTime getIfRefRequestDateAndTime(final String ifRef) {
        CapacityInfo capacityInfo = ifRefCapacityInfo.get(ifRef);
        if (capacityInfo != null) {
            return capacityInfo.getRequestDateAndTime();
        }

        return new ISO8601().dateAndTime();
    }

    public Long getIfRefStoredNominalCapacity(final String ifRef) {
        CapacityInfo capacityInfo = ifRefCapacityInfo.get(ifRef);
        if (capacityInfo != null) {
            return capacityInfo.getNominalCapacity();
        }

        return ZERO_LONG;
    }

    private void updateIfRefCurrentCapacity(final String ifRef, final Long currentCapacity) {
        CapacityInfo capacityInfo = ifRefCapacityInfo.get(ifRef);
        if (capacityInfo != null) {
            capacityInfo.setCurrentCapacity(currentCapacity);
        } else {
            capacityInfo = new CapacityInfo();
            capacityInfo.setCurrentCapacity(currentCapacity);
            capacityInfo.setRequiredCapacity(currentCapacity);
            capacityInfo.setRequestDateAndTime(new ISO8601().dateAndTime());
        }

        ifRefCapacityInfo.put(ifRef, capacityInfo);

        updateIfRefLinkBandwidth(ifRef, currentCapacity);
    }

    private void updateIfRefRequiredCapacity(final String ifRef, final Long requiredCapacity,
            final boolean actualized) {
        CapacityInfo capacityInfo = ifRefCapacityInfo.get(ifRef);
        if (capacityInfo == null) {
            capacityInfo = new CapacityInfo();
        }
        capacityInfo.setRequiredCapacity(requiredCapacity);
        capacityInfo.requestDateAndTime = new ISO8601().dateAndTime();
        ifRefCapacityInfo.put(ifRef, capacityInfo);
    }

    public void updateIfRefNominalCapacity(final String ifRef, final Long nominalCapacity) {
        CapacityInfo capacityInfo = ifRefCapacityInfo.get(ifRef);
        if (capacityInfo == null) {
            capacityInfo = new CapacityInfo();
        }
        capacityInfo.setNominalCapacity(nominalCapacity);
        ifRefCapacityInfo.put(ifRef, capacityInfo);
        LOG.info("InterfaceHandler.updateIfRefNominalCapacity: ifRef {} nominalCapacity {}",
                ifRef, nominalCapacity);
    }

    private Long getSingleInterfaceBandwidthCapacity(final String ifRef) {
        final IpAddress nodeIpAddress = getNodeIpAddress(ifRef);
        final Long currentCapacity = southboundCommunicationService.getInterfaceCurrentBandwidthCapacity(
                nodeIpAddress, ifRef, InventoryHandler.getIfRefNodeProductName(ifRef));
        updateIfRefCurrentCapacity(ifRef, currentCapacity);

        return currentCapacity;
    }

    private Long getSingleInterfaceMaximumBandwidthCapacity(final String ifRef) {
        final IpAddress nodeIpAddress = getNodeIpAddress(ifRef);
        final Long maximumCapacity = southboundCommunicationService.getInterfaceMaximumBandwidthCapacity(
                nodeIpAddress, ifRef, InventoryHandler.getIfRefNodeProductName(ifRef));

        return maximumCapacity;
    }

    public Long getInterfaceCurrentBandwidthCapacity(final String ifRef) {
        final List<String> childIfRefList = getChildIfRefList(ifRef, true);
        Long currentCapacity = ZERO_LONG;
        if (childIfRefList != null) {
            for (String childIfRef : childIfRefList) {
                currentCapacity = currentCapacity.longValue() +
                        getSingleInterfaceBandwidthCapacity(childIfRef).longValue();
            }
            updateIfRefCurrentCapacity(ifRef, currentCapacity);
            return currentCapacity;
        }

        return getSingleInterfaceBandwidthCapacity(ifRef);
    }

    public Long getInterfaceMaximumBandwidthCapacity(final String ifRef) {
        final List<String> childIfRefList = getChildIfRefList(ifRef, false);
        Long totalBandwidth = new Long(0);
        if (childIfRefList != null) {
            for (String childIfRef : childIfRefList) {
                Long ifMaximumBandwidthCapacity = getSingleInterfaceMaximumBandwidthCapacity(childIfRef);
                totalBandwidth = totalBandwidth.longValue() + ifMaximumBandwidthCapacity.longValue();

                updateIfRefNominalCapacity(ifRef, ifMaximumBandwidthCapacity);
            }
            updateIfRefNominalCapacity(ifRef, totalBandwidth);

            return totalBandwidth;
        }

        totalBandwidth = getSingleInterfaceBandwidthCapacity(ifRef);
        updateIfRefNominalCapacity(ifRef, totalBandwidth);

        return totalBandwidth;
    }

    public void setInterfaceMonitoring(final String ifRef, final Long collectionInterval,
            final boolean monitoringEnable, final Long historyLength) {
        final IpAddress nodeIpAddress = getNodeIpAddress(ifRef);

        LOG.info("InterfaceHandler.setInterfaceMonitoring: {} {} {} {} {}", ifRef,
                collectionInterval, monitoringEnable, historyLength, nodeIpAddress);

        if (ifRefThreadMap.get(ifRef) != null) {
            LOG.info("InterfaceHandler.setInterfaceMonitoring: statistics thread for if {} already started",
                    ifRef);
            return;
        }

        final RemoteStatisticsHandler remoteStatisticsHandler =
                new RemoteStatisticsHandler(southboundCommunicationService);
        final StatisticsRunnable statisticsRunnable =
                new StatisticsRunnable(nodeIpAddress, ifRef, this, collectionInterval,
                remoteStatisticsHandler, this);
        Thread thread = new Thread(statisticsRunnable);
        thread.setDaemon(true);
        thread.setName("RSH:".concat(ifRef));
        thread.start();

        ifRefThreadMap.put(ifRef, thread);
    }

    public synchronized List<String> getChildIfRefList(final String ifRef, final boolean filterOperUp) {
        /*
         * is the interface associated to a lag termination point ?
         */
        boolean isLag = LagNameHandler.isIfRefLagName(ifRef);
        if (!isLag) {
            return null;
        }

        String linkGroupName = ifRef;
        LOG.info("InterfaceHandler.getIfRefLagMembers: ifRef {} filterOperUp {} isLag {}",
                ifRef, filterOperUp, isLag);
        if (isLag) {
            linkGroupName = LagNameHandler.getLagName(ifRef);
            if (linkGroupName == null) {
                LOG.info("InterfaceHandler.getIfRefLagMembers:lag name is null for ifRef {} {}", ifRef, isLag);
                return null;
            }
        }

        /* the following list stores the linkId as string associated to the LAG members
        * having operative status as up
        */
        List<String> lChildIfRef = new ArrayList<>();

        /* the interface if-ref of the master link */
        String masterLinkIfRef = null;

        /*
         * gathering the LAG members info and identifying the master link
         */
        final LinkGroupInfo lagInfo = linkGroupMap.get(linkGroupName);
        if (lagInfo == null) {
            LOG.info("InterfaceHandler.getChildIfRefList: ifRef {} is not part of any group", ifRef);
            return null;
        }

        final List<LinkGroupMembershipInfo> lLinkGroupMemberInfo = lagInfo.getLinkGroupMembershipInfo();
        LOG.info("InterfaceHandler.getIfRefLagMembers: membersInfo {}", (lLinkGroupMemberInfo != null));
        if (lLinkGroupMemberInfo == null || lLinkGroupMemberInfo.isEmpty()) {
            LOG.error("InterfaceHandler.getChildIfRefList: lLinkGroupMemberInfo null or empty ifRef {} linkGroupName {}",
                    ifRef, linkGroupName);
            return null;
        }

        for (LinkGroupMembershipInfo linkGroupMember : lLinkGroupMemberInfo) {
            boolean operStatus = linkGroupMember.getOperStatus();
            LOG.info("InterfaceHandler.getChildIfRefList: inspecting memberships of {} operStatus {}",
                    ifRef, operStatus);
            if (!filterOperUp || (filterOperUp && operStatus)) {
                String currentIfRef = topologyHandler.getLinkSourceIfRef(linkGroupMember.getLinkId());
                if (currentIfRef != null) {
                    LOG.info("InterfaceHandler.getChildIfRefList: adding to list {}", currentIfRef);
                    lChildIfRef.add(currentIfRef);
                }
            }
        }

        return lChildIfRef;
    }

    public MonitoredDataSample getInterfaceMonitoredData(final String ifRef) {
        return statisticsHistoryHandler.getIfRefMonitoredData(ifRef);
    }

    private boolean checkIncrease(final String ifRef, final Long requiredCapacity) {
       final CapacityInfo capacityInfo = ifRefCapacityInfo.get(ifRef);
       Long currentCapacity = new Long(0);
       if (capacityInfo != null) {
           currentCapacity = capacityInfo.getCurrentCapacity();
       }
       LOG.info("InterfaceHandler.checkIncrease: ifRef {} currentCapacity {} requiredCapacity {}",
               ifRef, currentCapacity, requiredCapacity);

       return (requiredCapacity.intValue() > currentCapacity.intValue());
    }

    private boolean isLinkGroup(final String ifRef) {
         boolean isLag = LagNameHandler.isIfRefLagName(ifRef);
         if (isLag) {
             return true;
         }
         final Link link = topologyHandler.getIfRefLink(ifRef);
         final LinkGroupInfo linkGroupInfo = linkGroupMap.get(link.getLinkId().getValue());

         return linkGroupInfo != null;
    }

    public boolean setInterfaceCapacity(final String ifRef, final Long requiredCapacity) {
       updateIfRefRequiredCapacity(ifRef, requiredCapacity, false);
       final Long finalCapacity = computeFinalCapacity(ifRef, requiredCapacity);
       LOG.info("InterfaceHandler.setInterfaceCapacity: ifRef {} requiredCapacity {} finalCapacity {}",
               ifRef, requiredCapacity, finalCapacity);
       if (isLinkGroup(ifRef)) {
           return setLinkGroupCapacity(ifRef, finalCapacity);
       }

       return setLinkPairCapacity(ifRef, finalCapacity);
    }

    private Long computeFinalCapacity(final String ifRef, final Long requiredCapacity) {
        final Link link = topologyHandler.getIfRefLink(ifRef);
        final Link peerLink = topologyHandler.getPeerLink(link.getLinkId().getValue());
        final String ifRefPeer = topologyHandler.getLinkSourceIfRef(peerLink);
        final Long peerRequiredCapacity = getIfRefRequiredCapacity(ifRefPeer);
        LOG.info("InterfaceHandler.computeFinalCapacity: ifRef {} requiredCapacity {} peerRequiredCapacity {}",
               ifRef, requiredCapacity, peerRequiredCapacity);
        return new Long(Math.max(peerRequiredCapacity.longValue(), requiredCapacity.longValue()));
    }

    private boolean setLinkPairCapacity(final String ifRef, final Long requiredCapacity) {
        final Link link = topologyHandler.getIfRefLink(ifRef);
        final Link peerLink = topologyHandler.getPeerLink(link.getLinkId().getValue());
        final String ifRefPeer = topologyHandler.getLinkSourceIfRef(peerLink);
        boolean result = setLinkCapacity(ifRef, requiredCapacity);
        LOG.info("InterfaceHandler.setLinkPairCapacity: ifRef {} requiredCapacity {} result {}",
                ifRef, requiredCapacity, result);
        if (result) {
            result = setLinkCapacity(ifRefPeer, requiredCapacity);
             LOG.info("InterfaceHandler.setLinkPairCapacity: ifRefPeer {} requiredCapacity {} result {}",
                     ifRefPeer, requiredCapacity, result);
        }

        return result;
    }

    private boolean setLinkCapacity(final String ifRef, final Long requiredCapacity) {
       final IpAddress nodeIpAddress = getNodeIpAddress(ifRef);

       Long currentCapacity = getIfRefCurrentCapacity(ifRef);
       boolean increaseFlag = false;
       if (currentCapacity.longValue() == requiredCapacity.longValue()) {
	       return true;
       }
       if (currentCapacity.longValue() < requiredCapacity.longValue()) {
           increaseFlag = true;
       }

       final Integer currentChannelSpacing = southboundCommunicationService
               .getInterfaceCurrentChannelSpacing(nodeIpAddress, ifRef,
               InventoryHandler.getIfRefNodeProductName(ifRef));

       final Integer currentAcm = southboundCommunicationService
               .getInterfaceSelectedMaxAcm(nodeIpAddress, ifRef,
               InventoryHandler.getIfRefNodeProductName(ifRef));

       LOG.info("InterfaceHandler.setLinkCapacity: ifRef {} currentCapacity {} requiredCapacity {} increaseFlag {} currentChannelSpacing {} currentAcm {}",
               ifRef, currentCapacity, requiredCapacity, increaseFlag, currentChannelSpacing, currentAcm);

       final MiniLink_ChannelSpacing csEnum = MiniLink_ChannelSpacing.forCode(currentChannelSpacing);
       final MiniLink_ACM acmEnum = MiniLink_ACM.forCode(currentAcm);
       final MiniLink_ACM_Flavour acmFlavour = acmEnum.getFlavour();

       Integer currentBandwidthMbps = inventoryBandwidth.getMiniLinkBandwidthMbps(csEnum, acmEnum,
               InventoryHandler.getIfRefNodeProductName(ifRef));

       Integer currentTargetInputPowerFarEnd =
               southboundCommunicationService.getInterfaceCurrenTargetInputPowerFarEnd(
               nodeIpAddress, ifRef, InventoryHandler.getIfRefNodeProductName(ifRef));

       final MiniLinkBandwidthCoordinates coordinates = inventoryBandwidth.findCoordinates(csEnum,
               requiredCapacity, acmFlavour, InventoryHandler.getIfRefNodeProductName(ifRef));
       if (coordinates == null) {
           LOG.error("InterfaceHandler.setLinkCapacity: coordinates are null");
           return false;
       }

       LOG.info("InterfaceHandler.setLinkCapacity: coordinates {] {} {}",
               coordinates.getChannelSpacing(), coordinates.getAcm(), coordinates.getBandwidth());

       if (increaseFlag) {
            Integer newBandwidthMbps = inventoryBandwidth.getMiniLinkBandwidthMbps(
                    coordinates.getChannelSpacing(), coordinates.getAcm(),
                    InventoryHandler.getIfRefNodeProductName(ifRef));

            Integer powerDifference = inventoryBandwidth.getMiniLinkInputTargetPowerReduction(
                    coordinates.getChannelSpacing(), currentBandwidthMbps, newBandwidthMbps,
                    InventoryHandler.getIfRefNodeProductName(ifRef));

            int newPower = currentTargetInputPowerFarEnd.intValue() + powerDifference.intValue();

            LOG.info("InterfaceHandler.setLinkCapacity: ifRef {} currentBandwidthMbps {} newBandwidthMbps {} currentTargetInputPowerFarEnd {} powerDifference {} newPower {}",
               ifRef, currentBandwidthMbps, newBandwidthMbps, currentTargetInputPowerFarEnd, powerDifference, newPower);

            southboundCommunicationService.setInterfaceCurrenTargetInputPowerFarEnd(nodeIpAddress, ifRef,
                    new Integer(newPower), InventoryHandler.getIfRefNodeProductName(ifRef));

            southboundCommunicationService.setInterfaceSelectedMaxAcm(nodeIpAddress, ifRef,
                    coordinates.getAcm().getValue(), coordinates.getAcm().getValueAsString(),
                    InventoryHandler.getIfRefNodeProductName(ifRef));
       } else {
            southboundCommunicationService.setInterfaceSelectedMaxAcm(nodeIpAddress, ifRef,
                    coordinates.getAcm().getValue(), coordinates.getAcm().getValueAsString(),
                    InventoryHandler.getIfRefNodeProductName(ifRef));

            Integer newBandwidthMbps = inventoryBandwidth.getMiniLinkBandwidthMbps(
                    coordinates.getChannelSpacing(), coordinates.getAcm(),
                    InventoryHandler.getIfRefNodeProductName(ifRef));

            /* decreasing bandwidth the lower value is the newBandwidthMbps */
            Integer powerDifference = inventoryBandwidth.getMiniLinkInputTargetPowerReduction(
                    coordinates.getChannelSpacing(), newBandwidthMbps, currentBandwidthMbps,
                    InventoryHandler.getIfRefNodeProductName(ifRef));

            int newPower = currentTargetInputPowerFarEnd.intValue() - powerDifference.intValue();

            LOG.info("InterfaceHandler.setLinkCapacity: ifRef {} currentBandwidthMbps {} newBandwidthMbps {} currentTargetInputPowerFarEnd {} powerDifference {} newPower {}",
                    ifRef, currentBandwidthMbps, newBandwidthMbps, currentTargetInputPowerFarEnd, powerDifference, newPower);

            southboundCommunicationService.setInterfaceCurrenTargetInputPowerFarEnd(nodeIpAddress, ifRef,
                    new Integer(newPower), InventoryHandler.getIfRefNodeProductName(ifRef));
       }

       updateIfRefCurrentCapacity(ifRef, requiredCapacity);

       return true;
    }

    public boolean setLinkGroupCapacity(final String ifRef, final Long requiredCapacity) {
        /*
         * enquiring if it is LAG scenario
         */
        boolean isLag = LagNameHandler.isIfRefLagName(ifRef);
        boolean increase = checkIncrease(ifRef, requiredCapacity);
        LOG.info("InterfaceHandler.setLinkGroupCapacity: ifRef {} isLag {} increase {}", ifRef, isLag, increase);

        String linkGroupName = null;
        String linkGroupNamePeer = null;
        String ifRefPeer = null;

        if (isLag) {
            ifRefPeer = findLagPeer(ifRef);
            LOG.info("InterfaceHandler.setLinkGroupCapacity: ifRef {} peer ifRef {}",
                    ifRef, ifRefPeer);
            if (ifRefPeer != null) {
                linkGroupName = LagNameHandler.getLagName(ifRef);
                if (linkGroupName == null) {
                    LOG.error("InterfaceHandler.setLinkGroupCapacity: missing linkGroupName name for ifRef {}", ifRef);
                    return false;
                }
                linkGroupNamePeer = LagNameHandler.getLagName(ifRefPeer);
                if (linkGroupName == null) {
                    LOG.error("InterfaceHandler.setLinkGroupCapacity: missing linkGroupNamePeer name for ifRef {}", ifRef);
                    return false;
                }
            }
        } else {
            final Link link = topologyHandler.getIfRefLink(ifRef);
            if (link == null) {
                LOG.warn("InterfaceHandler.setLinkGroupCapacity: no link housing interface {}", ifRef);
                linkGroupName = ifRef;
            } else {
                linkGroupName = link.getLinkId().getValue();
            }
            ifRefPeer = findLinkGroupPeer(linkGroupName);
            linkGroupNamePeer = ifRefPeer;
            LOG.info("InterfaceHandler.setLinkGroupCapacity: ifRef {} ifRefPeer {} linkGroupNamePeer {}",
                    ifRef, ifRefPeer, linkGroupNamePeer);
        }

        setLinkGroupInterfaceCapacity(linkGroupNamePeer, isLag, ifRefPeer, requiredCapacity);

        syncLagInterfaceCapacity(linkGroupName, isLag, ifRef, ifRefPeer, linkGroupNamePeer,
              increase, requiredCapacity);

        return true;
    }

    private boolean syncLagInterfaceCapacity(final String linkGroupName, final boolean isLag,
            final String ifRef, final String lagIfRefPeer, final String linkGroupNamePeer,
            final boolean increase, final Long requiredCapacity) {
        final LinkGroupInfo linkGroupInfoPeer = linkGroupMap.get(linkGroupNamePeer);
        if (linkGroupInfoPeer == null) {
            LOG.error("InterfaceHandler.syncLagInterfaceCapacity: missing linkGroupInfo for peer {}", linkGroupNamePeer);
            return false;
        }
        final ArrayList<LinkGroupMembershipInfo> lLinkGroupMemberInfoPeer =
                (ArrayList<LinkGroupMembershipInfo>)linkGroupInfoPeer.getLinkGroupMembershipInfo().clone();

        LOG.info("InterfaceHandler.syncLagInterfaceCapacity: linkGroupName {} membersInfo {}",
               linkGroupName, (lLinkGroupMemberInfoPeer != null));

        if (lLinkGroupMemberInfoPeer == null || lLinkGroupMemberInfoPeer.isEmpty()) {
            LOG.error("InterfaceHandler.syncLagInterfaceCapacity: missing lag info {} {} {}",
                   linkGroupName, (lLinkGroupMemberInfoPeer != null), lLinkGroupMemberInfoPeer.isEmpty());
            return false;
        }

        LinkGroupInfo linkGroupInfo = linkGroupMap.get(linkGroupName);
        if (linkGroupInfo == null) {
            LOG.error("InterfaceHandler.syncLagInterfaceCapacity: missing linkGroupInfo for {}", linkGroupName);
            return false;
        }
        final ArrayList<LinkGroupMembershipInfo> lLinkGroupMemberInfo =
                (ArrayList<LinkGroupMembershipInfo>)linkGroupInfo.getLinkGroupMembershipInfo().clone();

        LOG.info("InterfaceHandler.syncLagInterfaceCapacity: linkGroupName {} membersInfo {}",
               linkGroupName, (lLinkGroupMemberInfo != null));

        if (lLinkGroupMemberInfo == null || lLinkGroupMemberInfo.isEmpty()) {
            LOG.error("InterfaceHandler.syncLagInterfaceCapacity: missing lLinkGroupMemberInfo for {} {} {}",
                   linkGroupName, (lLinkGroupMemberInfo != null), lLinkGroupMemberInfo.isEmpty());
            return false;
        }

        String masterLinkIfRef = null;
        List<String> ifRefListToTurnOn = new ArrayList<>();
        List<String> ifRefListToTurnOff = new ArrayList<>();
        List<String> memberLinkOperUpList = new ArrayList<>();

        for (LinkGroupMembershipInfo linkGroupMemberPeer : lLinkGroupMemberInfoPeer) {
            boolean operStatusPeer = linkGroupMemberPeer.getOperStatus();
            String currentIfRefPeer = topologyHandler.getLinkSourceIfRef(linkGroupMemberPeer.getLinkId());
            String linkIdPeer = linkGroupMemberPeer.getLinkId();
            LOG.info("InterfaceHandler.syncLagInterfaceCapacity: currentIfRefPeer {} operStatusPeer {}",
                    currentIfRefPeer, operStatusPeer);
            for (LinkGroupMembershipInfo linkGroupMember : lLinkGroupMemberInfo) {
                boolean operStatus = linkGroupMember.getOperStatus();
                String currentIfRef = topologyHandler.getLinkSourceIfRef(linkGroupMember.getLinkId());
                if (linkGroupMember.isMaster()) {
                    masterLinkIfRef = currentIfRef;
                }
                String linkIdToMatch = topologyHandler.getPeerLinkId(linkGroupMember.getLinkId());
                if (linkIdToMatch.equals(linkIdPeer)) {
                   if (operStatusPeer) {
                       LOG.info("InterfaceHandler.syncLagInterfaceCapacity: ifRefListToTurnOn {}", currentIfRef);
                       ifRefListToTurnOn.add(currentIfRef);
                       memberLinkOperUpList.add(linkGroupMember.getLinkId());
                   } else {
                       LOG.info("InterfaceHandler.syncLagInterfaceCapacity: ifRefListToTurnOff {}", currentIfRef);
                       ifRefListToTurnOff.add(currentIfRef);
                   }
                }
            }
        }

        if (increase && !ifRefListToTurnOn.isEmpty()) {
            /* ifrefList stores both the new ifref lag membership (new or removed entities) */
            if (isLag && !ifRefListToTurnOn.isEmpty()) {
                boolean result = setInterfacesToLag(ifRefListToTurnOn, masterLinkIfRef);
                if (!result) {
                    LOG.error("InterfaceHandler.syncLagInterfaceCapacity: setInterfacesToLag failed, masterLinkIfRef {}",
                            masterLinkIfRef);
                    return false;
                }
                /* turning admin status off for interfaces not to be lag members */
                setStackInterfacesAdminStatus(ifRefListToTurnOn, masterLinkIfRef, true, isLag);
            } else {
                setInterfaceAdminStatus(ifRefListToTurnOn, true);
            }
        } else {
            /* turning admin status off for interfaces not to be lag members */
           if (!ifRefListToTurnOff.isEmpty()) {
               if (isLag) {
                   setStackInterfacesAdminStatus(ifRefListToTurnOff, masterLinkIfRef, false, isLag);
               } else {
                   setInterfaceAdminStatus(ifRefListToTurnOff, false);
               }
           }
           if (isLag && !ifRefListToTurnOn.isEmpty()) {
               boolean result = setInterfacesToLag(ifRefListToTurnOn, masterLinkIfRef);
               if (!result) {
                    LOG.error("InterfaceHandler.syncLagInterfaceCapacity: setInterfacesToLag failed, masterLinkIfRef {}",
                            masterLinkIfRef);
                    return false;
               }
           }
        }

        ArrayList<LinkGroupMembershipInfo> lNewList = new ArrayList<>();
        for (LinkGroupMembershipInfo member : lLinkGroupMemberInfo) {
            if (memberLinkOperUpList.contains(member.getLinkId())) {
                lNewList.add(new LinkGroupMembershipInfo(member.getLinkId(), member.isMaster(), true, member.getCapacity()));
                updateLinkOperStatus(member.getLinkId(), true);
                LOG.info("InterfaceHandler.syncLagInterfaceCapacity: set oper status up {}", member.getLinkId());
            } else {
                lNewList.add(new LinkGroupMembershipInfo(member.getLinkId(), member.isMaster(), false, member.getCapacity()));
                updateLinkOperStatus(member.getLinkId(), false);
                LOG.info("InterfaceHandler.syncLagInterfaceCapacity: set oper status down {}", member.getLinkId());
            }
        }

        linkGroupInfo = linkGroupMap.get(linkGroupName);
        linkGroupInfo.setLinkGroupMembershipInfo(lNewList);

        Long peerCurrentCapacity = getIfRefCurrentCapacity(lagIfRefPeer);
        if (peerCurrentCapacity != null) {
            updateIfRefCurrentCapacity(ifRef, peerCurrentCapacity);
        }

        return true;
    }

    private boolean setLinkGroupInterfaceCapacity(final String linkGroupName, final boolean isLag,
            final String ifRef, final Long requiredCapacity) {
        /* the interface if-ref and the master link id */
        String masterLinkIfRef = null;
        String masterLinkId = null;

        /* links members of the lag including member having both oper status up or down */
        final LinkGroupInfo linkGroupInfo = linkGroupMap.get(linkGroupName);
        if (linkGroupInfo == null) {
            LOG.error("InterfaceHandler.setLinkGroupInterfaceCapacity: missing lag info {}", linkGroupName);
            return false;
        }
        final ArrayList<LinkGroupMembershipInfo> lLinkGroupMemberInfo = linkGroupInfo.getLinkGroupMembershipInfo();

        LOG.info("InterfaceHandler.setLinkGroupInterfaceCapacity: linkGroupName {} membersInfo {}",
               linkGroupName, (lLinkGroupMemberInfo != null));

        if (lLinkGroupMemberInfo == null || lLinkGroupMemberInfo.isEmpty()) {
            LOG.error("InterfaceHandler.setLinkGroupInterfaceCapacity: missing membership info {} {} {}",
                   linkGroupName, (lLinkGroupMemberInfo != null), lLinkGroupMemberInfo.isEmpty());
            return false;
        }

        /*
         * computing current bandwidth and determining number of links
         * needed to support required capacity
         */
         Long totBandwidthOperUp = new Long(0);
         List<String> ifRefOperUpList = new ArrayList<>();
         ArrayList<String> memberLinkOperUpList = new ArrayList<>();

         for (LinkGroupMembershipInfo linkGroupMember : lLinkGroupMemberInfo) {
             if (linkGroupMember.isMaster()) {
                 TeLinkAttributes teLinkAttributes =
                         topologyHandler.getTeLinkAttributes(linkGroupMember.getLinkId());
                 if (teLinkAttributes == null) {
                     continue;
                 }
                 TeBandwidth teBandwidth = teLinkAttributes.getMaxLinkBandwidth();
                 if (teBandwidth == null) {
                     continue;
                 }
                 totBandwidthOperUp = totBandwidthOperUp.longValue() + new Long(teBandwidth.getValue()).longValue();

                 masterLinkIfRef = topologyHandler.getLinkSourceIfRef(linkGroupMember.getLinkId());
                 masterLinkId = linkGroupMember.getLinkId();

                 ifRefOperUpList.add(masterLinkIfRef);
                 memberLinkOperUpList.add(masterLinkId);

                 LOG.info("InterfaceHandler.setLinkGroupInterfaceCapacity: masterLinkId {} totBandwidthOperUp {}",
                         masterLinkId, totBandwidthOperUp);
                 break;
             }
         }

         for (LinkGroupMembershipInfo linkGroupMember : lLinkGroupMemberInfo) {
             LOG.info("InterfaceHandler.setLinkGroupInterfaceCapacity: inspecting link memberships of ifRef {} linkId {} operStatus {}",
                     ifRef, linkGroupMember.getLinkId(), linkGroupMember.getOperStatus());
             if (linkGroupMember.isMaster() || !linkGroupMember.getOperStatus()) {
                 continue;
             }
             TeLinkAttributes teLinkAttributes =
                     topologyHandler.getTeLinkAttributes(linkGroupMember.getLinkId());
             if (teLinkAttributes == null) {
                 continue;
             }
             TeBandwidth teBandwidth = teLinkAttributes.getMaxLinkBandwidth();
             if (teBandwidth == null) {
                 continue;
             }
             String currentIfRef = topologyHandler.getLinkSourceIfRef(linkGroupMember.getLinkId());
             if (currentIfRef != null) {
                 totBandwidthOperUp = totBandwidthOperUp.longValue() + new Long(teBandwidth.getValue()).longValue();
                 ifRefOperUpList.add(currentIfRef);
                 memberLinkOperUpList.add(linkGroupMember.getLinkId());
             }
         }

         LOG.info("InterfaceHandler.setLinkGroupInterfaceCapacity: masterLinkIfRef {} masterLinkId {} totBandwidthOperUp {} requiredCapacity {}",
                masterLinkIfRef, masterLinkId, totBandwidthOperUp, requiredCapacity);

         if (totBandwidthOperUp.intValue() < requiredCapacity.intValue()) {
             increaseLinkGroupBandwidth(ifRef, linkGroupName, isLag,
                    (ArrayList<LinkGroupMembershipInfo>)lLinkGroupMemberInfo.clone(),
                     masterLinkIfRef, masterLinkId, ifRefOperUpList, memberLinkOperUpList,
                     totBandwidthOperUp, requiredCapacity);
         } else if (totBandwidthOperUp.intValue() > requiredCapacity.intValue()) {
             decreaseLinkGroupBandwidth(ifRef, linkGroupName, isLag,
                     (ArrayList<LinkGroupMembershipInfo>)lLinkGroupMemberInfo.clone(),
                     masterLinkIfRef, masterLinkId, ifRefOperUpList, memberLinkOperUpList,
                     totBandwidthOperUp, requiredCapacity);
         }

         return true;
    }

    private boolean increaseLinkGroupBandwidth(
            final String linkGroupIfRef,
            final String linkGroupName,
            final boolean isLag,
            final ArrayList<LinkGroupMembershipInfo> lLinkGroupMemberInfo,
            final String masterLinkIfRef,
            final String masterLinkId,
            final List<String> ifRefOperUpList,
            final ArrayList<String> memberLinkOperUpList,
            final Long totBandwidthOperUp,
            final Long requiredCapacity) {
        /*
         * list of the interfaces to be added to cover the bandwidth gap
         */
        Long bandwidthToSet = totBandwidthOperUp;
        /*
         * already adding current ifRef lag members having oper status up
         */

        List<String> ifRefListToTurnOn = new ArrayList<>();
        /*
         * looping on oper status down interfaces to gather more bandwidth
        */
        for (LinkGroupMembershipInfo linkGroupMember : lLinkGroupMemberInfo) {
             LOG.info("InterfaceHandler.setLinkGroupInterfaceCapacity: inspecting link memberships of ifRef {} linkId {} operStatus {}",
                     linkGroupIfRef, linkGroupMember.getLinkId(), linkGroupMember.getOperStatus());
            if (!linkGroupMember.getOperStatus()) {
                TeLinkAttributes teLinkAttributes =
                        topologyHandler.getTeLinkAttributes(linkGroupMember.getLinkId());
                if (teLinkAttributes == null) {
                    LOG.error("InterfaceHandler.increaseLinkGroupBandwidth: missing teLinkAttributes {}",
                            linkGroupMember.getLinkId());
                    continue;
                }
                String currentIfRef = topologyHandler.getLinkSourceIfRef(linkGroupMember.getLinkId());
                if (currentIfRef == null) {
                    LOG.error("InterfaceHandler.increaseLinkGroupBandwidth: missing currentIfRef {}",
                            linkGroupMember.getLinkId());
                    continue;
                }
                TeBandwidth teBandwidth = teLinkAttributes.getMaxLinkBandwidth();
                if (teBandwidth == null) {
                    LOG.error("InterfaceHandler.increaseLinkGroupBandwidth: missing teBandwidth {}",
                            linkGroupMember.getLinkId());
                    continue;
                }

                Long currentIfRefBandwidth = new Long(teBandwidth.getValue());
                bandwidthToSet = bandwidthToSet.longValue() + currentIfRefBandwidth.longValue();

                LOG.info("InterfaceHandler.increaseLinkGroupBandwidth: adding currentIfRef {} bandwidthToSet {} requiredCapacity {}",
                        currentIfRef, bandwidthToSet, requiredCapacity);

               /*
                * updating both ifref list and links to have oper status up
                */
                ifRefOperUpList.add(currentIfRef);
                ifRefListToTurnOn.add(currentIfRef);
                memberLinkOperUpList.add(linkGroupMember.getLinkId());

                if (bandwidthToSet.intValue() > requiredCapacity.intValue()) {
                    break;
                }
            }
        }

        if (ifRefOperUpList.isEmpty() && bandwidthToSet.intValue() >= requiredCapacity.intValue()) {
            LOG.error("InterfaceHandler.increaseLinkGroupBandwidth: no link members found to cover bandwidth gap, masterLinkIfRef {} totBandwidthOperUp {} bandwidthToSet {} requiredCapacity {}",
                    masterLinkIfRef, totBandwidthOperUp, bandwidthToSet, requiredCapacity);
            return true;
        } else if (ifRefOperUpList.isEmpty() && bandwidthToSet.intValue() < requiredCapacity.intValue()) {
            LOG.error("InterfaceHandler.increaseLinkGroupBandwidth: no link members found to cover bandwidth gap, masterLinkIfRef {} totBandwidthOperUp {} bandwidthToSet {} requiredCapacity {}",
                    masterLinkIfRef, totBandwidthOperUp, bandwidthToSet, requiredCapacity);
            return false;
        }

        if (isLag) {
            /* ifrefList stores both the new ifref lag membership (new or removed entities) */
            boolean result = setInterfacesToLag(ifRefOperUpList, masterLinkIfRef);
            if (!result) {
                LOG.error("InterfaceHandler.increaseLinkGroupBandwidth:: setInterfacesToLag failed, masterLinkIfRef {}",
                        masterLinkIfRef);
                return false;
            }

            /* turning admin status off for interfaces not to be lag members */
            setStackInterfacesAdminStatus(ifRefListToTurnOn, masterLinkIfRef, true, isLag);
        } else {
            setInterfaceAdminStatus(ifRefListToTurnOn, true);
        }

        ArrayList<LinkGroupMembershipInfo> lNewList = new ArrayList<>();
        for (LinkGroupMembershipInfo member : lLinkGroupMemberInfo) {
            if (memberLinkOperUpList.contains(member.getLinkId())) {
                LOG.info("InterfaceHandler.increaseLinkGroupBandwidth: set oper status up {}", member.getLinkId());
                lNewList.add(new LinkGroupMembershipInfo(member.getLinkId(), member.isMaster(), true, member.getCapacity()));
                updateLinkOperStatus(member.getLinkId(), true);
            } else {
				LOG.info("InterfaceHandler.increaseLinkGroupBandwidth: set oper status down {}", member.getLinkId());
                lNewList.add(new LinkGroupMembershipInfo(member.getLinkId(), member.isMaster(), false, member.getCapacity()));
			}
        }

        final LinkGroupInfo linkGroupInfo = linkGroupMap.get(linkGroupName);
        linkGroupInfo.setLinkGroupMembershipInfo(lNewList);

        updateIfRefCurrentCapacity(linkGroupIfRef, bandwidthToSet);

        return true;
    }

    private boolean decreaseLinkGroupBandwidth(
            final String linkGroupIfRef,
            final String linkGroupName,
            final boolean isLag,
            final ArrayList<LinkGroupMembershipInfo> lLinkGroupMemberInfo,
            final String masterLinkIfRef,
            final String masterLinkId,
            final List<String> ifRefOperUpList,
            final ArrayList<String> memberLinkOperUpList,
            final Long totBandwidthOperUp,
            final Long requiredCapacity) {
        final List<String> ifRefListToTurnOff = new ArrayList<>();

        /* bandwidth variable accumulating new values */
        Long bandwidthToSet = totBandwidthOperUp;
        ArrayList<String> memberLinkOperUpList_copy = (ArrayList<String>)memberLinkOperUpList.clone();
        Integer memberTotalCount = memberLinkOperUpList.size();
        Integer memberCount = 0;

        LOG.info("InterfaceHandler.decreaseLinkGroupBandwidth: linkGroupIfRef {} linkGroupName {} isLag {} masterLinkIfRef {} masterLinkId {}",
                   linkGroupIfRef, linkGroupName, isLag, masterLinkIfRef, masterLinkId);

        for (String member : memberLinkOperUpList) {
           if(masterLinkId != null && member.equals(masterLinkId)) {
               LOG.debug("InterfaceHandler.decreaseLinkGroupBandwidth: masterLinkId {} member {}",
                   masterLinkId, member);
               continue;
           }
           memberCount = memberCount + 1;

           TeLinkAttributes teLinkAttributes =
               topologyHandler.getTeLinkAttributes(member);
           if (teLinkAttributes == null) {
               LOG.error("InterfaceHandler.decreaseLinkGroupBandwidth: missing teLinkAttributes {}",
                   member);
               continue;
           }

           TeBandwidth teBandwidth = teLinkAttributes.getMaxLinkBandwidth();
           if (teBandwidth == null) {
               LOG.error("InterfaceHandler.decreaseLinkGroupBandwidth: missing teBandwidth {}", member);
               continue;
            }

            LOG.info("InterfaceHandler.decreaseLinkGroupBandwidth: masterLinkId {} memberCount {} memberTotalCount {}",
                   masterLinkId, memberCount, memberTotalCount);

            /*
             * keep at least one link in case no master link is defined
             */

            if (masterLinkId == null && memberCount.equals(memberTotalCount)) {
                break;
            }

            Long currentIfRefBandwidth = new Long(teBandwidth.getValue());
            if (currentIfRefBandwidth.longValue() < bandwidthToSet.longValue()) {
                long difference = bandwidthToSet.longValue() - currentIfRefBandwidth.longValue();
                if (difference < requiredCapacity.longValue()) {
                   break;
                }
            }

            String currentIfRef = topologyHandler.getLinkSourceIfRef(member);
            if (currentIfRef == null) {
                LOG.error("InterfaceHandler.decreaseLinkGroupBandwidth: missing currentIfRef {}", member);
                continue;
            }

            bandwidthToSet = bandwidthToSet - currentIfRefBandwidth;

            LOG.info("InterfaceHandler.decreaseLinkGroupBandwidth: to turn off currentIfRef {} linkId {}",
                   currentIfRef, member);

            ifRefListToTurnOff.add(currentIfRef);
            ifRefOperUpList.remove(currentIfRef);
            memberLinkOperUpList_copy.remove(member);
        }

        LOG.info("InterfaceHandler.decreaseLinkGroupBandwidth: bandwidthToSet {} requiredCapacity {}",
                   bandwidthToSet, requiredCapacity);

        if (!ifRefListToTurnOff.isEmpty()) {
            for (String toTurnOff : ifRefListToTurnOff) {
                LOG.info("InterfaceHandler.decreaseLinkGroupBandwidth: toTurnOff {}", toTurnOff);
            }
        }

        if (isLag) {
           /* turning admin status off for interfaces not to be lag members */

            setStackInterfacesAdminStatus(ifRefListToTurnOff, masterLinkIfRef, false, isLag);

            /* ifrefList stores both the new ifref lag memebership (new or removed entities) */
            boolean result = setInterfacesToLag(ifRefOperUpList, masterLinkIfRef);
            if (!result) {
                LOG.error("InterfaceHandler.decreaseLinkGroupBandwidth: setInterfacesToLag failed, masterLinkIfRef {}",
                        masterLinkIfRef);
                return false;
            }
        } else {
            setInterfaceAdminStatus(ifRefListToTurnOff, false);
        }

        ArrayList<LinkGroupMembershipInfo> lNewList = new ArrayList<>();
        for (LinkGroupMembershipInfo member : lLinkGroupMemberInfo) {
            if (memberLinkOperUpList_copy.contains(member.getLinkId())) {
                LOG.info("InterfaceHandler.decreaseLinkGroupBandwidth: set oper status up {}", member.getLinkId());
                lNewList.add(new LinkGroupMembershipInfo(member.getLinkId(), member.isMaster(), true, member.getCapacity()));
                updateLinkOperStatus(member.getLinkId(), true);
            } else {
                LOG.info("InterfaceHandler.decreaseLinkGroupBandwidth: set oper status down {}", member.getLinkId());
                lNewList.add(new LinkGroupMembershipInfo(member.getLinkId(), member.isMaster(), false, member.getCapacity()));
                updateLinkOperStatus(member.getLinkId(), false);
            }
        }

        final LinkGroupInfo linkGroupInfo = linkGroupMap.get(linkGroupName);
        linkGroupInfo.setLinkGroupMembershipInfo(lNewList);

        updateIfRefCurrentCapacity(linkGroupIfRef, bandwidthToSet);

        return true;
    }

    private void updateLinkOperStatus(final String linkId, final boolean statusUp) {
        TopologyUpdater topologyUpdater = topologyHandler.getTopologyUpdater();
        if (topologyUpdater != null) {
            LOG.info("InterfaceHandler.updateLinkOperStatus: linkId {} statusUp {}",
                    linkId, statusUp);
            topologyUpdater.enqueueOperation(new TopologyUpdateData(
                    TopologyUpdateData.UpdateType.OperStatusUpdate, linkId, statusUp));
        }

    }

    public void updateIfRefLinkOperStatus(final String ifRef, final String operStatus) {
        final Link link = topologyHandler.getIfRefLink(ifRef);
        if (link == null || operStatus == null) {
            LOG.warn("InterfaceHandler.updateIfRefLinkOperStatus: link or operStatus are null");
            return;
        }
        final String linkId = link.getLinkId().getValue();
        TopologyUpdater topologyUpdater = topologyHandler.getTopologyUpdater();
        if (topologyUpdater != null) {
            LOG.info("InterfaceHandler.updateIfRefLinkOperStatus: linkId {} operStatus {}",
                    linkId, operStatus);
            boolean statusUp = operStatus.equals("up");
            topologyUpdater.enqueueOperation(new TopologyUpdateData(
                    TopologyUpdateData.UpdateType.OperStatusUpdate, linkId, statusUp));
        }

    }

    private void updateIfRefLinkBandwidth(final String ifRef, final Long capacity) {
        TopologyUpdater topologyUpdater = topologyHandler.getTopologyUpdater();
        if (topologyUpdater != null) {
            topologyUpdater.enqueueOperation(new TopologyUpdateData(
                    TopologyUpdateData.UpdateType.BandwidthUpdate, ifRef, capacity));
        }
    }

    private boolean setInterfaceAdminStatus(final List<String> ifRefList, final boolean txOn) {
        for (String ifRef : ifRefList) {
            LOG.info("InterfaceHandler.setInterfaceAdminStatus: {} {}", ifRef, txOn);
        }
        boolean result = true;
        for (String ifRef : ifRefList) {
            final IpAddress nodeIpAddress = getNodeIpAddress(ifRef);
            result = result && southboundCommunicationService.setInterfaceAdminStatus(nodeIpAddress, ifRef, txOn);
        }

        return result;
    }

    private boolean setStackInterfacesAdminStatus(final List<String> ifRefList, final String masterLinkIfRef,
           final boolean txOn, final boolean isLag) {
        final IpAddress nodeIpAddress = getNodeIpAddress(masterLinkIfRef);
        for (String ifRef : ifRefList) {
            LOG.info("InterfaceHandler.setStackInterfacesAdminStatus: {} {}", ifRef, txOn);
        }

        return southboundCommunicationService.setStackInterfacesAdminStatus(nodeIpAddress, ifRefList,
                txOn, isLag, true);
    }

    private boolean setInterfacesToLag(final List<String> ifRefList, final String masterLinkIfRef) {
        if (ifRefList == null || ifRefList.isEmpty()) {
            LOG.info("InterfaceHandler.setInterfacesToLag: ifRefList null masterLinkIfRef {}",
                    masterLinkIfRef);
            return false;
        }
        for (String ifRef : ifRefList) {
            LOG.info("InterfaceHandler.addInterfacesToLag: {}", ifRef);
        }
        final IpAddress nodeIpAddress = getNodeIpAddress(masterLinkIfRef);

        return southboundCommunicationService.setInterfacesToLag(nodeIpAddress, ifRefList,
                masterLinkIfRef);
    }

    @Override
    public synchronized void notifyData(final StatisticsSample sample) {
        LOG.info("InterfaceHandler.notifyData: ifRef {}", sample.getIfRef());
        statisticsHistoryHandler.enqueueOperation(sample);
    }
}
