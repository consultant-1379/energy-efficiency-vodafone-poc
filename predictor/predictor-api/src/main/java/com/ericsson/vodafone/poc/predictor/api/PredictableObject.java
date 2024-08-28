package com.ericsson.vodafone.poc.predictor.api;

import com.ericsson.vodafone.poc.predictor.api.exception.PredictionUnavailableException;
import com.ericsson.vodafone.poc.predictor.api.exception.SerieOperationException;
import com.ericsson.vodafone.poc.predictor.api.utils.Serie;
import com.ericsson.vodafone.poc.predictor.api.utils.Prediction;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class PredictableObject<T> implements Predictable<T> {

    private String id;
    private Predictor predictor;
    private Serie<T> history;

    public PredictableObject(final String id, final Predictor predictor, final Serie<T> history) {
        this.id = id;
        this.predictor = predictor;
        this.history = history;
    }

    @Override
    public String getId() {
        return id;
    }

    public Predictor getPredictor() {
        return predictor;
    }

    public void setPredictor(final Predictor predictor) {
        this.predictor = predictor;
    }

    public Serie getHistory() {
        return history;
    }

    public void setHistory(final Serie history) {
        this.history = history;
    }

    public Serie<Prediction> getPrediction() throws PredictionUnavailableException {
        return predictor.predict(history);
    }

    @Override
    public void updateHistory(Serie s) throws SerieOperationException {
        history.push(s);
    }

    @Override
    public void pushToHistory(T t) throws SerieOperationException {
        history.push(t);
    }

}
