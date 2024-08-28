package com.ericsson.vodafone.poc.eee.dm;

import com.ericsson.vodafone.poc.eee.dm.utils.ActionPoint;
import com.ericsson.vodafone.poc.eee.jar.utils.Pair;
import com.ericsson.vodafone.poc.predictor.api.utils.Serie;
import com.ericsson.vodafone.poc.predictor.api.utils.Prediction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;

public class EnergyEfficiencyDecisionMaker extends DecisionMaker<Double,Double> {

    private Logger logger = LoggerFactory.getLogger(EnergyEfficiencyDecisionMaker.class);

    @Override
    public Decision makeDecision(final Serie<Double> monitoredValues,final Serie<Prediction<Double>> predictionList,
                                 final Pair<Integer, Integer> alignment, String ifRef, String networkRef) {

        Double monitoredRateFromODL;
        final Decision doNothing = new Decision(new ArrayList<Action>(Arrays.asList(new Action(ActionPoint.ASAP, CommandToApply.NO_ACTION))));

        try {
            monitoredRateFromODL = monitoredValues.getSerie().get(alignment.first);
            logger.info("EnergyEfficiencyDecisionMaker: \n monitoredRateFromODL {} sample {}",
                    monitoredRateFromODL, alignment.first);

            if((monitoredRateFromODL == null) || (monitoredRateFromODL == 0)) {
                logger.info("EnergyEfficiencyDecisionMaker: Do nothing");
                return doNothing;
            }
        } catch (Exception e) {
            logger.info("EnergyEfficiencyDecisionMaker: Monitored Rate from ODL unavailable, defaulting to NO ACTION");
            e.printStackTrace();

            return doNothing;
        }

        try {
            Double newRateToSet = monitoredRateFromODL;

            if (predictionList != null) {
                Prediction prediction = predictionList.getSerie().get(alignment.second);
                CommandToApply cmd;
                String makeDecisionOutput;

                if (!isValid(prediction)) {
                    cmd = CommandToApply.SET_MAXIMUM_CAPACITY;
                    makeDecisionOutput = "EnergyEfficiencyDecisionMaker: \n\nWARNING - PREDICTION NOT AVAILABLE for interface " + ifRef
                            + " network ref: " + networkRef
                            +"\n cmd to apply:" + cmd.getCmdName();
                }
                else {
                    if (exceedSafeUpperThreshold(monitoredRateFromODL, prediction)) {
                        cmd = CommandToApply.SET_MAXIMUM_CAPACITY;

                        if(!isInRange(monitoredRateFromODL, prediction)) {
                            makeDecisionOutput = "EnergyEfficiencyDecisionMaker: " +
                                    "\n\nmonitored rate OUT OF UpperThreshold\n cmd to apply: "
                                    + cmd.getCmdName();
                        }
                        else {
                            makeDecisionOutput = "EnergyEfficiencyDecisionMaker: " +
                                    "\n\nmonitored rate IN RANGE with prediction but OUT OF SafeUpperThreshold\n cmd to apply: "
                                    + cmd.getCmdName();
                        }
                    } else if (isInRange(monitoredRateFromODL, prediction)) {
                        // If monitored rate is in range with prediction, check next prediction
                        //WARNING: UpperThreshold will be sent
                        newRateToSet = (predictionList.getSerie().get(alignment.second + 1).getUpperThreshold());

                        cmd = CommandToApply.SET_NEW_INTERFACE_RATE;
                        cmd.setValue(newRateToSet.intValue());

                        if(exceedSafeLowerThreshold(monitoredRateFromODL, prediction)) {
                            makeDecisionOutput = "EnergyEfficiencyDecisionMaker: " +
                                    "\n\nmonitored rate IN RANGE with prediction but OUT OF SafeLowerThreshold\n cmd to apply: "
                                    + cmd.getCmdName() + " - newRateToSet " + cmd.getValue();
                        }
                        else {
                            makeDecisionOutput = "EnergyEfficiencyDecisionMaker: " +
                                    "\n\nmonitored rate IN RANGE with prediction\n cmd to apply: "
                                    + cmd.getCmdName() + " - newRateToSet " + cmd.getValue();
                        }

                    } else {
                        //WARNING: SafeLowerThreshold will be sent
                        //newRateToSet = (predictionList.getSerie().get(alignment.second + 1).getSafeLowerThreshold());
                        newRateToSet = (predictionList.getSerie().get(alignment.second + 1).getUpperThreshold());

                        cmd = CommandToApply.SET_NEW_INTERFACE_RATE;
                        cmd.setValue(newRateToSet.intValue());
                        makeDecisionOutput = "EnergyEfficiencyDecisionMaker: " +
                                "\n\nmonitored rate OUT OF LowerThreshold\n cmd to apply: "
                                + cmd.getCmdName() + " - newRateToSet " + cmd.getValue();
                    }
                }

                logger.info(makeDecisionOutput + "\n\tInterface: " + ifRef + " network ref: " + networkRef
                                + "\n\tMonitoredRate {}"
                                + "\n\tPrediction {} - sample {}" + "\n\tLowerThreshold {}\t\tUpperThreshold {} "
                                + "\n\tSafeLowerThreshold {}\t\tSafeUpperThreshold {}\n\n", monitoredRateFromODL, prediction.getPredictedValue(),
                        alignment.second, prediction.getLowerThreshold(), prediction.getUpperThreshold(), prediction.getSafeLowerThreshold(),
                        prediction.getSafeUpperThreshold());

                return new Decision(new ArrayList<>(Arrays.asList(new Action(ActionPoint.ASAP, cmd))));
            }

        } catch (Exception e) {
            logger.info("EnergyEfficiencyDecisionMaker: EXCEPTION CAUGHT durig decision, defaulting to NO ACTION?????");
            e.printStackTrace();

            // Qui probabilmente anzichè non fare niente sarebe meglio settare la banda al massimo
            logger.info("EnergyEfficiencyDecisionMaker: Do nothing?");
            return doNothing;

        }
        return doNothing;
    }

