import com.ericsson.vodafone.poc.eee.jar.EnergyEfficiencyCache
import com.ericsson.vodafone.poc.eee.jar.EnergyEfficiencyEngine
import com.ericsson.vodafone.poc.eee.jar.EnergyEfficiencyJobData
import com.ericsson.vodafone.poc.eee.jar.utils.MPAlignment
import com.ericsson.vodafone.poc.eee.odlPlugin.utils.InterfaceData
import com.ericsson.vodafone.poc.eee.service.input.rest.data.InterfaceItem
import com.ericsson.vodafone.poc.eee.service.input.rest.data.ObservationItem
import com.ericsson.vodafone.poc.eee.service.input.rest.data.PredictionItem
import com.ericsson.vodafone.poc.eee.service.input.rest.data.utils.EEEDataProvider
import com.ericsson.vodafone.poc.eee.jar.utils.IfMonitoredState
import com.ericsson.vodafone.poc.predictor.api.impl.DomainAgnosticIncidentPrediction
import com.ericsson.vodafone.poc.predictor.api.utils.Serie
import spock.lang.Shared
import spock.lang.Specification

/**
 * Created by esimalb on 12/1/17.
 */
class EEEDataProviderSpec extends Specification {

    EEEDataProvider eeeDataProvider = new EEEDataProvider()

    @Shared
    EnergyEfficiencyJobData eejd = new EnergyEfficiencyJobData()

    @Shared
    EnergyEfficiencyCache eeeData = EnergyEfficiencyCache.getInstance();

    //SamplingInterval in second for file mini-link-6351-1:LAN-1_1 is 60s - 504 samples in file
    static Long samplingIntervalInSeconds = 60L
    static int numOfSeries = 3
    static int sampleToInsert = 4

    static String stubbedIfRef = "stubbed_if_ref"
    static String stubbedNetworkRef = "stubbed_network_ref"
    static String stubbedHistoryFileName = "mini-link-6351-1_LAN-1_1.txt"

    static Long ifMaximumCapacity = 13984875L

    @Shared
    Long ifCurrentCapacity

    def setupSpec() {
        ifCurrentCapacity = 4661625L
    }

    def cleanup() {
        System.out.println("\ncleanup - clearEnergyEfficiencyJobDataInCache")
        eeeData.clearEnergyEfficiencyJobDataInCache()
    }

    def 'look for the stubbed InterfaceItem in the EnergyEfficiencyCache still empty'() {
        System.out.println("\nStarting Test 1")
        given: 'the EnergyEfficiencyCache empty'
        when: 'the stubbed InterfaceItem is not found in EnergyEfficiencyCache'
            InterfaceItem ifItem = eeeDataProvider.getInterfaceItem(stubbedIfRef, stubbedNetworkRef)
            System.out.println("\ncall getInterfaceItem(eejd) method\nReturned interfaceItem:\n\n" + ifItem.toString());
        then: 'ifMonitoredState value expected from GUI is 0: "not monitored"'
            ifItem.ifMonitoredState == Integer.valueOf(0)
        and: 'all other InterfaceItem fields are null'
            ifItem.getNetworkRef() == null
            ifItem.getIfRef() == null
            ifItem.getTimeInterval() == null

        System.out.println("\n done.")
    }

