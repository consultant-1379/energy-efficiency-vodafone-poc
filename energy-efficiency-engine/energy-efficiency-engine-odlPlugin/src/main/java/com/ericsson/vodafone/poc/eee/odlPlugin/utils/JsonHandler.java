package com.ericsson.vodafone.poc.eee.odlPlugin.utils;

/**
 * Created by esimalb on 8/21/17.
 */

import com.ericsson.vodafone.poc.eee.jar.utils.IfMonitoredState;
import com.ericsson.vodafone.poc.eee.odlPlugin.MonitoredRateHandler;
import com.ericsson.vodafone.poc.eee.odlPlugin.exception.OdlOperationFailureException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.ericsson.vodafone.poc.eee.odlPlugin.utils.Constants.*;

/**
 * The type Json handler.
 */
public class JsonHandler {
    private static final Logger logger = LoggerFactory.getLogger(JsonHandler.class);

    private String jsonFilePath;
    private String dataTemplateFilePath;

    /**
     * Instantiates a new Json handler.
     */
    public JsonHandler(){
        jsonFilePath = getJsonFilePathFromProperties();
        dataTemplateFilePath = getDataTemplatePathFromProperties();
    }

    /**
     * Gets json file path.
     *
     * @return the json file path
     */
    public String getJsonFilePath() {
        return jsonFilePath;
    }

    /**
     * Sets json file path.
     *
     * @param jsonFilePath the json file path
     */
    public void setJsonFilePath(String jsonFilePath) {
        this.jsonFilePath = jsonFilePath;
    }

    /**
     * Gets data template file path.
     *
     * @return the data template file path
     */
    public String getDataTemplateFilePath() {
        return dataTemplateFilePath;
    }

    /**
     * Sets data template file path.
     *
     * @param dataTemplateFilePath the data template file path
     */
    public void setDataTemplateFilePath(String dataTemplateFilePath) {
        this.dataTemplateFilePath = dataTemplateFilePath;
    }

    /**
     * Read and convert json file in string string.
     *
     * @param jsonFileName the json file name
     * @return the string
     * @throws IOException the io exception
     */
    public String readAndConvertJSONFileInString(String jsonFileName) throws IOException {
        return readAndConvertFileInString(getJsonFilePath() + "/" + jsonFileName);
    }

    /**
     * Read and convert data template file in string string.
     *
     * @param dataTemplateFileName the data template file name
     * @return the string
     * @throws IOException the io exception
     */
    public String readAndConvertDataTemplateFileInString(String dataTemplateFileName) throws IOException {
        return readAndConvertFileInString(getDataTemplateFilePath() + "/" + dataTemplateFileName);
    }

    /**
     * Read and convert file in string string.
     *
     * @param filePath the file path
     * @return the string
     * @throws IOException the io exception
     */
    public String readAndConvertFileInString(String filePath) throws IOException {
        logger.debug("File: {}",  filePath);

        InputStream odlnputStream = new FileInputStream(filePath);
        logger.debug("Input Stream: " + odlnputStream.toString());

        InputStreamReader odlReader = new InputStreamReader(odlnputStream);
        BufferedReader br = new BufferedReader(odlReader);

        String inputString = "";
        String line;
        while ((line = br.readLine()) != null) {
            inputString += line + "\n";
        }
        br.close();

        logger.debug("Final input: " + inputString);

        return inputString;
    }

    /**
     * Compile set interface rate data template string.
     *
     * @param networkRefField the network ref field
     * @param ifRefField      the if ref field
     * @param capacity        the rate field
     * @return the string
     * @throws IOException the io exception
     */
    public String compileSetInterfaceCapacityTemplate(String networkRefField, String ifRefField, Long capacity) throws IOException {
        String dataTemplate = readAndConvertDataTemplateFileInString(SET_INTERFACE_CAPACITY_TEMPLATE);

        networkRefField = "\"" + networkRefField + "\"";
        ifRefField = "\"" + ifRefField + "\"";

        dataTemplate = dataTemplate.replace("network-ref-field", networkRefField); //mini-link-topo
        dataTemplate = dataTemplate.replace("if-ref-field", ifRefField);//mini-link-6691-2:lag-2:WAN-1/1/1
        dataTemplate = dataTemplate.replace("capacity-field", String.valueOf(capacity)); //10000000

        logger.info("Final body: " + dataTemplate);

        return dataTemplate;
    }

