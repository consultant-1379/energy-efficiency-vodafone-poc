/*
 * Copyright (c) 2017 Ericsson, AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.basic.network.functions.inventory;

import com.ericsson.basic.network.functions.inventory.utils.DataTable;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.topology.inventory.rev170714.MINILINK6351;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.topology.inventory.rev170714.MINILINK6352;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.topology.inventory.rev170714.MINILINK6691;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.topology.inventory.rev170714.ProductNameBase;
import com.ericsson.equipment.minilink.MiniLink_ACM;
import com.ericsson.equipment.minilink.MiniLink_ACM_Flavour;
import com.ericsson.equipment.minilink.MiniLink_ChannelSpacing;
import com.ericsson.equipment.minilink.MiniLink6351_Cs50MHz_bdw;
import com.ericsson.equipment.minilink.MiniLink6351_Cs100MHz_bdw;
import com.ericsson.equipment.minilink.MiniLink6351_Cs150MHz_bdw;
import com.ericsson.equipment.minilink.MiniLink6351_Cs200MHz_bdw;
import com.ericsson.equipment.minilink.MiniLink6351_Cs250MHz_bdw;
import com.ericsson.equipment.minilink.MiniLink6352_Cs125MHz_bdw;
import com.ericsson.equipment.minilink.MiniLink6352_Cs250MHz_bdw;
import com.ericsson.equipment.minilink.MiniLink6352_Cs500MHz_bdw;
import com.ericsson.equipment.minilink.MiniLink6352_Cs750MHz_bdw;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ericsson
 */

public class InventoryBandwidth {

    private static final Logger LOG = LoggerFactory.getLogger(InventoryBandwidth.class);
    private static final int NULL_BANDWIDTH = -1;
    private DataTable ML6351_BandwidthTable;
    private DataTable ML6352_BandwidthTable;

    public void init() {
        initMiniLink6351();
        initMiniLink6352();
    }

