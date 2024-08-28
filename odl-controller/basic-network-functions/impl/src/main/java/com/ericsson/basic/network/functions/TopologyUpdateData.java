/*
 * Copyright (c) 2017 Ericsson AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.basic.network.functions;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.types.rev161026.TeOperStatus;

public final class TopologyUpdateData {

    public enum UpdateType {
        BandwidthUpdate,
        OperStatusUpdate
    }

    private UpdateType updateType;
    private String objectRef;
    private Long bandwidth;
    private boolean operStatusUp;

    public TopologyUpdateData(final UpdateType updateType, final String ifRef,
            final Long bandwidth) {
        this.updateType = updateType;
        this.objectRef = ifRef;
        this.bandwidth = bandwidth;
    }

    public TopologyUpdateData(final UpdateType updateType, final String linkId,
            final boolean operStatusUp) {
        this.updateType = updateType;
        this.objectRef = linkId;
        this.operStatusUp = operStatusUp;
    }

    public UpdateType getTopologyUpdateType() {
        return updateType;
    }

    public String getObjectRef() {
        return objectRef;
    }

    public Long getBandwidth() {
        return bandwidth;
    }

    public boolean getOperStatusUp() {
        return operStatusUp;
    }
}
