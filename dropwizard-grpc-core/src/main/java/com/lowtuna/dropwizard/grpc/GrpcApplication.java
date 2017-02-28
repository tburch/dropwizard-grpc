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
package com.lowtuna.dropwizard.grpc;

import io.dropwizard.Application;
import io.dropwizard.Bundle;
import io.dropwizard.Configuration;
import io.dropwizard.cli.CheckCommand;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class GrpcApplication<T extends Configuration> extends Application<T> {

  /**
   * When the application runs, this is called after the {@link Bundle}s are run. Override it to add
   * providers, resources, etc. for your  Grpc application.
   *
   * @param configuration           the parsed {@link Configuration} object
   * @param environment             the application's {@link Environment}
   * @param grpcEnvironmentBuilder  the builder that's being used to configure the gRPC server's services
   * @throws Exception if something goes wrong
   */
  public abstract void run(T configuration, Environment environment,
                           GrpcEnvironment.GrpcEnvironmentBuilder grpcEnvironmentBuilder) throws Exception;

  @Override
  protected void addDefaultCommands(Bootstrap<T> bootstrap) {
    bootstrap.addCommand(new GrpcServerCommand<>(this));
    bootstrap.addCommand(new CheckCommand<>(this));
  }

  @Override
  public void run(T configuration, Environment environment) throws Exception {
  }
}
