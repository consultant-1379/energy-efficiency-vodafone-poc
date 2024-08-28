/*
 * Copyright (c) 2017 Ericsson, AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.sb.communication;

import java.util.List;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.topology.inventory.rev170714.ProductNameBase;

/**
 * @author Ericsson
 */
public interface SouthboundCommunicationService {

    void setNodeSnmpCommunity(final IpAddress nodeIpAddress, final String snmpReadCommunity,
            final String snmpWriteCommunity);

    void setNodeCliLoginCredentials(final IpAddress nodeIpAddress, final String login,
            final String password);

    void loadInterfaceDataFromNodes(final String nodeId, final IpAddress nodeIpAddress,
            final java.lang.Class<? extends ProductNameBase> productNameClass);

    Long getInterfaceMaximumBandwidthCapacity(final IpAddress nodeIpAddress, final String ifRef,
            final java.lang.Class<? extends ProductNameBase> productNameClass);

    Long getInterfaceCurrentBandwidthCapacity(final IpAddress nodeIpAddress, final String ifRef,
            final java.lang.Class<? extends ProductNameBase> productNameClass);

    Integer getInterfaceOutputPower(final IpAddress nodeIpAddress, final String ifRef,
            final java.lang.Class<? extends ProductNameBase> productNameClass);

    Integer getInterfaceMaxOutputPowerSinceReset(final IpAddress nodeIpAddress, final String ifRef,
            final java.lang.Class<? extends ProductNameBase> productNameClass);

    Long getInterfaceInOctects(final IpAddress nodeIpAddress, final String ifRef,
            final java.lang.Class<? extends ProductNameBase> productNameClass);

    Long getInterfaceOutOctects(final IpAddress nodeIpAddress, final String ifRef,
            final java.lang.Class<? extends ProductNameBase> productNameClass);

    boolean setInterfacesToLag(final IpAddress nodeIpAddress, final List<String> ifRefList,
            final String masterIfRef);

    boolean setStackInterfacesAdminStatus(final IpAddress nodeIpAddress, final List<String> ifRefList,
            final boolean txOn, final boolean wanIf, final boolean radioIf);

    String getInterfaceAdminStatus(final IpAddress nodeIpAddress, final String ifRef,
            final java.lang.Class<? extends ProductNameBase> productNameClass);

    boolean setInterfaceAdminStatus(final IpAddress nodeIpAddress, final String ifRef, final boolean txOn);

    String getInterfaceOperStatus(final IpAddress nodeIpAddress, final String ifRef,
            final java.lang.Class<? extends ProductNameBase> productNameClass);

    public Integer getBridgePortId(final String ifRef);

    boolean setInterfaceSelectedMaxAcm(final IpAddress nodeIpAddress, final String ifRef, final Integer selectedMaxAcm,
            final String selectedMaxAcmString, final java.lang.Class<? extends ProductNameBase> productNameClass);

    Integer getInterfaceCurrentChannelSpacing(final IpAddress nodeIpAddress, final String ifRef,
            final java.lang.Class<? extends ProductNameBase> productNameClass);

    Integer getInterfaceSelectedMaxAcm(final IpAddress nodeIpAddress, final String ifRef,
            final java.lang.Class<? extends ProductNameBase> productNameClass);

    Integer getInterfaceCurrenTargetInputPowerFarEnd(final IpAddress nodeIpAddress, final String ifRef,
            final java.lang.Class<? extends ProductNameBase> productNameClass);

    boolean setInterfaceCurrenTargetInputPowerFarEnd(final IpAddress nodeIpAddress, final String ifRef,
            final Integer targetInputPowerFarEnd, final java.lang.Class<? extends ProductNameBase> productNameClass);

}
