/*
 * Copyright (c) 2017 Ericsson, AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.snmp.agent;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.snmp.agent.rev170714.IfIndexTable;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.snmp.agent.rev170714.IfIndexTableBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.snmp.agent.rev170714.IfRefTable;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.snmp.agent.rev170714.IfRefTableBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.snmp.agent.rev170714.IfStackTable;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.snmp.agent.rev170714.IfStackTableBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.snmp.agent.rev170714.GetInterfaceValueInput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.snmp.agent.rev170714.GetInterfaceValueOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.snmp.agent.rev170714.GetInterfaceValueOutput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.snmp.agent.rev170714.mib.object.oid.ObjectList;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.snmp.agent.rev170714.mib.object.oid.ObjectListKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.snmp.agent.rev170714.MibObjectOid;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.snmp.agent.rev170714.SetInterfaceValueInput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.snmp.agent.rev170714.SetInterfaceValueOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.snmp.agent.rev170714.SetInterfaceValueOutput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.snmp.agent.rev170714.SnmpAgentService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.snmp.agent.rev170714._if.index.table.IfIndexEntryKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.snmp.agent.rev170714._if.index.table.IfIndexEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.snmp.agent.rev170714._if.index.table.IfIndexEntry;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.snmp.agent.rev170714._if.ref.table.IfEntryKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.snmp.agent.rev170714._if.ref.table.IfEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.snmp.agent.rev170714._if.ref.table.IfEntry;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.snmp.agent.rev170714._if.stack.table.IfStackEntryKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.snmp.agent.rev170714._if.stack.table.IfStackEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.snmp.agent.rev170714._if.stack.table.IfStackEntry;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.topology.inventory.rev170714.ProductNameBase;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.topology.inventory.rev170714.MINILINK6351;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.topology.inventory.rev170714.MINILINK6352;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.topology.inventory.rev170714.MINILINK6691;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpGetInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpGetInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpGetType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpGetOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpSetInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpSetInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.snmp.get.output.Results;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.BaseValueType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.ValueTypeHexString;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ericsson
 */
