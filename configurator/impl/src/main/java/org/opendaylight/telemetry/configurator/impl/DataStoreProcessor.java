/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.configurator.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataStoreProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(DataStoreProcessor.class);

    private final DataBroker dataBroker;

    public DataStoreProcessor(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public void writeConfigToDataStore() {

    }

}
