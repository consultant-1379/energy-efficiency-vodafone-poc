/*
 * Copyright (c) 2017 Ericsson, AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.basic.network.functions;

/**
 * @author Ericsson
 */
public class LinkGroupMembershipInfo {

    private String linkId;
    private boolean isMaster;
    private boolean operStatus;
    private Long capacity;

    public LinkGroupMembershipInfo(final String linkId, final boolean isMaster) {
        this.linkId = linkId;
        this.isMaster = isMaster;
        this.operStatus = true;
        this.capacity = new Long(0);
    }

    public LinkGroupMembershipInfo(final String linkId, final boolean isMaster,
            final boolean operStatus, final Long capacity) {
        this.linkId = linkId;
        this.isMaster = isMaster;
        this.operStatus = operStatus;
        this.capacity = capacity;
    }

    public String getLinkId() {
        return linkId;
    }

    public boolean isMaster() {
        return isMaster;
    }

    public boolean getOperStatus() {
        return operStatus;
    }

    public Long getCapacity() {
        return capacity;
    }

    public void setLinkId(final String linkId) {
        this.linkId = linkId;
    }

    public void setMaster(final boolean isMaster) {
        this.isMaster = isMaster;
    }

    public void setOperStatus(final boolean operStatus) {
        this.operStatus = operStatus;
    }

    public void setCapacity(final Long capacity) {
        this.capacity = capacity;
    }
}
