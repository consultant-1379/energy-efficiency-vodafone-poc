/*
 * Copyright (c) 2017 Ericsson, AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.snmp.agent;

/*
 * with indexes: {entLogicalIndex, xfRadioFrameId} access the xfCarrierTerminationCapabilityTable, obtaining for example:
 *
 *    "oid": "1.3.6.1.4.1.193.81.3.4.5.1.4.1.6.5.1",
 *    "value": "6c:92:4d:80:00:00:00:00:00:00:00:00:00:00:00:00"
 *
 * the value is a bit-mask with bit order as "b0b1b2...b31", each bit represents a valid selectable ACM (for example 64QAMStrong)
 * To each ACM value (for example 64QAMStrong) there is a bandwidth capacity mapped to.
 *
 * with indexes: {entLogicalIndex, xfACMIndex} access the xfACMProfileCapacityTable to enquire for xfACMCapacity,
 * which represents the capacity in Kbps of the ACM profile identified by the positional index
 * xfACMIndex of the xfACMProfile in xfCarrierTerminationCapabilityTable."
 *
*/

/**
 * @author Ericsson
 */

 /*

public class AcmCoding {

    private enum AcmCodingMask {
        qam4Strong(1),
        qam4Std(2),
        qam4Light(3),

        qam16Strong(4),
        qam16Std(5),
        qam16Light(6),

        qam32Strong(7),
        qam32Std(8),
        qam32Light(9),

        qam64Strong(10),
        qam64Std(11),
        qam64Light(12),

        qam128Strong(13),
        qam128Std(14),
        qam128Light(15),

        qam256Strong(16),
        qam256Std(17),
        qam256Light(18),

        qam512Strong(19),
        qam512Std(20),
        qam512Light(21),

        qam1024Strong(22),
        qam1024Std(23),
        qam2014Light(24),

        qam2048Strong(25),
        qam2048Std(26),
        qam2048Light(27),

        qam4096Strong(28),
        qam4096Std(29),
        qam4096Light(30);
    }

    private final int acmCodingMask;

    public AcmCoding(int acmCodingMask) {
        this.acmCodingMask = acmCodingMask;
    }

    public int getAcmCodingMask() {
        return acmCodingMask;
    }
}
*/