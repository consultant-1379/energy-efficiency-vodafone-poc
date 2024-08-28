package com.ericsson.vodafone.poc.eee.dm;

/**
 * Created by esimalb on 9/13/17.
 */
public enum CommandToApply {
    ENABLE_MONITORING("EnableMonitoring"),
    DISABLE_MONITORING("DisableMonitoring"),
    GET_INTERFACE_RATE("GetInterfaceMonitoredRate"),
    SET_NEW_INTERFACE_RATE("SetNewRate"),
    SET_MAXIMUM_CAPACITY("MaximumCapacity"),
    NO_ACTION("NoAction");

    private final String cmdName;
    private int value;
    private String networkRef;
    private String ifRef;

    CommandToApply(String cmdName) {
        this.cmdName = cmdName;
        this.value = 0;
        this.networkRef = "";
        this.ifRef = "";
    }

    public String getCmdName() {
        return cmdName;
    }

    public int getValue() {
        return value;
    }

    public void setValue (int value) {
        this.value = value;
    }

    public String getNetworkRef() {
        return networkRef;
    }

    public void setNetworkRef(String networkRef) {
        this.networkRef = networkRef;
    }

    public String getIfRef() {
        return ifRef;
    }

    public void setIfRef(String ifRef) {
        this.ifRef = ifRef;
    }

    public void setInterfaceData(String networkRef, String ifRef) {
        this.networkRef = networkRef;
        this.ifRef = ifRef;
    }
}

