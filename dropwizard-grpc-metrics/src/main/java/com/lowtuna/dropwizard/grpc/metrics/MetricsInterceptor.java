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
package com.lowtuna.dropwizard.grpc.metrics;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;

public class MetricsInterceptor implements ServerInterceptor {
    private final MetricsContainer metricsContainer;

    public MetricsInterceptor(MetricRegistry metrics) {
        this.metricsContainer = MetricsContainer.builder()
                .activeCallsCounter(metrics.counter(MetricRegistry.name(MetricsInterceptor.class, "active-calls")))
                .responseTimer(metrics.timer(MetricRegistry.name(MetricsInterceptor.class, "calls")))
                .streamMessagesReceivedMeter(metrics.meter(MetricRegistry.name(MetricsInterceptor.class, "stream-messages-received")))
                .streamMessagesSentMeter(metrics.meter(MetricRegistry.name(MetricsInterceptor.class, "stream-messages-sent")))
                .statusMeters(CacheBuilder.newBuilder().build(new CacheLoader<Status, Meter>() {
                    @Override
                    public Meter load(Status key) throws Exception {
                        return metrics.meter(MetricRegistry.name(MetricsInterceptor.class, "statuses", key.getCode().toString()));
                    }
                }))
                .build();
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        MethodDescriptor.MethodType methodType = call.getMethodDescriptor().getType();
        ServerCall<ReqT, RespT> instrumentedServerCall = new InstrumentedServerCall<>(call, methodType, metricsContainer);
        return new InstrumentedServerCallListener<>(next.startCall(instrumentedServerCall, headers), methodType, metricsContainer);
    }
}
