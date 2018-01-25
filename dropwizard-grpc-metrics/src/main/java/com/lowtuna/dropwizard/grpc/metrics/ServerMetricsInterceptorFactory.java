package com.lowtuna.dropwizard.grpc.metrics;

import com.codahale.metrics.MetricRegistry;

public class ServerMetricsInterceptorFactory {
    public static ServerMetricsInterceptor usingDropwizardMetrics(MetricRegistry metricRegistry) {
        return new ServerMetricsInterceptor(new DropwizardMetricsServerMetrics(metricRegistry));
    }

    public ServerMetricsInterceptor usingLibratoMetrics() {
        return new ServerMetricsInterceptor(new LibratoMetricsServerMetrics());
    }
}
