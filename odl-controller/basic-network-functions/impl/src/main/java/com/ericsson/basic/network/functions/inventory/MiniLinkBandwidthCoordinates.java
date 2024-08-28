/*
 * Copyright (c) 2017 Ericsson AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.basic.network.functions.inventory;

import com.ericsson.equipment.minilink.MiniLink_ACM;
import com.ericsson.equipment.minilink.MiniLink_ChannelSpacing;

/**
 * @author Ericsson
 */
public final class MiniLinkBandwidthCoordinates {

    private MiniLink_ChannelSpacing channelSpacing;
    private MiniLink_ACM acm;
    private Long bandwidthBps;

    public MiniLinkBandwidthCoordinates(final MiniLink_ChannelSpacing channelSpacing,
            final MiniLink_ACM acm, final Long bandwidthBps) {
        this.channelSpacing = channelSpacing;
        this.acm = acm;
        this.bandwidthBps = bandwidthBps;
    }

    public MiniLink_ChannelSpacing getChannelSpacing() {
        return channelSpacing;
    }

    public MiniLink_ACM getAcm() {
        return acm;
    }

    public Long getBandwidth() {
        return bandwidthBps;
    }
}