    private boolean isValid(final Prediction<Double> p) {
        if((p == null) ||
           (p.getPredictedValue() < 0) ||
           ((p.getLowerThreshold().compareTo(p.getUpperThreshold()) >= 0))) {
            return false;
        }
        return true;
    }
    
    private boolean isInRange(final Double monitoredValue,final Prediction<Double> prediction) {
        return (!isLowerOutOfBound(monitoredValue, prediction)) &&
                !isUpperOutOfBound(monitoredValue, prediction);
    }

    private boolean isLowerOutOfBound(final Double monitoredValue,final Prediction<Double> prediction) {
        if(prediction != null) {
            int toLowerComparison = monitoredValue.compareTo(prediction.getLowerThreshold());
            return toLowerComparison <= 0;
        }
        return true;
    }

    private boolean isUpperOutOfBound(final Double monitoredValue,final Prediction<Double> prediction) {
        if(prediction != null) {
            int toUpperComparison = monitoredValue.compareTo(prediction.getUpperThreshold());
            return toUpperComparison >= 0;
        }
        return true;
    }

    private boolean exceedSafeUpperThreshold(final Double monitoredValue,final Prediction<Double> prediction) {
        if(prediction != null) {
            int toUpperComparison = monitoredValue.compareTo(prediction.getSafeUpperThreshold());
            return toUpperComparison >= 0;
        }
        return true;
    }

    private boolean exceedSafeLowerThreshold(final Double monitoredValue,final Prediction<Double> prediction) {
        if(prediction != null) {
            int toLowerComparison = monitoredValue.compareTo(prediction.getSafeLowerThreshold());
            return toLowerComparison <= 0;
        }
        return true;
    }
}
