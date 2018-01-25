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

import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

public class ServerMetricsInterceptor implements ServerInterceptor {
    private final ServerMetrics serverMetrics;

    public ServerMetricsInterceptor(ServerMetrics serverMetrics) {
        this.serverMetrics = serverMetrics;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        MethodDescriptor methodDescriptor = call.getMethodDescriptor();
        ServerCall<ReqT, RespT> instrumentedServerCall = new InstrumentedServerCall<>(call, methodDescriptor, serverMetrics);
        return new InstrumentedServerCallListener<>(next.startCall(instrumentedServerCall, headers), methodDescriptor, serverMetrics);
    }
}
