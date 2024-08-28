package com.ericsson.vodafone.poc.predictor.api.utils;

import com.ericsson.vodafone.poc.predictor.api.exception.SerieOperationException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Serie<T> {

    private BlockingQueue<T> serieQueue;
    private int encores;

    private Date lastSampleDate;
    private long samplingIntervalSeconds;

    public Date getLastSampleDate() {
        return lastSampleDate;
    }

    private void setLastSampleDate(final Date date) {
        this.lastSampleDate = date;

    }

    public long getSamplingIntervalSeconds() {
        return samplingIntervalSeconds;
    }

    public Serie(int encores, int size, Date lastSampleDate, long samplingIntervalSeconds) throws SerieOperationException {
        checkParameters(encores, size);
        this.serieQueue = new ArrayBlockingQueue<>(size, true);
        this.encores = encores;
        setLastSampleDate(lastSampleDate);
        this.samplingIntervalSeconds = samplingIntervalSeconds;
    }

    public Serie(int encores, int size, List<T> values, Date lastSampleDate, long samplingIntervalSeconds) throws SerieOperationException {
        checkParameters(encores, size);
        this.serieQueue = new ArrayBlockingQueue<>(size, true, values);
        this.encores = encores;
        this.lastSampleDate = lastSampleDate;
        this.samplingIntervalSeconds = samplingIntervalSeconds;
    }

    public int getPeriodicity() {
        return (serieQueue.size() + serieQueue.remainingCapacity())/ encores;
    }

    public long getEncoreDurationInSeconds() {
        return getPeriodicity() * getSamplingIntervalSeconds();
    }

    public void push(final T element) throws SerieOperationException {

        boolean added;

        try {
            this.serieQueue.add(element);
            added = true;
        }
        catch (IllegalStateException e) {
            this.serieQueue.poll();
            added = this.serieQueue.offer(element);
        }
        if(!added) {
            throw new SerieOperationException("Serie::push(): Element not added to queue");
        }
        else {
            final long lastSampleTime = getLastSampleDate().getTime();
            final long newTime = lastSampleTime + (getSamplingIntervalSeconds() * 1000);
            if(lastSampleTime != 0) {
                this.lastSampleDate.setTime(newTime);
            }
        }
    }

    public void push(final T element, final Date lastSampleDate) throws SerieOperationException {
        push(element);
        setLastSampleDate(lastSampleDate);
    }

    public void push(final Serie<T> serie) throws SerieOperationException {
        List<T> listToPush = serie.getSerie();
        for(T curr : listToPush) {
            push(curr);
        }
    }

    public void push(final Serie<T> serie, final Date lastSampleDate) throws SerieOperationException {
        push(serie);
        setLastSampleDate(lastSampleDate);
    }

    public int getPrevIndexInTime() {
        Date now = new Date();
        return getPrevIndexInTime(now);
    }

    public int getPrevIndexInTime(final Date date) {
        Date currDate = new Date(getLastSampleDate().getTime());
        int currIndex = getSize()-1;
        while(date.before(currDate) || date.equals(currDate)) {
            currDate.setTime(currDate.getTime() - samplingIntervalSeconds * 1000);
            currIndex--;
        }
        return currIndex;
    }

    public T getPrevSampleInTime(final Date date) {
        return getSerie().get(getPrevIndexInTime(date));
    }

    public T getPrevSampleInTime() {
        return getSerie().get(getPrevIndexInTime());
    }

    public void clear() {
        serieQueue.clear();
    }

    public List<T> getSerie() {
        List<T> serie = new ArrayList<T>(serieQueue);
        return serie;
    }

    /**
     * Returns a list containing alla the samples after a specified date
     * @param date the date to start from
     * @param includePrevSample if true include also the saple just before the specified date
     * @return the list of samples
     */
    public List<T> getSerieAfter(final Date date, boolean includePrevSample) {

        List<T> list = getSerie();
        int idx;
        if(includePrevSample) {
            idx = getPrevIndexInTime(date);
        }
        else {
            idx = getPrevIndexInTime(date) + 1;
        }
        if(idx < 0) {
            idx = 0;
        }
        return list.subList(idx, list.size());
    }

    public List<T> getSerieBetween(final Date startDate, final Date lastDate, boolean includePrevSample) {

        List<T> list = getSerie();
        int startIdx;
        if(includePrevSample) {
            startIdx = getPrevIndexInTime(startDate);
        }
        else {
            startIdx = getPrevIndexInTime(startDate) + 1;
        }
        int lastIdx = getPrevIndexInTime(lastDate);
        if(startIdx < 0) {
            startIdx = 0;
        }
        if(lastIdx > list.size()-1) {
            lastIdx = list.size()-1;
        }
        if(startIdx <= lastIdx+1) {
            return list.subList(startIdx, lastIdx + 1);
        }
        else {
            return list.subList(0, 0);  //EMPTY
        }
    }

    public List<T> getSerieBetween(final int startIndex, final int lastIndex) {

        List<T> list = getSerie();
        return list.subList(startIndex, lastIndex+1);
    }

    public Pair<Date, Date> getDatesOfSerieBetween(final Date startDate, final Date lastDate, boolean includePrevSample) {

        Pair<Date, Date> resPair = new Pair<>(null, null);
        int startIdx;
        if(includePrevSample) {
            startIdx = getPrevIndexInTime(startDate);
        }
        else {
            startIdx = getPrevIndexInTime(startDate) + 1;
        }
        int lastIdx = getPrevIndexInTime(lastDate);
        if(startIdx < 0) {
            startIdx = 0;
        }
        if(lastIdx > serieQueue.size()-1) {
            lastIdx = serieQueue.size()-1;
        }
        if(startIdx <= lastIdx+1) {
            resPair = new Pair(getSampleDate(startIdx), getSampleDate(lastIdx));
        }
        return resPair;
    }

    public int getSize() {
        return serieQueue.size();
    }

    public T getLastElement() {
        List<T> serie = getSerie();
        if(serie.size() > 0) {
            return serie.get(serie.size() - 1);
        }
        else {
            return null;
        }
    }

    private void checkParameters(int numOfSeries, int size) throws SerieOperationException {
        if (numOfSeries <= 0) {
            throw new SerieOperationException("Serie::Serie: encores MUST be > 0");
        }
        if (size <= 0) {
            throw new SerieOperationException("Serie::Serie: size MUST be > 0");
        }
        if (size % numOfSeries != 0) {
            throw new SerieOperationException("Serie::Serie: size MUST be must be a multiple of encores");

        }

    }

    public Date getFirstSampleDate() {
        //return new Date(lastSampleDate.getTime() - (this.serieQueue.size() -1) * getSamplingIntervalSeconds() * 1000);
        return getSampleDate(0);
    }

    public Date getSampleDate(final int index) {
        if(this.serieQueue.size() == 0 || index < 0 || index > (this.serieQueue.size() -1)) {
            return null;
        }
        return new Date(lastSampleDate.getTime() - (this.serieQueue.size() - 1 - index) * getSamplingIntervalSeconds() * 1000);
    }

}
