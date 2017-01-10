package com.lowtuna.dropwizard.grpc;

import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by tburch on 1/6/17.
 */
@Slf4j
@Data
class GrpcServer implements Managed {
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
    grpcEnvironment.getLifecycleEvents().parallelStream().forEach(throwableCatchingConsumer(
            GrpcServerLifecycleListener::preServerStart,
            (lifecycleListener, preStartException) -> log.warn("Caught exception while trying to call preServerStart on {}", lifecycleListener.getClass().getCanonicalName(), preStartException)
    ));

    boolean serverStarted = startServer(port, serverBuilder);

    if (serverStarted) {
      log.info("Started gRPC server listening on port {}", port);
      grpcEnvironment.getLifecycleEvents().parallelStream().forEach(throwableCatchingConsumer(
              GrpcServerLifecycleListener::postServerStart,
              (lifecycleListener, postStartException) -> log.warn("Caught exception while trying to call postServerStart on {}", lifecycleListener.getClass().getCanonicalName(), postStartException)
      ));
    } else {
      log.warn("Unable to start gRPC on port {}", port);
      throw new IllegalStateException("Unable to start gRPC server on port " + port);
    }
  }

  @Override
  public void stop() throws Exception {
    if (server.get().isPresent()) {
      grpcEnvironment.getLifecycleEvents().parallelStream().forEach(throwableCatchingConsumer(
              GrpcServerLifecycleListener::preServerStop,
              (lifecycleListener, preStopException) -> log.warn("Caught exception while trying to call preServerStop on {}", lifecycleListener.getClass().getCanonicalName(), preStopException)
      ));

      stopServer();

      grpcEnvironment.getLifecycleEvents().parallelStream().forEach(throwableCatchingConsumer(
              GrpcServerLifecycleListener::postServerStop,
              (lifecycleListener, postStopException) -> log.warn("Caught exception while trying to call postServerStop on {}", lifecycleListener.getClass().getCanonicalName(), postStopException)
      ));
    }
  }

  private boolean startServer(int port, ServerBuilder<?> serverBuilder) {
    try {
      Server grpcServer = serverBuilder.build().start();
      if (server.compareAndSet(Optional.empty(), Optional.of(grpcServer))) {
        return true;
      } else {
        log.warn("gRPC server already running on port {}", this.server.get().get().getPort());
        grpcServer.shutdownNow();
      }
    } catch (Exception exception) {
      log.warn("IOException while trying to start gRPC server on port {}", port, exception);
    }
    return false;
  }

  private boolean stopServer() {
    int port = connectorConfiguration.getPort();
    Server gRpcServer = server.get().get();
    try {
      log.info("Stopping gRPC server on port {}", port);
      gRpcServer.shutdown();
      log.info("Stopped gRPC server on port {}", port);
      return true;
    } catch (Exception shutDownException) {
      log.warn("Caught Exception when trying to stop gRPC server on port {}", shutDownException, port);
    }
    return false;
  }

  private static Consumer<GrpcServerLifecycleListener> throwableCatchingConsumer(Consumer<GrpcServerLifecycleListener> worker, BiConsumer<GrpcServerLifecycleListener, Throwable> throwableWorker) {
    return work -> {
      try {
        worker.accept(work);
      } catch (Throwable t) {
        throwableWorker.accept(work, t);
      }
    };
  }
}
