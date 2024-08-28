/*
 * Copyright (c) 2015 Ericsson, AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.snmp.agent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ML63xxRegEx {

    private static final String MINI_LINK_63XX_IFNAME_REGEX = "^.*([0-9]).*([0-9])$";

    private static String convertIfName(final String ifName) {
        Pattern r = Pattern.compile(MINI_LINK_63XX_IFNAME_REGEX);
        Matcher m = r.matcher(ifName);
        String ifNameNormalized = null;

        if (m.find()) {
            String slot = m.group(1);
            String port = m.group(2);
            if (ifName.toLowerCase().contains("radiolinkterminal")) {
                ifNameNormalized = slot.concat(".").concat(port); // supposed compatible with 66xx
            } else {
                ifNameNormalized = slot.concat("/").concat(port); // supposed compatible with 66xx
            }
        }

        return ifNameNormalized;
    }

    public static String normalizeIfName(final String ifName) {
        return convertIfName(ifName);
    }

    public static String extractIfDescr(final String ifName) {
        String ifDescr = null;
        if (ifName.toLowerCase().contains("wan")) {
            ifDescr = "WAN";
        } else if (ifName.toLowerCase().contains("lan")) {
             ifDescr = "LAN";
        } else if (ifName.toLowerCase().contains("radiolinkterminal")) {
            ifDescr = "RAU IF";
        } else if (ifName.toLowerCase().contains("carriertermination")) {
            ifDescr = "RF";
        }

        return ifDescr;
    }

    public static Integer extractSlotId(final String ifName) {
        Pattern r = Pattern.compile(MINI_LINK_63XX_IFNAME_REGEX);
        Matcher m = r.matcher(ifName);
        String ifNameNormalized = null;
        Integer slot = 0;
        if (m.find()) {
            slot = Integer.parseInt(m.group(1));
        }
        return slot;
    }

    public static boolean isWan(final String ifData) {
        return ifData.toLowerCase().contains("wan");
    }

    public static boolean isLan(final String ifData) {
        return ifData.toLowerCase().contains("lan");
    }

    public static boolean isRF(final String ifData) {
        return (ifData.toUpperCase().contains("RF") ||
                ifData.toLowerCase().contains("radiolinkterminal"));
    }

    public static boolean isRauIf(final String ifDescr) {
        return (ifDescr.toUpperCase().contains("RAU") ||
                ifDescr.toLowerCase().contains("carriertermination"));
    }

}
