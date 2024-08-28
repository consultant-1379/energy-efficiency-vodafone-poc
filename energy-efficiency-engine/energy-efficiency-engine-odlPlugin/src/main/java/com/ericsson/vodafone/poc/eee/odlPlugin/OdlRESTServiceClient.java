package com.ericsson.vodafone.poc.eee.odlPlugin;

import com.ericsson.vodafone.poc.eee.odlPlugin.exception.HttpURLConnectionFailException;
import com.ericsson.vodafone.poc.eee.odlPlugin.exception.OdlOperationFailureException;

/**
 * Created by esimalb on 8/18/17.
 */
public interface OdlRESTServiceClient {


    // ***** Configure Topology  - network-topology.json
    // - PUT http://localhost:8181/restconf/config/ietf-network:networks/network/mini-link-topo
    //   > jsonFileName = "network-topology.json"
    //   > networkId = "mini-link-topo" (non lo passo come paramentro, lo prendo dal file jsonFileName)
    public void configureTopology(String jsonFileName)throws HttpURLConnectionFailException, OdlOperationFailureException;

    // - GET http://localhost:8181/restconf/config/ietf-network:networks/network/mini-link-topo
    //   > networkId = "mini-link-topo"
    public boolean isTopologyAlreadyConfigured(String jsonFileName)throws HttpURLConnectionFailException, OdlOperationFailureException;

    // ***** Get interfaces list (List all interfaces housed into the specified network)
    // - POST http://localhost:8181/restconf/operations/dynamic-data-collector:get-interface-list
    //Nota: per il poc abbiamo una sola network, quindi al momento non servirà passare il networkRefField
    //il networkRefField viene ritornato ed EEE deve salvarlo per effettuare le richieste seguenti
    //che sono già generiche
    public InterfaceListHandler getInterfaceDataList() throws HttpURLConnectionFailException, OdlOperationFailureException;

    // ***** Set unset interface rate monitoring (Enable-Disable InterfaceData Rate Monitoring)
    // - POST http://localhost:8181/restconf/operations/dynamic-data-collector:set-interface-rate-monitoring
    //   > networkRefField: "mini-link-topo"
    //   > ifRefField: "mini-link-1:1/3/5" (Introduces the reference to the termination point housed into ietf-interface instance)
    //   > collectionInterval: "60" (Statistics collection time interval in seconds)
    //   > monitoringEnable: "true"/"false" (Enable/Disable - Flag to indicate monitoring enable state)
    //   > historyLength: "2" (Monitored values history records length)
    public void enableDisableInterfaceRateMonitoring(String networkRefField, String ifRefField,String collectionInterval,
                                                      String monitoringEnable, String historyLength) throws HttpURLConnectionFailException, OdlOperationFailureException;

    // ***** Set interface rate (Configure the interface tx bandwidth)
    // - POST http://localhost:8181/restconf/operations/radio-link-configurator:set-interface-rate
    //   > networkRefField: "mini-link-topo"
    //   > ifRefField: "mini-link-1:1/3/5" (Introduces the reference to the termination point housed into ietf-interface instance)
    //   > rate: "10000000" (Required interface rate in bytes/s)
    public void setInterfaceCurrentCapacity(String networkRefField, String ifRefField, Long rateField) throws HttpURLConnectionFailException, OdlOperationFailureException;

    // ***** Get interface monitored data  (Read current monitored data)
    // - POST http://localhost:8181/restconf/operations/dynamic-data-collector:get-interface-monitored-data
    //   > networkRefField: "mini-link-topo"
    //   > ifRefField: "mini-link-1:1/2/3" (Introduces the reference to the termination point housed into ietf-interface instance)
    public MonitoredRateHandler getInterfaceMonitoredData(String networkRefField,String ifRefField) throws HttpURLConnectionFailException, OdlOperationFailureException;
}
