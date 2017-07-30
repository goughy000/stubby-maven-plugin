package com.testingsyndicate.maven.plugin.stubby;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

abstract class ConfigurationMojo extends AbstractMojo {

    static final String MANAGER_KEY = "manager";

    @Parameter(property = "configFile", defaultValue = "src/test/resources/stubs.yaml")
    private File configFile;

    @Parameter(property = "httpPort", defaultValue = "8882")
    private Integer httpPort;

    @Parameter(property = "httpsPort")
    private Integer httpsPort;

    @Parameter(property = "adminPort")
    private Integer adminPort;

    @Parameter(property = "mute", defaultValue = "true")
    private Boolean mute;

    File getConfigFile() {
        return configFile;
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

}