    def 'look for the stubbed InterfaceItem inserted in the EnergyEfficiencyCache with IfMonitoredState UNAVAILABLE'() {
        System.out.println("\nStarting Test 2")
        given: 'the stubbed InterfaceItem with IfMonitoredState UNAVAILABLE in the EnergyEfficiencyCache'
            InterfaceData interfaceData = new InterfaceData(stubbedNetworkRef, stubbedIfRef, ifMaximumCapacity, ifCurrentCapacity)
            interfaceData.setIfMonitoredState(IfMonitoredState.UNAVAILABLE)
            eejd.setInterfaceData(interfaceData)
            String jobKey = EnergyEfficiencyEngine.createJobKey(stubbedIfRef, stubbedNetworkRef)
            eeeData.put(jobKey, eejd)
        when: 'the stubbed interfaceItem is found in EnergyEfficiencyCache'
            InterfaceItem ifItem = eeeDataProvider.getInterfaceItem(stubbedIfRef, stubbedNetworkRef)
            System.out.println("\ncall getInterfaceItem(eejd) method\nReturned interfaceItem: \n\n" + ifItem.toString());
        then: 'ifMonitoredState value expected from GUI is 0: NOT MONITORED'
            ifItem.ifMonitoredState == Integer.valueOf(0)
        and: 'only NetworkRef and IfRef fields are set on ifItem'
            ifItem.getNetworkRef() == stubbedNetworkRef
            ifItem.getIfRef() == stubbedIfRef
        and: 'all other InterfaceItem fields are null or empty'
            ifItem.getIfMaximumCapacity() == null
            ifItem.getIfCurrentCapacity() == null
            ifItem.getIfTrafficBandwidth() == null
            ifItem.getIfPredictedTrafficBandwidth() == null
            ifItem.getIfObservationListSize() == Integer.valueOf(0)
            ifItem.getIfPredictionListSize() == Integer.valueOf(0)
            ifItem.getIfSaving() == null

        System.out.println("\n done.")
    }

    def 'extract current InterfaceItem from EnergyEfficiencyCache'() {
        System.out.println("\nStarting Test 3")
        given: 'an EnergyEfficiencyCache with 4 observation item'
            System.out.println("\nadding 4 observation item to EnergyEfficiencyCache")
            adding4ObservationItemsAndPredictionInEECache()
        when: 'the current interfaceItem data are provided from EnergyEfficiencyCache'
            InterfaceItem ifItem = eeeDataProvider.getInterfaceItem(stubbedIfRef, stubbedNetworkRef)
            System.out.println("\ncall getInterfaceItem(eejd) method\nReturned interfaceItem:\n\n" + ifItem.toString());
        then: 'ifMonitoredState value expected from GUI is 1: MONITORED'
            ifItem.getIfMonitoredState() == Integer.valueOf(1)
        and: 'interface general data are set on ifItem'
            ifItem.getNetworkRef() == stubbedNetworkRef
            ifItem.getIfRef() == stubbedIfRef
            ifItem.getTimeInterval() == samplingIntervalInSeconds * 1000
            ifItem.getIfMaximumCapacity() == EnergyEfficiencyEngine.convertByte_sInMb_s(ifMaximumCapacity).longValue()
            ifItem.getIfCurrentCapacity() == EnergyEfficiencyEngine.convertByte_sInMb_s(ifCurrentCapacity).longValue()
        and: 'current IfTrafficBandwidth value is set on ifItem'
            ifItem.getIfTrafficBandwidth().getIfTrafficBandwidth() == Long.valueOf(21)
        and: 'current PredictedTrafficBandwidth values are set - note: Safe Threshold returned to GUI'
            //Sample: 3
            //[prediction: 81.57641648969822,
            // lowerThreshold: 63.0538247121473, upperThreshold: 100.09900826724913,
            // safeLowerThreshold: 67.68447265653504, safeUpperThreshold: 95.4683603228614]
            ifItem.getIfPredictedTrafficBandwidth().getPredictedValue() == Long.valueOf(81)
            ifItem.getIfPredictedTrafficBandwidth().getPredictedLowerThreshold() == Long.valueOf(67)
            ifItem.getIfPredictedTrafficBandwidth().getPredictedUpperThreshold() == Long.valueOf(95)
        and: 'following list are correctly empty '
            ifItem.getIfObservationListSize() == Integer.valueOf(0)
            ifItem.getIfPredictionListSize() == Integer.valueOf(0)
        and: 'saving values are set on ifItem'
            ifItem.getIfSaving().getCurrentSavingPercentage() == Double.valueOf(16.66)
            ifItem.getIfSaving().getHistoricalSavingPercentage() == Double.valueOf(20.83)
            ifItem.getIfSaving().getHistoricalSavingPeriod() == Integer.valueOf(1)

        System.out.println("\n done.")
    }

