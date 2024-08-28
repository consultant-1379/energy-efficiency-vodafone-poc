/*
 * Copyright (c) 2017 Ericsson, AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.basic.network.functions;

import com.ericsson.basic.network.functions.inventory.InventoryHandler;
import com.ericsson.sb.communication.SouthboundCommunicationService;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.Iterator;
import java.util.List;
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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.l2.topology.rev160707.L2LinkAttributes;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.rev161116.networks.Network;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.rev161116.networks.NetworkBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.rev161116.networks.NetworkKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.rev161116.networks.network.Node;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.rev161116.networks.network.NodeKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.rev161116.Networks;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.rev161116.NetworkId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.rev161116.NodeId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.rev161116.NodeRef;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.topology.rev161116.LinkId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.topology.rev161116.Network1;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.topology.rev161116.Network1Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.topology.rev161116.networks.network.LinkKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.topology.rev161116.networks.network.Link;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.topology.rev161116.networks.network.LinkBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.topology.rev161116.networks.network.link.SupportingLink;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.topology.rev161116.networks.network.node.TerminationPoint;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.topology.rev161116.networks.network.node.TerminationPointKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.topology.rev161116.Node1;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.topology.rev161116.TpId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.topology.rev161021.Link1;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.topology.rev161021.Link1Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.topology.rev161021.TeInfoSource;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.topology.rev161021.TeNodeAugment;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.topology.rev161021.TeNodeConfig;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.topology.rev161021.TeNodeConfigAttributes;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.topology.rev161021.TerminationPoint1;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.topology.rev161021.te.link.config.attributes.TeLinkAttributes;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.topology.rev161021.te.node.augment.Te;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.topology.rev161021.te.link.augment.TeBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.topology.rev161021.te.node.augment.te.Config;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.topology.rev161021.te.node.augment.te.State;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.topology.rev161021.te.link.augment.te.StateBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.topology.rev161021.te.node.config.attributes.TeNodeAttributes;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.types.rev161026.TeOperStatus;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.l2.topology.lag.rev170714.L2LinkAttributes1;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.l2.topology.lag.rev170714.lag.config.LagConfig;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.l2.topology.lag.rev170714.member.link.MemberLink;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.network.topology.interfaces.rev170714.TpIfRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ericsson
 */
public class TopologyHandler implements AutoCloseable{

    private static final Logger LOG = LoggerFactory.getLogger(TopologyHandler.class);
    private static final Long ZERO_LONG = new Long(0);
    private DataBroker dataBroker;
    private SouthboundCommunicationService southboundCommunicationService;
    private String networkName;
    private InstanceIdentifier<Network> exportedNetworkIid;
    private InterfaceHandler interfaceHandler = new InterfaceHandler();
    private TopologyUpdater topologyUpdater = new TopologyUpdater();
    private Thread topologyUpdateThread;

    public InterfaceHandler getInterfaceHandler() {
        return interfaceHandler;
    }

   /**
     * Returns the dataBroker
     *
     * @return DataBroker
     */
    public DataBroker getDataBroker() {
        return dataBroker;
    }

   /**
     * Sets the dataBroker
     *
     * @param dataBroker
     */
    public void setDataBroker(final DataBroker dataBroker) {
        LOG.info("TopologyHandler.setDataBroker");
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
    }

   /**
     * Returns the southboundCommunicationService;
     *
     * @return southboundCommunicationService
     */
    public SouthboundCommunicationService getSouthboundCommunicationService() {
        return southboundCommunicationService;
    }

   /**
     * Sets the southboundCommunicationService
     *
     * @param southboundCommunicationService
     */
    public void setSouthboundCommunicationService(
            final SouthboundCommunicationService southboundCommunicationService) {
        LOG.info("TopologyHandler.southboundCommunicationService");
        this.southboundCommunicationService = Preconditions.checkNotNull(southboundCommunicationService);
    }

    public void init() {
        interfaceHandler.init(dataBroker, southboundCommunicationService, this);
        topologyUpdater.init(dataBroker, this);

        topologyUpdateThread = new Thread(topologyUpdater);
        topologyUpdateThread.setDaemon(true);
        topologyUpdateThread.setName("TopoUpd");
        topologyUpdateThread.start();
    }

   @Override
    public void close() throws InterruptedException {
        interfaceHandler.close();
    }

    public TopologyUpdater getTopologyUpdater() {
        return topologyUpdater;
    }

    public InstanceIdentifier<Network> getExportedNetworkIid() {
        return exportedNetworkIid;
    }

    public InstanceIdentifier<Network> buildNetworkIid(final String networkName) {
        return InstanceIdentifier.create(Networks.class).child(Network.class,
                new NetworkKey(new NetworkId(networkName)));
    }

