package com.lowtuna.dropwizard.grpc;

import io.dropwizard.validation.PortRange;
import lombok.Data;

/**
 * Created by tburch on 1/6/17.
 */
@Data
public class GrpcConnectorConfiguration {
  @PortRange
  private int port = 8080;
}
