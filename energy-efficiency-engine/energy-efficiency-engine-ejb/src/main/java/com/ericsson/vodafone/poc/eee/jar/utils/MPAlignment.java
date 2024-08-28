package com.ericsson.vodafone.poc.eee.jar.utils;

import com.ericsson.vodafone.poc.eee.jar.EnergyEfficiencyJobData;
import com.ericsson.vodafone.poc.eee.jar.exception.MonitoredValuesUnavailableException;

public class MPAlignment {
    private Triplet<Integer, Integer, Integer> alignmentCouple;

    public MPAlignment() {
        this.alignmentCouple = new Triplet<>(-1,-1, -1);
    }

    public MPAlignment(final Integer monitoredTimeInterval, final Integer monitoredIndex, final Integer predictedIndex) {
        this.alignmentCouple = new Triplet<>(monitoredTimeInterval,monitoredIndex,predictedIndex);
    }

    public Integer getMonitoredTimeInterval() {
        return alignmentCouple.first;
    }

    public Integer getMonitoredIndex() {
        return alignmentCouple.second;
    }

    public Integer getPredictedIndex() {
        return alignmentCouple.third;
    }

    public void setMisaligned() {
        this.alignmentCouple = new Triplet<>(-1,-1, -1);
    }

    public boolean isAligned() {
        if((alignmentCouple == null) ||
           (alignmentCouple.first < 0) ||
           (alignmentCouple.second < 0) ||
           (alignmentCouple.third < 0))
            return false;
        else
            return true;
    }

    public void incrementAlignment(final EnergyEfficiencyJobData eejd) {

        Integer newMonitoredTimeInterval = getMonitoredTimeInterval()+1;
        Integer newMonitoredIndex;
        Integer newPredictedIndex;
        try {
            newMonitoredIndex = getMonitoredIndex() < eejd.getMonitoredValues().getSize() ? getMonitoredIndex()+1 : getMonitoredIndex();
            newPredictedIndex = getPredictedIndex() < eejd.getPrediction().getSize() ? getPredictedIndex()+1 : 0;
        } catch (Exception e) {
            e.printStackTrace();
            setMisaligned();
            return;
        }



        this.alignmentCouple = new Triplet<>(newMonitoredTimeInterval,newMonitoredIndex, newPredictedIndex);
    }

    @Override
    public String toString() {
        return new String("[monitoredTimeInterval: " + getMonitoredTimeInterval() +
                                 ", monitoredIndex: " + getMonitoredIndex() +
                                 ", predictedIndex: " + getPredictedIndex()  +
                                 "]");
    }
}


