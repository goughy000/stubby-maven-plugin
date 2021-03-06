package com.testingsyndicate.maven.plugin.stubby;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "start", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class StartStubbyMojo extends ConfigurationMojo {

  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().debug("Starting StartStubbyMojo::execute");
    ServerManager manager =
        ServerManager.newBuilder()
            .configurationFile(configFile)
            .httpPort(httpPort)
            .httpsPort(httpsPort)
            .adminPort(adminPort)
            .mute(mute)
            .debug(debug)
            .build();

    getPluginContext().put(MANAGER_KEY, manager);

    getLog().info(String.format("Starting Stubby on port %s", httpPort));
    manager.start();
  }
}
