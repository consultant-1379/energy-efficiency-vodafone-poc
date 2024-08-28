/*
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.cli.plugin.connection;

public interface ImmutableSessionHandler<TReq, TRes> {
    boolean isStarted();

    SessionState getSessionState();

}
