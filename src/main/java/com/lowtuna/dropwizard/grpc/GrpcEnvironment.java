package com.lowtuna.dropwizard.grpc;

import com.google.common.collect.ImmutableSet;
import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

/**
 * Created by tburch on 1/6/17.
 */
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
