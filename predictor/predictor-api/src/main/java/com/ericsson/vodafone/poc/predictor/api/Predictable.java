package com.ericsson.vodafone.poc.predictor.api;

import com.ericsson.vodafone.poc.predictor.api.exception.PredictionUnavailableException;
import com.ericsson.vodafone.poc.predictor.api.exception.SerieOperationException;
import com.ericsson.vodafone.poc.predictor.api.utils.Prediction;
import com.ericsson.vodafone.poc.predictor.api.utils.Serie;

import java.util.Collection;
import java.util.List;

/**
 * Created by esimalb on 7/20/17.
 */
public interface Predictable<T> {

    String getId();
    Serie<Prediction> getPrediction() throws PredictionUnavailableException;
    void updateHistory(Serie<T> s) throws SerieOperationException;
    void pushToHistory(T t) throws SerieOperationException;
}