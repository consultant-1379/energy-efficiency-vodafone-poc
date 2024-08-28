package com.ericsson.vodafone.poc.eee.odlPlugin;

import com.ericsson.vodafone.poc.eee.odlPlugin.exception.HttpURLConnectionFailException;
import com.ericsson.vodafone.poc.eee.odlPlugin.exception.OdlOperationFailureException;
import com.ericsson.vodafone.poc.eee.odlPlugin.utils.InterfaceData;
import com.ericsson.vodafone.poc.eee.odlPlugin.utils.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.ericsson.vodafone.poc.eee.odlPlugin.utils.Constants.*;

/**
 * The type Odl rest service client.
 */
public class OdlRESTServiceClientImpl implements OdlRESTServiceClient{
    private Logger logger = LoggerFactory.getLogger(OdlRESTServiceClientImpl.class);

    // ***** Configure Topology
    public void configureTopology(String jsonFileName) throws HttpURLConnectionFailException, OdlOperationFailureException {
        logger.info("\n Start configureTopology - jsonFileName: " + jsonFileName);

        try {
            JsonHandler jsonHandler = new JsonHandler();
            String jsonInputString = jsonHandler.readAndConvertJSONFileInString(jsonFileName);
            String networkId = jsonHandler.parseGetNetworkId(jsonFileName);
            String uri = CONFIG_URI + TOPOLOGY_URI + networkId;
            logger.info("\n URI: " + uri);

            try {
                HttpOperationHandler httoOperationHandler = new HttpOperationHandler();
                ResultHandler response = httoOperationHandler.put(uri, jsonInputString);

                if(response.isResultOk()) {
                    logger.info("configureTopology \n response {}", response.getResultMessage());
                }
                else {
                    logger.error("configureTopology failure - response {}", response.getResultMessage());
                    throw new OdlOperationFailureException("Odl response: " + response.getResultMessage());
                }

            } catch (Exception e) {
                System.out.println("\nError while calling Odl REST Service");
                System.out.println(e);
                throw new HttpURLConnectionFailException(e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new HttpURLConnectionFailException(e.getMessage());
        }
    }

    // ***** Read created topology
    public boolean isTopologyAlreadyConfigured(String jsonFileName) throws HttpURLConnectionFailException, OdlOperationFailureException{
        logger.info("\n Start isTopologyAlreadyConfigured - jsonFileName: " + jsonFileName);
        boolean isTopologyPresent = false;
        try {
            JsonHandler jsonHandler = new JsonHandler();
            String networkId = jsonHandler.parseGetNetworkId(jsonFileName);

            String uri = CONFIG_URI + GET_TOPOLOGY_FROM_OPERATIONAL_URI + networkId;
            logger.info("\n URI: " + uri);

            try {
                HttpOperationHandler httoOperationHandler = new HttpOperationHandler();
                ResultHandler response = httoOperationHandler.get(uri);

                if(response.isResultOk()) {
                    logger.info("isTopologyAlreadyConfigured \n response {} - topology already present on ODL", response.getResultMessage());
                    isTopologyPresent = true;
                }
                else if(response.isResultNotFound()) {
                    logger.info("isTopologyAlreadyConfigured \n response {} - topology not present on ODL", response.getResultMessage());
                    isTopologyPresent = false;
                }
                else {
                    logger.error("isTopologyAlreadyConfigured failure - response {}", response.getResultMessage());
                    throw new OdlOperationFailureException("Odl response: " + response.getResultMessage());
                }


            } catch (Exception e) {
                System.out.println("\nError while calling Odl REST Service");
                System.out.println(e);
                throw new HttpURLConnectionFailException(e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new HttpURLConnectionFailException(e.getMessage());
        }

        return isTopologyPresent;
    }

    // ***** Get all interfaces
    public InterfaceListHandler getInterfaceDataList() throws HttpURLConnectionFailException, OdlOperationFailureException{
        logger.info("\n Start getInterfaceDataList");

        String networkRef = "";

        List<InterfaceData> interfaceDataList = null;

        try {
            JsonHandler jsonHandler = new JsonHandler();
            String jsonInputString = jsonHandler.readAndConvertDataTemplateFileInString(GET_LIST_DATA_TEMPLATE);

            try {
                HttpOperationHandler httoOperationHandler = new HttpOperationHandler();

                String uri = OPERATIONS_URI + GET_INTERFACE_LIST_URI;
                logger.info("\n URI: " + uri);

                ResultHandler response = httoOperationHandler.post(uri, jsonInputString);

                if(response.isResultOk()) {
                    logger.info("getInterfaceDataList \n response {}", response);

                    String result = response.getResultMessage();
                    if(!jsonHandler.isResultOk(result)){
                        throw new OdlOperationFailureException("Odl response: not ok - " + response.getResultMessage());
                    }

                    networkRef = jsonHandler.parseGetNetworkRef(result);
                    interfaceDataList = jsonHandler.parseGetInterfaceDataList(result);

                    logger.info("getInterfaceDataList \nnetworkRef {}\n\tinterfaceDataList {}", networkRef, interfaceDataList);
                }
                else {
                    throw new OdlOperationFailureException("\n Odl response: " + response.getResultMessage());
                }

            } catch (Exception e) {
                System.out.println("\nError while calling Odl REST Service");
                System.out.println(e);
                throw new HttpURLConnectionFailException(e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new HttpURLConnectionFailException(e.getMessage());
        }

        return new InterfaceListHandler(networkRef,interfaceDataList);
    }

    // ***** Set unset interface rate monitoring (Enable-Disable InterfaceData Rate Monitoring)
    public void enableDisableInterfaceRateMonitoring(String networkRefField, String ifRefField, String collectionInterval,
                                                      String monitoringEnable, String historyLength) throws HttpURLConnectionFailException, OdlOperationFailureException{
        logger.info("\n Start enableDisableInterfaceRateMonitoring - networkRefField {} ifRefField {} collectionInterval {} monitoringEnable {} historyLength {}",
                networkRefField, ifRefField, collectionInterval, monitoringEnable, historyLength);

        try {
            JsonHandler jsonHandler = new JsonHandler();
            String dataTemplate = jsonHandler.compileEnableDisableInterfaceRateMonitoring(networkRefField,ifRefField,collectionInterval, monitoringEnable, historyLength);

            try {
                HttpOperationHandler httoOperationHandler = new HttpOperationHandler();
                String uri = OPERATIONS_URI + ENABLE_DISABLE_INTERFACE_RATE_MONITORING_URI;
                logger.info("\n URI: " + uri);

                ResultHandler response = httoOperationHandler.post(uri, dataTemplate);

                if(response.isResultOk()) {
                    String result = response.getResultMessage();
                    logger.info("enableDisableInterfaceRateMonitoring \ninterface networkRefField  {} - ifRefField {}\n response {}",
                            networkRefField, ifRefField,result);

                    if(!jsonHandler.isResultOk(result)){
                        throw new OdlOperationFailureException("Odl response: not ok - " + response.getResultMessage());
                    }
                }
                else {
                    throw new OdlOperationFailureException("\n Odl response: " + response.getResultMessage());
                }

            } catch (Exception e) {
                System.out.println("\nError while calling Odl REST Service");
                System.out.println(e);
                throw new HttpURLConnectionFailException(e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new HttpURLConnectionFailException(e.getMessage());
        }
    }

    // ***** Set interface rate
    public void setInterfaceCurrentCapacity(String networkRefField, String ifRefField, Long currentCapacity) throws HttpURLConnectionFailException, OdlOperationFailureException {
        logger.info("\n Start setInterfaceCurrentCapacity - networkRefField {} ifRefField {} rate {}",
                networkRefField, ifRefField, currentCapacity);

        try {
            JsonHandler jsonHandler = new JsonHandler();
            String dataTemplate = jsonHandler.compileSetInterfaceCapacityTemplate(networkRefField,ifRefField, currentCapacity);

            try {
                HttpOperationHandler httoOperationHandler = new HttpOperationHandler();

                String uri = OPERATIONS_URI + SET_INTERFACE_CAPACITY_URI;
                logger.info("\n URI: " + uri);
                ResultHandler response = httoOperationHandler.post(uri, dataTemplate);

                if(response.isResultOk()) {
                    String result = response.getResultMessage();
                    logger.info("setInterfaceCurrentCapacity \ninterface networkRefField  {} - ifRefField {}\nresponse {}",
                            networkRefField, ifRefField, result);

                    if(!jsonHandler.isResultOk(result)){
                        throw new OdlOperationFailureException("Odl response: not ok - " + response.getResultMessage());
                    }
                }
                else {
                    throw new OdlOperationFailureException("\n Odl response: " + response.getResultMessage());
                }

            } catch (Exception e) {
                System.out.println("\nError while calling Odl REST Service");
                System.out.println(e);
                throw new HttpURLConnectionFailException(e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new HttpURLConnectionFailException(e.getMessage());
        }
    }

    // ***** Get interface monitored data
    public MonitoredRateHandler getInterfaceMonitoredData(String networkRefField,String ifRefField) throws HttpURLConnectionFailException, OdlOperationFailureException{

        logger.info("\n Start getInterfaceMonitoredData - networkRefField: " + networkRefField + " ifRefField: " + ifRefField);

        MonitoredRateHandler monitoredRate = new MonitoredRateHandler();

        try {
            JsonHandler jsonHandler = new JsonHandler();
            String dataTemplate = jsonHandler.compileGetInterfaceMonitoredDataTemplate(networkRefField,ifRefField);

            try {
                HttpOperationHandler httoOperationHandler = new HttpOperationHandler();
                String uri = OPERATIONS_URI + GET_INTERFACE_MONITORES_DATA_URI;
                logger.info("\n URI: " + uri);
                ResultHandler response = httoOperationHandler.post(uri, dataTemplate);

                if(response.isResultOk()) {

                    String result = response.getResultMessage();
                    logger.info("\n getInterfaceMonitoredData: \ninterface networkRefField  {} - ifRefField {}\n response {}",
                            networkRefField, ifRefField,result);

                    if(!jsonHandler.isResultOk(result)){
                        throw new OdlOperationFailureException("Odl response: not ok - " + response.getResultMessage());
                    }

                    monitoredRate = jsonHandler.parseGetIntrfaceMonitoredData(result);

                    logger.info("\n getInterfaceMonitoredData: monitoredRate data from ODL \nnetworkRefField  {} - ifRefField {}" +
                                    "\n timeInterval {}" +
                                    "\n\tmonitoredRate {}\n\tcurrBwCapacity {}\n\ttimeStamp {}" +
                                    "\n\tnominalOutputPower {}\n\tcurrentOutputPower {}",
                            networkRefField, ifRefField,
                            monitoredRate.getTimeInterval(), monitoredRate.getMonitoredRate(), monitoredRate.getBandwidthCapacity(), monitoredRate.getTimeStamp(),
                            monitoredRate.getNominalOutputPower(), monitoredRate.getCurrentOutputPower());
                }
                else {
                    throw new OdlOperationFailureException("Odl response: " + response.getResultMessage());
                }

            } catch (Exception e) {
                System.out.println("\n Error while calling Odl REST Service");
                System.out.println(e);
                throw new HttpURLConnectionFailException(e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new HttpURLConnectionFailException(e.getMessage());
        }

        return monitoredRate;
    }
}
