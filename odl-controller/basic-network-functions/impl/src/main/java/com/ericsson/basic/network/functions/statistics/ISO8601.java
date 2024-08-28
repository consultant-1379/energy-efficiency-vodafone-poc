/*
 * Copyright (c) 2017 Ericsson, AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.basic.network.functions.statistics;

import com.google.common.base.Preconditions;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;

final public class ISO8601 {
    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    private static final String dateTimePatternString = DateAndTime.PATTERN_CONSTANTS.get(0);
    private static final Pattern dateTimePattern = Pattern.compile(dateTimePatternString);

    public DateAndTime dateAndTime(final Date dateTime) {
        Preconditions.checkState(DateAndTime.PATTERN_CONSTANTS.size() == 1);
        String formattedDateTime = formatDateTime(dateTime);
        Matcher matcher = dateTimePattern.matcher(formattedDateTime);
        Preconditions.checkState(matcher.matches(), "Formatted datetime %s does not match pattern %s",
                formattedDateTime, dateTimePattern);

        return new DateAndTime(formattedDateTime);
    }

    public DateAndTime dateAndTime() {
        return dateAndTime(new Date());
    }

    private String formatDateTime(final Date dateTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(ISO_DATE_FORMAT);
        return dateFormat.format(dateTime);
    }
}
