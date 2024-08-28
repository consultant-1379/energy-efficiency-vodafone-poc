/*
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package com.ericsson.cli.plugin;

import com.google.common.util.concurrent.Futures;
import java.util.concurrent.Future;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddressBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.cli.plugin.rev170714.CliPluginService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.cli.plugin.rev170714.InvokeCommandInput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.cli.plugin.rev170714.InvokeCommandOutput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.cli.plugin.rev170714.InvokeCommandOutputBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CliPluginAgent implements CliPluginAgentOperations, CliPluginService,
        AutoCloseable {

     private static final Logger LOG = LoggerFactory.getLogger(CliPluginAgentOperations.class);
     private final Integer SSH_DEFAULT_PORT = 22;
     private final int MINIMUM_TARGET_INPUT_POWER_FE = -99;
     private final int MAXIMUM_TARGET_INPUT_POWER_FE = -30;
     private RpcProviderRegistry rpcProviderRegistry;
     private BindingAwareBroker.RpcRegistration<CliPluginService> rpcRegistration;
     private LoginCredentialsHandler loginCredentialsHandler = new LoginCredentialsHandler();
     private SshSessionHandler sshSessionHandler;

   /**
     * Starts CLI-plugin Agent
     */
    public void startup() {
        LOG.info("CliPluginAgent.startup");
    }

    /**
     * Shutdown CLI-plugin Agent
     */
    @Override
    public void close() {
        LOG.info("CliPluginAgent.close");
        rpcRegistration.close();
    }
    /**
     * Returns the rpcProviderRegistry
     *
     * @return rpcProviderRegistry
     */
    public RpcProviderRegistry getRpcRegistry() {
        return rpcProviderRegistry;
    }

    /**
     * Sets the rpcProviderRegistry
     *
     * @param rpcProviderRegistry
     */
    public void setRpcRegistry(final RpcProviderRegistry rpcProviderRegistry) {
        LOG.info("CliPluginAgent.setRpcRegistry");
        this.rpcProviderRegistry = rpcProviderRegistry;
        rpcRegistration = rpcProviderRegistry.addRpcImplementation(CliPluginService.class, this);
    }

    @Override
    public Future<RpcResult<InvokeCommandOutput>> invokeCommand(InvokeCommandInput input) {
        InvokeCommandOutputBuilder invokeCommandOutputBuilder = new InvokeCommandOutputBuilder();
        String userLogin;
        String userPassword;
        final IpAddress nodeIpAddress = input.getIpAddress();
        /*
         * For Mini-link default cli user and "" password to first authentication, if not
         * differently specified
         */
        if (input.getUser() != null) {
            userLogin = input.getUser();
        } else {
            userLogin = loginCredentialsHandler.getNodeUserLogin(nodeIpAddress);
        }
        if (input.getPassword() != null) {
            userPassword = input.getPassword();
        } else {
            userPassword = loginCredentialsHandler.getNodeUserPassword(nodeIpAddress);
        }

        boolean result = runCommandInternal(userLogin, userPassword, nodeIpAddress, input.getCommand());
        invokeCommandOutputBuilder.setResultOk(result);

        return Futures.immediateFuture(RpcResultBuilder.<InvokeCommandOutput> success()
                .withResult(invokeCommandOutputBuilder.build()).build());
    }

    @Override
    public void setNodeLoginCredentials(final IpAddress nodeIpAddress,
            final String userLogin, final String userPassword) {
        LOG.info("CliPluginAgent.setNodeLoginCredentials. {} {} {}", nodeIpAddress, userLogin, userPassword);
        loginCredentialsHandler.addNodeUserLogin(nodeIpAddress, userLogin);
        loginCredentialsHandler.addNodeUserPassword(nodeIpAddress, userPassword);
    }

    @Override
    public boolean setIfSelectedMinSelectedMaxAcm(final IpAddress nodeIpAddress, final String ifRef,
            final Integer slot, final Integer ct, final String selectedMinAcm, final String selectedMaxAcm) {
        try {
             /*
             *  Example of CLI command:
             *
             *  "admin\nEricsson1\nconfig slot 1 ct 1 selected-min-acm 4_QAM selected-max-acm 128_QAM\nquit\n"
             *  "admin\nEricsson1\nconfig slot 1 ct 3 selected-max-acm 128_QAM\nquit\n"
             *  "admin\nEricsson1\nconfig slot 3 ct 1 target-input-power-far-end -30\nquit\n"
             *
             *  user, password and quit keyword are placed by the formatCommand procedure
             */
            final String setAcmString = "selected-min-acm " + selectedMinAcm + " selected max-acm " + selectedMaxAcm;
            final String command = "config slot " + slot + " ct " + ct + " " + setAcmString;
            return this.runCommand(nodeIpAddress, command);
        } catch (final Exception e) {
            LOG.error("CliPluginAgent.setIfMinAcmMaxAcm: nodeIpAddress {} ", nodeIpAddress, e);
        }

        return false;
    }

    @Override
    public boolean setIfSelectedMaxAcm(final IpAddress nodeIpAddress, final String ifRef,
            final Integer slot, final Integer ct, final String selectedMaxAcmValue) {
        try {
             /*
             *  Example of CLI command:
             *
             *  "admin\nEricsson1\nconfig slot 1 ct 1 selected-min-acm 4_QAM selected-max-acm 128_QAM\nquit\n"
             *  "admin\nEricsson1\nconfig slot 1 ct 3 selected-max-acm 128_QAM\nquit\n"
             *  "admin\nEricsson1\nconfig slot 3 ct 1 target-input-power-far-end -30\nquit\n"
             *
             *  user, password and quit keyword are placed by the formatCommand procedure
             */
            final String setAcmString = "selected-max-acm " + selectedMaxAcmValue;
            final String command = "config slot " + slot + " ct " + ct + " " + setAcmString;
            return this.runCommand(nodeIpAddress, command);
        } catch (final Exception e) {
            LOG.error("CliPluginAgent.setIfMinAcmMaxAcm: nodeIpAddress {} ", nodeIpAddress, e);
        }

        return false;
    }

    @Override
    public boolean setIfSelectedMinAcm(final IpAddress nodeIpAddress, final String ifRef,
            final Integer slot, final Integer ct, final String selectedMinAcmValue) {
        try {
             /*
             *  Example of CLI command:
             *
             *  "admin\nEricsson1\nconfig slot 1 ct 1 selected-min-acm 4_QAM selected-max-acm 128_QAM\nquit\n"
             *  "admin\nEricsson1\nconfig slot 1 ct 3 selected-max-acm 128_QAM\nquit\n"
             *  "admin\nEricsson1\nconfig slot 3 ct 1 target-input-power-far-end -30\nquit\n"
             *
             *  user, password and quit keyword are placed by the formatCommand procedure
             */
            final String setAcmString = "selected-min-acm " + selectedMinAcmValue;
            final String command = "config slot " + slot + " ct " + ct + " " + setAcmString;
            return this.runCommand(nodeIpAddress, command);
        } catch (final Exception e) {
            LOG.error("CliPluginAgent.setIfMinAcmMaxAcm: nodeIpAddress {} ", nodeIpAddress, e);
        }

        return false;
    }

    private String ckeckTargetInputPowerFarEndBoundary(final String setTargetInputPowerFarEndString) {
        int asIntValue = Integer.parseInt(setTargetInputPowerFarEndString);
        if (asIntValue < MINIMUM_TARGET_INPUT_POWER_FE) {
             return Integer.toString(MINIMUM_TARGET_INPUT_POWER_FE);
        } else if (asIntValue > MAXIMUM_TARGET_INPUT_POWER_FE) {
             return Integer.toString(MAXIMUM_TARGET_INPUT_POWER_FE);
        }

        return setTargetInputPowerFarEndString;
    }

    public boolean setIfTargetInputPowerFarEnd(final IpAddress nodeIpAddress, final String ifRef,
            final Integer slot, final Integer ct, final String targetInputPowerFarEndValue) {
            try {
             /*
             *  Example of CLI command:
             *
             *  "admin\nEricsson1\nconfig slot 3 ct 1 target-input-power-far-end -30\nquit\n"
             *
             ** user, password and quit keyword are placed by the formatCommand procedure
             */
            final String targetInputPowerFarEndValueChecked = ckeckTargetInputPowerFarEndBoundary(targetInputPowerFarEndValue);
            final String setTargetInputPowerFarEndString = "target-input-power-far-end " + targetInputPowerFarEndValueChecked;
            final String command = "config slot " + slot + " ct " + ct + " " + setTargetInputPowerFarEndString;
            return this.runCommand(nodeIpAddress, command);
        } catch (final Exception e) {
            LOG.error("CliPluginAgent.setIfTargetInputPowerFarEnd: nodeIpAddress {} ", nodeIpAddress, e);
        }

        return false;
    }

    private String formatCommand(final String userLogin, final String  userPassword, final String  command) {
        LOG.info("CliPluginAgent.formatCommand: {} {} {}", userLogin, userPassword, command);

        // multiple commands are separated by ";" and should be replaced with "\n"
        String formattedCommand = command.replace(";", "\n");
        formattedCommand = userLogin.concat("\n").concat(userPassword).concat("\n")
                .concat(formattedCommand).concat("\n").concat("quit\n");
        LOG.info("CliPluginAgent.formatCommand: formattedCommand {}", formattedCommand);

        return formattedCommand;
    }

    private boolean runCommandInternal(final String userLogin, final String userPassword,
            final IpAddress nodeIpAddress, final String command) {
        String formattedCommand = formatCommand(userLogin, userPassword, command);
        final String ipAddress = new String(nodeIpAddress.getValue());
        LOG.info("CliPluginAgent.runCommandInternal: {} {} {}", ipAddress, command, formattedCommand);
        final Device device = new Device(ipAddress, LoginCredentialsHandler.SSH_CLI_USER,
                LoginCredentialsHandler.SSH_CLI_PASSWORD, SSH_DEFAULT_PORT);
        SshSessionHandler sshSessionHandler = new SshSessionHandler(device);
        try {
            sshSessionHandler.start();
            sshSessionHandler.runShellCommand(formattedCommand);
            sshSessionHandler.close();
            sshSessionHandler.stop();
            return true;
        } catch (final Exception e) {
            LOG.error("CliPluginAgent.runCommand: ", e);
        }

        return false;
    }

    @Override
    public boolean runCommand(final IpAddress nodeIpAddress, final String command) {
        LOG.info("CliPluginAgent.runCommand: nodeIpAddress {} command {}", nodeIpAddress, command);
        final String userLogin = loginCredentialsHandler.getNodeUserLogin(nodeIpAddress);
        if (userLogin == null) {
            return false;
        }
        final String userPassword = loginCredentialsHandler.getNodeUserPassword(nodeIpAddress);
        if (userPassword == null) {
            return false;
        }

        return runCommandInternal(userLogin, userPassword, nodeIpAddress, command);
    }
}
