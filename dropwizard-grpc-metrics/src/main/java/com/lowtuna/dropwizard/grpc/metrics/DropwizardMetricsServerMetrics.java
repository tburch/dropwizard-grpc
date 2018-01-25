/**
 * Copyright 2018 Tristan Burch.
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

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.grpc.MethodDescriptor;
import io.grpc.Status;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class DropwizardMetricsServerMetrics implements ServerMetrics {

    private final Counter activeCallsCounter;
    private final Timer responseTimer;
    private final Meter streamMessagesSentMeter;
    private final Meter streamMessagesReceivedMeter;
    private final LoadingCache<Status, Meter> statusMeters;

    public DropwizardMetricsServerMetrics(MetricRegistry metrics) {
        this.activeCallsCounter = metrics.counter(MetricRegistry.name(ServerMetricsInterceptor.class, "active-calls"));
        this.responseTimer = metrics.timer(MetricRegistry.name(ServerMetricsInterceptor.class, "calls"));
        this.streamMessagesReceivedMeter = metrics.meter(MetricRegistry.name(ServerMetricsInterceptor.class, "stream-messages-received"));
        this.streamMessagesSentMeter = metrics.meter(MetricRegistry.name(ServerMetricsInterceptor.class, "stream-messages-sent"));
        this.statusMeters = CacheBuilder.newBuilder().build(new CacheLoader<Status, Meter>() {
            @Override
            public Meter load(Status key) throws Exception {
                return metrics.meter(MetricRegistry.name(ServerMetricsInterceptor.class, "statuses", key.getCode().toString()));
            }
        });
    }

    @Override
    public void markMessageReceived(MethodDescriptor methodDescriptor) {
        streamMessagesReceivedMeter.mark();
    }

    @Override
    public void markMessageComplete(Duration elapsed, MethodDescriptor methodDescriptor) {
        responseTimer.update(elapsed.toNanos(), TimeUnit.NANOSECONDS);
    }

    @Override
    public void markMessageCanceled(Duration elapsed, MethodDescriptor methodDescriptor) {
        responseTimer.update(elapsed.toNanos(), TimeUnit.NANOSECONDS);
    }

    @Override
    public void callClosed(Status status, MethodDescriptor methodDescriptor) {
        statusMeters.getUnchecked(status).mark();
    }

    @Override
    public void messageSent(MethodDescriptor methodDescriptor) {
        streamMessagesSentMeter.mark();
    }

    @Override
    public void incActiveRequests() {
        activeCallsCounter.inc();
    }

    @Override
    public void decActiveRequests() {
        activeCallsCounter.dec();
    }
}
