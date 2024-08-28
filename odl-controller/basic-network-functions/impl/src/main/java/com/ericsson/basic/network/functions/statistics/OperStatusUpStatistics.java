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
public class OperStatusUpStatistics {

    private Integer ifCurrentOutputPower;
    private Long ifCurrentBandwidthCapacity;
    private String ifOperStatus;

    public OperStatusUpStatistics(final Long ifCurrentBandwidthCapacity,
            final Integer ifCurrentOutputPower, final String ifOperStatus) {
        this.ifCurrentBandwidthCapacity = ifCurrentBandwidthCapacity;
        this.ifCurrentOutputPower = ifCurrentOutputPower;
        this.ifOperStatus = ifOperStatus;
    }

    public Integer getIfCurrentOutputPower() {
        return ifCurrentOutputPower;
    }

    public Long getIfCurrentBandwidthCapacity() {
        return ifCurrentBandwidthCapacity;
    }

    public String getIfOperStatus() {
        return ifOperStatus;
    }
}
