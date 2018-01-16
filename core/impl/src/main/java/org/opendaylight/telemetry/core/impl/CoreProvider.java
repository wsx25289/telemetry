/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.core.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.telemetry.core.api.TelemetryStreamMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoreProvider {

    private static final Logger LOG = LoggerFactory.getLogger(CoreProvider.class);

    private final DataBroker dataBroker;
    private TelemetryStreamMessageHandler handler;

    public CoreProvider(final DataBroker dataBroker, TelemetryStreamMessageHandlerImpl telemetryStreamMessageHandler) {
        this.dataBroker = dataBroker;
        this.handler = telemetryStreamMessageHandler;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("test Start");
//        TelemetryNotification telemetryNotification = new TelemetryNotificationImpl();
//        TelemetryPacketHandler handler = new TelemetryStreamMessageHandlerImpl();
        TelemetryNotificationImpl.getInstance().subscribe(handler);
        TelemetryNotificationImpl.getInstance().publish("Testtttttttttttttttttttttttttttttt");
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("test Closed");
    }
}
