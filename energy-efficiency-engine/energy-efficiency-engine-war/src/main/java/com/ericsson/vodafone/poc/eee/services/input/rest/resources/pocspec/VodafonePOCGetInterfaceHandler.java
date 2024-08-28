package com.ericsson.vodafone.poc.eee.services.input.rest.resources.pocspec;

import com.ericsson.vodafone.poc.eee.jar.EnergyEfficiencyCache;
import com.ericsson.vodafone.poc.eee.jar.EnergyEfficiencyEngine;
import com.ericsson.vodafone.poc.eee.jar.EnergyEfficiencyJobData;
import com.ericsson.vodafone.poc.eee.jar.exception.MonitoredValuesUnavailableException;
import com.ericsson.vodafone.poc.eee.service.input.rest.data.ConfiguredBandwidthItem;
import com.ericsson.vodafone.poc.eee.service.input.rest.data.InterfaceItem;
import com.ericsson.vodafone.poc.eee.service.input.rest.data.ObservationItem;
import com.ericsson.vodafone.poc.eee.service.input.rest.data.PredictionItem;
import com.ericsson.vodafone.poc.eee.service.input.rest.data.utils.EEEDataProvider;
import com.ericsson.vodafone.poc.eee.services.input.rest.resources.GetInterfaceRequest;
import com.ericsson.vodafone.poc.eee.services.input.rest.resources.utils.DateUtils;
import com.ericsson.vodafone.poc.eee.services.input.rest.resources.utils.GetInterfaceMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Date;

@Startup
@Singleton
public class VodafonePOCGetInterfaceHandler implements POCGetInterfaceDisplayRestResource {

    public static final int EEE_SAMPLING_INTERVAL_IN_SECONDS = 30;

    public static final int FIFTEEN_MINUTES_IN_MILLS = 15 * 60 * 1000;
    public static final int ONE_REAL_DAY_MILLS = 24*60*60*1000;
    public static final int ONE_FAKE_DAY = 96 * EEE_SAMPLING_INTERVAL_IN_SECONDS * 1000;
    private static final Logger LOGGER = LoggerFactory.getLogger(VodafonePOCGetInterfaceHandler.class);
    private static Date fakeNow;
    private static boolean startup;
    private static int fakeDayCount;

    @PostConstruct
    public void init() {
        startup = true;
        fakeDayCount = 0;
    }