public class SnmpAgent implements SnmpAgentOperations, SnmpAgentService, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(SnmpAgent.class);
    private static final String IFXTABLE = "1.3.6.1.2.1.31.1.1.1.1";
    private static final String IFTABLE = "1.3.6.1.2.1.2.2.1.2";
    private static final String IFSTACKTABLE = "1.3.6.1.2.1.31.1.2";
    private static final String XFLIMAPPINGTABLE = "1.3.6.1.4.1.193.81.3.4.5.1.2.1.1";
    private static final String XFCARRIERTERMINATIONTABLE = "1.3.6.1.4.1.193.81.3.4.5.1.3.1.4";
    private static final String XFETHERNETIFUSAGE = "1.3.6.1.4.1.193.81.4.1.3.1.1.1.1.2";
    private static final Long ZERO_LONG = new Long(0);
    private static final String NOT_READY = "3";

    private DataBroker dataBroker;
    private RpcProviderRegistry rpcProviderRegistry;
    private BindingAwareBroker.RpcRegistration<SnmpAgentService> rpcRegistration;
    private CommunityHandler communityHandler = new CommunityHandler();

    private class IfIndexData {
        private String ifDescr;
        private String ifName;
        private Long ifLowerIfIndex;
        private Long ifLowerIfIndex2;
        private Long ifLiEntLogicalIndex;
        private Long ifXfRadioFrameId;
        private Integer bridgePortId;
        private String ifRef;

        public IfIndexData() {
        }

        public String getIfDescr() {
            return ifDescr;
        }

        public String getIfName() {
            return ifName;
        }

        public Long getIfLowerIfIndex() {
            return ifLowerIfIndex;
        }

        public Long getIfLowerIfIndex2() {
            return ifLowerIfIndex2;
        }

        public Long getIfLiEntLogicalIndex() {
            return ifLiEntLogicalIndex;
        }

        public Long getIfXfRadioFrameId() {
            return ifXfRadioFrameId;
        }

        public String getIfRef() {
            return ifRef;
        }

        public Integer getBridgePortId() {
            return bridgePortId;
        }

        public void setIfDescr(final String ifDescr) {
            this.ifDescr = ifDescr;
        }

        public void setIfName(final String ifName) {
            this.ifName = ifName;
        }

        public void setIfLowerIfIndex(final Long ifLowerIfIndex) {
            this.ifLowerIfIndex = ifLowerIfIndex;
        }

        public void setIfLowerIfIndex2(final Long ifLowerIfIndex2) {
            this.ifLowerIfIndex2 = ifLowerIfIndex2;
        }

        public void setIfLiEntLogicalIndex(final Long ifLiEntLogicalIndex) {
            this.ifLiEntLogicalIndex = ifLiEntLogicalIndex;
        }

        public void setIfXfRadioFrameId(final Long ifXfRadioFrameId) {
            this.ifXfRadioFrameId = ifXfRadioFrameId;
        }

        public void setIfRef(final String ifRef) {
            this.ifRef = ifRef;
        }

        public void setBridgePortId(final Integer bridgePortId) {
            this.bridgePortId = bridgePortId;
        }
    }

    private class IfIndexPair {

        private Long higherIfIndex;
        private Long lowerIfIndex;

        public IfIndexPair(final Long higherIfIndex, final Long lowerIfIndex) {
            this.higherIfIndex = higherIfIndex;
            this.lowerIfIndex = lowerIfIndex;
        }

        public Long getHigherIfIndex() {
            return higherIfIndex;
       }

        public Long getLowerIfIndex() {
            return lowerIfIndex;
       }
    }

   /**
     * Starts SNMP Agent
     */
    public void startup() {
        LOG.info("SnmpAgent.startup");
        SnmpMibOidHandler snmpMibOidHandler = new SnmpMibOidHandler();
        snmpMibOidHandler.init(dataBroker);
        initializeIfRefTable();
        initializeIfIndexTable();
    }

   /**
     * Shutdown SNMP Agent
     */
    @Override
    public void close() {
        LOG.info("SnmpAgent.close");
        rpcRegistration.close();
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
        LOG.info("SnmpAgent.setDataBroker");
        this.dataBroker = dataBroker;
    }

   /**
     * Returns the rpcProviderRegistry
     *
     * @return rpcProviderRegistry
     */
    public RpcProviderRegistry getRpcRegistry() {
        return rpcProviderRegistry;
    }

    /**
     * Sets the rpcProviderRegistry
     *
     * @param rpcProviderRegistry
     */
    public void setRpcRegistry(final RpcProviderRegistry rpcProviderRegistry) {
        LOG.info("SnmpAgent.setRpcRegistry");
        this.rpcProviderRegistry = rpcProviderRegistry;
        rpcRegistration = rpcProviderRegistry.addRpcImplementation(SnmpAgentService.class, this);
    }

    /**
     * Returns the SnmpService
     *
     * @return SnmpService
     */
    public SnmpService getSnmpService() {
        return rpcProviderRegistry.getRpcService(SnmpService.class);
    }

    private void initializeIfRefTable() {
        final ReadWriteTransaction transaction = dataBroker.newReadWriteTransaction();
        final InstanceIdentifier<IfRefTable> path = InstanceIdentifier.create(IfRefTable.class);
        final CheckedFuture<Optional<IfRefTable>, ReadFailedException> ifRefTable =
                transaction.read(LogicalDatastoreType.OPERATIONAL, path);
        try {
            if (ifRefTable.get() == null || !ifRefTable.get().isPresent()) {
                final IfRefTableBuilder ifRefTableBuilder = new IfRefTableBuilder();
                LOG.info("SnmpAgent.initializeIfRefTable: {} {}", path, ifRefTableBuilder.build());
                transaction.put(LogicalDatastoreType.OPERATIONAL, path, ifRefTableBuilder.build());
                transaction.submit().get();
            } else {
                transaction.cancel();
            }
        } catch (final Exception e) {
            LOG.error("SnmpAgent.initializeIfRefTable ", e);
        }
    }

    private void initializeIfIndexTable() {
        final ReadWriteTransaction transaction = dataBroker.newReadWriteTransaction();
        final InstanceIdentifier<IfIndexTable> path = InstanceIdentifier.create(IfIndexTable.class);
        final CheckedFuture<Optional<IfIndexTable>, ReadFailedException> ifIndexTable =
                transaction.read(LogicalDatastoreType.OPERATIONAL, path);
        try {
            if (ifIndexTable.get() == null || !ifIndexTable.get().isPresent()) {
                final IfIndexTableBuilder ifIndexTableBuilder = new IfIndexTableBuilder();
                LOG.info("SnmpAgent.initializeIfIndexTable: {} {}", path, ifIndexTableBuilder.build());
                transaction.put(LogicalDatastoreType.OPERATIONAL, path, ifIndexTableBuilder.build());
                transaction.submit().get();
            } else {
                transaction.cancel();
            }
        } catch (final Exception e) {
            LOG.error("SnmpAgent.initializeIfIndexTable ", e);
        }
    }

    private Long oidToIfIndex(final String oid) {
        String[] split = oid.split("\\.");
        LOG.trace("SnmpAgent.oidToIfIndex {} {}", oid, split.length);
        if (split == null || split.length == 0) {
            return ZERO_LONG;
        }
        return new Long(Long.parseLong(split[split.length - 1]));
    }

    private IfIndexPair oidToIfIndexPair(final String oid) {
        String[] split = oid.split("\\.");
        LOG.info("SnmpAgent.oidToIfIndex {} {}", oid, split.length);
        if (split == null || split.length == 0) {
            return null;
        }
        Long higherIfIndex = new Long(Long.parseLong(split[split.length - 2]));
        Long lowerIfIndex = new Long(Long.parseLong(split[split.length - 1]));
        if (higherIfIndex.equals(ZERO_LONG) || lowerIfIndex.equals(ZERO_LONG)) {
            return null;
        }

        return new IfIndexPair(higherIfIndex, lowerIfIndex);
    }

    private String buildIfRef(final String nodeId, final String localIfName, final String ifDescr) {
       return nodeId.concat(":").concat(ifDescr).concat("-").concat(localIfName);
    }

    private Map<Long, Long> computeLiEntLogicalIndexMapping(final List<Results> xfCtTableResults) {
        Map<Long, Long> liEntLogicalIndex2RadioFrameId = new HashMap<Long, Long>();
        for (Results results : xfCtTableResults) {
            String oid = results.getOid();
            Long liEntLogicalIndex = oidToIfIndex(oid);
            Long radioFrameId = Long.parseLong(results.getValue());
            liEntLogicalIndex2RadioFrameId.put(liEntLogicalIndex, radioFrameId);
        }

        return liEntLogicalIndex2RadioFrameId;
    }

    private void store63xxIfTableData(final String nodeId, final List<Results> ifXTableResults) {
        if (ifXTableResults == null) {
            LOG.warn("SnmpAgent.storeIfTableData: null mandatory tables for node {} ifXTableResults {}",
                    nodeId, ifXTableResults);
            return;
        }

        Map<Long, IfIndexData> ifIndexToName = new HashMap<Long, IfIndexData>();

        List<String> ifRefList = new ArrayList<>();
        Map<Integer, List<Long>> slotToIfIndexList = new HashMap<>();

        /* storing in cache ifName */
        for (Results results : ifXTableResults) {
            String oid = results.getOid();
            Long ifIndex = oidToIfIndex(oid);
            if (ifIndex.equals(ZERO_LONG)) {
                continue;
            }
            IfIndexData ifIndexData = new IfIndexData();
            String ifName = ML63xxRegEx.normalizeIfName(results.getValue());
            String ifDescr = ML63xxRegEx.extractIfDescr(results.getValue());
            if (ifName != null) {
                ifIndexData.setIfName(ifName);
                ifIndexToName.put(ifIndex, ifIndexData);
            }
            if (ifDescr != null) {
                ifIndexData.setIfDescr(ifDescr);
                ifIndexToName.put(ifIndex, ifIndexData);
            }

            Integer slotId = ML63xxRegEx.extractSlotId(results.getValue());

            List<Long> ifIndexList = slotToIfIndexList.get(slotId);
            if (ifIndexList == null) {
                ifIndexList = new ArrayList<>();
            }
            ifIndexList.add(ifIndex);
            slotToIfIndexList.put(slotId, ifIndexList);
        }

        WriteTransaction transaction = dataBroker.newWriteOnlyTransaction();

        /*
         * ML6351 has ifStack table empty, then building our own
         */
        if (slotToIfIndexList.keySet() == null) {
            return;
        }
        Iterator<Integer> iter = slotToIfIndexList.keySet().iterator();
        while (iter.hasNext()) {
            Integer slot = iter.next();
            List<Long> ifIndexList = slotToIfIndexList.get(slot);
            LOG.info("SnmpAgent.store63xxIfTableData: slot {} ifIndexList.get(0) {}", slot, ifIndexList.get(0));
            if (ifIndexList == null || ifIndexList.isEmpty()) {
                continue;
            }
            /*
             * building the if-stack table from scratch as ML6351
             * does not provided it
             */
            String wanIfRef = null;
            Long wanIfRefIfIndex = ZERO_LONG;
            String lanIfRef = null;
            Long lanIfRefIfIndex = ZERO_LONG;
            String rauIfRef = null;
            Long rauIfRefIfIndex = ZERO_LONG;
            String rfIfRef = null;
            Long rfIfRefIfIndex = ZERO_LONG;

            for (Long ifIndex : ifIndexList) {
                IfIndexData ifIndexData = ifIndexToName.get(ifIndex);
                if (ifIndexData == null) {
                    continue;
                }
                String ifDescr = ifIndexData.getIfDescr();
                String ifRef = buildIfRef(nodeId, ifIndexData.getIfName(), ifIndexData.getIfDescr());
                LOG.info("SnmpAgent.store63xxIfTableData: ifRef {}", ifRef);
                if (ML63xxRegEx.isWan(ifRef)) {
                    wanIfRef = ifRef;
                    wanIfRefIfIndex = ifIndex;
                    LOG.info("SnmpAgent.store63xxIfTableData: wanIfRef {} {} {}", wanIfRef, wanIfRefIfIndex, ifIndex);
                } else if (ML63xxRegEx.isLan(ifRef)) {
                    lanIfRef = ifRef;
                    lanIfRefIfIndex = ifIndex;
                    LOG.info("SnmpAgent.store63xxIfTableData: lanIfRef {} {} {}", lanIfRef, lanIfRefIfIndex, ifIndex);
                } else if (ML63xxRegEx.isRauIf(ifRef)) {
                    rauIfRef = ifRef;
                    rauIfRefIfIndex = ifIndex;
                    LOG.info("SnmpAgent.store63xxIfTableData: rauIfRef {} {} {}", rauIfRef, rauIfRefIfIndex, ifIndex);
                } else if (ML63xxRegEx.isRF(ifRef)) {
                    rfIfRef = ifRef;
                    rfIfRefIfIndex = ifIndex;
                    LOG.info("SnmpAgent.store63xxIfTableData: rfIfRef {} {} {}", rfIfRef, rfIfRefIfIndex, ifIndex);
                }
            }

            LOG.info("SnmpAgent.store63xxIfTableData: {} {} {} {}", wanIfRef, lanIfRef, rauIfRef, rfIfRef);

            if (wanIfRef == null && lanIfRef == null) {
                continue;
            }

            IfIndexData ifIndexData = ifIndexToName.get(wanIfRefIfIndex);
            ifIndexData.setIfLowerIfIndex(rauIfRefIfIndex);
            ifIndexToName.put(wanIfRefIfIndex, ifIndexData);
            ifIndexData = ifIndexToName.get(rauIfRefIfIndex);
            ifIndexData.setIfLowerIfIndex(rfIfRefIfIndex);
            ifIndexToName.put(rauIfRefIfIndex, ifIndexData);

            String wanlanIfRef = (wanIfRef != null) ? wanIfRef : lanIfRef;

            LOG.info("SnmpAgent.store63xxIfTableData: wanlanIfRef {} ", wanlanIfRef);

            List<String> lowerIfRefList = new ArrayList<>();
            lowerIfRefList.add(rauIfRef);
            lowerIfRefList.add(rfIfRef);

            IfStackEntryKey ifStackEntryKey = new IfStackEntryKey(wanlanIfRef);
            IfStackEntryBuilder ifStackEntryBuilder = new IfStackEntryBuilder();
            ifStackEntryBuilder.setKey(ifStackEntryKey);
            ifStackEntryBuilder.setIfRef(wanlanIfRef);
            ifStackEntryBuilder.setIfRefStack(lowerIfRefList);
            InstanceIdentifier<IfStackEntry> iid = InstanceIdentifier.create(IfStackTable.class)
                     .child(IfStackEntry.class, ifStackEntryKey);
            transaction.put(LogicalDatastoreType.OPERATIONAL, iid, ifStackEntryBuilder.build(), true);
            try {
                transaction.submit().get();
            } catch (final Exception e) {
                LOG.error("SnmpAgent.storeAll ", e);
            }
        } // end while


        transaction = dataBroker.newWriteOnlyTransaction();

        for (Results results : ifXTableResults) {
            String oid = results.getOid();
            Long ifIndex = oidToIfIndex(oid);
            if (ifIndex.equals(new Long(0))) {
                continue;
            }
            IfIndexData ifIndexData = ifIndexToName.get(ifIndex);
            String ifRef = buildIfRef(nodeId, ifIndexData.getIfName(), ifIndexData.getIfDescr());
            ifIndexData.setIfRef(ifRef);
            IfEntryKey ifEntryKey = new IfEntryKey(ifRef);
            InstanceIdentifier<IfEntry> ifEntryIid = InstanceIdentifier.create(IfRefTable.class)
                    .child(IfEntry.class, ifEntryKey);
            IfEntryBuilder ifEntryBuilder = new IfEntryBuilder();
            ifEntryBuilder.setIfIndex(ifIndex);
            ifEntryBuilder.setIfName(ifIndexData.getIfName());
            ifEntryBuilder.setLowerIfIndex(ifIndexData.getIfLowerIfIndex());
            ifEntryBuilder.setIfDescr(ifIndexData.getIfDescr());
            ifEntryBuilder.setKey(ifEntryKey);
            LOG.info("SnmpAgent.storeAll: {} {} {} {} {} {} {}", nodeId, ifRef, ifIndex, ifIndexData.getIfName(),
                    ifIndexData.getIfDescr());
            transaction.merge(LogicalDatastoreType.OPERATIONAL, ifEntryIid, ifEntryBuilder.build());

            /*
             * if-index container
             */
            IfIndexEntryKey ifIndexEntryKey = new IfIndexEntryKey(ifIndex, nodeId);
            InstanceIdentifier<IfIndexEntry> ifIndexIid = InstanceIdentifier.create(IfIndexTable.class)
                    .child(IfIndexEntry.class, ifIndexEntryKey);
            IfIndexEntryBuilder ifIndexEntryBuilder = new IfIndexEntryBuilder();
            ifIndexEntryBuilder.setIfRef(ifRef);
            ifIndexEntryBuilder.setKey(ifIndexEntryKey);
            transaction.merge(LogicalDatastoreType.OPERATIONAL, ifIndexIid, ifIndexEntryBuilder.build());
        }

        try {
            transaction.submit().get();
        } catch (final Exception e) {
            LOG.error("SnmpAgent.storeAll ", e);
        }
    }

    private void store66xxIfTableData(
            final String nodeId,
            final List<Results> ifXTableResults, final List<Results> ifTableResults,
            final List<Results> xfLiTableResults, final List<Results> xfCtTableResults,
            final List<Results> ifStackTableResults, final List<Results> xfIfEthernetUsageTable) {

        if (ifXTableResults == null || ifTableResults == null || ifStackTableResults == null) {
            LOG.warn("SnmpAgent.storeIfTableData: null mandatory tables for node {} ifXTableResults {} ifTableResults {} ifStackTableResults {}",
                    nodeId, ifXTableResults, ifTableResults, ifStackTableResults);
            return;
        }

        Map<Long, IfIndexData> ifIndexToName = new HashMap<Long, IfIndexData>();

        /* storing in cache ifName */
        for (Results results : ifXTableResults) {
            String oid = results.getOid();
            Long ifIndex = oidToIfIndex(oid);
            if (ifIndex.equals(ZERO_LONG)) {
                continue;
            }
            IfIndexData ifIndexData = new IfIndexData();
            ifIndexData.setIfName(results.getValue());
            ifIndexToName.put(ifIndex, ifIndexData);
        }

        /* storing in cache ifDescr */
        for (Results results : ifTableResults) {
            String oid = results.getOid();
            Long ifIndex = oidToIfIndex(oid);
            if (ifIndex.equals(ZERO_LONG)) {
                continue;
            }
            IfIndexData ifIndexData = ifIndexToName.get(ifIndex);
            if (ifIndexData != null) {
                ifIndexData.setIfDescr(results.getValue());
                ifIndexToName.put(ifIndex, ifIndexData);
            }
        }

        /* storing in cache ifStackTable */
        for (Results results : ifStackTableResults) {
            if (results.getValue().equals(NOT_READY)) {
                continue;
            }
            String oid = results.getOid();
            IfIndexPair ifIndexPair = oidToIfIndexPair(oid);
            if (ifIndexPair == null) {
                continue;
            }
            Long higherIfIndex = ifIndexPair.getHigherIfIndex();
            Long lowerIfIndex = ifIndexPair.getLowerIfIndex();
            if (higherIfIndex.equals(ZERO_LONG) || lowerIfIndex.equals(ZERO_LONG)) {
                continue;
            }

            IfIndexData ifIndexData = ifIndexToName.get(higherIfIndex);
            if (ifIndexData != null) {
                if (ifIndexData.getIfLowerIfIndex() == null ||
                    ifIndexData.getIfLowerIfIndex().equals(ZERO_LONG)) {
                    // lower if index
                    ifIndexData.setIfLowerIfIndex(lowerIfIndex);
                } else {
                    // bonding scenario: the second bonding interface as associated to the same WAN if
                    ifIndexData.setIfLowerIfIndex2(lowerIfIndex);
                }
                ifIndexToName.put(higherIfIndex, ifIndexData);
            }
        }

        if (xfCtTableResults != null) {
            Map<Long, Long> liEntLogicalIndex2RadioFrameId = computeLiEntLogicalIndexMapping(xfCtTableResults);
            /* storing in cache liEntLogicalIndex */
            for (Results results : xfLiTableResults) {
                String oid = results.getOid();
                Long ifIndex = oidToIfIndex(oid);
                if (ifIndex.equals(ZERO_LONG)) {
                    continue;
                }
                IfIndexData ifIndexData = ifIndexToName.get(ifIndex);
                if (ifIndexData != null) {
                    Long ifLiEntLogicalIndex = new Long(Long.parseLong(results.getValue()));
                    ifIndexData.setIfLiEntLogicalIndex(ifLiEntLogicalIndex);
                    Long radioFrameId = liEntLogicalIndex2RadioFrameId.get(ifLiEntLogicalIndex);
                    if (radioFrameId != null) {
                        ifIndexData.setIfXfRadioFrameId(radioFrameId);
                    }
                    ifIndexToName.put(ifIndex, ifIndexData);
                } else {
                    LOG.info("SnmpAgent.storeIfTableData: {} {} NOT FOUND", nodeId, ifIndex);
                }
            }
        }

        if (xfIfEthernetUsageTable != null) {
            for (Results results : xfIfEthernetUsageTable) {
                String oid = results.getOid();
                Long ifIndex = oidToIfIndex(oid);
                LOG.info("SnmpAgent.storeIfTableData: xfIfEthernetUsageTable processing {} ", ifIndex);
                if (ifIndex.equals(ZERO_LONG)) {
                    continue;
                }
                IfIndexData ifIndexData = ifIndexToName.get(ifIndex);
                if (ifIndexData != null) {
                    try {
                        Integer bridgePortInt = new Integer(Integer.parseInt(results.getValue()));
                        LOG.info("SnmpAgent.bridgePort: ifIndex {} bridgePortInt {}", ifIndex, bridgePortInt);
                        ifIndexData.setBridgePortId(bridgePortInt);
                    } catch (final NumberFormatException e) {
                        LOG.error("SnmpAgent.bridgePort: {} ", ifIndex, e);
                    }
                }
            }
        }

        /* storing all info into operational datastore */
        if (ifXTableResults != null) {
            WriteTransaction transaction = dataBroker.newWriteOnlyTransaction();

            for (Results results : ifXTableResults) {
                String oid = results.getOid();
                Long ifIndex = oidToIfIndex(oid);
                if (ifIndex.equals(new Long(0))) {
                    continue;
                }
                IfIndexData ifIndexData = ifIndexToName.get(ifIndex);
                String ifRef = buildIfRef(nodeId, ifIndexData.getIfName(), ifIndexData.getIfDescr());
                ifIndexData.setIfRef(ifRef);
                ifIndexToName.put(ifIndex, ifIndexData);

                IfEntryKey ifEntryKey = new IfEntryKey(ifRef);
                InstanceIdentifier<IfEntry> ifEntryIid = InstanceIdentifier.create(IfRefTable.class)
                        .child(IfEntry.class, ifEntryKey);
                IfEntryBuilder ifEntryBuilder = new IfEntryBuilder();
                ifEntryBuilder.setIfIndex(ifIndex);
                ifEntryBuilder.setIfName(ifIndexData.getIfName());
                if (ifIndexData.getIfLowerIfIndex() != null && !ifIndexData.getIfLowerIfIndex().equals(ZERO_LONG)) {
                    ifEntryBuilder.setLowerIfIndex(ifIndexData.getIfLowerIfIndex());
                }
                if (ifIndexData.getIfLowerIfIndex2() != null && !ifIndexData.getIfLowerIfIndex2().equals(ZERO_LONG)) {
                    ifEntryBuilder.setLowerIfIndex2(ifIndexData.getIfLowerIfIndex2());
                }
                ifEntryBuilder.setIfDescr(ifIndexData.getIfDescr());
                ifEntryBuilder.setIfLiEntLogicalIndex(ifIndexData.getIfLiEntLogicalIndex());
                ifEntryBuilder.setIfRadioFrameId(ifIndexData.getIfXfRadioFrameId());
                ifEntryBuilder.setBridgePortId(ifIndexData.getBridgePortId());
                ifEntryBuilder.setKey(ifEntryKey);
                LOG.info("SnmpAgent.storeAll: {} {} {} {} {} {} {}", nodeId, ifRef, ifIndex, ifIndexData.getIfName(),
                        ifIndexData.getIfDescr(), ifIndexData.getIfLiEntLogicalIndex(), ifIndexData.getIfXfRadioFrameId());
                transaction.merge(LogicalDatastoreType.OPERATIONAL, ifEntryIid, ifEntryBuilder.build());
                /*
                 * if-index container
                 */
                IfIndexEntryKey ifIndexEntryKey = new IfIndexEntryKey(ifIndex, nodeId);
                InstanceIdentifier<IfIndexEntry> ifIndexIid = InstanceIdentifier.create(IfIndexTable.class)
                        .child(IfIndexEntry.class, ifIndexEntryKey);
                IfIndexEntryBuilder ifIndexEntryBuilder = new IfIndexEntryBuilder();
                ifIndexEntryBuilder.setIfRef(ifRef);
                ifIndexEntryBuilder.setKey(ifIndexEntryKey);
                transaction.merge(LogicalDatastoreType.OPERATIONAL, ifIndexIid, ifIndexEntryBuilder.build());
            }

            try {
                transaction.submit().get();
            } catch (final Exception e) {
                LOG.error("SnmpAgent.storeAll ", e);
            }
        }

        build66xxInterfaceStackingChain(nodeId, ifIndexToName);
    }

    private IfEntry fromIfRefToIfEntry(final String ifRef) {
       return null;
    }

    private String fromIfIndex2IfRef(final Long ifIndex) {
       return null;
    }

    private void build66xxInterfaceStackingChain(final String nodeId,
            final Map<Long, IfIndexData> ifIndexToName) {
        /*
         *  look-ups:
         * ifRef -> ifIndex -> lowerIfIndex -> ifRef2 -> lowerIfIndex2 -> ifRef3 -> lowerIfIndex3.
         *  mini-link-6691-1:WAN-1/1/1 -> (ifIndex = 21964522) -> (lowerIfIndex = 22445522) ->
         *    -> mini-link-6691-1:BONDING IF-1/1/1 -> (ifIndex=22443322) -> (lowerIfIndex = 21212121)
         *    -> mini-link-6691-1:RAU IF-1/1/1 -> (ifIndex = 22998811) -> (lowerIfIndex = 22665454)
         *    -> mini-link-6691-1:RF-1/1.1/1
         *
         * results: mini-link-6691-1:WAN-1/1/1 -> mini-link-6691-1:BONDING IF-1/1/1 -> mini-link-6691-1:RAU IF-1/1/1 -> mini-link-6691-1:RF-1/1.1/1
         */

         final Set<Long> ifIndexList = ifIndexToName.keySet();
         WriteTransaction transaction = dataBroker.newWriteOnlyTransaction();

         for (Long ifIndex : ifIndexList) {
            IfIndexData ifIndexData = ifIndexToName.get(ifIndex);
            if (ifIndexData == null) {
                LOG.error("SnmpAgent.buildInterfaceStackingChain: ifIndexData null for ifIndex {}", ifIndex);
                continue;
            }
            String ifDescr = ifIndexData.getIfDescr();
            if (!IfRefStackHandler.isWanLanInterface(ifDescr)) { // && !IfRefStackHandler.isBondingInterface(ifDescr)) {
                LOG.debug("SnmpAgent.buildInterfaceStackingChain: ifIndexData {} {} is not for WAN LAN BONDIING",
                        ifIndex, ifDescr);
                continue;
            }

            List<String> lowerIfRefList = new ArrayList<>();

            String ifRef = ifIndexData.getIfRef();
            String wanIfRef = ifRef;
            Long lowerIfIndex = ifIndexData.getIfLowerIfIndex();
            if (lowerIfIndex == null) {
                continue;
            }
            Long lowerIfIndexWan = lowerIfIndex;

            IfIndexData ifIndexDataLower = ifIndexToName.get(lowerIfIndex);
            if (ifIndexDataLower != null) {
                String lowerIfRef = ifIndexDataLower.getIfRef();
                if (lowerIfRef != null) {
                    lowerIfRefList.add(lowerIfRef);
                } else {
                    lowerIfIndex = null;
                }
            }

            lowerIfIndex = ifIndexDataLower.getIfLowerIfIndex();
            while (lowerIfIndex != null) {
                ifIndexDataLower = ifIndexToName.get(lowerIfIndex);
                if (ifIndexDataLower != null) {
                    String lowerIfRef = ifIndexDataLower.getIfRef();
                    if (lowerIfRef != null) {
                        lowerIfRefList.add(lowerIfRef);
                        lowerIfIndex = ifIndexDataLower.getIfLowerIfIndex();
                    } else {
                       lowerIfIndex = null;
                    }
                } else {
                    lowerIfIndex = null;
                }
            }

            List<String> lowerIfRefList2 = new ArrayList<>();

            ifIndexData = ifIndexToName.get(lowerIfIndexWan);
            ifRef = ifIndexData.getIfRef();
            // potential second if-stack related to bonding interface
            lowerIfIndex = ifIndexData.getIfLowerIfIndex2();
            if (lowerIfIndex != null) {
                lowerIfRefList2.add(ifIndexData.getIfRef()); // adding bonding ifIndex
                ifIndexDataLower = ifIndexToName.get(lowerIfIndex);
                if (ifIndexDataLower != null) {
                    String lowerIfRef = ifIndexDataLower.getIfRef();
                    if (lowerIfRef != null) {
                        lowerIfRefList2.add(lowerIfRef);
                    } else {
                        lowerIfIndex = null;
                    }
                }

                lowerIfIndex = ifIndexDataLower.getIfLowerIfIndex();
                while (lowerIfIndex != null) {
                    ifIndexDataLower = ifIndexToName.get(lowerIfIndex);
                    if (ifIndexDataLower != null) {
                        String lowerIfRef = ifIndexDataLower.getIfRef();
                        if (lowerIfRef != null) {
                            lowerIfRefList2.add(lowerIfRef);
                            lowerIfIndex = ifIndexDataLower.getIfLowerIfIndex();
                        } else {
                           lowerIfIndex = null;
                        }
                    } else {
                        lowerIfIndex = null;
                    }
                }
            }

            /*
            * if ref -> List {lowerIfRef}
            */
            IfStackEntryKey ifStackEntryKey = new IfStackEntryKey(wanIfRef);
            IfStackEntryBuilder ifStackEntryBuilder = new IfStackEntryBuilder();
            ifStackEntryBuilder.setKey(ifStackEntryKey);
            ifStackEntryBuilder.setIfRef(wanIfRef);
            ifStackEntryBuilder.setIfRefStack(lowerIfRefList);
            if (!lowerIfRefList2.isEmpty()) {
                ifStackEntryBuilder.setIfRefStack2(lowerIfRefList2);
            }
            InstanceIdentifier<IfStackEntry> iid = InstanceIdentifier.create(IfStackTable.class)
                    .child(IfStackEntry.class, ifStackEntryKey);
            transaction.put(LogicalDatastoreType.OPERATIONAL, iid, ifStackEntryBuilder.build(), true);
        }

        try {
            transaction.submit().get();
        } catch (final Exception e) {
            LOG.error("SnmpAgent.storeAll ", e);
        }
    }

    @Override
    public void setNodeSnmpCommunity(final IpAddress nodeIpAddress,
            final String readCommunity, final String writeCommunity) {
        LOG.info("SnmpAgent.setNodeSnmpCommunity. {} {} {}", nodeIpAddress, readCommunity, writeCommunity);
        communityHandler.addNodeReadCommunity(nodeIpAddress, readCommunity);
        communityHandler.addNodeWriteCommunity(nodeIpAddress, writeCommunity);
    }

    @Override
    public void loadInterfaceData(final String nodeId, final IpAddress nodeIpAddress,
            java.lang.Class<? extends ProductNameBase> productNameClass) {
        if (MINILINK6691.class.isAssignableFrom(productNameClass)) {
            load66xxInterfaceData(nodeId, nodeIpAddress);
        } else if (MINILINK6351.class.isAssignableFrom(productNameClass) ||
                   MINILINK6352.class.isAssignableFrom(productNameClass)) {
            load63xxInterfaceData(nodeId, nodeIpAddress);
        }

    }

    private void load63xxInterfaceData(final String nodeId, final IpAddress nodeIpAddress) {
        /*
         *  IfXTable
         */
        final SnmpGetInputBuilder snmpGetInputBuilder = new SnmpGetInputBuilder();
        snmpGetInputBuilder.setCommunity(communityHandler.getNodeReadCommunity(nodeIpAddress));
        snmpGetInputBuilder.setIpAddress(nodeIpAddress.getIpv4Address());
        snmpGetInputBuilder.setGetType(SnmpGetType.GETBULK);
        snmpGetInputBuilder.setOid(IFXTABLE);
        final SnmpService snmpService = getSnmpService();
        Future<RpcResult<SnmpGetOutput>> snmpGetOutput = snmpService.snmpGet(snmpGetInputBuilder.build());

        SnmpGetOutput rpcResultIfXTable = null;
        try {
            RpcResult<SnmpGetOutput> output = snmpGetOutput.get();
            if (output != null && output.isSuccessful()) {
                rpcResultIfXTable = output.getResult();
                if (rpcResultIfXTable == null || rpcResultIfXTable.getResults().isEmpty()) {
                    LOG.warn("SnmpAgent.loadNodeInterfaceData: ifxtable not found or empty {} {}",
                            nodeId, nodeIpAddress);
                    return;
                }
            }
        } catch (final InterruptedException | ExecutionException e) {
            LOG.warn("SnmpAgent.loadNodeInterfaceData: failed to get the node properties", e);
        }

        /*
         *  IfTable
        **/
        snmpGetInputBuilder.setOid(IFTABLE);
        snmpGetOutput = snmpService.snmpGet(snmpGetInputBuilder.build());

        SnmpGetOutput rpcResultIfTable = null;
        try {
            RpcResult<SnmpGetOutput> output = snmpGetOutput.get();
            if (output != null && output.isSuccessful()) {
                rpcResultIfTable = output.getResult();
                if (rpcResultIfTable == null || rpcResultIfTable.getResults().isEmpty()) {
                    LOG.warn("SnmpAgent.loadNodeInterfaceData: iftable not found or empty {} {}",
                            nodeId, nodeIpAddress);
                    return;
                }
            }
        } catch (final InterruptedException | ExecutionException e) {
            LOG.warn("SnmpAgent.loadNodeInterfaceData: Failed to get the node properties ", e);
        }

        if (rpcResultIfXTable != null) {
            store63xxIfTableData(nodeId, rpcResultIfXTable.getResults());
        }
    }

    private void load66xxInterfaceData(final String nodeId, final IpAddress nodeIpAddress) {
        /*
         *  IfXTable
         */
        final SnmpGetInputBuilder snmpGetInputBuilder = new SnmpGetInputBuilder();
        snmpGetInputBuilder.setCommunity(communityHandler.getNodeReadCommunity(nodeIpAddress));
        snmpGetInputBuilder.setIpAddress(nodeIpAddress.getIpv4Address());
        snmpGetInputBuilder.setGetType(SnmpGetType.GETBULK);
        snmpGetInputBuilder.setOid(IFXTABLE);
        final SnmpService snmpService = getSnmpService();
        Future<RpcResult<SnmpGetOutput>> snmpGetOutput = snmpService.snmpGet(snmpGetInputBuilder.build());

        SnmpGetOutput rpcResultIfXTable = null;
        try {
            RpcResult<SnmpGetOutput> output = snmpGetOutput.get();
            if (output != null && output.isSuccessful()) {
                rpcResultIfXTable = output.getResult();
                if (rpcResultIfXTable == null || rpcResultIfXTable.getResults().isEmpty()) {
                    LOG.warn("SnmpAgent.loadNodeInterfaceData: ifxtable not found or empty {} {}",
                            nodeId, nodeIpAddress);
                    return;
                }
            }
        } catch (final InterruptedException | ExecutionException e) {
            LOG.warn("SnmpAgent.loadNodeInterfaceData: failed to get the node properties", e);
        }

        /*
         *  IfTable
        **/
        snmpGetInputBuilder.setOid(IFTABLE);
        snmpGetOutput = snmpService.snmpGet(snmpGetInputBuilder.build());

        SnmpGetOutput rpcResultIfTable = null;
        try {
            RpcResult<SnmpGetOutput> output = snmpGetOutput.get();
            if (output != null && output.isSuccessful()) {
                rpcResultIfTable = output.getResult();
                if (rpcResultIfTable == null || rpcResultIfTable.getResults().isEmpty()) {
                    LOG.warn("SnmpAgent.loadNodeInterfaceData: iftable not found or empty {} {}",
                            nodeId, nodeIpAddress);
                    return;
                }
            }
        } catch (final InterruptedException | ExecutionException e) {
            LOG.warn("SnmpAgent.loadNodeInterfaceData: Failed to get the node properties ", e);
        }

        /*
         *  XfLiMappingTable
         **/
        snmpGetInputBuilder.setOid(XFLIMAPPINGTABLE);
        snmpGetOutput = snmpService.snmpGet(snmpGetInputBuilder.build());

        SnmpGetOutput rpcResultXfLiTable = null;
        try {
            RpcResult<SnmpGetOutput> output = snmpGetOutput.get();
            if (output != null && output.isSuccessful()) {
                rpcResultXfLiTable = output.getResult();
                if (rpcResultXfLiTable == null || rpcResultXfLiTable.getResults().isEmpty()) {
                    LOG.warn("SnmpAgent.loadNodeInterfaceData: XfLiMappingTable not found or empty {} {}",
                            nodeId, nodeIpAddress);
                }
            }
        } catch (final InterruptedException | ExecutionException e) {
            LOG.warn("SnmpAgent.loadNodeInterfaceData: failed to get the node properties", e);
        }

        /*
         *  XfCarrierTerminationTable
         **/
        snmpGetInputBuilder.setOid(XFCARRIERTERMINATIONTABLE);
        snmpGetOutput = snmpService.snmpGet(snmpGetInputBuilder.build());

        SnmpGetOutput rpcResultCarrierTerminationTable = null;
        try {
            RpcResult<SnmpGetOutput> output = snmpGetOutput.get();
            if (output != null && output.isSuccessful()) {
                rpcResultCarrierTerminationTable = output.getResult();
                if (rpcResultCarrierTerminationTable == null || rpcResultCarrierTerminationTable.getResults().isEmpty()) {
                    LOG.warn("SnmpAgent.loadNodeInterfaceData: rpcResultCarrierTerminationTable not found or empty {} {}",
                            nodeId, nodeIpAddress);
                }
            }
        } catch (final InterruptedException | ExecutionException e) {
            LOG.warn("SnmpAgent.loadNodeInterfaceData: failed to get the node properties", e);
        }

        /*
         * IfStackTable
         */
        snmpGetInputBuilder.setCommunity(communityHandler.getNodeReadCommunity(nodeIpAddress));
        snmpGetInputBuilder.setOid(IFSTACKTABLE);
        snmpGetInputBuilder.setGetType(SnmpGetType.GETWALK);
        snmpGetOutput = snmpService.snmpGet(snmpGetInputBuilder.build());

        SnmpGetOutput rpcResultIfStackTable = null;
        try {
            RpcResult<SnmpGetOutput> output = snmpGetOutput.get();
            if (output != null && output.isSuccessful()) {
                rpcResultIfStackTable = output.getResult();
                if (rpcResultIfStackTable == null || rpcResultIfStackTable.getResults().isEmpty()) {
                    LOG.warn("SnmpAgent.loadNodeInterfaceData: ifstacktable not found or empty {} {}",
                            nodeId, nodeIpAddress);
                }
            }
        } catch (final InterruptedException | ExecutionException e) {
            LOG.warn("SnmpAgent.loadNodeInterfaceData: failed to get the node properties", e);
        }

        snmpGetInputBuilder.setOid(XFETHERNETIFUSAGE);
        snmpGetInputBuilder.setGetType(SnmpGetType.GETBULK);
        snmpGetOutput = snmpService.snmpGet(snmpGetInputBuilder.build());

        SnmpGetOutput rpcResultIfEthernetUsageTable = null;
        try {
            RpcResult<SnmpGetOutput> output = snmpGetOutput.get();
            if (output != null && output.isSuccessful()) {
                rpcResultIfEthernetUsageTable = output.getResult();
                if (rpcResultIfEthernetUsageTable == null || rpcResultIfEthernetUsageTable.getResults().isEmpty()) {
                    LOG.warn("SnmpAgent.loadNodeInterfaceData: xfethernetifusage not found or empty {} {}",
                            nodeId, nodeIpAddress);
                }
            }
        } catch (final InterruptedException | ExecutionException e) {
            LOG.warn("SnmpAgent.loadNodeInterfaceData: failed to get the node properties", e);
        }

        store66xxIfTableData(nodeId,
              (rpcResultIfXTable != null) ? rpcResultIfXTable.getResults() : null,
              (rpcResultIfTable != null) ? rpcResultIfTable.getResults() : null,
              (rpcResultXfLiTable != null) ? rpcResultXfLiTable.getResults() : null,
              (rpcResultCarrierTerminationTable != null) ? rpcResultCarrierTerminationTable.getResults() : null,
              (rpcResultIfStackTable != null) ? rpcResultIfStackTable.getResults() : null,
              (rpcResultIfEthernetUsageTable != null) ? rpcResultIfEthernetUsageTable.getResults() : null);
    }

    private ObjectList oidLookUp(final String mibName, final String objectType) {
        final ObjectListKey objectListKey = new ObjectListKey(mibName, objectType);
        final InstanceIdentifier<ObjectList> path = InstanceIdentifier.create(MibObjectOid.class)
                .child(ObjectList.class, objectListKey);
        try {
            final ReadOnlyTransaction transaction = dataBroker.newReadOnlyTransaction();
            final Optional<ObjectList> optionalObjectList =
                    transaction.read(LogicalDatastoreType.OPERATIONAL, path).get();
            if (optionalObjectList == null || !optionalObjectList.isPresent()) {
                return null;
            }
            return optionalObjectList.get();

        } catch (final InterruptedException e) {
            LOG.error("SnmpAgent.oidLookUp: interrupted exception", e);
        } catch (final ExecutionException e) {
            LOG.error("SnmpAgent.oidLookUp: execution exception", e);
        }

        return null;
    }

    private Long ifIndexLookUp(final String ifName, boolean logicalIndexRequired) {
        LOG.info("SnmpAgent.ifIndexLookUp: {} {}", ifName, logicalIndexRequired);
        final IfEntryKey ifEntryKey = new IfEntryKey(ifName);
        final InstanceIdentifier<IfEntry> path = InstanceIdentifier.create(IfRefTable.class)
                .child(IfEntry.class, ifEntryKey);
        try {
            final ReadOnlyTransaction transaction = dataBroker.newReadOnlyTransaction();
            final Optional<IfEntry> optionalIfEntry =
                    transaction.read(LogicalDatastoreType.OPERATIONAL, path).get();
            if (optionalIfEntry == null || !optionalIfEntry.isPresent()) {
                return null;
            }
            final IfEntry ifEntry = optionalIfEntry.get();
            if (ifEntry == null) {
                return null;
            }
            if (logicalIndexRequired) {
                return ifEntry.getIfLiEntLogicalIndex();
            }

            return ifEntry.getIfIndex();

        } catch (final InterruptedException e) {
            LOG.error("SnmpAgent.ifIndexLookUp: interrupted exception", e);
        } catch (final ExecutionException e) {
            LOG.error("SnmpAgent.ifIndexLookUp: execution exception", e);
        }

        return null;
    }

    private Long radioFrameIdLookUp(final String ifName) {
        final IfEntryKey ifEntryKey = new IfEntryKey(ifName);
        final InstanceIdentifier<IfEntry> path = InstanceIdentifier.create(IfRefTable.class)
                .child(IfEntry.class, ifEntryKey);
        try {
            final ReadOnlyTransaction transaction = dataBroker.newReadOnlyTransaction();
            final Optional<IfEntry> optionalIfEntry =
                    transaction.read(LogicalDatastoreType.OPERATIONAL, path).get();
            if (optionalIfEntry == null || !optionalIfEntry.isPresent()) {
                return null;
            }
            final IfEntry ifEntry = optionalIfEntry.get();
            if (ifEntry == null) {
                return null;
            }

            return ifEntry.getIfRadioFrameId();

        } catch (final InterruptedException e) {
            LOG.error("SnmpAgent.ifIndexLookUp: interrupted exception", e);
        } catch (final ExecutionException e) {
            LOG.error("SnmpAgent.ifIndexLookUp: execution exception", e);
        }

        return null;
    }

    private String setForeignKeyOid(final String oid) {
        String[] splitted = oid.split("\\.");
        // foreign key is always placed in first position
        splitted[splitted.length-2] = "1";
        String oid2 = splitted[0].concat(".");

        for (int i = 1; i < (splitted.length-1); i++) {
            oid2 = oid2.concat(splitted[i]).concat(".");
        }
        oid2 = oid2.concat(splitted[splitted.length-1]);

        LOG.info("SnmpAgent.setForeignKeyOid: oid2 {}", oid2);

        return oid2;
    }

    private String getInterfaceValueWithForeignKey(final String oid, final SnmpGetInputBuilder snmpGetInputBuilder,
            final Long radioFrameId) {
        String oid2 = setForeignKeyOid(oid);

        snmpGetInputBuilder.setOid(oid2);
        snmpGetInputBuilder.setGetType(SnmpGetType.GETBULK);

        final SnmpService snmpService = getSnmpService();
        Future<RpcResult<SnmpGetOutput>> snmpGetOutput = snmpService.snmpGet(snmpGetInputBuilder.build());

        try {
            final RpcResult<SnmpGetOutput> output = snmpGetOutput.get();
            if (output != null && output.isSuccessful()) {
                final SnmpGetOutput rpcResult = output.getResult();
                if ((rpcResult == null || rpcResult.getResults().isEmpty())) {
                    LOG.error("SnmpAgent.getInterfaceValueWithForeignKey: rpcResult null or empty {}", oid2);
                    return null;
                }
                final List<Results> lResults = rpcResult.getResults();
                if (lResults == null || lResults.isEmpty()) {
                    LOG.error("SnmpAgent.getInterfaceValueWithForeignKey: results list null or empty {}", oid2);
                    return null;
                }
                for (Results results : lResults) {
                    Long rxRadioFrameId = new Long(Long.parseLong(results.getValue()));
                    if (rxRadioFrameId.equals(radioFrameId)) {
                        // returning back the last positioned number inside the OID
                        return Long.toString(oidToIfIndex(results.getOid()));
                    }
                }
            }
        } catch (final InterruptedException | ExecutionException e) {
            LOG.warn("SnmpAgent.getInterfaceValue: failed to get value", e);
        }

        return null;
    }

    @Override
    public String getInterfaceValue(final IpAddress nodeIpAddress, final String ifName,
            final String mibName, final String objectType) {
        final ObjectList objectList = oidLookUp(mibName, objectType);
        if (objectList == null) {
            LOG.error("SnmpAgent.getInterfaceValue: {} {} objectList null",
                     mibName, objectType);
            return null;
        }
        String prefixOid = objectList.getOid();
        if (prefixOid == null) {
            LOG.error("SnmpAgent.getInterfaceValue: {} {} prefix oid null",
                    mibName, objectType);
            return null;
        }
        final Long ifIndex = ifIndexLookUp(ifName, objectList.isLogicalIndexRequired());
        if (ifIndex == null) {
            LOG.error("SnmpAgent.getInterfaceValue: {} {} {} {} ifIndex null",
                     mibName, objectType, ifName, objectList.isLogicalIndexRequired());
            return null;
        }
        String oid = prefixOid.concat(".").concat(Long.toString(ifIndex));

        SnmpGetInputBuilder snmpGetInputBuilder = new SnmpGetInputBuilder();
        snmpGetInputBuilder.setCommunity(communityHandler.getNodeReadCommunity(nodeIpAddress));
        snmpGetInputBuilder.setIpAddress(nodeIpAddress.getIpv4Address());

        final boolean isRadioFrameIdRequired = objectList.isRadioFrameIdRequired();
        String internalIndex = null;
        if (isRadioFrameIdRequired) {
            Long radioFrameId = radioFrameIdLookUp(ifName);
            if (radioFrameId != null) {
                internalIndex = getInterfaceValueWithForeignKey(oid, snmpGetInputBuilder,
                        radioFrameId);
            }
        }
        if (isRadioFrameIdRequired && internalIndex != null) {
            oid = oid.concat(".").concat(internalIndex);
        }

        LOG.info("SnmpAgent.getInterfaceValue: isRadioFrameIdRequired {} internalIndex {} oid {}",
               isRadioFrameIdRequired, internalIndex, oid);

        snmpGetInputBuilder.setGetType(SnmpGetType.GET);
        snmpGetInputBuilder.setOid(oid);

        final SnmpService snmpService = getSnmpService();
        Future<RpcResult<SnmpGetOutput>> snmpGetOutput = snmpService.snmpGet(snmpGetInputBuilder.build());

        try {
            final RpcResult<SnmpGetOutput> output = snmpGetOutput.get();
            if (output != null && output.isSuccessful()) {
                final SnmpGetOutput rpcResult = output.getResult();
                if ((rpcResult == null || rpcResult.getResults().isEmpty())) {
                    LOG.error("SnmpAgent.getInterfaceValue: rpcResult null or empty {} {} {} {}",
                            mibName, objectType, ifName, oid);
                    return null;
                }
                final List<Results> lResults = rpcResult.getResults();
                if (lResults == null || lResults.isEmpty()) {
                    LOG.error("SnmpAgent.getInterfaceValue: results list null or empty {} {} {} {}",
                            mibName, objectType, ifName, oid);
                }

                return lResults.get(0).getValue();
            }
        } catch (final InterruptedException | ExecutionException e) {
            LOG.warn("SnmpAgent.getInterfaceValue: failed to get value", e);
        }

        return null;
    }

    @Override
    public boolean setInterfaceValue(final IpAddress nodeIpAddress, final String ifName,
            final String mibName, final String objectType,
            final java.lang.Class<? extends BaseValueType> valueType,
            final String value) {
        final ObjectList objectList = oidLookUp(mibName, objectType);
        if (objectList == null) {
            LOG.error("SnmpAgent.setInterfaceValue: {} {} objectList null",
                     mibName, objectType);
            return false;
        }
        String prefixOid = objectList.getOid();
        if (prefixOid == null) {
            LOG.error("SnmpAgent.setInterfaceValue: {} {} prefix oid null",
                    mibName, objectType);
            return false;
        }
        final Long ifIndex = ifIndexLookUp(ifName, objectList.isLogicalIndexRequired());
        if (ifIndex == null) {
            LOG.error("SnmpAgent.setInterfaceValue: {} {} {} {} ifIndex null",
                     mibName, objectType, ifName, objectList.isLogicalIndexRequired());
            return false;
        }
        final String oid = prefixOid.concat(".").concat(Long.toString(ifIndex));

        SnmpSetInputBuilder snmpSetInputBuilder = new SnmpSetInputBuilder();
        snmpSetInputBuilder.setCommunity(communityHandler.getNodeWriteCommunity(nodeIpAddress));
        snmpSetInputBuilder.setIpAddress(nodeIpAddress.getIpv4Address());
        snmpSetInputBuilder.setOid(oid);
        snmpSetInputBuilder.setValueType(valueType);
        snmpSetInputBuilder.setValue(value);

        final SnmpService snmpService = getSnmpService();
        Future<RpcResult<Void>> snmpSetOutput = snmpService.snmpSet(snmpSetInputBuilder.build());

        try {
            final RpcResult<Void> output = snmpSetOutput.get();
            LOG.info("SnmpAgent.setInterfaceValue: rpc result {}", output);
            if (output != null && output.isSuccessful()) {
                return true;
            }
        } catch (final InterruptedException | ExecutionException e) {
            LOG.warn("SnmpAgent.setInterfaceValue: failed to get value", e);
        }

        return false;
    }

    @Override
    public List<String> getInterfaceStack(final String ifRef, final boolean ifStack2) {
        final IfStackEntryKey ifStackEntryKey = new IfStackEntryKey(ifRef);
        final IfStackEntryBuilder ifStackEntryBuilder = new IfStackEntryBuilder();
        ifStackEntryBuilder.setKey(ifStackEntryKey);
        final InstanceIdentifier<IfStackEntry> iid = InstanceIdentifier.create(IfStackTable.class)
                .child(IfStackEntry.class, ifStackEntryKey);
        try {
            final ReadOnlyTransaction transaction = dataBroker.newReadOnlyTransaction();
            final Optional<IfStackEntry> optionalIfStackEntry =
                    transaction.read(LogicalDatastoreType.OPERATIONAL, iid).get();
            if (optionalIfStackEntry == null || !optionalIfStackEntry.isPresent()) {
                return null;
            }
            final IfStackEntry ifStackEntry = optionalIfStackEntry.get();
            if (ifStackEntry == null) {
                return null;
            }

            return ifStack2 ? ifStackEntry.getIfRefStack2() : ifStackEntry.getIfRefStack();

        } catch (final InterruptedException e) {
            LOG.error("SnmpAgent.getInterfaceStack: interrupted exception", e);
        } catch (final ExecutionException e) {
            LOG.error("SnmpAgent.getInterfaceStack: execution exception", e);
        }

        return null;
    }

    @Override
    public Future<RpcResult<GetInterfaceValueOutput>> getInterfaceValue(GetInterfaceValueInput input) {
        LOG.info("SnmpAgent.getInterfaceValue: {} {} {} {} ", input.getNodeIpAddress(), input.getIfRef(),
                input.getMibName(), input.getObjectType());
        final String value = getInterfaceValue(input.getNodeIpAddress(), input.getIfRef(),
                input.getMibName(), input.getObjectType());
        GetInterfaceValueOutputBuilder getInterfaceValueOutputBuilder  = new GetInterfaceValueOutputBuilder();
        getInterfaceValueOutputBuilder.setValue(value);

        return Futures.immediateFuture(RpcResultBuilder.<GetInterfaceValueOutput> success()
                .withResult(getInterfaceValueOutputBuilder.build()).build());
    }

    @Override
    public Future<RpcResult<SetInterfaceValueOutput>> setInterfaceValue(SetInterfaceValueInput input) {
        LOG.info("SnmpAgent.setInterfaceValue: {} {} {} {} {} {}", input.getNodeIpAddress(), input.getIfRef(),
                input.getMibName(), input.getObjectType(), input.getValueType(), input.getValue());
        final boolean result = setInterfaceValue(input.getNodeIpAddress(), input.getIfRef(),
                input.getMibName(), input.getObjectType(), input.getValueType(), input.getValue());
        SetInterfaceValueOutputBuilder setInterfaceValueOutputBuilder  = new SetInterfaceValueOutputBuilder();
        setInterfaceValueOutputBuilder.setResultOk(result);

        return Futures.immediateFuture(RpcResultBuilder.<SetInterfaceValueOutput> success()
                .withResult(setInterfaceValueOutputBuilder.build()).build());
    }

    @Override
    public Integer getBridgePortId(final String ifRef) {
        final IfEntryKey ifEntryKey = new IfEntryKey(ifRef);
        final InstanceIdentifier<IfEntry> iid = InstanceIdentifier.create(IfRefTable.class)
                .child(IfEntry.class, ifEntryKey);
        try {
            final ReadOnlyTransaction transaction = dataBroker.newReadOnlyTransaction();
            final Optional<IfEntry> optionalIfEntry =
                    transaction.read(LogicalDatastoreType.OPERATIONAL, iid).get();
            if (optionalIfEntry == null || !optionalIfEntry.isPresent()) {
                return null;
            }
            final IfEntry ifEntry = optionalIfEntry.get();
            if (ifEntry == null) {
                return null;
            }

            return ifEntry.getBridgePortId();

        } catch (final InterruptedException e) {
            LOG.error("SnmpAgent.getBridgePort: interrupted exception", e);
        } catch (final ExecutionException e) {
            LOG.error("SnmpAgent.getBridgePort: execution exception", e);
        }

        return null;
    }

    private int getMask(final Integer bridgePortId, int currentMask) {
        if (bridgePortId == 0) {
            return currentMask;
        }
        /*
         * one-based numbering
         */
        if (bridgePortId == 1) {
            currentMask = currentMask | 0x80;
        } else if (bridgePortId == 2) {
            currentMask = currentMask | 0x40;
        } else if (bridgePortId == 3) {
            currentMask = currentMask | 0x20;
        } else if (bridgePortId == 4) {
            currentMask = currentMask | 0x10;
        } else if (bridgePortId == 5) {
            currentMask = currentMask | 0x08;
        } else if (bridgePortId == 6) {
            currentMask = currentMask | 0x04;
        } else if (bridgePortId == 7) {
            currentMask = currentMask | 0x02;
        } else if (bridgePortId == 8) {
            currentMask = currentMask | 0x01;
        }

        return currentMask;
    }

    @Override
    public boolean setLinkLagMembership(final IpAddress nodeIpAddress, final List<String> ifRefList,
            final String ifRefMaster) {
        int lagMembersMask = 0;
        for (String ifRef : ifRefList) {
             Integer bpId = getBridgePortId(ifRef);
             lagMembersMask = getMask(bpId, lagMembersMask);
        }
        Integer bpMasterId = getBridgePortId(ifRefMaster);
        Integer mask = new Integer(lagMembersMask);

        LOG.info("SnmpAgent.setLinkLagMembership: lagMembersMask {} bpMasterId {}, mask {}",
                lagMembersMask, bpMasterId, mask);

        String portList = Integer.toHexString(mask);
        LOG.info("SnmpAgent.setLinkLagMembership: portList {}", portList);
        portList = portList.concat(":00");
        LOG.info("SnmpAgent.setLinkLagMembership: portList {}", portList);
        return setLagMembership(nodeIpAddress, ifRefList.get(0), bpMasterId, portList);
    }

    private boolean setLagMembership(final IpAddress nodeIpAddress, final String ifName,
            final Integer bpMasterId, final String value) {
        final ObjectList objectList = oidLookUp("xfEthernetBridgeMIB", "xfLagMembers");
        if (objectList == null) {
            LOG.error("SnmpAgent.setLagMembership: xfEthernetBridgeMIB xfLagMembers objectList null");
            return false;
        }
        String prefixOid = objectList.getOid();
        if (prefixOid == null) {
            LOG.error("SnmpAgent.setLagMembership: xfEthernetBridgeMIB xfLagMembers prefix oid null");
            return false;
        }
        final String oid = prefixOid.concat(".").concat(Integer.toString(bpMasterId));

        SnmpSetInputBuilder snmpSetInputBuilder = new SnmpSetInputBuilder();
        snmpSetInputBuilder.setCommunity(communityHandler.getNodeWriteCommunity(nodeIpAddress));
        snmpSetInputBuilder.setIpAddress(nodeIpAddress.getIpv4Address());
        snmpSetInputBuilder.setOid(oid);
        snmpSetInputBuilder.setValueType(ValueTypeHexString.class);
        snmpSetInputBuilder.setValue(value);

        final SnmpService snmpService = getSnmpService();
        Future<RpcResult<Void>> snmpSetOutput = snmpService.snmpSet(snmpSetInputBuilder.build());

        try {
            final RpcResult<Void> output = snmpSetOutput.get();
            LOG.info("SnmpAgent.setLagMembership: rpc result {}", output);
            if (output != null && output.isSuccessful()) {
                return true;
            }
        } catch (final InterruptedException | ExecutionException e) {
            LOG.warn("SnmpAgent.setLagMembership: failed to get value", e);
        }

        return false;
    }
}
