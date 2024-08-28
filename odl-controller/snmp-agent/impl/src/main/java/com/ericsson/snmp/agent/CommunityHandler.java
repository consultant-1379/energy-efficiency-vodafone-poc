/*
 * Copyright (c) 2017 Ericsson, AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.snmp.agent;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;

/**
 * @author Ericsson
 */
public class CommunityHandler {

    private static final String DEFAULT_READ_COMMUNITY_63xx = "public";
    private static final String DEFAULT_READ_COMMUNITY_66xx = "Ericsson_r";
    private static final String DEFAULT_WRITE_COMMUNITY_63xx = "private";
    private static final String DEFAULT_WRITE_COMMUNITY_66xx = "Ericsson_w";
    private Map<IpAddress, String> readCommunityMap = new HashMap<>();
    private Map<IpAddress, String> writeCommunityMap = new HashMap<>();

    public void addNodeReadCommunity(final IpAddress nodeIpAddress, final String readCommunity) {
        readCommunityMap.put(nodeIpAddress, readCommunity);
    }

    public String getNodeReadCommunity(final IpAddress nodeIpAddress) {
        return readCommunityMap.get(nodeIpAddress);
    }

    public void addNodeWriteCommunity(final IpAddress nodeIpAddress, final String readCommunity) {
        writeCommunityMap.put(nodeIpAddress, readCommunity);
    }

    public String getNodeWriteCommunity(final IpAddress nodeIpAddress) {
        return writeCommunityMap.get(nodeIpAddress);
    }
}
