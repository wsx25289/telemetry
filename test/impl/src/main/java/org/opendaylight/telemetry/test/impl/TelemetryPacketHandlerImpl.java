/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.test.impl;

import org.opendaylight.telemetry.test.api.TelemetryPacketHandler;

public class TelemetryPacketHandlerImpl implements TelemetryPacketHandler {


    @Override
    public <T> void process(T data) {
        System.out.print("Process data is: " + data);
    }
}
