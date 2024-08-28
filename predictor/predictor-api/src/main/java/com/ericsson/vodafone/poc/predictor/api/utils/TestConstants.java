package com.ericsson.vodafone.poc.predictor.api.utils;

public class TestConstants {

    public static final String PREDICTOR_HISTORY_PATH_PROP = "predictorHistoryPath";
    public static final String PREDICTOR_HISTORY_DELIMITER_PROP = "predictorHistoryDelimiter";

    public static final String ENGINE_DEFAULT_SAMPLING_INTERVAL_IN_SECONDS = "engineSamplingIntervalInSeconds";
    public static final String ENGINE_DEFAULT_TRAFFIC_POLLER_INTERVAL_IN_SECONDS = "engineTrafficPollerIntervalInSeconds";

    public static final String ODL_DEFAULT_CONFIGURATION_POLLER_INTERVAL_IN_SECONDS = "odlConfigurationPollerIntervalInSeconds";
    public static final String ODL_DEFAULT_INTERFACE_MANAGEMENT_POLLER_INTERVAL_IN_SECONDS = "interfaceManagementPollerIntervalInSeconds";
    public static final String ODL_DEFAULT_COLLECTION_INTERVAL = "odlCollectionInterval";
    public static final String ODL_DEFAULT_HISTORY_LENGHT = "odlHistoryLength";

    public static final int EEE_JOB_NUM_OF_READ_RETRIES_ON_DUPLICATE_TIME_INTEVAL = 3;

    public static final String JOB_KEY_SEPARATOR = "_";
    public static final int JOB_KEY_ID_POS = 0;
    public static final int JOB_KEY_GROUP_POS = 1;

    public static final String JOB_DATA_MAP_JOBKEY = "jobKey";
    public static final String STARTUP_JOBKEY = "startup";

    public static final String NETWORK_TOPOLOGY_JSON_FILE_NAME = "network-topology.json";
    public static final String VPN_SERVICE_POC_JSON_FILE_NAME_FOR_TEST = "vpn-service-poc.json";
}
