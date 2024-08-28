/*
 * Copyright (c) 2017 Ericsson, AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.basic.network.functions;

import com.ericsson.basic.network.functions.statistics.MonitoredDataSample;
import java.util.List;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.network.rev161116.NetworkId;

/**
 * @author Ericsson
 */
public interface BasicNetworkFunctionsService {

    /*
     * general purpose
     */
    NetworkId getNetworkRef();

    /*
     * dynamic data collector purpose
     */
    List<String> getAllInterfaces();

    Long getInterfaceCurrentBandwidthCapacity(final String ifRef);

    Long getInterfaceMaximumBandwidthCapacity(final String ifRef);

    MonitoredDataSample getInterfaceMonitoredData(final String networkId, final String ifRef);

    void setInterfaceMonitoring(final String networkId, final String ifRef,
            final Long collectionInterval, final boolean monitoringEnable,
            final Long historyLength);

    void setInterfaceCapacity(final String networkId, final String ifRef, final Long rate);

    void resyncTopologyMaxBandwidth(final String ifRef, final Long capacity);
}