    private Network readIetfNetwork() {
        Optional<Network> resultOptional = null;
        final ReadOnlyTransaction rt = dataBroker.newReadOnlyTransaction();
        CheckedFuture<Optional<Network>, ReadFailedException> result =
                rt.read(LogicalDatastoreType.OPERATIONAL, exportedNetworkIid);
        try {
            resultOptional = result.get();
            if (resultOptional == null && !resultOptional.isPresent()) {
                return null;
            }
        } catch (final InterruptedException e) {
            LOG.error("TopologyHandler.readIetfNetwork: InterruptedException", e);
            return null;
        } catch (final ExecutionException e) {
            LOG.error("TopologyHandler.readIetfNetwork: ExecutionException", e);
            return null;
        }

        return resultOptional.get();
    }

    private Node readIetfNode(final String sNodeId) {
        Optional<Node> resultOptional = null;
        InstanceIdentifier<Node> nodeIid = exportedNetworkIid
                .child(Node.class, new NodeKey(new NodeId(sNodeId)));
        final ReadOnlyTransaction rt = dataBroker.newReadOnlyTransaction();
        CheckedFuture<Optional<Node>, ReadFailedException> result =
                rt.read(LogicalDatastoreType.OPERATIONAL, nodeIid);
        try {
            resultOptional = result.get();
            if (resultOptional == null || !resultOptional.isPresent()) {
                return null;
            }
        } catch (final InterruptedException e) {
            LOG.error("TopologyHandler.readIetfNetwork: InterruptedException", e);
            return null;
        } catch (final ExecutionException e) {
            LOG.error("TopologyHandler.readIetfNetwork: ExecutionException", e);
            return null;
        }

        return resultOptional.get();
    }

    private TerminationPoint readIetfTerminationPoint(final String sNodeId,
            final String sTpId) {
        Optional<TerminationPoint> resultOptional = null;

        final InstanceIdentifier<TerminationPoint> terminationPointIid =
                exportedNetworkIid.child(Node.class, new NodeKey(new NodeId(sNodeId)))
                .augmentation(Node1.class)
                .child(TerminationPoint.class, new TerminationPointKey(new TpId(sTpId)));

        final ReadOnlyTransaction rt = dataBroker.newReadOnlyTransaction();
        CheckedFuture<Optional<TerminationPoint>, ReadFailedException> result =
                rt.read(LogicalDatastoreType.OPERATIONAL, terminationPointIid);
        try {
            resultOptional = result.get();
            if (resultOptional == null || !resultOptional.isPresent()) {
                return null;
            }
        } catch (final InterruptedException e) {
            LOG.error("TopologyHandler.readIetfTerminationPoint: InterruptedException", e);
            return null;
        } catch (final ExecutionException e) {
            LOG.error("TopologyHandler.readIetfTerminationPoint: ExecutionException", e);
            return null;
        }

        return resultOptional.get();
    }

    private List<Link> readIetfLinkList() {
        Network network = readIetfNetwork();
        if (network == null) {
            return null;
        }
        Network1 network1 = network.getAugmentation(Network1.class);
        if (network1 == null) {
            return null;
        }

        return network1.getLink();
    }

    public Link readIetfLink(final String strLinkId) {
        Optional<Link> resultOptional = null;

        final LinkKey linkKey = new LinkKey(new LinkId(strLinkId));
        final InstanceIdentifier<Link> linkIid = exportedNetworkIid.augmentation(Network1.class)
                  .child(Link.class, linkKey);

        final ReadOnlyTransaction rt = dataBroker.newReadOnlyTransaction();
        CheckedFuture<Optional<Link>, ReadFailedException> result =
                rt.read(LogicalDatastoreType.OPERATIONAL, linkIid);
        try {
            resultOptional = result.get();
            if (resultOptional == null || !resultOptional.isPresent()) {
                return null;
            }
        } catch (final InterruptedException e) {
            LOG.error("TopologyHandler.readLink: InterruptedException", e);
            return null;
        } catch (final ExecutionException e) {
            LOG.error("TopologyHandler.readLink: ExecutionException", e);
            return null;
        }

        return resultOptional.get();
    }

    public void createIetfNetwork(final String networkName) {
        final InstanceIdentifier<Network> networkIid = buildNetworkIid(networkName);
        this.networkName = networkName;
        this.exportedNetworkIid = networkIid;

        final NetworkBuilder networkBuilder = new NetworkBuilder();
        final NetworkId networkId = new NetworkId(networkName);
        final NetworkKey networkKey = new NetworkKey(networkId);
        networkBuilder.setKey(networkKey);
        networkBuilder.setNetworkId(networkId);

        writeNetwork(networkIid, networkBuilder.build());
    }

    public synchronized IpAddress getNodeIpAddress(final String nodeId) {
        final Node node = readIetfNode(nodeId);
        if (node == null) {
            return null;
        }
        final org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te
                .topology.rev161021.Node1 node1 = node.getAugmentation(org.opendaylight
                .yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.topology.rev161021.Node1.class);
        if (node1 == null) {
            return null;
        }
        Te te = node1.getTe();
        if (te == null) {
            return null;
        }
        State state = te.getState();
        if (state == null) {
            return null;
        }
        TeNodeAttributes teNodeAttributes = state.getTeNodeAttributes();
        if (teNodeAttributes == null) {
            return null;
        }
        List<IpAddress> lIpAddress = teNodeAttributes.getSignalingAddress();
        if (lIpAddress == null || lIpAddress.isEmpty()) {
            return null;
        }

        return lIpAddress.get(0);
    }

