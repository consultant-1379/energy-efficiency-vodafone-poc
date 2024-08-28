/*
 * Copyright (c) 2017 Ericsson, AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.basic.network.functions.statistics;

import com.ericsson.sb.communication.SouthboundCommunicationService;
import java.util.List;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import com.ericsson.basic.network.functions.inventory.InventoryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ericsson
 *
 */
public class RemoteStatisticsHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RemoteStatisticsHandler.class);
    private static final Long ZERO_LONG = new Long(0);
    private SouthboundCommunicationService southboundCommunicationService;

    public RemoteStatisticsHandler(final SouthboundCommunicationService southboundCommunicationService) {
        this.southboundCommunicationService = southboundCommunicationService;
    }

    private OperStatusAnyStatistics getOperStatusAnyStatistics(final IpAddress nodeIpAddress, final String ifRef) {
        LOG.info("RemoteStatisticsHandler.getBandwidthStatistics: {} {}", nodeIpAddress, ifRef);

        final Long ifHcInOctets = southboundCommunicationService.getInterfaceInOctects(nodeIpAddress, ifRef,
                InventoryHandler.getIfRefNodeProductName(ifRef));

        final Long ifHcOutOctets = southboundCommunicationService.getInterfaceOutOctects(nodeIpAddress, ifRef,
                InventoryHandler.getIfRefNodeProductName(ifRef));

        final Integer ifCurrentOutputPower =
                southboundCommunicationService.getInterfaceOutputPower(nodeIpAddress, ifRef,
                InventoryHandler.getIfRefNodeProductName(ifRef));

        final String ifOperStatus = southboundCommunicationService.getInterfaceOperStatus(nodeIpAddress, ifRef,
                InventoryHandler.getIfRefNodeProductName(ifRef));

        LOG.info("RemoteStatisticsHandler.getBandwidthStatistics: ifHcInOctets {} IfHcOutOctets {} ifCurrentOutputPower {}, ifOperStatus {}",
                ifHcInOctets, ifHcOutOctets, ifCurrentOutputPower, ifOperStatus);

        return new OperStatusAnyStatistics(ifHcInOctets, ifHcOutOctets, ifCurrentOutputPower, ifOperStatus);
    }

    private OperStatusUpStatistics getOperStatusUpStatistics(final IpAddress nodeIpAddress, final String ifRef) {
        LOG.info("RemoteStatisticsHandler.getOutputPowerStatistics: {} {}", nodeIpAddress, ifRef);

        final Long ifCurrentBandwidthCapacity =
                southboundCommunicationService.getInterfaceCurrentBandwidthCapacity(nodeIpAddress, ifRef,
                InventoryHandler.getIfRefNodeProductName(ifRef));

        final Integer ifCurrentOutputPower =
                southboundCommunicationService.getInterfaceOutputPower(nodeIpAddress, ifRef,
                InventoryHandler.getIfRefNodeProductName(ifRef));

        final String ifOperStatus = southboundCommunicationService.getInterfaceOperStatus(nodeIpAddress, ifRef,
                InventoryHandler.getIfRefNodeProductName(ifRef));

        LOG.info("RemoteStatisticsHandler.getOutputPowerStatistics: ifCurrentBandwidthCapacity {}, ifCurrentOutputPower {} ifOperStatus {}",
                ifCurrentBandwidthCapacity, ifCurrentOutputPower, ifOperStatus);

        return new OperStatusUpStatistics(ifCurrentBandwidthCapacity, ifCurrentOutputPower, ifOperStatus);
    }

    public Statistics getStatistics(final IpAddress nodeIpAddress, final String ifRef) {
        LOG.info("RemoteStatisticsHandler.getStatistics: {} {}", nodeIpAddress, ifRef);

        final Long ifHcInOctets = southboundCommunicationService.getInterfaceInOctects(nodeIpAddress, ifRef,
                InventoryHandler.getIfRefNodeProductName(ifRef));

        final Long ifHcOutOctets = southboundCommunicationService.getInterfaceOutOctects(nodeIpAddress, ifRef,
                InventoryHandler.getIfRefNodeProductName(ifRef));

        final Long ifCurrentBandwidthCapacity =
                southboundCommunicationService.getInterfaceCurrentBandwidthCapacity(nodeIpAddress, ifRef,
                InventoryHandler.getIfRefNodeProductName(ifRef));

        final Integer currentOutputPower =
                southboundCommunicationService.getInterfaceOutputPower(nodeIpAddress, ifRef,
                InventoryHandler.getIfRefNodeProductName(ifRef));

        final String ifOperStatus = southboundCommunicationService.getInterfaceOperStatus(nodeIpAddress, ifRef,
                InventoryHandler.getIfRefNodeProductName(ifRef));

        LOG.info("RemoteStatisticsHandler.getStatistics: ifHcInOctets {} IfHcOutOctets {} ifCurrentBandwidthCapacity {} currentOutputPower {} ifOperStatus {}",
                ifHcInOctets, ifHcOutOctets, ifCurrentBandwidthCapacity, currentOutputPower, ifOperStatus);

        return new Statistics(ifHcInOctets, ifHcOutOctets, ifCurrentBandwidthCapacity,
                currentOutputPower, ifOperStatus);
    }

    public Statistics getChildIfRefStatistics(final IpAddress nodeIpAddress, final List<String> childIdfRefAll,
            final List<String> childIdfRefOperUp) {
        Long totIfHcInOctets = new Long(0);
        Long totIfHcOutOctets = new Long(0);
        Long totIfCurrentBandwidthCapacity = new Long(0);
        Integer totIfOutputPower = 0;

        if (childIdfRefAll != null && !childIdfRefAll.isEmpty()) {
            for (String ifRef : childIdfRefAll) {
                LOG.info("RemoteStatisticsHandler.getChildIfRefStatistics: childIdfRefAll ifRef {}", ifRef);
                OperStatusAnyStatistics operStatusAnyStatistics = getOperStatusAnyStatistics(nodeIpAddress, ifRef);
                if (operStatusAnyStatistics != null) {
                    totIfHcInOctets = totIfHcInOctets.longValue() + operStatusAnyStatistics.getIfHcInOctets().longValue();
                    totIfHcOutOctets = totIfHcOutOctets.longValue() + operStatusAnyStatistics.getIfHcOutOctets().longValue();
                }
            }
        }

        if (childIdfRefOperUp != null && !childIdfRefOperUp.isEmpty()) {
            for (String ifRef : childIdfRefOperUp) {
                LOG.info("RemoteStatisticsHandler.getChildIfRefStatistics: childIdfRefOperUp ifRef {}",
                        ifRef);
                OperStatusUpStatistics operStatusUpStatistics = getOperStatusUpStatistics(nodeIpAddress, ifRef);
                if (operStatusUpStatistics != null) {
                    totIfCurrentBandwidthCapacity = totIfCurrentBandwidthCapacity.longValue() +
                            operStatusUpStatistics.getIfCurrentBandwidthCapacity().longValue();
                    totIfOutputPower = totIfOutputPower.intValue() +
                            operStatusUpStatistics.getIfCurrentOutputPower().intValue();
                }
            }
        }

        return new Statistics(totIfHcInOctets, totIfHcOutOctets, totIfCurrentBandwidthCapacity, totIfOutputPower, "up");
    }
}