    private void initMiniLink6351() {
        int max_cs = MiniLink_ChannelSpacing.getMaxValue();
        int max_acm = MiniLink_ACM.getMaxValue();
        ML6351_BandwidthTable = new DataTable(max_cs, max_acm, NULL_BANDWIDTH);

         /*
          * CS 50 MHz
          */
         int i_idx = MiniLink_ChannelSpacing.cs50MHz.getValue();
         int j_idx = MiniLink_ACM.acm4Qam.getValue();
         ML6351_BandwidthTable.setValue(i_idx, j_idx, MiniLink6351_Cs50MHz_bdw.acm4Qam_bdw.getBandwidth());

         i_idx = MiniLink_ChannelSpacing.cs50MHz.getValue();
         j_idx = MiniLink_ACM.acm16Qam.getValue();
         ML6351_BandwidthTable.setValue(i_idx, j_idx, MiniLink6351_Cs50MHz_bdw.acm16Qam_bdw.getBandwidth());

         i_idx = MiniLink_ChannelSpacing.cs50MHz.getValue();
         j_idx = MiniLink_ACM.acm32Qam.getValue();
         ML6351_BandwidthTable.setValue(i_idx, j_idx, MiniLink6351_Cs50MHz_bdw.acm32Qam_bdw.getBandwidth());

         i_idx = MiniLink_ChannelSpacing.cs50MHz.getValue();
         j_idx = MiniLink_ACM.acm64Qam.getValue();
         ML6351_BandwidthTable.setValue(i_idx, j_idx, MiniLink6351_Cs50MHz_bdw.acm64Qam_bdw.getBandwidth());

         i_idx = MiniLink_ChannelSpacing.cs50MHz.getValue();
         j_idx = MiniLink_ACM.acm128Qam.getValue();
         ML6351_BandwidthTable.setValue(i_idx, j_idx, MiniLink6351_Cs50MHz_bdw.acm128Qam_bdw.getBandwidth());

         i_idx = MiniLink_ChannelSpacing.cs50MHz.getValue();
         j_idx = MiniLink_ACM.acm256Qam.getValue();
         ML6351_BandwidthTable.setValue(i_idx, j_idx, MiniLink6351_Cs50MHz_bdw.acm256Qam_bdw.getBandwidth());

         /*
          * CS 100 MHz
          */
         i_idx = MiniLink_ChannelSpacing.cs100MHz.getValue();
         j_idx = MiniLink_ACM.acm4Qam.getValue();
         ML6351_BandwidthTable.setValue(i_idx, j_idx, MiniLink6351_Cs100MHz_bdw.acm4Qam_bdw.getBandwidth());

         i_idx = MiniLink_ChannelSpacing.cs100MHz.getValue();
         j_idx = MiniLink_ACM.acm16Qam.getValue();
         ML6351_BandwidthTable.setValue(i_idx, j_idx, MiniLink6351_Cs100MHz_bdw.acm16Qam_bdw.getBandwidth());

         i_idx = MiniLink_ChannelSpacing.cs100MHz.getValue();
         j_idx = MiniLink_ACM.acm32Qam.getValue();
         ML6351_BandwidthTable.setValue(i_idx, j_idx, MiniLink6351_Cs100MHz_bdw.acm32Qam_bdw.getBandwidth());

         i_idx = MiniLink_ChannelSpacing.cs100MHz.getValue();
         j_idx = MiniLink_ACM.acm64Qam.getValue();
         ML6351_BandwidthTable.setValue(i_idx, j_idx, MiniLink6351_Cs100MHz_bdw.acm64Qam_bdw.getBandwidth());

         i_idx = MiniLink_ChannelSpacing.cs100MHz.getValue();
         j_idx = MiniLink_ACM.acm128Qam.getValue();
         ML6351_BandwidthTable.setValue(i_idx, j_idx, MiniLink6351_Cs100MHz_bdw.acm128Qam_bdw.getBandwidth());

         i_idx = MiniLink_ChannelSpacing.cs100MHz.getValue();
         j_idx = MiniLink_ACM.acm256Qam.getValue();
         ML6351_BandwidthTable.setValue(i_idx, j_idx, MiniLink6351_Cs100MHz_bdw.acm256Qam_bdw.getBandwidth());

         /*
           * CS 150 MHz
           */
         i_idx = MiniLink_ChannelSpacing.cs150MHz.getValue();
         j_idx = MiniLink_ACM.acm4Qam.getValue();
         ML6351_BandwidthTable.setValue(i_idx, j_idx, MiniLink6351_Cs150MHz_bdw.acm4Qam_bdw.getBandwidth());

         i_idx = MiniLink_ChannelSpacing.cs150MHz.getValue();
         j_idx = MiniLink_ACM.acm16Qam.getValue();
         ML6351_BandwidthTable.setValue(i_idx, j_idx, MiniLink6351_Cs150MHz_bdw.acm16Qam_bdw.getBandwidth());

         i_idx = MiniLink_ChannelSpacing.cs150MHz.getValue();
         j_idx = MiniLink_ACM.acm32Qam.getValue();
         ML6351_BandwidthTable.setValue(i_idx, j_idx, MiniLink6351_Cs150MHz_bdw.acm32Qam_bdw.getBandwidth());

         i_idx = MiniLink_ChannelSpacing.cs150MHz.getValue();
         j_idx = MiniLink_ACM.acm64Qam.getValue();
         ML6351_BandwidthTable.setValue(i_idx, j_idx, MiniLink6351_Cs150MHz_bdw.acm64Qam_bdw.getBandwidth());

         i_idx = MiniLink_ChannelSpacing.cs150MHz.getValue();
         j_idx = MiniLink_ACM.acm128Qam.getValue();
         ML6351_BandwidthTable.setValue(i_idx, j_idx, MiniLink6351_Cs150MHz_bdw.acm128Qam_bdw.getBandwidth());

         i_idx = MiniLink_ChannelSpacing.cs150MHz.getValue();
         j_idx = MiniLink_ACM.acm256Qam.getValue();
         ML6351_BandwidthTable.setValue(i_idx, j_idx, MiniLink6351_Cs150MHz_bdw.acm256Qam_bdw.getBandwidth());

         /*
           * CS 200 MHz
           */
         i_idx = MiniLink_ChannelSpacing.cs200MHz.getValue();
         j_idx = MiniLink_ACM.acm4Qam.getValue();
         ML6351_BandwidthTable.setValue(i_idx, j_idx, MiniLink6351_Cs200MHz_bdw.acm4Qam_bdw.getBandwidth());

         i_idx = MiniLink_ChannelSpacing.cs200MHz.getValue();
         j_idx = MiniLink_ACM.acm16Qam.getValue();
         ML6351_BandwidthTable.setValue(i_idx, j_idx, MiniLink6351_Cs200MHz_bdw.acm16Qam_bdw.getBandwidth());

         i_idx = MiniLink_ChannelSpacing.cs200MHz.getValue();
         j_idx = MiniLink_ACM.acm32Qam.getValue();
         ML6351_BandwidthTable.setValue(i_idx, j_idx, MiniLink6351_Cs200MHz_bdw.acm32Qam_bdw.getBandwidth());

         i_idx = MiniLink_ChannelSpacing.cs200MHz.getValue();
         j_idx = MiniLink_ACM.acm64Qam.getValue();
         ML6351_BandwidthTable.setValue(i_idx, j_idx, MiniLink6351_Cs200MHz_bdw.acm64Qam_bdw.getBandwidth());

         i_idx = MiniLink_ChannelSpacing.cs200MHz.getValue();
         j_idx = MiniLink_ACM.acm128Qam.getValue();
         ML6351_BandwidthTable.setValue(i_idx, j_idx, MiniLink6351_Cs200MHz_bdw.acm128Qam_bdw.getBandwidth());

         i_idx = MiniLink_ChannelSpacing.cs200MHz.getValue();
         j_idx = MiniLink_ACM.acm256Qam.getValue();
         ML6351_BandwidthTable.setValue(i_idx, j_idx, MiniLink6351_Cs200MHz_bdw.acm256Qam_bdw.getBandwidth());

          /*
           * CS 250 MHz
           */
         i_idx = MiniLink_ChannelSpacing.cs250MHz.getValue();
         j_idx = MiniLink_ACM.acm4Qam.getValue();
         ML6351_BandwidthTable.setValue(i_idx, j_idx, MiniLink6351_Cs250MHz_bdw.acm4Qam_bdw.getBandwidth());

         i_idx = MiniLink_ChannelSpacing.cs250MHz.getValue();
         j_idx = MiniLink_ACM.acm16Qam.getValue();
         ML6351_BandwidthTable.setValue(i_idx, j_idx, MiniLink6351_Cs250MHz_bdw.acm16Qam_bdw.getBandwidth());

         i_idx = MiniLink_ChannelSpacing.cs250MHz.getValue();
         j_idx = MiniLink_ACM.acm32Qam.getValue();
         ML6351_BandwidthTable.setValue(i_idx, j_idx, MiniLink6351_Cs250MHz_bdw.acm32Qam_bdw.getBandwidth());

         i_idx = MiniLink_ChannelSpacing.cs250MHz.getValue();
         j_idx = MiniLink_ACM.acm64Qam.getValue();
         ML6351_BandwidthTable.setValue(i_idx, j_idx, MiniLink6351_Cs250MHz_bdw.acm64Qam_bdw.getBandwidth());

         i_idx = MiniLink_ChannelSpacing.cs250MHz.getValue();
         j_idx = MiniLink_ACM.acm128Qam.getValue();
         ML6351_BandwidthTable.setValue(i_idx, j_idx, MiniLink6351_Cs250MHz_bdw.acm128Qam_bdw.getBandwidth());

         i_idx = MiniLink_ChannelSpacing.cs250MHz.getValue();
         j_idx = MiniLink_ACM.acm256Qam.getValue();
         ML6351_BandwidthTable.setValue(i_idx, j_idx, MiniLink6351_Cs250MHz_bdw.acm256Qam_bdw.getBandwidth());
    }

