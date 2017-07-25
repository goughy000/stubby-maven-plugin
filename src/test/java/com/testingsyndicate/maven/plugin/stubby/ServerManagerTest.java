package com.testingsyndicate.maven.plugin.stubby;

import io.github.azagniotov.stubby4j.client.StubbyClient;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ServerManagerTest {

    private ServerManager sut;
    private File mockConfiguration;
    private StubbyClient mockClient;

    @Before
    public void setup() {
        mockConfiguration = mock(File.class);
        mockClient = mock(StubbyClient.class);

        when(mockConfiguration.getPath()).thenReturn("/filepath");

        sut = ServerManager.newBuilder()
                .adminPort(8888)
                .httpPort(8887)
                .httpsPort(8886)
                .configurationFile(mockConfiguration)
                .client(mockClient)
                .build();
    }

    @Test
    public void builderConstructsInstance() {
        // given
        File file = new File("/dev/null");
        ServerManager.Builder builder = ServerManager.newBuilder()
                .configurationFile(file)
                .httpPort(8080)
                .httpsPort(8443)
                .adminPort(8081);

        // when
        ServerManager actual = builder.build();

        // then
        assertThat(actual.getConfigurationFile()).isEqualTo(file);
        assertThat(actual.getHttpPort()).isEqualTo(8080);
        assertThat(actual.getHttpsPort()).isEqualTo(8443);
        assertThat(actual.getAdminPort()).isEqualTo(8081);
        assertThat(actual.getClient()).isNotNull();
    }

    @Test
    public void startsStubbyWhenStart() throws Exception {
        // given

        // when
        sut.start();

        // then
        verify(mockClient).startJetty(8887, 8886, 8888, "localhost", "/filepath");
    }

    @Test
    public void throwsRuntimeWhenCannotStart() throws Exception {
        // given
        Exception error = new Exception("Hello");
        doThrow(error).when(mockClient).startJetty(anyInt(), anyInt(), anyInt(), anyString(), anyString());

        // when
        try {
            sut.start();
            fail("Expected RuntimeException");
        } catch (RuntimeException actual) {
            // then
            assertThat(actual)
                    .hasMessage("Failed to start stubby")
                    .hasCause(error);
        }
    }

    @Test
    public void stopsStubbyWhenStop() throws Exception {
        // given
        sut.start();

        // when
        sut.stop();

        // then
        verify(mockClient).stopJetty();
    }

    @Test
    public void throwsRuntimeWhenCannotStop() throws Exception {
        // given
        Exception error = new Exception("Boom!");
        doThrow(error).when(mockClient).stopJetty();
        sut.start();

        // when
        try {
            sut.stop();
            fail("Expected RuntimeException");
        } catch (RuntimeException actual) {
            // then
            assertThat(actual)
                    .hasMessage("Failed to stop stubby")
                    .hasCause(error);
        }
    }

    @Test
    public void throwsIllegalStateIfNotStarted() throws Exception {
        // given
        // not started

        // when
        try {
            sut.stop();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException actual) {
            // then
            assertThat(actual)
                    .hasMessage("Cannot stop stubby when it has not been started");
        }
    }

    @Test
    public void startsAndJoinsJettyIfNotStarted() throws Exception {
        // given

        // when
        sut.join();

        // then
        verify(mockClient).startJetty(anyInt(), anyInt(), anyInt(), anyString(), anyString());
        verify(mockClient).joinJetty();
    }

    @Test
    public void joinsIfAlreadyStarted() throws Exception {
        // given
        sut.start();

        // when
        sut.join();

        // then
        verify(mockClient, times(1)).startJetty(anyInt(), anyInt(), anyInt(), anyString(), anyString());
        verify(mockClient).joinJetty();
    }

    @Test
    public void throwsRuntimeWhenCannotJoin() throws Exception {
        // given
        Exception error = new Exception("broken");
        doThrow(error).when(mockClient).joinJetty();

        // when
        try {
            sut.join();
            fail("Expected RuntimeException");
        } catch (RuntimeException actual) {
            assertThat(actual)
                    .hasMessage("Could not join stubby")
                    .hasCause(error);
        }

    }
}
