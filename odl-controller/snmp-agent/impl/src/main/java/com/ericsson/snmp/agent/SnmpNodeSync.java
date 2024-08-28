/*
 * Copyright (c) 2017 Ericsson AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.snmp.agent;

import com.google.common.base.Preconditions;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.OptimisticLockFailedException;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SnmpNodeSync implements AutoCloseable, Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(SnmpNodeSync.class);
    private static final int MESSAGE_QUEUE_DEPTH_DEFAULT = 10;
    private static final int MAX_RETRY = 5;
    private DataBroker dataBroker;
    private SnmpAgentOperations snmpAgentOperations;
    private BlockingQueue<SnmpNodeSyncData> queue =
            new LinkedBlockingQueue<SnmpNodeSyncData>(MESSAGE_QUEUE_DEPTH_DEFAULT);
    private volatile boolean finishing = false;

    public void init(final DataBroker dataBroker, final SnmpAgentOperations snmpAgentOperations) {
        LOG.info("SnmpNodeSync.init");
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
        this.snmpAgentOperations = Preconditions.checkNotNull(snmpAgentOperations);
    }

    public void enqueueOperation(final SnmpNodeSyncData snmpNodeSyncData) {
        try {
            queue.put(snmpNodeSyncData);
        } catch (final InterruptedException e) {
            LOG.warn("SnmpNodeSync.enqueueOperation: Interrupted ",
                    e);
        }
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
                LOG.debug("SnmpNodeSync.enqueueOperation: OptimisticLockFailedException: retry ",
                        retry);
            }
        } while (retrying && retry < MAX_RETRY);
    }

    @Override
    public void run() {
        while (!finishing) {
            try {
                SnmpNodeSyncData snmpNodeSyncData = queue.take();
                LOG.info("SnmpNodeSync.run: processing {} {}", snmpNodeSyncData.getNodeId(),
                        snmpNodeSyncData.getNodeIpAddress());

                if (snmpNodeSyncData != null) {
                    snmpAgentOperations.loadInterfaceData(
                            snmpNodeSyncData.getNodeId(), snmpNodeSyncData.getNodeIpAddress(),
                            snmpNodeSyncData.getProductName());
                }

                if (Thread.currentThread().isInterrupted()) {
                    finishing = true;
                }
            } catch (final IllegalStateException e) {
                LOG.warn("SnmpNodeSync.run: ", e);
                cleanDataStoreOperQueue();
            } catch (final InterruptedException e) {
                LOG.warn("SnmpNodeSync.run: ", e);
                finishing = true;
            } catch (final Exception e) {
                LOG.warn("SnmpNodeSync.run: ", e);
            }
        }
    }

    @Override
    public void close() {

    }

    private void cleanDataStoreOperQueue() {
        while (!queue.isEmpty()) {
            queue.poll();
        }
    }
}