    private void initMiniLink6352() {
        int max_cs = MiniLink_ChannelSpacing.getMaxValue();
        int max_acm = MiniLink_ACM.getMaxValue();
        ML6352_BandwidthTable = new DataTable(max_cs, max_acm, NULL_BANDWIDTH);

         /*
          * CS 125 MHz
          */
        int i_idx = MiniLink_ChannelSpacing.cs125MHz.getValue();
        int j_idx = MiniLink_ACM.acmHalfBpsk.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs125MHz_bdw.acmHalfBpsk_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs125MHz.getValue();
        j_idx = MiniLink_ACM.acmHalfBpskLight.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs125MHz_bdw.acmHalfBpskLight_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs125MHz.getValue();
        j_idx = MiniLink_ACM.acm4Qam.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs125MHz_bdw.acm4Qam_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs125MHz.getValue();
        j_idx = MiniLink_ACM.acm4QamLight.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs125MHz_bdw.acm4QamLight_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs125MHz.getValue();
        j_idx = MiniLink_ACM.acm16Qam.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs125MHz_bdw.acm16Qam_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs125MHz.getValue();
        j_idx = MiniLink_ACM.acm16QamLight.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs125MHz_bdw.acm16QamLight_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs125MHz.getValue();
        j_idx = MiniLink_ACM.acm32Qam.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs125MHz_bdw.acm32Qam_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs125MHz.getValue();
        j_idx = MiniLink_ACM.acm32QamLight.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs125MHz_bdw.acm32QamLight_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs125MHz.getValue();
        j_idx = MiniLink_ACM.acm64Qam.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs125MHz_bdw.acm64Qam_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs125MHz.getValue();
        j_idx = MiniLink_ACM.acm64QamLight.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs125MHz_bdw.acm64QamLight_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs125MHz.getValue();
        j_idx = MiniLink_ACM.acm128Qam.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs125MHz_bdw.acm128Qam_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs125MHz.getValue();
        j_idx = MiniLink_ACM.acm128QamLight.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs125MHz_bdw.acm128QamLight_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs125MHz.getValue();
        j_idx = MiniLink_ACM.acm256Qam.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs125MHz_bdw.acm256Qam_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs125MHz.getValue();
        j_idx = MiniLink_ACM.acm256QamLight.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs125MHz_bdw.acm256QamLight_bdw.getBandwidth());

