/*
 * Copyright (c) 2017 Ericsson, AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.basic.network.functions.statistics;

import com.ericsson.basic.network.functions.InterfaceHandler;
import java.util.List;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ericsson
 */
public class StatisticsRunnable implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsRunnable.class);
    private String ifRef;
    private InterfaceHandler interfaceHandler;
    private RemoteStatisticsHandler remoteStatisticsHandler;
    private IpAddress nodeIpAddress;
    private Long collectionInterval;
    private StatisticsObserver observer;
    private volatile boolean finishing = false;

    public StatisticsRunnable(final IpAddress nodeIpAddress, final String ifRef,
            final InterfaceHandler interfaceHandler,
            final Long collectionInterval,
            final RemoteStatisticsHandler remoteStatisticsHandler,
            final StatisticsObserver observer) {
        this.nodeIpAddress = nodeIpAddress;
        this.ifRef = ifRef;
        this.interfaceHandler = interfaceHandler;
        this.collectionInterval = collectionInterval;
        this.observer = observer;
        this.remoteStatisticsHandler = remoteStatisticsHandler;
    }

    public void stop() {
        finishing = true;
    }

    private void notifyObserver(final StatisticsSample sample) {
        if (observer != null) {
            try {
                observer.notifyData(sample);
            } catch (final Exception e) {
                LOG.error("StatisticsRunnable.notifyObserver: exception in notifier ", e);
            }
        }
    }

    @Override
    public void run() {
        StatisticsPoller statisticsPoller = new StatisticsPoller(nodeIpAddress, ifRef, interfaceHandler,
                remoteStatisticsHandler, collectionInterval);

        StatisticsSample sample = statisticsPoller.start();
        if (sample != null) {
            notifyObserver(sample);
        }

        while (!finishing) {
            try {
                sample = statisticsPoller.getData();
                if (sample != null) {
                    notifyObserver(sample);
                }
                if (Thread.currentThread().isInterrupted()) {
                    finishing = true;
                }
            } catch (final InterruptedException e) {
                LOG.info("StatisticsRunnable.run thread interupted ", e);
                finishing = true;
            } catch (final RuntimeException e) {
                LOG.error("StatisticsRunnable.run RuntimeException exception ", e);
            } catch (final Exception e) {
                LOG.warn("StatisticsRunnable.run exception ", e);
            }
        }
    }
}
