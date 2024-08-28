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
public class IfRefOperStatusHandler {

    private static final String OPER_STATUS_UNDETERMINED = "0";

    private static final String IF_OPER_STATUS_UP = "1";
    private static final String IF_OPER_STATUS_UP_STRING = "up";

    private static final String IF_OPER_STATUS_DOWN = "2";
    private static final String IF_OPER_STATUS_DOWN_STRING = "down";

    private static final String IF_OPER_STATUS_TESTING = "3";
    private static final String IF_OPER_STATUS_TESTING_STRING = "testing";

    private static final String IF_OPER_STATUS_UNKNOWN = "4";
    private static final String IF_OPER_STATUS_UNKNOWN_STRING = "unknown";

    private static final String IF_OPER_STATUS_DORMANT = "5";
    private static final String IF_OPER_STATUS_DORMANT_STRING = "dormant";

    private static final String IF_OPER_STATUS_NOT_PRESENT = "6";
    private static final String IF_OPER_STATUS_NOT_PRESENT_STRING = "notpresent";

    private static final String IF_OPER_STATUS_LAYER_DOWN = "7";
    private static final String IF_OPER_STATUS_LAYER_DOWN_STRING = "layerdown";

    public static boolean isInterfaceOperStatusUp(final String operStatus) {
        return operStatus.equals(IF_OPER_STATUS_UP) || operStatus.equals(IF_OPER_STATUS_UP_STRING);
    }

    public static String convertSnmpValueToString(final String snmpValue) {
        if (snmpValue.equals(IF_OPER_STATUS_UP)) {
            return IF_OPER_STATUS_UP_STRING;
        }
        if (snmpValue.equals(IF_OPER_STATUS_DOWN)) {
            return IF_OPER_STATUS_DOWN_STRING;
        }
        if (snmpValue.equals(IF_OPER_STATUS_UNKNOWN)) {
            return IF_OPER_STATUS_UNKNOWN_STRING;
        }
        if (snmpValue.equals(IF_OPER_STATUS_TESTING)) {
            return IF_OPER_STATUS_TESTING_STRING;
        }
        if (snmpValue.equals(IF_OPER_STATUS_DORMANT)) {
            return IF_OPER_STATUS_DORMANT_STRING;
        }
        if (snmpValue.equals(IF_OPER_STATUS_NOT_PRESENT)) {
            return IF_OPER_STATUS_NOT_PRESENT_STRING;
        }
        if (snmpValue.equals(IF_OPER_STATUS_LAYER_DOWN)) {
            return IF_OPER_STATUS_LAYER_DOWN_STRING;
        }

        return null;
    }
}
