package com.ericsson.vodafone.poc.eee.service.input.rest.data;

import javax.xml.bind.annotation.XmlElement;

public class ConfiguredBandwidthItem extends BaseItem {

    @XmlElement
    private Long ifConfiguredBandwidth;

    public Long getIfConfiguredBandwidth() {
        return ifConfiguredBandwidth;
    }

    public void setIfConfiguredBandwidth(Long ifConfiguredBandwidth) {
        this.ifConfiguredBandwidth = ifConfiguredBandwidth;
    }

    @Override
    public String toString() {
        return "ConfiguredBandwidthItem {" +
                super.toString() +
                "ifConfiguredBandwidth='" + ifConfiguredBandwidth +
                '}';
    }
}
