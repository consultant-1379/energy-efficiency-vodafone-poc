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
public class BondingHandler {

    public static final String BONDING_FICTITIOUS_MASTER_1 = "RF-1/1.1/1";
    public static final String BONDING_FICTITIOUS_MASTER_2 = "RF-1/1.3/1";

    public static boolean isFictitiousMaster(final String supportingLinkRef) {
        return supportingLinkRef.contains(BONDING_FICTITIOUS_MASTER_1) ||
               supportingLinkRef.contains(BONDING_FICTITIOUS_MASTER_2);
    }
}
