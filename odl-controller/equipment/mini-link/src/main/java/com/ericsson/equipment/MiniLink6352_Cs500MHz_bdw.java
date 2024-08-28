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

/*
 * reference: MINI-LINK 6351 Rellease 2.8 Product Specification 1301-HRA 901 17/7 Uen L
 *  $2.1 Traffic Capacity
 *  $4.3.4 Detection Performance
 */

/*
 * ML6351
 */

public enum MiniLink6352_Cs500MHz_bdw {
    acmHalfBpsk_bdw (186, 1, MiniLink_ACM_Flavour.STANDARD),
    acmHalfBpskLight_bdw (201, 2, MiniLink_ACM_Flavour.LIGHT),
    acmBpsk_bdw (373, 1, MiniLink_ACM_Flavour.STANDARD),
    acmBpskLight_bdw (403, 2, MiniLink_ACM_Flavour.LIGHT),
    acm4Qam_bdw (747, 1, MiniLink_ACM_Flavour.STANDARD),
    acm4QamLight_bdw (807, 6, MiniLink_ACM_Flavour.LIGHT),
    acm16Qam_bdw (1495, 1, MiniLink_ACM_Flavour.STANDARD),
    acm16QamLight_bdw (1602, 2, MiniLink_ACM_Flavour.LIGHT),
    acm32Qam_bdw (1868, 1, MiniLink_ACM_Flavour.STANDARD),
    acm32QamLight_bdw (2003, 2, MiniLink_ACM_Flavour.LIGHT),
    acm64Qam_bdw (2242, 1, MiniLink_ACM_Flavour.STANDARD),
    acm64QamLight_bdw (2403, 2, MiniLink_ACM_Flavour.LIGHT),
    acm128Qam_bdw (2616, 1, MiniLink_ACM_Flavour.STANDARD),
    acm128QamLight_bdw (2804, 2, MiniLink_ACM_Flavour.LIGHT),
    acm256Qam_bdw (2990, 1, MiniLink_ACM_Flavour.STANDARD),
    acm256QamLight_bdw (3124, 0, MiniLink_ACM_Flavour.LIGHT);

    private Integer bandwidth;
    private Integer tipfe;
    private MiniLink_ACM_Flavour flavour;

    private MiniLink6352_Cs500MHz_bdw(final Integer bandwidth, final Integer tipfe,
            final MiniLink_ACM_Flavour flavour) {
        this.bandwidth = bandwidth;
        this.tipfe = tipfe;
        this.flavour = flavour;
    }

    public Integer getBandwidth() {
        return bandwidth;
	}

    public Integer getTargetInputPowerDiff() {
        return tipfe;
    }

    public MiniLink_ACM_Flavour getFlavour() {
        return flavour;
    }

    public static Integer getInputTargetPowerReduction(final Integer lowValue,
            final Integer highValue) {
        Integer cumulativeTipfe = 0;
        boolean lowValueFound = false;

        if (lowValue.equals(highValue)) {
            return cumulativeTipfe;
        }

        for (MiniLink6352_Cs500MHz_bdw type : MiniLink6352_Cs500MHz_bdw.values()) {
            if (lowValueFound && type.getBandwidth().intValue() < highValue.intValue()) {
                cumulativeTipfe = cumulativeTipfe + type.getTargetInputPowerDiff();
            }
            if (!lowValueFound && type.getBandwidth().equals(lowValue)) {
                lowValueFound = true;
                cumulativeTipfe = type.getTargetInputPowerDiff();
            }
        }

        return cumulativeTipfe;
    }
}
