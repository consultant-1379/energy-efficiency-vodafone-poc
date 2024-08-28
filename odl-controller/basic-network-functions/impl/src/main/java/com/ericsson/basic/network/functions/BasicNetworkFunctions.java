/*
 * Copyright (c) 2017 Ericsson, AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.basic.network.functions;

import com.ericsson.basic.network.functions.statistics.MonitoredDataSample;
import com.ericsson.sb.communication.SouthboundCommunicationService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Future;
import java.util.List;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.rev161116.networks.Network;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.rev161116.NetworkId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ericsson
 */
public class BasicNetworkFunctions implements BasicNetworkFunctionsService,
        DataTreeChangeListener<Network>, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(BasicNetworkFunctions.class);
    private static final String TOPOLOGY_NAME = "mini-link-topo";
    private DataBroker dataBroker;
    private RpcProviderRegistry rpcProviderRegistry;
    private ListenerRegistration<BasicNetworkFunctions> listenerRegistration;
    private SouthboundCommunicationService southboundCommunicationService;
    private TopologyHandler topologyHandler = new TopologyHandler();

   /**
     * Starts BasicNetworkFunctions
     */
    public void startup() {
        LOG.info("BasicNetworkFunctions.startup");
        topologyHandler.setDataBroker(dataBroker);
        topologyHandler.setSouthboundCommunicationService(southboundCommunicationService);
        topologyHandler.init();
        topologyHandler.createIetfNetwork(TOPOLOGY_NAME);
        registerNetworkListening(topologyHandler.buildNetworkIid(TOPOLOGY_NAME));
    }

   /**
     * Shutdown BasicNetworkFunctions
     */
    @Override
    public void close() throws InterruptedException {
        LOG.info("BasicNetworkFunctions.close");
        topologyHandler.close();
    }

   /**
     * Returns the dataBroker
     *
     * @return DataBroker
     */
    public DataBroker getDataBroker() {
        return dataBroker;
    }

   /**
     * Sets the dataBroker
     *
     * @param dataBroker
     */
    public void setDataBroker(final DataBroker dataBroker) {
        LOG.info("BasicNetworkFunctions.setDataBroker");
        this.dataBroker = dataBroker;
    }

   /**
     * Returns the rpcProviderRegistry
     *
     * @return rpcProviderRegistry
     */
    public RpcProviderRegistry getRpcRegistry() {
        return rpcProviderRegistry;
    }

    /**
     * Sets the rpcProviderRegistry
     *
     * @param rpcProviderRegistry
     */
    public void setRpcRegistry(final RpcProviderRegistry rpcProviderRegistry) {
        LOG.info("BasicNetworkFunctions.setRpcRegistry");
        this.rpcProviderRegistry = rpcProviderRegistry;
    }

     /**
     * Returns the southboundCommunicationService
     *
     * @return southboundCommunicationService
     */
    public SouthboundCommunicationService getSouthboundCommunicationService() {
        return southboundCommunicationService;
    }

   /**
     * Sets the southboundCommunicationService
     *
     * @param southboundCommunicationService
     */
    public void setSouthboundCommunicationService(
            final SouthboundCommunicationService southboundCommunicationService) {
        LOG.info("BasicNetworkFunctions.southboundCommunicationService");
        this.southboundCommunicationService = southboundCommunicationService;
    }

     protected void registerNetworkListening(final InstanceIdentifier<Network> networkIid) {
        final DataTreeIdentifier<Network> dataTreeIid =
                new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION, networkIid);
        this.listenerRegistration = dataBroker.registerDataTreeChangeListener(dataTreeIid, this);
    }

    public void onDataTreeChanged(Collection<DataTreeModification<Network>> changes) {
        topologyHandler.handleNetworkChanges(LogicalDatastoreType.CONFIGURATION, changes);
    }

    @Override
    public NetworkId getNetworkRef() {
        return new NetworkId(TOPOLOGY_NAME);
    }

    @Override
    public List<String> getAllInterfaces() {
        return topologyHandler.getAllInterfaces(true);
    }

    @Override
    public Long getInterfaceCurrentBandwidthCapacity(final String ifRef) {
        Long currentBandwidthCapacity = topologyHandler.getInterfaceHandler()
                .getInterfaceCurrentBandwidthCapacity(ifRef);

        return currentBandwidthCapacity;
    }

    @Override
    public Long getInterfaceMaximumBandwidthCapacity(final String ifRef) {
        return topologyHandler.getInterfaceHandler().getInterfaceMaximumBandwidthCapacity(ifRef);
    }

    @Override
    public MonitoredDataSample getInterfaceMonitoredData(final String networkId, final String ifRef) {
        return topologyHandler.getInterfaceHandler().getInterfaceMonitoredData(ifRef);
    }

    @Override
    public void setInterfaceMonitoring(final String networkId, final String ifRef,
            final Long collectionInterval, final boolean monitoringEnable,
            final Long historyLength) {
        topologyHandler.getInterfaceHandler().setInterfaceMonitoring(ifRef,
                collectionInterval, monitoringEnable, historyLength);
    }

    @Override
    public synchronized void setInterfaceCapacity(final String networkId, final String ifRef, final Long rate) {
		LOG.info("BasicNetworkFunctions.setInterfaceCapacity ifRef {}", ifRef);
        topologyHandler.getInterfaceHandler().setInterfaceCapacity(ifRef, rate);
    }

    @Override
    public void resyncTopologyMaxBandwidth(final String ifRef, final Long capacity) {
       topologyHandler.resyncTopologyMaxBandwidth(ifRef, capacity);
    }
}
