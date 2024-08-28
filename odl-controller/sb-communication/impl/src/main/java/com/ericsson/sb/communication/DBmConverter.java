/*
 * Copyright (c) 2017 Ericsson, AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.sb.communication;

import java.lang.Math;

/* reference. http://www.rapidtables.com/electric/dBm.htm

dBm   microWatt
-10   100
-9    126
-8    158
-7    200
-6    251
-5    316
-4    398
-3    501
-2    630
-1    794
 0    1000
 1    1259
 2    1585
 3    1995
 4    2512
 5    3162
 6    3981
 7    5012
 8    6309
 9    7943
 10   10000
 11   12589
 12   15849
 13   19952
 14   25118
 15   31623
 16   39811
 17   50119
 18   63096
 19   79433
 20   100000
 21   125893

*/

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ericsson
 */
public class DBmConverter {

    private static final Logger LOG = LoggerFactory.getLogger(DBmConverter.class);

    static final Integer dBmToMicroWatts(final Integer dBmValue) {
		 double dBmValueDouble = dBmValue.doubleValue();
         double microWattsDouble = ((double)1000)*Math.pow((double)10, (double)dBmValue/(double)10);
         Long longValue = new Long((long) microWattsDouble);
         LOG.info("DBmConverter.dBmToMicroWatts: {} {}", dBmValue, longValue);

         return Integer.valueOf(longValue.intValue());
    }
}