    private boolean applyIfRefFilter(final String ifRef) {
        if (IfTypeHandler.isLan(ifRef) || IfTypeHandler.isRadio(ifRef)) {
            return false;
        }

        return true;
    }

    private List<String> removeChildIfRef(final List<String> ifRefList) {
         List<String> childIfRefTotalList = new ArrayList<>();
         List<String> childIfRefFilteredList = new ArrayList<>();

         for (String ifRef : ifRefList) {
            List<String> childIfRefList = interfaceHandler.getChildIfRefList(ifRef, false);
            if (childIfRefList == null || childIfRefList.isEmpty()) {
                 continue;
            }
            for (String childIfRef : childIfRefList) {
                childIfRefTotalList.add(childIfRef);
            }
        }

        for (String ifRef : ifRefList) {
             if (!childIfRefTotalList.contains(ifRef)) {
                 childIfRefFilteredList.add(ifRef);
             }
         }

         return childIfRefFilteredList;
    }

    public List<String> getAllInterfaces(final boolean applyFiltering) {
        final Network ietfNetwork = readIetfNetwork();
        if (ietfNetwork == null) {
            return null;
        }
        final List<Node> lNode = ietfNetwork.getNode();
        if (lNode == null || lNode.isEmpty()) {
            return null;
        }
        final List<String> lIfRef = new ArrayList<String>();
        for (Node node : lNode) {
            Node1 node1 = node.getAugmentation(Node1.class);
            if (node1 == null) {
                continue;
            }
            List<TerminationPoint> lTp = node1.getTerminationPoint();
            if (lTp == null || lTp.isEmpty()) {
                continue;
            }
            for (TerminationPoint tp : lTp) {
                TpIfRef tpIfRef = tp.getAugmentation(TpIfRef.class);
                if (tpIfRef == null) {
                    continue;
                }
                String ifRefString = tpIfRef.getIfRef();
                if (ifRefString != null && (!applyFiltering || applyIfRefFilter(ifRefString))) {
                    lIfRef.add(ifRefString);
                }
            }
        }

        final List<String> lFilteredIfRef = removeChildIfRef(lIfRef);

        return lFilteredIfRef;
    }

     /*
      * returns the interface which sources traffic on the specified link
      */
     public synchronized String getLinkSourceIfRef(final Link link) {
        final NodeId sourceNodeId = link.getSource().getSourceNode();
        /*
         * in ietf-network-topology sourceTpId/destTpId are declared straight
         * as Java Object and hence need cast to string
         */
        final String sourceTpId = (String)link.getSource().getSourceTp();
        final TerminationPoint terminationPoint =
                readIetfTerminationPoint(sourceNodeId.getValue(), sourceTpId);
        if (terminationPoint == null) {
            LOG.warn("TopologyHandler.getLinkSourceIfRef: termination point null {} {}",
                    sourceNodeId.getValue(), sourceTpId);
            return null;
        }
        final TpIfRef tpIfRef = terminationPoint.getAugmentation(TpIfRef.class);
        if (tpIfRef == null) {
            LOG.warn("TopologyHandler.getLinkSourceIfRef: tpIfRef null {} {}",
                    sourceNodeId.getValue(), sourceTpId);
            return null;
        }

        return tpIfRef.getIfRef();
    }

    public synchronized String getLinkSourceIfRef(final String strLinkId) {
        final Link link = readIetfLink(strLinkId);
        if (link == null) {
            return null;
        }

        return getLinkSourceIfRef(link);
    }

    /*
     * returns the link where the specified interface is housed
     */
    public synchronized Link getIfRefLink(final String ifRef) {
        List<Link> lLink = readIetfLinkList();
        if (lLink == null || lLink.isEmpty()) {
            LOG.warn("TopologyHandler.getLinkSourceIfRef: link list null for ifRef {}", ifRef);
            return null;
        }
        Link foundLink = null;
        for (Link link : lLink) {
           String sourceIfRef = getLinkSourceIfRef(link.getLinkId().getValue());
           if (sourceIfRef != null && sourceIfRef.equals(ifRef)) {
               foundLink = link;
               LOG.info("TopologyHandler.getLinkSourceIfRef: link {} found for ifref {}",
                       foundLink, ifRef);
               break;
           }
        }

        return foundLink;
    }

    public Link getPeerLink(final String linkId) {
        final Link link1 = readIetfLink(linkId);
        if (link1 == null) {
            return null;
        }
        List<Link> lLink = readIetfLinkList();
        if (lLink == null || lLink.isEmpty()) {
            LOG.warn("TopologyHandler.getPeerLink: link list null for ifRef {}", linkId);
            return null;
        }

        Link foundLink = null;
        final String destNodeId = link1.getDestination().getDestNode().getValue();
        final String destTpId = (String)link1.getDestination().getDestTp();
        for (Link link : lLink) {
           String sourceNodeId = link.getSource().getSourceNode().getValue();
           String sourceTpId = (String)link.getSource().getSourceTp();
           if (sourceNodeId.equals(destNodeId) && sourceTpId.equals(destTpId)) {
               foundLink = link;
               LOG.info("TopologyHandler.getPeerLink: link {} found peer links {} {}",
                       linkId, foundLink.getLinkId().getValue());
               break;
           }
        }

        return foundLink;
    }

