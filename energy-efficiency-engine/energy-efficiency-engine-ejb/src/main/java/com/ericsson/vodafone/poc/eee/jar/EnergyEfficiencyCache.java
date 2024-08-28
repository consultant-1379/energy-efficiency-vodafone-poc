package com.ericsson.vodafone.poc.eee.jar;

import static com.ericsson.vodafone.poc.eee.jar.EnergyEfficiencyEngine.createJobKey;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.vodafone.poc.eee.jar.utils.IfMonitoredState;
import com.ericsson.vodafone.poc.eee.odlPlugin.utils.InterfaceData;

public class EnergyEfficiencyCache {

    private static EnergyEfficiencyCache instance = null;

    private Logger logger = LoggerFactory.getLogger(EnergyEfficiencyEngine.class);
    private Map <String, EnergyEfficiencyJobData> jobDataMap;

    private EnergyEfficiencyCache() {
        jobDataMap = new HashMap<>();
    }

    public static EnergyEfficiencyCache getInstance() {
        if(instance == null) {
            instance = new EnergyEfficiencyCache();
        }
        return instance;
    }

    public void clearEnergyEfficiencyJobDataInCache() {
        jobDataMap.clear();
    }

    public EnergyEfficiencyJobData get(final String jobKey) {
        return jobDataMap.get(jobKey);
    }

    public void put(final String jobKey, final EnergyEfficiencyJobData data) {
        logger.info("*** EnergyEfficiencyCache - put EnergyEfficiencyJobData with jobKey {}", jobKey);
        jobDataMap.put(jobKey, data);
    }

    public Set<String> getKeySet() {
        return this.jobDataMap.keySet();
    }

    public EnergyEfficiencyJobData remove(final String jobKey) {
        return this.jobDataMap.remove(jobKey);
    }




    public InterfaceData getInterfaceData(final String ifRef, final String networkRef) {
    final String jobKey = createJobKey(ifRef, networkRef);
        EnergyEfficiencyJobData energyEfficiencyJobData = jobDataMap.get(jobKey);
        return energyEfficiencyJobData.getInterfaceData();
    }

    /*public boolean putInterfaceDataInMap(final String jobKey,final InterfaceData data) {
        if(jobDataMap.containsKey(jobKey)) {
            EnergyEfficiencyJobData energyEfficiencyJobData = jobDataMap.get(jobKey);
            energyEfficiencyJobData.setInterfaceData(data);
            jobDataMap.put(jobKey, energyEfficiencyJobData);
            return true;
        }
        return false;
    }*/

    public boolean updateIfMonitoredState(final String ifRef, final String networkRef, final IfMonitoredState ifMonitoredState) {
        final String jobKey = createJobKey(ifRef, networkRef);
        if(jobDataMap.containsKey(jobKey)) {
            EnergyEfficiencyJobData energyEfficiencyJobData = jobDataMap.get(jobKey);
            InterfaceData interfaceData = energyEfficiencyJobData.getInterfaceData();
            interfaceData.setIfMonitoredState(ifMonitoredState);
            energyEfficiencyJobData.setInterfaceData(interfaceData);
            jobDataMap.put(jobKey, energyEfficiencyJobData);
            return true;
        }
        return false;
    }

    public IfMonitoredState getIfMonitoredState(final String ifRef, final String networkRef) {
        final String jobKey = createJobKey(ifRef, networkRef);
        if(jobDataMap.containsKey(jobKey)) {
            EnergyEfficiencyJobData energyEfficiencyJobData = jobDataMap.get(jobKey);
            return energyEfficiencyJobData.getInterfaceData().getIfMonitoredState();
        }
        return IfMonitoredState.DISABLE;
    }

    /*public boolean updateCurrentCapacity(final String jobKey, final long currentCapacity) {
        if(jobDataMap.containsKey(jobKey)) {
            EnergyEfficiencyJobData energyEfficiencyJobData = jobDataMap.get(jobKey);
            InterfaceData interfaceData = energyEfficiencyJobData.getInterfaceData();
            interfaceData.setIfCurrentCapacity(currentCapacity);
            energyEfficiencyJobData.setInterfaceData(interfaceData);
            jobDataMap.put(jobKey, energyEfficiencyJobData);
            return true;
        }
        return false;
    }
    */

}
