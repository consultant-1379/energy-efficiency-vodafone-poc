/*
 * Copyright (c) 2017 Ericsson, AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.snmp.agent;

/**
 * @author Ericsson
 */
public class IfRefAdminStatusHandler {

    private static final String ADMIN_STATUS_UNDETERMINED = "0";
    private static final String WAN_IF_ADMIN_STATUS_UP = "1";
    private static final String WAN_IF_ADMIN_STATUS_DOWN = "2";
    private static final String RF_IF_ADMIN_STATUS_UP = "3";
    private static final String RF_IF_ADMIN_STATUS_DOWN = "2";

    public static String getInterfaceAdminStatusValue(final String ifRef, final boolean adminStatusOn) {
        if (IfRefStackHandler.isWanLanInterface(ifRef)) {
            return adminStatusOn ? WAN_IF_ADMIN_STATUS_UP : WAN_IF_ADMIN_STATUS_DOWN;
        } else if (IfRefStackHandler.isRfInterface(ifRef)) {
            return adminStatusOn ? RF_IF_ADMIN_STATUS_UP : RF_IF_ADMIN_STATUS_DOWN;
        }

        return ADMIN_STATUS_UNDETERMINED;
    }

    public static String getWanLanInterfaceAdminStatusValue(final boolean adminStatusOn) {
        return adminStatusOn ? WAN_IF_ADMIN_STATUS_UP : WAN_IF_ADMIN_STATUS_DOWN;
    }

    public static boolean isWanLanInterfaceAdminStatusUp(final String adminStatus) {
        return adminStatus.equals(WAN_IF_ADMIN_STATUS_UP);
    }

    public static boolean isWanLanInterfaceAdminStatusDown(final String adminStatus) {
        return adminStatus.equals(WAN_IF_ADMIN_STATUS_DOWN);
    }

    public static String getRfInterfaceAdminStatusValue(final boolean adminStatusOn) {
        return adminStatusOn ? RF_IF_ADMIN_STATUS_UP : RF_IF_ADMIN_STATUS_DOWN;
    }

    public static boolean isRfInterfaceAdminStatusUp(final String adminStatus) {
        return adminStatus.equals(RF_IF_ADMIN_STATUS_UP);
    }

    public static boolean isRfInterfaceAdminStatusDown(final String adminStatus) {
        return adminStatus.equals(RF_IF_ADMIN_STATUS_DOWN);
    }
}
