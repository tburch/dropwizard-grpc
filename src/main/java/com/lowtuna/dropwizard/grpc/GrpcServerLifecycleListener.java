package com.lowtuna.dropwizard.grpc;

/**
 * Created by tburch on 1/6/17.
 */
public interface GrpcServerLifecycleListener {
  void preServerStart();

  void postServerStart();

  void preServerStop();

  void postServerStop();
}
