package com.ericsson.vodafone.poc.predictor.api;

import com.ericsson.vodafone.poc.predictor.api.exception.PredictionUnavailableException;
import com.ericsson.vodafone.poc.predictor.api.utils.Serie;
import com.ericsson.vodafone.poc.predictor.api.utils.Prediction;

import java.util.List;

public interface Predictor {
    Serie<Prediction> predict(Serie history) throws PredictionUnavailableException;
}
