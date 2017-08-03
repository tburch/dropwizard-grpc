/**
 * Copyright 2017 Tristan Burch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lowtuna.dropwizard.grpc;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.grpc.BindableService;
import io.grpc.CompressorRegistry;
import io.grpc.DecompressorRegistry;
import io.grpc.HandlerRegistry;
import io.grpc.ServerInterceptor;
import io.grpc.ServerServiceDefinition;
import io.grpc.ServerStreamTracer;
import io.grpc.ServerTransportFilter;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import org.apache.commons.lang3.tuple.Pair;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.concurrent.ExecutorService;

@Data
@Builder
public class GrpcEnvironment {
    @NotNull
    private final ExecutorService executorService;
    @Singular
    private final ImmutableSet<ServerServiceDefinition> serverServiceDefinitions;
    @Singular
    private final ImmutableSet<BindableService> bindableServices;
    @Singular
    private final ImmutableList<ServerInterceptor> interceptors;
    @Singular
    private final ImmutableList<ServerTransportFilter> transportFilters;
    @Singular
    private final ImmutableList<ServerStreamTracer.Factory> streamTracers;
    private final HandlerRegistry fallbackRegistry;
    private final Pair<File, File> tlsCertAndPrivateKey;
    private final DecompressorRegistry decompressorRegistry;
    private final CompressorRegistry compressorRegistry;
    @Singular
    private final ImmutableSet<GrpcServerLifecycleListener> lifecycleEvents;
}
