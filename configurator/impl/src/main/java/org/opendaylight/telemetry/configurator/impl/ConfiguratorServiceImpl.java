/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.configurator.impl;

import java.util.concurrent.Future;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.AddTelemetryConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.AddTelemetryConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.AddTelemetryConfigOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.DeleteTelemetryConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.DeleteTelemetryConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.ModifyTelemetryConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.ModifyTelemetryConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.TelemetryConfiguratorApiService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfiguratorServiceImpl implements TelemetryConfiguratorApiService {
    private static final Logger LOG = LoggerFactory.getLogger(ConfiguratorServiceImpl.class);

    private ConfigurationWriter configurationWriter;
    private DataStoreProcessor dataStoreProcessor;

    public ConfiguratorServiceImpl(ConfigurationWriter configurationWriter, DataStoreProcessor dataStoreProcessor) {
        this.configurationWriter = configurationWriter;
        this.dataStoreProcessor = dataStoreProcessor;
    }

    public Future<RpcResult<AddTelemetryConfigOutput>> addTelemetryConfig(AddTelemetryConfigInput var1) {
        //check input
        convertAddedInputToSouthData();
        //configurationWriter.writeTelemetryConfig(ConfigurationType.ADD, "1", "2");
        dataStoreProcessor.writeConfigToDataStore();
        AddTelemetryConfigOutputBuilder builder = new AddTelemetryConfigOutputBuilder();
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public Future<RpcResult<ModifyTelemetryConfigOutput>> modifyTelemetryConfig(ModifyTelemetryConfigInput var1) {
        //check input
        convertModifiedInputToSouthData();
        configurationWriter.writeTelemetryConfig(ConfigurationType.MODIFY, "1", "2");
        dataStoreProcessor.writeConfigToDataStore();
        return null;
    }

    public Future<RpcResult<DeleteTelemetryConfigOutput>> deleteTelemetryConfig(DeleteTelemetryConfigInput var1) {
        //check input
        convertDeletedInputToSouthData();
        configurationWriter.writeTelemetryConfig(ConfigurationType.DELETE, "1", "2");
        dataStoreProcessor.writeConfigToDataStore();
        return null;
    }

    private void convertAddedInputToSouthData() {

    }

    private void convertModifiedInputToSouthData() {

    }

    private void convertDeletedInputToSouthData() {

    }
}
