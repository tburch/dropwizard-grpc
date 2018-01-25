/**
 * Copyright 2018 Tristan Burch.
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
