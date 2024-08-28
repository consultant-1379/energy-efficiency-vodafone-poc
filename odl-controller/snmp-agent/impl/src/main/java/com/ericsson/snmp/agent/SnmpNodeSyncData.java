/*
 * Copyright (c) 2017 Ericsson AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.snmp.agent;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.topology.inventory.rev170714.ProductNameBase;

public final class SnmpNodeSyncData {

    private String nodeId;
    private IpAddress nodeIpAddress;
    private java.lang.Class<? extends ProductNameBase> productNameClass;

    public SnmpNodeSyncData(final String nodeId, final IpAddress nodeIpAddress,
            java.lang.Class<? extends ProductNameBase> productNameClass) {
        this.nodeId = nodeId;
        this.nodeIpAddress = nodeIpAddress;
        this.productNameClass = productNameClass;
    }

    public String getNodeId() {
        return nodeId;
    }

    public IpAddress getNodeIpAddress() {
        return nodeIpAddress;
    }

    public java.lang.Class<? extends ProductNameBase> getProductName() {
        return productNameClass;
    }

}
