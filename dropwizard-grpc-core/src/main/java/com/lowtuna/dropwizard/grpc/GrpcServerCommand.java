/**
 * Copyright 2017 Tristan Burch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

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
    int maxThreads = 64;
    ExecutorService executorService = environment.lifecycle()
            .executorService("gRPC-executor-service")
            .maxThreads(maxThreads)
            .workQueue(new LinkedBlockingQueue<>(maxThreads))
            .build();
    GrpcEnvironment.GrpcEnvironmentBuilder grpcEnvironmentBuilder = GrpcEnvironment.builder().executorService(executorService);

    log.info("Running application {}", application.getClass().getName());
    application.run(configuration, environment, grpcEnvironmentBuilder);

    GrpcEnvironment grpcEnvironment = grpcEnvironmentBuilder.build();
    if (configuration.getServerFactory() instanceof GrpcServerFactory) {
      GrpcServerFactory grpcServerFactory = (GrpcServerFactory) configuration.getServerFactory();

      GrpcServer grpcServer = new GrpcServer(grpcEnvironment, grpcServerFactory.getApplicationConnector());
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
          log.warn("Failure while stopping gRPC server after unsuccessful start", stopException);
        }
        try {
          cleanup();
        } catch (Exception cleanupException) {
          log.warn("Failure during cleanup after unsuccessful start", cleanupException);
        }
        throw startException;
      }
    } else {
      log.error("ServerFactory is not an instance of GrpcServerFactory therefore cannot start gRPC server");
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
