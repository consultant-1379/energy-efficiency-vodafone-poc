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

public enum MiniLink_ACM {
    acmHalfBpsk (1, "HALF_BPSK", MiniLink_ACM_Flavour.STANDARD),
    acmHalfBpskLight (2, "HALF_BPSK_LIGHT", MiniLink_ACM_Flavour.LIGHT),
    acmHalfBpskStrong (3, "HALF_BPSK_STRONG", MiniLink_ACM_Flavour.STRONG),
    acmBpsk (4, "BPSK", MiniLink_ACM_Flavour.STANDARD),
    acmBpskLight (5, "BPSK_LIGHT", MiniLink_ACM_Flavour.LIGHT),
    acmBpskStrong (6, "BPSK_STRONG", MiniLink_ACM_Flavour.STRONG),
    acm4Qam (7, "4_QAM", MiniLink_ACM_Flavour.STANDARD),
    acm4QamLight (8, "4_QAM_LIGHT", MiniLink_ACM_Flavour.LIGHT),
    acm4QamStrong (9, "4_QAM_STRONG", MiniLink_ACM_Flavour.STRONG),
    acm16Qam (10, "16_QAM", MiniLink_ACM_Flavour.STANDARD),
    acm16QamLight (11, "16_QAM_LIGHT", MiniLink_ACM_Flavour.LIGHT),
    acm16QamStrong (12, "16_QAM_STRONG", MiniLink_ACM_Flavour.STRONG),
    acm32Qam (13, "32_QAM", MiniLink_ACM_Flavour.STANDARD),
    acm32QamLight (14, "32_QAM_LIGHT", MiniLink_ACM_Flavour.LIGHT),
    acm32QamStrong (15, "32_QAM_STRONG", MiniLink_ACM_Flavour.STRONG),
    acm64Qam (16, "64_QAM", MiniLink_ACM_Flavour.STANDARD),
    acm64QamLight (17, "64_QAM_LIGHT", MiniLink_ACM_Flavour.LIGHT),
    acm64QamStrong (18, "64_QAM_STRONG", MiniLink_ACM_Flavour.STRONG),
    acm128Qam (19, "128_QAM", MiniLink_ACM_Flavour.STANDARD),
    acm128QamLight (20, "128_QAM_LIGHT", MiniLink_ACM_Flavour.LIGHT),
    acm128QamStrong (21, "128_QAM_STRONG", MiniLink_ACM_Flavour.STRONG),
    acm256Qam (22, "256_QAM", MiniLink_ACM_Flavour.STANDARD),
    acm256QamLight (23, "256_QAM_LIGHT", MiniLink_ACM_Flavour.LIGHT),
    acm256QamStrong (24, "256_QAM_STRONG", MiniLink_ACM_Flavour.STRONG),
    acm512Qam (25, "512_QAM_STRONG", MiniLink_ACM_Flavour.STANDARD),
    acm512QamLight (26, "512_QAM_LIGHT", MiniLink_ACM_Flavour.LIGHT),
    acm512QamStrong (27, "512_QAM_STRONG", MiniLink_ACM_Flavour.STRONG),
    acm1024Qam (28, "1024_QAM", MiniLink_ACM_Flavour.STANDARD),
    acm1024QamLight (29, "1024_QAM_LIGHT", MiniLink_ACM_Flavour.LIGHT),
    acm1024QamStrong (30, "1024_QAM_STRONG", MiniLink_ACM_Flavour.STRONG),
    acm2048Qam (31, "2048_QAM", MiniLink_ACM_Flavour.STANDARD),
    acm2048QamLight (32, "2048_QAM_LIGHT", MiniLink_ACM_Flavour.LIGHT),
    acm2048QamStrong (33, "2048_QAM_STRONG", MiniLink_ACM_Flavour.STRONG),
    acm4096Qam (34, "4096_QAM", MiniLink_ACM_Flavour.STANDARD),
    acm4096QamLight (35, "4096_QAM_LIGHT", MiniLink_ACM_Flavour.LIGHT),
    acm4096QamStrong (36, "4096_QAM_STRONG", MiniLink_ACM_Flavour.STRONG);

    private int acmValue;
    private String acmValueAsString;
    private MiniLink_ACM_Flavour flavour;

    private MiniLink_ACM(final int acmValue, final String acmValueAsString, MiniLink_ACM_Flavour flavour) {
        this.acmValue = acmValue;
        this.acmValueAsString = acmValueAsString;
        this.flavour = flavour;
    }

    public int getValue() {
	    return acmValue;
	}

    public String getValueAsString() {
	    return acmValueAsString;
	}

    public MiniLink_ACM_Flavour getFlavour() {
    	return flavour;
    }

    public static int getMinValue() {
        return acmHalfBpsk.getValue();
    }

    public static int getMaxValue() {
        return acm4096QamStrong.getValue();
    }

    public static MiniLink_ACM forCode(final int code) {
        for (MiniLink_ACM type : MiniLink_ACM.values()) {
            if (type.getValue() == code) {
                return type;
            }
        }

        return null;
    }
}