    public synchronized String getPeerLinkId(final String linkId) {
        final Link link = getPeerLink(linkId);
        if (link == null) {
            return null;
        }

        return link.getLinkId().getValue();
    }

    public void setLinkOperStatus(final String linkId, final boolean enabledStatus) {
        final TeOperStatus teOperStatus = (enabledStatus)  ? TeOperStatus.Up : TeOperStatus.Down;
        final Link ietfLink = readIetfLink(linkId);
        if (ietfLink == null) {
             return;
        }

        org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.topology.rev161021
                .te.link.augment.te.State state = getLinkTeState(ietfLink);
        final StateBuilder stateBuilder = new StateBuilder(state);
        stateBuilder.setOperStatus(teOperStatus);

        final TeBuilder teBuilder = new TeBuilder();
        teBuilder.setState(stateBuilder.build());
        Link1Builder link1Builder = new Link1Builder();
        link1Builder.setTe(teBuilder.build());
        final LinkBuilder ietfLinkBuilder = new LinkBuilder(ietfLink);
        ietfLinkBuilder.addAugmentation(Link1.class, link1Builder.build());
        handleLinkUpdate(LogicalDatastoreType.OPERATIONAL, ietfLinkBuilder.build());
    }

    public synchronized org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.topology.rev161021
                .te.link.augment.te.State getLinkTeState(final Link link) {
        final Link1 link1 = link.getAugmentation(Link1.class);
        if (link1 == null) {
            LOG.warn("TopologyHandler.getLinkTeState: no Link1 augmentation for link {}",
                    link.getLinkId());
            return null;
        }

        final org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.topology
                .rev161021.te.link.augment.Te te = link1.getTe();
        if (te == null) {
            LOG.warn("TopologyHandler.getLinkTeState: no Te for link {}", link.getLinkId());
            return null;
        }

        return te.getState();
    }

    public synchronized TeLinkAttributes getTeLinkAttributes(final Link link) {
        final org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.topology.rev161021
                .te.link.augment.te.State state = getLinkTeState(link);
        if (state == null) {
            LOG.warn("TopologyHandler.getTeLinkAttributes: no state for link {}",
                    link.getLinkId());
            return null;
        }

        return state.getTeLinkAttributes();
    }

    public synchronized TeLinkAttributes getTeLinkAttributes(final String strLinkId) {
        final Link link = readIetfLink(strLinkId);
        if (link == null) {
            return null;
        }

        return getTeLinkAttributes(link);
    }

    private void writeNetwork(final InstanceIdentifier<Network> networkIid, final Network network) {
        final WriteTransaction wt = dataBroker.newWriteOnlyTransaction();
        wt.put(LogicalDatastoreType.OPERATIONAL, networkIid, network);

        try {
            wt.submit().get();
        } catch (final Exception e) {
            LOG.error("TopologyHandler.deletewriteNetworkObject: ", e);
        }
    }

    private void handleNodeUpdate(final Node node) {
        writeNode(node, true);
        final org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te
                .topology.rev161021.Node1 node1 = node.getAugmentation(org.opendaylight
                .yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.topology.rev161021.Node1.class);
        if (node1 == null) {
            return;
        }
        Te te = node1.getTe();
        if (te == null) {
            return;
        }
        State state = te.getState();
        if (state == null) {
            return;
        }
        TeNodeAttributes teNodeAttributes = state.getTeNodeAttributes();
        if (teNodeAttributes == null) {
            return;
        }
        List<IpAddress> lIpAddress = teNodeAttributes.getSignalingAddress();
        if (lIpAddress == null || lIpAddress.isEmpty()) {
            return;
        }

        final IpAddress nodeIpAddress = lIpAddress.get(0);
        final String snmpReadCommunity = InventoryHandler.getNodeSnmpReadCommunity(node);
        final String snmpWriteCommunity = InventoryHandler.getNodeSnmpWriteCommunity(node);
        southboundCommunicationService.setNodeSnmpCommunity(nodeIpAddress,
                snmpReadCommunity, snmpWriteCommunity);

        final String cliUserLogin = InventoryHandler.getNodeCliUserLogin(node);
        final String cliPasswordLogin = InventoryHandler.getNodeCliPasswordLogin(node);
        if (cliUserLogin != null && cliPasswordLogin != null) {
            southboundCommunicationService.setNodeCliLoginCredentials(nodeIpAddress,
                    cliUserLogin, cliPasswordLogin);
        }

        interfaceHandler.loadInterfaceDataFromNodes(node.getNodeId().getValue(),
                nodeIpAddress);
    }

