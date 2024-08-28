package com.ericsson.vodafone.poc.eee.services.input.rest.resources.utils;

import java.io.Serializable;

public enum GetInterfaceMode implements Serializable {

    CURRENT_DAY("current_day"),
    PREVIOUS_DAY("previous_day"),
    SAVING("saving");

    private final String mode;

    private GetInterfaceMode(String s) {
        mode = s;
    }

    public boolean equals(String otherMode) {
        // (otherName == null) check is not needed because name.equals(null) returns false
        return mode.equals(otherMode);
    }

    public String toString() {
        return this.mode;
    }



}
