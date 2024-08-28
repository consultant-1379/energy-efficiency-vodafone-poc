/*
 * Copyright (c) 2017 Ericsson, AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.snmp.agent;

import java.util.List;

/**
 * @author Ericsson
 */
public class IfRefStackHandler {

    private static final String BONDING_INTERFACE = "BONDING";
    private static final String RAU_IF_INTERFACE = "RAU";
    private static final String RF_INTERFACE = "RF";
    private static final String WAN_INTERFACE = "WAN";
    private static final String LAN_INTERFACE = "LAN";

    public static boolean isWanInterface(final String ifRef) {
        return ifRef.contains(WAN_INTERFACE);
    }

    public static boolean isLanInterface(final String ifRef) {
        return ifRef.contains(LAN_INTERFACE);
    }

    public static boolean isWanLanInterface(final String ifRef) {
        return ifRef.contains(WAN_INTERFACE) || ifRef.contains(LAN_INTERFACE);
    }

    public static boolean isBondingInterface(final String ifRef) {
        return ifRef.contains(BONDING_INTERFACE);
    }

    public static boolean isRauInterface(final String ifRef) {
        return ifRef.contains(RAU_IF_INTERFACE);
    }

    public static boolean isRfInterface(final String ifRef) {
        return ifRef.contains(RF_INTERFACE);
    }

    public static String getBondingInterface(final List<String> ifRefStack) {
        String foundIfRef = null;
        for (String ifRef : ifRefStack) {
            if (isBondingInterface(ifRef)) {
               foundIfRef = ifRef;
               break;
            }
        }

        return foundIfRef;
    }

    public static String getRauInterface(final List<String> ifRefStack) {
        String foundIfRef = null;
        for (String ifRef : ifRefStack) {
            if (isRauInterface(ifRef)) {
               foundIfRef = ifRef;
               break;
            }
        }

        return foundIfRef;
    }

    public static String getRfInterface(final List<String> ifRefStack) {
        String foundIfRef = null;
        for (String ifRef : ifRefStack) {
            if (isRfInterface(ifRef)) {
               foundIfRef = ifRef;
               break;
            }
        }

        return foundIfRef;
    }

    public static String getRfIfRefSlot(final String rfIfRef) {
        String[] splitted = rfIfRef.split(":");
        if(splitted.length == 0) {
            return null;
        }
        String ifPath = splitted[1];

        splitted  = ifPath.split("-");
        String[] slotInfo = null;
        if (splitted[1].contains(".")) {
            slotInfo = splitted[1].split("\\.");
            if (slotInfo.length >= 1) {
                slotInfo = slotInfo[0].split("\\/");
                if (slotInfo.length >= 1) {
                   return slotInfo[1];
                }
            }
        } else {
            slotInfo = splitted[1].split("\\/");
            if (slotInfo.length >= 1) {
                return slotInfo[0];
            }
        }

        return null;
    }

    public static String getCarrierTerminalId(final String rfIfRef) {
        /*
         * it it contains a dot, it is a ML6691 path;
         * for example: mini-link-6691-1:RF-1/1.3/1 (slot=1, ct=3)
         * if not, it is ML6351/6352
         * for example: mini-link-6351-2:RF-1/2  (slot=1, ct=2)
         */
        String[] splitted = rfIfRef.split(":");
        if(splitted.length == 0) {
            return null;
        }
        String ifPath = splitted[1];

        splitted  = ifPath.split("-");
        String[] ctInfo = null;
        if (splitted.length >= 1 && splitted[1].contains(".")) {
            ctInfo = splitted[1].split("\\.");
            if (ctInfo.length >= 1) {
                ctInfo = ctInfo[1].split("\\/");
                if (ctInfo.length >= 1) {
                   return ctInfo[1];
                }
            }
        } else if (splitted.length >= 1){
            ctInfo = splitted[1].split("\\/");
            if (ctInfo.length >= 1) {
                return ctInfo[1];
            }
        }

        return null;
    }
}
