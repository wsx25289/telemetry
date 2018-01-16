/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.core.impl;

import com.lmax.disruptor.EventFactory;
import java.util.Collection;
import org.opendaylight.telemetry.core.api.TelemetryStreamMessageHandler;

public class TelemetryNotificationEvent<T> {

    public static final EventFactory<TelemetryNotificationEvent> FACTORY = TelemetryNotificationEvent::new;
    private T data;
    private Collection<TelemetryStreamMessageHandler> subscribers;

    public void initialize(T data, Collection<TelemetryStreamMessageHandler> subscribers) {
        this.data = data;
        this.subscribers = subscribers;
    }

    public void deliverNotification() {
        for (TelemetryStreamMessageHandler r : subscribers) {
            if (null != r) {
                r.process(data);
            }
        }
        //System.out.print("Data is: " + data);
    }
}
