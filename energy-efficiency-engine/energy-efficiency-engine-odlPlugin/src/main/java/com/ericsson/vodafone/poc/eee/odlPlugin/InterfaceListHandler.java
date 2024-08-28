package com.ericsson.vodafone.poc.eee.odlPlugin;

import com.ericsson.vodafone.poc.eee.odlPlugin.utils.InterfaceData;
import com.ericsson.vodafone.poc.eee.odlPlugin.utils.JsonHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by esimalb on 8/28/17.
 */
public class InterfaceListHandler {
    private String networkRef = "";
    private List<InterfaceData> interfaceDataList = null;

    private static final Logger logger = LoggerFactory.getLogger(JsonHandler.class);

    InterfaceListHandler(String network, List<InterfaceData> interfaceData) {
        networkRef = network;
        setInterfaceDataList(interfaceData);
    }

    public String getNetworkRef() {
        return networkRef;
    }

    public void setNetworkRef(String networkRef) {
        this.networkRef = networkRef;
    }

    public List<InterfaceData> getInterfaceDataList() {
        return interfaceDataList;
    }

    public void setInterfaceDataList(List<InterfaceData> interfaceDataList) {
        this.interfaceDataList = interfaceDataList;
    }
}
