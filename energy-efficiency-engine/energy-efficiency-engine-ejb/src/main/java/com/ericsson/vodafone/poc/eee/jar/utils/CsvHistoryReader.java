/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.vodafone.poc.eee.jar.utils;

import com.ericsson.vodafone.poc.predictor.api.exception.SerieOperationException;
import com.ericsson.vodafone.poc.predictor.api.utils.Serie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

/**
 * CsvHistoryReader.
 */
public class CsvHistoryReader implements HistoryReader {

    private Logger logger = LoggerFactory.getLogger(CsvHistoryReader.class);

    private String fileName;
    private int numOfSeries;

    private Date lastSampleDate;
    private long samplingIntervalSeconds;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getNumOfSeries() {
        return numOfSeries;
    }

    public void setNumOfSeries(int numOfSeries) {
        this.numOfSeries = numOfSeries;
    }

    public Date getLastSampleDate() {
        return lastSampleDate;
    }

    public void setLastSampleDate(Date lastSampleDate) {
        this.lastSampleDate = lastSampleDate;
    }

    public long getSamplingIntervalSeconds() {
        return samplingIntervalSeconds;
    }

    public void setSamplingIntervalSeconds(long samplingIntervalSeconds) {
        this.samplingIntervalSeconds = samplingIntervalSeconds;
    }

    public CsvHistoryReader() {
    }

    public CsvHistoryReader(String fileName, int numOfSeries, Date lastSampleDate, long samplingIntervalSeconds) {
        this.fileName = fileName;
        this.numOfSeries = numOfSeries;
        this.lastSampleDate = lastSampleDate;
        this.samplingIntervalSeconds = samplingIntervalSeconds;
    }

    public Serie loadHistory() throws FileNotFoundException, SerieOperationException {

        File inputFile = new File(getPredictorHistoryPath() + fileName);
        List<Double> values = new ArrayList<>();

        Scanner inputStream;

        inputStream = new Scanner(inputFile);
        String delimiter = getPredictorHistoryDelimiter();
        inputStream.useDelimiter(delimiter);

        while (inputStream.hasNextDouble()) {
            values.add(inputStream.nextDouble());
        }
        inputStream.close();

        return new Serie<Double>(numOfSeries, values.size(), values, lastSampleDate, samplingIntervalSeconds);
    }

    private String getPredictorHistoryDelimiter() {
        String predictorHistoryDelimiter = null;
        if (System.getProperty("predictorHistoryDelimiter") != null) {
            predictorHistoryDelimiter = System.getProperty("predictorHistoryDelimiter");
        } else {
            final PropertiesReader readProperty = new PropertiesReader();
            try {
                predictorHistoryDelimiter = readProperty.loadProperty(Constants.PREDICTOR_HISTORY_DELIMITER_PROP);
            } catch (final FileNotFoundException e) {
                logger.warn("getPredictorHistoryDelimiter: Unable to find Property File for: predictorHistoryDelimiter");
                logger.error(e.toString());
            }
        }
        return predictorHistoryDelimiter;
    }

    private String getPredictorHistoryPath() {
        String predictorHistoryPath = null;
        if (System.getProperty("predictorHistoryPath") != null) {
            predictorHistoryPath = System.getProperty("predictorHistoryPath");
        } else {
            final PropertiesReader readProperty = new PropertiesReader();
            try {
                predictorHistoryPath = readProperty.loadProperty(Constants.PREDICTOR_HISTORY_PATH_PROP);
            } catch (final FileNotFoundException e) {
                logger.warn("getPredictorHistoryPath: Unable to find Property File for: " + "predictorHistoryPath");
                logger.error(e.toString());
            }
        }
        return predictorHistoryPath;
    }

    public Triplet<Double, Double, Double> getHistorySamples(Integer historyIndex) throws FileNotFoundException, SerieOperationException {
        List<Double> history = loadHistory().getSerie();
        int sampleInWeek = history.size()/3;
        Double firstWeek = 0.0;
        Double secondWeek = 0.0;
        Double thirdWeek = 0.0;

        if(historyIndex < sampleInWeek){
            firstWeek = history.get(historyIndex);
            secondWeek = history.get(historyIndex+sampleInWeek);
            thirdWeek = history.get(historyIndex+2*sampleInWeek);

        }

        return new Triplet(firstWeek, secondWeek, thirdWeek);
    }

    public boolean historyFileExist(){

        File inputFile = new File(getPredictorHistoryPath() + fileName);
        if(inputFile.exists())
            return true;

        return false;
    }

}
