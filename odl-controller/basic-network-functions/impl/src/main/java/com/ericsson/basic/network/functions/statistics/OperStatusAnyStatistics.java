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
public class OperStatusAnyStatistics {

    private Long ifHcInOctets;
    private Long ifHcOutOctets;
    private Integer ifCurrentOutputPower;
    private String ifOperStatus;

    public OperStatusAnyStatistics(final Long ifHcInOctets, final Long ifHcOutOctets,
            final Integer ifCurrentOutputPower, final String ifOperStatus) {
        this.ifHcInOctets = ifHcInOctets;
        this.ifHcOutOctets = ifHcOutOctets;
        this.ifCurrentOutputPower = ifCurrentOutputPower;
        this.ifOperStatus = ifOperStatus;
    }

    public Long getIfHcInOctets() {
        return ifHcInOctets;
    }

    public Long getIfHcOutOctets() {
        return ifHcOutOctets;
    }

    public Integer getIfCurrentOutputPower() {
        return ifCurrentOutputPower;
    }

    public String getIfOperStatus() {
        return ifOperStatus;
    }
}