    def 'extract 10 InterfaceItems from EnergyEfficiencyCache starting from a selected date'() {
        System.out.println("\nStarting Test 4")
        given: 'an EnergyEfficiencyCache with 4 observation item'
            System.out.println("\nadding 4 observation item to EnergyEfficiencyCache")
            adding4ObservationItemsAndPredictionInEECache()
        and: 'a starting date'
            Date from = eejd.getPrediction().getFirstSampleDate()
        when: 'the 10 sample of interfaceItem are requested from Prediction date'
            InterfaceItem ifItem = eeeDataProvider.getInterfaceItem(stubbedIfRef, stubbedNetworkRef,from, 10);
            System.out.println("\ncall getInterfaceItem(eejd," + eejd.getPrediction().getFirstSampleDate() + ", " + "10)" +
                    "\nReturned interfaceItem:\n\n" + ifItem.toString());
        then: 'ifMonitoredState value expected from GUI is 1: MONITORED'
            ifItem.getIfMonitoredState() == Integer.valueOf(1)
        and: 'interface general data are set on ifItem'
            ifItem.getNetworkRef() == stubbedNetworkRef
            ifItem.getIfRef() == stubbedIfRef
            ifItem.getTimeInterval() == samplingIntervalInSeconds * 1000
            ifItem.getIfMaximumCapacity() == EnergyEfficiencyEngine.convertByte_sInMb_s(ifMaximumCapacity).longValue()
            ifItem.getIfCurrentCapacity() == EnergyEfficiencyEngine.convertByte_sInMb_s(ifCurrentCapacity).longValue()
        and: 'current IfTrafficBandwidth value is set on ifItem'
            ifItem.getIfTrafficBandwidth().getIfTrafficBandwidth() == Long.valueOf(21)
        and: 'current PredictedTrafficBandwidth values are set - note: Safe Threshold returned to GUI'
            //Sample: 3
            //[prediction: 81.57641648969822,
            // lowerThreshold: 63.0538247121473, upperThreshold: 100.09900826724913,
            // safeLowerThreshold: 67.68447265653504, safeUpperThreshold: 95.4683603228614]
            ifItem.getIfPredictedTrafficBandwidth().getPredictedValue() == Long.valueOf(81)
            ifItem.getIfPredictedTrafficBandwidth().getPredictedLowerThreshold() == Long.valueOf(67)
            ifItem.getIfPredictedTrafficBandwidth().getPredictedUpperThreshold() == Long.valueOf(95)
        and: 'the 4 obsItem inserted in EnergyEfficiencyCache are present in obsItemList (we have only 4 sample in list and not 10 as requested)'
            ifItem.getIfObservationListSize() == Integer.valueOf(4)
            List<ObservationItem> obsItemList = ifItem.getIfObservationList();
            obsItemList.get(3).getIfTrafficBandwidth() == Long.valueOf(21);
        and: '10 requested predItem are present in IfPredictionList'
            //PredictionList
            ifItem.getIfPredictionListSize() == Integer.valueOf(10)
            ifItem.getIfPredictionList().size() == 10
            List<PredictionItem> predItemLinst = ifItem.getIfPredictionList()
            predItemLinst.get(4).getPredictedValue() == Long.valueOf(81)
            predItemLinst.get(4).getPredictedLowerThreshold() == Long.valueOf(65)
            predItemLinst.get(4).getPredictedUpperThreshold() == Long.valueOf(98)
        and: 'saving values are set on ifItem'
            ifItem.getIfSaving().getCurrentSavingPercentage() == Double.valueOf(16.66)
            ifItem.getIfSaving().getHistoricalSavingPercentage() == Double.valueOf(20.83)
            ifItem.getIfSaving().getHistoricalSavingPeriod() == Integer.valueOf(1)

        System.out.println("\n done.")
    }

