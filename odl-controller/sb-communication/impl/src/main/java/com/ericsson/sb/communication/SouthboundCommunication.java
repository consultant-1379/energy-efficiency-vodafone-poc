/*
 * Copyright (c) 2017 Ericsson, AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.sb.communication;

import com.ericsson.snmp.agent.SnmpAgentOperations;
import com.ericsson.snmp.agent.SnmpNodeSync;
import com.ericsson.snmp.agent.SnmpNodeSyncData;
import com.ericsson.snmp.agent.IfRefStackHandler;
import com.ericsson.snmp.agent.IfRefAdminStatusHandler;
import com.ericsson.snmp.agent.IfRefOperStatusHandler;
import com.ericsson.cli.plugin.CliPluginAgentOperations;
import com.ericsson.cli.plugin.SshSessionHandler;
import com.ericsson.cli.plugin.Device;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Future;
import java.util.List;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.rev161116.networks.Network;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.rev161116.NetworkId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.topology.inventory.rev170714.ProductNameBase;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.topology.inventory.rev170714.MINILINK6351;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.topology.inventory.rev170714.MINILINK6352;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.topology.inventory.rev170714.MINILINK6691;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.ValueTypeHexString;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.ValueTypeInt32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ericsson
 */
public class SouthboundCommunication implements SouthboundCommunicationService, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(SouthboundCommunication.class);
    private static final Long ZERO_LONG = new Long(0);
    private DataBroker dataBroker;
    private RpcProviderRegistry rpcProviderRegistry;
    private SnmpAgentOperations snmpAgentOperations;
    private CliPluginAgentOperations cliPluginAgentOperations;
    private SnmpNodeSync snmpNodeSync = new SnmpNodeSync();
    private Thread snmpNodeSyncThread;
    private boolean started = false;


   /**
     * Starts BasicNetworkFunctions
     */
    public void startup() {
        LOG.info("SouthboundCommunication.startup");
        snmpNodeSync.init(dataBroker, snmpAgentOperations);
        snmpNodeSyncThread = new Thread(snmpNodeSync);
        snmpNodeSyncThread.setDaemon(true);
        snmpNodeSyncThread.setName("SnmpSync:");
        snmpNodeSyncThread.start();
    }

    @Override
    public void close() throws InterruptedException {
        if (snmpNodeSyncThread != null) {
            snmpNodeSyncThread.interrupt();
            snmpNodeSyncThread.join();
            snmpNodeSyncThread = null;
        }
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
        LOG.info("SouthboundCommunication.setDataBroker");
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
        LOG.info("SouthboundCommunication.setRpcRegistry");
        this.rpcProviderRegistry = rpcProviderRegistry;
    }

     /**
     * Returns the snmpAgentOperations
     *
     * @return snmpAgentOperations
     */
    public SnmpAgentOperations getSnmpAgentOperations() {
        return snmpAgentOperations;
    }

   /**
     * Sets the setSnmpAgentOperations
     *
     * @param snmpAgentOperations
     */
    public void setSnmpAgentOperations(final SnmpAgentOperations snmpAgentOperations) {
        LOG.info("SouthboundCommunication.setSnmpAgentOperations");
        this.snmpAgentOperations = snmpAgentOperations;
    }

     /**
     * Returns the cliPluginAgentOperations
     *
     * @return cliPluginAgentOperations
     */
    public CliPluginAgentOperations getCliPluginAgentOperations() {
        return cliPluginAgentOperations;
    }

   /**
     * Sets the setCliPluginAgentOperations
     *
     * @param cliPluginAgentOperations
     */
    public void setCliPluginAgentOperations(final CliPluginAgentOperations cliPluginAgentOperations) {
        LOG.info("SouthboundCommunication.setCliPluginAgentOperations");
        this.cliPluginAgentOperations = cliPluginAgentOperations;
    }

    @Override
    public void setNodeSnmpCommunity(final IpAddress nodeIpAddress, final String snmpReadCommunity,
            final String snmpWriteCommunity) {
        snmpAgentOperations.setNodeSnmpCommunity(nodeIpAddress, snmpReadCommunity, snmpWriteCommunity);
    }

    @Override
    public void setNodeCliLoginCredentials(final IpAddress nodeIpAddress, final String userLogin,
            final String userPassword) {
        cliPluginAgentOperations.setNodeLoginCredentials(nodeIpAddress, userLogin, userPassword);
    }

    @Override
    public void loadInterfaceDataFromNodes(final String nodeId, final IpAddress nodeIpAddress,
            final java.lang.Class<? extends ProductNameBase> productNameClass) {
        snmpNodeSync.enqueueOperation(new SnmpNodeSyncData(nodeId, nodeIpAddress, productNameClass));
    }

    /*
     * Current Bandwidth Capacity section
     */
    private Long getXXyyInterfaceCurrentBandwidthCapacity(final IpAddress nodeIpAddress,
            final String ifRef, final String mibObjectName, final String mibObjectType) {
        String value = snmpAgentOperations.getInterfaceValue(nodeIpAddress, ifRef,
                mibObjectName, mibObjectType);
        Long ifCurrentBandwidthCapacity = ZERO_LONG;
        if (value != null) {
            try {
                ifCurrentBandwidthCapacity = Long.parseLong(value)/8;
            } catch (final NumberFormatException e) {
                LOG.warn("SouthboundCommunication.getXXyyInterfaceBandwidthCapacity: {} {} ",
                        nodeIpAddress, ifRef, e);
            }
        }
        return ifCurrentBandwidthCapacity;
    }

    private Long getRauIfInterfaceCurrentBandwidthCapacity(final IpAddress nodeIpAddress,
            final List<String> ifRefStack, final String mibObjectName, final String mibObjectType) {
        Long ifCurrentBandwidthCapacity = ZERO_LONG;

        String ifRefRF = IfRefStackHandler.getRfInterface(ifRefStack);
        if (ifRefRF == null) {
            LOG.error("SouthboundCommunication.getRauIfInterfaceCurrentBandwidthCapacity: no RF interface for nodeIpAddress {} ifRefRF {}",
                    nodeIpAddress, ifRefRF);
            return ZERO_LONG;
        }

        String rxAdminStatus = get66yyInterfaceAdminStatus(nodeIpAddress, ifRefRF);

        LOG.info("SouthboundCommunication.getRauIfInterfaceCurrentBandwidthCapacity: ifRefRF {} rxAdminStatus {}",
                ifRefRF, rxAdminStatus);
        if (rxAdminStatus == null) {
            LOG.error("SouthboundCommunication.getRauIfInterfaceCurrentBandwidthCapacity: no adminStatus available for nodeIpAddress {} ifRefRF {}",
                    nodeIpAddress, ifRefRF);
            return ZERO_LONG;
        }

        if (IfRefAdminStatusHandler.isRfInterfaceAdminStatusUp(rxAdminStatus)) {
            String ifRefRauIF = IfRefStackHandler.getRauInterface(ifRefStack);
            if (ifRefRauIF == null) {
                return ZERO_LONG;
            }

            String value = snmpAgentOperations.getInterfaceValue(nodeIpAddress, ifRefRauIF,
                    mibObjectName, mibObjectType);

            if (value != null) {
                try {
                    ifCurrentBandwidthCapacity = Long.parseLong(value)*1000/8;

                    LOG.info("SouthboundCommunication.getRauIfInterfaceCurrentBandwidthCapacity: nodeIpAddress {} ifRefRauIF {} ifCurrentBandwidthCapacity {}",
                            nodeIpAddress, ifRefRauIF, ifCurrentBandwidthCapacity);
                } catch (final NumberFormatException e) {
                    LOG.warn("SouthboundCommunication.getRauIfInterfaceCurrentBandwidthCapacity: {} {} ",
                            nodeIpAddress, ifRefRauIF, e);
                }
            }
        }

        return ifCurrentBandwidthCapacity;
    }

    private Long getXXyyWanInterfaceCurrentBandwidthCapacity(final IpAddress nodeIpAddress,
            final String ifRef, final String mibObjectName, final String mibObjectType) {
        Long ifCurrentBandwidthCapacity = ZERO_LONG;
        List<String> ifRefStack = snmpAgentOperations.getInterfaceStack(ifRef, false);
        if (ifRefStack == null) {
            return ZERO_LONG;
        }

        ifCurrentBandwidthCapacity = getRauIfInterfaceCurrentBandwidthCapacity(nodeIpAddress,
                ifRefStack, mibObjectName, mibObjectType);

        ifRefStack = snmpAgentOperations.getInterfaceStack(ifRef, true);
        if (ifRefStack == null) {
            return ifCurrentBandwidthCapacity;
        }

        ifCurrentBandwidthCapacity = ifCurrentBandwidthCapacity +
                getRauIfInterfaceCurrentBandwidthCapacity(nodeIpAddress, ifRefStack,
                mibObjectName, mibObjectType);

        return ifCurrentBandwidthCapacity;
    }

    @Override
    public Long getInterfaceCurrentBandwidthCapacity(final IpAddress nodeIpAddress, final String ifRef,
            final java.lang.Class<? extends ProductNameBase> productNameClass) {
        if (MINILINK6691.class.isAssignableFrom(productNameClass)) {
            if (IfRefStackHandler.isLanInterface(ifRef)) {
                return getXXyyInterfaceCurrentBandwidthCapacity(nodeIpAddress, ifRef,
                        "xfEthernetBridgeMIB", "xfEthernetIfMaxSpeed");
            } else if (IfRefStackHandler.isWanInterface(ifRef)) {
                /*
                 * scenario: WAN interface querying the underlying RAU interface(s).
                 * In case of bonding, we have to take into account the admin status
                 * of the underlying RF interfaces
                 */
                return getXXyyWanInterfaceCurrentBandwidthCapacity(nodeIpAddress, ifRef,
                        "xfRadioLinkRltMIB", "xfCarrierTermActualCapacity");
            } else if (IfRefStackHandler.isBondingInterface(ifRef)) {
               /*
               * scenario:Bonding interface which is always present also in case of
               * no actual bonding configuration
               */
               return getXXyyInterfaceCurrentBandwidthCapacity(nodeIpAddress, ifRef,
                        "xfRadioLinkRltMIB", "xfRLTActualTXTotalCapacity");
            } else if (IfRefStackHandler.isRauInterface(ifRef)) {
               // scenario: RAU interface
               return getXXyyInterfaceCurrentBandwidthCapacity(nodeIpAddress, ifRef,
                        "xfRadioLinkRltMIB", "xfCarrierTermActualCapacity");
            }
        } else if (MINILINK6351.class.isAssignableFrom(productNameClass) ||
                   MINILINK6352.class.isAssignableFrom(productNameClass)) {
            return getXXyyInterfaceCurrentBandwidthCapacity(nodeIpAddress, ifRef,
                    "interfaces", "ifSpeed");
        }

        return ZERO_LONG;
    }

    /*
     * Maximum Bandwidth Capacity section
     */
    private Long getXXyyInterfaceMaximumBandwidthCapacity(final IpAddress nodeIpAddress, final String ifRef,
            final String mibObjectName, final String mibObjectType) {
        Long ifMaximumBandwidthCapacity = ZERO_LONG;
        final List<String> ifRefStack = snmpAgentOperations.getInterfaceStack(ifRef, false);
        if (ifRefStack == null) {
            return ifMaximumBandwidthCapacity;
        }
        final String ifRefRauIF = IfRefStackHandler.getRauInterface(ifRefStack);
        if (ifRefRauIF == null) {
            return ifMaximumBandwidthCapacity;
        }

        String value = snmpAgentOperations.getInterfaceValue(nodeIpAddress, ifRefRauIF,
                mibObjectName, mibObjectType);
        if (value != null) {
            try {
                ifMaximumBandwidthCapacity = Long.parseLong(value)*1000/8;
            } catch (final NumberFormatException e) {
                LOG.warn("SouthboundCommunication.getXXyyInterfaceMaximumBandwidthCapacity: ifMaximumBandwidthCapacity ", e);
            }
        }

        return ifMaximumBandwidthCapacity;
    }

    @Override
    public Long getInterfaceMaximumBandwidthCapacity(final IpAddress nodeIpAddress, final String ifRef,
            final java.lang.Class<? extends ProductNameBase> productNameClass) {
        if (MINILINK6691.class.isAssignableFrom(productNameClass)) {
            return getXXyyInterfaceMaximumBandwidthCapacity(nodeIpAddress, ifRef,
                    "xfRadioLinkRltMIB", "xfMaxACMCapacity");
        } else if (MINILINK6351.class.isAssignableFrom(productNameClass) ||
                   MINILINK6352.class.isAssignableFrom(productNameClass)) {
            return getXXyyInterfaceMaximumBandwidthCapacity(nodeIpAddress, ifRef,
                    "ptRadioLinkMIB", "actualTxTotalCapacity");
        }

        return ZERO_LONG;
    }

    /*
     * Output Power section
     */
    private Integer getXXyyInterfaceOutputPower(final IpAddress nodeIpAddress, final String ifRef,
             final String mibObjectName, final String mibObjectType) {
        Integer currentOutputPower = null;
        Integer currentOutputPowerMicroW = 0;

        LOG.info("SouthboundCommunication.getXXyyInterfaceOutputPower: nodeIpAddress {} ifRef {}",
                nodeIpAddress, ifRef);

        List<String> ifRefStack = snmpAgentOperations.getInterfaceStack(ifRef, false);
        if (ifRefStack == null) {
            LOG.error("SouthboundCommunication.getXXyyInterfaceOutputPower: no if stack for nodeIpAddress {} ifRef {}",
                nodeIpAddress, ifRef);
            return currentOutputPowerMicroW;
        }
        String ifRefRF = IfRefStackHandler.getRfInterface(ifRefStack);
        if (ifRefRF == null) {
            LOG.error("SouthboundCommunication.getXXyyInterfaceOutputPower: no RF interface for nodeIpAddress {} ifRef {}",
                nodeIpAddress, ifRef);
            return currentOutputPowerMicroW;
        }

        String rxAdminStatus = get66yyInterfaceAdminStatus(nodeIpAddress, ifRefRF);

        LOG.info("SouthboundCommunication.getXXyyInterfaceOutputPower: ifRefRF {} rxAdminStatus {}",
                ifRefRF, rxAdminStatus);

        if (IfRefAdminStatusHandler.isRfInterfaceAdminStatusUp(rxAdminStatus)) {
            String value = snmpAgentOperations.getInterfaceValue(nodeIpAddress, ifRefRF,
                    mibObjectName, mibObjectType);

            if (value != null) {
                try {
                    currentOutputPower = Integer.parseInt(value);
                    currentOutputPowerMicroW = DBmConverter.dBmToMicroWatts(currentOutputPower);
                } catch (final NumberFormatException e) {
                    LOG.warn("SouthboundCommunication.getXXyyInterfaceOutputPower: currentOutputPower ", e);
                }
            }
        }

        ifRefStack = snmpAgentOperations.getInterfaceStack(ifRef, true);
        if (ifRefStack == null) {
            return currentOutputPowerMicroW;
        }
        ifRefRF = IfRefStackHandler.getRfInterface(ifRefStack);
        if (ifRefRF == null) {
            return currentOutputPowerMicroW;
        }

        rxAdminStatus = get66yyInterfaceAdminStatus(nodeIpAddress, ifRefRF);

        LOG.info("SouthboundCommunication.getXXyyInterfaceOutputPower: stack-2: ifRefRF {} rxAdminStatus {}",
                ifRefRF, rxAdminStatus);

        if (IfRefAdminStatusHandler.isRfInterfaceAdminStatusUp(rxAdminStatus)) {
            String value = snmpAgentOperations.getInterfaceValue(nodeIpAddress, ifRefRF,
                    mibObjectName, mibObjectType);

            if (value != null) {
                try {
                    currentOutputPower = Integer.parseInt(value);
                    currentOutputPowerMicroW = currentOutputPowerMicroW +
                            DBmConverter.dBmToMicroWatts(currentOutputPower);
                } catch (final NumberFormatException e) {
                    LOG.warn("SouthboundCommunication.getXXyyInterfaceOutputPower: currentOutputPower ", e);
                }
            }
        }

        return currentOutputPowerMicroW;
    }

    private Integer get63yyInterfaceOutputPower(final IpAddress nodeIpAddress, final String ifRef,
            final String mibObjectName, final String mibObjectType) {
        Integer currentOutputPower = null;
        Integer currentOutputPowerMicroW = 0;

        List<String> ifRefStack = snmpAgentOperations.getInterfaceStack(ifRef, false);
        if (ifRefStack == null) {
            return currentOutputPowerMicroW;
        }
        String ifRefRF = IfRefStackHandler.getRfInterface(ifRefStack);
        if (ifRefRF == null) {
            return currentOutputPowerMicroW;
        }

        String value = snmpAgentOperations.getInterfaceValue(nodeIpAddress, ifRefRF,
                mibObjectName, mibObjectType);

        LOG.info("SouthboundCommunication.get63yyInterfaceOutputPower: ifRefRF {} {} {} {}",
                ifRefRF, mibObjectName, mibObjectType, value);

        if (value != null) {
            try {
                String[] valueArray = value.split("\\.");
                if (valueArray.length > 0) {
                    currentOutputPower = Integer.parseInt(valueArray[0]);
                    currentOutputPowerMicroW = DBmConverter.dBmToMicroWatts(currentOutputPower);
                }
            } catch (final NumberFormatException e) {
                LOG.warn("SouthboundCommunication.get63yyInterfaceOutputPower: currentOutputPower {} ",
                        value, e);
            }
        }

        LOG.info("SouthboundCommunication.get63yyInterfaceOutputPower: ifRefRF {} {}",
                ifRefRF, currentOutputPowerMicroW);

        ifRefStack = snmpAgentOperations.getInterfaceStack(ifRef, true);
        if (ifRefStack == null) {
            return currentOutputPowerMicroW;
        }
        ifRefRF = IfRefStackHandler.getRfInterface(ifRefStack);
        if (ifRefRF == null) {
            return currentOutputPowerMicroW;
        }

        value = snmpAgentOperations.getInterfaceValue(nodeIpAddress, ifRefRF,
                mibObjectName, mibObjectType);

        if (value != null) {
            try {
                String[] valueArray = value.split("\\.");
                if (valueArray.length > 0) {
                    currentOutputPower = Integer.parseInt(valueArray[0]);
                    currentOutputPowerMicroW = currentOutputPowerMicroW +
                            DBmConverter.dBmToMicroWatts(currentOutputPower);
                }
            } catch (final NumberFormatException e) {
                LOG.warn("SouthboundCommunication.get63yyInterfaceOutputPower: maxOutputPowerSinceReset {} ",
                        value, e);
            }
        }

        return currentOutputPowerMicroW;
    }

    private Integer get63yyInterfaceMaxOutputPowerSinceReset(final IpAddress nodeIpAddress, final String ifRef,
            final String mibObjectName, final String mibObjectType) {
        Integer maxOutputPowerSinceReset = 0;
        Integer maxOutputPowerSinceResetMicroW = 0;

        final List<String> ifRefStack = snmpAgentOperations.getInterfaceStack(ifRef, false);
        if (ifRefStack == null) {
            return maxOutputPowerSinceReset;
        }
        final String ifRefRF = IfRefStackHandler.getRfInterface(ifRefStack);
        if (ifRefRF == null) {
            return maxOutputPowerSinceReset;
        }

        String value = snmpAgentOperations.getInterfaceValue(nodeIpAddress, ifRefRF,
                mibObjectName, mibObjectType);

        if (value != null) {
            try {
                 maxOutputPowerSinceReset = Integer.parseInt(value);
                 maxOutputPowerSinceResetMicroW = maxOutputPowerSinceResetMicroW +
                         DBmConverter.dBmToMicroWatts(maxOutputPowerSinceReset);
            } catch (final NumberFormatException e) {
                LOG.warn("SouthboundCommunication.getXXyyInterfaceMaxOutputPowerSinceReset: maxOutputPowerSinceReset {} ",
                        value, e);
            }
        }

        return maxOutputPowerSinceResetMicroW;
    }

    @Override
    public Integer getInterfaceOutputPower(final IpAddress nodeIpAddress, final String ifRef,
             final java.lang.Class<? extends ProductNameBase> productNameClass) {
       if (MINILINK6691.class.isAssignableFrom(productNameClass)) {
            return getXXyyInterfaceOutputPower(nodeIpAddress, ifRef,
                    "xfRadioLinkPtpRadioMIB", "xfRfCurrentOutputPower");
        } else if (MINILINK6351.class.isAssignableFrom(productNameClass) ||
                   MINILINK6352.class.isAssignableFrom(productNameClass)) {
            return get63yyInterfaceOutputPower(nodeIpAddress, ifRef,
                    "ptRadioLinkMIB", "ActualOutputPower");
        }

        return 0;
    }

    @Override
    public Integer getInterfaceMaxOutputPowerSinceReset(final IpAddress nodeIpAddress, final String ifRef,
            final java.lang.Class<? extends ProductNameBase> productNameClass) {
       if (MINILINK6691.class.isAssignableFrom(productNameClass)) {
            return getXXyyInterfaceOutputPower(nodeIpAddress, ifRef,
                    "xfRadioLinkPtpRadioMIB", "xfRfMaxOutputPowerSinceReset");
        } else if (MINILINK6351.class.isAssignableFrom(productNameClass) ||
                   MINILINK6352.class.isAssignableFrom(productNameClass)) {
            return get63yyInterfaceMaxOutputPowerSinceReset(nodeIpAddress, ifRef,
                    "ptRadioLinkMIB", "selectedMaxOutputPower");
        }

        return 0;
    }

    /*
     * In Octects section
     */
    @Override
    public Long getInterfaceInOctects(final IpAddress nodeIpAddress, final String ifRef,
            final java.lang.Class<? extends ProductNameBase> productNameClass) {
        String value = snmpAgentOperations.getInterfaceValue(nodeIpAddress, ifRef,
                 "ifMIB", "ifHCInOctets");
        Long ifHcInOctets = ZERO_LONG;

        if (value != null) {
            try {
                ifHcInOctets = Long.parseLong(value);
            } catch (final NumberFormatException e) {
                 LOG.warn("SouthboundCommunication.getStatistics: ifHcInOctets ", e);
            }
        }

        return ifHcInOctets;
    }

    /*
     * Out Octects section
     */
    @Override
    public Long getInterfaceOutOctects(final IpAddress nodeIpAddress, final String ifRef,
            final java.lang.Class<? extends ProductNameBase> productNameClass) {
        String value = snmpAgentOperations.getInterfaceValue(nodeIpAddress, ifRef, "ifMIB", "ifHCOutOctets");
        Long ifHcOutOctets = ZERO_LONG;

        if (value != null) {
            try {
                ifHcOutOctets = Long.parseLong(value);
            } catch (final NumberFormatException e) {
                LOG.warn("SouthboundCommunication.getStatistics: ifHcOutOctets ", e);
            }
       }

        return ifHcOutOctets;
    }

    @Override
    public boolean setInterfacesToLag(final IpAddress nodeIpAddress, final List<String> ifRefList,
            final String ifRefMaster) {
        if (ifRefList == null || ifRefList.isEmpty()){
            return false;
        }
        /*
         * configure LAG membership
         */
        return snmpAgentOperations.setLinkLagMembership(nodeIpAddress, ifRefList, ifRefMaster);
    }

    @Override
    public boolean setStackInterfacesAdminStatus(final IpAddress nodeIpAddress, final List<String> ifRefList,
            final boolean txOn, final boolean wanIf, final boolean radioIf) {
        boolean globalResult = true;

        for (String ifRef : ifRefList) {
            List<String> ifRefStack = snmpAgentOperations.getInterfaceStack(ifRef, false);
            LOG.info("SouthboundCommunication.setStackInterfacesAdminStatus: nodeIpAddress {} ifRef {} ifRefStack {}",
                    nodeIpAddress, ifRef, ifRefStack != null);
            if (ifRefStack == null) {
                continue;
            }
            String ifRefRF = IfRefStackHandler.getRfInterface(ifRefStack);
            if (ifRefRF == null) {
                continue;
            }

            String wanAdminValue = IfRefAdminStatusHandler.getWanLanInterfaceAdminStatusValue(txOn);
            String rfAdminValue = IfRefAdminStatusHandler.getRfInterfaceAdminStatusValue(txOn);
            boolean result = false;

            if (txOn) {
                if (wanIf) {
                    result = snmpAgentOperations.setInterfaceValue(nodeIpAddress, ifRef, "interfaces",
                            "ifAdminStatus", ValueTypeInt32.class, wanAdminValue);
                }
                if (radioIf) {
                    result = snmpAgentOperations.setInterfaceValue(nodeIpAddress, ifRefRF, "xfRadioLinkPtpRadioMIB",
                            "xfRFTxAdminStatus", ValueTypeInt32.class, rfAdminValue);
                }
            } else {
                if (radioIf) {
                    result = snmpAgentOperations.setInterfaceValue(nodeIpAddress, ifRefRF, "xfRadioLinkPtpRadioMIB",
                            "xfRFTxAdminStatus", ValueTypeInt32.class, rfAdminValue);
                }
                if (wanIf) {
                    result = snmpAgentOperations.setInterfaceValue(nodeIpAddress, ifRef, "interfaces",
                            "ifAdminStatus", ValueTypeInt32.class, wanAdminValue);
                }
            }

            globalResult = globalResult & result;
        }

        return globalResult;
    }

    @Override
    public boolean setInterfaceAdminStatus(final IpAddress nodeIpAddress, final String ifRef, boolean txOn) {
         boolean result = false;
         if (IfRefStackHandler.isWanLanInterface(ifRef)) {
             String adminValue = IfRefAdminStatusHandler.getWanLanInterfaceAdminStatusValue(txOn);
             LOG.info("SouthboundCommunication.setInterfaceAdminStatus: WAN interface: nodeIpAddress {} ifRef {} txOn {} adminValue {}",
                     nodeIpAddress, ifRef, txOn, adminValue);
             result = snmpAgentOperations.setInterfaceValue(nodeIpAddress, ifRef, "interfaces",
                     "ifAdminStatus", ValueTypeInt32.class, adminValue);
         } else if (IfRefStackHandler.isRfInterface(ifRef)) {
             String adminValue = IfRefAdminStatusHandler.getRfInterfaceAdminStatusValue(txOn);
             LOG.info("SouthboundCommunication.setInterfaceAdminStatus: RF interface: nodeIpAddress {} ifRef {} txOn {} adminValue {}",
                     nodeIpAddress, ifRef, txOn, adminValue);
             result = snmpAgentOperations.setInterfaceValue(nodeIpAddress, ifRef, "xfRadioLinkPtpRadioMIB",
                     "xfRFTxAdminStatus", ValueTypeInt32.class, adminValue);
         }
         /*
          * no admin status associated to BONDING or RAU interfaces
          */
          return result;
     }

     public String get66yyInterfaceAdminStatus(final IpAddress nodeIpAddress, final String ifRef) {
         String result = null;
         if (IfRefStackHandler.isWanLanInterface(ifRef)) {
             result = snmpAgentOperations.getInterfaceValue(nodeIpAddress, ifRef, "interfaces",
                     "ifAdminStatus");
         } else if (IfRefStackHandler.isRfInterface(ifRef)) {
             result = snmpAgentOperations.getInterfaceValue(nodeIpAddress, ifRef, "xfRadioLinkPtpRadioMIB",
                     "xfRFTxAdminStatus");
         }
         /*
          * no admin status associated to BONDING or RAU interfaces
          */
          return result;
    }

    @Override
    public String getInterfaceAdminStatus(final IpAddress nodeIpAddress, final String ifRef,
            final java.lang.Class<? extends ProductNameBase> productNameClass) {
        if (MINILINK6691.class.isAssignableFrom(productNameClass)) {
            return get66yyInterfaceAdminStatus(nodeIpAddress, ifRef);
        } else if (MINILINK6351.class.isAssignableFrom(productNameClass) ||
                   MINILINK6352.class.isAssignableFrom(productNameClass)) {
            return null;
        }

        return null;
    }

    private String getXXyyInterfaceOperStatus(final IpAddress nodeIpAddress, final String ifRef) {
        String snmpValue = snmpAgentOperations.getInterfaceValue(nodeIpAddress, ifRef,
                "interfaces", "ifOperStatus");
        if (snmpValue == null) {
            return null;
        }

        return IfRefOperStatusHandler.convertSnmpValueToString(snmpValue);
    }

    @Override
    public String getInterfaceOperStatus(final IpAddress nodeIpAddress, final String ifRef,
            final java.lang.Class<? extends ProductNameBase> productNameClass) {
        return getXXyyInterfaceOperStatus(nodeIpAddress, ifRef);
    }

    @Override
    public Integer getBridgePortId(final String ifRef) {
         return snmpAgentOperations.getBridgePortId(ifRef);
    }

    private Integer get66yyCurrentChannelSpacing(final IpAddress nodeIpAddress, final String ifRef,
             final String mibObjectName, final String mibObjectType) {
         List<String> ifRefStack = snmpAgentOperations.getInterfaceStack(ifRef, false);
         LOG.info("SouthboundCommunication.get66yyCurrentChannelSpacing: nodeIpAddress {} ifRef {} {}",
                nodeIpAddress, ifRef, ifRefStack != null);
         if (ifRefStack == null) {
             LOG.error("SouthboundCommunication.get66yyCurrentChannelSpacing: null ifRef stack for ifRef {} ", ifRef);
             return null;
         }
         String ifRefRauIf = IfRefStackHandler.getRauInterface(ifRefStack);
         if (ifRefRauIf == null) {
             LOG.error("SouthboundCommunication.get66yyCurrentChannelSpacing: RAU interface not found for ifRef {}",
                     ifRef);
             return null;
         }

        String value = snmpAgentOperations.getInterfaceValue(nodeIpAddress, ifRef,
                 mibObjectName, mibObjectType);
        Integer channelSpacing = null;

        if (value != null) {
            try {
                channelSpacing = Integer.parseInt(value);
            } catch (final NumberFormatException e) {
                 LOG.warn("SouthboundCommunication.get66yyCurrentChannelSpacing: value {} ", value, e);
            }
        }

        return channelSpacing;
    }

    private Integer get63yyCurrentChannelSpacing(final IpAddress nodeIpAddress, final String ifRef,
             final String mibObjectName, final String mibObjectType) {
         final List<String> ifRefStack = snmpAgentOperations.getInterfaceStack(ifRef, false);
         LOG.info("SouthboundCommunication.get63yyCurrentChannelSpacing: nodeIpAddress {} ifRef {}",
                nodeIpAddress, ifRef);
         if (ifRefStack == null) {
             return null;
         }
         final String ifRefRf = IfRefStackHandler.getRfInterface(ifRefStack);
         if (ifRefRf == null) {
             return null;
         }

         LOG.info("SouthboundCommunication.get63yyCurrentChannelSpacing: ifRefRf {}", ifRefRf);

         String value = snmpAgentOperations.getInterfaceValue(nodeIpAddress, ifRefRf,
                  mibObjectName, mibObjectType);
         Integer channelSpacing = null;

         if (value != null) {
             try {
                 channelSpacing = Integer.parseInt(value);
             } catch (final NumberFormatException e) {
                 LOG.warn("SouthboundCommunication.get63yyCurrentChannelSpacing: value {}", value, e);
             }
         }

         return channelSpacing;
    }

     @Override
     public Integer getInterfaceCurrentChannelSpacing(final IpAddress nodeIpAddress, final String ifRef,
             final java.lang.Class<? extends ProductNameBase> productNameClass) {
       if (MINILINK6691.class.isAssignableFrom(productNameClass)) {
            return get66yyCurrentChannelSpacing(nodeIpAddress, ifRef,
                    "xfRadioLinkRltMIB", "xfChannelSpacing");
        } else if (MINILINK6351.class.isAssignableFrom(productNameClass) ||
                   MINILINK6352.class.isAssignableFrom(productNameClass)) {
            return get63yyCurrentChannelSpacing(nodeIpAddress, ifRef,
                    "ptRadioLinkMIB", "channelSpacing");
        }

        return 0;
     }

    private boolean set66yyInterfaceSelectedMaxAcm(final IpAddress nodeIpAddress, final String ifRef,
           Integer selectedMaxAcm, final String mibObjectName, final String mibObjectType) {
         List<String> ifRefStack = snmpAgentOperations.getInterfaceStack(ifRef, false);
         LOG.info("SouthboundCommunication.set66yyInterfaceSelectedMaxAcm: nodeIpAddress {} ifRef {} selectedMaxAcm {}",
                nodeIpAddress, ifRef, selectedMaxAcm);
         if (ifRefStack == null) {
             return false;
         }
         String ifRefRauIf = IfRefStackHandler.getRauInterface(ifRefStack);
         if (ifRefRauIf == null) {
             return false;
         }

         return snmpAgentOperations.setInterfaceValue(nodeIpAddress, ifRefRauIf,
                  mibObjectName, mibObjectType, ValueTypeInt32.class, selectedMaxAcm.toString());
    }

    private boolean set63yyInterfaceSelectedMaxAcm(final IpAddress nodeIpAddress, final String ifRef,
           final Integer selectedMaxAcm, final String selectedMaxAcmString) {
         List<String> ifRefStack = snmpAgentOperations.getInterfaceStack(ifRef, false);
         LOG.info("SouthboundCommunication.set63yyInterfaceSelectedMaxAcm: nodeIpAddress {} ifRef {} selectedMaxAcm {} selectedMaxAcmString {}",
                nodeIpAddress, ifRef, selectedMaxAcm, selectedMaxAcmString);
         if (ifRefStack == null) {
             return false;
         }
         String ifRefRfIf = IfRefStackHandler.getRfInterface(ifRefStack);
         if (ifRefRfIf == null) {
             return false;
         }

         final String slot = IfRefStackHandler.getRfIfRefSlot(ifRefRfIf);
         final String ct = IfRefStackHandler.getCarrierTerminalId(ifRefRfIf);
         final Integer slotId = Integer.parseInt(slot);
         final Integer ctId = Integer.parseInt(ct);

         return cliPluginAgentOperations.setIfSelectedMaxAcm(nodeIpAddress, ifRefRfIf,
                 slotId, ctId, selectedMaxAcmString);
    }

    @Override
    public boolean setInterfaceSelectedMaxAcm(final IpAddress nodeIpAddress, final String ifRef,
            final Integer selectedMaxAcm, final String selectedMaxAcmString,
            final java.lang.Class<? extends ProductNameBase> productNameClass) {
       if (MINILINK6691.class.isAssignableFrom(productNameClass)) {
            return set66yyInterfaceSelectedMaxAcm(nodeIpAddress, ifRef, selectedMaxAcm,
                    "xfRadioLinkRltMIB", "xfCarrierTermSelectedMaxACM");
        } else if (MINILINK6351.class.isAssignableFrom(productNameClass) ||
                   MINILINK6352.class.isAssignableFrom(productNameClass)) {
            /*
             * calling CLI plugin Agent for ML63xx equipment
             */
            set63yyInterfaceSelectedMaxAcm(nodeIpAddress, ifRef, selectedMaxAcm, selectedMaxAcmString);
        }

        return false;
    }

    private String get66yyXfRadioLinkRltMibValue(final IpAddress nodeIpAddress, final String ifRef,
            final String mibObjectName, final String mibObjectType) {
        List<String> ifRefStack = snmpAgentOperations.getInterfaceStack(ifRef, false);
        LOG.info("SouthboundCommunication.get66yyXfRadioLinkRltMibValue: nodeIpAddress {} ifRef {}",
               nodeIpAddress, ifRef);
        if (ifRefStack == null) {
            return null;
        }
        String ifRefRauIf = IfRefStackHandler.getRauInterface(ifRefStack);
        if (ifRefRauIf == null) {
            return null;
        }

        return snmpAgentOperations.getInterfaceValue(nodeIpAddress, ifRefRauIf,
                 mibObjectName, mibObjectType);
    }

    private String get66yyXfRadioLinkPtpRadioMibValue(final IpAddress nodeIpAddress, final String ifRef,
            final String mibObjectName, final String mibObjectType) {
        List<String> ifRefStack = snmpAgentOperations.getInterfaceStack(ifRef, false);
        LOG.info("SouthboundCommunication.get66yyXfRadioLinkPtpRadioMibValue: nodeIpAddress {} ifRef {}",
               nodeIpAddress, ifRef);
        if (ifRefStack == null) {
            return null;
        }
        String ifRefRfIf = IfRefStackHandler.getRfInterface(ifRefStack);
        if (ifRefRfIf == null) {
            return null;
        }

        return snmpAgentOperations.getInterfaceValue(nodeIpAddress, ifRefRfIf,
                 mibObjectName, mibObjectType);
    }

    private String get63yyPtRadioLinkMibValue(final IpAddress nodeIpAddress, final String ifRef,
            final String mibObjectName, final String mibObjectType) {
        List<String> ifRefStack = snmpAgentOperations.getInterfaceStack(ifRef, false);
        LOG.info("SouthboundCommunication.get63yyPtRadioLinkMibValue: nodeIpAddress {} ifRef {}",
               nodeIpAddress, ifRef);
        if (ifRefStack == null) {
            return null;
        }
        String ifRefRf = IfRefStackHandler.getRfInterface(ifRefStack);
        if (ifRefRf == null) {
            return null;
        }

        return snmpAgentOperations.getInterfaceValue(nodeIpAddress, ifRefRf,
                 mibObjectName, mibObjectType);
    }

    private Integer get66yyInterfaceSelectedMaxAcm(final IpAddress nodeIpAddress, final String ifRef,
            final String mibObjectName, final String mibObjectType) {
        final String value = get66yyXfRadioLinkRltMibValue(nodeIpAddress, ifRef,
                mibObjectName, mibObjectType);

        Integer valueAsInt = null;
        if (value != null) {
             try {
                 valueAsInt = Integer.parseInt(value);
             } catch (final NumberFormatException e) {
                 LOG.warn("SouthboundCommunication.get66yyInterfaceSelectedMaxAcm: value {}", value, e);
             }
         }

         return valueAsInt;
    }

    private Integer get63yyInterfaceSelectedMaxAcm(final IpAddress nodeIpAddress, final String ifRef,
            final String mibObjectName, final String mibObjectType) {
        final String value = get63yyPtRadioLinkMibValue(nodeIpAddress, ifRef,
                mibObjectName, mibObjectType);

        Integer valueAsInt = null;
        if (value != null) {
             try {
                 valueAsInt = Integer.parseInt(value);
             } catch (final NumberFormatException e) {
                 LOG.warn("SouthboundCommunication.get63yyInterfaceSelectedMaxAcm: value {}", value, e);
             }
         }

         return valueAsInt;
    }

    @Override
    public Integer getInterfaceSelectedMaxAcm(final IpAddress nodeIpAddress, final String ifRef,
            final java.lang.Class<? extends ProductNameBase> productNameClass) {
        if (MINILINK6691.class.isAssignableFrom(productNameClass)) {
            return get66yyInterfaceSelectedMaxAcm(nodeIpAddress, ifRef,
                    "xfRadioLinkRltMIB", "xfCarrierTermSelectedMaxACM");
        } else if (MINILINK6351.class.isAssignableFrom(productNameClass) ||
                   MINILINK6352.class.isAssignableFrom(productNameClass)) {
            return get63yyInterfaceSelectedMaxAcm(nodeIpAddress, ifRef,
                    "ptRadioLinkMIB", "selectedMaxAcm");
        }

        return 0;
    }

    private String getXXyyInterfaceCurrenTargetInputPowerFarEnd(final IpAddress nodeIpAddress,
           final String ifRef, final String mibObjectName, final String mibObjectType) {
        List<String> ifRefStack = snmpAgentOperations.getInterfaceStack(ifRef, false);
        LOG.info("SouthboundCommunication.getXXyyInterfaceCurrenTargetInputPowerFarEnd: nodeIpAddress {} ifRef {} mibObjectName {} mibObjectType {}",
               nodeIpAddress, ifRef, mibObjectName, mibObjectType);
        if (ifRefStack == null) {
            return null;
        }
        String ifRefRf = IfRefStackHandler.getRfInterface(ifRefStack);
        if (ifRefRf == null) {
            return null;
        }

        return snmpAgentOperations.getInterfaceValue(nodeIpAddress, ifRefRf,
                 mibObjectName, mibObjectType);
    }

    private Integer get66yyInterfaceCurrenTargetInputPowerFarEnd(final IpAddress nodeIpAddress,
           final String ifRef, final String mibObjectName, final String mibObjectType) {
        final String value = get66yyXfRadioLinkPtpRadioMibValue(nodeIpAddress, ifRef,
                mibObjectName, mibObjectType);
        if (value == null) {
            return null;
        }

        Integer valueAsInt = null;
        if (value != null) {
             try {
                 valueAsInt = Integer.parseInt(value);
             } catch (final NumberFormatException e) {
                 LOG.warn("SouthboundCommunication.get66yyInterfaceCurrenTargetInputPowerFarEnd: value {} ",
                         value, e);
             }
         }

         return valueAsInt;
    }

    private Integer get63yyInterfaceCurrenTargetInputPowerFarEnd(final IpAddress nodeIpAddress,
           final String ifRef, final String mibObjectName, final String mibObjectType) {
        final String value = get63yyPtRadioLinkMibValue(nodeIpAddress, ifRef,
                mibObjectName, mibObjectType);
        if (value == null) {
            return null;
        }

        Integer valueAsInt = null;
        if (value != null) {
             try {
                 valueAsInt = Integer.parseInt(value);
             } catch (final NumberFormatException e) {
                 LOG.warn("SouthboundCommunication.get63yyInterfaceCurrenTargetInputPowerFarEnd: value {} ",
                         value, e);
             }
         }

         return valueAsInt;
    }

    @Override
    public Integer getInterfaceCurrenTargetInputPowerFarEnd(final IpAddress nodeIpAddress,
            final String ifRef, final java.lang.Class<? extends ProductNameBase> productNameClass) {
       if (MINILINK6691.class.isAssignableFrom(productNameClass)) {
            return get66yyInterfaceCurrenTargetInputPowerFarEnd(nodeIpAddress, ifRef,
                    "xfRadioLinkPtpRadioMIB", "xfRfAtpcTargetInputPowerFE");
        } else if (MINILINK6351.class.isAssignableFrom(productNameClass) ||
                   MINILINK6352.class.isAssignableFrom(productNameClass)) {
            return get63yyInterfaceCurrenTargetInputPowerFarEnd(nodeIpAddress, ifRef,
                    "ptRadioLinkMIB", "targetInputPowerFarEnd");
        }

        return 0;
    }

    private boolean set66yyXfRadioLinkPtpRadioMibValue(final IpAddress nodeIpAddress, final String ifRef,
                final Integer targetInputPowerFarEnd, final String mibObjectName, final String mibObjectType) {
        List<String> ifRefStack = snmpAgentOperations.getInterfaceStack(ifRef, false);
        LOG.info("SouthboundCommunication.set66yyXfRadioLinkPtpRadioMibValue: nodeIpAddress {} ifRef {} targetInputPowerFarEnd {}",
               nodeIpAddress, ifRef, targetInputPowerFarEnd);
        if (ifRefStack == null) {
            return false;
        }
        String ifRefRf = IfRefStackHandler.getRfInterface(ifRefStack);
        if (ifRefRf == null) {
            return false;
        }

        return snmpAgentOperations.setInterfaceValue(nodeIpAddress, ifRef, mibObjectName,
                     mibObjectType, ValueTypeInt32.class, targetInputPowerFarEnd.toString());
    }

    private boolean set63yyInterfaceCurrenTargetInputPowerFarEnd(final IpAddress nodeIpAddress,
            final String ifRef, final Integer targetInputPowerFarEnd) {
        List<String> ifRefStack = snmpAgentOperations.getInterfaceStack(ifRef, false);
        LOG.info("SouthboundCommunication.set63yyInterfaceCurrenTargetInputPowerFarEnd: nodeIpAddress {} ifRef {} targetInputPowerFarEnd {}",
               nodeIpAddress, ifRef, targetInputPowerFarEnd);
        if (ifRefStack == null) {
            return false;
        }
        String ifRefRf = IfRefStackHandler.getRfInterface(ifRefStack);
        if (ifRefRf == null) {
            return false;
        }

        final String slot = IfRefStackHandler.getRfIfRefSlot(ifRefRf);
        final String ct = IfRefStackHandler.getCarrierTerminalId(ifRefRf);
        final Integer slotId = Integer.parseInt(slot);
        final Integer ctId = Integer.parseInt(ct);
        final String targetInputPowerFarEndString = Integer.toString(targetInputPowerFarEnd);;

        return cliPluginAgentOperations.setIfTargetInputPowerFarEnd(nodeIpAddress, ifRefRf,
                slotId, ctId, targetInputPowerFarEndString);
    }

    @Override
    public boolean setInterfaceCurrenTargetInputPowerFarEnd(final IpAddress nodeIpAddress, final String ifRef,
            final Integer targetInputPowerFarEnd, final java.lang.Class<? extends ProductNameBase> productNameClass) {
       if (MINILINK6691.class.isAssignableFrom(productNameClass)) {
            return set66yyXfRadioLinkPtpRadioMibValue(nodeIpAddress, ifRef, targetInputPowerFarEnd,
                    "xfRadioLinkPtpRadioMIB", "xfRfAtpcTargetInputPowerFE");
        } else if (MINILINK6351.class.isAssignableFrom(productNameClass) ||
                   MINILINK6352.class.isAssignableFrom(productNameClass)) {
            return set63yyInterfaceCurrenTargetInputPowerFarEnd(nodeIpAddress, ifRef, targetInputPowerFarEnd);
        }

        return false;
    }
}