    private void handleTpUpdate(final LogicalDatastoreType logicalDatastoreType,
            final Node node, TerminationPoint tp) {
        writeTp(logicalDatastoreType, node, tp);

        final TpIfRef tpIfRef = tp.getAugmentation(TpIfRef.class);
        if (tpIfRef == null) {
            return;
        }

        interfaceHandler.createInterface(networkName, node.getNodeId().getValue(),
                tp.getTpId().getValue(), tpIfRef.getIfRef());
    }

    private void writeTp(final LogicalDatastoreType logicalDatastoreType,
            final Node ietfNode, TerminationPoint ietfTp) {
        final NodeId nodeId = ietfNode.getNodeId();
        TpId tpId = ietfTp.getTpId();
        final InstanceIdentifier<TerminationPoint> tpIid = exportedNetworkIid
                .child(Node.class, new NodeKey(nodeId)).augmentation(Node1.class)
                .child(TerminationPoint.class, new TerminationPointKey(tpId));

        final WriteTransaction wt = dataBroker.newWriteOnlyTransaction();
        wt.put(LogicalDatastoreType.OPERATIONAL, tpIid, ietfTp);

        try {
            wt.submit().get();
        } catch (final Exception e) {
            LOG.error("TopologyHandler.writeIetfNetwork: ", e);
        }
    }

    private void checkAndCreateLinkContainer() {
        final Network network = readIetfNetwork();
        Network1 network1 = network.getAugmentation(Network1.class);
        if (network1 != null) {
            return;
        }
        Network1Builder network1Builder = new Network1Builder();

        InstanceIdentifier<Network1> network1Iid = exportedNetworkIid.augmentation(Network1.class);

        final WriteTransaction wt = dataBroker.newWriteOnlyTransaction();
        wt.put(LogicalDatastoreType.OPERATIONAL, network1Iid, network1Builder.build());

        try {
            wt.submit().get();
        } catch (final Exception e) {
            LOG.error("TopologyHandler.writeIetfNetwork: ", e);
        }
    }

    private void writeNode(final Node node, final boolean mergeFlag) {
        final InstanceIdentifier<Node> nodeIid = exportedNetworkIid.child(Node.class, node.getKey());
        final WriteTransaction wt = dataBroker.newWriteOnlyTransaction();
        if (mergeFlag) {
            wt.merge(LogicalDatastoreType.OPERATIONAL, nodeIid, node);
        } else {
            wt.put(LogicalDatastoreType.OPERATIONAL, nodeIid, node);
        }

        try {
            wt.submit().get();
        } catch (final Exception e) {
            LOG.error("TopologyHandler.deleteObject: ", e);
        }
    }

    private void deleteObject(final LogicalDatastoreType type,
           final InstanceIdentifier<?> instanceIdentifierIid) {
       final WriteTransaction wt = dataBroker.newWriteOnlyTransaction();
       wt.delete(type, instanceIdentifierIid);

       try {
           wt.submit().get();
       } catch (final Exception e) {
           LOG.error("TopologyHandler.deleteObject: ", e);
       }
    }

    public void handleNetworkChanges(final LogicalDatastoreType type,
            final Collection<DataTreeModification<Network>> changes) {
        if (changes == null || changes.isEmpty()) {
            LOG.warn("TopologyHandler.onDataTreeChangedImpl: changes null or empty");
            return;
        }

        for (DataTreeModification<Network> change : changes) {
            DataObjectModification<Network> root = (DataObjectModification<Network>)(change.getRootNode());
            switch (root.getModificationType()) {
            case DELETE:
                onIetfTopoRemoved(type, root);
                break;
            case WRITE:
            case SUBTREE_MODIFIED:
                if (root.getDataBefore() == null) {
                    onIetfTopoAdded(type, root);
                } else {
                    onIetfTopoChanges(type, root);
                }
                break;
            default:
                LOG.warn("TopologyHandler.onDataTreeChangedImpl: unknown modification type");
                break;
            }
        }
    }

    private void onIetfTopoAdded(final LogicalDatastoreType logicalDatastoreType,
            final DataObjectModification<? extends DataObject> root) {
        LOG.info("TopologyHandler.onIetfTopoAdded called");
        onIetfTopoChanges(logicalDatastoreType, root);
    }

    private void onIetfTopoRemoved(final LogicalDatastoreType logicalDatastoreType,
            final DataObjectModification<? extends DataObject> root) {
        LOG.info("TopologyHandler.onIetfTopoRemoved called");
        onIetfTopoChanges(logicalDatastoreType, root);
    }