    /**
     * Compile get interface rate data template string.
     *
     * @param networkRefField the network ref field
     * @param ifRefField      the if ref field
     * @return the string
     * @throws IOException the io exception
     */
    public String compileGetInterfaceMonitoredDataTemplate(String networkRefField,String ifRefField) throws IOException {
        String dataTemplate = readAndConvertDataTemplateFileInString(GET_INTERFACE_MONITORED_DATA_TEMPLATE);

        networkRefField = "\"" + networkRefField + "\"";
        ifRefField = "\"" + ifRefField + "\"";

        dataTemplate = dataTemplate.replace("network-ref-field", networkRefField); //mini-link-topo
        dataTemplate = dataTemplate.replace("if-ref-field", ifRefField);//mini-link-6691-2:lag-2:WAN-1/1/1

        logger.info("Final body: " + dataTemplate);

        return dataTemplate;
    }

    /**
     * Compile enable disable interface rate monitoring string.
     *
     * @param networkRefField    the network ref field
     * @param ifRefField         the if ref field
     * @param collectionInterval the collection interval
     * @param monitoringEnable   the monitoring enable
     * @param historyLength      the history length
     * @return the string
     * @throws IOException the io exception
     */
    public String compileEnableDisableInterfaceRateMonitoring(String networkRefField, String ifRefField,String collectionInterval,
                                                               String monitoringEnable, String historyLength) throws IOException {
        String dataTemplate = readAndConvertDataTemplateFileInString(SET_INTERFACE_RATE_MONITORING_DATA_TEMPLATE);

        networkRefField = "\"" + networkRefField + "\"";
        ifRefField = "\"" + ifRefField + "\"";

        dataTemplate = dataTemplate.replace("network-ref-field", networkRefField); //mini-link-topo
        dataTemplate = dataTemplate.replace("if-ref-field", ifRefField);//mini-link-6691-2:lag-2:WAN-1/1/1
        dataTemplate = dataTemplate.replace("collection-interval-field", collectionInterval);//60
        dataTemplate = dataTemplate.replace("monitoring-enable-field", monitoringEnable);//true/false
        dataTemplate = dataTemplate.replace("history-length-field", historyLength);//2

        logger.info("Final body: " + dataTemplate);

        return dataTemplate;
    }

    /**
     * Is result ok boolean.
     *
     * @param jsonResponse the json response
     * @return the boolean
     */
    public boolean isResultOk(final String jsonResponse) {
        String result = parseGetResult(jsonResponse);
        return result.matches("true");
    }

    /**
     * Parse get result string.
     *
     * @param jsonResponse the json response
     * @return the string
     */
    public String parseGetResult(final String jsonResponse) {
        String response = null;
        try {

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root_node = objectMapper.readValue(jsonResponse, JsonNode.class);
            JsonNode node = root_node.path("output");
            if (node != null) {
                response = node.get("result-ok").asText();
            }
        } catch (final IOException e) {
            logger.error("JsonHandler.parseGetTimeInterval exception ", e);
        }

        return response;
    }

    /**
     * Parse get time interval string.
     *
     * @param jsonResponse the json response
     * @return the string
     */
    public String parseGetTimeInterval(final String jsonResponse) {
        String response = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root_node = objectMapper.readValue(jsonResponse, JsonNode.class);
            JsonNode node = root_node.path("output");
            if (node != null) {
                response = node.get("time-interval").asText();
            }
        } catch (final IOException e) {
            logger.error("JsonHandler.parseGetTimeInterval exception ", e);
        }

        return response;
    }

    /**
     * Parse get monitored rate monitored data handler.
     *
     * @param jsonResponse the json response
     * @return the monitored data handler
     * @throws OdlOperationFailureException the odl operation failure exception
     */
    public MonitoredRateHandler parseGetIntrfaceMonitoredData(final String jsonResponse) throws OdlOperationFailureException {
        MonitoredRateHandler monitoredRateHandler = new MonitoredRateHandler();

        JSONObject fullResponse = new JSONObject(jsonResponse);
        JSONObject output = fullResponse.getJSONObject("output");

        if (!output.getBoolean("result-ok")) {
            throw new OdlOperationFailureException("get-interface-monitored-data result not OK");
        }

        monitoredRateHandler.setMonitoredRate(output.getLong("tx-traffic-rate"));
        monitoredRateHandler.setTimeInterval(output.getInt("time-interval"));
        monitoredRateHandler.setBandwidthCapacity(output.getLong("current-bandwidth-capacity"));
        monitoredRateHandler.setTimeStamp(output.getString("timestamp"));
        monitoredRateHandler.setCurrentOutputPower(output.getLong("current-output-power"));
        monitoredRateHandler.setNominalOutputPower(output.getLong("nominal-output-power"));


        return monitoredRateHandler;
    }

    /**
     * Parse get network id string.
     *
     * @param jsonFileName the json file name
     * @return the string
     */
