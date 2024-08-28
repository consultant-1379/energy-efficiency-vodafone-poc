package com.ericsson.vodafone.poc.eee.jar;

import static com.ericsson.vodafone.poc.eee.jar.utils.Constants.ENGINE_DEFAULT_SAMPLING_INTERVAL_IN_SECONDS;

import java.io.FileNotFoundException;
import java.util.*;

import com.ericsson.vodafone.poc.eee.dm.Decision;
import com.ericsson.vodafone.poc.eee.dm.DecisionMaker;
import com.ericsson.vodafone.poc.eee.jar.exception.ConfiguredBandwidthUnavailableException;
import com.ericsson.vodafone.poc.eee.jar.exception.MonitoredValuesUnavailableException;
import com.ericsson.vodafone.poc.eee.jar.utils.*;
import com.ericsson.vodafone.poc.eee.odlPlugin.utils.InterfaceData;
import com.ericsson.vodafone.poc.predictor.api.Predictable;
import com.ericsson.vodafone.poc.predictor.api.exception.PredictionUnavailableException;
import com.ericsson.vodafone.poc.predictor.api.exception.SerieOperationException;
import com.ericsson.vodafone.poc.predictor.api.utils.Prediction;
import com.ericsson.vodafone.poc.predictor.api.utils.Serie;

public class EnergyEfficiencyJobData<T> {

    private Predictable predictable;
    private Integer samplingIntervalInSeconds;
    private DecisionMaker decisionMaker;
    private InterfaceData interfaceData;

    // Prediction
    private Serie<T> monitoredValues;
    private Serie<Prediction<T>> prediction;
    private Serie<T> configuredBandwidthValues;
    private MPAlignment mpAlignment;

    // Savings
    private Serie<SavingData> savingDataValues;

