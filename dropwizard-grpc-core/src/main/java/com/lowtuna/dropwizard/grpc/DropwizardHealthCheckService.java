package com.lowtuna.dropwizard.grpc;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.common.base.Predicate;
import io.grpc.Server;
import io.grpc.ServerServiceDefinition;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthGrpc;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
class DropwizardHealthCheckService extends HealthGrpc.HealthImplBase {

    private final HealthCheckRegistry healthCheckRegistry;
    private final AtomicReference<Optional<Server>> server;

    @Override
    public void check(HealthCheckRequest request, StreamObserver<HealthCheckResponse> responseObserver) {
        String serviceName = request.getService();
        boolean matchedServiceName = StringUtils.isEmpty(serviceName);
        HealthCheckResponse.ServingStatus servingStatus = HealthCheckResponse.ServingStatus.UNKNOWN;
        if (!server.get().isPresent()) {
            servingStatus = HealthCheckResponse.ServingStatus.NOT_SERVING;
        } else if (!matchedServiceName) {
            matchedServiceName = server.get().get().getServices().parallelStream().filter((Predicate<ServerServiceDefinition>) input -> input.getServiceDescriptor().getName().equals(serviceName)).count() == 1;
        }

        if (matchedServiceName) {
            Map<String, HealthCheck.Result> healthCheckResults = healthCheckRegistry.runHealthChecks();
            boolean allPassed = healthCheckResults.values().parallelStream().filter(HealthCheck.Result::isHealthy).count() == healthCheckResults.values().size();
            servingStatus = allPassed ? HealthCheckResponse.ServingStatus.SERVING : HealthCheckResponse.ServingStatus.NOT_SERVING;
        }

        responseObserver.onNext(HealthCheckResponse.newBuilder().setStatus(servingStatus).build());
        responseObserver.onCompleted();
    }
}