//TODO For POC only one network is available
    //TODO to modify when more networks will be managed:
    //while instead of if
    //return List<String> instead of String
    public String parseGetNetworkId(String jsonFileName) {
        String response = null;
        try {
            String jsonSting = readAndConvertJSONFileInString(jsonFileName);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root_node = objectMapper.readValue(jsonSting, JsonNode.class);
            JsonNode network = root_node.path("network");
            Iterator<JsonNode> iterator = network.elements();
            
            if (iterator.hasNext()) {
                JsonNode interf = iterator.next();
                String listTmp = interf.get("network-id").asText();
                return listTmp;
            }
            
        } catch (final IOException e) {
            logger.error("JsonHandler.parseGetNetworkId exception ", e);
        }

        return response;
    }

    /**
     * Parse get network ref string.
     *
     * @param jsonResponse the json response
     * @return the string
     */
    public String parseGetNetworkRef(final String jsonResponse) {
        String response = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode root_node = objectMapper.readValue(jsonResponse, JsonNode.class);
            JsonNode node = root_node.path("output");
            if (node != null) {
                String jsonResponse2 = node.toString();
                JsonNode root_node2 = objectMapper.readValue(jsonResponse2, JsonNode.class);
                JsonNode node2 = root_node2.path("interfaces");
                if (node2 != null) {
                    response = node2.get("network-ref").asText();
                }
            }

        } catch (final IOException e) {
            logger.error("JsonHandler.parseGetNetworkRef exception ", e);
        }

        return response;
    }

    /**
     * Parse get interface item list list.
     *
     * @param jsonResponse the json response
     * @return the list
     */
    public List<InterfaceData> parseGetInterfaceDataList(final String jsonResponse) {

        List<InterfaceData> interfaceDataList = new ArrayList<>();

        try {
            JSONObject fullResponse = new JSONObject(jsonResponse);
            JSONObject output = fullResponse.getJSONObject("output");
            JSONObject interfaces = output.getJSONObject("interfaces");
            String networkRef = interfaces.getString("network-ref");
            JSONArray ifList = interfaces.getJSONArray("interface-list");

            for (int i=0; i < ifList.length(); i++) {
                InterfaceData ifItem = new InterfaceData();
                JSONObject jsonIf = ifList.getJSONObject(i);

                ifItem.setNetworkRef(networkRef);
                ifItem.setIfRef(jsonIf.getString("if-ref"));
                ifItem.setIfMonitoredState(IfMonitoredState.DISABLE);
                ifItem.setIfMaximumCapacity(jsonIf.getLong("maximum-bandwidth-capacity"));
                ifItem.setIfCurrentCapacity(jsonIf.getLong("current-bandwidth-capacity"));

                interfaceDataList.add(ifItem);
            }
        } catch (final JSONException e) {
            logger.error("JsonHandler.parseGetInterfaceDataList exception ", e);
        }

        return interfaceDataList;
    }

    private String getJsonFilePathFromProperties() {
        String jsonFilePath = null;
        if (System.getProperty("jsonFilePath") != null) {
            jsonFilePath = System.getProperty("jsonFilePath");
        } else {
            final PropertiesReader readProperty = new PropertiesReader();
            try {
                jsonFilePath = readProperty.loadProperty(Constants.JSON_FILE_PATH_PROP);
            } catch (final FileNotFoundException e) {
                logger.warn("getPredictorHistoryPath: Unable to find Property File for: " + "jsonFilePath");
                logger.error(e.toString());
            }
        }
        return jsonFilePath;
    }

    private String getDataTemplatePathFromProperties() {
        String dataTemplatePath = null;
        if (System.getProperty("dataTemplatePath") != null) {
            dataTemplatePath = System.getProperty("dataTemplatePath");
        } else {
            final PropertiesReader readProperty = new PropertiesReader();
            try {
                dataTemplatePath = readProperty.loadProperty(Constants.DATA_TEMPLATE_PATH_PROP);
            } catch (final FileNotFoundException e) {
                logger.warn("getPredictorHistoryPath: Unable to find Property File for: " + "dataTemplatePath");
                logger.error(e.toString());
            }
        }
        return dataTemplatePath;
    }
}
