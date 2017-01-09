package com.lowtuna.dropwizard.grpc;

import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by tburch on 1/6/17.
 */
@Slf4j
@Data
public class GrpcServer implements Managed {
  private final AtomicReference<Optional<Server>> server = new AtomicReference<>(Optional.empty());

  private final GrpcEnvironment grpcEnvironment;
  private final Environment environment;
  private final GrpcConnectorConfiguration connectorConfiguration;

  @Override
  public void start() throws Exception {
    int port = connectorConfiguration.getPort();

    ServerBuilder<?> serverBuilder = ServerBuilder.forPort(port);
    serverBuilder.executor(environment.lifecycle().executorService("gRPC").build());

    log.debug("Adding {} ServerServiceDefinitions...", grpcEnvironment.getServerServiceDefinitions().size());
    grpcEnvironment.getServerServiceDefinitions().forEach(serverBuilder::addService);
    log.debug("Adding {} BindableServices...", grpcEnvironment.getBindableServices().size());
    grpcEnvironment.getBindableServices().forEach(serverBuilder::addService);

    log.info("Starting gRPC server listening on port {}", port);

    for (GrpcServerLifecycleListener eventListener : grpcEnvironment.getLifecycleEvents()) {
      try {
        eventListener.preServerStart();
      } catch (Exception preStartException) {
        log.warn("Caught exception while trying to call preServerStart on {}", eventListener.getClass().getCanonicalName(), preStartException);
      }
    }

    Optional<Server> serverOptional = Optional.empty();
    try {
      Server server = serverBuilder.build().start();
      serverOptional = Optional.of(server);
      boolean set = this.server.compareAndSet(Optional.empty(), serverOptional);
      if (!set) {
        log.warn("gRPC server already running on port {}", this.server.get().get().getPort());
        try {
          server.shutdownNow();
        } finally {
          serverOptional = Optional.empty();
        }
      }
    } catch (IOException serverStartException) {
      log.warn("IOException while trying to start gRPC server on port {}", port, serverStartException);
    }

    if (serverOptional.isPresent()) {
      log.info("Started gRPC server listening on port {}", port);
      for (GrpcServerLifecycleListener eventListener : grpcEnvironment.getLifecycleEvents()) {
        try {
          eventListener.postServerStart();
        } catch (Exception postStartException) {
          log.warn("Caught exception while trying to call postServerStart on {}", eventListener.getClass().getCanonicalName(), postStartException);
        }
      }
    } else {
      log.warn("Unable to start gRPC on port {}", port);
      throw new IllegalStateException("Unable to start gRPC server on port " + port);
    }
  }

  @Override
  public void stop() throws Exception {
    int port = connectorConfiguration.getPort();

    if (server.get().isPresent()) {
      Server gRpcServer = server.get().get();
      for (GrpcServerLifecycleListener eventListener : grpcEnvironment.getLifecycleEvents()) {
        try {
          eventListener.preServerStop();
        } catch (Exception preStopException) {
          log.warn("Caught exception while trying to call preServerStop on {}", eventListener.getClass().getCanonicalName(), preStopException);
        }
      }

      try {
        log.info("Stopping gRPC server on port {}", port);
        gRpcServer.shutdown();
        log.info("Stopped gRPC server on port {}", port);
      } catch (Exception shutDownException) {
        log.warn("Caught Exception when trying to stop gRPC server on port {}", shutDownException, port);
      }

      for (GrpcServerLifecycleListener eventListener : grpcEnvironment.getLifecycleEvents()) {
        try {
          eventListener.postServerStop();
        } catch (Exception postStopException) {
          log.warn("Caught exception while trying to call postServerStop on {}", eventListener.getClass().getCanonicalName(), postStopException);
        }
      }
    }
  }
}
