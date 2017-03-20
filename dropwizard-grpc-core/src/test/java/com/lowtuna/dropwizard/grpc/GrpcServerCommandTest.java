package com.lowtuna.dropwizard.grpc;


import io.dropwizard.cli.Command;
import io.dropwizard.server.ServerFactory;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.function.Function;

/**
 * Created by tburch on 3/20/17.
 */
public class GrpcServerCommandTest {
  private static final int GRPC_PORT = 9000;
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
    CONFIG = configuration;
  }

  @ClassRule
  public static final DropwizardAppRule<TestGrpcApplicationConfiguration> APP_RULE = new DropwizardAppRule(
          TestGrpcApplication.class,
          CONFIG,
          new Function<TestGrpcApplication, Command>() {
            @Override
            public Command apply(TestGrpcApplication application) {
              return new GrpcServerCommand<>(application);
            }
          }
  );

  private FooServiceGrpc.FooServiceBlockingStub fooService;

  @Before
  public void setupFoodService() {
    ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", GRPC_PORT)
            .usePlaintext(true)
            .build();
    this.fooService = FooServiceGrpc.newBlockingStub(channel);
  }

  @Test
  public void bar() {
    FooRequest request = FooRequest.newBuilder().setBar("123").build();
    FooResponse response = fooService.doFoo(request);
    Assert.assertTrue(response.getBaz().contains("123"));
  }
}