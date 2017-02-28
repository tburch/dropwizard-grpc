package com.lowtuna.dropwizard.grpc.example;

import io.dropwizard.Configuration;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * Created by tburch on 2/27/17.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class HelloWorldConfiguration extends Configuration {
  @NotNull
  private String name;
}
