package com.testingsyndicate.maven.plugin.stubby;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "stop", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class StopStubbyMojo extends ConfigurationMojo {
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().debug("Starting StopStubbyMojo::execute");
        ServerManager manager = (ServerManager) getPluginContext().get(MANAGER_KEY);
        getLog().info("Stopping Stubby");
        manager.stop();
    }
}
