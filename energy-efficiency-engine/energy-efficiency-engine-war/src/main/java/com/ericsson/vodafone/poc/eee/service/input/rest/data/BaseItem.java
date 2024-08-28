package com.ericsson.vodafone.poc.eee.service.input.rest.data;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

public class BaseItem implements Serializable, Aggregable {

    @XmlElement
    private Integer index;

    @XmlElement
    private Long time;

    public BaseItem() {
    }

    public BaseItem(Long time) {
        this.time = time;
    }

    public BaseItem(Integer index, Long time) {
        this.index = index;
        this.time = time;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    @Override
    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "BaseItem {" +
                "index='" + index +
                ", time='" + time +
                '}';
    }
}
