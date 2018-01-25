package com.lowtuna.dropwizard.grpc;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthGrpc;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RequiredArgsConstructor
class DropwizardHealthCheckService extends HealthGrpc.HealthImplBase {

    private final HealthCheckRegistry healthCheckRegistry;

    private final ExecutorService healthCheckExecutorService = Executors.newCachedThreadPool();

    @Override
    public void check(HealthCheckRequest request, StreamObserver<HealthCheckResponse> responseObserver) {
        String serviceName = request.getService();
        HealthCheckResponse.ServingStatus servingStatus;

        Map<String, HealthCheck.Result> healthCheckResults = healthCheckRegistry.runHealthChecks(healthCheckExecutorService);
        if (StringUtils.isEmpty(serviceName)) {
            boolean allPassed = healthCheckResults.values().parallelStream().filter(HealthCheck.Result::isHealthy).count() == healthCheckResults.values().size();
            servingStatus = allPassed ? HealthCheckResponse.ServingStatus.SERVING : HealthCheckResponse.ServingStatus.NOT_SERVING;
        } else {
            if (healthCheckResults.containsKey(serviceName)) {
                servingStatus = healthCheckResults.get(serviceName).isHealthy() ? HealthCheckResponse.ServingStatus.SERVING : HealthCheckResponse.ServingStatus.NOT_SERVING;
            } else {
                servingStatus = HealthCheckResponse.ServingStatus.UNKNOWN;
            }
        }

        responseObserver.onNext(HealthCheckResponse.newBuilder().setStatus(servingStatus).build());
        responseObserver.onCompleted();
    }
}
