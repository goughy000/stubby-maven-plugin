package com.testingsyndicate.maven.plugin.stubby;

import io.github.azagniotov.stubby4j.cli.CommandLineInterpreter;
import io.github.azagniotov.stubby4j.server.StubbyManager;
import io.github.azagniotov.stubby4j.server.StubbyManagerFactory;
import io.github.azagniotov.stubby4j.stubs.StubHttpLifecycle;
import io.github.azagniotov.stubby4j.yaml.YAMLParser;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class ServerManager {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(1);

    private final File configurationFile;
    private final Integer httpPort;
    private final Integer httpsPort;
    private final Integer adminPort;
    private final Boolean mute;
    private final StubbyManagerFactory factory;
    private StubbyManager manager;

    private boolean isStarted = false;

    private ServerManager(Builder builder) {
        this.configurationFile = builder.configurationFile;
        this.httpPort = builder.httpPort;
        this.httpsPort = builder.httpsPort;
        this.adminPort = builder.adminPort;
        this.mute = builder.mute;
        this.factory = null == builder.factory ? new StubbyManagerFactory() : builder.factory;
    }

    void start() {
        try {
            Future<List<StubHttpLifecycle>> future = EXECUTOR_SERVICE.submit(new Callable<List<StubHttpLifecycle>>() {
                public List<StubHttpLifecycle> call() throws Exception {
                    return (new YAMLParser()).parse(configurationFile.getParent(), configurationFile);
                }
            });

            manager = factory.construct(configurationFile, constructArguments(), future);
            manager.startJetty();
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
            manager.stopJetty();
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
            manager.joinJetty();
            isStarted = false;
        } catch (Exception ex) {
            throw new RuntimeException("Could not join stubby", ex);
        }
    }

    private Map<String, String> constructArguments() {
        Map<String, String> args = new HashMap<String, String>();
        args.put(CommandLineInterpreter.OPTION_ADDRESS, "localhost");
        args.put(CommandLineInterpreter.OPTION_CONFIG, configurationFile.getPath());
        args.put(CommandLineInterpreter.OPTION_CLIENTPORT, httpPort.toString());

        if (null != httpsPort) {
            args.put(CommandLineInterpreter.OPTION_TLSPORT, httpsPort.toString());
        } else {
            args.put(CommandLineInterpreter.OPTION_DISABLE_SSL, "");
        }

        if (null != adminPort) {
            args.put(CommandLineInterpreter.OPTION_ADMINPORT, adminPort.toString());
        } else {
            args.put(CommandLineInterpreter.OPTION_DISABLE_ADMIN, "");
        }

        if (null != mute && mute) {
            args.put(CommandLineInterpreter.OPTION_MUTE, "");
        }

        return args;
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

    Boolean getMute() {
        return mute;
    }

    StubbyManagerFactory getFactory() {
        return factory;
    }

    static Builder newBuilder() {
        return new Builder();
    }

    static class Builder {
        private File configurationFile;
        private Integer httpPort;
        private Integer httpsPort;
        private Integer adminPort;
        private Boolean mute;
        private StubbyManagerFactory factory;

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

        Builder mute(Boolean mute) {
            this.mute = mute;
            return this;
        }

        Builder factory(StubbyManagerFactory factory) {
            this.factory = factory;
            return this;
        }

        ServerManager build() {
            return new ServerManager(this);
        }

    }

}
