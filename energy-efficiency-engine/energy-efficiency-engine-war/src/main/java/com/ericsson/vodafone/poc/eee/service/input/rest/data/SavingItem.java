package com.ericsson.vodafone.poc.eee.service.input.rest.data;

import javax.xml.bind.annotation.XmlElement;

public class SavingItem extends BaseItem {

    @XmlElement
    private Double currentSavingPercentage;

    @XmlElement
    private Double historicalSavingPercentage;

    @XmlElement
    //value 1 = 24h (the only managed value at the moment)
    private Integer historicalSavingPeriod = 1;

    public Double getCurrentSavingPercentage() {
        return currentSavingPercentage;
    }

    public void setCurrentSavingPercentage(Double currentSavingPercentage) {
        this.currentSavingPercentage = currentSavingPercentage;
    }

    public Double getHistoricalSavingPercentage() {
        return historicalSavingPercentage;
    }

    public void setHistoricalSavingPercentage(Double historicalSavingPercentage) {
        this.historicalSavingPercentage = historicalSavingPercentage;
    }

    public Integer getHistoricalSavingPeriod() {
        return historicalSavingPeriod;
    }

    public void setHistoricalSavingPeriod(Integer historicalSavingPeriod) {
        this.historicalSavingPeriod = historicalSavingPeriod;
    }

    @Override
    public String toString() {
        return "SavingItem {" +
                super.toString() +
                "currentSavingPercentage='" + currentSavingPercentage +
                ", historicalSavingPercentage='" + historicalSavingPercentage +
                ", historicalSavingPeriod=" + historicalSavingPeriod +
                '}';
    }
}
