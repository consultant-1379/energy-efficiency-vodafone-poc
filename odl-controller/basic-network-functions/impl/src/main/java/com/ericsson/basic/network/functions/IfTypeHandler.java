/*
 * Copyright (c) 2017 Ericsson, AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.basic.network.functions;

/**
 * @author Ericsson
 */
public class IfTypeHandler {

    public static boolean isWan(final String ifData) {
        return ifData.toLowerCase().contains("wan");
    }

    public static boolean isLan(final String ifData) {
        return ifData.toLowerCase().contains("lan");
    }

    public static boolean isRau(final String ifData) {
	   return ifData.toLowerCase().contains("rau");
    }

    public static boolean isRf(final String ifData) {
	   return ifData.toLowerCase().contains("rf");
    }

    public static boolean isBonding(final String ifData) {
	   return ifData.toLowerCase().contains("bonding");
    }

    public static boolean isRadio(final String ifData) {
        return isRau(ifData) || isRf(ifData) || isBonding(ifData);
    }
}
