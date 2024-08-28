/*
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package com.ericsson.cli.plugin;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;

public interface CliPluginAgentOperations {

    void setNodeLoginCredentials(final IpAddress nodeIpAddress,
            final String login, final String password);

    boolean setIfSelectedMinSelectedMaxAcm(final IpAddress nodeIpAddress, final String ifRef,
            final Integer slot, final Integer ct, final String selectedMinAcmValue,
            final String selectedMaxAcmValue);

    boolean setIfSelectedMinAcm(final IpAddress nodeIpAddress, final String ifRef,
            final Integer slot, final Integer ct, final String selectedMinAcmValue);

    boolean setIfSelectedMaxAcm(final IpAddress nodeIpAddress, final String ifRef,
            final Integer slot, final Integer ct, final String selectedMaxAcmValue);

    boolean setIfTargetInputPowerFarEnd(final IpAddress nodeIpAddress, final String ifRef,
            final Integer slot, final Integer ct, final String targetInputPowerFarEndValue);

    boolean runCommand(final IpAddress nodeIpAddress, final String command);
}