    private void onIetfTopoChanges(final LogicalDatastoreType logicalDatastoreType,
            final DataObjectModification<? extends DataObject> root) {
        LOG.trace("TopologyHandler.onIetfTopoChanges: root modificationType {}",
                root.getModificationType());

        if (root.getModifiedChildren() == null || root.getModifiedChildren().isEmpty()) {
            LOG.debug("TopologyHandler.onIetfTopoChanges: children changes list is null or empty");
            return;
        }

        for (DataObjectModification<? extends DataObject> child : root.getModifiedChildren()) {
            LOG.trace("TopologyHandler.onIetfTopoChanges. child data type {} {}",
                    child.getDataType(), child.getModificationType());
            if (child.getDataType().equals(Node.class)) {
                try {
                    switch (child.getModificationType()) {
                    case DELETE:
                        onIetfNodeRemoved(logicalDatastoreType, (DataObjectModification<Node>) child);
                        break;
                    case WRITE:
                    case SUBTREE_MODIFIED:
                        if (child.getDataBefore() == null) {
                            onIetfNodeAdded(logicalDatastoreType, (DataObjectModification<Node>) child);
                        } else {
                            onIetfNodeModified(logicalDatastoreType, (DataObjectModification<Node>) child);
                        }
                    break;
                    default:
                        LOG.warn("TopologyHandler.onIetfTopoChanges: node changed: unknown modification type");
                        break;
                    }
                } catch (final IllegalStateException e) {
                    LOG.warn("TopologyHandler.onIetfTopoChanges: data tree modification exception {}",
                            e.getMessage());
                }
            }
            if (child.getDataType().equals(Link.class)) {
                try {
                    switch (child.getModificationType()) {
                    case DELETE:
                        onIetfLinkRemoved(logicalDatastoreType, (DataObjectModification<Link>) child);
                        break;
                    case WRITE:
                    case SUBTREE_MODIFIED:
                        if (child.getDataBefore() == null) {
                            onIetfLinkAdded(logicalDatastoreType, (DataObjectModification<Link>) child);
                        } else {
                            onIetfLinkModified(logicalDatastoreType, (DataObjectModification<Link>) child);
                        }
                        break;
                    default:
                        LOG.warn("TopologyHandler.onIetfTopoChanges: link changed: unknown modification type");
                        break;
                    }
                } catch (final IllegalStateException e) {
                    LOG.warn("TopologyHandler.onIetfTopoChanges: data tree modification exception {}",
                            e.getMessage());
                }
            }

            LOG.debug("TopologyHandler.onIetfTopoChanges: root.getModificationType {}",
                    root.getModificationType());

            switch (root.getModificationType()) {
                case WRITE:
                case SUBTREE_MODIFIED:
                    onIetfTopoChanges(logicalDatastoreType, child);
                break;
                default:
                    LOG.warn("TopologyHandler.onIetfTopoChanges: unknown parent modification type");
                break;
            }
        }
    }

    private void onIetfNodeModified(final LogicalDatastoreType logicalDatastoreType,
            final DataObjectModification<Node> root) {
        final Node ietfNode = root.getDataAfter();

        if (root.getModifiedChildren() == null || root.getModifiedChildren().isEmpty()) {
            LOG.trace("TopologyHandler.onIetfNodeModified: {} children changes list is null or empty");
            return;
        }

        for (DataObjectModification<?> child : root.getModifiedChildren()) {
            LOG.debug("TopologyHandler.onIetfNodeModified: child data type {}", child.getDataType());
            if (child.getDataType().equals(Node1.class)) {
                for (DataObjectModification<?> child2 : child.getModifiedChildren()) {
                    LOG.debug("TopologyHandler.onIetfNodeModified: child2 data type {}", child2.getDataType());
                    if (child2.getDataType().equals(TerminationPoint.class)) {
                        try {
                            switch (child2.getModificationType()) {
                            case DELETE:
                                onIetfTpRemoved(logicalDatastoreType, ietfNode, (DataObjectModification<TerminationPoint>) child2);
                            break;
                            case WRITE:
                            case SUBTREE_MODIFIED:
                                if (child2.getDataBefore() == null) {
                                    onIetfTpAdded(logicalDatastoreType, ietfNode, (DataObjectModification<TerminationPoint>) child2);
                                } else {
                                    onIetfTpModified(logicalDatastoreType, ietfNode, (DataObjectModification<TerminationPoint>) child2);
                                }
                            break;
                            default:
                                LOG.warn("TopologyHandler.onIetfNodeAdded: TerminationPoint changed: unknown modification type");
                            break;
                            }
                        } catch (final IllegalStateException e) {
                            LOG.warn("TopologyHandler.onIetfNodeAdded: data tree modification exception {}",
                                    e.getMessage());
                        }
                    }
                }
            }
        }
    }

