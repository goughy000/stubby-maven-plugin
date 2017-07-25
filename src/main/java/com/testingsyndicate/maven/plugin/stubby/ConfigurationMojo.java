package com.testingsyndicate.maven.plugin.stubby;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

abstract class ConfigurationMojo extends AbstractMojo {

    static final String MANAGER_KEY = "manager";

    @Parameter(property = "configFile", defaultValue = "target/stubs.yaml")
    private File configFile;

    @Parameter(property = "httpPort", defaultValue = "8882")
    private Integer httpPort;

    @Parameter(property = "httpsPort", defaultValue = "7443")
    private Integer httpsPort;

    @Parameter(property = "adminPort", defaultValue = "8889")
    private Integer adminPort;

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

}
