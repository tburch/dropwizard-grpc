package com.lowtuna.dropwizard.grpc;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.cli.CheckCommand;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * Created by tburch on 1/6/17.
 */
public abstract class GrpcApplication<T extends Configuration> extends Application<T> {
  protected abstract void run(T configuration, Environment environment, GrpcEnvironment.GrpcEnvironmentBuilder grpcEnvironmentBuilder);

  @Override
  protected void addDefaultCommands(Bootstrap<T> bootstrap) {
    bootstrap.addCommand(new GrpcServerCommand<>(this));
    bootstrap.addCommand(new CheckCommand<>(this));
  }

  @Override
  public void run(T configuration, Environment environment) throws Exception {
  }
}
