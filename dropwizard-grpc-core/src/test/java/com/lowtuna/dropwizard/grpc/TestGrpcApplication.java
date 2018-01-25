package com.lowtuna.dropwizard.grpc;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.health.HealthCheck;
import com.lowtuna.dropwizard.grpc.metrics.ServerMetricsInterceptor;
import com.lowtuna.dropwizard.grpc.metrics.ServerMetricsInterceptorFactory;
import io.dropwizard.setup.Environment;
import lombok.Getter;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by tburch on 3/20/17.
 */
public class TestGrpcApplication extends GrpcApplication<TestGrpcApplicationConfiguration> {
  @Getter
  private final AtomicBoolean failHealthCheck = new AtomicBoolean(false);
  @Override
  public void run(TestGrpcApplicationConfiguration configuration, Environment environment, GrpcEnvironment.GrpcEnvironmentBuilder grpcEnvironmentBuilder) throws Exception {
    FooServiceImpl fooService = new FooServiceImpl();
    grpcEnvironmentBuilder.bindableService(fooService);
    grpcEnvironmentBuilder.interceptor(ServerMetricsInterceptorFactory.usingDropwizardMetrics(environment.metrics()));

    HealthCheck flappableHealthCheck = new HealthCheck() {
      @Override
      protected Result check() throws Exception {
        return failHealthCheck.get() ? Result.unhealthy("fail") : Result.healthy();
      }
    };

    HealthCheck healthyHealthCheck = new HealthCheck() {
      @Override
      protected Result check() throws Exception {
        return Result.healthy();
      }
    };

    CompositeHealthCheck fooServiceHealthCheck = CompositeHealthCheck.builder()
                                                        .healthCheck(healthyHealthCheck)
                                                        .healthCheck(flappableHealthCheck)
                                                        .build();

    environment.healthChecks().register(FooServiceGrpc.SERVICE_NAME, fooServiceHealthCheck);
    environment.healthChecks().register("flappableHealthCheck", flappableHealthCheck);
    environment.healthChecks().register("healthyHealthCheck", healthyHealthCheck);

    ConsoleReporter reporter = ConsoleReporter
            .forRegistry(environment.metrics())
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .build();
    reporter.start(1, TimeUnit.SECONDS);
  }
}
