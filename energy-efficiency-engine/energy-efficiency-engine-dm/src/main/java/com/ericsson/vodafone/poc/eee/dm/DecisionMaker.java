
package com.ericsson.vodafone.poc.eee.dm;

import com.ericsson.vodafone.poc.eee.jar.utils.Pair;
import com.ericsson.vodafone.poc.predictor.api.utils.Prediction;
import com.ericsson.vodafone.poc.predictor.api.utils.Serie;

public abstract class DecisionMaker <S, T> {

    public abstract Decision makeDecision(final Serie<S> monitoredValues,final Serie<Prediction<T>> predictionList,final Pair<Integer, Integer> alignment,String ifRef,String networkRef);
}