         /*
          * CS 250 MHz
          */
        i_idx = MiniLink_ChannelSpacing.cs250MHz.getValue();
        j_idx = MiniLink_ACM.acmHalfBpsk.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs250MHz_bdw.acmHalfBpsk_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs250MHz.getValue();
        j_idx = MiniLink_ACM.acmHalfBpskLight.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs250MHz_bdw.acmHalfBpskLight_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs250MHz.getValue();
        j_idx = MiniLink_ACM.acm4Qam.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs250MHz_bdw.acm4Qam_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs250MHz.getValue();
        j_idx = MiniLink_ACM.acm4QamLight.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs250MHz_bdw.acm4QamLight_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs250MHz.getValue();
        j_idx = MiniLink_ACM.acm16Qam.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs250MHz_bdw.acm16Qam_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs250MHz.getValue();
        j_idx = MiniLink_ACM.acm16QamLight.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs250MHz_bdw.acm16QamLight_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs250MHz.getValue();
        j_idx = MiniLink_ACM.acm32Qam.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs250MHz_bdw.acm32Qam_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs250MHz.getValue();
        j_idx = MiniLink_ACM.acm32QamLight.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs250MHz_bdw.acm32QamLight_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs250MHz.getValue();
        j_idx = MiniLink_ACM.acm64Qam.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs250MHz_bdw.acm64Qam_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs250MHz.getValue();
        j_idx = MiniLink_ACM.acm64QamLight.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs250MHz_bdw.acm64QamLight_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs250MHz.getValue();
        j_idx = MiniLink_ACM.acm128Qam.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs250MHz_bdw.acm128Qam_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs250MHz.getValue();
        j_idx = MiniLink_ACM.acm128QamLight.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs250MHz_bdw.acm128QamLight_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs250MHz.getValue();
        j_idx = MiniLink_ACM.acm256Qam.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs250MHz_bdw.acm256Qam_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs250MHz.getValue();
        j_idx = MiniLink_ACM.acm256QamLight.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs250MHz_bdw.acm256QamLight_bdw.getBandwidth());

         /*
          * CS 500 MHz
          */
        i_idx = MiniLink_ChannelSpacing.cs500MHz.getValue();
        j_idx = MiniLink_ACM.acmHalfBpsk.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs500MHz_bdw.acmHalfBpsk_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs500MHz.getValue();
        j_idx = MiniLink_ACM.acmHalfBpskLight.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs500MHz_bdw.acmHalfBpskLight_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs500MHz.getValue();
        j_idx = MiniLink_ACM.acm4Qam.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs500MHz_bdw.acm4Qam_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs500MHz.getValue();
        j_idx = MiniLink_ACM.acm4QamLight.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs500MHz_bdw.acm4QamLight_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs500MHz.getValue();
        j_idx = MiniLink_ACM.acm16Qam.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs500MHz_bdw.acm16Qam_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs500MHz.getValue();
        j_idx = MiniLink_ACM.acm16QamLight.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs500MHz_bdw.acm16QamLight_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs500MHz.getValue();
        j_idx = MiniLink_ACM.acm32Qam.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs500MHz_bdw.acm32Qam_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs500MHz.getValue();
        j_idx = MiniLink_ACM.acm32QamLight.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs500MHz_bdw.acm32QamLight_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs500MHz.getValue();
        j_idx = MiniLink_ACM.acm64Qam.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs500MHz_bdw.acm64Qam_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs500MHz.getValue();
        j_idx = MiniLink_ACM.acm64QamLight.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs500MHz_bdw.acm64QamLight_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs500MHz.getValue();
        j_idx = MiniLink_ACM.acm128Qam.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs500MHz_bdw.acm128Qam_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs500MHz.getValue();
        j_idx = MiniLink_ACM.acm128QamLight.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs500MHz_bdw.acm128QamLight_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs500MHz.getValue();
        j_idx = MiniLink_ACM.acm256Qam.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs500MHz_bdw.acm256Qam_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs500MHz.getValue();
        j_idx = MiniLink_ACM.acm256QamLight.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs500MHz_bdw.acm256QamLight_bdw.getBandwidth());

         /*
          * CS 750 MHz
          */
        i_idx = MiniLink_ChannelSpacing.cs750MHz.getValue();
        j_idx = MiniLink_ACM.acmHalfBpsk.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs750MHz_bdw.acmHalfBpsk_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs750MHz.getValue();
        j_idx = MiniLink_ACM.acmHalfBpskLight.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs750MHz_bdw.acmHalfBpskLight_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs750MHz.getValue();
        j_idx = MiniLink_ACM.acm4Qam.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs750MHz_bdw.acm4Qam_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs750MHz.getValue();
        j_idx = MiniLink_ACM.acm4QamLight.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs750MHz_bdw.acm4QamLight_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs750MHz.getValue();
        j_idx = MiniLink_ACM.acm16Qam.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs750MHz_bdw.acm16Qam_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs750MHz.getValue();
        j_idx = MiniLink_ACM.acm16QamLight.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs750MHz_bdw.acm16QamLight_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs750MHz.getValue();
        j_idx = MiniLink_ACM.acm32Qam.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs750MHz_bdw.acm32Qam_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs750MHz.getValue();
        j_idx = MiniLink_ACM.acm32QamLight.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs750MHz_bdw.acm32QamLight_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs750MHz.getValue();
        j_idx = MiniLink_ACM.acm64Qam.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs750MHz_bdw.acm64Qam_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs750MHz.getValue();
        j_idx = MiniLink_ACM.acm64QamLight.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs750MHz_bdw.acm64QamLight_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs750MHz.getValue();
        j_idx = MiniLink_ACM.acm128Qam.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs750MHz_bdw.acm128Qam_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs750MHz.getValue();
        j_idx = MiniLink_ACM.acm128QamLight.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs750MHz_bdw.acm128QamLight_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs750MHz.getValue();
        j_idx = MiniLink_ACM.acm256Qam.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs750MHz_bdw.acm256Qam_bdw.getBandwidth());

        i_idx = MiniLink_ChannelSpacing.cs750MHz.getValue();
        j_idx = MiniLink_ACM.acm256QamLight.getValue();
        ML6352_BandwidthTable.setValue(i_idx, j_idx, MiniLink6352_Cs750MHz_bdw.acm256QamLight_bdw.getBandwidth());
    }

    private Integer getMiniLink6351BandwidthMbps(final MiniLink_ChannelSpacing channelSpacing,
            final MiniLink_ACM acm) {
        int bandwidthMbps = ML6351_BandwidthTable.getValue(channelSpacing.getValue(), acm.getValue());

        return new Integer(bandwidthMbps);
    }

    private Integer getMiniLink6352BandwidthMbps(final MiniLink_ChannelSpacing channelSpacing,
            final MiniLink_ACM acm) {
        int bandwidthMbps = ML6352_BandwidthTable.getValue(channelSpacing.getValue(), acm.getValue());

        return new Integer(bandwidthMbps);
    }

    private MiniLinkBandwidthCoordinates findCoordinates(final DataTable bandwidthTable,
            final MiniLink_ChannelSpacing channelSpacing, final Long bandwidthBps,
            final MiniLink_ACM_Flavour acmFlavour) {
        try {
            MiniLinkBandwidthCoordinates bandwidthCoordinates = null;
            final int bandwidthMbps = bandwidthBps.intValue()*8/1000000;
            final int i_idx = channelSpacing.getValue();
            final int upper_idx = ML6351_BandwidthTable.getNumCol() - 1;
            for (int j = 0; j < upper_idx; j++) {
                int value = bandwidthTable.getValue(i_idx, j);
                int value_after = bandwidthTable.getValue(i_idx, j+1);
                LOG.info("InventoryBandwidth.findCoordinates: {} {} {}",
                        value, value_after, bandwidthMbps);
                if (value_after >= bandwidthMbps && value < bandwidthMbps) {
                    MiniLink_ACM acm = MiniLink_ACM.forCode(j+1);
                    if (!acmFlavour.equals(acm.getFlavour())) {
                        int k = j+1;
                        while (k < upper_idx && !MiniLink_ACM.forCode(k).equals(acm.getFlavour())) {
                            k = k + 1;
                        }
                        if (k < upper_idx) {
                            acm = MiniLink_ACM.forCode(k);
                        }
                    }
                    if (acmFlavour.equals(acm.getFlavour())) {
                        LOG.info("InventoryBandwidth.findCoordinates: j {} ", j);
                        final long convertedBps = value_after*1000000/8;
                        final Long newBandwidthBps  = new Long(convertedBps);
                        bandwidthCoordinates = new MiniLinkBandwidthCoordinates(channelSpacing,
                                 acm, newBandwidthBps);
                        break;
                    }
                 }
            }

            return bandwidthCoordinates;
         } catch (final ArrayIndexOutOfBoundsException e) {
             LOG.error("InventoryBandwidth.findCoordinates: ", e);
         }

        return null;
    }

    public MiniLinkBandwidthCoordinates findCoordinates(final MiniLink_ChannelSpacing channelSpacing,
            final Long bandwidthBps, final MiniLink_ACM_Flavour acmFlavour,
            final java.lang.Class<? extends ProductNameBase> productNameClass) {
        if (MINILINK6351.class.isAssignableFrom(productNameClass)){
            return findCoordinates(ML6351_BandwidthTable, channelSpacing, bandwidthBps, acmFlavour);
        } else if (MINILINK6352.class.isAssignableFrom(productNameClass)){
            return findCoordinates(ML6352_BandwidthTable, channelSpacing, bandwidthBps, acmFlavour);
        }

        return null;
    }

    public Integer getMiniLinkBandwidthMbps(final MiniLink_ChannelSpacing channelSpacing,
            final MiniLink_ACM acm, final java.lang.Class<? extends ProductNameBase> productNameClass) {
        if (MINILINK6351.class.isAssignableFrom(productNameClass)){
            return ML6351_BandwidthTable.getValue(channelSpacing.getValue(), acm.getValue());
        } else if (MINILINK6352.class.isAssignableFrom(productNameClass)){
            return ML6352_BandwidthTable.getValue(channelSpacing.getValue(), acm.getValue());
        }

        return null;
    }

    public Integer getMiniLink6351InputTargetPowerReduction(final MiniLink_ChannelSpacing channelSpacing,
            final Integer lowBandwidth, final Integer highBandwidth) {
       Integer inputTargetPowerReduction = 0;

       LOG.info("InventoryBandwidth.getMiniLink6351InputTargetPowerReduction {} {} {}", channelSpacing,
               lowBandwidth, highBandwidth);

       switch (channelSpacing) {
          case cs50MHz:
              inputTargetPowerReduction =
                      MiniLink6351_Cs50MHz_bdw.getInputTargetPowerReduction(lowBandwidth, highBandwidth);
          break;
          case cs100MHz:
              LOG.info("InventoryBandwidth.getMiniLink6351InputTargetPowerReduction cs100Mhz");
              inputTargetPowerReduction =
                      MiniLink6351_Cs100MHz_bdw.getInputTargetPowerReduction(lowBandwidth, highBandwidth);
          break;
          case cs150MHz:
              inputTargetPowerReduction =
                      MiniLink6351_Cs150MHz_bdw.getInputTargetPowerReduction(lowBandwidth, highBandwidth);
          break;
          case cs200MHz:
              inputTargetPowerReduction =
                      MiniLink6351_Cs200MHz_bdw.getInputTargetPowerReduction(lowBandwidth, highBandwidth);
          break;
          case cs250MHz:
              inputTargetPowerReduction =
                      MiniLink6351_Cs250MHz_bdw.getInputTargetPowerReduction(lowBandwidth, highBandwidth);
          break;
          default:
              inputTargetPowerReduction = 0;
          break;
       }

        return inputTargetPowerReduction;
    }

    public Integer getMiniLink6352InputTargetPowerReduction(final MiniLink_ChannelSpacing channelSpacing,
            final Integer lowBandwidth, final Integer highBandwidth) {
       Integer inputTargetPowerReduction = 0;

       LOG.info("InventoryBandwidth.getMiniLink6351InputTargetPowerReduction {} {} {}", channelSpacing,
               lowBandwidth, highBandwidth);

       switch (channelSpacing) {
          case cs125MHz:
              inputTargetPowerReduction =
                      MiniLink6352_Cs125MHz_bdw.getInputTargetPowerReduction(lowBandwidth, highBandwidth);
          break;
          case cs250MHz:
              inputTargetPowerReduction =
                      MiniLink6352_Cs250MHz_bdw.getInputTargetPowerReduction(lowBandwidth, highBandwidth);
          break;
          case cs500MHz:
              inputTargetPowerReduction =
                      MiniLink6352_Cs500MHz_bdw.getInputTargetPowerReduction(lowBandwidth, highBandwidth);
          break;
          case cs750MHz:
              inputTargetPowerReduction =
                      MiniLink6352_Cs750MHz_bdw.getInputTargetPowerReduction(lowBandwidth, highBandwidth);
          break;
          default:
              inputTargetPowerReduction = 0;
          break;
       }

        return inputTargetPowerReduction;
    }

    public Integer getMiniLinkInputTargetPowerReduction(final MiniLink_ChannelSpacing channelSpacing,
            final Integer lowBandwidth, final Integer highBandwidth,
            final java.lang.Class<? extends ProductNameBase> productNameClass) {
        if (MINILINK6351.class.isAssignableFrom(productNameClass)){
            return getMiniLink6351InputTargetPowerReduction(channelSpacing, lowBandwidth,
                    highBandwidth);
        } else if (MINILINK6352.class.isAssignableFrom(productNameClass)) {
            return getMiniLink6352InputTargetPowerReduction(channelSpacing, lowBandwidth,
                    highBandwidth);
        }

        return null;
    }
}

