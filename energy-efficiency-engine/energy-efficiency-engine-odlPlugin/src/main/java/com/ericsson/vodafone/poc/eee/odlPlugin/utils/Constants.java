package com.ericsson.vodafone.poc.eee.odlPlugin.utils;

public class Constants {
    public static final String JSON_FILE_PATH_PROP = "jsonFilePath";
    public static final String DATA_TEMPLATE_PATH_PROP = "dataTemplatePath";

    //ConnectionInfo
    public static final String USER_NAME = "admin";
    public static final String PASSWORD = "admin";
    public static final String ACCEPT = "Accept";
    public static final String CONTENT_TYPE_VALUE = "application/json";
    public static final String CONTENT_TYPE = "Content-type";
    public static final String AUTHORIZATION_BASIC = "Basic";
    public static final String AUTHORIZATION = "Authorization";

    //Uri
    //PUT/GET/DELETE
    public static final String CONFIG_URI = "http://localhost:8181/restconf/config";
    // - put/get command
    public static final String TOPOLOGY_URI = "/ietf-network:networks/network/";
    public static final String MIB_OBJECT_OID_URI = "/snmp-agent:mib-object-oid";
    public static final String VPN_SERVICE_POC_URI = "/vpn-service-poc:e-lines";

    //GET
    public static final String OPERATIONAL_URI = "http://localhost:8181/restconf/operational";
    // - get command
    public static final String GET_TOPOLOGY_FROM_OPERATIONAL_URI = "/ietf-network:networks/network/";

    //POST
    public static final String OPERATIONS_URI = "http://localhost:8181/restconf/operations";
    // - post command
    public static final String SET_INTERFACE_CAPACITY_URI = "/radio-link-configurator:set-interface-capacity";
    public static final String GET_INTERFACE_MONITORES_DATA_URI = "/dynamic-data-collector:get-interface-monitored-data";
    public static final String GET_INTERFACE_LIST_URI = "/dynamic-data-collector:get-interface-list";
    public static final String ENABLE_DISABLE_INTERFACE_RATE_MONITORING_URI = "/dynamic-data-collector:set-interface-rate-monitoring";

    //DataTemplate
    public static final String SET_INTERFACE_CAPACITY_TEMPLATE = "SetInterfaceCapacityTemplate.txt";
    public static final String GET_INTERFACE_MONITORED_DATA_TEMPLATE = "GetInterfaceMonitoredDataTemplate.txt";
    public static final String GET_LIST_DATA_TEMPLATE = "GetInterfaceListDataTemplate.txt";
    public static final String SET_INTERFACE_RATE_MONITORING_DATA_TEMPLATE = "SetInterfaceRateMonitoringDataTemplate.txt";

    public static final String NETWORK_TOPOLOGY_JSON_FILE_NAME_FOR_TEST = "network-topology.json";
}
