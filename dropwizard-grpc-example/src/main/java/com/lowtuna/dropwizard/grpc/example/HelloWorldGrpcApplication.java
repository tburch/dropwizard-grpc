package com.lowtuna.dropwizard.grpc.example;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.lowtuna.dropwizard.grpc.GrpcApplication;
import com.lowtuna.dropwizard.grpc.GrpcEnvironment;
import com.lowtuna.dropwizard.grpc.examples.helloworld.GreeterGrpc;
import io.dropwizard.setup.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

/**
 * Created by tburch on 2/27/17.
 */
public class HelloWorldGrpcApplication extends GrpcApplication<HelloWorldConfiguration> {

  public static void main(String[] args) throws Exception {
    File tmpDir = Files.createTempDir();
    tmpDir.deleteOnExit();

    File tmpConfig = new File(tmpDir, "helloWorld.yml");
    URL config = Resources.getResource("helloWorld.yml");
    Resources.copy(config, new FileOutputStream(tmpConfig));

    String[] appArgs = new String[] {"gRPC", tmpConfig.getAbsolutePath()};
    new HelloWorldGrpcApplication().run(appArgs);
  }

  @Override
  public void run(HelloWorldConfiguration configuration, Environment environment, GrpcEnvironment.GrpcEnvironmentBuilder grpcEnvironmentBuilder) throws Exception {
    GreeterGrpc.GreeterImplBase greeterService = new GreeterServiceImpl(configuration.getName());
    grpcEnvironmentBuilder.bindableService(greeterService);
  }

}