    private void onIetfNodeAdded(final LogicalDatastoreType logicalDatastoreType,
            final DataObjectModification<Node> root) {
        final Node ietfNode = root.getDataAfter();
        LOG.trace("TopologyHandler.onIetfNodeAdded: called {}", ietfNode.getNodeId().getValue());

        handleNodeUpdate(ietfNode);

        if (root.getModifiedChildren() == null || root.getModifiedChildren().isEmpty()) {
            LOG.debug("TopologyHandler.onIetfNodeAdded: {} children changes list is null or empty");
            return;
        }

        for (DataObjectModification<?> child : root.getModifiedChildren()) {
            LOG.debug("TopologyHandler.onIetfNodeAdded: child data type {}", child.getDataType());
            if (child.getDataType().equals(Node1.class)) {
                for (DataObjectModification<?> child2 : child.getModifiedChildren()) {
                    LOG.debug("TopologyHandler.onIetfNodeAdded: child2 data type {}", child2.getDataType());
                    if (child2.getDataType().equals(TerminationPoint.class)) {
                        try {
                            switch (child2.getModificationType()) {
                            case DELETE:
                                onIetfTpRemoved(logicalDatastoreType, ietfNode, (DataObjectModification<TerminationPoint>) child2);
                            break;
                            case WRITE:
                            case SUBTREE_MODIFIED:
                                if (child2.getDataBefore() == null) {
                                    onIetfTpAdded(logicalDatastoreType, ietfNode, (DataObjectModification<TerminationPoint>) child2);
                                } else {
                                    onIetfTpModified(logicalDatastoreType, ietfNode, (DataObjectModification<TerminationPoint>) child2);
                                }
                            break;
                            default:
                                LOG.warn("TopologyHandler.onIetfNodeAdded: TerminationPoint changed: unknown modification type");
                            break;
                            }
                        } catch (final IllegalStateException e) {
                            LOG.warn("TopologyHandler.onIetfNodeAdded: data tree modification exception {}",
                                    e.getMessage());
                        }
                    }
                }
            }
        }
    }

    private void onIetfNodeRemoved(final LogicalDatastoreType logicalDatastoreType,
            final DataObjectModification<Node> root) {
        final Node ietfNode = root.getDataBefore();
        final NodeId ietfNodeId = ietfNode.getNodeId();
        LOG.info("TopologyHandler.onIetfNodeRemoved called {}", ietfNode);
        InstanceIdentifier<Node> nodeIid = exportedNetworkIid.child(Node.class, new NodeKey(ietfNodeId));
    }

    private void onIetfTpAdded(final LogicalDatastoreType logicalDatastoreType,
            final Node ietfNode, final DataObjectModification<TerminationPoint> root) {

        final TerminationPoint ietfTp = root.getDataAfter();
        LOG.info("TopologyHandler.onIetfTpAdded called {}", ietfTp.getTpId().getValue());
        handleTpUpdate(logicalDatastoreType, ietfNode, ietfTp);
    }

    private void onIetfTpModified(final LogicalDatastoreType logicalDatastoreType,
            final Node ietfNode, final DataObjectModification<TerminationPoint> root) {
        final TerminationPoint ietfTp = root.getDataAfter();
        LOG.info("TopologyHandler.onIetfTpModified called {}", ietfTp.getTpId().getValue());
        handleTpUpdate(logicalDatastoreType, ietfNode, ietfTp);
    }

    private void onIetfTpRemoved(final LogicalDatastoreType logicalDatastoreType,
            final Node ietfNode, final DataObjectModification<TerminationPoint> root) {
        final TerminationPoint ietfTp = root.getDataBefore();
        LOG.info("TopologyHandler.onIetfTpRemoved called {}", ietfTp.getTpId().getValue());
        final NodeId nodeId = ietfNode.getNodeId();
        final TpId tpId = ietfTp.getTpId();
        InstanceIdentifier<TerminationPoint> tpIid = exportedNetworkIid
                .child(Node.class, new NodeKey(nodeId))
                .augmentation(Node1.class).child(TerminationPoint.class, new TerminationPointKey(tpId));
        deleteObject(LogicalDatastoreType.OPERATIONAL, tpIid);
    }

    private LagConfig getLinkLagInfo(final Link link) {
        final org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang
                .ietf.l2.topology.rev160707.Link1 link1 =
                link.getAugmentation(org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang
                .ietf.l2.topology.rev160707.Link1.class);
        if (link1 == null) {
            return null;
        }

        org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.l2.topology.rev160707.l2
                .link.attributes.L2LinkAttributes l2LinkAttributes = link1.getL2LinkAttributes();

        final L2LinkAttributes1 l2LinkAttributes1 =
                l2LinkAttributes.getAugmentation(L2LinkAttributes1.class);
        if (l2LinkAttributes1 == null) {
            return null;
        }
        return l2LinkAttributes1.getLagConfig();
    }

