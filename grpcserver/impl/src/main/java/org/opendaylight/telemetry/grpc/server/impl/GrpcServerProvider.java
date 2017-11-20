/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.grpc.server.impl;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloRequest;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrpcServerProvider {

    private static final Logger LOG = LoggerFactory.getLogger(GrpcServerProvider.class);

    private final DataBroker dataBroker;

    private Server server;

    public GrpcServerProvider(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("GrpcServerProvider Session Initiated");
        try {
            start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("GrpcServerProvider Closed");
    }

    private void start() throws IOException {
        int port = 50051;
        server = ServerBuilder.forPort(port).addService(new GreeterImpl()).build().start();
        LOG.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                stopnow();
                System.err.println("*** server shut down");
            }
        });
    }

    private void stopnow() {
        if (null != server) {
            server.shutdown();
        }
    }

    static class GreeterImpl extends GreeterGrpc.GreeterImplBase {
        @Override
        public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
            HelloReply reply = HelloReply.newBuilder().setMessage("Hello beautiful " + req.getName()).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }
}
