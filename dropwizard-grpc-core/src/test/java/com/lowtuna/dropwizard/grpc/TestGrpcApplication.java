package com.lowtuna.dropwizard.grpc;

import io.dropwizard.setup.Environment;

/**
 * Created by tburch on 3/20/17.
 */
public class TestGrpcApplication extends GrpcApplication<TestGrpcApplicationConfiguration> {
  @Override
  public void run(TestGrpcApplicationConfiguration configuration, Environment environment, GrpcEnvironment.GrpcEnvironmentBuilder grpcEnvironmentBuilder) throws Exception {
    grpcEnvironmentBuilder.bindableService(new FooServiceImpl());
  }
}