    def 'extract InterfaceItems from EnergyEfficiencyCache from/until a date - 2 time interval'() {
        System.out.println("\nStarting Test 5")
        given: 'an EnergyEfficiencyCache with 4 observation item'
            System.out.println("\nadding 4 observation item to EnergyEfficiencyCache")
            adding4ObservationItemsAndPredictionInEECache()
        and: 'a starting date'
            Date from = eejd.getPrediction().getFirstSampleDate()
        and: 'an ending date - after two time intervals'
            int timeIntervals = 2
            Date until = new Date(eejd.getPrediction().getFirstSampleDate().getTime() + (samplingIntervalInSeconds*timeIntervals*1000))
        when: 'the InterfaceItem data are requested from the starting date to the ending'
            InterfaceItem ifItem = EEEDataProvider.getInterfaceItem(stubbedIfRef, stubbedNetworkRef, from, until);
            System.out.println("\ncall getInterfaceItem(eejd, " + eejd.getPrediction().getFirstSampleDate()+ ", " + until + ")" +
                    "\nReturned interfaceItem:\n\n" + ifItem.toString());
        then: 'ifMonitoredState value expected from GUI is 1: MONITORED'
            ifItem.getIfMonitoredState() == Integer.valueOf(1)
        and: 'interface general data are set on ifItem'
            ifItem.getNetworkRef() == stubbedNetworkRef
            ifItem.getIfRef() == stubbedIfRef
            ifItem.getTimeInterval() == samplingIntervalInSeconds * 1000
            ifItem.getIfMaximumCapacity() == EnergyEfficiencyEngine.convertByte_sInMb_s(ifMaximumCapacity).longValue()
            ifItem.getIfCurrentCapacity() == EnergyEfficiencyEngine.convertByte_sInMb_s(ifCurrentCapacity).longValue()
        and: 'current IfTrafficBandwidth value is set on ifItem'
            ifItem.getIfTrafficBandwidth().getIfTrafficBandwidth() == Long.valueOf(21)
        and: 'current PredictedTrafficBandwidth values are set - note: Safe Threshold returned to GUI'
            //Sample: 3
            //[prediction: 81.57641648969822,
            // lowerThreshold: 63.0538247121473, upperThreshold: 100.09900826724913,
            // safeLowerThreshold: 67.68447265653504, safeUpperThreshold: 95.4683603228614]
            ifItem.getIfPredictedTrafficBandwidth().getPredictedValue() == Long.valueOf(81)
            ifItem.getIfPredictedTrafficBandwidth().getPredictedLowerThreshold() == Long.valueOf(67)
            ifItem.getIfPredictedTrafficBandwidth().getPredictedUpperThreshold() == Long.valueOf(95)
        and: 'only two observation are present in ObservationList'
            ifItem.getIfObservationListSize() == Integer.valueOf(2)
            List<ObservationItem> obsItemList = ifItem.getIfObservationList();
            obsItemList.get(1).getIfTrafficBandwidth() == Long.valueOf(41);
        and: 'only two predItem are present in PredictionList'
            ifItem.getIfPredictionListSize() == Integer.valueOf(2)
            ifItem.getIfPredictionList().size() == 2
            List<PredictionItem> predItemLinst = ifItem.getIfPredictionList()
            predItemLinst.get(1).getPredictedValue() == Long.valueOf(78)
            predItemLinst.get(1).getPredictedLowerThreshold() == Long.valueOf(64)
            predItemLinst.get(1).getPredictedUpperThreshold() == Long.valueOf(92)
        and: 'saving values are set on ifItem'
            ifItem.getIfSaving().getCurrentSavingPercentage() == Double.valueOf(16.66)
            ifItem.getIfSaving().getHistoricalSavingPercentage() == Double.valueOf(20.83)
            ifItem.getIfSaving().getHistoricalSavingPeriod() == Integer.valueOf(1)

        System.out.println("\n done.")
    }

