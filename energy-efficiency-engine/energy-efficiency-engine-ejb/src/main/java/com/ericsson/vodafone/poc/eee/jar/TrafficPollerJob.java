package com.ericsson.vodafone.poc.eee.jar;

import com.ericsson.vodafone.poc.eee.jar.utils.IfMonitoredState;
import com.ericsson.vodafone.poc.eee.odlPlugin.exception.HttpURLConnectionFailException;
import com.ericsson.vodafone.poc.eee.odlPlugin.MonitoredRateHandler;
import com.ericsson.vodafone.poc.eee.odlPlugin.exception.OdlOperationFailureException;
import com.ericsson.vodafone.poc.eee.odlPlugin.OdlRESTServiceClientImpl;
import com.ericsson.vodafone.poc.predictor.api.exception.SerieOperationException;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ericsson.vodafone.poc.eee.jar.utils.Constants.JOB_DATA_MAP_JOBKEY;

import java.io.FileNotFoundException;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class TrafficPollerJob implements Job {

    private Logger logger = LoggerFactory.getLogger(EnergyEfficiencyEngine.class);
    EnergyEfficiencyCache eeCache = EnergyEfficiencyCache.getInstance();

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        System.out.println("\n TrafficPollerJob started.");
        logger.info("\n TrafficPollerJob started.");

        JobKey myJobKey = jobExecutionContext.getJobDetail().getKey();
        JobDataMap data = jobExecutionContext.getJobDetail().getJobDataMap();
        String dataJobKey = data.getString(JOB_DATA_MAP_JOBKEY);

        EnergyEfficiencyJobData eejd = eeCache.get(dataJobKey);
        logger.info("\n TrafficPollerJob: dataJobKey {} - myJobKey {}", dataJobKey, myJobKey);
        logger.info(eejd.getInterfaceData().toString());

        try {
            if (eejd.getMpAlignment().isAligned()) { //WHI AM I RUNNING? I WILL UNSCHEDULE MYSELF AND EXIT
                logger.error("\nWARNING: Already Aligned!!!");

                EnergyEfficiencyEngine.unscheduleJob(myJobKey);
                logger.debug("TrafficPollerJob: unscheduleJob");
                return;
            }
            // NOT ALIGNED
            // READ FROM ODL
            OdlRESTServiceClientImpl odlRESTServiceClient = new OdlRESTServiceClientImpl();

            final String id_ifRefField = EnergyEfficiencyEngine.getIdFromJobKey(dataJobKey);
            final String group_networkRefField = EnergyEfficiencyEngine.getGroupFromJobKey(dataJobKey);

            //Monitored rate: request to ODL
            MonitoredRateHandler monitoredRateHandler = odlRESTServiceClient.getInterfaceMonitoredData(group_networkRefField, id_ifRefField);

            Long monitoredRate = monitoredRateHandler.getMonitoredRate();
            logger.info("\n TrafficPollerJob: getInterfaceMonitoredData - ODL response: monitored Rate: {}", monitoredRate);

            if (monitoredRate == 0) {
                eeCache.updateIfMonitoredState(id_ifRefField, group_networkRefField, IfMonitoredState.WAITING_FOR_DATA);
                logger.info("\n TrafficPollerJob: monitoredRate 0 - Retry later");
                return;
            }
            else {
                EnergyEfficiencyEngine.prepareandAddJobDataToCache(id_ifRefField, group_networkRefField, monitoredRateHandler);

                logger.info("\n TrafficPollerJob: EEE can start monitoring on interface {}", id_ifRefField);

                EnergyEfficiencyEngine.unscheduleJob(myJobKey);
                EnergyEfficiencyEngine.scheduleEEJob(id_ifRefField, group_networkRefField, eejd.getSamplingIntervalInSeconds());
                logger.info("\n TrafficPollerJob: EEJob scheduled ifRef {} networkRef {}", id_ifRefField, group_networkRefField);

                eejd = eeCache.get(dataJobKey);
                logger.info("\n TrafficPollerJob: JobData in eeCache {}",eejd.jobDataToString());
            }
        } catch (HttpURLConnectionFailException e) {
            logger.info("TrafficPollerJob: Exception HttpUrl Connection Operation: {} - Retry", e.getMessage());
            //e.printStackTrace();
        } catch (OdlOperationFailureException e) {
            logger.info("TrafficPollerJob: Exception ODL Operation: {} - Retry", e.getMessage());
            //e.printStackTrace();
        } catch (SchedulerException e) {
            logger.error("TrafficPollerJob: Exception during Scheduler Operation: {}\n EEJob not scheduled on interface", e.getMessage());
            e.printStackTrace();
            try {
                EnergyEfficiencyEngine.unscheduleJob(myJobKey);
            } catch (SchedulerException e1) {
                e1.printStackTrace();
            }
        } catch (SerieOperationException e) {
            logger.error("TrafficPollerJob: Exception during Cache Operation: {}\n EEJob not scheduled on interface", e.getMessage());
            e.printStackTrace();
            try {
                EnergyEfficiencyEngine.unscheduleJob(myJobKey);
            } catch (SchedulerException e1) {
                e1.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            //TODO OnlyForPoc - need to manage learning state
            logger.error("TrafficPollerJob: properties file not found: {}\n EEJob not scheduled on interface", e.getMessage());
            e.printStackTrace();
            try {
                EnergyEfficiencyEngine.unscheduleJob(myJobKey);
            } catch (SchedulerException e1) {
                e1.printStackTrace();
            }
        }
    }

}
