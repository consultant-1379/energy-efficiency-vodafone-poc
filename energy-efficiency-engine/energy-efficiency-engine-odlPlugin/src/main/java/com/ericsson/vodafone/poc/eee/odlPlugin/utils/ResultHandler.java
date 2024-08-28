package com.ericsson.vodafone.poc.eee.odlPlugin.utils;

/**
 * Created by esimalb on 8/25/17.
 */
public class ResultHandler {
    private String resultMessage;
    private int code = 0;

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    //HTTP_CREATED = 201
    //HTTP_OK = 200
    public boolean isResultOk() {
        if((code == 200) || (code == 201)) {
            return true;
        }

        return false;
    }

    //HTTP_NOT_FOUND = 404
    public boolean isResultNotFound() {
        if(code == 404) {
            return true;
        }

        return false;
    }

    public String toString(){
        return resultMessage + " - response code: " + Integer.toString(code);
    }
}
