/**
 * Copyright 2017 Tristan Burch.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lowtuna.dropwizard.grpc;

import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Slf4j
@Data
class GrpcServer implements Managed {
    private final AtomicReference<Optional<Server>> server = new AtomicReference<>(Optional.empty());

    private final GrpcEnvironment grpcEnvironment;
    private final Environment dropwizardEnvironment;
    private final GrpcConnectorConfiguration connectorConfiguration;

    @Override
    public void start() throws Exception {
        int port = connectorConfiguration.getPort();

        ServerBuilder<?> serverBuilder = ServerBuilder.forPort(port);
        serverBuilder.executor(grpcEnvironment.getExecutorService());

        log.debug("Adding {} ServerServiceDefinitions...", grpcEnvironment.getServerServiceDefinitions().size());
        grpcEnvironment.getServerServiceDefinitions().forEach(serverBuilder::addService);
        log.debug("Adding {} BindableServices...", grpcEnvironment.getBindableServices().size());
        grpcEnvironment.getBindableServices().forEach(serverBuilder::addService);

        log.debug("Adding {} Interceptors...", grpcEnvironment.getInterceptors().size());
        grpcEnvironment.getInterceptors().forEach(serverBuilder::intercept);

        log.debug("Adding {} TransportFilters...", grpcEnvironment.getTransportFilters().size());
        grpcEnvironment.getTransportFilters().forEach(serverBuilder::addTransportFilter);

        log.debug("Adding {} StreamTracers...", grpcEnvironment.getStreamTracers().size());
        grpcEnvironment.getStreamTracers().forEach(serverBuilder::addStreamTracerFactory);

        if (Objects.nonNull(grpcEnvironment.getFallbackRegistry())) {
            log.debug("Setting FallbackRegistry");
            serverBuilder.fallbackHandlerRegistry(grpcEnvironment.getFallbackRegistry());
        }

        if (Objects.nonNull(grpcEnvironment.getFallbackRegistry())) {
            log.debug("Setting FallbackRegistry");
            serverBuilder.fallbackHandlerRegistry(grpcEnvironment.getFallbackRegistry());
        }

        if (Objects.nonNull(grpcEnvironment.getDecompressorRegistry())) {
            log.debug("Setting DecompressorRegistry");
            serverBuilder.decompressorRegistry(grpcEnvironment.getDecompressorRegistry());
        }

        if (Objects.nonNull(grpcEnvironment.getCompressorRegistry())) {
            log.debug("Setting CompressorRegistry");
            serverBuilder.compressorRegistry(grpcEnvironment.getCompressorRegistry());
        }

        if (Objects.nonNull(grpcEnvironment.getTlsCertAndPrivateKey())) {
            log.debug("Enabling TLS");
            serverBuilder.useTransportSecurity(grpcEnvironment.getTlsCertAndPrivateKey().getLeft(), grpcEnvironment.getTlsCertAndPrivateKey().getRight());
        }

        DropwizardHealthCheckService healthCheckService = new DropwizardHealthCheckService(dropwizardEnvironment.healthChecks(), server);
        serverBuilder.addService(healthCheckService);

        log.info("Starting gRPC server listening on port {}", port);
        grpcEnvironment.getLifecycleEvents().parallelStream().forEach(throwableCatchingConsumer(
                GrpcServerLifecycleListener::preServerStart,
                (lifecycleListener, preStartException) -> log.warn("Caught exception while trying to call preServerStart on {}", lifecycleListener.getClass().getCanonicalName(), preStartException)
        ));

        boolean serverStarted = startServer(serverBuilder);

        if (serverStarted) {
            StringBuilder sb = new StringBuilder(GrpcEnvironment.class.getCanonicalName() + ": gRPC services =");
            sb.append(System.lineSeparator());
            sb.append(System.lineSeparator());
            server.get().get().getServices().forEach(ssd -> {
                sb.append(String.format("    %-7s%n", ssd.getServiceDescriptor().getName()));
            });
            log.info(sb.toString());
            log.info("Started gRPC server listening on port {}", port);
            grpcEnvironment.getLifecycleEvents().parallelStream().forEach(throwableCatchingConsumer(
                    GrpcServerLifecycleListener::postServerStart,
                    (lifecycleListener, postStartException) -> log.warn("Caught exception while trying to call postServerStart on {}", lifecycleListener.getClass().getCanonicalName(), postStartException)
            ));
        } else {
            log.warn("Unable to start gRPC server on port {}", port);
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

    private boolean startServer(ServerBuilder<?> serverBuilder) {
        try {
            Server grpcServer = serverBuilder.build().start();
            if (server.compareAndSet(Optional.empty(), Optional.of(grpcServer))) {
                return true;
            } else {
                log.warn("gRPC server already running on port {}", this.server.get().get().getPort());
                grpcServer.shutdownNow();
            }
        } catch (IOException exception) {
            log.warn("IOException while trying to start gRPC server", exception);
        }
        return false;
    }

    private boolean stopServer() {
        if (server.get().isPresent()) {
            Server gRpcServer = server.get().get();
            try {
                log.info("Stopping gRPC server on port {}", gRpcServer.getPort());
                gRpcServer.shutdown();
                log.info("Stopped gRPC server on port {}", gRpcServer.getPort());
                return true;
            } catch (Exception shutDownException) {
                log.warn("Caught Exception when trying to stop gRPC server on port {}", shutDownException, gRpcServer.getPort());
            }
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
