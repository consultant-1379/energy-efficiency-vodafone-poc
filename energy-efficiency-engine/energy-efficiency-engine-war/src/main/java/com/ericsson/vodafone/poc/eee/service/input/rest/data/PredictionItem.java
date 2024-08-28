package com.ericsson.vodafone.poc.eee.service.input.rest.data;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
public class PredictionItem extends BaseItem {

    @XmlElement
    private Long predictedValue;

    @XmlElement
    private Long predictedLowerThreshold;

    @XmlElement
    private Long predictedUpperThreshold;

    public Long getPredictedValue() {
        return predictedValue;
    }

    public void setPredictedValue(Long predictedValue) {
        this.predictedValue = predictedValue;
    }

    public Long getPredictedLowerThreshold() {
        return predictedLowerThreshold;
    }

    public void setPredictedLowerThreshold(Long predictedLowerThreshold) {
        this.predictedLowerThreshold = predictedLowerThreshold;
    }

    public Long getPredictedUpperThreshold() {
        return predictedUpperThreshold;
    }

    public void setPredictedUpperThreshold(Long predictedUpperThreshold) {
        this.predictedUpperThreshold = predictedUpperThreshold;
    }

    @Override
    public String toString() {
        return "PredictionItem {" +
                super.toString() +
                ", predictedValue='" + predictedValue +
                ", predictedLowerThreshold='" + predictedLowerThreshold +
                ", predictedUpperThreshold=" + predictedUpperThreshold +
                '}';
    }
}
