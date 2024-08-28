package com.ericsson.vodafone.poc.eee.jar;

import static com.ericsson.vodafone.poc.eee.jar.utils.Constants.NETWORK_TOPOLOGY_JSON_FILE_NAME;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.vodafone.poc.eee.odlPlugin.exception.HttpURLConnectionFailException;
import com.ericsson.vodafone.poc.eee.odlPlugin.exception.OdlOperationFailureException;
import com.ericsson.vodafone.poc.eee.odlPlugin.OdlRESTServiceClientImpl;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class OdlConfigurationPollerJob implements Job {

    private Logger logger = LoggerFactory.getLogger(EnergyEfficiencyEngine.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        System.out.println("\nOdlConfigurationPollerJob started.");
        logger.info("\nOdlConfigurationPollerJob started.");

        OdlRESTServiceClientImpl odlRESTServiceClient = new OdlRESTServiceClientImpl();

        try {
            //ODL Topology Configuration
            logger.debug("\n*** Configure ODL Topology \n json file name: {} ", NETWORK_TOPOLOGY_JSON_FILE_NAME);
            //if (!odlRESTServiceClient.isTopologyAlreadyConfigured(NETWORK_TOPOLOGY_JSON_FILE_NAME)) {
                odlRESTServiceClient.configureTopology(NETWORK_TOPOLOGY_JSON_FILE_NAME);
                logger.info("\n Topology configured on ODL");
            //} else {
            //    logger.debug("Topology already configured on ODL");
            //}

            //Time left to ODL to complete configuration
            logger.info("\n Sleep waiting for ODL configuration");
            try {
                Thread.sleep(25000);
            } catch (InterruptedException e1) {
                logger.error("InterruptedException - sleep");
                e1.printStackTrace();
            }

            logger.info("\n*** ODL configured");
            System.out.println("\n*** ODL configured");

            JobKey myJobKey = jobExecutionContext.getJobDetail().getKey();
            EnergyEfficiencyEngine.unscheduleJob(myJobKey);
            EnergyEfficiencyEngine.scheduleInterfacesMonitoringPollerJob();

            logger.info("\n schedule InterfacesManagementPollerJob");
            System.out.println("\n schedule InterfacesManagementPollerJob");

        } catch (HttpURLConnectionFailException e) {
            logger.info("OdlConfigurationPollerJob - ODL connection fails {}", e.getMessage());
            System.out.println("OdlConfigurationPollerJob - ODL connection fails - Retry later");
            //e.printStackTrace();
        } catch (OdlOperationFailureException e) {
            logger.info("OdlConfigurationPollerJob - ODL operation fails {}", e.getMessage());
            System.out.println("OdlConfigurationPollerJob - ODL operation fails - Retry later");
            //e.printStackTrace();
        } catch (SchedulerException e) {
            logger.error("OdlConfigurationPollerJob was unable to unschedule itself");
            e.printStackTrace();
        }

    }

}
