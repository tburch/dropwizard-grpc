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

import io.grpc.ForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.Status;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class InstrumentedServerCall<ReqT, RespT> extends ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT> {
    private final Instant start;
    private final MethodDescriptor.MethodType methodType;
    private final MetricsContainer container;

    InstrumentedServerCall(ServerCall<ReqT, RespT> delegate, MethodDescriptor.MethodType methodType, MetricsContainer container) {
        super(delegate);

        this.methodType = methodType;
        this.container = container;

        this.start = Instant.now();
        container.getActiveCallsCounter().inc();
    }

    @Override
    public void close(Status status, Metadata responseHeaders) {
        container.getResponseTimer().update(Duration.between(start, Instant.now()).toNanos(), TimeUnit.NANOSECONDS);
        container.getActiveCallsCounter().dec();
        container.getStatusMeters().getUnchecked(status).mark();
        super.close(status, responseHeaders);
    }

    @Override
    public void sendMessage(RespT message) {
        if (!methodType.serverSendsOneMessage()) {
            container.getStreamMessagesSentMeter().mark();
        }
        super.sendMessage(message);
    }
}
