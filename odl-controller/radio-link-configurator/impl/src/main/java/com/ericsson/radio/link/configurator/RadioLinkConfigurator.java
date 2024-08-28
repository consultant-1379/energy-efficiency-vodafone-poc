/*
 * Copyright (c) 2017 Ericsson, AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.radio.link.configurator;

import com.ericsson.basic.network.functions.BasicNetworkFunctionsService;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Future;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.radio.link.configurator.rev170714.RadioLinkConfiguratorService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.radio.link.configurator.rev170714.SetInterfaceCapacityInput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.radio.link.configurator.rev170714.SetInterfaceCapacityOutput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.radio.link.configurator.rev170714.SetInterfaceCapacityOutputBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ericsson
 */
public class RadioLinkConfigurator implements RadioLinkConfiguratorService, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(RadioLinkConfigurator.class);
    private DataBroker dataBroker;
    private RpcProviderRegistry rpcProviderRegistry;
    private BasicNetworkFunctionsService basicNetworkFunctionsService;
    private BindingAwareBroker.RpcRegistration<RadioLinkConfiguratorService> rpcRegistration;

   /**
     * Starts Radio Link Configurator
     */
    public void startup() {
        LOG.info("RadioLinkConfigurator.startup");
    }

   /**
     * Shutdown Radio Link Configurator
     */
    @Override
    public void close() {
        LOG.info("RadioLinkConfigurator.close");
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
        LOG.info("RadioLinkConfigurator.setDataBroker");
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
        LOG.info("RadioLinkConfigurator.setRpcRegistry");
        this.rpcProviderRegistry = rpcProviderRegistry;
        rpcRegistration = rpcProviderRegistry.addRpcImplementation(RadioLinkConfiguratorService.class, this);
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
        LOG.info("RadioLinkConfigurator.setBasicNetworkFunctionsService");
        this.basicNetworkFunctionsService = basicNetworkFunctionsService;
    }

    public Future<RpcResult<SetInterfaceCapacityOutput>> setInterfaceCapacity(final SetInterfaceCapacityInput input) {
        final SetInterfaceCapacityOutputBuilder setInterfaceCapacityOutputBuilder =
                new SetInterfaceCapacityOutputBuilder();
        final String networkId = input.getNetworkRef().getValue();

        try {
            final Long requiredCapacity = new Long(input.getBandwidthCapacity().getValue().longValue());
            LOG.info("RadioLinkConfigurator.setInterfaceCapacity: {} {} requiredCapacity {}",
                   input.getNetworkRef(), input.getIfRef(), requiredCapacity);
            basicNetworkFunctionsService.setInterfaceCapacity(networkId, input.getIfRef(), requiredCapacity);
            setInterfaceCapacityOutputBuilder.setResultOk(true);
        } catch (final NumberFormatException e) {
            LOG.error("RadioLinkConfigurator.setInterfaceCapacity: {} ", input.getIfRef(), e);
            setInterfaceCapacityOutputBuilder.setResultOk(false);
        }

        return Futures.immediateFuture(RpcResultBuilder.<SetInterfaceCapacityOutput> success()
                .withResult(setInterfaceCapacityOutputBuilder.build()).build());
    }
}
