package com.ericsson.vodafone.poc.eee.jar;

import com.ericsson.vodafone.poc.eee.dm.EnergyEfficiencyDecisionMaker;
import com.ericsson.vodafone.poc.eee.jar.utils.*;
import com.ericsson.vodafone.poc.eee.odlPlugin.*;
import com.ericsson.vodafone.poc.eee.jar.utils.IfMonitoredState;
import com.ericsson.vodafone.poc.eee.odlPlugin.utils.InterfaceData;
import com.ericsson.vodafone.poc.predictor.api.exception.PredictionUnavailableException;
import com.ericsson.vodafone.poc.predictor.api.exception.SerieOperationException;
import com.ericsson.vodafone.poc.predictor.api.impl.DomainAgnosticIncidentPrediction;
import com.ericsson.vodafone.poc.predictor.api.utils.Serie;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import static com.ericsson.vodafone.poc.eee.jar.utils.Constants.*;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;
import java.io.FileNotFoundException;
import java.util.*;

@Startup
@Singleton
public class EnergyEfficiencyEngine {

    private static Logger logger = LoggerFactory.getLogger(EnergyEfficiencyEngine.class);
    private static Scheduler scheduler;

    OdlRESTServiceClientImpl odlRESTServiceClient = new OdlRESTServiceClientImpl();

    @PostConstruct
    public void init() {
        System.out.println("*** init: Starting EEE execution.");
        logger.info("*** init: Starting EEE execution.");

        try {
            System.out.println("init: Starting scheduler...");
            logger.info("init: Starting scheduler...");

            // Grab the Scheduler instance from the Factory
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();

            logger.info("init: Scheduler started - starting OdlConfigurationPollerJob");
            System.out.println("init: Scheduler started - OdlConfigurationPollerJob");

            scheduleOdlConfigurationPollerJob();

        } catch (final SchedulerException se) {
            se.printStackTrace();
        }

        logger.debug("*** init: End EEE init method");
    }

    @PreDestroy
    public void stop() {
        try {

            //TODO non penso sia necessare fare la disableInterfaceRateMonitoring - decidere se e quando farla
            scheduler.shutdown();
            logger.debug("stop: Scheduler stopped");
        } catch (final SchedulerException e) {
            e.printStackTrace();
        }
    }


    static public EnergyEfficiencyJobData prepareandAddInterfaceDataToCache(final InterfaceData interfaceData) {
        final EnergyEfficiencyJobData eejd = new EnergyEfficiencyJobData();
        eejd.setInterfaceData(interfaceData);
        eejd.getMpAlignment().setMisaligned();
        addJobDataToCache(interfaceData.getIfRef(), interfaceData.getNetworkRef(),eejd);
        return eejd;
    }

    static public void prepareandAddJobDataToCache(final String ifRef,final String networkRef,MonitoredRateHandler monitoredRateHandler) throws FileNotFoundException, SerieOperationException {
        logger.debug("prepareandAddJobDataToCache: networkRef {} - ifRef {}", ifRef, networkRef);
        EnergyEfficiencyJobData jobData = prepareJobData(ifRef,networkRef, monitoredRateHandler);
        addJobDataToCache(ifRef, networkRef,jobData);

    }


    static public void addJobDataToCache(final String ifRef, final String networkRef, final EnergyEfficiencyJobData jobData) {
        logger.debug("addJobDataToCache: networkRef{} - ifRef {}", networkRef, ifRef);
        final String jobKey = createJobKey(ifRef, networkRef);
        logger.debug("addJobDataToCache: jobKey {}", jobKey);
        EnergyEfficiencyCache eeCache = EnergyEfficiencyCache.getInstance();
        eeCache.put(jobKey, jobData);
    }

