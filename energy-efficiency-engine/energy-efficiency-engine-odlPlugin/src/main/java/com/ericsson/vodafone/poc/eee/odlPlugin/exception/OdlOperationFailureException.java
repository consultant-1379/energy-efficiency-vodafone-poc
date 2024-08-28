package com.ericsson.vodafone.poc.eee.odlPlugin.exception;

/**
 * Created by esimalb on 8/23/17.
 */
public class OdlOperationFailureException extends Exception {

    public OdlOperationFailureException(String errorMessage) {

        super(errorMessage);
    }

    public String toString() {

        return getMessage();
    }
}
