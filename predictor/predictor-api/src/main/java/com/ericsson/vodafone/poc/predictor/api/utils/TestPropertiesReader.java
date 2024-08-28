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

import java.io.*;
import java.util.Properties;

/**
 * PropertiesReader.
 *
 */
public class TestPropertiesReader {

    final String enginePropertiesFile = System.getProperty("user.dir") + "/resources/" + "predictor.properties";

    Properties file = new Properties();

    public String loadProperty(final String property) throws FileNotFoundException {
        locatePropertiesFile(enginePropertiesFile);
        return file.getProperty(property);
    }

    private void locatePropertiesFile(final String fileName) throws FileNotFoundException {
        // final InputStream stream = getClass().getClassLoader().getResourceAsStream(fileName);
        final InputStream stream = new FileInputStream(fileName);
        ;
        try {
            file.load(stream);
        } catch (final IOException e) {
            throw new FileNotFoundException("Property file " + fileName + " not found");
        }
    }

}
