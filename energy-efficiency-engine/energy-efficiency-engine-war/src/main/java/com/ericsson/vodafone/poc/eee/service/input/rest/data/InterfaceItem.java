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

package com.ericsson.vodafone.poc.eee.service.input.rest.data;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The type InterfaceItem
 */
@XmlRootElement
public class InterfaceItem extends BaseItem {

    private static final long serialVersionUID = 1L;

    @XmlElement
    private String networkRef;

    @XmlElement
    private String ifRef;

    // in milliseconds
    @XmlElement
    private Long timeInterval;

    @XmlElement
    private Integer ifMonitoredState = Integer.valueOf(0);

    @XmlElement
    private Long ifMaximumCapacity;

    @XmlElement
    private Long ifCurrentCapacity;

    @XmlElement
    private ObservationItem ifTrafficBandwidth;

    @XmlElement
    private PredictionItem ifPredictedTrafficBandwidth;

    @XmlElement
    private Integer ifObservationListSize = 0;

    @XmlElement
    private List<ObservationItem> ifObservationList;

    @XmlElement
    private Integer ifPredictionListSize = 0;

    @XmlElement
    private List<PredictionItem> ifPredictionList;

    @XmlElement
    private Integer ifConfiguredBandwidthListSize = 0;

    @XmlElement
    private List<ConfiguredBandwidthItem> ifConfiguredBandwidthList;

    @XmlElement
    private SavingItem ifSaving;

    /**
     * Instantiates a new Interface Item.
     */
    public InterfaceItem() {
        super();
    }

    public InterfaceItem(final InterfaceItem i) {
        this(i.getIndex(),
             i.getTime(),
             i.networkRef,
             i.ifRef,
             i.timeInterval,
             i.ifMonitoredState,
             i.ifMaximumCapacity,
             i.ifCurrentCapacity,
             i.ifTrafficBandwidth,
             i.ifPredictedTrafficBandwidth,
             i.ifObservationListSize,
             i.ifObservationList,
             i.ifPredictionListSize,
             i.ifPredictionList,
             i.ifConfiguredBandwidthListSize,
             i.ifConfiguredBandwidthList,
             i.ifSaving);

    }

