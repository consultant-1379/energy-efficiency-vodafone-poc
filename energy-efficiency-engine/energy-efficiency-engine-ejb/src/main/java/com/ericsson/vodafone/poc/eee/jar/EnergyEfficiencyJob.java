package com.ericsson.vodafone.poc.eee.jar;

import com.ericsson.vodafone.poc.eee.dm.Action;
import com.ericsson.vodafone.poc.eee.dm.CommandToApply;
import com.ericsson.vodafone.poc.eee.dm.Decision;
import com.ericsson.vodafone.poc.eee.jar.exception.MonitoredValuesUnavailableException;
import com.ericsson.vodafone.poc.eee.jar.utils.CsvHistoryReader;
import com.ericsson.vodafone.poc.eee.jar.utils.IfMonitoredState;
import com.ericsson.vodafone.poc.eee.jar.utils.Triplet;
import com.ericsson.vodafone.poc.eee.odlPlugin.exception.HttpURLConnectionFailException;
import com.ericsson.vodafone.poc.eee.odlPlugin.MonitoredRateHandler;
import com.ericsson.vodafone.poc.eee.odlPlugin.exception.OdlOperationFailureException;
import com.ericsson.vodafone.poc.eee.odlPlugin.OdlRESTServiceClientImpl;
import com.ericsson.vodafone.poc.predictor.api.exception.PredictionUnavailableException;
import com.ericsson.vodafone.poc.predictor.api.exception.SerieOperationException;

import com.ericsson.vodafone.poc.predictor.api.utils.Prediction;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ericsson.vodafone.poc.eee.jar.EnergyEfficiencyEngine.convertMb_sInByte_s;
import static com.ericsson.vodafone.poc.eee.jar.EnergyEfficiencyEngine.getGroupFromJobKey;
import static com.ericsson.vodafone.poc.eee.jar.EnergyEfficiencyEngine.getIdFromJobKey;
import static com.ericsson.vodafone.poc.eee.jar.EnergyEfficiencyEngine.convertByte_sInMb_s;
import static com.ericsson.vodafone.poc.eee.jar.utils.Constants.EEE_JOB_NUM_OF_READ_RETRIES_ON_DUPLICATE_TIME_INTEVAL;
import static com.ericsson.vodafone.poc.eee.jar.utils.Constants.JOB_DATA_MAP_JOBKEY;

