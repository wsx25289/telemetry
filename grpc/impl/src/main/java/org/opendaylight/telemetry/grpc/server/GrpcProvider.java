/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.grpc.server;

import com.google.common.util.concurrent.SettableFuture;
import io.grpc.Server;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.grpc.rev170830.GrpcService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.grpc.rev170830.StartGrpcServerInput;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrpcProvider implements GrpcService {
    private static final Logger LOG = LoggerFactory.getLogger(GrpcProvider.class);
    private final DataBroker dataBroker;
    private Server server;

    public GrpcProvider(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public void init() {
        LOG.info("gRPC Provider Session Initiated");
    }

    public void close() {
        LOG.info("GrpcServerProvider Closed");
    }

    private void stop() {
        if (null != server) {
            server.shutdown();
        }
    }

    @Override
    public Future<RpcResult<Void>> startGrpcServer(StartGrpcServerInput input) {
        int port = input.getPort().getValue();
        SettableFuture<RpcResult<Void>> future = SettableFuture.create();

        new Thread(() -> {
            try {
                TelemetryServer server = new TelemetryServer(port);
                server.start();
                server.blockUntilShutdown();
            } catch (Exception e) {
                future.set(RpcResultBuilder
                        .<Void>failed()
                        .withError(RpcError.ErrorType.APPLICATION, "Start gRPC server failed.").build());
            }
        }).start();

        return future;
    }
}
