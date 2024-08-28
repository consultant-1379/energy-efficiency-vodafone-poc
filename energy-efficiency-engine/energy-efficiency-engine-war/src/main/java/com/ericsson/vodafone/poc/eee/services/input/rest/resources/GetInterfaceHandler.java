package com.ericsson.vodafone.poc.eee.services.input.rest.resources;

import com.ericsson.vodafone.poc.eee.service.input.rest.data.InterfaceItem;
import com.ericsson.vodafone.poc.eee.service.input.rest.data.utils.EEEDataProvider;
import com.ericsson.vodafone.poc.eee.services.input.rest.resources.utils.DateUtils;
import com.ericsson.vodafone.poc.eee.services.input.rest.resources.utils.GetInterfaceMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.Date;

public class GetInterfaceHandler implements GetInterfaceDisplayRestResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetInterfaceHandler.class);

    @Override
    public Response getInterface(final GetInterfaceRequest getInterfaceRequest) {

        LOGGER.info("Request received to get interface : {} ", getInterfaceRequest.toString());

        String networkRef = getInterfaceRequest.getNetworkRef();
        String ifRef = getInterfaceRequest.getIfRef();
        GetInterfaceMode mode = getInterfaceRequest.getMode();

        InterfaceItem returnInterfaceItem = getInterface(ifRef, networkRef, mode);

        if(returnInterfaceItem == null) {
            LOGGER.info("Response - returnInterfaceItem == null");
            //return Response.noContent().build();
            returnInterfaceItem = new InterfaceItem();
        }

        LOGGER.info("Response (mode {}) - returnInterfaceItem {}", mode.toString(), returnInterfaceItem.toString());

        Response r = Response.status(200).header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
                .header("Access-Control-Allow-Credentials", "true").header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
                .header("Access-Control-Max-Age", "1209600").entity(returnInterfaceItem).build();

        return r;

        /* TEST */
        /*
        final InterfaceItem returnInterfaceItem = new InterfaceItem("mini-link-topo", "mini-link-6691-1:WAN-1/1/2", new Integer(1) ,
                new Long(1000), new Long(10000), new Long(100000));
        */

    }

    private InterfaceItem getInterface(final String ifRef, final String networkRef, final GetInterfaceMode mode) {

        InterfaceItem ifItem = null;
        Date now = new Date();

        switch (mode) {
            case CURRENT_DAY:
            default:
                Long twoMinutes = new Long(2 * 60 * 1000);
                ifItem = EEEDataProvider.getInterfaceItem(ifRef, networkRef, DateUtils.getStartOfDay(now), DateUtils.getEndOfDay(now));
                break;
            case PREVIOUS_DAY:
                Date oneDayAgo = DateUtils.subtractDay(now);
                Long oneHour = new Long(60 * 60 * 1000);
                ifItem = EEEDataProvider.getInterfaceItem(ifRef, networkRef, DateUtils.getStartOfDay(oneDayAgo), DateUtils.getEndOfDay(oneDayAgo), oneHour);
                break;
            case SAVING:
                ifItem = EEEDataProvider.getInterfaceItemSavingData(ifRef, networkRef);
                break;
        }

        return ifItem;
    }
}
