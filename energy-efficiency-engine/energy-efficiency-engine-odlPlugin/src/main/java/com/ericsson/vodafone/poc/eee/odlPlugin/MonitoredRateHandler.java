package com.ericsson.vodafone.poc.eee.odlPlugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.vodafone.poc.eee.odlPlugin.utils.JsonHandler;

/**
 * Created by esimalb on 8/28/17.
 */
public class MonitoredRateHandler {
    private Long monitoredRate;
    private int timeInterval;
    private Long bandwidthCapacity;
    private String timeStamp;
    private Long nominalOutputPower;
    private Long currentOutputPower;

    private static final Logger logger = LoggerFactory.getLogger(JsonHandler.class);

    public MonitoredRateHandler() {
        monitoredRate = 0L;
        timeInterval = 0;
        bandwidthCapacity = 0L;
        timeStamp = "";
        nominalOutputPower = 0L;
        currentOutputPower = 0L;

        logger.debug("monitoredRate: { } - timeInterval: { } - bwCapacity: {} - timeStamp: {}, nominalOutputPower: {}, currentOutputPower: {}",
                monitoredRate, timeInterval, bandwidthCapacity, timeStamp, nominalOutputPower, currentOutputPower);
    }

    /**
     * Gets bandwidth capacity.
     *
     * @return the bandwidth capacity
     */
    public Long getBandwidthCapacity() {
        return bandwidthCapacity;
    }

    /**
     * Sets bandwidth capacity.
     *
     * @param bandwidthCapacity the bandwidth capacity
     */
    public void setBandwidthCapacity(Long bandwidthCapacity) {
        this.bandwidthCapacity = bandwidthCapacity;
    }

    /**
     * Gets time stamp.
     *
     * @return the time stamp
     */
    public String getTimeStamp() {
        return timeStamp;
    }

    /**
     * Sets time stamp.
     *
     * @param timeStamp the time stamp
     */
    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    /**
     * Gets monitored rate.
     *
     * @return the monitored rate
     */
    public Long getMonitoredRate() {
        return monitoredRate;
    }

    /**
     * Sets monitored rate.
     *
     * @param monitoredRate the monitored rate
     */
    public void setMonitoredRate(final Long monitoredRate) {
        this.monitoredRate = monitoredRate;
    }

    /**
     * Gets time interval.
     *
     * @return the time interval
     */
    public int getTimeInterval() {
        return timeInterval;
    }

    /**
     * Sets time interval.
     *
     * @param timeInterval the time interval
     */
    public void setTimeInterval(final int timeInterval) {
        this.timeInterval = timeInterval;
    }

    /**
     * Gets nominal output power.
     *
     * @return nominal output power
     */
    public Long getNominalOutputPower() {
        return nominalOutputPower;
    }

    /**
     * Sets nominal output power.
     *
     * @param nominalOutputPower current output power
     */
    public void setNominalOutputPower(Long nominalOutputPower) {
        this.nominalOutputPower = nominalOutputPower;
    }

    /**
     * Gets current output power.
     *
     * @return current output power
     */
    public Long getCurrentOutputPower() {
        return currentOutputPower;
    }

    /**
     * Sets current output power.
     *
     * @param currentOutputPower current output power
     */
    public void setCurrentOutputPower(Long currentOutputPower) {
        this.currentOutputPower = currentOutputPower;
    }
}
