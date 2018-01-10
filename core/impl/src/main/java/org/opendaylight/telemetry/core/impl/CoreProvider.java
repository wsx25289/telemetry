/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.core.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.telemetry.core.api.TelemetryNotification;
import org.opendaylight.telemetry.core.api.TelemetryPacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoreProvider {

    private static final Logger LOG = LoggerFactory.getLogger(CoreProvider.class);

    private final DataBroker dataBroker;
    private TelemetryNotification telemetryNotification;
    private TelemetryPacketHandler handler;

    public CoreProvider(final DataBroker dataBroker, TelemetryNotificationImpl telemetryNotification,
                        TelemetryPacketHandlerImpl telemetryPacketHandler) {
        this.dataBroker = dataBroker;
        this.telemetryNotification = telemetryNotification;
        this.handler = telemetryPacketHandler;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("test Start");
//        TelemetryNotification telemetryNotification = new TelemetryNotificationImpl();
//        TelemetryPacketHandler handler = new TelemetryPacketHandlerImpl();
        telemetryNotification.subscribe(handler);
        telemetryNotification.publish("Testtttttttttttttttttttttttttttttt");
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("test Closed");
    }
}
