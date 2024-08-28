/*
 * Copyright (c) 2017 Ericsson, AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.basic.network.functions.inventory;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.rev161116.networks.network.Node;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.topology.inventory.rev170714.InventoryNode;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.topology.inventory.rev170714.ProductFamilyBase;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.topology.inventory.rev170714.ProductNameBase;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.topology.inventory.rev170714.MINILINK6691;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.topology.inventory.rev170714.MINILINK6351;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.topology.inventory.rev170714.MINILINK6352;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ericsson
 */
public class InventoryHandler {

    private static final Logger LOG = LoggerFactory.getLogger(InventoryHandler.class);

    public static java.lang.Class<? extends ProductNameBase>
            getNodeProductName(final String nodeId) {
        /*
         * when inventory info shall be provided within topology node,
         * some info grabbed from there shall be used in place of node-id
         */
        if (nodeId.contains("6691")) {
            return MINILINK6691.class;
        } else if (nodeId.contains("6351")) {
            return MINILINK6351.class;
        } else if (nodeId.contains("6352")) {
            return MINILINK6352.class;
        }

        return MINILINK6691.class;
    }

    public static java.lang.Class<? extends ProductNameBase>
            getIfRefNodeProductName(final String ifRef) {
        String[] splitName = ifRef.split(":");
        if (splitName == null || splitName.length == 0) {
            return null;
        }
        return getNodeProductName(splitName[0]);
    }

    public static String getNodeInventoryName(final Node node) {
        /*
         * information not yet provided within topology
         */
        InventoryNode inventoryNode = node.getAugmentation(InventoryNode.class);
        if (inventoryNode == null) {
            LOG.error("InventoryHandler.getNodeProductFamilyName: null inventory for node {}",
                     node.getNodeId().getValue());
            return null;
        }

        return inventoryNode.getInventoryName();
    }

    public static java.lang.Class<? extends ProductFamilyBase> getNodeProductFamily(final Node node) {
        /*
         * information not yet provided within topology
         */
        InventoryNode inventoryNode = node.getAugmentation(InventoryNode.class);
        if (inventoryNode == null) {
            LOG.error("InventoryHandler.getNodeProductFamilyName: null inventory for node {}",
                     node.getNodeId().getValue());
            return null;
        }

        return inventoryNode.getProductFamily();
    }

    public static String getNodeManufacturer(final Node node) {
         InventoryNode inventoryNode = node.getAugmentation(InventoryNode.class);
         if (inventoryNode == null) {
             LOG.error("InventoryHandler.getNodeProductFamilyName: null inventory for node {}",
                     node.getNodeId().getValue());
            return null;
        }

        return inventoryNode.getManufacturer();
    }

    public static java.lang.Class<? extends ProductNameBase> getNodeProductName(final Node node) {
         InventoryNode inventoryNode = node.getAugmentation(InventoryNode.class);
         if (inventoryNode == null) {
             LOG.error("InventoryHandler.getNodeProductName: null inventory for node {}",
                     node.getNodeId().getValue());
            return null;
        }

        return inventoryNode.getProductName();
    }

    public static String getNodeSnmpReadCommunity(final Node node) {
         InventoryNode inventoryNode = node.getAugmentation(InventoryNode.class);
         if (inventoryNode == null) {
             LOG.error("InventoryHandler.getSnmpReadCommunity: null inventory for node {}",
                     node.getNodeId().getValue());
            return null;
        }

        return inventoryNode.getSnmpReadCommunity();
    }

    public static String getNodeSnmpWriteCommunity(final Node node) {
         InventoryNode inventoryNode = node.getAugmentation(InventoryNode.class);
         if (inventoryNode == null) {
             LOG.error("InventoryHandler.getSnmpWriteCommunity: null inventory for node {}",
                     node.getNodeId().getValue());
            return null;
        }

        return inventoryNode.getSnmpWriteCommunity();
    }

    public static String getNodeCliUserLogin(final Node node) {
        InventoryNode inventoryNode = node.getAugmentation(InventoryNode.class);
        if (inventoryNode == null) {
            LOG.error("InventoryHandler.getNodeCliUserLogin: null inventory for node  {}",
                    node.getNodeId().getValue());
            return null;
        }

        return inventoryNode.getCliUserLogin();
    }

    public static String getNodeCliPasswordLogin(final Node node) {
        InventoryNode inventoryNode = node.getAugmentation(InventoryNode.class);
        if (inventoryNode == null) {
            LOG.error("InventoryHandler.getNodeCliPasswordLogin: null inventory for node {}",
                    node.getNodeId().getValue());
            return null;
        }

        return inventoryNode.getCliPasswordLogin();
    }

}
