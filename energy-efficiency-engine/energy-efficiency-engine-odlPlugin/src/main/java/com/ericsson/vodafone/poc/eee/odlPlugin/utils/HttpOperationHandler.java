/*
 * Copyright (c) 2016 Ericsson, AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.vodafone.poc.eee.odlPlugin.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.vodafone.poc.eee.odlPlugin.exception.HttpURLConnectionFailException;

/**
 * @author Simona Albanesi (Giorgio Garziano)
 *
 */
public class HttpOperationHandler {
    private static final Logger logger = LoggerFactory.getLogger(HttpOperationHandler.class);

    protected HttpConnectionHandler httpConnectionHandler;

    public HttpOperationHandler() {
        this.httpConnectionHandler = new HttpConnectionHandler();
    }

    public ResultHandler post(final String uri, final String data) throws HttpURLConnectionFailException {

        return doOperation(uri, "POST", data);
    }

    public ResultHandler put(final String uri, final String data) throws HttpURLConnectionFailException {

        return doOperation(uri, "PUT", data);
    }

    public ResultHandler get(final String uri) throws HttpURLConnectionFailException {

        return doOperation(uri, "GET", null);
    }

    public ResultHandler delete(final String uri) throws HttpURLConnectionFailException {

        return doOperation(uri, "DELETE", null);
    }

    public ResultHandler options(final String uri) throws HttpURLConnectionFailException {

        return doOperation(uri, "OPTIONS", null);
    }

    public ResultHandler head(final String uri) throws HttpURLConnectionFailException {

        return doOperation(uri, "HEAD", null);
    }

    public ResultHandler doOperation(final String uri, final String httpCommand, final String data) throws HttpURLConnectionFailException {
        ResultHandler resultHandler = new ResultHandler();

        logger.trace("HttpOperationHandler doOperation uri {} httpCommand {}", uri, httpCommand);
        try {
            HttpURLConnection conn = httpConnectionHandler.prepareConnection(uri, httpCommand);

            if (data != null) {
                conn.setDoOutput(true);
                conn.getOutputStream().write(data.getBytes());
                conn.getOutputStream().flush();
            }

            int code = conn.getResponseCode();
            resultHandler.setCode(code);

            if(code == HttpURLConnection.HTTP_OK || code == HttpURLConnection.HTTP_CREATED){
                logger.debug("HttpOperationHandler doOperation respons code OK {} {} {}", code, httpCommand, uri);
            }
            else {
                if (code == HttpURLConnection.HTTP_NOT_FOUND) {
                    logger.debug("HttpOperationHandler doOperation respons code not found {} {} {}", code, httpCommand, uri);
                    resultHandler.setResultMessage("Not found");
                }
                else {
                    logger.error("HttpOperationHandler doOperation respons code not OK {} {} {}", code, httpCommand, uri);
                    resultHandler.setResultMessage("Not ok");
                    throw new HttpURLConnectionFailException("HTTP Status-Code" + conn.getResponseMessage());
                }

                return resultHandler;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            String output;
            String response = "";
            while ((output = br.readLine()) != null) {
                response = response.concat(output);
            }
            resultHandler.setResultMessage(response);

            br.close();
            conn.disconnect();

        } catch (final MalformedURLException e) {
            logger.error("HttpOperationHandler MalformedURLException exception {}", e);
            throw new HttpURLConnectionFailException(e.getMessage());
        } catch (final IOException e) {
            logger.warn("HttpOperationHandler IOException exception {}", e);
            throw new HttpURLConnectionFailException(e.getMessage());
        }

        logger.trace("HttpOperationHandler doOperation result {}", resultHandler.toString());

        return resultHandler;
    }

}
