package com.ericsson.vodafone.poc.eee.jar.utils;

/**
 * The type Saving data.
 *
 * @description Type containing the data about saved Tx Power on a given interface at a given time
 */
public class SavingData {

    private Long currentTxPower;
    private Long nominalTxPower;

    /**
     * Instantiates a new Saving data.
     *
     * @param currentTxPower the current saving
     * @param nominalTxPower  the nominal value
     */
    public SavingData(Long currentTxPower, Long nominalTxPower) {
        this.currentTxPower = currentTxPower;
        this.nominalTxPower = nominalTxPower;
    }

    /**
     * Gets current saving.
     *
     * @return the current saving
     */
    public Long getCurrentTxPower() {
        return currentTxPower;
    }

    /**
     * Sets current saving.
     *
     * @param currentTxPower the current saving
     */
    public void setCurrentTxPower(Long currentTxPower) {
        this.currentTxPower = currentTxPower;
    }

    /**
     * Gets nominal value.
     *
     * @return the nominal value
     */
    public Long getNominalTxPower() {
        return nominalTxPower;
    }

    /**
     * Sets nominal value.
     *
     * @param nominalTxPower the nominal value
     */
    public void setNominalTxPower(Long nominalTxPower) {
        this.nominalTxPower = nominalTxPower;
    }

    public Long currentSaving () {
        if(currentTxPower > nominalTxPower){
            return Long.valueOf(0);
        }

        return (nominalTxPower - currentTxPower);
    }

    public Double currentSavingPercentageInDouble () {
        return Double.valueOf(currentSavingPercentageInLong()/100.0);
    }

    public Long currentSavingPercentageInLong() {
        if(nominalTxPower == 0) {
            return Long.valueOf(0);
        }
        return currentSaving() * 10000 / nominalTxPower;
    }

    @Override
    public String toString() {
        return "\n{" +
                   "currentTxPower:" + currentTxPower +
                   ", nominalTxPower:" + nominalTxPower +
                   " - currentSavingPercentage= "+ currentSavingPercentageInDouble() + "}";
    }
}