    private InterfaceItem(final Integer index,
                          final Long time,
                          final String networkRef,
                          final String ifRef,
                          final Long timeInterval,
                          final Integer ifMonitoredState,
                          final Long ifMaximumCapacity,
                          final Long ifCurrentCapacity,
                          final ObservationItem ifTrafficBandwidth,
                          final PredictionItem ifPredictedTrafficBandwidth,
                          final Integer ifObservationListSize,
                          final List<ObservationItem> ifObservationList,
                          final Integer ifPredictionListSize,
                          final List<PredictionItem> ifPredictionList,
                          final Integer ifConfiguredBandwidthListSize,
                          final List<ConfiguredBandwidthItem> ifConfiguredBandwidthList,
                          final SavingItem ifSaving) {
        super(index, time);
        this.networkRef = networkRef;
        this.ifRef = ifRef;
        this.timeInterval = timeInterval;
        this.ifMonitoredState = ifMonitoredState;
        this.ifMaximumCapacity = ifMaximumCapacity;
        this.ifCurrentCapacity = ifCurrentCapacity;
        this.ifTrafficBandwidth = ifTrafficBandwidth;
        this.ifPredictedTrafficBandwidth = ifPredictedTrafficBandwidth;
        this.ifObservationListSize = ifObservationListSize;
        this.ifObservationList = ifObservationList;
        this.ifPredictionListSize = ifPredictionListSize;
        this.ifPredictionList = ifPredictionList;
        this.ifConfiguredBandwidthListSize = ifConfiguredBandwidthListSize;
        this.ifConfiguredBandwidthList = ifConfiguredBandwidthList;
        this.ifSaving = ifSaving;
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
     * @param networkRef
     *            the Network Identifier to set
     */
    public void setNetworkRef(final String networkRef) {
        this.networkRef = networkRef;
    }

    /**
     * Get interface reference
     *
     * @return the Interface Identifier: unique within the network
     */
    public String getIfRef() {
        return ifRef;
    }

    /**
     * Set interface reference
     *
     * @param ifRef
     *            the Interface Identifier to set
     */
    public void setIfRef(final String ifRef) {
        this.ifRef = ifRef;
    }

    public Long getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(Long timeInterval) {
        this.timeInterval = timeInterval;
    }

    /**
     * Get interface monitored state
     *
     * @return the interface Monitored State
     */
    public Integer getIfMonitoredState() {
        return ifMonitoredState;
    }

    /**
     * Set interface monitored state
     *
     * @param ifMonitoredState
     *            the Interface Monitored State to set
     */
    public void setIfMonitoredState(final Integer ifMonitoredState) {
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
     * @param ifMaximumCapacity
     *            the interface maximum capacity to set
     */
    public void setIfMaximumCapacity(final Long ifMaximumCapacity) {
        this.ifMaximumCapacity = ifMaximumCapacity;
    }

    /**
     * Get interface current capacity
     *
     * @return the interface current capacity
     */
    public Long getIfCurrentCapacity() {
        return ifCurrentCapacity;
    }

    /**
     * Set if current capacity
     *
     * @param ifCurrentCapacity
     *            the interface current capacity to set
     */
    public void setIfCurrentCapacity(final Long ifCurrentCapacity) {
        this.ifCurrentCapacity = ifCurrentCapacity;
    }


    /**
     * Get interface current observation
     *
     * @return the current observation
     */
    public ObservationItem getIfTrafficBandwidth() {
        return ifTrafficBandwidth;
    }

    /**
     * Set if current observation
     *
     * @param ifTrafficBandwidth
     *            the interface current observation to set
     */
    public void setIfTrafficBandwidth(final ObservationItem ifTrafficBandwidth) {
        this.ifTrafficBandwidth = ifTrafficBandwidth;
    }

    /**
     * Get interface current predicted traffic bandwidth
     *
     * @return the current predicted traffic bandwidth
     */
    public PredictionItem getIfPredictedTrafficBandwidth() {
        return ifPredictedTrafficBandwidth;
    }

    /**
     * Set if current predicted traffic bandwidth
     *
     * @param ifPredictedTrafficBandwidth
     *            the interface current predicted traffic bandwidth to set
     */
    public void setIfPredictedTrafficBandwidth(PredictionItem ifPredictedTrafficBandwidth) {
        this.ifPredictedTrafficBandwidth = ifPredictedTrafficBandwidth;
    }

    public Integer getIfObservationListSize() {
        return ifObservationListSize;
    }

    public List<ObservationItem> getIfObservationList() {
        return ifObservationList;
    }

    public void setIfObservationList(final List<ObservationItem> ifObservationList) {
        this.ifObservationList = ifObservationList;
        this.ifObservationListSize = ifObservationList.size();
    }

    public Integer getIfPredictionListSize() {
        return ifPredictionListSize;
    }

    /**
     * Set Interface PredictionItem
     *
     * @param ifPredictionList
     *            The traffic bandwidth prediction for future and past monitoring interval for this interface
     */
    public void setIfPredictionList(final List<PredictionItem> ifPredictionList) {
        this.ifPredictionList = ifPredictionList;
        this.ifPredictionListSize = ifPredictionList.size();
    }

    public List<PredictionItem> getIfPredictionList() {
        return ifPredictionList;
    }

    public Integer getIfConfiguredBandwidthListSize() {
        return ifConfiguredBandwidthListSize;
    }

    public List<ConfiguredBandwidthItem> getIfConfiguredBandwidthList() {
        return ifConfiguredBandwidthList;
    }

    public void setIfConfiguredBandwidthList(List<ConfiguredBandwidthItem> ifConfiguredBandwidthList) {
        this.ifConfiguredBandwidthList = ifConfiguredBandwidthList;
        this.ifConfiguredBandwidthListSize = ifConfiguredBandwidthList.size();
    }

    public SavingItem getIfSaving() {
        return ifSaving;
    }

    public void setIfSaving(SavingItem ifSaving) {
        this.ifSaving = ifSaving;
    }

    @Override
    public String toString() {

        String ifObservationListStr = "{ ";
        if(getIfObservationListSize() == 0) {
            ifObservationListStr = "EMPTY";
        }
        else {
            for (ObservationItem o : getIfObservationList()) {
                ifObservationListStr += "\n[ " + o.toString() + " ]";
            }
        }
        ifObservationListStr += " }";

        String ifPredictionListStr = "{ ";
        if(getIfPredictionListSize() == 0) {
            ifPredictionListStr = "EMPTY";
        }
        else {
            for (PredictionItem p : getIfPredictionList()) {
                ifPredictionListStr += "\n[ " + p.toString() + " ]";
            }
        }
        ifPredictionListStr += " }";

        String ifConfiguredBandwidthListStr = "{ ";
        if(getIfConfiguredBandwidthListSize() == 0) {
            ifConfiguredBandwidthListStr = "EMPTY";
        }
        else {
            for (ConfiguredBandwidthItem c : getIfConfiguredBandwidthList()) {
                ifConfiguredBandwidthListStr += "\n[ " + c.toString() + " ]";
            }
        }
        ifConfiguredBandwidthListStr += " }";

        return "InterfaceItem{" +
                super.toString() +
                "\nnetworkRef='" + networkRef + '\'' +
                ",\nifRef='" + ifRef + '\'' +
                ",\ntimeInterval=" + timeInterval +
                ",\nifMonitoredState=" + ifMonitoredState +
                ",\nifMaximumCapacity=" + ifMaximumCapacity +
                ",\nifCurrentCapacity=" + ifCurrentCapacity +
                ",\nifTrafficBandwidth=" + ifTrafficBandwidth +
                ",\nifPredictedTrafficBandwidth=" + ifPredictedTrafficBandwidth +
                ",\nifObservationListSize=" + ifObservationListSize +
                ",\nifObservationList: " + ifObservationListStr +
                ",\nifPredictionListSize=" + ifPredictionListSize +
                ",\nifPredictionList: " + ifPredictionListStr +
                ",\nifConfiguredBandwidthListSize=" + ifConfiguredBandwidthListSize +
                ",\nifPredictionList: " + ifConfiguredBandwidthListStr +
                ",\nifSaving=" + ifSaving +
                '}';
    }
}
