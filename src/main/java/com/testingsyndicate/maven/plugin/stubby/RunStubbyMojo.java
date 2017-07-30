package com.testingsyndicate.maven.plugin.stubby;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "run")
public class RunStubbyMojo extends ConfigurationMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().debug("Starting RunStubbyMojo::execute");
        getLog().info(String.format("Starting Stubby on port %s", getHttpPort()));
        ServerManager.newBuilder()
                .configurationFile(getConfigFile())
                .httpPort(getHttpPort())
                .httpsPort(getHttpsPort())
                .adminPort(getAdminPort())
                .mute(getMute())
                .build()
                .join();
        getLog().info("Stopping Stubby");
    }
}
