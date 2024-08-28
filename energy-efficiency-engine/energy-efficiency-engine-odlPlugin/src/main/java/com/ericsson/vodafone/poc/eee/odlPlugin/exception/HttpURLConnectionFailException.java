package com.ericsson.vodafone.poc.eee.odlPlugin.exception;

/**
 * Created by esimalb on 8/23/17.
 */
public class HttpURLConnectionFailException extends Exception {

    public HttpURLConnectionFailException(String errorMessage) {
        super(errorMessage);
    }

    public String toString() {
        return getMessage();
    }
}
