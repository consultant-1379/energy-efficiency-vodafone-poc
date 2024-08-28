/*
 * Copyright (c) 2016 Ericsson, AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.vodafone.poc.eee.odlPlugin.utils;

import static com.ericsson.vodafone.poc.eee.odlPlugin.utils.Constants.AUTHORIZATION;
import static com.ericsson.vodafone.poc.eee.odlPlugin.utils.Constants.CONTENT_TYPE;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import org.apache.commons.codec.binary.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Simona Albanesi (Giorgio Garziano)
 *
 */

public class HttpConnectionHandler {

    private static final Logger logger = LoggerFactory.getLogger(HttpConnectionHandler.class);

    public HttpURLConnection prepareConnection (final String uri,
                                               final String httpCommand) throws IOException{
        HttpURLConnection conn = null;

        final URL url = new URL(uri);
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(httpCommand);

        ConnectionInfo connectionInfo = new ConnectionInfo();

        String accept = connectionInfo.getAccept();
        conn.setRequestProperty(accept, connectionInfo.getContentTypeValue());

        String contentType = connectionInfo.getContentTypeValue();
        conn.setRequestProperty(CONTENT_TYPE, contentType);

        final String username = connectionInfo.getUser();
        final String password = connectionInfo.getPassword();

        final String auth = username.concat(":").concat(password);
        final byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
        final String authorization = connectionInfo.getAuthorization();
        final String authHeader = authorization.concat(" ").concat(new String(encodedAuth));

        conn.setRequestProperty(AUTHORIZATION, authHeader);

        logger.trace("HttpConnectionHandler prepareConnection uri {}", uri);
        logger.trace("HttpConnectionHandler prepareConnection accept {}", accept);
        logger.trace("HttpConnectionHandler prepareConnection contentType {}", contentType);
        logger.trace("HttpConnectionHandler prepareConnection username {}", username);
        logger.trace("HttpConnectionHandler prepareConnection password {}", password);
        logger.trace("HttpConnectionHandler prepareConnection authorization {}", authorization);
        logger.trace("HttpConnectionHandler prepareConnection authHeader {}", authHeader);

        return conn;
    }
}
