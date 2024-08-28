/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
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

import java.util.ArrayList;
import java.util.Collection;

import javax.enterprise.context.RequestScoped;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.ericsson.vodafone.poc.eee.service.input.rest.data.utils.EEEDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.vodafone.poc.eee.service.input.rest.data.InterfaceItem;

// import org.apache.commons.lang3.StringUtils;
// import com.ericsson.oss.service.constants.Constants;
//import com.ericsson.oss.service.ejbs.DataServiceBean;

/**
 * Input Service endpoint implementation (version 1).
 */
@RequestScoped
@Path("/EEEApplication")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InputTreeResource {

    // Injects the Request resource in the Restful service
    @Context
    Request restRequest;

    @Context
    HttpServletRequest servletRequest;

    @Context
    UriInfo uriInfo;

    /*
     * @Inject private EnergyEfficiencyEngine eeEngine;
     */

    /**
     * Set by Resteasy framework when the http-headers contain the X-Tor-UserID entry
     */
    private String userId;
    private Logger logger = LoggerFactory.getLogger(InputTreeResource.class);

    @GET
    @Path("/interfaces/")
    public Response getInterfaces() {
        // validateUserHeader();

        final Collection<InterfaceItem> returnNodeItems = this.getInetrfaces();

        return Response.status(200).header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
                .header("Access-Control-Allow-Credentials", "true").header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
                .header("Access-Control-Max-Age", "1209600").entity(returnNodeItems).build();

    }

//    @GET
//    @Path("/interface/{networkref}/{ifref}/{mode}/{date}")
//    public Response getInterfaceData() {
//        /* Default response */
//        return Response.status(200).build();
//    }
//
//    @GET
//    @Path("/interface")
//    public Response getInterfaceData(@QueryParam("networkref") final String networkref, @QueryParam("ifref") final String ifref,
//                                     @QueryParam("mode") final String mode) {
//        //validateUserHeader();
//        /* Default response */
//        return Response.status(200).build();
//    }

    @GET
    @Path("/interfaces")
    public Response getInterfaces(@QueryParam("networkref") final String netId) {
        //validateUserHeader();

        final Collection<InterfaceItem> returnNodeItems = this.getInetrfaces();

        return Response.status(200).header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
                .header("Access-Control-Allow-Credentials", "true").header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
                .header("Access-Control-Max-Age", "1209600").entity(returnNodeItems).build();

    }

    private Collection<InterfaceItem> getInetrfaces() {

        /* TEST */
//        final InterfaceItem[] testSample = new InterfaceItem[] {
//                new InterfaceItem("mini-link-topo", "mini-link-6691-1:WAN-1/1/2", 1, new Long(10000), new Long(10000), new Long(10000)),
//                new InterfaceItem("mini-link-topo", "mini-link-6691-1:WAN-1/2/1", 1, new Long(10000), new Long(10000), new Long(10000)) };
//
//        for (int i = 0; i < testSample.length; i++) {
//            ifItems.add(testSample[i]);
//        }
        return EEEDataProvider.getInterfaceItemList();
    }

//    private InterfaceItem getInterface(final String ifRef, final String networkRef) {
//        InterfaceItem res = new InterfaceItem();
//        //return EEEDataProvider.getInterfaceItem(ifRef, networkRef);
//
//        return res;
//    }

    /**l
     * Called by Resteasy when the http-headers contain the X-Tor-UserID entry
     *
     * @param userId
     *            {String} the value of X-Tor-UserID header
     */
    @HeaderParam("X-Tor-userId")
    public void setUserId(final String userId) {
        logger.debug("User logged in is {}", userId);
        this.userId = userId != null ? userId.toLowerCase() : userId;
        logger.debug("User collectionService dealing with is {}", userId);
    }

    /**
     * If the User Header is empty, throws an exception
     */
    private void validateUserHeader() {

        if (!userId.isEmpty()) {
            logger.debug("User logged in is {}", userId);
            //throw new UserNotFoundException("User not in header");
        }
    }
}
