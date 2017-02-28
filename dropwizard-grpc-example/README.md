#
Running the example:

1. Run `mvn verify`
2. Run `mvn exec:java -Dexec.mainClass=com.lowtuna.dropwizard.grpc.example.HelloWorldGrpcApplication` to start the gRPC server.
3. Open a  new terminal window in the same directory and run `mvn exec:java -Dexec.mainClass=com.lowtuna.dropwizard.grpc.example.GreeterClient` to run the gRPC stub that makes RPCs to the server.
