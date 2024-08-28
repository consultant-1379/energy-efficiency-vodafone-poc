package com.ericsson.vodafone.poc.predictor.api.impl;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.vodafone.poc.predictor.api.Predictor;
import com.ericsson.vodafone.poc.predictor.api.exception.PredictionUnavailableException;
import com.ericsson.vodafone.poc.predictor.api.exception.SerieOperationException;
import com.ericsson.vodafone.poc.predictor.api.utils.*;
import com.ericsson.vodafone.poc.predictor.kpi.management.KpiSerie;
import com.ericsson.vodafone.poc.predictor.kpi.predictor.KpiPredictor;

public class AnomalyDetectionAndPredictionImpl implements Predictor {
    private static Logger logger = LoggerFactory.getLogger(AnomalyDetectionAndPredictionImpl.class);

    private final double anomalyThreshold = 4.0; //This parameter can be fixed independently by the input: the internal logic must consider the reliability measure of the pattern
    // Was private final double anomalyThreshold = 3.5;
    private final double safetyThreshold = 3.0;
    // Was private final double safetyThreshold = 2.0;
    private int historicalImportance = 0;
    private int derivativeDependency = 0;
    private double patternThreshold = 0.07;

    public double getAnomalyThreshold() {
        return anomalyThreshold;
    }

    public int getHistoricalImportance() {
        return historicalImportance;
    }

    public void setHistoricalImportance(final int historicalImportance) {
        this.historicalImportance = historicalImportance;
    }

    public int getDerivativeDependency() {
        return derivativeDependency;
    }

    public void setDerivativeDependency(final int derivativeDependency) {
        this.derivativeDependency = derivativeDependency;
    }

    public double getPatternThreshold() {
        return patternThreshold;
    }

    public void setPatternThreshold(final double patternThreshold) {
        this.patternThreshold = patternThreshold;
    }

    @Override
    public Serie<Prediction> predict(final Serie history) throws PredictionUnavailableException {

        final Double[] historyArray = (Double[]) history.getSerie().toArray(new Double[0]);

        final KpiPredictor predictor = new KpiPredictor(generateKpiSerie(historyArray), history.getPeriodicity(), historicalImportance,
                derivativeDependency, anomalyThreshold, safetyThreshold, patternThreshold);

        final List<Double> prediction = predictor.getNextValues();
        final List<Double> lowerThreshold = predictor.getLowerThreshold();
        final List<Double> upperThreshold = predictor.getUpperThreshold();
        final List<Double> lowerSafetyThreshold = predictor.getLowerSafetyThreshold();
        final List<Double> upperSafetyThreshold = predictor.getUpperSafetyThreshold();

        if (prediction == null || lowerThreshold == null || upperThreshold == null || lowerSafetyThreshold == null || upperSafetyThreshold == null) {
            throw new PredictionUnavailableException("Prediction not available - null");
        }

        if ((prediction.size() == lowerThreshold.size()) && (prediction.size() == upperThreshold.size())
        /* liuba && (prediction.size() == lowerSafetyThreshold.size()) && (prediction.size() == upperSafetyThreshold.size()) */) {

            final List<Prediction> predictionList = new ArrayList<>();
            final List<Prediction> safePredictionList = new ArrayList<>();
            for (int i = 0; i < prediction.size(); i++) {

                final Prediction<Double> curr = new Prediction<>((prediction.get(i) < 0.0) ? 0 : prediction.get(i),
                        (lowerThreshold.get(i) < 0.0) ? 0 : lowerThreshold.get(i), (upperThreshold.get(i) < 0.0) ? 0 : upperThreshold.get(i),
                        (lowerSafetyThreshold.get(i) < 0.0) ? 0 : lowerSafetyThreshold.get(i),
                        (upperSafetyThreshold.get(i) < 0.0) ? 0 : upperSafetyThreshold.get(i));
                predictionList.add(curr);
            }

            Serie<Prediction> predictionSerie = null;
            try {
                predictionSerie = new Serie<Prediction>(1, predictionList.size(), predictionList, calculatePredictionLastSampleDate(history),
                        history.getSamplingIntervalSeconds());
            } catch (final SerieOperationException e) {
                e.printStackTrace();
                throw new PredictionUnavailableException("Prediction not available - predictionSerie not inizialized");
            }
            return predictionSerie;
        } else {
            throw new PredictionUnavailableException("Prediction not available - different size");
        }
    }

