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

package com.ericsson.vodafone.poc.eee.odlPlugin.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 *PropertiesReader.
 *
 */
public class PropertiesReader {

    final String odlClientPropertiesFile = "odlClient.properties";

    Properties file = new Properties();

    public String loadProperty(String property) throws FileNotFoundException {
        locatePropertiesFile(odlClientPropertiesFile);
        return file.getProperty(property);
    }

    private void locatePropertiesFile(final String fileName) throws FileNotFoundException {
        final InputStream stream = getClass().getClassLoader().getResourceAsStream(fileName);
        try {
            file.load(stream);
        } catch (final IOException e) {
            throw new FileNotFoundException("Property file " + fileName + " not found");
        }
    }

}
