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
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.destination.specification.TelemetryDestinationGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.destination.specification.telemetry.destination.group.DestinationProfile;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.node.subscription.TelemetryNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.TelemetrySubscription;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.telemetry.subscription.TelemetryDestination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.telemetry.subscription.TelemetrySensor;
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
            builder.setConfigureResult(getConfigResult(false, "There is no Sensor group provided by input!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        for (TelemetrySensorGroup sensorGroup : sensorGroupList) {
            if (null == sensorGroup.getTelemetrySensorPaths() || sensorGroup.getTelemetrySensorPaths().isEmpty()) {
                builder.setConfigureResult(getConfigResult(false, "The sensor paths of " + sensorGroup.getTelemetrySensorGroupId() + " is null or empty!"));
                return RpcResultBuilder.success(builder.build()).buildFuture();
            }
        }


        LOG.info("Get sensor group in data store");
        List<TelemetrySensorGroup> allSensorGroupList = dataProcessor.getSensorGroupFromDataStore(IidConstants.TELEMETRY_IID);

        LOG.info("Check sensor group whether exist");
        for (TelemetrySensorGroup sensorGroup : sensorGroupList) {
            boolean existFlag = false;
            if (null != allSensorGroupList) {
                for (TelemetrySensorGroup allSensorGroup : allSensorGroupList) {
                    if (sensorGroup.getTelemetrySensorGroupId()
                            .equals(allSensorGroup.getTelemetrySensorGroupId())) {
                        existFlag = true;
                        break;
                    }
                }
            }
            if (existFlag) {
                builder.setConfigureResult(getConfigResult(false, "Sensor group " + sensorGroup.getTelemetrySensorGroupId() + " is exist!"));
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
            builder.setConfigureResult(getConfigResult(false, "There is no sensor group id provided by input!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        List<TelemetrySensorGroup> allSensorGroupList = dataProcessor.getSensorGroupFromDataStore(IidConstants.TELEMETRY_IID);
        if (null == allSensorGroupList || allSensorGroupList.isEmpty()) {
            builder.setConfigureResult(getConfigResult(false, "No sensor group configured!"));
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
            builder.setConfigureResult(getConfigResult(false, "There is no destination group provided by input!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }
        for (TelemetryDestinationGroup destinationGroup : destinationGroupList) {
            if (null == destinationGroup.getDestinationProfile() || destinationGroup.getDestinationProfile().isEmpty()) {
                builder.setConfigureResult(getConfigResult(false, "The destination profile of " + destinationGroup.getDestinationGroupId() + " is null or empty!"));
                return RpcResultBuilder.success(builder.build()).buildFuture();
            }
        }

        LOG.info("Get destination group in data store");
        List<TelemetryDestinationGroup> allDestinationGroupList = dataProcessor.getDestinationGroupFromDataStore(IidConstants.TELEMETRY_IID);

        LOG.info("Check destination group whether exist");
        for (TelemetryDestinationGroup destinationGroup : destinationGroupList) {
            boolean existFlag = false;
            if (null != allDestinationGroupList) {
                for (TelemetryDestinationGroup allDestinationGroup : allDestinationGroupList) {
                    if (destinationGroup.getDestinationGroupId()
                            .equals(allDestinationGroup.getDestinationGroupId())) {
                        existFlag = true;
                        break;
                    }
                }
            }
            if (existFlag) {
                builder.setConfigureResult(getConfigResult(false, "Destination group " + destinationGroup.getDestinationGroupId() + " is exist!"));
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
            builder.setConfigureResult(getConfigResult(false, "There is no destination group id provided by input!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        List<TelemetryDestinationGroup> allDestinationGroupList = dataProcessor.getDestinationGroupFromDataStore(IidConstants.TELEMETRY_IID);
        if (null == allDestinationGroupList || allDestinationGroupList.isEmpty()) {
            builder.setConfigureResult(getConfigResult(false, "No destination group configured!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        dataProcessor.deleteDestinationGroupFromDataStore(var1.getTelemetryDestination(), allDestinationGroupList);
        builder.setConfigureResult(getConfigResult(true, ""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public Future<RpcResult<ConfigureNodeTelemetrySubscriptionOutput>> configureNodeTelemetrySubscription(ConfigureNodeTelemetrySubscriptionInput var1) {
        //check input
        ConfigureNodeTelemetrySubscriptionOutputBuilder builder = new ConfigureNodeTelemetrySubscriptionOutputBuilder();
        if (null == var1) {
            builder.setConfigureResult(getConfigResult(false, "Input is null!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        List<TelemetryNode> nodeGroupList = var1.getTelemetryNode();
        if (null == nodeGroupList || nodeGroupList.isEmpty()) {
            builder.setConfigureResult(getConfigResult(false, "There is no node provided by input!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        for (TelemetryNode telemetryNodeGroup : nodeGroupList) {
            if (null == telemetryNodeGroup.getTelemetrySubscription() || telemetryNodeGroup.getTelemetrySubscription().isEmpty()) {
                builder.setConfigureResult(getConfigResult(false, "The subscription of " + telemetryNodeGroup.getNodeId() + " is null or empty!"));
                return RpcResultBuilder.success(builder.build()).buildFuture();
            }

            for (TelemetrySubscription subscription : telemetryNodeGroup.getTelemetrySubscription()) {
                if (!checkParamsInSubscriptionExist(subscription)) {
                    builder.setConfigureResult(getConfigResult(false, "Exit Params in subscription " + subscription.getSubscriptionName() + " of " + telemetryNodeGroup.getNodeId() + " is null!"));
                    return RpcResultBuilder.success(builder.build()).buildFuture();
                }

                if (null == subscription.getTelemetrySensor() || subscription.getTelemetrySensor().isEmpty()) {
                    builder.setConfigureResult(getConfigResult(false, "The sensor group of " + telemetryNodeGroup.getNodeId() + " is null or empty!"));
                    return RpcResultBuilder.success(builder.build()).buildFuture();
                } else {
                    for (TelemetrySensor sensor : subscription.getTelemetrySensor()) {
                        if (!checkSensorExit(sensor.getSensorGroupId())) {
                            builder.setConfigureResult(getConfigResult(false, "The sensor " + sensor.getSensorGroupId() + " of " + telemetryNodeGroup.getNodeId() + " is not configured in data store!"));
                            return RpcResultBuilder.success(builder.build()).buildFuture();
                        }
                        if (!checkParamsInSensorExist(sensor)) {
                            builder.setConfigureResult(getConfigResult(false, "Exit Params in sensor " + sensor.getSensorGroupId() + " of " + telemetryNodeGroup.getNodeId() + " is null!"));
                            return RpcResultBuilder.success(builder.build()).buildFuture();
                        }
                    }
                }

                if (null == subscription.getTelemetryDestination() || subscription.getTelemetryDestination().isEmpty()) {
                    builder.setConfigureResult(getConfigResult(false, "The destination group of " + telemetryNodeGroup.getNodeId() + " is null or empty!"));
                    return RpcResultBuilder.success(builder.build()).buildFuture();
                } else {
                    for (TelemetryDestination destination : subscription.getTelemetryDestination()) {
                        if (!checkDestinationExit(destination.getDestinationGroupId())) {
                            builder.setConfigureResult(getConfigResult(false, "The destination " + destination.getDestinationGroupId() + " of " + telemetryNodeGroup.getNodeId() + " is not configured in data store!"));
                            return RpcResultBuilder.success(builder.build()).buildFuture();
                        }
                    }
                }
            }


        }

//        LOG.info("Get node subscription in data store");
//        List<TelemetryNodeGroup> allnodeGroupList = dataProcessor.getNodeSubscriptionFromDataStore(IidConstants.TELEMETRY_IID);

        dataProcessor.addNodeSubscriptionToDataStore(var1.getTelemetryNode());
        builder.setConfigureResult(getConfigResult(true, ""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public Future<RpcResult<QueryNodeTelemetrySubscriptionOutput>> queryNodeTelemetrySubscription(QueryNodeTelemetrySubscriptionInput var1) {
        //check input
        if (null == var1) {
            return rpcErr("Input is null!");
        }

        List<TelemetryNode> allNodeSubscriptionList = dataProcessor.getNodeSubscriptionFromDataStore(IidConstants.TELEMETRY_IID);
        if (null == allNodeSubscriptionList || allNodeSubscriptionList.isEmpty()) {
            return rpcErr("No node subscription configured");
        }
        QueryNodeTelemetrySubscriptionOutputBuilder builder = new QueryNodeTelemetrySubscriptionOutputBuilder();
        builder.setTelemetryNode(allNodeSubscriptionList);
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    public Future<RpcResult<DeleteNodeTelemetrySubscriptionOutput>> deleteNodeTelemetrySubscription(DeleteNodeTelemetrySubscriptionInput var1) {
        //check input
        DeleteNodeTelemetrySubscriptionOutputBuilder builder = new DeleteNodeTelemetrySubscriptionOutputBuilder();
        if (null == var1) {
            builder.setConfigureResult(getConfigResult(false, "Input is null!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        if (null == var1.getTelemetryNode() || var1.getTelemetryNode().isEmpty()) {
            builder.setConfigureResult(getConfigResult(false, "There is no node id provided by input!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }
        List<TelemetryNode> allNodeSubscriptionList = dataProcessor.getNodeSubscriptionFromDataStore(IidConstants.TELEMETRY_IID);
        if (null == allNodeSubscriptionList || allNodeSubscriptionList.isEmpty()) {
            builder.setConfigureResult(getConfigResult(false, "No node subscription configured!"));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        dataProcessor.deleteNodeSubscriptionFromDataStore(var1.getTelemetryNode(), allNodeSubscriptionList);
        builder.setConfigureResult(getConfigResult(true, ""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
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

    private boolean checkParamsInSubscriptionExist(TelemetrySubscription subscription) {
        if (null == subscription.getLocalSourceAddress()) {
            return false;
        }
        return true;
    }

    private boolean checkSensorExit(String sensorId) {
        List<TelemetrySensorGroup> sensorGroupList = dataProcessor.getSensorGroupFromDataStore(IidConstants.TELEMETRY_IID);
        if (null ==  sensorGroupList || sensorGroupList.isEmpty()) {
            return false;
        }
        for (TelemetrySensorGroup sensorGroup : sensorGroupList) {
            if (sensorGroup.getTelemetrySensorGroupId().equals(sensorId)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkParamsInSensorExist(TelemetrySensor sensor) {
        if (null == sensor.getSampleInterval()) {
            return false;
        }
        return true;
    }

    private boolean checkDestinationExit(String destinationId) {
        List<TelemetryDestinationGroup> destinationGroupList = dataProcessor.getDestinationGroupFromDataStore(IidConstants.TELEMETRY_IID);
        if (null == destinationGroupList || destinationGroupList.isEmpty()) {
            return false;
        }
        for (TelemetryDestinationGroup destinationGroup : destinationGroupList) {
            if (destinationGroup.getDestinationGroupId().equals(destinationId)) {
                return true;
            }
        }
        return false;
    }

}
