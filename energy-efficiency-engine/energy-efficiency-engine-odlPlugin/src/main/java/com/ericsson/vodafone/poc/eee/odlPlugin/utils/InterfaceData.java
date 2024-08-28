/*------------------------------------------------------------------------------
*******************************************************************************
* COPYRIGHT Ericsson 2017
*
* The copyright to the computer program(s) herein is the property of
* Ericsson Inc. The programs may be used and/or copied only with written
* permission from Ericsson Inc. or in accordance with the terms and
* conditions stipulated in the agreement/contract under which the
* program(s) have been supplied.
*******************************************************************************
*----------------------------------------------------------------------------*/

package com.ericsson.vodafone.poc.eee.odlPlugin.utils;


import com.ericsson.vodafone.poc.eee.jar.utils.IfMonitoredState;

/**
 * The type InterfaceData
 */
public class InterfaceData {
    private String networkRef;
    private String ifRef;
    private IfMonitoredState ifMonitoredState = IfMonitoredState.DISABLE;;
    private Long ifMaximumCapacity;
    private Long ifCurrentCapacity;

    /**
     * Instantiates a new InterfaceData Item.
     */
    public InterfaceData() {
        super();
    }

    /**
     * Instantiates a new InterfaceData Item.
     * @param networkRef        the network reference
     * @param ifRef             the interface reference
     * @param ifMonitoredState  the interface monitored state
     * @param ifMaximumCapacity the interface maximum capacity
     */
    public InterfaceData(final String networkRef,final String ifRef,final IfMonitoredState ifMonitoredState,final Long ifMaximumCapacity,final Long ifCurrentCapacity) {
        super();
        this.networkRef = networkRef;
        this.ifRef = ifRef;
        this.ifMonitoredState = ifMonitoredState;
        this.ifMaximumCapacity = ifMaximumCapacity;
        this.ifCurrentCapacity = ifCurrentCapacity;
    }

    public InterfaceData(final String networkRef,final String ifRef,final Long ifMaximumCapacity,final Long ifCurrentCapacity) {
        super();
        this.networkRef = networkRef;
        this.ifRef = ifRef;
        this.ifMonitoredState = IfMonitoredState.DISABLE;
        this.ifMaximumCapacity = ifMaximumCapacity;
        this.ifCurrentCapacity = ifCurrentCapacity;
    }

    /**
     * Get the network reference
     *
     * @return the label
     */
    public String getNetworkRef() {
        return networkRef;
    }

    /**
     * Set the network reference
     *
     * @param networkRef the Network Identifier to set
     */
    public void setNetworkRef(final String networkRef) {
        this.networkRef = networkRef;
    }

    /**
     * Get interface reference
     *
     * @return the InterfaceData Identifier: unique within the network
     */
    public String getIfRef() {
        return ifRef;
    }

    /**
     * Set interface reference
     *
     * @param ifRef the InterfaceData Identifier to set
     */
    public void setIfRef(final String ifRef) {
        this.ifRef = ifRef;
    }

    /**
     * Get interface monitored state
     *
     * @return the interface Monitored State
     */
    public IfMonitoredState getIfMonitoredState() {
        return ifMonitoredState;
    }

    /**
     * Set interface monitored state
     *
     * @param ifMonitoredState the InterfaceData Monitored State to set
     */
    public void setIfMonitoredState(final IfMonitoredState ifMonitoredState) {
        this.ifMonitoredState = ifMonitoredState;
    }

    /**
     * Gets interface maximum capacity.
     *
     * @return the interface maximum capacity
     */
    public Long getIfMaximumCapacity() {
        return ifMaximumCapacity;
    }

    /**
     * Sets interface maximum capacity.
     *
     * @param ifMaximumCapacity the interface maximum capacity to set
     */
    public void setIfMaximumCapacity(final Long ifMaximumCapacity) {
        this.ifMaximumCapacity = ifMaximumCapacity;
    }

    public Long getIfCurrentCapacity() {
        return ifCurrentCapacity;
    }

    public void setIfCurrentCapacity(Long ifCurrentCapacity) {
        this.ifCurrentCapacity = ifCurrentCapacity;
    }

    @Override
    public String toString() {
        return "\n InterfaceData{" +
                "\n\tnetworkRef='" + networkRef + '\'' +
                ",\n\tifRef='" + ifRef + '\'' +
                ",\n\tifMonitoredState=" + ifMonitoredState.name() +
                ",\n\tifMaximumCapacity=" + ifMaximumCapacity +
                ",\n\tifCurrentCapacity=" + ifCurrentCapacity +
                "}\n";
    }
}
