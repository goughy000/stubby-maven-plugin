package com.testingsyndicate.maven.plugin.stubby;

import java.io.File;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

abstract class ConfigurationMojo extends AbstractMojo {

  static final String MANAGER_KEY = "manager";

  @Parameter(property = "configFile", defaultValue = "src/test/resources/stubs.yaml")
  protected File configFile;

  @Parameter(property = "httpPort", defaultValue = "8882")
  protected Integer httpPort;

  @Parameter(property = "httpsPort")
  protected Integer httpsPort;

  @Parameter(property = "adminPort")
  protected Integer adminPort;

  @Parameter(property = "mute", defaultValue = "true")
  protected Boolean mute;

  @Parameter(property = "debug", defaultValue = "false")
  protected Boolean debug;
}
