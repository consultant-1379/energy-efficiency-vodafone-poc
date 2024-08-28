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


public enum MiniLink_ChannelSpacing {
    cs7MHz (1),
    cs10MHz (2),
    cs14MHz (3),
    cs20MHz (4),
    cs28MHz (5),
    cs30MHz (6),
    cs40MHz (7),
    cs50MHz (8),
    cs56MHz (9),
    cs250MHz (10),
    cs60MHz (11),
    cs500MHz (12),
    cs750MHz (13),
    cs100MHz (14),
    cs150MHz (15),
    cs200MHz (16),
    cs125MHz (17),
    cs80MHz (18),
    cs112MHz (19),
    cs1000MHz (20),
    cs1250MHz (21),
    cs1500MHz (22),
    cs62p5MHz (23),
    cs2000MHz (24),
    unknown (25);

    private int csValue;

    private MiniLink_ChannelSpacing(int csValue) {
        this.csValue = csValue;
    }

    public int getValue() {
        return csValue;
    }

    public static int getMinValue() {
        return cs7MHz.getValue();
    }

    public static int getMaxValue() {
        return unknown.getValue();
    }

    public static MiniLink_ChannelSpacing forCode(final int code) {
        for (MiniLink_ChannelSpacing type : MiniLink_ChannelSpacing.values()) {
            if (type.getValue() == code) {
                return type;
            }
        }

        return null;
    }
}
