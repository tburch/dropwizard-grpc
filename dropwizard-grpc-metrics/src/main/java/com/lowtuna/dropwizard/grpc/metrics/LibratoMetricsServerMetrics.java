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
import com.google.common.collect.ImmutableList;
import com.librato.metrics.client.Tag;
import com.librato.metrics.reporter.Librato;
import io.grpc.MethodDescriptor;
import io.grpc.Status;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LibratoMetricsServerMetrics implements ServerMetrics {
    private final Counter activeRequests;

    public LibratoMetricsServerMetrics() {
        this.activeRequests = Librato.metric("grpc-server-active-messages").counter();
    }

    @Override
    public void markMessageReceived(MethodDescriptor methodDescriptor) {
        GrpcMethod method = new GrpcMethod(methodDescriptor);
        Librato.metric("grpc-client-messages")
                .tags(method.getTags())
                .tag("direction", "inbound")
                .meter().mark();
    }

    @Override
    public void markMessageComplete(Duration elapsed, MethodDescriptor methodDescriptor) {
        GrpcMethod method = new GrpcMethod(methodDescriptor);
        Librato.metric("grpc-server-calls")
                .tags(method.getTags())
                .tag("status", "complete")
                .tag("direction", "inbound")
                .timer().update(elapsed.toNanos(), TimeUnit.NANOSECONDS);
    }

    @Override
    public void markMessageCanceled(Duration elapsed, MethodDescriptor methodDescriptor) {
        GrpcMethod method = new GrpcMethod(methodDescriptor);
        Librato.metric("grpc-server-calls")
                .tags(method.getTags())
                .tag("status", "cancelled")
                .tag("direction", "inbound")
                .timer().update(elapsed.toNanos(), TimeUnit.NANOSECONDS);
    }

    @Override
    public void callClosed(Status status, MethodDescriptor methodDescriptor) {
        GrpcMethod method = new GrpcMethod(methodDescriptor);
        Librato.metric("grpc-server-call-status")
                .tags(method.getTags())
                .tag("status", status.getCode())
                .meter().mark();
    }

    @Override
    public void messageSent(MethodDescriptor methodDescriptor) {
        GrpcMethod method = new GrpcMethod(methodDescriptor);
        Librato.metric("grpc-server-messages")
                .tags(method.getTags())
                .tag("direction", "outbound")
                .meter().mark();
    }

    @Override
    public void incActiveRequests() {
        activeRequests.inc();
    }

    @Override
    public void decActiveRequests() {
        activeRequests.dec();
    }

    private class GrpcMethod {
        private final String service;
        private final String method;
        private final MethodDescriptor.MethodType type;

        GrpcMethod(MethodDescriptor descriptor) {
            this.type = descriptor.getType();

            final String fullName = descriptor.getFullMethodName();
            final int delimIndex = fullName.lastIndexOf("/");
            final String unknown = "UNKNOWN";
            if (delimIndex == -1) {
                this.service = unknown;
                this.method = unknown;
            } else {
                this.service = fullName.substring(0, delimIndex);
                if (delimIndex >= fullName.length()) {
                    this.method = unknown;
                } else {
                    this.method = fullName.substring(delimIndex+1);
                }
            }
        }

        List<Tag> getTags() {
            return ImmutableList.of(
                    new Tag("grpcservice", service),
                    new Tag("method", method),
                    new Tag("type", type.toString())
            );
        }
    }
}
