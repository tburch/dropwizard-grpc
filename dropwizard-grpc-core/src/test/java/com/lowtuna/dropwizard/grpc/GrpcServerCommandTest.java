package com.lowtuna.dropwizard.grpc;

import ch.qos.logback.classic.spi.ILoggingEvent;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.cli.Command;
import io.dropwizard.logging.ConsoleAppenderFactory;
import io.dropwizard.logging.DefaultLoggingFactory;
import io.dropwizard.logging.LoggingFactory;
import io.dropwizard.server.ServerFactory;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthGrpc;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.Collections;
import java.util.function.Function;

/**
 * Created by tburch on 3/20/17.
 */
public class GrpcServerCommandTest {
  private static final int GRPC_PORT = 9004;
  private static final ServerFactory SERVER_FACTORY;
  static {
    GrpcServerFactory grpcServerFactory = new GrpcServerFactory();
    GrpcConnectorConfiguration connectorConfiguration = new GrpcConnectorConfiguration();
    connectorConfiguration.setPort(GRPC_PORT);
    grpcServerFactory.setApplicationConnector(connectorConfiguration);
    SERVER_FACTORY =  grpcServerFactory;
  }

  private static final TestGrpcApplicationConfiguration CONFIG;
  static {
    TestGrpcApplicationConfiguration configuration = new TestGrpcApplicationConfiguration();
    configuration.setServerFactory(SERVER_FACTORY);

    DefaultLoggingFactory loggingFactory = new DefaultLoggingFactory();
    ConsoleAppenderFactory<ILoggingEvent> consoleAppenderFactory = new ConsoleAppenderFactory<>();
    consoleAppenderFactory.setLogFormat("%-5p [%d{ISO8601}] [%thread] %c{5}: %m%n%xEx");
    loggingFactory.setAppenders(Collections.singletonList(consoleAppenderFactory));
    configuration.setLoggingFactory(loggingFactory);

    CONFIG = configuration;
  }

  @ClassRule
  public static final DropwizardAppRule<TestGrpcApplicationConfiguration> APP_RULE = new DropwizardAppRule<>(
          TestGrpcApplication.class,
          CONFIG,
          testGrpcApplicationConfigurationApplication -> {
            TestGrpcApplication application = (TestGrpcApplication) testGrpcApplicationConfigurationApplication;
            return new GrpcServerCommand<>(application);
          }
  );

  private FooServiceGrpc.FooServiceBlockingStub fooService;
  private HealthGrpc.HealthBlockingStub healthService;

  @Before
  public void setupServices() {
    ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", GRPC_PORT)
            .usePlaintext(true)
            .build();
    this.fooService = FooServiceGrpc.newBlockingStub(channel);
    this.healthService = HealthGrpc.newBlockingStub(channel);
  }

  @Test
  public void testFooService() {
    FooRequest request = FooRequest.newBuilder().setBar("123").build();
    FooResponse response = fooService.doFoo(request);
    Assert.assertTrue(response.getBaz().contains("123"));
  }

  @Test
  public void testHealthCheckWithNoService() {
    TestGrpcApplication app = APP_RULE.getApplication();
    app.getFailHealthCheck().set(false);

    HealthCheckRequest request = HealthCheckRequest.newBuilder().setService(StringUtils.EMPTY).build();
    HealthCheckResponse response = healthService.check(request);
    Assert.assertEquals(HealthCheckResponse.ServingStatus.SERVING, response.getStatus());
  }

  @Test
  public void testHealthCheckWithValidService() {
    TestGrpcApplication app = APP_RULE.getApplication();
    app.getFailHealthCheck().set(false);

    HealthCheckRequest request = HealthCheckRequest.newBuilder().setService(FooServiceGrpc.SERVICE_NAME).build();
    HealthCheckResponse response = healthService.check(request);
    Assert.assertEquals(HealthCheckResponse.ServingStatus.SERVING, response.getStatus());
  }

  @Test
  public void testHealthCheckWithInvalidService() {
    TestGrpcApplication app = APP_RULE.getApplication();
    app.getFailHealthCheck().set(false);

    HealthCheckRequest request = HealthCheckRequest.newBuilder().setService("xyz." + FooServiceGrpc.SERVICE_NAME).build();
    HealthCheckResponse response = healthService.check(request);
    Assert.assertEquals(HealthCheckResponse.ServingStatus.UNKNOWN, response.getStatus());
  }

  @Test
  public void testHealthCheckWithNoServiceFailingHealthCheck() {
    TestGrpcApplication app = APP_RULE.getApplication();
    app.getFailHealthCheck().set(true);

    HealthCheckRequest request = HealthCheckRequest.newBuilder().setService(StringUtils.EMPTY).build();
    HealthCheckResponse response = healthService.check(request);
    Assert.assertEquals(HealthCheckResponse.ServingStatus.NOT_SERVING, response.getStatus());
  }
}