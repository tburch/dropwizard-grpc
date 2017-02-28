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

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.jetty.ConnectorFactory;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.jetty.RoutingHandler;
import io.dropwizard.server.AbstractServerFactory;
import io.dropwizard.setup.Environment;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.ContainerLifeCycle;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 a lot of this was copied from https://github.com/dropwizard/dropwizard/blob/7295c5faf42840325c161cc82ad4ed1db2bf8419/dropwizard-core/src/main/java/io/dropwizard/server/DefaultServerFactory.java
 */
@JsonTypeName("gRPC")
@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class GrpcServerFactory extends AbstractServerFactory {
  @Valid
  @NotNull
  @Getter
  private GrpcConnectorConfiguration applicationConnector = new GrpcConnectorConfiguration();

  @NotEmpty
  private String adminContextPath = "/";

  @Valid
  @NotNull
  private List<ConnectorFactory> adminConnectors = Collections.singletonList(HttpConnectorFactory.admin());

  @Override
  public Server build(Environment environment) {
    printBanner(environment.getName());
    final ThreadPool threadPool = createThreadPool(environment.metrics());
    final Server server = buildServer(environment.lifecycle(), threadPool);

    final Handler adminHandler = createAdminServlet(server,
            environment.getAdminContext(),
            environment.metrics(),
            environment.healthChecks());
    final RoutingHandler routingHandler = buildRoutingHandler(environment.metrics(),
            server,
            adminHandler);
    final Handler gzipHandler = buildGzipHandler(routingHandler);
    server.setHandler(addStatsHandler(addRequestLog(server, gzipHandler, environment.getName())));
    return server;
  }

  private RoutingHandler buildRoutingHandler(MetricRegistry metricRegistry,
                                             Server server,
                                             Handler adminHandler) {
    final List<Connector> adConnectors = buildAdminConnectors(metricRegistry, server);

    final Map<Connector, Handler> handlers = new LinkedHashMap<>();

    for (Connector connector : adConnectors) {
      server.addConnector(connector);
      handlers.put(connector, adminHandler);
    }

    return new RoutingHandler(handlers);
  }

  private List<Connector> buildAdminConnectors(MetricRegistry metricRegistry, Server server) {
    // threadpool is shared between all the connectors, so it should be managed by the server instead of the
    // individual connectors
    final QueuedThreadPool threadPool = new QueuedThreadPool(getMaxThreads(), getMinThreads());
    threadPool.setName("dw-admin");
    server.addBean(threadPool);

    final List<Connector> connectors = new ArrayList<>();
    for (ConnectorFactory factory : adminConnectors) {
      final Connector connector = factory.build(server, metricRegistry, "admin", threadPool);
      if (connector instanceof ContainerLifeCycle) {
        ((ContainerLifeCycle) connector).unmanage(threadPool);
      }
      connectors.add(connector);
    }
    return connectors;
  }

  @Override
  public void configure(Environment environment) {
    log.info("Registering admin handler with root path prefix: {}", adminContextPath);
    environment.getAdminContext().setContextPath(adminContextPath);
  }
}
