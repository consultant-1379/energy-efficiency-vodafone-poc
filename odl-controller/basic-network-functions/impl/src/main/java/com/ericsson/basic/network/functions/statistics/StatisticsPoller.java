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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ericsson
 *
 */
public class StatisticsPoller implements StatisticsPolling {

    private static enum MessageType {
        polling, request
    };

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsPoller.class);
    private static final Long POLL_PERIOD_MSEC = new Long(60000);
    private static final int MESSAGE_QUEUE_DEPTH_DEFAULT = 10;
    private Long pollingInterval = POLL_PERIOD_MSEC;
    private Long collectionInterval = new Long(60);
    private Long timeInterval = new Long(0);
    private RemoteStatisticsHandler remoteStatisticsHandler;
    private IpAddress nodeIpAddress;
    private String ifRef;
    private InterfaceHandler interfaceHandler;
    private BlockingQueue<MessageType> queue =
            new LinkedBlockingQueue<MessageType>(MESSAGE_QUEUE_DEPTH_DEFAULT);
    private int enqueued = 0;

    public StatisticsPoller(final IpAddress nodeIpAddress, final String ifRef,
            final InterfaceHandler interfaceHandler,
            final RemoteStatisticsHandler remoteStatisticsHandler,
            final Long collectionInterval) {
        this.remoteStatisticsHandler = remoteStatisticsHandler;
        this.nodeIpAddress = nodeIpAddress;
        this.ifRef = ifRef;
        this.interfaceHandler = interfaceHandler;
        this.collectionInterval = collectionInterval; // used to compute bandwidth based on counters
        this.pollingInterval = 1000*collectionInterval; // seconds to milliseconds conversion, used for timer
    }

    private StatisticsSample buildStatisticsSample(final String ifRef,
            final Statistics statistics) {
        final StatisticsSample statisticsSample =  new StatisticsSample(ifRef,  collectionInterval, timeInterval,
                new ISO8601().dateAndTime(), statistics);
        timeInterval = timeInterval + 1L;
        return statisticsSample;
    }

    @Override
    public StatisticsSample start() {
        LOG.trace("StatisticsPoller.start invoked");
        final Statistics statistics = getInterfaceStatistics(nodeIpAddress, ifRef);
        enqueuePollingMsg();

        return buildStatisticsSample(ifRef, statistics);
    }

    @Override
    public void statisticsOnRequest() {
        LOG.trace("StatisticsPoller.statisticsOnRequest invoked");
        requestDataUpdate(MessageType.request);
    }

    private Statistics getInterfaceStatistics(final IpAddress nodeIpAddress,
            final String ifRef) {
        List<String> childIfRefAll = interfaceHandler.getChildIfRefList(ifRef, false);
        List<String> childIfRefOperUp = interfaceHandler.getChildIfRefList(ifRef, true);
        if (childIfRefAll != null && !childIfRefAll.isEmpty()) {
            LOG.info("StatisticsPoller.getInterfaceStatistics: child of {} {}",
                    nodeIpAddress, ifRef);
            return remoteStatisticsHandler.getChildIfRefStatistics(nodeIpAddress, childIfRefAll, childIfRefOperUp);
        }

        LOG.info("StatisticsPoller.getInterfaceStatistics: {} {}", nodeIpAddress, ifRef);

        return remoteStatisticsHandler.getStatistics(nodeIpAddress, ifRef);
    }

    @Override
    public StatisticsSample getData() throws InterruptedException {
        MessageType message = null;

        try {
            message = queue.take();
            if (enqueued > 0) {
                enqueued = enqueued - 1;
            }

            LOG.info("StatisticsPoller.getData: about to call remoteStatisticsGetHandler.getRemoteData {} {}",
                    nodeIpAddress, ifRef);

            Statistics statistics = getInterfaceStatistics(nodeIpAddress, ifRef);

            if (message == MessageType.polling) {
                LOG.trace("StatisticsPoller.getData: managed a message of type MessageType.polling.");
            } else {
                LOG.trace("StatisticsPoller.getData: managed a message of type MessageType.request.");
            }

            return buildStatisticsSample(ifRef, statistics);

        } catch (final InterruptedException e) {
            LOG.warn("StatisticsPoller.getData interrupted exception ", e);
            throw e;
        } catch (final Exception e) {
            LOG.warn("StatisticsPoller.getData exception ", e);
        }

        return null;
    }

    private void enqueuePollingMsg() {
        new Timer("SP:".concat(ifRef)).scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                try {
                    LOG.trace("StatisticsPoller.enqueuePollingMsg: put new polling message in queue");
                    queue.put(MessageType.polling);
                } catch (final InterruptedException e) {
                    LOG.warn("StatisticsPoller.enqueuePollingMsg: error during putting in queue of message", e);
                }
            }
        }, (pollingInterval == null) ? POLL_PERIOD_MSEC : pollingInterval,
           (pollingInterval == null) ? POLL_PERIOD_MSEC : pollingInterval);
    }

    private void requestDataUpdate(final MessageType request) {
        LOG.trace("StatisticsPoller.requestDataUpdate invoked");
        try {
            queue.put(request);
            enqueued = enqueued + 1;
        } catch (final InterruptedException e) {
            LOG.warn("StatisticsPoller.requestDataUpdate: interrupted while submitting enqueued {}",
                    enqueued, e);
        }
    }

    private void cleanDataStoreOperQueue() {
        while (!queue.isEmpty()) {
            queue.poll();
        }
    }
}
