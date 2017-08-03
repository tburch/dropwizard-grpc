package com.lowtuna.dropwizard.grpc;

import com.codahale.metrics.ConsoleReporter;
import com.lowtuna.dropwizard.grpc.metrics.MetricsInterceptor;
import io.dropwizard.setup.Environment;

import java.util.concurrent.TimeUnit;

/**
 * Created by tburch on 3/20/17.
 */
public class TestGrpcApplication extends GrpcApplication<TestGrpcApplicationConfiguration> {
  @Override
  public void run(TestGrpcApplicationConfiguration configuration, Environment environment, GrpcEnvironment.GrpcEnvironmentBuilder grpcEnvironmentBuilder) throws Exception {
    grpcEnvironmentBuilder.bindableService(new FooServiceImpl());
    grpcEnvironmentBuilder.interceptor(new MetricsInterceptor(environment.metrics()));

    ConsoleReporter reporter = ConsoleReporter
            .forRegistry(environment.metrics())
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .build();
    reporter.start(1, TimeUnit.SECONDS);
  }
}
