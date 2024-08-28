/*
 * Copyright (c) 2017 Ericsson, AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.equipment.minilink;

/**
 * @author ericsson
 */

public enum MiniLink_ACM_Flavour {
    LIGHT ("LIGHT"),
    STANDARD ("STANDARD"),
    STRONG ("STRONG");

    final String flavour;

    private MiniLink_ACM_Flavour(final String flavour) {
        this.flavour = flavour;
    }
}