import java.io.FileNotFoundException;
import java.util.Date;
import java.util.List;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class EnergyEfficiencyJob implements Job {

    EnergyEfficiencyCache eeCache = EnergyEfficiencyCache.getInstance();

    private Logger logger = LoggerFactory.getLogger(EnergyEfficiencyEngine.class);

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println("\nEnergyEfficiencyJob start");
        logger.debug("\nEnergyEfficiencyJob start");

        JobDataMap data = jobExecutionContext.getJobDetail().getJobDataMap();
        String cacheJobKey = data.getString(JOB_DATA_MAP_JOBKEY);

        JobKey quartzJobKey = jobExecutionContext.getJobDetail().getKey();

        EnergyEfficiencyJobData eejd = eeCache.get(cacheJobKey);

        OdlRESTServiceClientImpl odlRESTServiceClient = new OdlRESTServiceClientImpl();
        MonitoredRateHandler monitoredRateHandler = null;

        final String id_ifRefField = EnergyEfficiencyEngine.getIdFromJobKey(cacheJobKey);
        final String group_networkRefField = EnergyEfficiencyEngine.getGroupFromJobKey(cacheJobKey);

        System.out.println("\nEnergyEfficiencyJob is executing for interface " + id_ifRefField);
        logger.info("\nEnergyEfficiencyJob is executing for interface {}", id_ifRefField );

        Double monitoredRate = new Double(0);
        Double configuredBandwidth = new Double(0);
        int timeInterval = 0;
        logger.info(eejd.getInterfaceData().toString());

        try {
            for (int i=0; i < EEE_JOB_NUM_OF_READ_RETRIES_ON_DUPLICATE_TIME_INTEVAL; i++) {
                //Monitored rate: request to ODL
                try {
                    monitoredRateHandler = odlRESTServiceClient.getInterfaceMonitoredData(group_networkRefField, id_ifRefField);

                    //Odl sends byte/s - interfaces needs Mb/s
                    monitoredRate = convertByte_sInMb_s(monitoredRateHandler.getMonitoredRate());
                    configuredBandwidth = convertByte_sInMb_s(monitoredRateHandler.getBandwidthCapacity());
                    timeInterval = monitoredRateHandler.getTimeInterval();

                    logger.info("\nEnergyEfficiencyJob: getInterfaceMonitoredData for interface {} - ODL response: \n\n monitored Rate: {} Mb/s - {} byte/s\n",
                            id_ifRefField, monitoredRate, monitoredRateHandler.getMonitoredRate());

                    //If BandwidthCapacity != 0, is a loss of traffic to be managed
                    if(monitoredRate == 0 &&
                            monitoredRateHandler.getBandwidthCapacity() == 0) {
                        eejd.getMpAlignment().setMisaligned();
                        eeCache.updateIfMonitoredState(id_ifRefField, group_networkRefField, IfMonitoredState.UNAVAILABLE);
                        EnergyEfficiencyEngine.unscheduleJob(quartzJobKey);
                        //EnergyEfficiencyEngine.scheduleTrafficPollerJob(id_ifRefField, group_networkRefField);
                        logger.error("EnergyEfficiencyJob: interface unavailable. \nEnd of monitoring of interface {}", id_ifRefField);
                        return;
                    }

                    if(!eejd.getMpAlignment().isAligned()) {
                        // ERRORE GRAVISSIMO E NON RECUPERABILE AL MOMENTO, COSA FACCIAMO?
                        logger.error("EnergyEfficiencyJob: Unable to compare Monitored Rate with Prediction for interface {}: no alignement available." +
                                "\nEnd of monitoring of interface.", id_ifRefField);
                        eeCache.updateIfMonitoredState(id_ifRefField, group_networkRefField, IfMonitoredState.UNAVAILABLE);
                        EnergyEfficiencyEngine.unscheduleJob(quartzJobKey);
                        //EnergyEfficiencyEngine.scheduleTrafficPollerJob(id_ifRefField, group_networkRefField);
                        return;
                    }

                    int expectedTimeInterval = (eejd.getMpAlignment().getMonitoredTimeInterval() + 1);
                    if(timeInterval == expectedTimeInterval) {
                        // NON HO BISOGNO DI ASPETTARE, IL PROGRESSIVO CHE HO RICEVUTO È IL SUCCESSIVO DI QUELLO MEMORIZZATO
                        logger.info("\nEnergyEfficiencyJob: valid timeInterval for interface {}",timeInterval, id_ifRefField);
                        break;
                    }
                    else if(timeInterval < expectedTimeInterval) {
                        final Date prev = jobExecutionContext.getTrigger().getPreviousFireTime();
                        final Date next = jobExecutionContext.getTrigger().getNextFireTime();
                        final long waitTimeout = (next.getTime() - prev.getTime()) / 5;
                        logger.info("\nEnergyEfficiencyJob: INVALID timeInterval {}  expectedTimeInterval {} for interface {} - Retry ({} - waitTimeout {})",
                                timeInterval, expectedTimeInterval, id_ifRefField, i, waitTimeout);
                        //wait(waitTimeout);
                        try {
                            Thread.sleep(waitTimeout);
                        } catch (InterruptedException e1) {
                            logger.error("InterruptedException - sleep");
                            e1.printStackTrace();
                        }
                    }
                    else {
                        logger.error("\nERROR - timeInterval {} > expectedTimeInterval {} for interface {}",
                                timeInterval, expectedTimeInterval, id_ifRefField);
                        int fakeSamples = timeInterval - expectedTimeInterval;
                        logger.info("\n {} samples to recover for interface {}", fakeSamples, id_ifRefField);
                        while (fakeSamples > 0) {
                            eejd.pushMonitoredValue(monitoredRate);
                            eejd.pushConfiguredBandwidthValue(configuredBandwidth);
                            eejd.getMpAlignment().incrementAlignment(eejd);
                            eejd.pushSavingDataValues(monitoredRateHandler.getCurrentOutputPower(), monitoredRateHandler.getNominalOutputPower());
                            fakeSamples--;
                            logger.info("\nRecovering lost values for interface {}:\n fake monitoredRate {}\n fake configuredBandwidth {}\n New MpAlignment: {}",
                                    id_ifRefField, monitoredRate, configuredBandwidth, eejd.getMpAlignment().toString());
                        }
                        logger.info("\nEnd Recovering lost values for interface {}", id_ifRefField);
                    }
                } catch (OdlOperationFailureException e) {
                    logger.error("EnergyEfficiencyJob: getInterfaceMonitoredData failed - Exception ODL Operation: {}",
                            e.getMessage());
                    e.printStackTrace();
                    try {
                        if(!eejd.isPredictionAvailable()) {
                            monitoredRate = (Double) eejd.getMonitoredValues().getLastElement();
                        }
                        else {
                            Prediction<Long> prediction = eejd.getCurrentPrediction();
                            monitoredRate = convertMb_sInByte_s(prediction.getPredictedValue().doubleValue()).doubleValue();
                        }
                        break;
                    } catch (PredictionUnavailableException e1) {
                        monitoredRate = 0.0;
                        logger.error("EnergyEfficiencyJob: prediction not found {} - setting monitoredRate = 0\n", e.getMessage());
                        e1.printStackTrace();
                    } catch (MonitoredValuesUnavailableException e1) {
                        logger.error("EnergyEfficiencyJob: last monitoredValue unavailable. \nEnd of monitoring of interface {}\n{}", id_ifRefField, e.getMessage());
                        e1.printStackTrace();
                        eejd.getMpAlignment().setMisaligned();
                        eeCache.updateIfMonitoredState(id_ifRefField, group_networkRefField, IfMonitoredState.UNAVAILABLE);
                        EnergyEfficiencyEngine.unscheduleJob(quartzJobKey);
                        //EnergyEfficiencyEngine.scheduleTrafficPollerJob(id_ifRefField, group_networkRefField);
                        return;
                    }
                }
            }

            eejd.pushMonitoredValue(monitoredRate);
            eejd.pushConfiguredBandwidthValue(configuredBandwidth);
            eejd.getMpAlignment().incrementAlignment(eejd);
            eejd.pushSavingDataValues(monitoredRateHandler.getCurrentOutputPower(), monitoredRateHandler.getNominalOutputPower());
            eejd.getInterfaceData().setIfCurrentCapacity(monitoredRateHandler.getBandwidthCapacity());

            Decision decision = eejd.makeDecision(id_ifRefField, group_networkRefField);

            List<Action> actionList= decision.getActionList();
            for(Action action: actionList) {
                CommandToApply commandToApply = action.getCommandToApply();
                if(commandToApply == CommandToApply.SET_NEW_INTERFACE_RATE) {
                    //Odl need byte/s
                    Long newCapacityToSet = convertMb_sInByte_s(Double.valueOf(commandToApply.getValue())) ;
                    logger.info("\n EnergyEfficiencyJob: setInterfaceCurrentCapacity \n\tcapacity to set: {} Mb/s - {} byte/s on interface {}\n\tCommand {}",
                            convertByte_sInMb_s(newCapacityToSet), newCapacityToSet, id_ifRefField, commandToApply.getCmdName());

                    odlRESTServiceClient.setInterfaceCurrentCapacity(
                            getGroupFromJobKey(cacheJobKey), getIdFromJobKey(cacheJobKey), newCapacityToSet);

                }
                else if(commandToApply == CommandToApply.SET_MAXIMUM_CAPACITY) {
                    Long newCapacityToSet = eejd.getInterfaceData().getIfMaximumCapacity();
                    logger.info("\n EnergyEfficiencyJob: setInterfaceCurrentCapacity \n\tcapacity to set: {} Mb/s - {} byte/s on interface {}\n\tCommand {}",
                            convertByte_sInMb_s(newCapacityToSet),newCapacityToSet, id_ifRefField, commandToApply.getCmdName());

                    odlRESTServiceClient.setInterfaceCurrentCapacity(
                            getGroupFromJobKey(cacheJobKey), getIdFromJobKey(cacheJobKey), newCapacityToSet);
                }
                else {
                    logger.info("\n EnergyEfficiencyJob: \n\tCommand on interface {}: {}", id_ifRefField, commandToApply.getCmdName());
                }

                //TODO remove this debug
                String fileName = id_ifRefField.replace('/', '_');
                fileName = fileName.replace(':', '_');
                fileName = fileName + ".txt";
                CsvHistoryReader reader = new CsvHistoryReader(fileName, 3, new Date(), eejd.getSamplingIntervalInSeconds());
                try {
                    Triplet<Double, Double, Double> tripletOfHistoryValues = reader.getHistorySamples(eejd.getMpAlignment().getPredictedIndex());
                    logger.info(" MonitoredValues in history file for interface: {}\n\n sample {}\n\tFirstWeek {}\n\tSecondWeek {}\n\tThirdWeek {}\n",
                            id_ifRefField, eejd.getMpAlignment().getPredictedIndex(),tripletOfHistoryValues.first, tripletOfHistoryValues.second, tripletOfHistoryValues.third);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                //TODO remove this debug

                eejd = eeCache.get(cacheJobKey);
                logger.info("\n EnergyEfficiencyJob: JobData updated in eeCache\n{}\n{}\n\n{}\n\nMpAlignment {}\n",
                        eejd.getInterfaceData().toString(), eejd.monitoredValuesToString(),eejd.savingDataValuesToString(), eejd.getMpAlignment().toString());
            }
        } catch (HttpURLConnectionFailException e) {
            logger.info("EnergyEfficiencyJob: Exception HttpUrl Connection Operation: {}", e.getMessage());
            e.printStackTrace();
        } catch (OdlOperationFailureException e) {
            logger.info("EnergyEfficiencyJob: Exception ODL Operation: {}", e.getMessage());
            e.printStackTrace();
        } catch (SchedulerException e) {
            logger.info("EnergyEfficiencyJob: Exception during Scheduler Operation: {}", e.getMessage());
            e.printStackTrace();
        } catch (SerieOperationException e) {
            logger.info("EnergyEfficiencyJob: Exception during Cache Operation: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}