    private Date calculatePredictionLastSampleDate(final Serie history) {
        final long lastSampleDate = history.getLastSampleDate().getTime();
        final long encoreMills = history.getEncoreDurationInSeconds() * 1000;
        final long sum = lastSampleDate + encoreMills;
        return new Date(sum);
    }

    private KpiSerie<Double> generateKpiSerie(final Double[] historicalData) {
        final KpiSerie<Double> historicalKPIValues = new KpiSerie<Double>();

        for (int i = 0; i < historicalData.length; i++) {
            historicalKPIValues.add(new AbstractMap.SimpleEntry<Integer, Double>(i, (double) historicalData[i % historicalData.length]));
        }

        return historicalKPIValues;
    }

    public static void main(final String[] args) throws FileNotFoundException, SerieOperationException {
        String historyFileName = null;
        String predictionFileName = null;

        final AnomalyDetectionAndPredictionImpl myPredictor = new AnomalyDetectionAndPredictionImpl();

        if (args[0] != null) {
            historyFileName = args[0];
            AnomalyDetectionAndPredictionImpl.logger.info("prepareJobData: history fileName: {}", historyFileName);
        } else {
            AnomalyDetectionAndPredictionImpl.logger.error("Wrong history filename");
            return;
        }

        final String workingDir = System.getProperty("user.dir");
        predictionFileName = workingDir + "/logs/prediction.txt";
        final PrintStream predictionOutputStream = new PrintStream(predictionFileName);

        final TestHistoryReader reader = new TestCsvHistoryReader(historyFileName, 3, new Date(), 30);
        final Serie<Integer> startupHistory = reader.loadHistory();

        AnomalyDetectionAndPredictionImpl.logger.debug("prepareJobData:loadHistory completed");
        predictionOutputStream.println("prepareJobData:loadHistory completed");

        Serie<Prediction> testPrediction = null;
        try {
            testPrediction = myPredictor.predict(startupHistory);
            System.out.println("sizeof(startupHistory) = " + startupHistory.getSize() + "\n");
            System.out.println("sizeof(Prediction) = " + testPrediction.getSize() + "\n");
        } catch (final PredictionUnavailableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        final List<Prediction> predictionList = testPrediction.getSerie();

        int i = 0;
        for (final Prediction predictionElem : predictionList) {
            //            System.out.println("\nPrediction Element N. " + i + "\n");
            //            System.out.println("LowerThreshold = " + predictionElem.getLowerThreshold() + ", ");
            //            System.out.println("SafeLowerThreshold = " + predictionElem.getSafeLowerThreshold() + ", ");
            //            System.out.println("PredictedValue = " + predictionElem.getPredictedValue() + ", ");
            //            System.out.println("SafeUpperThreshold = " + predictionElem.getSafeUpperThreshold() + ", ");
            //            System.out.println("UpperThreshold = " + predictionElem.getUpperThreshold() + ", ");

            predictionOutputStream.println("\nPrediction Element N. " + i + "\n");
            predictionOutputStream.println("LowerThreshold = " + predictionElem.getLowerThreshold() + ", SafeLowerThreshold = "
                    + predictionElem.getSafeLowerThreshold() + ", PredictedValue = " + predictionElem.getPredictedValue() + ", SafeUpperThreshold = "
                    + predictionElem.getSafeUpperThreshold() + ", UpperThreshold = " + predictionElem.getUpperThreshold() + ", ");
            i++;
        }
    }

}
