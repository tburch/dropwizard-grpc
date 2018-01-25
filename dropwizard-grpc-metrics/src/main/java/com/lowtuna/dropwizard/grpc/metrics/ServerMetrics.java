package com.lowtuna.dropwizard.grpc.metrics;

import io.grpc.MethodDescriptor;
import io.grpc.Status;

import java.time.Duration;

public interface ServerMetrics {
    void markMessageReceived(MethodDescriptor methodDescriptor);

    void markMessageComplete(Duration elapsed, MethodDescriptor methodDescriptor);

    void markMessageCanceled(Duration elapsed, MethodDescriptor methodDescriptor);

    void callClosed(Status status, MethodDescriptor methodDescriptor);

    void messageSent(MethodDescriptor methodDescriptor);

    void incActiveRequests();

    void decActiveRequests();
}
