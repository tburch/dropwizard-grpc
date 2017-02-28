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

public interface GrpcServerLifecycleListener {
  /**
   * Called before the gRPC server is started.
   */
  void preServerStart();

  /**
   * Called after the gRPC server has been started. This will only be called if the server successfully starts.
   */
  void postServerStart();

  /**
   * Called before the gRPC server is stopped.
   */
  void preServerStop();

  /**
   * Called after the gRPC server has been stopped.
   */
  void postServerStop();
}
