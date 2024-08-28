/*
 * Copyright (c) 2017 Ericsson, AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.basic.network.functions;

import java.util.ArrayList;

/**
 * @author Ericsson
 */
public class LinkGroupInfo {

    private String linkGroupId;  // could be a lag-id or a bonding-id
    private String linkId;
    private String sourceNodeId;
    private String sourceTpId;
    private String destNodeId;
    private String destTpId;
    private String peerLinkGroupId;
    private ArrayList<LinkGroupMembershipInfo> linkGroupMembershipInfo;
    private boolean isLag = false;

    public LinkGroupInfo(final String linkGroupId, final String linkId, final String sourceNodeId,
            final String sourceTpId, final String destNodeId, final String destTpId,
            final ArrayList<LinkGroupMembershipInfo> linkGroupMembershipInfo, final boolean isLag) {
        this.linkGroupId = linkGroupId;
        this.linkId = linkId;
        this.sourceNodeId = sourceNodeId;
        this.sourceTpId = sourceTpId;
        this.destNodeId = destNodeId;
        this.destTpId = destTpId;
        this.linkGroupMembershipInfo = linkGroupMembershipInfo;
        this.isLag = isLag;
    }

    public boolean isLag() {
        return isLag;
    }

    public String getLinkGroupId() {
        return linkGroupId;
    }

    public String getLinkId() {
        return linkId;
    }

    public String getPeerLinkGroupId() {
        return peerLinkGroupId;
    }

    public ArrayList<LinkGroupMembershipInfo> getLinkGroupMembershipInfo() {
        return linkGroupMembershipInfo;
    }

    public String getSourceNodeId() {
        return sourceNodeId;
    }

    public String getSourceTpId() {
        return sourceTpId;
    }

    public String getDestNodeId() {
        return destNodeId;
    }

    public String getDestTpId() {
        return destTpId;
    }

    public void setLinkId(final String linkId) {
        this.linkId = linkId;
    }

    public void setPeerLinkGroupId(final String peerLinkGroupId) {
        this.peerLinkGroupId = peerLinkGroupId;
    }

    public void setLinkGroupMembershipInfo(final ArrayList<LinkGroupMembershipInfo> linkGroupMembershipInfo) {
        this.linkGroupMembershipInfo = linkGroupMembershipInfo;
    }
}
