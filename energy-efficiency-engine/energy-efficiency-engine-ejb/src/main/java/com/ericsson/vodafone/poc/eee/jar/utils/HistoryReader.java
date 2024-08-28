package com.ericsson.vodafone.poc.eee.jar.utils;

import java.io.FileNotFoundException;

import com.ericsson.vodafone.poc.predictor.api.exception.SerieOperationException;
import com.ericsson.vodafone.poc.predictor.api.utils.Serie;

public interface HistoryReader {

    Serie loadHistory() throws FileNotFoundException, SerieOperationException;
}
