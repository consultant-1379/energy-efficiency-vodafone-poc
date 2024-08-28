/*
 * Copyright (c) 2016 Ericsson, AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.basic.network.functions.statistics;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;

/**
 * @author Ericsson
 */
public class MonitoredDataSample {

    private String ifRef;
    private Long collectionInterval;
    private Long timeInterval;
    private DateAndTime date;
    private Long txBandwidth;
    private Long bandwidthCapacity;
    private Integer outputPower;
    private Integer nominalOutputPower;
    private String operStatus;

    public MonitoredDataSample(final String ifRef, final Long collectionInterval,
            final Long timeInterval, final DateAndTime date, final Long txBandwidth,
            final Long bandwidthCapacity, final Integer outputPower,
            final Integer nominalOutputPower, final String operStatus) {
        this.ifRef = ifRef;
        this.collectionInterval = collectionInterval;
        this.timeInterval = timeInterval;
        this.date = date;
        this.txBandwidth = txBandwidth;
        this.bandwidthCapacity = bandwidthCapacity;
        this.outputPower = outputPower;
        this.nominalOutputPower = nominalOutputPower;
        this.operStatus = operStatus;
    }

    public String getIfRef() {
        return ifRef;
    }

    public Long getCollectionInterval() {
        return collectionInterval;
    }

    public Long getTimeInterval() {
        return timeInterval;
    }

    public DateAndTime getDate() {
        return date;
    }

    public Long getTxBandwidth() {
        return txBandwidth;
    }

    public Long getIfCurrentBandwidthCapacity() {
        return bandwidthCapacity;
    }

    public Integer getIfCurrentOutputPower() {
        return outputPower;
    }

    public Integer getIfNominalOutputPower() {
        return nominalOutputPower;
    }

    public String getIfOperStatus() {
        return operStatus;
    }
}
