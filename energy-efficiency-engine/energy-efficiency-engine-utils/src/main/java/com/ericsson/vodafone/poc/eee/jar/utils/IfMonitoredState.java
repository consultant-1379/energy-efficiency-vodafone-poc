package com.ericsson.vodafone.poc.eee.jar.utils;

/**
 * Created by esimalb on 10/11/17.
 */
public enum IfMonitoredState {
    UNAVAILABLE("DataUnavailable"), //Error occurs during enable or retriving data from ODL
    DISABLE("MonitoringDisabled"), //DefaultValue
    ENABLE("MonitoringEnabled"), //Monitoring on ODL enabled
    ENABLE_NOT_MONITORED("EnableNotMoniotred"), //Error occurs during monitoring EEE start
    WAITING_FOR_DATA("WaitingForData"), //Error occurs during monitoring EEE start
    LEARNING("Learning"), //Prediction not available - Monitoring on EEE enabled
    MONITORING("Moniotring"); //Monitoring on EEE enabled


    private final String stateName;

    IfMonitoredState(String stateName) {
        this.stateName = stateName;
    }
}
