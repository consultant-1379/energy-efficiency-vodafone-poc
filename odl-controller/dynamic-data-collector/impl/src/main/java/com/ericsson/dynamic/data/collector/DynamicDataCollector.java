/*
 * Copyright (c) 2017 Ericsson, AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.dynamic.data.collector;

import com.ericsson.basic.network.functions.BasicNetworkFunctionsService;
import com.ericsson.basic.network.functions.statistics.MonitoredDataSample;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
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
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter64;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.dynamic.data.collector.rev170714.DynamicDataCollectorService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.dynamic.data.collector.rev170714.GetInterfaceListInput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.dynamic.data.collector.rev170714.GetInterfaceListOutput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.dynamic.data.collector.rev170714.GetInterfaceListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.dynamic.data.collector.rev170714.SetInterfaceRateMonitoringInput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.dynamic.data.collector.rev170714.SetInterfaceRateMonitoringOutput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.dynamic.data.collector.rev170714.SetInterfaceRateMonitoringOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.dynamic.data.collector.rev170714.GetInterfaceMonitoredDataInput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.dynamic.data.collector.rev170714.GetInterfaceMonitoredDataOutput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.dynamic.data.collector.rev170714.GetInterfaceMonitoredDataOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.dynamic.data.collector.rev170714._interface.list.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.dynamic.data.collector.rev170714._interface.list.interfaces.InterfaceList;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.dynamic.data.collector.rev170714._interface.list.interfaces.InterfaceListKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.dynamic.data.collector.rev170714._interface.list.interfaces.InterfaceListBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ericsson
 */
