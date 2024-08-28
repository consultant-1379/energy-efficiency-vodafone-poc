package com.ericsson.vodafone.poc.predictor.api.impl;

import com.ericsson.vodafone.poc.predictor.api.PredictableObjectDecorator;
import com.ericsson.vodafone.poc.predictor.api.utils.Serie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DomainAgnosticIncidentPrediction<T> extends PredictableObjectDecorator<T> {

    public DomainAgnosticIncidentPrediction(final String id, final Serie<T> history) {
        super(id, new AnomalyDetectionAndPredictionImpl(), history);
    }
}
