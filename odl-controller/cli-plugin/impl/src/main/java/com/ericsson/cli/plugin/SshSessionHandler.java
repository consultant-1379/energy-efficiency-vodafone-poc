/*
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.cli.plugin;

import com.ericsson.cli.plugin.connection.SessionHandler;
import com.ericsson.cli.plugin.connection.SessionState;
import com.google.common.base.Predicate;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.Charset;
import java.security.Security;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.apache.sshd.ClientChannel;
import org.apache.sshd.ClientSession;
import org.apache.sshd.common.util.NoCloseInputStream;
import org.apache.sshd.common.util.NoCloseOutputStream;
import org.apache.sshd.SshClient;
import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SshSessionHandler implements SessionHandler<List<String>, List<String>> {
    private static final Logger LOG = LoggerFactory.getLogger(SshSessionHandler.class);

    public static final int MAX_FAIL_ALLOW = 5;
    public static final int RETRY_COUNT = 5;

    private ExecutorService executor = Executors.newFixedThreadPool(1);
    private Device device;
    private transient boolean started = false;
    private SshClient client;
    private ClientSession clientSession;
    private SessionState state = SessionState.NOT_STARTED;
    private SessionReader reader = null;

    public SshSessionHandler(Device device) {
        super();
        Security.addProvider(new BouncyCastleProvider());
        this.device = device;
        this.reader = new SessionReader().promptChecker(
                new CliPromptChecker().postfixPrompt("#"))
                .executorService(executor).maxFailAllow(MAX_FAIL_ALLOW);
    }

    public SshSessionHandler promptChecker(Predicate<String> promptChecker) {
        if (reader != null) {
            reader.promptChecker(promptChecker);
        }
        return this;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    private boolean authenticateToServer(final Device device) throws IOException, InterruptedException {
        LOG.info("SshSessionHandler.start: setUpDefaultClient to {} {}", device.getIpAddress(),
                device.getConnectionPort());
        client = SshClient.setUpDefaultClient();
        client.setServerKeyVerifier(AcceptAllServerKeyVerifier.INSTANCE);

        LOG.info("SshSessionHandler.start: client.start to {} {}", device.getIpAddress(),
                device.getConnectionPort());
        client.start();

        LOG.info("SshSessionHandler.start: client.connect to {} {} {}", device.getUsername(),
                device.getIpAddress(), device.getConnectionPort());
        clientSession = client.connect(device.getUsername(), device.getIpAddress(), device.getConnectionPort())
                .await().getSession();

        LOG.info("SshSessionHandler.start: client.authPassword to {} {}", device.getUsername(),
                device.getPassword());
        clientSession.authPassword(device.getUsername(), device.getPassword());

        LOG.info("SshSessionHandler.start: client.waitFor to {} {} {}", device.getIpAddress(),
                device.getConnectionPort());
        int authState = clientSession.waitFor(ClientSession.WAIT_AUTH | ClientSession.CLOSED
                | ClientSession.AUTHED, 0L);

        LOG.info("SshSessionHandler.start: authState {}", authState);

        if ((authState & ClientSession.AUTHED) != 0) {
           LOG.info("SshSessionHandler.start: AUTHENTICATED");
           state = SessionState.LOGGED_IN;
           return true;
        }

        if ((authState & ClientSession.CLOSED) != 0) {
           LOG.error("SshSessionHandler.start: authoritation state and closed status mismatch");
        }

        return false;
    }

    @Override
    public synchronized void start() throws IOException, InterruptedException {
        LOG.info("SshSessionHandler.start: setUpDefaultClient to {} {} {}", device.getIpAddress(),
                device.getConnectionPort());

        boolean authenticated = authenticateToServer(device);
        if (authenticated) {
            this.state = SessionState.LOGGED_IN;
        }

        started = true;
    }

    @Override
    public synchronized void runExecCommand(List<String> request) {
        if (request == null || request.isEmpty()) {
            LOG.error("SshSessionHandler.execute: request is null");
            return;
        }

        for (String req : request) {
            try {
                execCommand(req);
            } catch (final Exception e) {
                LOG.error("SshSessionHandler.execute: ", e);
            }
        }
    }

    private void execCommand(String command) throws IOException, InterruptedException {
        LOG.info("SshSessionHandler.executeRemoteCommand: executing {} on node {} {}",
                command, device.getIpAddress(), device.getConnectionPort());

        final OutputStream channelErr = new ByteArrayOutputStream(Byte.MAX_VALUE);
        final OutputStream channelOut = new ByteArrayOutputStream(Byte.MAX_VALUE);

        ClientChannel channel = clientSession.createExecChannel(command);
        channel.setOut(channelOut);
        channel.setErr(channelErr);
        LOG.info("SshSessionHandler.execCommand: channel.open().await()");
        channel.open().await();

        LOG.info("SshSessionHandler.execCommand: channelOut: {} channelErr: {}",
                channelOut.toString(), channelErr.toString());

        int waitMask = channel.waitFor(ClientChannel.TIMEOUT | ClientChannel.CLOSED, 2000L);
        if ((waitMask & ClientChannel.TIMEOUT) != 0) {
            LOG.warn("SshSessionHandler.execCommand: timeout");
        }

        LOG.info("SshSessionHandler.execCommand: waitMask: {} channelOut: {} channelErr: {}",
                     waitMask, channelOut.toString(), channelErr.toString());

        Integer exitStatus = channel.getExitStatus();
        LOG.info("SshSessionHandler.execCommand: exitStatus {}", exitStatus);

        state = SessionState.NOT_CONNECTED;
    }

    @Override
    public synchronized void runShellCommand(final String command) {
        try {
            LOG.info("SshSessionHandler.runShellCommand: executing {} on node {} {}",
                    command, device.getIpAddress(), device.getConnectionPort());

            final OutputStream channelErr = new ByteArrayOutputStream(Byte.MAX_VALUE);
            final OutputStream channelOut = new ByteArrayOutputStream(Byte.MAX_VALUE);

            ChannelShell channel = (ChannelShell)clientSession.createShellChannel();
            channel.setOut(channelOut);
            channel.setErr(channelErr);

            /*
             *  examples of shell command:
             *    "admin\nEricsson1\nconfig slot 1 ct 1 selected-min-acm 4_QAM selected-max-acm 128_QAM\nquit\n"
             *    "admin\nEricsson1\nconfig slot 1 ct 1 selected-max-acm 128_QAM\nquit\n"
             */
             channel.open().await();
             channel.getInvertedIn().write(command.getBytes("UTF-8"));
             channel.getInvertedIn().flush();

             Integer waitMask = channel.waitFor(ClientChannel.TIMEOUT | ClientChannel.CLOSED, 2000L);
             if ((waitMask & ClientChannel.TIMEOUT) != 0) {
                 LOG.warn("SshSessionHandler.runShellCommand: timeout");
             }

             LOG.info("SshSessionHandler.runShellCommand: waitMask: {} channelOut: {} channelErr: {}",
                     waitMask, channelOut.toString(), channelErr.toString());

             channel.close(false);
             Integer exitStatus = channel.getExitStatus();
             LOG.info("SshSessionHandler.runShellCommand: exitStatus {}", exitStatus);

             state = SessionState.NOT_CONNECTED;
        } catch (final Exception e) {
             LOG.error("SshSessionHandler.runShellCommand: ", e);
        }
    }

    @Override
    public synchronized void close() {
        if (clientSession != null && started) {
            clientSession.close(true);
        }

        state = SessionState.NOT_CONNECTED;
    }

    @Override
    public synchronized void stop() {
        if (client != null) {
            client.stop();
        }

        state = SessionState.NOT_STARTED;
    }

    @Override
    public synchronized SessionState getSessionState() {
        return state;
    }

    public Device getDevice() {
        return device;
    }
}
