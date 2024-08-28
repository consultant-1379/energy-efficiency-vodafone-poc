/*
 * Copyright (c) 2017 Ericsson, AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.snmp.agent;

import java.util.List;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.topology.inventory.rev170714.ProductNameBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.BaseValueType;

/**
 * @author ericsson
 */
public interface SnmpAgentOperations {

    void setNodeSnmpCommunity(final IpAddress nodeIpAddress,
            final String readCommunity, final String writeCommunity);

    void loadInterfaceData(final String nodeId, final IpAddress nodeIpAddress,
            java.lang.Class<? extends ProductNameBase> productNameClass);

    List<String> getInterfaceStack(final String ifRef, final boolean ifRefStack2);

    String getInterfaceValue(final IpAddress nodeIpAddress, final String ifName,
            final String mibName, final String objectType);

    boolean setInterfaceValue(final IpAddress nodeIpAddress, final String ifName,
            final String mibName, final String objectType,
            final java.lang.Class<? extends BaseValueType> valueType,
            final String value);

    boolean setLinkLagMembership(final IpAddress nodeIpAddress,
            final List<String> ifRefList, final String ifRefMaster);

    Integer getBridgePortId(final String ifRef);
}
