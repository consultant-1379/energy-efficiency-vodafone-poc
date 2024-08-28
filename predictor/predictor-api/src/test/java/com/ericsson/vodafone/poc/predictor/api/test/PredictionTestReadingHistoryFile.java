package com.ericsson.vodafone.poc.predictor.api.test;

import static org.junit.Assert.assertEquals;

import java.io.*;
import java.util.*;

import com.ericsson.vodafone.poc.predictor.kpi.management.KpiSerie;
import com.ericsson.vodafone.poc.predictor.kpi.predictor.KpiPredictor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.vodafone.poc.predictor.api.exception.PredictionUnavailableException;
import com.ericsson.vodafone.poc.predictor.api.exception.SerieOperationException;
import com.ericsson.vodafone.poc.predictor.api.impl.AnomalyDetectionAndPredictionImpl;
import com.ericsson.vodafone.poc.predictor.api.impl.DomainAgnosticIncidentPrediction;
import com.ericsson.vodafone.poc.predictor.api.utils.*;

import javax.swing.*;

@RunWith(MockitoJUnitRunner.class)
public class PredictionTestReadingHistoryFile {

    //Source file directory: predictor-api/resources
    final String filesTotest[] = { "POC_3wk_Link1_GG_301007_1430_20x_SD=0.8.txt",
                                   "POC_3wk_Link2_GG_301007_1430_20x_SD=0.8.txt",
                                   "POC_3wk_LinkBonding_GG_301007_1430_20x_SD=0.8.txt"};

    @Test
    public void testPredictionWithValidValues() {
        try {
            final String workingDir = System.getProperty("user.dir");
            String predictionFileName = workingDir + "/logs/prediction.txt";
            PrintStream predictionOutputStream = new PrintStream(predictionFileName);

            System.out.println("\n\n\t *** Start testPredictionWithValidValues.");
            predictionOutputStream.println("\n\n\t *** Start testPredictionWithValidValues.");

            final AnomalyDetectionAndPredictionImpl myPredictor = new AnomalyDetectionAndPredictionImpl();

            for (String historyFileName : filesTotest) {

                System.out.println("\n\n\t\t *** History fileName to test: " + historyFileName);
                predictionOutputStream.println("\n\n\t\t *** History fileName to test: " + historyFileName);

                final TestHistoryReader reader = new TestCsvHistoryReader(historyFileName, 3, new Date(), 30);
                final Serie<Integer> startupHistory = reader.loadHistory();

                System.out.println("\nloadHistory completed");

                Serie<Prediction> testPrediction = null;

                testPrediction = myPredictor.predict(startupHistory);
                System.out.println("sizeof(startupHistory) = " + startupHistory.getSize() + "\n");
                System.out.println("sizeof(Prediction) = " + testPrediction.getSize() + "\n");
                System.out.println("asking prediction...\n");

                final List<Prediction> predictionList = testPrediction.getSerie();
                String output = "\nPrediction: ";

                Integer i = 0;
                for (final Prediction predictionElem : predictionList) {
                    output += "\nSample: " + i + " (" + historyFileName + ") " + predictionElem.toString() + "\n";

                    Triplet<Double, Double, Double> tripletOfHistoryValues = reader.getHistorySamples(i);

                    output += " FirstWeek " + tripletOfHistoryValues.first + "\tSecondWeek " + tripletOfHistoryValues.second + "\tThirdWeek "
                            + tripletOfHistoryValues.third + "\n";
                    i++;

                }
                predictionOutputStream.println(output);
                System.out.println(output);

                final double anomalyThreshold = 4.0; //This parameter can be fixed independently by the input: the internal logic must consider the reliability measure of the pattern
                // Was private final double anomalyThreshold = 3.5;
                final double safetyThreshold = 3.0;
                // Was private final double safetyThreshold = 2.0;
                int historicalImportance = 0;
                int derivativeDependency = 0;
                double patternThreshold = 0.07;

                final Double[] historyArray = (Double[]) startupHistory.getSerie().toArray(new Double[0]);

                final KpiPredictor predictor = new KpiPredictor(generateKpiSerie(historyArray), startupHistory.getPeriodicity(), historicalImportance, derivativeDependency,
                        anomalyThreshold, safetyThreshold, patternThreshold);

                if (predictor.getNextValues() != null) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            final List<List<Integer>> tmp = new ArrayList<List<Integer>>();
                            List<Integer> pattern = new ArrayList<Integer>();
                            for (int i = 0; i < predictor.getNextValues().size(); i++) {
                                pattern.add(predictor.getNextValues().get(i).intValue());
                            }
                            tmp.add(pattern);
                            DrawGraph.createAndShowGui(tmp, "Prediction");
                        }
                    });
                }

                System.out.println("\n\n\t\t *** End Test on History file: " + historyFileName);
                predictionOutputStream.println("\n\n\t\t *** End Test on History file: " + historyFileName);
            }

            System.out.println("\n\n\t *** End testPredictionWithValidValues.");
            predictionOutputStream.println("\n\n\t *** End testPredictionWithValidValues.");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SerieOperationException e) {
            e.printStackTrace();
        } catch (final PredictionUnavailableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    private KpiSerie<Double> generateKpiSerie(final Double[] historicalData) {
        final KpiSerie<Double> historicalKPIValues = new KpiSerie<Double>();

        for (int i = 0; i < historicalData.length; i++) {
            historicalKPIValues.add(new AbstractMap.SimpleEntry<Integer, Double>(i, (double) historicalData[i % historicalData.length]));
        }

        return historicalKPIValues;
    }
}
