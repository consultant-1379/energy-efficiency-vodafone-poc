/*
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.cli.plugin.connection;

import com.ericsson.cli.plugin.Device;
import java.io.IOException;
import java.util.List;

public interface SessionHandler<TReq, TRes> extends ImmutableSessionHandler<TReq, TRes> {
    Device getDevice();

    void start() throws IOException, InterruptedException;

    void runExecCommand(final List<String> request) throws
            IOException, InterruptedException;

    void runShellCommand(final String command) throws
            IOException, InterruptedException;

    void close();

    void stop();
}
