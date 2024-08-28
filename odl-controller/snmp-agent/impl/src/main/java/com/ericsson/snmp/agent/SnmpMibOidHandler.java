/*
 * Copyright (c) 2017 Ericsson, AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.snmp.agent;

import java.util.ArrayList;
import java.util.List;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.snmp.agent.rev170714.MibObjectOid;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.snmp.agent.rev170714.MibObjectOidBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.snmp.agent.rev170714.mib.object.oid.ObjectList;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.snmp.agent.rev170714.mib.object.oid.ObjectListBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.snmp.agent.rev170714.mib.object.oid.ObjectListKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ericsson
 */
public class SnmpMibOidHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SnmpMibOidHandler.class);

    public void init(final DataBroker dataBroker) {
       List<ObjectList> objectListToAdd = new ArrayList<>();

       ObjectListKey objectListKey = new ObjectListKey("interfaces", "ifInOctets");
       ObjectListBuilder objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("interfaces");
       objectListBuilder.setObjectType("ifInOctets");
       objectListBuilder.setOid("1.3.6.1.2.1.2.2.1.10");
       objectListBuilder.setLogicalIndexRequired(false);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("interfaces", "ifOutOctets");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("interfaces");
       objectListBuilder.setObjectType("ifOutOctets");
       objectListBuilder.setOid("1.3.6.1.2.1.2.2.1.16");
       objectListBuilder.setLogicalIndexRequired(false);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("interfaces", "ifSpeed");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("interfaces");
       objectListBuilder.setObjectType("ifSpeed");
       objectListBuilder.setOid("1.3.6.1.2.1.2.2.1.5");
       objectListBuilder.setLogicalIndexRequired(false);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("interfaces", "ifAdminStatus");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("interfaces");
       objectListBuilder.setObjectType("ifAdminStatus");
       objectListBuilder.setOid("1.3.6.1.2.1.2.2.1.7");
       objectListBuilder.setLogicalIndexRequired(false);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("ifMIB", "ifHCInOctets");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("ifMIB");
       objectListBuilder.setObjectType("ifHCInOctets");
       objectListBuilder.setOid("1.3.6.1.2.1.31.1.1.1.6");
       objectListBuilder.setLogicalIndexRequired(false);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("ifMIB", "ifHCOutOctets");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("ifMIB");
       objectListBuilder.setObjectType("ifHCOutOctets");
       objectListBuilder.setOid("1.3.6.1.2.1.31.1.1.1.10");
       objectListBuilder.setLogicalIndexRequired(false);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("ifMIB", "ifHighSpeed");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("ifMIB");
       objectListBuilder.setObjectType("ifHighSpeed");
       objectListBuilder.setOid("1.3.6.1.2.1.31.1.1.1.15");
       objectListBuilder.setLogicalIndexRequired(false);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("interfaces", "ifOperStatus");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("interfaces");
       objectListBuilder.setObjectType("ifOperStatus");
       objectListBuilder.setOid("1.3.6.1.2.1.2.2.1.8");
       objectListBuilder.setLogicalIndexRequired(false);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("xfRadioLinkPtpRadioMIB", "xfRfSelectedMinOutputPower");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("xfRadioLinkPtpRadioMIB");
       objectListBuilder.setObjectType("xfRfSelectedMinOutputPower");
       objectListBuilder.setOid("1.3.6.1.4.1.193.81.3.4.3.1.8.1.1");
       objectListBuilder.setLogicalIndexRequired(false);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("xfRadioLinkPtpRadioMIB", "xfRfSelectedMaxOutputPower");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("xfRadioLinkPtpRadioMIB");
       objectListBuilder.setObjectType("xfRfSelectedMaxOutputPower");
       objectListBuilder.setOid("1.3.6.1.4.1.193.81.3.4.3.1.8.1.2");
       objectListBuilder.setLogicalIndexRequired(false);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("xfRadioLinkPtpRadioMIB", "xfRfCurrentOutputPower");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("xfRadioLinkPtpRadioMIB");
       objectListBuilder.setObjectType("xfRfCurrentOutputPower");
       objectListBuilder.setOid("1.3.6.1.4.1.193.81.3.4.3.1.8.1.3");
       objectListBuilder.setLogicalIndexRequired(false);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("xfRadioLinkPtpRadioMIB", "xfRfMaxOutputPowerSinceReset");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("xfRadioLinkPtpRadioMIB");
       objectListBuilder.setObjectType("xfRfMaxOutputPowerSinceReset");
       objectListBuilder.setOid("1.3.6.1.4.1.193.81.3.4.3.1.8.1.29");
       objectListBuilder.setLogicalIndexRequired(false);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("xfRadioLinkPtpRadioMIB", "xfRfMaxOutputPowerLast7Days");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("xfRadioLinkPtpRadioMIB");
       objectListBuilder.setObjectType("xfRfMaxOutputPowerLast7Days");
       objectListBuilder.setOid("1.3.6.1.4.1.193.81.3.4.3.1.8.1.27");
       objectListBuilder.setLogicalIndexRequired(false);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("xfRadioLinkPtpRadioMIB", "xfRfAtpcTargetInputPowerFE");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("xfRadioLinkPtpRadioMIB");
       objectListBuilder.setObjectType("xfRfAtpcTargetInputPowerFE");
       objectListBuilder.setOid("1.3.6.1.4.1.193.81.3.4.3.1.8.1.6");
       objectListBuilder.setLogicalIndexRequired(false);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("xfRadioLinkPtpRadioMIB", "xfRFTxAdminStatus");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("xfRadioLinkPtpRadioMIB");
       objectListBuilder.setObjectType("xfRFTxAdminStatus");
       objectListBuilder.setOid("1.3.6.1.4.1.193.81.3.4.3.1.2.1.8");
       objectListBuilder.setLogicalIndexRequired(false);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("xfRadioLinkPtpRadioMIB", "xfRFTxOperStatus");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("xfRadioLinkPtpRadioMIB");
       objectListBuilder.setObjectType("xfRFTxOperStatus");
       objectListBuilder.setOid("1.3.6.1.4.1.193.81.3.4.3.1.2.1.7");
       objectListBuilder.setLogicalIndexRequired(false);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("xfRadioLinkPtpRadioMIB", "xfCarrierTermSelectedMinACM");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("xfRadioLinkPtpRadioMIB");
       objectListBuilder.setObjectType("xfCarrierTermSelectedMinACM");
       objectListBuilder.setOid("1.3.6.1.4.1.193.81.3.4.5.1.3.1.6");
       objectListBuilder.setLogicalIndexRequired(true);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("xfRadioLinkRltMIB", "xfCarrierTermActualACM");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("xfRadioLinkRltMIB");
       objectListBuilder.setObjectType("xfCarrierTermActualACM");
       objectListBuilder.setOid("1.3.6.1.4.1.193.81.3.4.3.1.2.1.7");
       objectListBuilder.setLogicalIndexRequired(true);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("xfRadioLinkRltMIB", "xfCarrierTermSelectedMaxACM");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("xfRadioLinkRltMIB");
       objectListBuilder.setObjectType("xfCarrierTermSelectedMaxACM");
       objectListBuilder.setOid("1.3.6.1.4.1.193.81.3.4.5.1.3.1.8");
       objectListBuilder.setLogicalIndexRequired(true);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("xfRadioLinkRltMIB", "xfCarrierTermRadioFrameId");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("xfRadioLinkRltMIB");
       objectListBuilder.setObjectType("xfCarrierTermRadioFrameId");
       objectListBuilder.setOid("1.3.6.1.4.1.193.81.3.4.5.1.3.1.4");
       objectListBuilder.setLogicalIndexRequired(true);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("xfRadioLinkRltMIB", "xfMaxACMCapacity");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("xfRadioLinkRltMIB");
       objectListBuilder.setObjectType("xfMaxACMCapacity");
       objectListBuilder.setOid("1.3.6.1.4.1.193.81.3.4.5.1.4.1.5");
       objectListBuilder.setLogicalIndexRequired(true);
       objectListBuilder.setRadioFrameIdRequired(true);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("xfRadioLinkRltMIB", "xfRLWANActualCapacity");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("xfRadioLinkRltMIB");
       objectListBuilder.setObjectType("xfRLWANActualCapacity");
       objectListBuilder.setOid("1.3.6.1.4.1.193.81.3.4.5.1.12.1.8");
       objectListBuilder.setLogicalIndexRequired(false);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("xfRadioLinkRltMIB", "xfCarrierTermActualCapacity");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("xfRadioLinkRltMIB");
       objectListBuilder.setObjectType("xfCarrierTermActualCapacity");
       objectListBuilder.setOid("1.3.6.1.4.1.193.81.3.4.5.1.3.1.13");
       objectListBuilder.setLogicalIndexRequired(true);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("xfRadioLinkRltMIB", "xfChannelSpacing");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("xfRadioLinkRltMIB");
       objectListBuilder.setObjectType("xfChannelSpacing");
       objectListBuilder.setOid("1.3.6.1.4.1.193.81.3.4.5.1.4.1.2");
       objectListBuilder.setLogicalIndexRequired(true);
       objectListBuilder.setRadioFrameIdRequired(true);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("xfRadioLinkRltMIB", "xfRLTActualTXTotalCapacity");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("xfRadioLinkRltMIB");
       objectListBuilder.setObjectType("xfRLTActualTXTotalCapacity");
       objectListBuilder.setOid("1.3.6.1.4.1.193.81.3.4.5.1.6.1.12");
       objectListBuilder.setLogicalIndexRequired(true);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("xfRadioLinkRltMIB", "xfRLTActualTXPacketCapacity");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("xfRadioLinkRltMIB");
       objectListBuilder.setObjectType("xfRLTActualTXPacketCapacity");
       objectListBuilder.setOid("1.3.6.1.4.1.193.81.3.4.5.1.6.1.13");
       objectListBuilder.setLogicalIndexRequired(false);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("xfEthernetBridgeMIB", "xfEthernetIfMinSpeed");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("xfEthernetBridgeMIB");
       objectListBuilder.setObjectType("xfEthernetIfMinSpeed");
       objectListBuilder.setOid("1.3.6.1.4.1.193.81.4.1.3.1.1.1.1.5");
       objectListBuilder.setLogicalIndexRequired(false);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("xfEthernetBridgeMIB", "xfEthernetIfMaxSpeed");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("xfEthernetBridgeMIB");
       objectListBuilder.setObjectType("xfEthernetIfMaxSpeed");
       objectListBuilder.setOid("1.3.6.1.4.1.193.81.4.1.3.1.1.1.1.6");
       objectListBuilder.setLogicalIndexRequired(false);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("xfEthernetBridgeMIB", "xfEthernetIfUsage");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("xfEthernetBridgeMIB");
       objectListBuilder.setObjectType("xfEthernetIfUsage");
       objectListBuilder.setOid("1.3.6.1.4.1.193.81.4.1.3.1.1.1.1.2");
       objectListBuilder.setLogicalIndexRequired(false);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("xfEthernetBridgeMIB", "xfLagMembers");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("xfEthernetBridgeMIB");
       objectListBuilder.setObjectType("xfLagMembers");
       objectListBuilder.setOid("1.3.6.1.4.1.193.81.4.1.2.1.7.1.1.2");
       objectListBuilder.setLogicalIndexRequired(false);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("ptRadioLinkMIB", "actualTxTotalCapacity");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("ptRadioLinkMIB");
       objectListBuilder.setObjectType("actualTxTotalCapacity");
       objectListBuilder.setOid("1.3.6.1.4.1.193.223.2.7.3.1.9");
       objectListBuilder.setLogicalIndexRequired(false);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("ptRadioLinkMIB", "ActualOutputPower");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("ptRadioLinkMIB");
       objectListBuilder.setObjectType("ActualOutputPower");
       objectListBuilder.setOid("1.3.6.1.4.1.193.223.2.7.1.1.2");
       objectListBuilder.setLogicalIndexRequired(false);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("ptRadioLinkMIB", "txAdminStatus");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("ptRadioLinkMIB");
       objectListBuilder.setObjectType("txAdminStatus");
       objectListBuilder.setOid("1.3.6.1.4.1.193.223.2.7.1.1.26");
       objectListBuilder.setLogicalIndexRequired(false);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("ptRadioLinkMIB", "txOperStatus");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("ptRadioLinkMIB");
       objectListBuilder.setObjectType("txOperStatus");
       objectListBuilder.setOid("1.3.6.1.4.1.193.223.2.7.1.1.25");
       objectListBuilder.setLogicalIndexRequired(false);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("ptRadioLinkMIB", "selectedMaxOutputPower");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("ptRadioLinkMIB");
       objectListBuilder.setObjectType("selectedMinOutputPower");
       objectListBuilder.setOid("1.3.6.1.4.1.193.223.2.7.1.1.29");
       objectListBuilder.setLogicalIndexRequired(false);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("ptRadioLinkMIB", "selectedMaxOutputPower");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("ptRadioLinkMIB");
       objectListBuilder.setObjectType("selectedMaxOutputPower");
       objectListBuilder.setOid("1.3.6.1.4.1.193.223.2.7.1.1.30");
       objectListBuilder.setLogicalIndexRequired(false);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("ptRadioLinkMIB", "channelSpacing");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("ptRadioLinkMIB");
       objectListBuilder.setObjectType("channelSpacing");
       objectListBuilder.setOid("1.3.6.1.4.1.193.223.2.7.1.1.23");
       objectListBuilder.setLogicalIndexRequired(false);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("ptRadioLinkMIB", "selectedMinAcm");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("ptRadioLinkMIB");
       objectListBuilder.setObjectType("selectedMinAcm");
       objectListBuilder.setOid("1.3.6.1.4.1.193.223.2.7.1.1.55");
       objectListBuilder.setLogicalIndexRequired(false);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("ptRadioLinkMIB", "selectedMaxAcm");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("ptRadioLinkMIB");
       objectListBuilder.setObjectType("selectedMaxAcm");
       objectListBuilder.setOid("1.3.6.1.4.1.193.223.2.7.1.1.56");
       objectListBuilder.setLogicalIndexRequired(false);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       objectListKey = new ObjectListKey("ptRadioLinkMIB", "targetInputPowerFarEnd");
       objectListBuilder = new ObjectListBuilder();
       objectListBuilder.setKey(objectListKey);
       objectListBuilder.setMibName("ptRadioLinkMIB");
       objectListBuilder.setObjectType("targetInputPowerFarEnd");
       objectListBuilder.setOid("1.3.6.1.4.1.193.223.2.7.1.1.34");
       objectListBuilder.setLogicalIndexRequired(false);
       objectListBuilder.setRadioFrameIdRequired(false);
       objectListToAdd.add(objectListBuilder.build());

       MibObjectOidBuilder mibObjectOidBuilder = new MibObjectOidBuilder();
       mibObjectOidBuilder.setObjectList(objectListToAdd);

       final WriteTransaction transaction = dataBroker.newWriteOnlyTransaction();
       InstanceIdentifier<MibObjectOid> iid = InstanceIdentifier.create(MibObjectOid.class);
       transaction.put(LogicalDatastoreType.OPERATIONAL, iid, mibObjectOidBuilder.build(), true);
       try {
            transaction.submit().get();
       } catch (final Exception e) {
            LOG.error("SnmpMibOidHandler.init ", e);
       }
   }
}
