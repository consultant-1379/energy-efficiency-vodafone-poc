package com.ericsson.vodafone.poc.eee.services.input.rest.resources;

import com.ericsson.vodafone.poc.eee.services.input.rest.resources.utils.GetInterfaceMode;

import java.io.Serializable;

public class GetInterfaceRequest implements Serializable {

    private static final long serialVersionUID = 6L;

    private String networkRef;
    private String ifRef;
    private GetInterfaceMode mode;
    private Long date;

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

    public GetInterfaceMode getMode() {
        return mode;
    }

    public void setMode(GetInterfaceMode mode) {
        this.mode = mode;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    @Override
    public String toString() {
                final StringBuilder builder = new StringBuilder();
                builder.append("GetInterfaceRequest [networkRef=").append(networkRef).append(", ifRef=").append(ifRef).append(", mode=")
                        .append(mode.toString()).append(", date=").append(date).append("]");
                return builder.toString();
            }
}