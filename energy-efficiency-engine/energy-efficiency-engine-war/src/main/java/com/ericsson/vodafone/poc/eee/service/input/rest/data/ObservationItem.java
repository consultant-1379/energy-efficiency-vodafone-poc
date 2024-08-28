package com.ericsson.vodafone.poc.eee.service.input.rest.data;


import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

public class ObservationItem extends BaseItem {

    @XmlElement
    private Long ifTrafficBandwidth;

    public Long getIfTrafficBandwidth() {
        return ifTrafficBandwidth;
    }

    public void setIfTrafficBandwidth(Long ifTrafficBandwidth) {
        this.ifTrafficBandwidth = ifTrafficBandwidth;
    }

    @Override
    public String toString() {
        return "ObservationItem {" +
                super.toString() +
                "ifTrafficBandwidth='" + ifTrafficBandwidth +
                '}';
    }
}
