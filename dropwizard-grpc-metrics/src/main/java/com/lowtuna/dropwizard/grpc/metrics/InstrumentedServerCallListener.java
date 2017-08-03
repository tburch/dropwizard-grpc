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

public class InstrumentedServerCallListener<ReqT> extends ForwardingServerCallListener<ReqT> {
    private final ServerCall.Listener<ReqT> delegate;
    private final MethodDescriptor.MethodType methodType;
    private final MetricsContainer metricsContainer;

    InstrumentedServerCallListener(ServerCall.Listener<ReqT> delegate, MethodDescriptor.MethodType methodType, MetricsContainer metricsContainer) {
        this.delegate = delegate;
        this.methodType = methodType;
        this.metricsContainer = metricsContainer;
    }

    @Override
    protected ServerCall.Listener<ReqT> delegate() {
        return delegate;
    }

    @Override
    public void onMessage(ReqT request) {
        if (!methodType.clientSendsOneMessage()) {
            metricsContainer.getStreamMessagesReceivedMeter().mark();
        }
        super.onMessage(request);
    }
}
