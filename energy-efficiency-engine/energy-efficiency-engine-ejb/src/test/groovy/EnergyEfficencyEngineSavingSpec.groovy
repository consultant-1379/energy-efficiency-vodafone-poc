import com.ericsson.vodafone.poc.eee.jar.EnergyEfficiencyJobData
import com.ericsson.vodafone.poc.eee.jar.utils.SavingData
import com.ericsson.vodafone.poc.predictor.api.utils.Serie
import spock.lang.Shared
import spock.lang.Specification

/**
 * Created by esimalb on 12/6/17.
 */
class EnergyEfficencyEngineSavingSpec extends Specification{

    @Shared
    EnergyEfficiencyJobData eejd = new EnergyEfficiencyJobData()

    //504 samples in history file = 3 weeks of monitoring
    static int numOfSampleInWeek = 504/3
    static Long samplingIntervalInSeconds = 60L
    static int numOfSeries = 1

    /*static String stubbedIfRef = "stubbed_if_ref"
    static String stubbedNetworkRef = "stubbed_network_ref"*/

    //static Long ifMaximumCapacity = 13984875L

    def setupSpec() {
        Date lastHistorySampleDate = new Date(System.currentTimeMillis() - (samplingIntervalInSeconds*1000) * numOfSampleInWeek)
        eejd.setSavingDataValues(new Serie(numOfSeries, numOfSampleInWeek, lastHistorySampleDate, samplingIntervalInSeconds))
    }

    def cleanupSpec() {
        eejd.clearSavingDataValues()
    }

    def 'check currentEnergySavingPercentage and last24hEnergySavingPercentage if 13 samples is inserted in SavingData'() {
        System.out.println("\nStarting Test 1")
        given: 'an empty SavingDataValues'
        when: 'a new sample is inserted in SavingDataValues'
        eejd.pushSavingDataValues(currentTxPower, nominalTxPower)
            System.out.println(eejd.savingDataValuesToString());
        then: 'check currentEnergySavingPercentage value'
            eejd.getCurrentEnergySavingPercentage() == currentSavingPercentage
        and: 'check last24hEnergySavingPercentage value'
            eejd.last24hEnergySavingPercentage() == last24hEnergySavingPercentage
            System.out.println("\n done.")
        where:
            currentTxPower | nominalTxPower || currentSavingPercentage | last24hEnergySavingPercentage
            1946L          | 2110L          || 7.77                    | 7.77
            1946L          | 2110L          || 7.77                    | 7.77
            1946L          | 2110L          || 7.77                    | 7.77
            316L           | 2110L          || 85.02                   | 27.08
            316L           | 2110L          || 85.02                   | 38.67
            316L           | 2110L          || 85.02                   | 46.39
            316L           | 2110L          || 85.02                   | 51.91
            316L           | 2110L          || 85.02                   | 56.05
            316L           | 2110L          || 85.02                   | 59.27
            316L           | 2110L          || 85.02                   | 61.84
            316L           | 2110L          || 85.02                   | 63.95
            316L           | 2110L          || 85.02                   | 65.7
            316L           | 2110L          || 85.02                   | 67.19
    }
}
