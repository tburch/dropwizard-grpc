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

import io.grpc.ForwardingServerCallListener;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

public class InstrumentedServerCallListener<ReqT> extends ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT> {
    private final MethodDescriptor methodDescriptor;
    private final ServerMetrics serverMetrics;

    private final AtomicReference<Instant> start = new AtomicReference<>();

    InstrumentedServerCallListener(ServerCall.Listener<ReqT> delegate, MethodDescriptor methodDescriptor, ServerMetrics serverMetrics) {
        super(delegate);
        this.methodDescriptor = methodDescriptor;
        this.serverMetrics = serverMetrics;
    }

    @Override
    public void onReady() {
        start.compareAndSet(null, Instant.now());
        serverMetrics.incActiveRequests();
        super.onReady();
    }

    @Override
    public void onMessage(ReqT request) {
        serverMetrics.markMessageReceived(methodDescriptor);
        super.onMessage(request);
    }

    @Override
    public void onComplete() {
        try {
            super.onComplete();
        } finally {
            serverMetrics.decActiveRequests();
            serverMetrics.markMessageComplete(Duration.between(start.get(), Instant.now()), methodDescriptor);
        }
    }

    @Override
    public void onCancel() {
        try {
            super.onCancel();
        } finally {
            serverMetrics.decActiveRequests();
            serverMetrics.markMessageCanceled(Duration.between(start.get(), Instant.now()), methodDescriptor);
        }
    }
}
