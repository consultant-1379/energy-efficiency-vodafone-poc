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
package com.ericsson.vodafone.poc.eee.services.input.rest.resources;

import org.jboss.resteasy.plugins.server.servlet.FilterDispatcher;
import org.jboss.resteasy.spi.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

/**
 * A class to initialize the Rest WebService with Servlet 3.0 annotations.
 *
 * @author ejonbli
 */
public class MainDispatcher extends FilterDispatcher {

    private Logger logger = LoggerFactory.getLogger(MainDispatcher.class);

    /**
     * @param servletRequest  {ServletRequest}
     * @param servletResponse {ServletResponse}
     * @param filterChain     {FilterChain}
     * @throws IOException      {IOException}
     * @throws ServletException {ServletException}
     */
    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain)
            throws IOException, ServletException {
        try {

            if (logger.isDebugEnabled()) {
                logger.debug("Called REST URI: {}", ((HttpServletRequest) servletRequest).getPathInfo());
                logger.debug("Method: {}", ((HttpServletRequest) servletRequest).getMethod());
                logger.debug("X-Tor-UserID: {}", ((HttpServletRequest) servletRequest).getHeader("X-Tor-UserID"));
                logger.debug("Request Headers: ");

                final Enumeration headerNames = ((HttpServletRequest) servletRequest).getHeaderNames();
                while (headerNames.hasMoreElements()) {
                    final String name = (String) headerNames.nextElement();
                    logger.debug("   {} -> {}", name, ((HttpServletRequest) servletRequest).getHeader(name));
                }
            }

            this.servletContainerDispatcher.service(((HttpServletRequest) servletRequest).getMethod(), (HttpServletRequest) servletRequest,
                    (HttpServletResponse) servletResponse, false);

        } catch (final NotFoundException exception) {
            logger.error(exception.getMessage(), exception);
            filterChain.doFilter(servletRequest, servletResponse);
        }

    }

}