    @Override
    public Response getInterface(final GetInterfaceRequest getInterfaceRequest) {

        LOGGER.info("Request received to get interface : {} ", getInterfaceRequest.toString());

        String networkRef = getInterfaceRequest.getNetworkRef();
        String ifRef = getInterfaceRequest.getIfRef();
        GetInterfaceMode mode = getInterfaceRequest.getMode();

        InterfaceItem returnInterfaceItem = vodafonePOC_getFakeInterface(ifRef, networkRef, mode);

        if (returnInterfaceItem == null) {
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
    }

    private Date vodafonePOC_getFakeStartOfDay(final String ifRef, final String networkRef, final Date date) {

        long howManySamplesIf15Min = (date.getTime() - DateUtils.getStartOfDay(date).getTime()) // milliseconds until date from midnight
                /
                (FIFTEEN_MINUTES_IN_MILLS); // milliseconds in 15 min

        // Now I have to return a Date such as it selects howManySamplesIf15Min samples

        EnergyEfficiencyCache cache = EnergyEfficiencyCache.getInstance();
        EnergyEfficiencyJobData d = cache.get(EnergyEfficiencyEngine.createJobKey(ifRef, networkRef));
        long realSamplingIntervalInMilliSeconds = d.getSamplingIntervalInSeconds() * 1000;

        long millsToSelectWantedSamples = howManySamplesIf15Min * realSamplingIntervalInMilliSeconds;

        return new Date(date.getTime() - millsToSelectWantedSamples);
    }

    private Date vodafonePOC_getFakeEndOfDay(final String ifRef, final String networkRef, final Date date) {

        long howManySamplesIf15Min = (DateUtils.getEndOfDay(date).getTime() - date.getTime()) // milliseconds until midnight from date
                /
                (FIFTEEN_MINUTES_IN_MILLS); // milliseconds in 15 min

        // Now I have to return a Date such as it selects howManySamplesIf15Min samples

        EnergyEfficiencyCache cache = EnergyEfficiencyCache.getInstance();
        EnergyEfficiencyJobData d = cache.get(EnergyEfficiencyEngine.createJobKey(ifRef, networkRef));
        long realSamplingIntervalInMilliSeconds = d.getSamplingIntervalInSeconds() * 1000;

        long millsToSelectWantedSamples = howManySamplesIf15Min * realSamplingIntervalInMilliSeconds;

        return new Date(date.getTime() + millsToSelectWantedSamples);
    }

    private Date vodafonePOC_getFakeStartOfDayNEW(final String ifRef, final String networkRef, final Date date) {

        long howManySamplesIf15Min = 1;

        EnergyEfficiencyCache cache = EnergyEfficiencyCache.getInstance();
        EnergyEfficiencyJobData d = cache.get(EnergyEfficiencyEngine.createJobKey(ifRef, networkRef));

        long realSamplingIntervalInMilliSeconds = 0;
        if(d != null){
            realSamplingIntervalInMilliSeconds = d.getSamplingIntervalInSeconds() * 1000;
        }

        long millsToSelectWantedSamples = howManySamplesIf15Min * realSamplingIntervalInMilliSeconds;

        return new Date(date.getTime() - millsToSelectWantedSamples);
    }

    private Date vodafonePOC_getFakeEndOfDayNEW(final String ifRef, final String networkRef, final Date date) {

        long howManySamplesIf15Min = 96;

        EnergyEfficiencyCache cache = EnergyEfficiencyCache.getInstance();
        EnergyEfficiencyJobData d = cache.get(EnergyEfficiencyEngine.createJobKey(ifRef, networkRef));

        long realSamplingIntervalInMilliSeconds = 0;
        if(d != null) {
            realSamplingIntervalInMilliSeconds= d.getSamplingIntervalInSeconds() * 1000;
        }

        long millsToSelectWantedSamples = howManySamplesIf15Min * realSamplingIntervalInMilliSeconds;
        return new Date(date.getTime() + millsToSelectWantedSamples);
    }


    private InterfaceItem vodafonePOC_computeFakeIfItemDates(final InterfaceItem ifItem, final Date fakeStartDate, final long fakeSamplingIntervalMills) {
        InterfaceItem fakeIfItem = new InterfaceItem(ifItem);

        //fakeIfItem.setTimeInterval(new Long(fakeSamplingIntervalMills));

        long fakePTime = fakeStartDate.getTime();
        if(fakeIfItem.getIfPredictionList() != null) {
            for (PredictionItem p : fakeIfItem.getIfPredictionList()) {
                p.setTime(new Long(fakePTime));
                fakePTime += fakeSamplingIntervalMills;
            }
        }

        long fakeOTime = fakeStartDate.getTime();
        if(fakeIfItem.getIfObservationList() != null) {
            for (ObservationItem o : fakeIfItem.getIfObservationList()) {
                o.setTime(new Long(fakeOTime));
                fakeOTime += fakeSamplingIntervalMills;
            }
        }

        long fakeCTime = fakeStartDate.getTime();
        if(fakeIfItem.getIfConfiguredBandwidthList() != null) {
            for (ConfiguredBandwidthItem c : fakeIfItem.getIfConfiguredBandwidthList()) {
                c.setTime(new Long(fakeCTime));
                fakeCTime += fakeSamplingIntervalMills;
            }
        }

        if((fakeIfItem.getIfTrafficBandwidth() != null) &&
           (fakeIfItem.getIfPredictedTrafficBandwidth() != null) &&
           (fakeIfItem.getIfObservationList() != null)) {

            long timeToSet = fakeIfItem.getIfObservationList().get(fakeIfItem.getIfObservationListSize() - 1).getTime();

            fakeIfItem.getIfTrafficBandwidth().setTime(timeToSet);
            fakeIfItem.getIfPredictedTrafficBandwidth().setTime(timeToSet);

        }
        fakeIfItem.setTime(fakeStartDate.getTime());
        return fakeIfItem;
    }

    private InterfaceItem  vodafonePOC_getFakeInterface(final String ifRef, final String networkRef, final GetInterfaceMode mode) {

        EnergyEfficiencyCache cache = EnergyEfficiencyCache.getInstance();
        EnergyEfficiencyJobData d = cache.get(EnergyEfficiencyEngine.createJobKey(ifRef, networkRef));

        InterfaceItem fakeIfItemRES = new InterfaceItem();

        if(d == null)
            return fakeIfItemRES;

        if(startup) {
            try {
                fakeNow = d.getMonitoredValues().getFirstSampleDate();
                startup = false;
            } catch (MonitoredValuesUnavailableException e) {
                e.printStackTrace();
                return fakeIfItemRES;
            }
        }

        switch (mode) {
            case CURRENT_DAY:
            default:
                InterfaceItem restrictedIfItem = EEEDataProvider.getInterfaceItem(ifRef, networkRef,
                        vodafonePOC_getFakeStartOfDayNEW(ifRef, networkRef, fakeNow),
                        vodafonePOC_getFakeEndOfDayNEW(ifRef, networkRef, fakeNow));

                if (restrictedIfItem.getIfMonitoredState() != 0) {
                    fakeIfItemRES = vodafonePOC_computeFakeIfItemDates(restrictedIfItem,
                            DateUtils.getStartOfDay(new Date(fakeNow.getTime() + (fakeDayCount * ONE_REAL_DAY_MILLS))), (FIFTEEN_MINUTES_IN_MILLS));

                    if ((fakeIfItemRES.getIfObservationList() != null) &&
                            (fakeIfItemRES.getIfPredictionList() != null) &&
                            (fakeIfItemRES.getIfObservationListSize() != 0) &&
                            (fakeIfItemRES.getIfPredictionListSize() != 0)) {
                        long lastFakeMonitoredDate = fakeIfItemRES.getIfObservationList().get(fakeIfItemRES.getIfObservationListSize() - 1).getTime();
                        long lastFakePredictedDate = fakeIfItemRES.getIfPredictionList().get(fakeIfItemRES.getIfPredictionListSize() - 1).getTime();

                        if (lastFakeMonitoredDate >= lastFakePredictedDate) {
                            fakeNow = new Date(fakeNow.getTime() + ONE_FAKE_DAY);
                            fakeDayCount++;
                        }
                    } else {
                        fakeIfItemRES = restrictedIfItem;
                    }
                }
                break;

            case PREVIOUS_DAY:
                Date oneFakeDayAgo = new Date(fakeNow.getTime() - ONE_FAKE_DAY);

                restrictedIfItem = EEEDataProvider.getInterfaceItem(ifRef, networkRef,
                        vodafonePOC_getFakeStartOfDayNEW(ifRef, networkRef, oneFakeDayAgo),
                        vodafonePOC_getFakeEndOfDayNEW(ifRef, networkRef, oneFakeDayAgo));

                if (restrictedIfItem.getIfMonitoredState() != 0) {
                    InterfaceItem fakeIfItem = vodafonePOC_computeFakeIfItemDates(restrictedIfItem,
                            DateUtils.getStartOfDay(new Date(fakeNow.getTime() + (fakeDayCount * ONE_REAL_DAY_MILLS) - ONE_REAL_DAY_MILLS)),
                            (FIFTEEN_MINUTES_IN_MILLS));

                    Long oneHour = new Long(60 * 60 * 1000);
                    fakeIfItemRES = EEEDataProvider.aggregateInterfaceItem(fakeIfItem, oneHour);                }
                else {
                    fakeIfItemRES = restrictedIfItem;
                }
                if(fakeIfItemRES.getIfObservationListSize() == 1) {
                    fakeIfItemRES.setIfConfiguredBandwidthList(new ArrayList<>());
                    fakeIfItemRES.setIfObservationList(new ArrayList<>());
                    fakeIfItemRES.setIfPredictionList(new ArrayList<>());
                }
                break;
            case SAVING:
                fakeIfItemRES = EEEDataProvider.getInterfaceItemSavingData(ifRef, networkRef);
                break;
        }


        return fakeIfItemRES;
    }
}