    public EnergyEfficiencyJobData() {
        this.mpAlignment = new MPAlignment();
        setSamplingIntervalInSeconds(0);

        final PropertiesReader pReader = new PropertiesReader();
        try {
            setSamplingIntervalInSeconds(new Integer(pReader.loadProperty(ENGINE_DEFAULT_SAMPLING_INTERVAL_IN_SECONDS)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    Predictable getPredictable() {
        return predictable;
    }

    public void setPredictable(final Predictable predictable) throws PredictionUnavailableException {
        this.predictable = predictable;
        prediction = this.predictable.getPrediction();
    }

    public DecisionMaker getDecisionMaker() {
        return decisionMaker;
    }

    public void setDecisionMaker(final DecisionMaker decisionMaker) {
        this.decisionMaker = decisionMaker;
    }

    public Serie<Prediction<T>> getPrediction() throws PredictionUnavailableException {
        if(prediction == null || prediction.getSize() == 0) {
            throw new PredictionUnavailableException("Prediction not available - null");
        }
        return prediction;
    }

    public boolean isPredictionAvailable() {
        if(prediction == null || prediction.getSize() == 0) {
           return false;
        }
        return true;
    }

    public void triggerPrediction() throws PredictionUnavailableException{
        prediction = this.predictable.getPrediction();
    }


    public Prediction<T> getCurrentPrediction() throws PredictionUnavailableException {
        if(getMpAlignment().isAligned()) {
            return getPrediction().getSerie().get(getMpAlignment().getPredictedIndex());
        }
        else {
            return null;
        }
    }

    public MPAlignment getMpAlignment() {
        return mpAlignment;
    }

    public void setMpAlignment(final MPAlignment mpAlignment) {
        this.mpAlignment = mpAlignment;
    }

    public InterfaceData getInterfaceData() {
        return interfaceData;
    }

    public void setInterfaceData(InterfaceData interfaceData) {
        this.interfaceData = interfaceData;
    }

    public Serie<T> getMonitoredValues() throws MonitoredValuesUnavailableException {
        if(monitoredValues ==  null || monitoredValues.getSize() == 0)
            throw new MonitoredValuesUnavailableException();
        return monitoredValues;
    }

    public void setMonitoredValues(Serie<T> monitoredValues) {
        this.monitoredValues = monitoredValues;
    }

    public void pushMonitoredValue(T t) throws SerieOperationException {
        monitoredValues.push(t);
    }

    public void pushMonitoredValue(T t, final Date lastSampleDate) throws SerieOperationException {
        monitoredValues.push(t, lastSampleDate);
    }

    public void pushMonitoredValues(Serie<T> s) throws SerieOperationException {
        monitoredValues.push(s);
    }

    public void pushMonitoredValues(Serie<T> s, final Date lastSampleDate) throws SerieOperationException {
        monitoredValues.push(s, lastSampleDate);
    }

    /* CONFIGURED BANDWIDTH */
    public Serie<T> getConfiguredBandwidthValues() throws ConfiguredBandwidthUnavailableException {
        if(configuredBandwidthValues ==  null || configuredBandwidthValues.getSize() == 0)
            throw new ConfiguredBandwidthUnavailableException();
        return configuredBandwidthValues;
    }

    public void setConfiguredBandwidthValues(final Serie<T> configuredBandwidthValues) {
        this.configuredBandwidthValues = configuredBandwidthValues;
    }

    public void pushConfiguredBandwidthValue(T t) throws SerieOperationException {
        configuredBandwidthValues.push(t);
    }

    public void pushConfiguredBandwidthValue(T t, final Date lastSampleDate) throws SerieOperationException {
        configuredBandwidthValues.push(t, lastSampleDate);
    }

    public void pushConfiguredBandwidthValues(Serie<T> c) throws SerieOperationException {
        configuredBandwidthValues.push(c);
    }

    public void pushConfiguredBandwidthValues(Serie<T> c, final Date lastSampleDate) throws SerieOperationException {
        configuredBandwidthValues.push(c, lastSampleDate);
    }

    public Decision makeDecision(String ifRef,String networkRef) {
        Pair<Integer, Integer> alignment = new Pair<>(mpAlignment.getMonitoredIndex(), mpAlignment.getPredictedIndex());
        return decisionMaker.makeDecision(monitoredValues, prediction, alignment, ifRef, networkRef);
    }

    public String monitoredValuesToString() {
        String data = " MonitoredValues: "+ "\n " + monitoredValues.getSerie().toString();
        data += "\n FirstSampleDate " + monitoredValues.getFirstSampleDate();
        data += "\n LastSampleDate " + monitoredValues.getLastSampleDate();
        data += "\n Size: " + monitoredValues.getSerie().size();
        return data;
    }

    public String predictionToString() {
        int count = 0;
        String pred = " Prediction: " + "\n";
        if(prediction != null){
            for(Prediction value: prediction.getSerie()) {
                count++;
                pred += "Sample: " + count + " " + value.toString() + "\n";
            }
            pred += " FirstSampleDate: " + prediction.getFirstSampleDate()+ "\n";
            pred += " LastSampleDate: " + prediction.getLastSampleDate()+ "\n";
        }
        else {
            pred = "EMPTY" + "\n";
        }

        return pred;
    }

    public String jobDataToStringWithHistoryFile() {
        String output = "\n" + interfaceData.toString() + "\n";
        output += "SamplingIntervalInSeconds: " + getSamplingIntervalInSeconds() + "\n";

        String id_ifRefField = interfaceData.getIfRef();
        String fileName = id_ifRefField.replace('/', '_');
        fileName = fileName.replace(':', '_');
        fileName = fileName + ".txt";

        CsvHistoryReader reader = new CsvHistoryReader(fileName, 3, new Date(), getSamplingIntervalInSeconds());

        int count = 0;
        output += "Prediction: " + "\n";
        if(prediction != null){
            for(Prediction value: prediction.getSerie()) {
                output += "Sample: " + count + " " + value.toString() + "\n";
                try {
                    Triplet<Double, Double, Double> tripletOfHistoryValues = null;
                    try {
                        tripletOfHistoryValues = reader.getHistorySamples(count);
                    } catch (SerieOperationException e) {
                        e.printStackTrace();
                    }

                    output += " FirstWeek " + tripletOfHistoryValues.first +
                            "\tSecondWeek " + tripletOfHistoryValues.second +
                            "\tThirdWeek "+ tripletOfHistoryValues.third + "\n";

                    count++;

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            output += "\nFirstSampleDate: " + prediction.getFirstSampleDate()+ "\n";
            output += "LastSampleDate: " + prediction.getLastSampleDate()+ "\n";
        }
        else {
            output = "EMPTY" + "\n";
        }

        output += "\n" + monitoredValuesToString() + "\n";
        output += "\n" + savingDataValuesToString() + "\n";
        output += "\n MpAlignment: " + mpAlignment.toString() + "\n";

        return output;
    }

    public String jobDataToString() {
        String output = "\n" + interfaceData.toString() + "\n";
        output += "SamplingIntervalInSeconds: " + getSamplingIntervalInSeconds() + "\n";

        int count = 0;
        output += "Prediction: " + "\n";
        if(prediction != null){
            for(Prediction value: prediction.getSerie()) {
                output += "Sample: " + count + " " + value.toString() + "\n";
                count++;
            }
            output += "\nFirstSampleDate: " + prediction.getFirstSampleDate()+ "\n";
            output += "LastSampleDate: " + prediction.getLastSampleDate()+ "\n";
        }
        else {
            output = "EMPTY" + "\n";
        }

        output += "\n" + monitoredValuesToString() + "\n";
        output += "\n" + savingDataValuesToString() + "\n";
        output += "\n MpAlignment: " + mpAlignment.toString() + "\n";

        return output;
    }

    public Serie<SavingData> getSavingDataValues() {
        return savingDataValues;
    }

    public void setSavingDataValues(Serie<SavingData> savingDataValues) {
        this.savingDataValues = savingDataValues;
    }

    public void clearSavingDataValues() {
        this.savingDataValues.clear();
    }

    public Double totalPeriodicEnergySavingPercentage() {
        List<SavingData> savingDataList = getSavingDataValues().getSerie();
        return getHistoricalEnergySavingPercentageFromListInDouble(savingDataList);
    }

    public Double lastWeekEnergySavingPercentage () {
        Date now = new Date(System.currentTimeMillis());
        Calendar startDate = Calendar.getInstance();
        startDate.setTime(now);
        startDate.add(Calendar.DATE, -7);

        return getHistoricalEnergySavingPercentage(startDate.getTime());
    }

    public Double last24hEnergySavingPercentage () {
        Date now = new Date(System.currentTimeMillis());
        Calendar startDate = Calendar.getInstance();
        startDate.setTime(now);
        startDate.add(Calendar.DATE, -1);

        return getHistoricalEnergySavingPercentage(startDate.getTime());
    }

    public Double getHistoricalEnergySavingPercentage(Date startDate) {
        if(savingDataValues ==  null || savingDataValues.getSize() == 0) {
            return null;
        }

        long firstSampleDate = savingDataValues.getFirstSampleDate().getTime();

        List<SavingData> savingDataList;
        if(firstSampleDate >  startDate.getTime()) {
            savingDataList  = getSavingDataValues().getSerie();
        }
        else {
            savingDataList = getSavingDataValues().getSerieAfter(startDate, true);
        }
        return getHistoricalEnergySavingPercentageFromListInDouble(savingDataList);
    }

    public Double getCurrentEnergySavingPercentage() {
        if(savingDataValues ==  null || savingDataValues.getSize() == 0) {
            return null;
        }
        return savingDataValues.getLastElement().currentSavingPercentageInDouble();
    }

    public Double getHistoricalEnergySavingPercentageFromListInDouble(List<SavingData> savingDataList) {
        if(savingDataList.size() == 0) {
            return null;
        }
        Long sum = 0L;
        for(SavingData savingData:  savingDataList) {
            sum += savingData.currentSavingPercentageInLong();
        }

        Long percentage = Double.valueOf(sum.doubleValue()/savingDataList.size()).longValue();
        return (percentage.doubleValue())/100;
    }

    public void pushSavingDataValues(Long currentTxPower, Long nominalTxPower, final Date lastSampleDate) throws SerieOperationException {
        SavingData savingData = new SavingData(currentTxPower, nominalTxPower);
        savingDataValues.push(savingData,lastSampleDate);
    }

    public void pushSavingDataValues(Long currentTxPower, Long nominalTxPower) throws SerieOperationException {
        SavingData savingData = new SavingData(currentTxPower, nominalTxPower);
        savingDataValues.push(savingData);
    }

    public String savingDataValuesToString() {
        String data = " Saving data: " + getSavingDataValues().getSerie().toString();
        data += "\n Last24hEnergySavingPercentage " + last24hEnergySavingPercentage();
        data += "\n FirstSampleDate " + savingDataValues.getFirstSampleDate();
        data += "\n LastSampleDate " + savingDataValues.getLastSampleDate();
        return data;
    }


    public T getCurrentBandWidth() throws MonitoredValuesUnavailableException {
        if(getMpAlignment().isAligned()) {
            return getMonitoredValues().getSerie().get(getMpAlignment().getMonitoredIndex());
        }
        else {
            return null;
        }
    }

    public T getCurrentConfiguredBandWidth() throws ConfiguredBandwidthUnavailableException {
        if(getMpAlignment().isAligned()) {
            return getConfiguredBandwidthValues().getSerie().get(getMpAlignment().getMonitoredIndex());
        }
        else {
            return null;
        }
    }

    public Integer getSamplingIntervalInSeconds() {
        return samplingIntervalInSeconds;
    }

    public void setSamplingIntervalInSeconds(Integer samplingIntervalInSeconds) {
        this.samplingIntervalInSeconds = samplingIntervalInSeconds;
    }
}
