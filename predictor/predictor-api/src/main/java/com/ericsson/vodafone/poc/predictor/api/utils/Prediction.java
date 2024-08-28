package com.ericsson.vodafone.poc.predictor.api.utils;

public class Prediction<T> {

    private SafeTriplet<T, T, T, T, T> predictionTriplet;

    public Prediction(final T predictedValue, final T lowerThreshold, final T upperThreshold, final T safeLowerThreshold,
                          final T safeUpperThreshold) {
        predictionTriplet = new SafeTriplet<T, T, T, T, T>(predictedValue, lowerThreshold, upperThreshold, safeLowerThreshold, safeUpperThreshold);
    }

    public T getPredictedValue() {
        return predictionTriplet.first;
    }

    public T getLowerThreshold() {
        return predictionTriplet.second;
    }

    public T getUpperThreshold() {
        return predictionTriplet.third;
    }

    public T getSafeLowerThreshold() {
        return predictionTriplet.fourth;
    }

    public T getSafeUpperThreshold() {
        return predictionTriplet.fifth;
    }

    @Override
    public String toString() {
        return new String(
                "\n[prediction: " + getPredictedValue() + ", \n\tlowerThreshold: " + getLowerThreshold() + ", upperThreshold: " + getUpperThreshold()
                        + ", \n\tsafeLowerThreshold: " + getSafeLowerThreshold() + ", safeUpperThreshold: " + getSafeUpperThreshold() + "]");
    }

}
