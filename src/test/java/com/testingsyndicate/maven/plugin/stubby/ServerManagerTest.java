package com.testingsyndicate.maven.plugin.stubby;

import io.github.azagniotov.stubby4j.server.StubbyManager;
import io.github.azagniotov.stubby4j.server.StubbyManagerFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.AssertionsForClassTypes.fail;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ServerManagerTest {

    private ServerManager sut;

    @Mock
    public File mockConfiguration;

    @Mock
    public StubbyManagerFactory mockFactory;

    @Mock
    public StubbyManager mockManager;

    @Captor
    public ArgumentCaptor<Map<String, String>> commandLineCaptor;

    @Before
    public void setup() throws Exception {
        when(mockConfiguration.getPath()).thenReturn("/filepath");
        when(mockFactory.construct(any(File.class), ArgumentMatchers.<String, String>anyMap(), any(Future.class)))
        .thenReturn(mockManager);

        sut = ServerManager.newBuilder()
                .adminPort(8888)
                .httpPort(8887)
                .httpsPort(8886)
                .configurationFile(mockConfiguration)
                .mute(true)
                .debug(true)
                .factory(mockFactory)
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
                .adminPort(8081)
                .mute(true)
                .debug(true);

        // when
        ServerManager actual = builder.build();

        // then
        assertThat(actual.getConfigurationFile()).isEqualTo(file);
        assertThat(actual.getHttpPort()).isEqualTo(8080);
        assertThat(actual.getHttpsPort()).isEqualTo(8443);
        assertThat(actual.getAdminPort()).isEqualTo(8081);
        assertThat(actual.getMute()).isEqualTo(true);
        assertThat(actual.getDebug()).isEqualTo(true);
        assertThat(actual.getFactory()).isNotNull();
    }

    @Test
    public void startsStubbyWhenStart() throws Exception {
        // given

        // when
        sut.start();

        // then
        verify(mockFactory).construct(eq(mockConfiguration), commandLineCaptor.capture(), any(Future.class));
        assertThat(commandLineCaptor.getValue())
                .isNotEmpty()
                .contains(entry("data", "/filepath"))
                .contains(entry("location", "localhost"))
                .contains(entry("stubs", "8887"))
                .contains(entry("tls", "8886"))
                .contains(entry("admin", "8888"))
                .contains(entry("mute", ""))
                .contains(entry("debug", ""));

        verify(mockManager).startJetty();
    }

    @Test
    public void disablesPortalsWhenNotConfigured() throws Exception {
        // given
        sut = ServerManager.newBuilder()
                .configurationFile(mockConfiguration)
                .factory(mockFactory)
                .httpPort(7000)
                .build();

        // when
        sut.start();

        // then
        verify(mockFactory).construct(eq(mockConfiguration), commandLineCaptor.capture(), any(Future.class));
        assertThat(commandLineCaptor.getValue())
                .isNotEmpty()
                .contains(entry("data", "/filepath"))
                .contains(entry("location", "localhost"))
                .contains(entry("stubs", "7000"))
                .contains(entry("disable_ssl", ""))
                .contains(entry("disable_admin_portal", ""));
    }

    @Test
    public void throwsRuntimeWhenCannotStart() throws Exception {
        // given
        Exception error = new Exception("Hello");
        doThrow(error).when(mockManager).startJetty();

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
        verify(mockManager).stopJetty();
    }

    @Test
    public void throwsRuntimeWhenCannotStop() throws Exception {
        // given
        Exception error = new Exception("Boom!");
        doThrow(error).when(mockManager).stopJetty();
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
        verify(mockManager).startJetty();
        verify(mockManager).joinJetty();
    }

    @Test
    public void joinsIfAlreadyStarted() throws Exception {
        // given
        sut.start();

        // when
        sut.join();

        // then
        verify(mockManager, times(1)).startJetty();
        verify(mockManager).joinJetty();
    }

    @Test
    public void throwsRuntimeWhenCannotJoin() throws Exception {
        // given
        Exception error = new Exception("broken");
        doThrow(error).when(mockManager).joinJetty();

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
