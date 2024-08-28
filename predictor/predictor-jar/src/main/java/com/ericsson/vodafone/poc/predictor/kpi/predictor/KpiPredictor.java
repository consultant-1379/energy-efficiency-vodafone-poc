package com.ericsson.vodafone.poc.predictor.kpi.predictor;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.vodafone.poc.predictor.kpi.management.KpiSerie;
import com.ericsson.vodafone.poc.predictor.pattern.detection.PatternDetector;

/**
 * @author EGIUBUR
 *
 */
public class KpiPredictor {
    private PatternDetector detector = null;
    private double patternThreshold = 0.07;
    private double anomalyThreshold = 4.5;
    private double safetyThreshold = 2.0;
    private List<Double> nextValues = null;
    private List<Double> upperThreshold = null;
    private List<Double> lowerThreshold = null;
    private List<Double> upperSafetyThreshold = null;
    private List<Double> lowerSafetyThreshold = null;
    private int numOfMaxRefining = 5;
    private static final int filterWindow = 7; //to smooth the noise in the variance

    public List<Double> getUpperThreshold() {
        return upperThreshold;
    }

    public List<Double> getLowerThreshold() {
        return lowerThreshold;
    }

    public List<Double> getUpperSafetyThreshold() {
        return upperSafetyThreshold;
    }

    public List<Double> getLowerSafetyThreshold() {
        return lowerSafetyThreshold;
    }

    public PatternDetector getDetector() {
        return detector;
    }

    public List<Double> getNextValues() {
        return nextValues;
    }

    /**
     * @param historicalKpiData
     *            : is the list of KPI values (and timestamp) for the historical time slot of observation (minimum 3 * periodicity values)
     * @param periodicity
     *            : is the number of samples in a period (in a day or in a week, usually)
     * @param historicalImportance
     *            (default 0): is the weight [0...+1] to give less/more importance to the historical data age (weight = 1 + alpha * time): if a KPI is
     *            very old, it can be considered less reliable in the pattern computation (historicalImportance higher) if an old KPI should be
     *            considered reliable as a new one, historicalImportance should be set to 0
     * @param derivativeDependency
     *            (default 0): is the "C" coefficient [0...+inf) to introduce the standard deviation dependency on the KPI derivative (see formula 7).
     *            If you increase this param, the derivative is more important
     * @param anomalyThreshold
     *            (default 4.5): is the coefficient (0..+inf) to classify as anomaly a divergent historical KPI value. If you increase this param,
     *            less anomalies are detected
     * @param patternThreshold
     *            (default 0.07): it's the pattern affinity threshold to promote a KPI as predictable (usually, from heuristic tests, it seems 0.07 is
     *            a good value)
     */
    public KpiPredictor(final KpiSerie<Double> historicalKpiData, final int periodicity, final double historicalImportance,
                        final double derivativeDependency, final double anomalyThreshold, final double safetyThreshold,
                        final double patternThreshold) {
        super();

        //Allocate the pattern detector that computes and evaluates the pattern
        this.detector = new PatternDetector(historicalKpiData, periodicity, historicalImportance, derivativeDependency, anomalyThreshold);
        this.patternThreshold = patternThreshold;
        this.anomalyThreshold = anomalyThreshold;
        this.safetyThreshold = safetyThreshold;

        //If the pattern reliability of this series is acceptable, continue the analysis
        if (this.detector.getReliability() < this.patternThreshold) {
            System.out.println("No repetitiviness found");
            return;
        }

        //check for anomalies in the historical values, remove them and refine the pattern accordingly (here there's the risk of infinite refining steps, present in some of corner cases)
        //Note: In order to avoid infinite refining steps, you can check if the refined pattern reliability is increasing or not.
        while (numOfMaxRefining-- >= 0
                && this.detector.refinePatternForAnomalies(periodicity, historicalImportance, derivativeDependency, anomalyThreshold)) {
            ;
        }
        {
            System.out.println("Reliability :" + this.detector.getReliability());
        }

        computeNextSeries();
    }

    /**
     * @param currentValues
     *            : this is the list of current values at t0, t1, t2, etc. Only the latest 3 samples are used to compute the quadratic interpolation.
     * @return the expected value for next time (quadratic interpolation)
     */
    private double computeNextValue(final List<Double> currentValues) {
        final int size = currentValues.size();
        final double newValue = currentValues.get(size - 3) - 3 * currentValues.get(size - 2) + 3 * currentValues.get(size - 1);
        System.out.println(
                "da " + currentValues.get(size - 3) + "-" + currentValues.get(size - 2) + "-" + currentValues.get(size - 1) + " ottengo " + newValue);
        return newValue;

    }

    private void computeNextSeries() {
        this.nextValues = new ArrayList<Double>();
        this.upperThreshold = new ArrayList<Double>();
        this.lowerThreshold = new ArrayList<Double>();
        this.upperSafetyThreshold = new ArrayList<Double>();
        this.lowerSafetyThreshold = new ArrayList<Double>();

        //compute the next average and gain values
        final double nextAverage = this.computeNextValue(this.detector.getAverageValues());
        final double nextGain = this.computeNextValue(this.detector.getGainValues());

        //filter for delta: the expected variation is the sum of the neighbors (not just the point variation)
        double basicDelta = 0.0;
        for (int i = 0; i < filterWindow; i++) {
            basicDelta += this.detector.getNormDeviation().get(i);
        }
        for (int i = 0; i < this.detector.getPattern().size(); i++) {
            final double nextValue = this.detector.getPattern().get(i) * nextGain + nextAverage;
            double delta = 0.0;
            double safetyDelta = 0.0;
            if ((i <= (filterWindow / 2)) || (i >= (this.detector.getPattern().size() - (filterWindow / 2)))) {
                delta = basicDelta * nextGain * this.anomalyThreshold;
                safetyDelta = basicDelta * nextGain * this.safetyThreshold;
            } else {
                basicDelta = basicDelta - this.detector.getNormDeviation().get(i - (filterWindow / 2))
                        + this.detector.getNormDeviation().get(i + (filterWindow / 2));
                delta = basicDelta * nextGain * this.anomalyThreshold;
                safetyDelta = basicDelta * nextGain * this.safetyThreshold;
            }

            this.nextValues.add(nextValue);
            this.upperThreshold.add(nextValue + delta); //TODO check why the delta is so small... bugs in standard deviation computation?
            this.lowerThreshold.add(nextValue - delta);
            this.upperSafetyThreshold.add(nextValue + safetyDelta);
            this.lowerSafetyThreshold.add(nextValue - safetyDelta);
        }
    }
}
