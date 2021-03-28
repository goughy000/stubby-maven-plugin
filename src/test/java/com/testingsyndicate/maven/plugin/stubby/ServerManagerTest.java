package com.testingsyndicate.maven.plugin.stubby;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.azagniotov.stubby4j.server.StubbyManager;
import io.github.azagniotov.stubby4j.server.StubbyManagerFactory;
import java.io.File;
import java.util.Map;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

class ServerManagerTest {

  private File mockConfiguration;
  private StubbyManagerFactory mockFactory;
  private StubbyManager mockManager;
  private ArgumentCaptor<Map<String, String>> commandLineCaptor;

  private ServerManager sut;

  @BeforeEach
  void beforeEach() throws Exception {
    mockConfiguration = mock(File.class);
    mockFactory = mock(StubbyManagerFactory.class);
    mockManager = mock(StubbyManager.class);

    commandLineCaptor = ArgumentCaptor.forClass(Map.class);

    when(mockConfiguration.getPath()).thenReturn("/filepath");
    when(mockFactory.construct(
            any(File.class), ArgumentMatchers.<String, String>anyMap(), any(Future.class)))
        .thenReturn(mockManager);

    sut =
        ServerManager.newBuilder()
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
  void builderConstructsInstance() {
    // given
    File file = new File("/dev/null");
    ServerManager.Builder builder =
        ServerManager.newBuilder()
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
  void startsStubbyWhenStart() throws Exception {
    // given

    // when
    sut.start();

    // then
    verify(mockFactory)
        .construct(eq(mockConfiguration), commandLineCaptor.capture(), any(Future.class));
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
  void disablesPortalsWhenNotConfigured() throws Exception {
    // given
    sut =
        ServerManager.newBuilder()
            .configurationFile(mockConfiguration)
            .factory(mockFactory)
            .httpPort(7000)
            .build();

    // when
    sut.start();

    // then
    verify(mockFactory)
        .construct(eq(mockConfiguration), commandLineCaptor.capture(), any(Future.class));
    assertThat(commandLineCaptor.getValue())
        .isNotEmpty()
        .contains(entry("data", "/filepath"))
        .contains(entry("location", "localhost"))
        .contains(entry("stubs", "7000"))
        .contains(entry("disable_ssl", ""))
        .contains(entry("disable_admin_portal", ""));
  }

  @Test
  void throwsRuntimeWhenCannotStart() throws Exception {
    // given
    Exception cause = new Exception("Hello");
    doThrow(cause).when(mockManager).startJetty();

    // when
    Throwable actual = catchThrowable(() -> sut.start());

    // then
    assertThat(actual)
        .isExactlyInstanceOf(RuntimeException.class)
        .hasMessage("Failed to start stubby")
        .hasCauseReference(cause);
  }

  @Test
  void stopsStubbyWhenStop() throws Exception {
    // given
    sut.start();

    // when
    sut.stop();

    // then
    verify(mockManager).stopJetty();
  }

  @Test
  void throwsRuntimeWhenCannotStop() throws Exception {
    // given
    Exception cause = new Exception("Boom!");
    doThrow(cause).when(mockManager).stopJetty();
    sut.start();

    // when
    Throwable actual = catchThrowable(() -> sut.stop());

    // then
    assertThat(actual)
        .isExactlyInstanceOf(RuntimeException.class)
        .hasMessage("Failed to stop stubby")
        .hasCauseReference(cause);
  }

  @Test
  void throwsIllegalStateIfNotStarted() {
    // given
    // not started

    // when
    Throwable actual = catchThrowable(() -> sut.stop());

    // then
    assertThat(actual)
        .isExactlyInstanceOf(IllegalStateException.class)
        .hasMessage("Cannot stop stubby when it has not been started");
  }

  @Test
  void startsAndJoinsJettyIfNotStarted() throws Exception {
    // given

    // when
    sut.join();

    // then
    verify(mockManager).startJetty();
    verify(mockManager).joinJetty();
  }

  @Test
  void joinsIfAlreadyStarted() throws Exception {
    // given
    sut.start();

    // when
    sut.join();

    // then
    verify(mockManager, times(1)).startJetty();
    verify(mockManager).joinJetty();
  }

  @Test
  void throwsRuntimeWhenCannotJoin() throws Exception {
    // given
    Exception cause = new Exception("broken");
    doThrow(cause).when(mockManager).joinJetty();

    // when
    Throwable actual = catchThrowable(() -> sut.join());

    // then
    assertThat(actual)
        .isExactlyInstanceOf(RuntimeException.class)
        .hasMessage("Could not join stubby")
        .hasCauseReference(cause);
  }
}