    void adding4ObservationItemsAndPredictionInEECache() {
        System.out.println("inserting data to EnergyEfficiencyCache")

        InterfaceData interfaceData = new InterfaceData(stubbedNetworkRef, stubbedIfRef, ifMaximumCapacity, ifCurrentCapacity)
        interfaceData.setIfMonitoredState(IfMonitoredState.MONITORING)
        eejd.setInterfaceData(interfaceData)

        //I'm going to put 4 samples in data in MonotoredValues list and SavingData list
        Date lastHistorySampleDate = new Date(System.currentTimeMillis() - (samplingIntervalInSeconds*1000) * sampleToInsert)
        //HistoryReader reader = new CsvHistoryReader(stubbedHistoryFileName, numOfSeries, lastHistorySampleDate, samplingIntervalInSeconds)

        Serie<Integer> startupHistory = loadHistory(lastHistorySampleDate);

        System.out.println("loadHistory completed - size " + startupHistory.getSize()
                + "\tFirstSampleDate " + startupHistory.getFirstSampleDate()
                + "\tLastSampleDate " + startupHistory.getLastSampleDate()
                + "\nasking prediction ..." + "\n")

        DomainAgnosticIncidentPrediction daip = new DomainAgnosticIncidentPrediction(stubbedIfRef, startupHistory);
        eejd.setPredictable(daip);

        System.out.println("\nPrediction set.")

        eejd.getMpAlignment().setMisaligned()
        eejd.setMpAlignment(new MPAlignment(0, 0, 0))

        int size = startupHistory.getSize()/numOfSeries
        eejd.setMonitoredValues(new Serie(1, size, new Date(0), samplingIntervalInSeconds))

        eejd.pushMonitoredValue(51.62375D, eejd.getPrediction().getFirstSampleDate())
        eejd.setMpAlignment(new MPAlignment(0,0,0))

        eejd.pushMonitoredValue(41.62375D)
        eejd.getMpAlignment().incrementAlignment(eejd)

        eejd.pushMonitoredValue(31.62375D)
        eejd.getMpAlignment().incrementAlignment(eejd)

        eejd.pushMonitoredValue(21.62375D)
        eejd.getMpAlignment().incrementAlignment(eejd)

        ifCurrentCapacity = 9323250L
        eejd.getInterfaceData().setIfCurrentCapacity(ifCurrentCapacity)

        eejd.setSamplingIntervalInSeconds(samplingIntervalInSeconds.intValue())

        //1814400 = 3 weeks
        eejd.setSavingDataValues(new Serie(1, startupHistory.getSize(), new Date(0), samplingIntervalInSeconds))

        eejd.pushSavingDataValues(12L, 12L, eejd.getPrediction().getFirstSampleDate())
        eejd.pushSavingDataValues(8L, 12L)
        eejd.pushSavingDataValues(8L, 12L)
        eejd.pushSavingDataValues(10L, 12L)
        //System.out.println("\n" + eejd.savingDataValuesToString());

        String jobKey = EnergyEfficiencyEngine.createJobKey(stubbedIfRef, stubbedNetworkRef)
        eeeData.put(jobKey, eejd)

        System.out.println("\n*** Data ready in cache ***");
    }

    Serie loadHistory(Date lastHistorySampleDate) {

        File inputFile = new File('./src/test/dataForTest/' + stubbedHistoryFileName);
        List<Double> values = new ArrayList<>();

        Scanner inputStream;

        inputStream = new Scanner(inputFile);
        inputStream.useDelimiter(',');

        while (inputStream.hasNextDouble()) {
            values.add(inputStream.nextDouble());
        }
        inputStream.close();

        return new Serie<Double>(numOfSeries, values.size(), values, lastHistorySampleDate, samplingIntervalInSeconds);
    }
}