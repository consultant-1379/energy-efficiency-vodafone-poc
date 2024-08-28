/*
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package com.ericsson.cli.plugin;

public class Device {

    private final String ipAddress;
    private final String username;
    private final String password;
    private final Integer connectionPort;

    public Device(final String ipAddress, final String username, final String password,
            final Integer connectionPort)
    {
        this.ipAddress = ipAddress;
        this.username = username;
        this.password = password;
        this.connectionPort = connectionPort;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Integer getConnectionPort() {
    	return connectionPort;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Device [ip=");
        builder.append(ipAddress);
        builder.append(", username=");
        builder.append(username);
        builder.append(", password=");
        builder.append(password);
        builder.append(", connectionPort=");
        builder.append(connectionPort);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ipAddress == null) ? 0 : ipAddress.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        Device other = (Device) obj;
        if (ipAddress == null) {
            if (other.ipAddress != null) {
                return false;
            }
        } else if (!ipAddress.equals(other.ipAddress)) {
             return false;
        }

        return true;
    }
}
