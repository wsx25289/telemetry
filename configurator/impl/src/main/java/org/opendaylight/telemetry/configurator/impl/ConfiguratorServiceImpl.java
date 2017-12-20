/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.configurator.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.sensor.paths.TelemetrySensorPaths;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.sensor.specification.TelemetrySensorGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.configure.result.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.configure.result.ConfigureResult;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.delete.telemetry.destination.input.TelemetryDestination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.delete.telemetry.sensor.input.TelemetrySensor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.destination.specification.TelemetryDestinationGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.destination.specification.telemetry.destination.group.DestinationProfile;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.configure.result.ConfigureResult.Result;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfiguratorServiceImpl implements TelemetryConfiguratorApiService {
    private static final Logger LOG = LoggerFactory.getLogger(ConfiguratorServiceImpl.class);

    private ConfigurationWriter configurationWriter;
    private DataProcessor dataProcessor;

    public ConfiguratorServiceImpl(ConfigurationWriter configurationWriter, DataProcessor dataProcessor) {
        this.configurationWriter = configurationWriter;
        this.dataProcessor = dataProcessor;
    }

    public Future<RpcResult<AddTelemetrySensorOutput>> addTelemetrySensor(AddTelemetrySensorInput var1) {
        //check input
        AddTelemetrySensorOutputBuilder builder = new AddTelemetrySensorOutputBuilder();
        if (null == var1) {
            builder.setConfigureResult(getConfigResult(false, "Input is null!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }
        List<TelemetrySensorGroup> sensorGroupList = var1.getTelemetrySensorGroup();
        if (null == sensorGroupList || sensorGroupList.isEmpty()) {
            builder.setConfigureResult(getConfigResult(false, "Sensor group is null or empty!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        for (TelemetrySensorGroup sensorGroup : sensorGroupList) {
            if (null == sensorGroup.getTelemetrySensorGroupId()) {
                builder.setConfigureResult(getConfigResult(false, "Exist sensor group id is null!"));
                return RpcResultBuilder.success(builder.build()).buildFuture();
            }
            if (null == sensorGroup.getTelemetrySensorPaths() || sensorGroup.getTelemetrySensorPaths().isEmpty()) {
                builder.setConfigureResult(getConfigResult(false, "The sensor paths of {} is null or empty!" + sensorGroup.getTelemetrySensorGroupId()));
                return RpcResultBuilder.success(builder.build()).buildFuture();
            }
            for (TelemetrySensorPaths sensorPath : sensorGroup.getTelemetrySensorPaths()) {
                if (null == sensorPath.getTelemetrySensorPath()) {
                    builder.setConfigureResult(getConfigResult(false, "The sensor path of {} is null!" + sensorGroup.getTelemetrySensorGroupId()));
                    return RpcResultBuilder.success(builder.build()).buildFuture();
                }
            }
        }

        LOG.info("Get sensor group in data store");
        List<TelemetrySensorGroup> allSensorGroupList = dataProcessor.getSensorGroupFromDataStore(IidConstants.TELEMETRY_IID);

        LOG.info("Check sensor group whether exist");
        for (int iloop = 0; iloop < sensorGroupList.size(); ++iloop) {
            boolean existFlag = false;
            if (null != allSensorGroupList) {
                for (int jloop = 0; jloop < allSensorGroupList.size(); ++jloop) {
                    if (sensorGroupList.get(iloop).getTelemetrySensorGroupId()
                            .equals(allSensorGroupList.get(jloop).getTelemetrySensorGroupId())) {
                        existFlag = true;
                        break;
                    }
                }
            }
            if (existFlag) {
                builder.setConfigureResult(getConfigResult(false, "Sensor group is exist!"));
                return RpcResultBuilder.success(builder.build()).buildFuture();
            }
        }

        LOG.info("Write add telemetry sensor config to dataStore");
        dataProcessor.addSensorGroupToDataStore(sensorGroupList);
        builder.setConfigureResult(getConfigResult(true, ""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public Future<RpcResult<QueryTelemetrySensorOutput>> queryTelemetrySensor(QueryTelemetrySensorInput var1) {
        if (null == var1) {
            return rpcErr("Input is null!");
        }

        List<TelemetrySensorGroup> allSensorGroupList = dataProcessor.getSensorGroupFromDataStore(IidConstants.TELEMETRY_IID);
        if (null == allSensorGroupList || allSensorGroupList.isEmpty()) {
            return rpcErr("No sensor group configured");
        }
        QueryTelemetrySensorOutputBuilder builder = new QueryTelemetrySensorOutputBuilder();
        builder.setTelemetrySensorGroup(allSensorGroupList);
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public Future<RpcResult<DeleteTelemetrySensorOutput>> deleteTelemetrySensor(DeleteTelemetrySensorInput var1) {
        //check input
        DeleteTelemetrySensorOutputBuilder builder = new DeleteTelemetrySensorOutputBuilder();
        if (null == var1) {
            builder.setConfigureResult(getConfigResult(false, "Input is null!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }
        if (null == var1.getTelemetrySensor() || var1.getTelemetrySensor().isEmpty()) {
            builder.setConfigureResult(getConfigResult(false, "Sensor group is null or empty!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }
        for (TelemetrySensor sensor : var1.getTelemetrySensor()) {
            if (null == sensor.getSensorGroupId()) {
                builder.setConfigureResult(getConfigResult(false, "Exist sensor group id is null!"));
                return RpcResultBuilder.success(builder.build()).buildFuture();
            }
        }

        List<TelemetrySensorGroup> allSensorGroupList = dataProcessor.getSensorGroupFromDataStore(IidConstants.TELEMETRY_IID);
        if (null == allSensorGroupList || allSensorGroupList.isEmpty()) {
            builder.setConfigureResult(getConfigResult(false, "No sensor group in data store!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        //if (!dataProcessor.checkSensorGroupExist(var1.getTelemetrySensor(), allSensorGroupList)) {
        //    builder.setConfigureResult(getConfigResult(false, "Sensor group is not exist!"));
        //    return RpcResultBuilder.success(builder.build()).buildFuture();
        //}
        dataProcessor.deleteSensorGroupFromDataStore(var1.getTelemetrySensor(), allSensorGroupList);
        //convertModifiedInputToSouthData();
        //configurationWriter.writeTelemetryConfig(ConfigurationType.MODIFY, "1", "2");
        builder.setConfigureResult(getConfigResult(true, ""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public Future<RpcResult<AddTelemetryDestinationOutput>> addTelemetryDestination(AddTelemetryDestinationInput var1) {
        //check input
        AddTelemetryDestinationOutputBuilder builder = new AddTelemetryDestinationOutputBuilder();
        if (null == var1) {
            builder.setConfigureResult(getConfigResult(false, "Input is null!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }
        List<TelemetryDestinationGroup> destinationGroupList = var1.getTelemetryDestinationGroup();
        if (null == destinationGroupList || destinationGroupList.isEmpty()) {
            builder.setConfigureResult(getConfigResult(false, "Destination group is null or empty!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        for (TelemetryDestinationGroup destinationGroup : destinationGroupList) {
            if (null == destinationGroup.getDestinationGroupId()) {
                builder.setConfigureResult(getConfigResult(false, "Exist destination group id is null!"));
                return RpcResultBuilder.success(builder.build()).buildFuture();
            }
            if (null == destinationGroup.getDestinationProfile() || destinationGroup.getDestinationProfile().isEmpty()) {
                builder.setConfigureResult(getConfigResult(false, "The destination profile of {} is null or empty!" + destinationGroup.getDestinationGroupId()));
                return RpcResultBuilder.success(builder.build()).buildFuture();
            }
            for (DestinationProfile destinationProfile : destinationGroup.getDestinationProfile()) {
                if (null == destinationProfile.getDestinationAddress() || null == destinationProfile.getDestinationPort()) {
                    builder.setConfigureResult(getConfigResult(false, "The adress or port of destination profile {} is null!" + destinationGroup.getDestinationGroupId()));
                    return RpcResultBuilder.success(builder.build()).buildFuture();
                }
            }
        }

        LOG.info("Get destination group in data store");
        List<TelemetryDestinationGroup> allDestinationGroupList = dataProcessor.getDestinationGroupFromDataStore(IidConstants.TELEMETRY_IID);

        LOG.info("Check destination group whether exist");
        for (int iloop = 0; iloop < destinationGroupList.size(); ++iloop) {
            boolean existFlag = false;
            if (null != allDestinationGroupList) {
                for (int jloop = 0; jloop < allDestinationGroupList.size(); ++jloop) {
                    if (destinationGroupList.get(iloop).getDestinationGroupId()
                            .equals(allDestinationGroupList.get(jloop).getDestinationGroupId())) {
                        existFlag = true;
                        break;
                    }
                }
            }
            if (existFlag) {
                builder.setConfigureResult(getConfigResult(false, "Destination group is exist!"));
                return RpcResultBuilder.success(builder.build()).buildFuture();
            }
        }

        LOG.info("Write add telemetry sensor config to dataStore");
        dataProcessor.addDestinationGroupToDataStore(destinationGroupList);
        builder.setConfigureResult(getConfigResult(true, ""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public Future<RpcResult<QueryTelemetryDestinationOutput>> queryTelemetryDestination(QueryTelemetryDestinationInput var1) {
        //check input
        if (null == var1) {
            return rpcErr("Input is null!");
        }

        List<TelemetryDestinationGroup> allDestinationGroupList = dataProcessor.getDestinationGroupFromDataStore(IidConstants.TELEMETRY_IID);
        if (null == allDestinationGroupList || allDestinationGroupList.isEmpty()) {
            return rpcErr("No destination group configured");
        }
        QueryTelemetryDestinationOutputBuilder builder = new QueryTelemetryDestinationOutputBuilder();
        builder.setTelemetryDestinationGroup(allDestinationGroupList);
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public Future<RpcResult<DeleteTelemetryDestinationOutput>> deleteTelemetryDestination(DeleteTelemetryDestinationInput var1) {
        //check input
        DeleteTelemetryDestinationOutputBuilder builder = new DeleteTelemetryDestinationOutputBuilder();
        if (null == var1) {
            builder.setConfigureResult(getConfigResult(false, "Input is null!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }
        if (null == var1.getTelemetryDestination() || var1.getTelemetryDestination().isEmpty()) {
            builder.setConfigureResult(getConfigResult(false, "Destination group is null or empty!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }
        for (TelemetryDestination telDst : var1.getTelemetryDestination()) {
            if (null == telDst.getDestinationGroupId()) {
                builder.setConfigureResult(getConfigResult(false, "Exist destination group id is null!"));
                return RpcResultBuilder.success(builder.build()).buildFuture();
            }
        }

        List<TelemetryDestinationGroup> allDestinationGroupList = dataProcessor.getDestinationGroupFromDataStore(IidConstants.TELEMETRY_IID);
        if (null == allDestinationGroupList || allDestinationGroupList.isEmpty()) {
            builder.setConfigureResult(getConfigResult(false, "No destination group in data store!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        dataProcessor.deleteDestinationGroupFromDataStore(var1.getTelemetryDestination(), allDestinationGroupList);
        builder.setConfigureResult(getConfigResult(true, ""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public Future<RpcResult<ConfigureTelemetrySubscriptionOutput>> configureTelemetrySubscription(ConfigureTelemetrySubscriptionInput var1) {
        return null;
    }

    public Future<RpcResult<QueryTelemetrySubscriptionOutput>> queryTelemetrySubscription(QueryTelemetrySubscriptionInput var1) {
        return null;
    }

    private void convertAddedInputToSouthData() {

    }

    private void convertModifiedInputToSouthData() {

    }

    private void convertDeletedInputToSouthData() {

    }

    private ConfigureResult getConfigResult(boolean result, String errorCause) {
        ConfigureResultBuilder cfgResultBuilder  = new ConfigureResultBuilder();
        if (result) {
            cfgResultBuilder.setResult(Result.SUCCESS);
        } else {
            cfgResultBuilder.setResult(Result.FAILURE);
            cfgResultBuilder.setErrorCause(errorCause);
        }
        return cfgResultBuilder.build();
    }

    private <T> Future<RpcResult<T>> rpcErr(String errMsg) {
        return RpcResultBuilder.<T>failed().withError(RpcError.ErrorType.APPLICATION, errMsg).buildFuture();
    }
}
