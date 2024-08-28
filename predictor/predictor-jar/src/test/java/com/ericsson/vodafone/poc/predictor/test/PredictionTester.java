package com.ericsson.vodafone.poc.predictor.test;

import java.util.*;

import javax.swing.SwingUtilities;

import com.ericsson.vodafone.poc.predictor.kpi.management.KpiSerie;
import com.ericsson.vodafone.poc.predictor.kpi.predictor.KpiPredictor;

public class PredictionTester {
    static private final double anomalyThreshold = 3.5; //This parameter can be fixed independently by the input: the internal logic must consider the reliability measure of the pattern
    static private final double safetyThreshold = 2.0;
    static private final int randomicity = 10;
    static private int historicalImportance = 0;
    static private int derivativeDependency = 0;
    static private double patternThreshold = 0.07;
    static final Integer rawValues[] = { 30, 29, 28, 27, 26, 25, 24, 23, 22, 21, 20, 19, 18, 18, 17, 17, 16, 16, 16, 15, 15, 15, 16, 16, 16, 17, 17,
            17, 18, 18, 18, 19, 19, 20, 20, 21, 21, 22, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 40, 42, 44, 46, 48, 50,
            52, 54, 56, 58, 60, 62, 64, 66, 68, 70, 72, 73, 75, 76, 78, 79, 80, 82, 83, 84, 85, 86, 87, 88, 89, 89, 90, 90, 91, 91, 92, 92, 92, 93,
            93, 93, 94, 94, 94, 93, 92, 92, 91, 90, 88, 86, 84, 83, 82, 82, 82, 83, 84, 85, 87, 89, 91, 92, 93, 94, 95, 96, 96, 96, 96, 96, 95, 95,
            94, 93, 92, 91, 90, 89, 88, 87, 86, 85, 84, 83, 82, 81, 80, 79, 78, 77, 76, 75, 74, 73, 72, 70, 69, 67, 66, 64, 63, 61, 59, 57, 55, 53,
            51, 49, 47, 45, 43, 41, 40, 38, 37, 36, 34, 33, 32, 31 };

    public static void main(final String[] args) {
        final List<Integer> completeSequence = new ArrayList<Integer>();

        //Generate KPI emulted values
        final KpiSerie<Double> historicalKPIValues = new KpiSerie<Double>();
        generateKpiSerie(rawValues, randomicity, historicalKPIValues, completeSequence);

        final KpiPredictor predictor = new KpiPredictor(historicalKPIValues, rawValues.length, historicalImportance, derivativeDependency,
                anomalyThreshold, safetyThreshold, patternThreshold);

        if (predictor.getNextValues() != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    final List<List<Integer>> tmp = new ArrayList<List<Integer>>();
                    List<Integer> pattern = new ArrayList<Integer>();
                    pattern.addAll(completeSequence);
                    for (int i = 0; i < predictor.getNextValues().size(); i++) {
                        pattern.add(predictor.getNextValues().get(i).intValue());
                    }
                    tmp.add(pattern);

                    pattern = new ArrayList<Integer>();
                    pattern.addAll(completeSequence);
                    for (int i = 0; i < predictor.getUpperThreshold().size(); i++) {
                        pattern.add(predictor.getUpperThreshold().get(i).intValue());
                    }
                    tmp.add(pattern);

                    pattern = new ArrayList<Integer>();
                    pattern.addAll(completeSequence);
                    for (int i = 0; i < predictor.getLowerThreshold().size(); i++) {
                        pattern.add(predictor.getLowerThreshold().get(i).intValue());
                    }
                    tmp.add(pattern);

                    /** liuba */
                    pattern = new ArrayList<Integer>();
                    pattern.addAll(completeSequence);
                    for (int i = 0; i < predictor.getLowerSafetyThreshold().size(); i++) {
                        pattern.add(predictor.getLowerSafetyThreshold().get(i).intValue());
                    }
                    tmp.add(pattern);

                    pattern = new ArrayList<Integer>();
                    pattern.addAll(completeSequence);
                    for (int i = 0; i < predictor.getUpperSafetyThreshold().size(); i++) {
                        pattern.add(predictor.getUpperSafetyThreshold().get(i).intValue());
                    }
                    tmp.add(pattern);
                    /* liuba **/

                    com.ericsson.vodafone.poc.predictor.test.utils.DrawGraph.createAndShowGui(tmp, "Original Sequence + prediction");
                }
            });

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    final List<List<Integer>> tmp = new ArrayList<List<Integer>>();
                    List<Integer> pattern = new ArrayList<Integer>();
                    for (int i = 0; i < predictor.getNextValues().size(); i++) {
                        pattern.add(predictor.getNextValues().get(i).intValue());
                    }
                    tmp.add(pattern);

                    pattern = new ArrayList<Integer>();
                    for (int i = 0; i < predictor.getUpperThreshold().size(); i++) {
                        pattern.add(predictor.getUpperThreshold().get(i).intValue());
                    }
                    tmp.add(pattern);

                    pattern = new ArrayList<Integer>();
                    for (int i = 0; i < predictor.getLowerThreshold().size(); i++) {
                        pattern.add(predictor.getLowerThreshold().get(i).intValue());
                    }
                    tmp.add(pattern);

                    com.ericsson.vodafone.poc.predictor.test.utils.DrawGraph.createAndShowGui(tmp, "prediction + thresholds");
                }
            });
        }

    }

    //Add noise to the basic series and repeat it for 3 times (3 repetitions) + add anomalies
    private static void generateKpiSerie(final Integer[] rawValues, final int randomicity, final KpiSerie<Double> historicalKPIValues,
                                         final List<Integer> completeSequence) {
        final Random rand = new Random();
        int anomalia = rand.nextInt(rawValues.length * 3);
        System.out.println("Amonaly inserted in sample " + anomalia);
        for (int i = 0; i < rawValues.length * 3; i++) {
            int noise = rand.nextInt(randomicity);
            if (i == anomalia) {
                noise = randomicity * 7;
                anomalia = rand.nextInt(rawValues.length * 3);
            }
            final Integer noisedValue = rawValues[i % rawValues.length] + noise + (i / 10);
            historicalKPIValues.add(new AbstractMap.SimpleEntry<Integer, Double>(i, (double) noisedValue));
            completeSequence.add(noisedValue);
        }
    }

    private static List<Integer> adaptPattern(final List<Double> serie) {
        final List<Integer> convertita = new ArrayList<Integer>();
        for (int i = 0; i < serie.size(); i++) {
            convertita.add((int) (50 * serie.get(i) + 50));
        }
        return convertita;
    }

}