public class DynamicDataCollector implements DynamicDataCollectorService, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DynamicDataCollector.class);
    private DataBroker dataBroker;
    private RpcProviderRegistry rpcProviderRegistry;
    private BasicNetworkFunctionsService basicNetworkFunctionsService;
    private BindingAwareBroker.RpcRegistration<DynamicDataCollectorService> rpcRegistration;

   /**
     * Starts Dynamic Data Collector
     */
    public void startup() {
        LOG.info("DynamicDataCollector.startup");
    }

   /**
     * Shutdown DynamicDataCollector
     */
    @Override
    public void close() {
        LOG.info("DynamicDataCollector.close");
        rpcRegistration.close();
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
        LOG.info("DynamicDataCollector.setDataBroker");
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
        LOG.info("DynamicDataCollector.setRpcRegistry");
        this.rpcProviderRegistry = rpcProviderRegistry;
        rpcRegistration = rpcProviderRegistry.addRpcImplementation(DynamicDataCollectorService.class, this);
    }

   /**
     * Returns the basicNetworkFunctionService
     *
     * @return basicNetworkFunctionService
     */
    public BasicNetworkFunctionsService getBasicNetworkFunctionsService() {
        return basicNetworkFunctionsService;
    }

   /**
     * Sets the setBasicNetworkFunctionsService
     *
     * @param basicNetworkFunctionsService
     */
    public void setBasicNetworkFunctionsService(
            final BasicNetworkFunctionsService basicNetworkFunctionsService) {
        LOG.info("DynamicDataCollector.setBasicNetworkFunctionsService");
        this.basicNetworkFunctionsService = basicNetworkFunctionsService;
    }

    @Override
    public Future<RpcResult<GetInterfaceListOutput>> getInterfaceList(final GetInterfaceListInput input) {
        final GetInterfaceListOutputBuilder getInterfaceListOutputBuilder =
                new GetInterfaceListOutputBuilder();
        final InterfacesBuilder interfacesBuilder = new InterfacesBuilder();
        final List<String> interfaceList = basicNetworkFunctionsService
                .getAllInterfaces();

        final InterfaceListBuilder interfaceListBuilder = new InterfaceListBuilder();
        final List<InterfaceList> lInterfaceList = new ArrayList<>();
        if (interfaceList != null && !interfaceList.isEmpty()) {
            for (String ifRef : interfaceList) {
                Long bandwidthCapacity =
                        basicNetworkFunctionsService.getInterfaceCurrentBandwidthCapacity(ifRef);
                Long maximumBandwidthCapacity =
                        basicNetworkFunctionsService.getInterfaceMaximumBandwidthCapacity(ifRef);
                InterfaceListKey interfaceListKey = new InterfaceListKey(ifRef);
                interfaceListBuilder.setIfRef(ifRef);
                interfaceListBuilder.setKey(interfaceListKey);
                interfaceListBuilder.setMaximumBandwidthCapacity(
                        Counter64.getDefaultInstance(Long.toString(maximumBandwidthCapacity)));
                interfaceListBuilder.setCurrentBandwidthCapacity(
                        Counter64.getDefaultInstance(Long.toString(bandwidthCapacity)));
                lInterfaceList.add(interfaceListBuilder.build());
                basicNetworkFunctionsService.resyncTopologyMaxBandwidth(ifRef, bandwidthCapacity);
            }
            interfacesBuilder.setInterfaceList(lInterfaceList);
        }

        interfacesBuilder.setNetworkRef(basicNetworkFunctionsService.getNetworkRef());
        getInterfaceListOutputBuilder.setInterfaces(interfacesBuilder.build());
        getInterfaceListOutputBuilder.setResultOk(true);

        return Futures.immediateFuture(RpcResultBuilder.<GetInterfaceListOutput> success()
                .withResult(getInterfaceListOutputBuilder.build()).build());
    }

    @Override
    public Future<RpcResult<SetInterfaceRateMonitoringOutput>> setInterfaceRateMonitoring(
            final SetInterfaceRateMonitoringInput input) {
        final SetInterfaceRateMonitoringOutputBuilder setInterfaceRateMonitoringOutputBuilder =
                new SetInterfaceRateMonitoringOutputBuilder();
        final String networkId = input.getNetworkRef().getValue();
        basicNetworkFunctionsService.setInterfaceMonitoring(networkId, input.getIfRef(),
                input.getCollectionInterval(), input.isMonitoringEnable(), input.getHistoryLength());
        setInterfaceRateMonitoringOutputBuilder.setResultOk(true);

        return Futures.immediateFuture(RpcResultBuilder.<SetInterfaceRateMonitoringOutput> success()
                .withResult(setInterfaceRateMonitoringOutputBuilder.build()).build());
    }

    @Override
    public Future<RpcResult<GetInterfaceMonitoredDataOutput>> getInterfaceMonitoredData(
            final GetInterfaceMonitoredDataInput input) {
        final GetInterfaceMonitoredDataOutputBuilder getInterfaceMonitoredDataOutputBuilder =
                new GetInterfaceMonitoredDataOutputBuilder();
        final String networkId = input.getNetworkRef().getValue();
        final MonitoredDataSample monitoredDataSample =
                basicNetworkFunctionsService.getInterfaceMonitoredData(networkId, input.getIfRef());
        if (monitoredDataSample == null) {
            getInterfaceMonitoredDataOutputBuilder.setResultOk(false);
        } else {
            getInterfaceMonitoredDataOutputBuilder.setTimestamp(monitoredDataSample.getDate());
            getInterfaceMonitoredDataOutputBuilder.setTimeInterval(monitoredDataSample.getTimeInterval());
            getInterfaceMonitoredDataOutputBuilder.setTxTrafficRate(
                    Counter64.getDefaultInstance(Long.toString(monitoredDataSample.getTxBandwidth())));
            getInterfaceMonitoredDataOutputBuilder.setCurrentBandwidthCapacity(
                    Counter64.getDefaultInstance(Long.toString(monitoredDataSample.getIfCurrentBandwidthCapacity())));
            getInterfaceMonitoredDataOutputBuilder.setCurrentOutputPower(monitoredDataSample.getIfCurrentOutputPower());
            getInterfaceMonitoredDataOutputBuilder.setNominalOutputPower(monitoredDataSample.getIfNominalOutputPower());

            getInterfaceMonitoredDataOutputBuilder.setResultOk(true);
        }

        return Futures.immediateFuture(RpcResultBuilder.<GetInterfaceMonitoredDataOutput> success()
                .withResult(getInterfaceMonitoredDataOutputBuilder.build()).build());
    }
}
