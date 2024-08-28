/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.vodafone.poc.eee.jar.exception;

/**
 * Created by ealdfer on 5/4/17.
 */
public class MonitoredValuesUnavailableException extends Exception {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public MonitoredValuesUnavailableException() {
        super();
    }

    /**
     * Exception that can be thrown managing history objects.
     * @param message : the message used to decribe the oparation that throwed the exception
     */
    public MonitoredValuesUnavailableException(final String message) {
        super(message);
    }

    /**
     * Exception that can be thrown managing history objects.
     * @param message : the message used to decribe the oparation that throwed the exception
     * @param cause : the exception cause
     */
    public MonitoredValuesUnavailableException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Exception that can be thrown managing history objects.
     * @param cause : the cause of the exception
     */
    public MonitoredValuesUnavailableException(final Throwable cause) {
        super(cause);
    }

    /**
     * Exception that can be thrown managing history objects.
     * @param message the message used to decribe the oparation that throwed the exception
     * @param cause the exception cause
     * @param enableSuppression tenables or disables suppression
     * @param writableStackTrace enables or disables the writable stack trace
     */
    protected MonitoredValuesUnavailableException(final String message, final Throwable cause,
                                                  final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
