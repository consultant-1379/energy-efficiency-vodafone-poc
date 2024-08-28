package com.ericsson.vodafone.poc.eee.service.input.rest.data.utils;

import static com.ericsson.vodafone.poc.eee.jar.EnergyEfficiencyEngine.convertByte_sInMb_s;

import com.ericsson.vodafone.poc.eee.jar.EnergyEfficiencyCache;
import com.ericsson.vodafone.poc.eee.jar.EnergyEfficiencyEngine;
import com.ericsson.vodafone.poc.eee.jar.EnergyEfficiencyJobData;
import com.ericsson.vodafone.poc.eee.jar.exception.ConfiguredBandwidthUnavailableException;
import com.ericsson.vodafone.poc.eee.jar.exception.MonitoredValuesUnavailableException;
import com.ericsson.vodafone.poc.eee.jar.utils.IfMonitoredState;
import com.ericsson.vodafone.poc.eee.service.input.rest.data.*;
import com.ericsson.vodafone.poc.predictor.api.exception.PredictionUnavailableException;
import com.ericsson.vodafone.poc.predictor.api.utils.Pair;
import com.ericsson.vodafone.poc.predictor.api.utils.Prediction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class EEEDataProvider {

    private static Logger logger = LoggerFactory.getLogger(EEEDataProvider.class);

    private static EnergyEfficiencyCache eeeData = EnergyEfficiencyCache.getInstance();

    public static Collection<InterfaceItem> getInterfaceItemList() {

        logger.debug("EEEDataProvider: CALL TO getInterfaceItemList()");
        List<InterfaceItem> interfaceItems = new ArrayList<>();
        Set<String> keySet = eeeData.getKeySet();
        for (String key : keySet) {
            interfaceItems.add(getInterfaceItem(eeeData.get(key)));
        }
        return interfaceItems;
    }

    public static InterfaceItem getInterfaceItem(final String ifRef, final String networkRef) {
        EnergyEfficiencyJobData d = eeeData.get(EnergyEfficiencyEngine.createJobKey(ifRef, networkRef));
        return getInterfaceItem(d);
    }

    private static InterfaceItem getInterfaceItem(final EnergyEfficiencyJobData d) {
        InterfaceItem ifItem = new InterfaceItem();

        ifItem.setIndex(0);
        ifItem.setTime(new Date().getTime());

        if (d != null) {

            // Interface Coordinates
            ifItem.setIfRef(d.getInterfaceData().getIfRef());
            ifItem.setNetworkRef(d.getInterfaceData().getNetworkRef());

            ifItem.setTimeInterval(d.getSamplingIntervalInSeconds().longValue() * 1000);

            // TIMING DATA
            Long time = null;

            // Monitored state and capacity
            if (d.getInterfaceData().getIfMonitoredState() == IfMonitoredState.MONITORING) {
                logger.debug("getInterfaceItem: interface {} monitored", d.getInterfaceData().getIfRef());

                ifItem.setIfMonitoredState(Integer.valueOf(1));

                try {
                    time = d.getMonitoredValues().getLastSampleDate().getTime();
                } catch (MonitoredValuesUnavailableException e) {
                    e.printStackTrace();
                }

                ifItem.setIfCurrentCapacity(convertByte_sInMb_s(d.getInterfaceData().getIfCurrentCapacity()).longValue());
                ifItem.setIfMaximumCapacity(convertByte_sInMb_s(d.getInterfaceData().getIfMaximumCapacity()).longValue());

                //Current and predicted Bandwidth
                try {
                    Double currentBandWidth = (Double) d.getCurrentBandWidth();
                    if (currentBandWidth != null) {
                        ObservationItem currOItem = buildObservation(0, time, (Double) d.getCurrentBandWidth());
                        ifItem.setIfTrafficBandwidth(currOItem);
                    }
                } catch (MonitoredValuesUnavailableException e) {
                    e.printStackTrace();
                }
                try {
                    Prediction p = d.getCurrentPrediction();
                    if (p != null) {
                        PredictionItem currPitem = buildPrediction(0, time, d.getCurrentPrediction());
                        ifItem.setIfPredictedTrafficBandwidth(currPitem);
                    }
                } catch (PredictionUnavailableException e) {
                    e.printStackTrace();
                }

                //Saving data
                ifItem.setIfSaving(buildInterfaceItemSavingData(0, time, d));

            } else {
                logger.debug("getInterfaceItem: interface {} NOT monitored", d.getInterfaceData().getIfRef());
                ifItem.setIfMonitoredState(Integer.valueOf(0));
            }
        }
        else {
            logger.debug("getInterfaceItem: interface {} NOT present in cache");
            ifItem.setIfMonitoredState(Integer.valueOf(0));
        }
        return ifItem;
    }

    public static InterfaceItem getInterfaceItem(final String ifRef, final String networkRef, final Date from, final int numOfSamples) {

        EnergyEfficiencyJobData d = eeeData.get(EnergyEfficiencyEngine.createJobKey(ifRef, networkRef));

        Date until = new Date(from.getTime() + (d.getSamplingIntervalInSeconds() * 1000 * numOfSamples));
        return getInterfaceItem(d, from, until);
    }

    public static InterfaceItem getInterfaceItem(final String ifRef, final String networkRef, final Date from, final Date until) {
        EnergyEfficiencyJobData d = eeeData.get(EnergyEfficiencyEngine.createJobKey(ifRef, networkRef));
        return getInterfaceItem(d, from, until);
    }

    public static InterfaceItem getInterfaceItem(final String ifRef, final String networkRef, final Date from, final int numOfSamples,
                                                 final Long aggregation) {
        InterfaceItem ifItem = getInterfaceItem(ifRef, networkRef, from, numOfSamples);
        if (ifItem == null || (ifItem.getIfMonitoredState() == 0)) {
            return ifItem;
        }
        InterfaceItem aggregatedIfItem = aggregateInterfaceItem(ifItem, aggregation);
        return aggregatedIfItem;
    }

    public static InterfaceItem getInterfaceItem(final String ifRef, final String networkRef, final Date from, final Date until,
                                                 final Long aggregation) {
        InterfaceItem ifItem = getInterfaceItem(ifRef, networkRef, from, until);
        if (ifItem == null || (ifItem.getIfMonitoredState() == 0)) {
            return ifItem;
        }
        InterfaceItem aggregatedIfItem = aggregateInterfaceItem(ifItem, aggregation);
        return aggregatedIfItem;
    }

    private static InterfaceItem getInterfaceItem(final EnergyEfficiencyJobData d, final Date from, final Date until) {
        InterfaceItem ifItem = getInterfaceItem(d);
        if (ifItem == null || (ifItem.getIfMonitoredState() == 0)) {
            return ifItem;
        }

        try {
            ifItem.setIfPredictionList(getIfPredictionList(d, from, until));
        } catch (PredictionUnavailableException e) {
            logger.info("getInterfaceItem: prediction not available on interface {} from time {} to time {} ",
                    d.getInterfaceData().getIfRef(), from.toString(), until.toString());
            //e.printStackTrace();
        }

        List<ObservationItem> oiList = null;
        try {
            oiList = getIfObservationList(d, from, until);
            ifItem.setIfObservationList(oiList);
        } catch (MonitoredValuesUnavailableException e) {
            logger.info("getInterfaceItem: observation not available on interface {} from time {} to time (now) {} ",
                    d.getInterfaceData().getIfRef(), from.toString(), until.toString());
            //e.printStackTrace();
        }

        List<ConfiguredBandwidthItem> ciList = null;
        try {
            ciList = getIfConfiguredBandwidthList(d, from, until);
            ifItem.setIfConfiguredBandwidthList(ciList);
        } catch (ConfiguredBandwidthUnavailableException e) {
            logger.info("getInterfaceItem: configured bandwidth not available on interface {} from time {} to time (now) {} ",
                    d.getInterfaceData().getIfRef(), from.toString(), until.toString());
            //e.printStackTrace();
        }
        return ifItem;
    }

    public static InterfaceItem getInterfaceItemSavingData(final String ifRef, final String networkRef) {
        EnergyEfficiencyJobData d = eeeData.get(EnergyEfficiencyEngine.createJobKey(ifRef, networkRef));
        InterfaceItem ifItem = new InterfaceItem();
        Long time = null;
        try {
            time = d.getMonitoredValues().getLastSampleDate().getTime();
        } catch (MonitoredValuesUnavailableException e) {
            e.printStackTrace();
        }
        ifItem.setIfSaving(buildInterfaceItemSavingData(0, time, d));
        return ifItem;
    }

    private static SavingItem buildInterfaceItemSavingData(final Integer index, final Long time, final EnergyEfficiencyJobData d) {

        if (d == null || (d.getInterfaceData().getIfMonitoredState() != IfMonitoredState.MONITORING)) {
            return null;
        }

        SavingItem savingItem = new SavingItem();
        savingItem.setIndex(index);
        savingItem.setTime(time);
        savingItem.setCurrentSavingPercentage(d.getCurrentEnergySavingPercentage());
        savingItem.setHistoricalSavingPercentage(d.last24hEnergySavingPercentage());
        savingItem.setHistoricalSavingPeriod(1); //24H
        return savingItem;
    }

    private static List<PredictionItem> getIfPredictionList(final EnergyEfficiencyJobData d, final Date pFrom, final Date pUntil)
            throws PredictionUnavailableException {
        List<Prediction> pList = d.getPrediction().getSerieBetween(pFrom, pUntil, false);
        if (pList == null || pList.size() == 0) {
            throw new PredictionUnavailableException("Empty or null Prediction List");
        }
        final Pair<Date, Date> datesOfSerieBetween = d.getPrediction().getDatesOfSerieBetween(pFrom, pUntil, false);
        final Long firstSampleTime = datesOfSerieBetween.getLeft().getTime();
        final Long samplingInterval = d.getPrediction().getSamplingIntervalSeconds() * 1000;
        return EEEDataProvider.buildPredictionList(pList, firstSampleTime, samplingInterval);
    }

    private static List<ObservationItem> getIfObservationList(final EnergyEfficiencyJobData d, final Date oFrom, final Date oUntil)
            throws MonitoredValuesUnavailableException {
        List<Double> oList = d.getMonitoredValues().getSerieBetween(oFrom, oUntil, false);
        if (oList == null || oList.size() == 0) {
            throw new MonitoredValuesUnavailableException("Empty or null MonitoredValues List");
        }
        final Pair<Date, Date> datesOfSerieBetween = d.getMonitoredValues().getDatesOfSerieBetween(oFrom, oUntil, false);
        final Long firstSampleTime = datesOfSerieBetween.getLeft().getTime();
        final Long samplingInterval = d.getMonitoredValues().getSamplingIntervalSeconds() * 1000;
        return buildObservationList(oList, firstSampleTime, samplingInterval);
    }

    private static List<ConfiguredBandwidthItem> getIfConfiguredBandwidthList(final EnergyEfficiencyJobData d, final Date oFrom, final Date oUntil)
            throws ConfiguredBandwidthUnavailableException {
        List<Double> cList = d.getConfiguredBandwidthValues().getSerieBetween(oFrom, oUntil, false);
        if (cList == null || cList.size() == 0) {
            throw new ConfiguredBandwidthUnavailableException("Empty or null ConfiguredBandwidthValues List");
        }
        final Pair<Date, Date> datesOfSerieBetween = d.getConfiguredBandwidthValues().getDatesOfSerieBetween(oFrom, oUntil, false);
        final Long firstSampleTime = datesOfSerieBetween.getLeft().getTime();
        final Long samplingInterval = d.getConfiguredBandwidthValues().getSamplingIntervalSeconds() * 1000;
        return buildConfiguredBandwidthList(cList, firstSampleTime, samplingInterval);
    }

    private static List<PredictionItem> buildPredictionList(final List<Prediction> pList, final Long firstSampleTime, final Long samplingInterval) {
        List<PredictionItem> pItemList = new ArrayList<>();
        for (int i = 0; i < pList.size(); i++) {
            Prediction currPrediction = pList.get(i);
            PredictionItem predictionItem = buildPrediction(i, firstSampleTime + (samplingInterval * i), currPrediction);
            pItemList.add(predictionItem);
        }
        return pItemList;
    }

    private static PredictionItem buildPrediction(Integer index, final Long time, final Prediction p) {
        PredictionItem pItem = new PredictionItem();
        pItem.setIndex(index);
        pItem.setPredictedValue(((Double) p.getPredictedValue()).longValue());
        pItem.setPredictedLowerThreshold(((Double) p.getSafeLowerThreshold()).longValue());
        pItem.setPredictedUpperThreshold(((Double) p.getSafeUpperThreshold()).longValue());
        pItem.setTime(time);
        return pItem;
    }

    private static List<ObservationItem> buildObservationList(final List<Double> oList, final Long firstSampleTime, final Long samplingInterval) {
        List<ObservationItem> oItemList = new ArrayList<>();
        for (int i = 0; i < oList.size(); i++) {
            Double currObservation = oList.get(i);
            ObservationItem observationItem = buildObservation(i, firstSampleTime + (samplingInterval * i), currObservation);
            oItemList.add(observationItem);
        }
        return oItemList;
    }

    private static ObservationItem buildObservation(final Integer index, final Long time, final Double o) {
        ObservationItem oItem = new ObservationItem();
        oItem.setIndex(index);
        oItem.setTime(time);
        oItem.setIfTrafficBandwidth(o.longValue());
        return oItem;
    }

    /* CONFIGURED BANDWIDTH */
    private static List<ConfiguredBandwidthItem> buildConfiguredBandwidthList(final List<Double> cList, final Long firstSampleTime, final Long samplingInterval) {
        List<ConfiguredBandwidthItem> cItemList = new ArrayList<>();
        for (int i = 0; i < cList.size(); i++) {
            Double currConfiguredBandwidth = cList.get(i);
            ConfiguredBandwidthItem configuredBandwidthItem = buildConfiguredBandwidth(i, firstSampleTime + (samplingInterval * i), currConfiguredBandwidth);
            cItemList.add(configuredBandwidthItem);
        }
        return cItemList;
    }

    private static ConfiguredBandwidthItem buildConfiguredBandwidth(final Integer index, final Long time, final Double o) {
        ConfiguredBandwidthItem cItem = new ConfiguredBandwidthItem();
        cItem.setIndex(index);
        cItem.setTime(time);
        cItem.setIfConfiguredBandwidth(o.longValue());
        return cItem;
    }

    /* VODAFONE POC */
    // this method can be private but for vodafone poc i need to use it in GetInterfaceHandler
    public static InterfaceItem aggregateInterfaceItem(final InterfaceItem ifItem, final Long aggregation) {

        List<PredictionItem> pItemList = new ArrayList<>();
        List<ObservationItem> oItemList = new ArrayList<>();
        List<ConfiguredBandwidthItem> cItemList = new ArrayList<>();

        InterfaceItem aggregatedIfItem = new InterfaceItem(ifItem);
        aggregatedIfItem.setTimeInterval(aggregation);

        if (ifItem.getIfPredictionListSize() > 0) {
            pItemList = ifItem.getIfPredictionList();
            List<Aggregable> pAggregable = new ArrayList<>(ifItem.getIfPredictionList());
            final List<List<Integer>> pItemAggregation = calculateAggregation(pAggregable, aggregation);
            List<PredictionItem> aggregatedPItemList = aggregatePredictionItem(pItemList, pItemAggregation);
            aggregatedIfItem.setIfPredictionList(aggregatedPItemList);

        }

        if (ifItem.getIfObservationListSize() > 0) {
            oItemList = ifItem.getIfObservationList();
            List<Aggregable> oAggregable = new ArrayList<>(ifItem.getIfObservationList());
            final List<List<Integer>> oItemAggregation = calculateAggregation(oAggregable, aggregation);
            List<ObservationItem> aggregatedOItemList = aggregateObservationItem(oItemList, oItemAggregation);
            aggregatedIfItem.setIfObservationList(aggregatedOItemList);
        }

        if (ifItem.getIfConfiguredBandwidthListSize() > 0) {
            cItemList = ifItem.getIfConfiguredBandwidthList();
            List<Aggregable> cAggregable = new ArrayList<>(ifItem.getIfConfiguredBandwidthList());
            final List<List<Integer>> cItemAggregation = calculateAggregation(cAggregable, aggregation);
            List<ConfiguredBandwidthItem> aggregatedCItemList = aggregateConfiguredBandwidthItem(cItemList, cItemAggregation);
            aggregatedIfItem.setIfConfiguredBandwidthList(aggregatedCItemList);
        }

        return aggregatedIfItem;
    }

    private static List<PredictionItem> aggregatePredictionItem(final List<PredictionItem> pItemList, final List<List<Integer>> pItemAggregation) {
        List<PredictionItem> res = new ArrayList<>();
        for (int i = 0; i < pItemAggregation.size(); i++) {

            List<Integer> a = pItemAggregation.get(i);
            PredictionItem aggregatedPItem = new PredictionItem();

            Long p = new Long(0);
            Long l = new Long(0);
            Long u = new Long(0);
            for (Integer index : a) {
                p += pItemList.get(index).getPredictedValue();
                l += pItemList.get(index).getPredictedLowerThreshold();
                u += pItemList.get(index).getPredictedUpperThreshold();
            }

            aggregatedPItem.setIndex(i);
            // assegno la data del primo campione dell'aggregazione
            // TODO occhio, perche i campioni aggregati potrebbero non essere equispaziati
            aggregatedPItem.setTime(pItemList.get(a.get(0)).getTime());
            aggregatedPItem.setPredictedValue(new Double(p / a.size()).longValue());
            aggregatedPItem.setPredictedLowerThreshold(new Double(l / a.size()).longValue());
            aggregatedPItem.setPredictedUpperThreshold(new Double(u / a.size()).longValue());

            res.add(i, aggregatedPItem);
        }
        return res;
    }

    private static List<ObservationItem> aggregateObservationItem(final List<ObservationItem> oItemList, final List<List<Integer>> oItemAggregation) {
        List<ObservationItem> res = new ArrayList<>();
        for (int i = 0; i < oItemAggregation.size(); i++) {

            List<Integer> a = oItemAggregation.get(i);
            ObservationItem aggregatedOItem = new ObservationItem();

            Long o = new Long(0);
            for (Integer index : a) {
                o += oItemList.get(index).getIfTrafficBandwidth();
            }
            aggregatedOItem.setIndex(i);
            aggregatedOItem.setTime(oItemList.get(a.get(0)).getTime());
            aggregatedOItem.setIfTrafficBandwidth(new Double(o / a.size()).longValue());

            res.add(i, aggregatedOItem);
        }

        return res;
    }

    private static List<ConfiguredBandwidthItem> aggregateConfiguredBandwidthItem(final List<ConfiguredBandwidthItem> cItemList, final List<List<Integer>> cItemAggregation) {
        List<ConfiguredBandwidthItem> res = new ArrayList<>();
        for (int i = 0; i < cItemAggregation.size(); i++) {

            List<Integer> a = cItemAggregation.get(i);
            ConfiguredBandwidthItem aggregatedCItem = new ConfiguredBandwidthItem();

            Long c = new Long(0);
            for (Integer index : a) {
                c += cItemList.get(index).getIfConfiguredBandwidth();
            }
            aggregatedCItem.setIndex(i);
            aggregatedCItem.setTime(cItemList.get(a.get(0)).getTime());
            aggregatedCItem.setIfConfiguredBandwidth(new Double(c / a.size()).longValue());

            res.add(i, aggregatedCItem);
        }

        return res;
    }

    private static List<List<Integer>> calculateAggregation(final List<Aggregable> input, final Long aggregation) {

        List<List<Integer>> output = new ArrayList<>();

        Long baseTime = input.get(0).getTime();
        int targetIndex = 0;
        List<Integer> indicesList = new ArrayList<>();

        for (int i = 0; i < input.size(); i++) {

            if (input.get(i).getTime() < baseTime + aggregation) {
                indicesList.add(i);
            } else {
                output.add(targetIndex, indicesList);
                targetIndex++;
                baseTime += aggregation;
                indicesList = new ArrayList<>();
                indicesList.add(i);
            }
        }
        output.add(targetIndex, indicesList);
        return output;
    }
}
