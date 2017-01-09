package com.lowtuna.dropwizard.grpc;

import io.dropwizard.Configuration;
import io.dropwizard.cli.EnvironmentCommand;
import io.dropwizard.setup.Environment;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.argparse4j.inf.Namespace;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;

/**
 * Created by tburch on 1/6/17.
 */
@Slf4j
public class GrpcServerCommand<T extends Configuration> extends EnvironmentCommand<T> {
  private final GrpcApplication<T> application;

  @Getter
  private final Class<T> configurationClass;

  public GrpcServerCommand(GrpcApplication<T> application) {
    super(application, "gRPC", "Runs the Dropwizard application as a gRPC server");
    this.application = application;
    this.configurationClass = application.getConfigurationClass();
  }

  @Override
  protected void run(Environment environment, Namespace namespace, T configuration) throws Exception {
    GrpcEnvironment.GrpcEnvironmentBuilder grpcEnvironmentBuilder = GrpcEnvironment.builder();

    application.run(configuration, environment, grpcEnvironmentBuilder);

    GrpcEnvironment grpcEnvironment = grpcEnvironmentBuilder.build();
    if (configuration.getServerFactory() instanceof GrpcServerFactory) {
      GrpcServerFactory grpcServerFactory = (GrpcServerFactory) configuration.getServerFactory();

      GrpcServer grpcServer = new GrpcServer(grpcEnvironment, environment, grpcServerFactory.getApplicationConnector());
      environment.lifecycle().manage(grpcServer);

      Server server = grpcServerFactory.build(environment);
      server.addLifeCycleListener(new LifeCycleListener());
      cleanupAsynchronously();
      try {
        server.start();
      } catch (Exception startException) {
        log.error("Unable to start gRPC server, shutting down", startException);
        try {
          server.stop();
        } catch (Exception stopException) {
          log.warn("Failure while stopping gRPC server", stopException);
        }
        try {
          cleanup();
        } catch (Exception cleanupException) {
          log.warn("Failure during cleanup", cleanupException);
        }
        throw startException;
      }
    } else {
      log.error("ServerFactory is not an instance of GrpcServerFactory, thus cannot start gRPC server");
      throw new IllegalArgumentException("ServerFactory is not an instance of GrpcServerFactory");
    }
  }

  private class LifeCycleListener extends AbstractLifeCycle.AbstractLifeCycleListener {
    @Override
    public void lifeCycleStopped(LifeCycle event) {
      cleanup();
    }
  }
}