    static void scheduleEEJob(final String ifRef, final String networkRef, final int intervalInSeconds)
            throws SchedulerException, FileNotFoundException {

        final String jobKey = createJobKey(ifRef, networkRef);

        final JobDetail job = newJob(EnergyEfficiencyJob.class).withIdentity(ifRef, networkRef).build();
        job.getJobDataMap().put(JOB_DATA_MAP_JOBKEY, jobKey);

        if(!scheduler.checkExists(job.getKey())) {
            final PropertiesReader pReader = new PropertiesReader();

            Long sleep = new Long(pReader.loadProperty(ENGINE_DEFAULT_SAMPLING_INTERVAL_IN_SECONDS));
            Date date = new Date(System.currentTimeMillis() + (sleep * Long.valueOf(1000)));
            logger.info("scheduleEEJob - EEJob {} will be started at {}", jobKey, date);

            final Trigger trigger = newTrigger().withIdentity(ifRef, networkRef).startAt(date)
                    .withSchedule(simpleSchedule().withIntervalInSeconds(intervalInSeconds).repeatForever()).build();

            scheduler.scheduleJob(job, trigger);
        }
    }

    static void scheduleTrafficPollerJob(final String id, final String group) throws SchedulerException {
        final PropertiesReader pReader = new PropertiesReader();
        try {
            final Integer samplingIntervalInSeconds = new Integer(pReader.loadProperty(ENGINE_DEFAULT_TRAFFIC_POLLER_INTERVAL_IN_SECONDS));
            scheduleTrafficPollerJob(id, group, samplingIntervalInSeconds);
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    static void scheduleTrafficPollerJob(final String id, final String group, final int intervalInSeconds)
            throws SchedulerException {

        final String jobKey = createJobKey(id, group);

        final JobDetail job = newJob(TrafficPollerJob.class).withIdentity(id, group).build();
        job.getJobDataMap().put(JOB_DATA_MAP_JOBKEY, jobKey);

        final Trigger trigger = newTrigger().withIdentity(id, group).startNow()
                .withSchedule(simpleSchedule().withIntervalInSeconds(intervalInSeconds).repeatForever()).build();

        scheduler.scheduleJob(job, trigger);

    }

    static void unscheduleJob(JobKey jobKey) throws SchedulerException {
        scheduler.deleteJob(jobKey);
    }

    //ODL Configuration Poller
    public static void scheduleOdlConfigurationPollerJob() throws SchedulerException {
        final PropertiesReader pReader = new PropertiesReader();
        try {
            final Integer samplingIntervalInSeconds = new Integer(pReader.loadProperty(ODL_DEFAULT_CONFIGURATION_POLLER_INTERVAL_IN_SECONDS));
            scheduleOdlConfigurationPollerJob(samplingIntervalInSeconds);
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    static void scheduleOdlConfigurationPollerJob(final int intervalInSeconds)
            throws SchedulerException {

        final JobDetail job = newJob(OdlConfigurationPollerJob.class).withIdentity(STARTUP_JOBKEY).build();
        job.getJobDataMap().put(JOB_DATA_MAP_JOBKEY, STARTUP_JOBKEY);

        final Trigger trigger = newTrigger().withIdentity(STARTUP_JOBKEY).startNow()
                .withSchedule(simpleSchedule().withIntervalInSeconds(intervalInSeconds).repeatForever()).build();

        scheduler.scheduleJob(job, trigger);

    }

    //Interfaces Management Poller
    public static void scheduleInterfacesMonitoringPollerJob() throws SchedulerException {
        final PropertiesReader pReader = new PropertiesReader();
        try {
            final Integer samplingIntervalInSeconds = new Integer(pReader.loadProperty(ODL_DEFAULT_INTERFACE_MANAGEMENT_POLLER_INTERVAL_IN_SECONDS));
            scheduleInterfacesMonitoringPollerJob(samplingIntervalInSeconds);
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    static void scheduleInterfacesMonitoringPollerJob(final int intervalInSeconds)
            throws SchedulerException {

        final JobDetail job = newJob(InterfacesMonitoringPollerJob.class).withIdentity(STARTUP_JOBKEY).build();
        job.getJobDataMap().put(JOB_DATA_MAP_JOBKEY, STARTUP_JOBKEY);

        final Trigger trigger = newTrigger().withIdentity(STARTUP_JOBKEY).startNow()
                .withSchedule(simpleSchedule().withIntervalInSeconds(intervalInSeconds).repeatForever()).build();

        scheduler.scheduleJob(job, trigger);

    }

    static private EnergyEfficiencyJobData prepareJobData(final String interfaceId,final String group,MonitoredRateHandler monitoredRateHandler) throws SerieOperationException, FileNotFoundException {
        final EnergyEfficiencyJobData eejd = new EnergyEfficiencyJobData();

        EnergyEfficiencyCache eeCache = EnergyEfficiencyCache.getInstance();
        eejd.setInterfaceData(eeCache.getInterfaceData(interfaceId, group));

        String fileName = interfaceId.replace('/', '_');
        fileName = fileName.replace(':', '_');
        fileName = fileName + ".txt";
        logger.info("prepareJobData: history fileName: {}", fileName);

        HistoryReader reader = new CsvHistoryReader(fileName, 3, new Date(), eejd.getSamplingIntervalInSeconds());
        Serie<Integer> startupHistory = reader.loadHistory();

        logger.debug("prepareJobData:loadHistory completed - size {}", startupHistory.getSize());

        DomainAgnosticIncidentPrediction daip = new DomainAgnosticIncidentPrediction(interfaceId, startupHistory);
        try {
            eejd.setPredictable(daip);
            eejd.getInterfaceData().setIfMonitoredState(IfMonitoredState.MONITORING);
            logger.debug("prepareJobData: Prediction available");
            //logger.info(eejd.predictionToString() );
        } catch (PredictionUnavailableException e) {
            logger.info("prepareJobData: WARNING: PREDICTION NOT AVAILABLE");
            eejd.getInterfaceData().setIfMonitoredState(IfMonitoredState.LEARNING);
            //e.printStackTrace();
        }

        Serie<Long> monitoredValuesSerie = new Serie(1, startupHistory.getSize()/3, new Date(0), eejd.getSamplingIntervalInSeconds());
        eejd.setMonitoredValues(monitoredValuesSerie);

        Serie<Long> configuredBandwidthValuesSerie = new Serie(1, startupHistory.getSize()/3, new Date(0), eejd.getSamplingIntervalInSeconds());
        eejd.setConfiguredBandwidthValues(configuredBandwidthValuesSerie);

        Serie<SavingData> savingDataSerie = new Serie(3, startupHistory.getSize(), new Date(0), eejd.getSamplingIntervalInSeconds());
        eejd.setSavingDataValues(savingDataSerie);

        eejd.setDecisionMaker(new EnergyEfficiencyDecisionMaker());

        logger.info("prepareJobData: JobData ready");

        Date firstSampleDate = new Date();
        try {
            firstSampleDate = eejd.getPrediction().getFirstSampleDate();
        } catch (PredictionUnavailableException e) {
            //e.printStackTrace();
            logger.info("\n WARNING: PREDICTION NOT AVAILABLE");
        }
        eejd.pushMonitoredValue(convertByte_sInMb_s(monitoredRateHandler.getMonitoredRate()), new Date(firstSampleDate.getTime()));
        eejd.pushConfiguredBandwidthValue(convertByte_sInMb_s(monitoredRateHandler.getBandwidthCapacity()), new Date(firstSampleDate.getTime()));

        int timeInterval = monitoredRateHandler.getTimeInterval();
        eejd.setMpAlignment(new MPAlignment(timeInterval, 0, 0));

        eejd.pushSavingDataValues(monitoredRateHandler.getCurrentOutputPower(), monitoredRateHandler.getNominalOutputPower(), new Date(firstSampleDate.getTime()));

        return eejd;
    }

    public static String createJobKey(final String id, final String group) {
        return id.concat(Constants.JOB_KEY_SEPARATOR).concat(group);
    }

    public static String getIdFromJobKey(final String jobKey) {
        return jobKey.split(Constants.JOB_KEY_SEPARATOR)[Constants.JOB_KEY_ID_POS];
    }

    public static String getGroupFromJobKey(final String jobKey) {
        return jobKey.split(Constants.JOB_KEY_SEPARATOR)[Constants.JOB_KEY_GROUP_POS];
    }

    public static Double convertByte_sInMb_s(Long value) {
        return value.doubleValue()*8/1000000;
    }

    public static Long convertMb_sInByte_s(Double value) {
        value = value*1000000/8;
        return value.longValue();
    }

}
