package com.ericsson.vodafone.poc.predictor.api.utils;

import java.io.FileNotFoundException;

import com.ericsson.vodafone.poc.predictor.api.exception.SerieOperationException;

public interface TestHistoryReader {

    Serie loadHistory() throws FileNotFoundException, SerieOperationException;
    public Triplet<Double, Double, Double> getHistorySamples(Integer historyIndex) throws FileNotFoundException, SerieOperationException;
    }
