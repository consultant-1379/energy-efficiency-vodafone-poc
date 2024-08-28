/*
 * Copyright (c) 2017 Ericsson, AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.basic.network.functions.statistics;

/**
 * @author Ericsson
 */
public class Statistics {

    private Long ifHcInOctets;
    private Long ifHcOutOctets;
    private Long ifCurrentBandwidthCapacity;
    private Integer ifCurrentOutputPower;
    private String ifOperStatus;

    public Statistics(final Long ifHcInOctets, final Long ifHcOutOctets,
            final Long ifCurrentBandwidthCapacity, final Integer ifCurrentOutputPower,
            final String ifOperStatus) {
        this.ifHcInOctets = ifHcInOctets;
        this.ifHcOutOctets = ifHcOutOctets;
        this.ifCurrentBandwidthCapacity = ifCurrentBandwidthCapacity;
        this.ifCurrentOutputPower = ifCurrentOutputPower;
        this.ifOperStatus = ifOperStatus;
    }

    public Long getIfHcInOctets() {
        return ifHcInOctets;
    }

    public Long getIfHcOutOctets() {
        return ifHcOutOctets;
    }

    public Long getIfCurrentBandwidthCapacity() {
        return ifCurrentBandwidthCapacity;
    }

    public Integer getIfCurrentOutputPower() {
        return ifCurrentOutputPower;
    }

    public String getIfOperStatus() {
	    return ifOperStatus;
    }
}
