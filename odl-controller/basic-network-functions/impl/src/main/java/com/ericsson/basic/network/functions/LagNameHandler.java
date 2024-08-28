/*
 * Copyright (c) 2017 Ericsson, AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.basic.network.functions;

import java.util.List;

/**
 * @author Ericsson
 */
public class LagNameHandler {

    public static final String LAG_IF_REF_PREFIX = "lag";
    public static final String LAG_IF_FULL_NAME_SEPARATOR = ":";

    public static boolean isIfRefLagName(final String entityLagRelated) {
        return entityLagRelated.contains(LAG_IF_REF_PREFIX);
    }

    public static String getLagName(final String lagIfRef, final String splitToken) {
        String[] split = lagIfRef.split(splitToken);
        if (split == null || split.length < 2) {
            return null;
        }

        return split[0].concat(LAG_IF_FULL_NAME_SEPARATOR).concat(split[1]);
    }

    public static String getLagName(final String lagIfRef) {
        return getLagName(lagIfRef, LAG_IF_FULL_NAME_SEPARATOR);
    }
}
