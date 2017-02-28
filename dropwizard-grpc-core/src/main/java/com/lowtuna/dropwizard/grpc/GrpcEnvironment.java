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

import com.google.common.collect.ImmutableSet;
import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

@Data
@Builder
public class GrpcEnvironment {
  @Singular
  private final ImmutableSet<ServerServiceDefinition> serverServiceDefinitions;
  @Singular
  private final ImmutableSet<BindableService> bindableServices;
  @Singular
  private final ImmutableSet<GrpcServerLifecycleListener> lifecycleEvents;
}
