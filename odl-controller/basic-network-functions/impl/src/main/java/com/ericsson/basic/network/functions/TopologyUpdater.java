/*
 * Copyright (c) 2017 Ericsson AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.basic.network.functions;

import com.google.common.base.Preconditions;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.HashMap;
import java.util.Map;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.OptimisticLockFailedException;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.rev161116.networks.Network;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.topology.rev161116.Network1;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.topology.rev161116.LinkId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.topology.rev161116.networks.network.Link;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.topology.rev161116.networks.network.LinkKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.topology.rev161116.networks.network.LinkBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.topology.rev161021.Link1;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.topology.rev161021.Link1Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.topology.rev161021.te.link.augment.Te;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.topology.rev161021.te.link.augment.TeBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.topology.rev161021.te.link.augment.te.State;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.topology.rev161021.te.link.augment.te.StateBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.topology.rev161021.te.link.config.attributes.TeLinkAttributes;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.topology.rev161021.te.link.config.attributes.TeLinkAttributesBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.te.topology.rev161021.TeBandwidth;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TopologyUpdater implements AutoCloseable, Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(TopologyUpdater.class);
    private static final int MESSAGE_QUEUE_DEPTH_DEFAULT = 50;
    private static final int MAX_RETRY = 5;
    private static final Long ZERO_LONG = new Long(0);
    private DataBroker dataBroker;
    private TopologyHandler topologyHandler;
    private BlockingQueue<TopologyUpdateData> queue =
            new LinkedBlockingQueue<TopologyUpdateData>(MESSAGE_QUEUE_DEPTH_DEFAULT);
    private volatile boolean finishing = false;

    public void init(final DataBroker dataBroker, final TopologyHandler topologyHandler) {
        LOG.info("TopologyUpdater.init");
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
        this.topologyHandler = Preconditions.checkNotNull(topologyHandler);
    }

    public void enqueueOperation(final TopologyUpdateData topologyUpdateData) {
        LOG.info("TopologyUpdater.enqueueOperation");
        try {
            queue.put(topologyUpdateData);
        } catch (final InterruptedException e) {
            LOG.warn("TopologyUpdater.enqueueOperation: Interrupted ",
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
                LOG.debug("TopologyUpdater.enqueueOperation: OptimisticLockFailedException: retry ",
                        retry);
            }
        } while (retrying && retry < MAX_RETRY);
    }

    @Override
    public void run() {
        while (!finishing) {
            try {
                TopologyUpdateData topologyUpdateData = queue.take();
                if (topologyUpdateData == null) {
                    continue;
                }
                if (topologyUpdateData.getTopologyUpdateType()
                        .equals(TopologyUpdateData.UpdateType.BandwidthUpdate)) {
                    Link link = topologyHandler.getIfRefLink(topologyUpdateData.getObjectRef());
                    if (link != null) {
                        LOG.info("TopologyUpdater.run: updateLinkBandwidth {} {}",
                                link.getLinkId(), topologyUpdateData.getBandwidth());
                        updateLinkBandwidth(link, topologyUpdateData.getBandwidth());
                    }
                } else if (topologyUpdateData.getTopologyUpdateType()
                        .equals(TopologyUpdateData.UpdateType.OperStatusUpdate)) {
                    LOG.info("TopologyUpdater.run: updateOperStatus {} {}",
                            topologyUpdateData.getObjectRef(), topologyUpdateData.getOperStatusUp());
                    updateLinkOperStatus(topologyUpdateData.getObjectRef(), topologyUpdateData.getOperStatusUp());
                }

                if (Thread.currentThread().isInterrupted()) {
                    finishing = true;
                }
            } catch (final IllegalStateException e) {
                LOG.warn("topologyUpdateData.run: ", e);
                cleanDataStoreOperQueue();
            } catch (final InterruptedException e) {
                LOG.warn("topologyUpdateData.run: ", e);
                finishing = true;
            } catch (final Exception e) {
                LOG.warn("topologyUpdateData.run: ", e);
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

    /*
     * update link te bandwidth inside topology link is housed into
     */
    private void updateLinkBandwidth(final Link link, final Long bandwidth) {
        LOG.info("topologyUpdateData.updateLinkBandwidth");
        final LinkBuilder ietfLinkBuilder = new LinkBuilder(link);

        /*
         * some of the objects retrieved are used later in builders
         * that is why we do not use topologyHandler.getTeLinkAttributes()
         */
        final Link1 link1 = link.getAugmentation(Link1.class);
        if (link1 == null) {
            LOG.warn("topologyUpdateData.updateLinkBandwidth: no Link1 augmentation for link {}",
                    link.getLinkId());
            return;
        }

        final Te te = link1.getTe();
        if (te == null) {
            LOG.warn("topologyUpdateData.updateLinkBandwidth: no Te for link {}",
                    link.getLinkId());
            return;
        }

        final State state = te.getState();
        if (state == null) {
            LOG.warn("topologyUpdateData.updateLinkBandwidth: no state for link {}",
                    link.getLinkId());
            return;
        }

        final TeLinkAttributes teLinkAttributes = state.getTeLinkAttributes();

        final TeLinkAttributesBuilder teLinkAttributesBuilder =
                (teLinkAttributes != null) ?
                new TeLinkAttributesBuilder(teLinkAttributes) :
                new TeLinkAttributesBuilder();

        teLinkAttributesBuilder.setMaxLinkBandwidth(TeBandwidth.getDefaultInstance(
                bandwidth.toString()));

        final StateBuilder stateBuilder = new StateBuilder(state);
        stateBuilder.setTeLinkAttributes(teLinkAttributesBuilder.build());

        final TeBuilder teBuilder = new TeBuilder(te);
        teBuilder.setState(stateBuilder.build());

        final Link1Builder link1Builder = new Link1Builder(link1);
        link1Builder.setTe(teBuilder.build());

        ietfLinkBuilder.addAugmentation(Link1.class, link1Builder.build());
        final LinkKey linkKey = new LinkKey(link.getLinkId());
        final InstanceIdentifier<Link> linkIid = topologyHandler.getExportedNetworkIid()
                .augmentation(Network1.class).child(Link.class, linkKey);

        WriteTransaction wt = dataBroker.newWriteOnlyTransaction();
        wt.put(LogicalDatastoreType.OPERATIONAL, linkIid, ietfLinkBuilder.build());

        try {
            submit(wt);
        } catch (final Exception e) {
            LOG.error("topologyUpdateData.updateLinkBandwidth: ", e);
       }
    }

    private void updateLinkOperStatus(final String linkId, final boolean enabledStatus) {
        topologyHandler.setLinkOperStatus(linkId, enabledStatus);
    }
}
