package com.motadata.data;

import com.motadata.util.Validation;

import java.util.InputMismatchException;

public class DiscoverCredentials {
    String metricType;
    String host;
    String port;
    String user;
    String password;
    String version;
    String Community;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCommunity() {
        return Community;
    }

    public void setCommunity(String community) {
        Community = community;
    }

    public String getMetricType() {
        return metricType;
    }

    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }

    public String getHost() {
        return host;
    }


    public void setHost(String host)throws InputMismatchException {

            if (Validation.isValidIp(host)) {
                this.host = host;
            }

    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


}




