/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.test.impl;

import org.opendaylight.telemetry.core.api.TelemetryNotification;
import org.opendaylight.telemetry.test.api.TelemetryPacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestClass {

    private static final Logger LOG = LoggerFactory.getLogger(TestClass.class);

    private TelemetryNotification telemetryNotification;

    public TestClass(TelemetryNotification telemetryNotification) {
        this.telemetryNotification = telemetryNotification;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("test Start");
        TelemetryPacketHandler handler = new TelemetryPacketHandlerImpl();
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
