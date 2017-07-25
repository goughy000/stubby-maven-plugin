package com.testingsyndicate.maven.plugin.stubby;

import io.github.azagniotov.stubby4j.client.StubbyClient;

import java.io.File;

class ServerManager {

    private final File configurationFile;
    private final Integer httpPort;
    private final Integer httpsPort;
    private final Integer adminPort;
    private final StubbyClient client;

    private boolean isStarted = false;

    private ServerManager(Builder builder) {
        this.configurationFile = builder.configurationFile;
        this.httpPort = builder.httpPort;
        this.httpsPort = builder.httpsPort;
        this.adminPort = builder.adminPort;
        this.client = null == builder.client ? new StubbyClient() : builder.client;
    }

    void start() {
        try {
            client.startJetty(httpPort, httpsPort, adminPort, "localhost", configurationFile.getPath());
            isStarted = true;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to start stubby", ex);
        }
    }

    void stop() {
        if (!isStarted) {
            throw new IllegalStateException("Cannot stop stubby when it has not been started");
        }
        try {
            client.stopJetty();
            isStarted = false;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to stop stubby", ex);
        }
    }

    public void join() {
        if (!isStarted) {
            start();
        }
        try {
            client.joinJetty();
            isStarted = false;
        } catch (Exception ex) {
            throw new RuntimeException("Could not join stubby", ex);
        }
    }

    File getConfigurationFile() {
        return configurationFile;
    }

    Integer getHttpPort() {
        return httpPort;
    }

    Integer getHttpsPort() {
        return httpsPort;
    }

    Integer getAdminPort() {
        return adminPort;
    }

    StubbyClient getClient() {
        return client;
    }

    static Builder newBuilder() {
        return new Builder();
    }

    static class Builder {
        private File configurationFile;
        private Integer httpPort;
        private Integer httpsPort;
        private Integer adminPort;
        private StubbyClient client;

        private Builder() { }

        Builder configurationFile(File configurationFile) {
            this.configurationFile = configurationFile;
            return this;
        }

        Builder httpPort(Integer httpPort) {
            this.httpPort = httpPort;
            return this;
        }

        Builder httpsPort(Integer httpsPort) {
            this.httpsPort = httpsPort;
            return this;
        }

        Builder adminPort(Integer adminPort) {
            this.adminPort = adminPort;
            return this;
        }

        Builder client(StubbyClient client) {
            this.client = client;
            return this;
        }

        ServerManager build() {
            return new ServerManager(this);
        }

    }

}
