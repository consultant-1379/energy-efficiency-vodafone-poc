/*
 * Copyright (c) 2017 Ericsson, AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.cli.plugin;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;

/**
 * @author Ericsson
 */
public class LoginCredentialsHandler {

    public static final String SSH_CLI_USER = "cli";
    public static final String SSH_CLI_PASSWORD = "";

    private Map<IpAddress, String> userLoginMap = new HashMap<>();
    private Map<IpAddress, String> passwordLoginMap = new HashMap<>();

    public void addNodeUserLogin(final IpAddress nodeIpAddress, final String userLogin) {
        userLoginMap.put(nodeIpAddress, userLogin);
    }

    public String getNodeUserLogin(final IpAddress nodeIpAddress) {
        return userLoginMap.get(nodeIpAddress);
    }

    public void addNodeUserPassword(final IpAddress nodeIpAddress, final String userPassword) {
        passwordLoginMap.put(nodeIpAddress, userPassword);
    }

    public String getNodeUserPassword(final IpAddress nodeIpAddress) {
        return passwordLoginMap.get(nodeIpAddress);
    }
}
