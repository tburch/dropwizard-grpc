package com.lowtuna.dropwizard.grpc;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.health.HealthCheck;
import com.lowtuna.dropwizard.grpc.metrics.MetricsInterceptor;
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
    grpcEnvironmentBuilder.bindableService(new FooServiceImpl());
    grpcEnvironmentBuilder.interceptor(new MetricsInterceptor(environment.metrics()));

    environment.healthChecks().register("test", new HealthCheck() {
      @Override
      protected Result check() throws Exception {
        return failHealthCheck.get() ? Result.unhealthy("fail") : Result.healthy();
      }
    });

    ConsoleReporter reporter = ConsoleReporter
            .forRegistry(environment.metrics())
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .build();
    reporter.start(1, TimeUnit.SECONDS);
  }
}
