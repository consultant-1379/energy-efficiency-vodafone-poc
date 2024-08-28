package com.ericsson.vodafone.poc.eee.jar;

import static com.ericsson.vodafone.poc.eee.jar.EnergyEfficiencyEngine.prepareandAddInterfaceDataToCache;
import static com.ericsson.vodafone.poc.eee.jar.utils.Constants.ODL_DEFAULT_COLLECTION_INTERVAL;
import static com.ericsson.vodafone.poc.eee.jar.utils.Constants.ODL_DEFAULT_HISTORY_LENGHT;

import java.io.FileNotFoundException;
import java.util.Date;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.vodafone.poc.eee.jar.utils.*;
import com.ericsson.vodafone.poc.eee.odlPlugin.utils.InterfaceData;
import com.ericsson.vodafone.poc.eee.odlPlugin.exception.HttpURLConnectionFailException;
import com.ericsson.vodafone.poc.eee.odlPlugin.InterfaceListHandler;
import com.ericsson.vodafone.poc.eee.odlPlugin.exception.OdlOperationFailureException;
import com.ericsson.vodafone.poc.eee.odlPlugin.OdlRESTServiceClientImpl;
import com.ericsson.vodafone.poc.predictor.api.exception.SerieOperationException;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class InterfacesMonitoringPollerJob implements Job {

    private Logger logger = LoggerFactory.getLogger(EnergyEfficiencyEngine.class);
    EnergyEfficiencyCache eeCache = EnergyEfficiencyCache.getInstance();

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        logger.info("\nInterfacesMonitoringPollerJob started.");

        OdlRESTServiceClientImpl odlRESTServiceClient = new OdlRESTServiceClientImpl();

        Integer odlCollectionInterval = 30;
        Integer odlHistoryLength = 2;
        Integer samplingIntervalInSeconds = 0;

        final PropertiesReader pReader = new PropertiesReader();
        try {
            samplingIntervalInSeconds = new Integer(pReader.loadProperty(Constants.ENGINE_DEFAULT_SAMPLING_INTERVAL_IN_SECONDS));
            odlCollectionInterval = new Integer(pReader.loadProperty(ODL_DEFAULT_COLLECTION_INTERVAL));
            odlHistoryLength = new Integer(pReader.loadProperty(ODL_DEFAULT_HISTORY_LENGHT));
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }

        logger.debug("\nCall getInterfaceItemList");
        try {
            InterfaceListHandler interfaceListHandler = odlRESTServiceClient.getInterfaceDataList();
            logger.debug("\nCall enableDisableInterfaceRateMonitorning");

            String interfacesMonitored = "";
            int count = 0;
            for (InterfaceData interfaceData : interfaceListHandler.getInterfaceDataList()) {
                String networkRef = interfaceData.getNetworkRef();
                String ifRef = interfaceData.getIfRef();

                //TODO!!! MANAGE unavailable interfaceData with a job
                logger.info("\nINFO: Interface networkRef.getIfCurrentCapacity() = {}", interfaceData.getIfCurrentCapacity());
                logger.info("\nINFO: Interface networkRef.getIfMaximumCapacity() = {}", interfaceData.getIfMaximumCapacity());
                //if (interfaceData.getIfCurrentCapacity() == 0 && interfaceData.getIfMaximumCapacity() == 0) {
                if (interfaceData.getIfCurrentCapacity().equals(new Long(0)) && 
                    interfaceData.getIfMaximumCapacity().equals(new Long(0))) {
                    logger.info("\nWARNING: Interface networkRef: {}  ifRef: {} unavailable! - SKIP", networkRef, ifRef);
                    interfaceData.setIfMonitoredState(IfMonitoredState.UNAVAILABLE);
                } else {
                    count++;
                    interfacesMonitored += "\n" + interfaceData.getIfRef();
                    logger.info(interfaceData.toString());
                }

                prepareandAddInterfaceDataToCache(interfaceData);

                if (interfaceData.getIfMonitoredState() == IfMonitoredState.DISABLE) {
                    logger.info("\nEnabling networkRef: {}  ifRef: {} ", networkRef, ifRef);
                    try {
                        odlRESTServiceClient.enableDisableInterfaceRateMonitoring(networkRef, ifRef, odlCollectionInterval.toString(), "true",
                                odlHistoryLength.toString());
                        eeCache.updateIfMonitoredState(ifRef, networkRef, IfMonitoredState.ENABLE);
                    } catch (HttpURLConnectionFailException e) {
                        e.printStackTrace();
                        logger.error("Enabling networkRef: {}  ifRef: {} ", networkRef, ifRef);
                        eeCache.updateIfMonitoredState(ifRef, networkRef, IfMonitoredState.UNAVAILABLE);
                    } catch (OdlOperationFailureException e) {
                        e.printStackTrace();
                        eeCache.updateIfMonitoredState(ifRef, networkRef, IfMonitoredState.UNAVAILABLE);
                    }
                }
            }

            if (count == 0) {
                throw new OdlOperationFailureException(
                        "\nAll interfaces CurrentCapacity and MaximumCapacity empty - interface monitoring cannot be started");
            } else {
                logger.info("\n*** Sent enable command to ODL on {} available interfaces: {}", count, interfacesMonitored);

                logger.info("\nSleep waiting for ODL monitornig start");
                try {
                    Long waitIntervall = Long.valueOf((odlCollectionInterval - 1) * 1000);
                    Thread.sleep(waitIntervall);
                } catch (InterruptedException e1) {
                    logger.error("InterruptedException - sleep");
                    e1.printStackTrace();
                }
            }

            logger.info("\n*** Interfaces monitoring enabled on ODL- Start Scheduling TrafficPollerJob");

            interfacesMonitored = "";
            count = 0;
            for (InterfaceData interfaceData : interfaceListHandler.getInterfaceDataList()) {
                //TODO!!! MANAGE unavailable interfaceData with a job
                if (eeCache.getIfMonitoredState(interfaceData.getIfRef(), interfaceData.getNetworkRef()) == IfMonitoredState.ENABLE) {
                    JobKey myJobKey = jobExecutionContext.getJobDetail().getKey();
                    try {
                        String fileName = interfaceData.getIfRef().replace('/', '_');
                        fileName = fileName.replace(':', '_');
                        fileName = fileName + ".txt";
                        logger.info("prepareJobData: history fileName: {}", fileName);
                        CsvHistoryReader reader = new CsvHistoryReader(fileName, 3, new Date(), samplingIntervalInSeconds);

                        if (reader.historyFileExist()) {
                            eeCache.updateIfMonitoredState(interfaceData.getIfRef(), interfaceData.getNetworkRef(), IfMonitoredState.WAITING_FOR_DATA);
                            EnergyEfficiencyEngine.scheduleTrafficPollerJob(interfaceData.getIfRef(), interfaceData.getNetworkRef());

                            logger.info("\n***Traffic poller Job started on networkRef: {}  ifRef: {}", interfaceData.getNetworkRef(),
                                    interfaceData.getIfRef());
                            count++;
                            interfacesMonitored += "\n" + interfaceData.getIfRef();
                            logger.info(interfaceData.toString());
                        } else {
                            //TODO only for POC - manage learning state
                            eeCache.updateIfMonitoredState(interfaceData.getIfRef(), interfaceData.getNetworkRef(),
                                    IfMonitoredState.ENABLE_NOT_MONITORED);
                            logger.error("HistoryFile {} not found for interface ifRef {} - SKIP", fileName, interfaceData.getIfRef());
                        }

                    } catch (SchedulerException e) {
                        eeCache.updateIfMonitoredState(interfaceData.getIfRef(), interfaceData.getNetworkRef(), IfMonitoredState.ENABLE_NOT_MONITORED);
                        logger.error("Error on schedule job on interface networkRef: {}  ifRef: {} - {} - SKIP", interfaceData.getNetworkRef(),
                                interfaceData.getIfRef(), e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            logger.info("\n*** {} traffic poller job started on available interfaces: {}", count, interfacesMonitored);

            JobKey myJobKey = jobExecutionContext.getJobDetail().getKey();
            EnergyEfficiencyEngine.unscheduleJob(myJobKey);

            logger.info("\nEnd InterfacesMonitoringPollerJob.");

        } catch (HttpURLConnectionFailException e) {
            logger.info("*** GetInterfaceDataList failed - {} - Retry", e.getMessage());
            e.printStackTrace();
        } catch (OdlOperationFailureException e) {
            logger.info("*** GetInterfaceDataList failed - {} - Retry", e.getMessage());
            e.printStackTrace();
        } catch (SchedulerException e) {
            logger.error("Error on deschedule job - Retry later");
            e.printStackTrace();
        }
    }
}