    private void handleLagConfig(final Link ietfLink) {
        /*
         * informing the interface handler about the LAG configuration
         */
        final LagConfig lagConfig = getLinkLagInfo(ietfLink);
        if (lagConfig == null) {
            LOG.debug("TopologyHandler.handleLinkUpdate: no lag config for link {}", ietfLink.getLinkId());
            return;
        }

        final String lagId = lagConfig.getLagId().getValue();
        LOG.info("TopologyHandler.handleLinkUpdate: lag {} info handling", lagId);

        final List<org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns
                .yang.l2.topology.lag.rev170714.member.link.MemberLink> lMemberLink =
                lagConfig.getMemberLink();

        if (lagId == null || lMemberLink == null || lMemberLink.isEmpty()){
            return;
        }
        ArrayList<LinkGroupMembershipInfo> listMembershipInfo = new ArrayList<>();

        for (MemberLink member : lMemberLink) {
            String linkMemberId = (String)member.getLinkId();
            boolean isMaster = member.isMaster();
            LOG.info("TopologyHandler.handleLinkUpdate: lag {} member {} {}", lagId, linkMemberId, isMaster);
            LinkGroupMembershipInfo lagMembershipInfo = new LinkGroupMembershipInfo(linkMemberId, isMaster);
            listMembershipInfo.add(lagMembershipInfo);
        }

        final LinkGroupInfo linkGroupInfo = new LinkGroupInfo(
                lagId,
                ietfLink.getLinkId().getValue(),
                ietfLink.getSource().getSourceNode().getValue(),
                (String)ietfLink.getSource().getSourceTp(),
                ietfLink.getDestination().getDestNode().getValue(),
                (String)ietfLink.getDestination().getDestTp(),
                listMembershipInfo,
                true);

        interfaceHandler.addLinkGroupConfig(linkGroupInfo);
    }

    private void handleBondingConfig(final Link ietfLink) {
        List<SupportingLink> lSupportingLink = ietfLink.getSupportingLink();
        if (lSupportingLink == null || lSupportingLink.isEmpty()) {
            return;
        }
        ArrayList<LinkGroupMembershipInfo> listMembershipInfo = new ArrayList<>();
        for (SupportingLink supportingLink : lSupportingLink) {
           String supportingLinkId = (String) supportingLink.getLinkRef();
           boolean isFictitiousMaster = BondingHandler.isFictitiousMaster(supportingLinkId);
           LinkGroupMembershipInfo lagMembershipInfo =
                   new LinkGroupMembershipInfo(supportingLinkId, isFictitiousMaster);
           listMembershipInfo.add(lagMembershipInfo);
        }

        /*
         * in case of a bonding, the link group identifier is the link-id itself
         */
        final LinkGroupInfo bondingInfo = new LinkGroupInfo(
                ietfLink.getLinkId().getValue(),
                ietfLink.getLinkId().getValue(),
                ietfLink.getSource().getSourceNode().getValue(),
                (String)ietfLink.getSource().getSourceTp(),
                ietfLink.getDestination().getDestNode().getValue(),
                (String)ietfLink.getDestination().getDestTp(),
                listMembershipInfo,
                false);

       LOG.info("TopologyHandler.handleBondingConfig: {} {} {} {}",
               ietfLink.getLinkId().getValue(),
               ietfLink.getSource().getSourceNode().getValue(),
               (String)ietfLink.getSource().getSourceTp(),
               listMembershipInfo.size());

        interfaceHandler.addLinkGroupConfig(bondingInfo);
    }

    private void handleLinkUpdate(final LogicalDatastoreType logicalDatastoreType, final Link ietfLink) {
        checkAndCreateLinkContainer();
        final InstanceIdentifier<Link> linkIid = exportedNetworkIid.augmentation(Network1.class)
                  .child(Link.class, ietfLink.getKey());
        final WriteTransaction wt = dataBroker.newWriteOnlyTransaction();
        wt.put(LogicalDatastoreType.OPERATIONAL, linkIid, ietfLink);
        try {
            wt.submit().get();
        } catch (final Exception e) {
            LOG.error("TopologyHandler.handleLinkUpdate: ", e);
        }

        handleLagConfig(ietfLink);
        handleBondingConfig(ietfLink);
    }

    private void onIetfLinkAdded(final LogicalDatastoreType logicalDatastoreType,
            final DataObjectModification<Link> root) {
        final Link ietfLink = root.getDataAfter();
        LOG.info("TopologyHandler.onIetfLinkAdded called {}", ietfLink.getLinkId().getValue());
        handleLinkUpdate(logicalDatastoreType, ietfLink);
    }

    private void onIetfLinkModified(final LogicalDatastoreType logicalDatastoreType,
             final DataObjectModification<Link> root) {
        final Link ietfLink = root.getDataAfter();
        LOG.info("TopologyHandler.onIetfLinkModified called {}", ietfLink.getLinkId().getValue());
        handleLinkUpdate(logicalDatastoreType, ietfLink);
    }

    private void onIetfLinkRemoved(final LogicalDatastoreType logicalDatastoreType,
            final DataObjectModification<Link> root) {
        final Link ietfLink = root.getDataBefore();
        LOG.info("TopologyHandler.onIetfLinkRemoved called {}", ietfLink.getLinkId().getValue());
        InstanceIdentifier<Link> linkIid = exportedNetworkIid.augmentation(Network1.class)
                .child(Link.class, ietfLink.getKey());
        deleteObject(LogicalDatastoreType.OPERATIONAL, linkIid);
    }

    public void resyncTopologyMaxBandwidth(final String ifRef, final long capacity) {
        topologyUpdater.enqueueOperation(
                new TopologyUpdateData(TopologyUpdateData.UpdateType.BandwidthUpdate,
                ifRef, capacity));
    }
}
