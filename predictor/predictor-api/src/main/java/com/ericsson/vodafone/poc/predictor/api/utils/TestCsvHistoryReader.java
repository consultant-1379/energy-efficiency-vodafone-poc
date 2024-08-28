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

package com.ericsson.vodafone.poc.predictor.api.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.vodafone.poc.predictor.api.exception.SerieOperationException;

/**
 * CsvHistoryReader.
 */
public class TestCsvHistoryReader implements TestHistoryReader {

    private Logger logger = LoggerFactory.getLogger(TestCsvHistoryReader.class);

    private String fileName;
    private int numOfSeries;

    private Date lastSampleDate;
    private long samplingIntervalSeconds;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public int getNumOfSeries() {
        return numOfSeries;
    }

    public void setNumOfSeries(final int numOfSeries) {
        this.numOfSeries = numOfSeries;
    }

    public Date getLastSampleDate() {
        return lastSampleDate;
    }

    public void setLastSampleDate(final Date lastSampleDate) {
        this.lastSampleDate = lastSampleDate;
    }

    public long getSamplingIntervalSeconds() {
        return samplingIntervalSeconds;
    }

    public void setSamplingIntervalSeconds(final long samplingIntervalSeconds) {
        this.samplingIntervalSeconds = samplingIntervalSeconds;
    }

    public TestCsvHistoryReader() {
    }

    public TestCsvHistoryReader(final String fileName, final int numOfSeries, final Date lastSampleDate, final long samplingIntervalSeconds) {
        this.fileName = fileName;
        this.numOfSeries = numOfSeries;
        this.lastSampleDate = lastSampleDate;
        this.samplingIntervalSeconds = samplingIntervalSeconds;
    }

    @Override
    public Serie loadHistory() throws FileNotFoundException, SerieOperationException {

        final File inputFile = new File(getPredictorHistoryPath() + fileName);
        final List<Double> values = new ArrayList<>();

        Scanner inputStream;

        inputStream = new Scanner(inputFile);
        final String delimiter = getPredictorHistoryDelimiter();
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
            final TestPropertiesReader readProperty = new TestPropertiesReader();
            try {
                predictorHistoryDelimiter = readProperty.loadProperty(TestConstants.PREDICTOR_HISTORY_DELIMITER_PROP);
            } catch (final FileNotFoundException e) {
                logger.warn("getPredictorHistoryDelimiter: Unable to find Property File for: predictorHistoryDelimiter");
                logger.error(e.toString());
            }
        }
        return predictorHistoryDelimiter;
    }

    private String getPredictorHistoryPath() {
        String predictorHistoryPath = null;
        predictorHistoryPath = System.getProperty("user.dir") + "/resources/";
        //        if (System.getProperty("predictorHistoryPath") != null) {
        //            predictorHistoryPath = System.getProperty("user.dir") + "/resources/";
        //        } else {
        //            final TestPropertiesReader readProperty = new TestPropertiesReader();
        //            try {
        //                predictorHistoryPath = readProperty.loadProperty(TestConstants.PREDICTOR_HISTORY_PATH_PROP);
        //            } catch (final FileNotFoundException e) {
        //                logger.warn("getPredictorHistoryPath: Unable to find Property File for: " + "predictorHistoryPath");
        //                logger.error(e.toString());
        //            }
        //        }
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

}
