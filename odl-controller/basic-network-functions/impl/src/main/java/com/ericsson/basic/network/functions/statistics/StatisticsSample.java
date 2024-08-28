/*
 * Copyright (c) 2017 Ericsson, AB.  All rights reserved.
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
public class StatisticsSample {

    private String ifRef;
    private Long collectionInterval;
    private Long timeInterval;
    private DateAndTime date;
    private Statistics statistics;

    public StatisticsSample(final String ifRef, final Long collectionInterval,
            final Long timeInterval, final DateAndTime date, final Statistics statistics) {
        this.ifRef = ifRef;
        this.collectionInterval = collectionInterval;
        this.timeInterval = timeInterval;
        this.date = date;
        this.statistics = statistics;
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

    public Statistics getStatistics() {
        return statistics;
    }
}
