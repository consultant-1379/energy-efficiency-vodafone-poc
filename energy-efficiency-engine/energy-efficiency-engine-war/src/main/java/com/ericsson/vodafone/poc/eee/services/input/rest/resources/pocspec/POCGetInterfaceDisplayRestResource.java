package com.ericsson.vodafone.poc.eee.services.input.rest.resources.pocspec;

import com.ericsson.vodafone.poc.eee.services.input.rest.resources.GetInterfaceRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/EEEApplication/interface")
public interface POCGetInterfaceDisplayRestResource {

    @POST
    @Path("/POCdata")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Response getInterface(GetInterfaceRequest getInterfaceRequest);

}
