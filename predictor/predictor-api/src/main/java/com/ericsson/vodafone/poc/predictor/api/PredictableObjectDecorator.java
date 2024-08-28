package com.ericsson.vodafone.poc.predictor.api;

import com.ericsson.vodafone.poc.predictor.api.exception.PredictionUnavailableException;
import com.ericsson.vodafone.poc.predictor.api.exception.SerieOperationException;
import com.ericsson.vodafone.poc.predictor.api.utils.Serie;
import com.ericsson.vodafone.poc.predictor.api.utils.Prediction;

import java.util.List;

public abstract class PredictableObjectDecorator<T> implements Predictable<T> {

    protected PredictableObject predictableObject;

    public PredictableObjectDecorator(final PredictableObject predictableObject) {
        this.predictableObject = predictableObject;
    }

    public PredictableObjectDecorator(final String id, final Predictor predictor, final Serie history) {
        this.predictableObject = new PredictableObject(id, predictor, history);
    }

    @Override
    public String getId() {
        return predictableObject.getId();
    }

    @Override
    public Serie<Prediction> getPrediction() throws PredictionUnavailableException {
        return predictableObject.getPrediction();
    }

    @Override
    public void updateHistory(Serie s) throws SerieOperationException {
        predictableObject.updateHistory(s);
    }

    @Override
    public void pushToHistory(T t) throws SerieOperationException {
        predictableObject.pushToHistory(t);
    }

}
