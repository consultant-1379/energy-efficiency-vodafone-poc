/*
 * Copyright (c) 2017 Ericsson AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.basic.network.functions.statistics;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.OptimisticLockFailedException;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.dynamic.data.collector.rev170714.HistoricalData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.dynamic.data.collector.rev170714.HistoricalDataBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.dynamic.data.collector.rev170714.historical.data.History;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.dynamic.data.collector.rev170714.historical.data.HistoryKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.dynamic.data.collector.rev170714.HistoricalData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.dynamic.data.collector.rev170714.historical.data.history.CollectedData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.dynamic.data.collector.rev170714.historical.data.history.CollectedDataKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.dynamic.data.collector.rev170714.historical.data.history.CollectedDataBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter64;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.basic.network.functions.InterfaceHandler;
import com.ericsson.basic.network.functions.TopologyUpdater;
import com.google.common.base.Preconditions;

public final class StatisticsHistoryHandler implements AutoCloseable, Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(StatisticsHistoryHandler.class);
    private static final int MESSAGE_QUEUE_DEPTH_DEFAULT = 50;
    private static final int MAX_RETRY = 5;
    private static final int MAX_HISTORY_LENGTH = 15;
    private static final Long ZERO_LONG = new Long(0);
    private DataBroker dataBroker;
    private TopologyUpdater topologyUpdater;
    private InterfaceHandler interfaceHandler;
    private Map<String, StatisticsSample> ifRefLastSample = new HashMap<>();
    private Map<String, Integer> ifRefFirstOutputPower = new HashMap<>();
    private Map<String, MonitoredDataSample> ifRefMonitoredDataSample = new HashMap<>();
    private BlockingQueue<StatisticsSample> queue = new LinkedBlockingQueue<StatisticsSample>(MESSAGE_QUEUE_DEPTH_DEFAULT);
    private volatile boolean finishing = false;

    public void init(final DataBroker dataBroker, final TopologyUpdater topologyUpdater, final InterfaceHandler interfaceHandler) {
        LOG.info("StatisticsHistoryHandler.init");
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
        this.interfaceHandler = Preconditions.checkNotNull(interfaceHandler);
        this.topologyUpdater = Preconditions.checkNotNull(topologyUpdater);
        initHistory();
    }

    public void enqueueOperation(final StatisticsSample statisticsSample) {
        try {
            queue.put(statisticsSample);
        } catch (final InterruptedException e) {
            LOG.warn("StatisticsHistoryHandler.enqueueOperation: Interrupted ", e);
        }
    }

    public synchronized MonitoredDataSample getIfRefMonitoredData(final String ifRef) {
        return ifRefMonitoredDataSample.get(ifRef);
    }

    private void submit(final WriteTransaction tx) throws Exception {
        boolean retrying = false;
        int retry = 0;
        do {
            try {
                tx.submit().checkedGet();
                retrying = false;
                retry = 0;
            } catch (final OptimisticLockFailedException e) {
                retrying = true;
                retry = retry + 1;
                LOG.debug("StatisticsHistoryHandler.enqueueOperation: OptimisticLockFailedException: retry ", retry);
            }
        } while (retrying && retry < MAX_RETRY);
    }

    @Override
    public void run() {
        while (!finishing) {
            try {
                StatisticsSample statisticsSample = queue.take();
                final MonitoredDataSample monitoredDataSample = computeMonitoredDataSample(statisticsSample);
                if (monitoredDataSample != null) {
                    LOG.info("StatisticsHistoryHandler.run: {}", monitoredDataSample);
                    updateHistoryDatastore(monitoredDataSample);
                }
                if (Thread.currentThread().isInterrupted()) {
                    finishing = true;
                }
            } catch (final IllegalStateException e) {
                LOG.warn("StatisticsHistoryHandler.run: ", e);
                cleanDataStoreOperQueue();
            } catch (final InterruptedException e) {
                LOG.warn("StatisticsHistoryHandler.run: ", e);
                finishing = true;
            } catch (final Exception e) {
                LOG.warn("StatisticsHistoryHandler.run: ", e);
            }
        }

        cleanDataStoreOperQueue();
    }

    @Override
    public void close() {

    }

    private void cleanDataStoreOperQueue() {
        while (!queue.isEmpty()) {
            queue.poll();
        }
    }

    private MonitoredDataSample computeMonitoredDataSample(final StatisticsSample statisticSample) {
        final Long collectionInterval = statisticSample.getCollectionInterval();
        final Long timeInterval = statisticSample.getTimeInterval();
        final DateAndTime date = statisticSample.getDate();
        final Long currentIfHcOutOctets = statisticSample.getStatistics().getIfHcOutOctets();
        final Long ifCurrentBandwidthCapacity = statisticSample.getStatistics().getIfCurrentBandwidthCapacity();
        final Integer ifOutputPower = statisticSample.getStatistics().getIfCurrentOutputPower();
        final String ifOperStatus = statisticSample.getStatistics().getIfOperStatus();

        Long lastIfHcOutOctets = ZERO_LONG;
        Long currentBandwidth = ZERO_LONG;

        if (ifRefFirstOutputPower.get(statisticSample.getIfRef()) == null) {
            ifRefFirstOutputPower.put(statisticSample.getIfRef(), statisticSample.getStatistics().getIfCurrentOutputPower());
        }

        MonitoredDataSample monitoredDataSample = null;
        final StatisticsSample lastHistoricalSample = ifRefLastSample.get(statisticSample.getIfRef());
        if (lastHistoricalSample != null) {
            lastIfHcOutOctets = lastHistoricalSample.getStatistics().getIfHcOutOctets();
            currentBandwidth = (currentIfHcOutOctets.longValue() - lastIfHcOutOctets.longValue()) / collectionInterval.longValue();

            LOG.info(
                    "StatisticsHistoryHandler.computeMonitoredDataSample: currentIfHcOutOctets {}, lastIfHcOutOctets {} collectionInterval {} currentBandwidth {}",
                    currentIfHcOutOctets, lastIfHcOutOctets, collectionInterval, currentBandwidth);

            if (currentBandwidth.longValue() < 0) {
                LOG.warn("StatisticsHistoryHandler.computeMonitoredDataSample: computed bandwidth {} is negative changing to zero", currentBandwidth);
                currentBandwidth = ZERO_LONG;
            } else if (currentBandwidth.longValue() > ifCurrentBandwidthCapacity.longValue()) {
                LOG.warn("StatisticsHistoryHandler.computeMonitoredDataSample: computed bandwidth {} is greater than interface capacity {}",
                        currentBandwidth, ifCurrentBandwidthCapacity);
                currentBandwidth = ifCurrentBandwidthCapacity;
            }

            Integer nominalOutputPower = ifRefFirstOutputPower.get(statisticSample.getIfRef());

            /*
             * updates of the nominal output power is available when the current bandwidth is equal to the maximum bandwidth capacity
             */
            final Long nominalCapacity = interfaceHandler.getIfRefStoredNominalCapacity(statisticSample.getIfRef());

            if (nominalCapacity != null && nominalCapacity.equals(ifCurrentBandwidthCapacity)) {
                nominalOutputPower = ifOutputPower;
                ifRefFirstOutputPower.put(statisticSample.getIfRef(), nominalOutputPower);
            }

            /*
             * bandwidth may increase with respect the initial value
             */
            if (ifCurrentBandwidthCapacity.longValue() >= nominalCapacity.longValue() ) {
                interfaceHandler.updateIfRefNominalCapacity(statisticSample.getIfRef(), ifCurrentBandwidthCapacity);
            }

            LOG.info(
                    "StatisticsHistoryHandler.computeMonitoredDataSample: ifRef {} nominalCapacity {}, ifCurrentBandwidthCapacity {} ifOutputPower {} nominalOutputPower {}",
                    statisticSample.getIfRef(), nominalCapacity, ifCurrentBandwidthCapacity, ifOutputPower, nominalOutputPower);

            /*
             * covering the scenario where the currentOutputPower increases above the originally recorded nominalOutputPower. The latter is updated
             * with the former value.
             */
            if (ifOutputPower.intValue() > nominalOutputPower.intValue()) {
                ifRefFirstOutputPower.put(statisticSample.getIfRef(), ifOutputPower);
                nominalOutputPower = ifOutputPower;
            }

            monitoredDataSample = new MonitoredDataSample(statisticSample.getIfRef(), collectionInterval, timeInterval, date, currentBandwidth,
                    ifCurrentBandwidthCapacity, ifOutputPower, nominalOutputPower, ifOperStatus);

            ifRefMonitoredDataSample.put(statisticSample.getIfRef(), monitoredDataSample);
        }

        /*
         * last sample update
         */
        ifRefLastSample.put(statisticSample.getIfRef(), statisticSample);

        interfaceHandler.updateIfRefLinkOperStatus(statisticSample.getIfRef(), ifOperStatus);

        return monitoredDataSample;
    }

    private void updateHistoryDatastore(final MonitoredDataSample monitoredDataSample) {
        LOG.info("StatisticsHistoryHandler.updateHistoryDatastore");
        final Long timeIntervalDsIndex = monitoredDataSample.getTimeInterval() % MAX_HISTORY_LENGTH;
        final CollectedDataKey collectedDataKey = new CollectedDataKey(timeIntervalDsIndex);
        final HistoryKey historyKey = new HistoryKey(monitoredDataSample.getIfRef());

        InstanceIdentifier<CollectedData> iid = InstanceIdentifier.create(HistoricalData.class).child(History.class, historyKey)
                .child(CollectedData.class, collectedDataKey);

        final CollectedDataBuilder collectedDataBuilder = new CollectedDataBuilder();
        collectedDataBuilder.setKey(collectedDataKey);
        collectedDataBuilder.setIndex(timeIntervalDsIndex);
        collectedDataBuilder.setTimeInterval(monitoredDataSample.getTimeInterval());
        collectedDataBuilder.setTimestamp(monitoredDataSample.getDate());
        collectedDataBuilder.setTxTrafficRate(Counter64.getDefaultInstance(Long.toString(monitoredDataSample.getTxBandwidth())));
        collectedDataBuilder
                .setCurrentBandwidthCapacity(Counter64.getDefaultInstance(Long.toString(monitoredDataSample.getIfCurrentBandwidthCapacity())));
        collectedDataBuilder.setCurrentOutputPower(monitoredDataSample.getIfCurrentOutputPower());
        collectedDataBuilder.setNominalOutputPower(monitoredDataSample.getIfNominalOutputPower());
        collectedDataBuilder.setOperStatus(monitoredDataSample.getIfOperStatus());
        collectedDataBuilder.setRequiredBandwidthCapacity(
                Counter64.getDefaultInstance(Long.toString(interfaceHandler.getIfRefRequiredCapacity(monitoredDataSample.getIfRef()))));
        collectedDataBuilder.setRequestDateAndTime(interfaceHandler.getIfRefRequestDateAndTime(monitoredDataSample.getIfRef()));

        WriteTransaction wt = dataBroker.newWriteOnlyTransaction();
        wt.put(LogicalDatastoreType.OPERATIONAL, iid, collectedDataBuilder.build(), true);

        try {
            submit(wt);
        } catch (final Exception e) {
            LOG.error("StatisticsHistoryHandler.updateHistoryDatastore: ", e);
        }
    }

    private void initHistory() {
        LOG.info("StatisticsHistoryHandler.initHistory");
        final InstanceIdentifier<HistoricalData> iid = InstanceIdentifier.create(HistoricalData.class);
        final HistoricalDataBuilder historicalDataBuilder = new HistoricalDataBuilder();

        WriteTransaction wt = dataBroker.newWriteOnlyTransaction();
        wt.put(LogicalDatastoreType.OPERATIONAL, iid, historicalDataBuilder.build());

        try {
            submit(wt);
        } catch (final Exception e) {
            LOG.error("StatisticsHistoryHandler.updateHistoryDatastore: ", e);
        }
    }
}
