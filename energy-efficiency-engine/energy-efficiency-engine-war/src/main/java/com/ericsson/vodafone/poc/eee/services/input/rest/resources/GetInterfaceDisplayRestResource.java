package com.ericsson.vodafone.poc.eee.services.input.rest.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/EEEApplication/interface")
public interface GetInterfaceDisplayRestResource {

    @POST
    @Path("/data")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Response getInterface(GetInterfaceRequest getInterfaceRequest);

}
