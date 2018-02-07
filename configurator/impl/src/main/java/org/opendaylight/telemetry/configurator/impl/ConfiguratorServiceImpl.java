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

    private DataProcessor dataProcessor;
    private static final String INPUT_NULL = "Input is null";
    private static final String SENSOR_GROUP_NULL = "There is no sensor group provided by input!";
    private static final String SENSOR_PATHS = " sensor paths not provided by input!";
    private static final String SENSOR_GROUP_EXIST = "There are sensor groups Exist!";
    private static final String NO_SENSOR_GROUP = "No sensor group configured!";
    private static final String SENSOR_GROUP_ID_NULL = "There is no sensor group id provided by input!";

    private static final String DES_GROUP_NULL = "There is no destination group provided by input!";
    private static final String DES_FILE = " destination profile not provided by input!";
    private static final String DES_GROUP_EXIST = "There are destination groups Exist!";
    private static final String NO_DES_GROUP = "No destination group configured!";
    private static final String DES_GROUP_ID_NULL = "There is no destination group id provided by input!";
    private static final String NO_SUBSCR = "No node subscription configured!";

    private static final String NODE_NULL = "There is no node id provided by input!";
    private static final String SUBSCR_NULL = " no subscription provided by input!";
    private static final String NODE_SUBSCR_NULL = "Exist node not provide subscription!";
    private static final String NODE_SUBSCR_SENSOR_NULL = "There is no sensor provided in node subscription!";
    private static final String NODE_SUBSCR_DES_NULL = "There is no destination provided in node subscription!";
    private static final String SUBSCR_PARAS_NULL = "Exist Params is null!";
    private static final String SUBSCR_SENSOR_ABNORMAL = "Sensor empty in node subscription or exist Params in" +
            " sensor is null or exist sensor not configured!";
    private static final String SUBSCR_DES_ABNORMAL = "Destination empty in node subscription" +
            " or exist destination not configured!";

    public ConfiguratorServiceImpl(DataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor;
    }

    @Override
    public Future<RpcResult<AddTelemetrySensorOutput>> addTelemetrySensor(AddTelemetrySensorInput input) {
        //check input
        AddTelemetrySensorOutputBuilder builder = new AddTelemetrySensorOutputBuilder();
        if (null == input) {
            builder.setConfigureResult(getConfigResult(false, INPUT_NULL));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }
        List<TelemetrySensorGroup> sensorGroupList = input.getTelemetrySensorGroup();
        if (null == sensorGroupList || sensorGroupList.isEmpty()) {
            builder.setConfigureResult(getConfigResult(false, SENSOR_GROUP_NULL));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        for (TelemetrySensorGroup sensorGroup : sensorGroupList) {
            if (null == sensorGroup.getTelemetrySensorPaths() || sensorGroup.getTelemetrySensorPaths().isEmpty()) {
                builder.setConfigureResult(getConfigResult(false, sensorGroup.getTelemetrySensorGroupId() + SENSOR_PATHS));
                return RpcResultBuilder.success(builder.build()).buildFuture();
            }
        }

        LOG.info("Check sensor group whether exist");
        if (checkSensorGroupExistedInDataStore(sensorGroupList)) {
            builder.setConfigureResult(getConfigResult(false, SENSOR_GROUP_EXIST));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        LOG.info("Write add telemetry sensor config to dataStore");
        dataProcessor.addSensorGroupToDataStore(sensorGroupList);
        builder.setConfigureResult(getConfigResult(true, ""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<QueryTelemetrySensorOutput>> queryTelemetrySensor(QueryTelemetrySensorInput input) {
        if (null == input) {
            return rpcErr(INPUT_NULL);
        }

        List<TelemetrySensorGroup> allSensorGroupList = dataProcessor.getSensorGroupFromDataStore(IidConstants.TELEMETRY_IID);
        if (null == allSensorGroupList || allSensorGroupList.isEmpty()) {
            return rpcErr(NO_SENSOR_GROUP);
        }
        QueryTelemetrySensorOutputBuilder builder = new QueryTelemetrySensorOutputBuilder();
        builder.setTelemetrySensorGroup(allSensorGroupList);
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<DeleteTelemetrySensorOutput>> deleteTelemetrySensor(DeleteTelemetrySensorInput input) {
        //check input
        DeleteTelemetrySensorOutputBuilder builder = new DeleteTelemetrySensorOutputBuilder();
        if (null == input) {
            builder.setConfigureResult(getConfigResult(false, INPUT_NULL));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }
        if (null == input.getTelemetrySensorGroup() || input.getTelemetrySensorGroup().isEmpty()) {
            builder.setConfigureResult(getConfigResult(false, SENSOR_GROUP_ID_NULL));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        for (int i = 0; i < input.getTelemetrySensorGroup().size(); i++) {
            dataProcessor.deleteSensorGroupFromDataStore(input.getTelemetrySensorGroup().get(i).getSensorGroupId());
        }

        builder.setConfigureResult(getConfigResult(true, ""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<AddTelemetryDestinationOutput>> addTelemetryDestination(AddTelemetryDestinationInput input) {
        //check input
        AddTelemetryDestinationOutputBuilder builder = new AddTelemetryDestinationOutputBuilder();
        if (null == input) {
            builder.setConfigureResult(getConfigResult(false, INPUT_NULL));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }
        List<TelemetryDestinationGroup> destinationGroupList = input.getTelemetryDestinationGroup();
        if (null == destinationGroupList || destinationGroupList.isEmpty()) {
            builder.setConfigureResult(getConfigResult(false, DES_GROUP_NULL));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }
        for (TelemetryDestinationGroup destinationGroup : destinationGroupList) {
            if (null == destinationGroup.getDestinationProfile() || destinationGroup.getDestinationProfile().isEmpty()) {
                builder.setConfigureResult(getConfigResult(false, destinationGroup.getDestinationGroupId() + DES_FILE));
                return RpcResultBuilder.success(builder.build()).buildFuture();
            }
        }

        LOG.info("Check destination group whether exist");
        if (checkDesGroupExistedInDataStore(destinationGroupList)) {
            builder.setConfigureResult(getConfigResult(false, DES_GROUP_EXIST));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        LOG.info("Write add telemetry sensor config to dataStore");
        dataProcessor.addDestinationGroupToDataStore(destinationGroupList);
        builder.setConfigureResult(getConfigResult(true, ""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<QueryTelemetryDestinationOutput>> queryTelemetryDestination(QueryTelemetryDestinationInput input) {
        //check input
        if (null == input) {
            return rpcErr(INPUT_NULL);
        }

        List<TelemetryDestinationGroup> allDestinationGroupList = dataProcessor.getDestinationGroupFromDataStore(IidConstants.TELEMETRY_IID);
        if (null == allDestinationGroupList || allDestinationGroupList.isEmpty()) {
            return rpcErr(NO_DES_GROUP);
        }
        QueryTelemetryDestinationOutputBuilder builder = new QueryTelemetryDestinationOutputBuilder();
        builder.setTelemetryDestinationGroup(allDestinationGroupList);
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<DeleteTelemetryDestinationOutput>> deleteTelemetryDestination(DeleteTelemetryDestinationInput input) {
        //check input
        DeleteTelemetryDestinationOutputBuilder builder = new DeleteTelemetryDestinationOutputBuilder();
        if (null == input) {
            builder.setConfigureResult(getConfigResult(false, INPUT_NULL));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }
        if (null == input.getTelemetryDestinationGroup() || input.getTelemetryDestinationGroup().isEmpty()) {
            builder.setConfigureResult(getConfigResult(false, DES_GROUP_ID_NULL));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        for (int i = 0; i < input.getTelemetryDestinationGroup().size(); i++) {
            dataProcessor.deleteDestinationGroupFromDataStore(input.getTelemetryDestinationGroup().get(i).getDestinationGroupId());
        }

        builder.setConfigureResult(getConfigResult(true, ""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<ConfigureNodeTelemetrySubscriptionOutput>> configureNodeTelemetrySubscription(ConfigureNodeTelemetrySubscriptionInput input) {
        //check input
        ConfigureNodeTelemetrySubscriptionOutputBuilder builder = new ConfigureNodeTelemetrySubscriptionOutputBuilder();
        if (null == input) {
            builder.setConfigureResult(getConfigResult(false, INPUT_NULL));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        List<TelemetryNode> nodeGroupList = input.getTelemetryNode();
        if (null == nodeGroupList || nodeGroupList.isEmpty()) {
            builder.setConfigureResult(getConfigResult(false, NODE_NULL));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        for (TelemetryNode telemetryNodeGroup : nodeGroupList) {
            if (null == telemetryNodeGroup.getTelemetrySubscription() || telemetryNodeGroup.getTelemetrySubscription().isEmpty()) {
                builder.setConfigureResult(getConfigResult(false, telemetryNodeGroup.getNodeId() + SUBSCR_NULL));
                return RpcResultBuilder.success(builder.build()).buildFuture();
            }

            for (TelemetrySubscription subscription : telemetryNodeGroup.getTelemetrySubscription()) {
                if (!checkParamsInSubscriptionExist(subscription)) {
                    builder.setConfigureResult(getConfigResult(false, subscription.getSubscriptionName() + SUBSCR_PARAS_NULL + telemetryNodeGroup.getNodeId()));
                    return RpcResultBuilder.success(builder.build()).buildFuture();
                }
            }
        }

        if (!checkSubscriSensorProvidedByConfigSubscriInput(nodeGroupList)) {
            builder.setConfigureResult(getConfigResult(false, SUBSCR_SENSOR_ABNORMAL));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        if (!checkSubscriDesProvidedByConfigSubscriInput(nodeGroupList)) {
            builder.setConfigureResult(getConfigResult(false, SUBSCR_DES_ABNORMAL));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        dataProcessor.addNodeSubscriptionToDataStore(input.getTelemetryNode());
        builder.setConfigureResult(getConfigResult(true, ""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<QueryNodeTelemetrySubscriptionOutput>> queryNodeTelemetrySubscription(QueryNodeTelemetrySubscriptionInput input) {
        //check input
        if (null == input) {
            return rpcErr(INPUT_NULL);
        }

        List<TelemetryNode> allNodeSubscriptionList = dataProcessor.getNodeSubscriptionFromDataStore(IidConstants.TELEMETRY_IID);
        if (null == allNodeSubscriptionList || allNodeSubscriptionList.isEmpty()) {
            return rpcErr(NO_SUBSCR);
        }
        QueryNodeTelemetrySubscriptionOutputBuilder builder = new QueryNodeTelemetrySubscriptionOutputBuilder();
        builder.setTelemetryNode(allNodeSubscriptionList);
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<DeleteNodeTelemetrySubscriptionOutput>> deleteNodeTelemetrySubscription(DeleteNodeTelemetrySubscriptionInput input) {
        //check input
        DeleteNodeTelemetrySubscriptionOutputBuilder builder = new DeleteNodeTelemetrySubscriptionOutputBuilder();
        if (null == input) {
            builder.setConfigureResult(getConfigResult(false, INPUT_NULL));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        if (null == input.getTelemetryNode() || input.getTelemetryNode().isEmpty()) {
            builder.setConfigureResult(getConfigResult(false, NODE_NULL));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        for (int i = 0; i < input.getTelemetryNode().size(); i++) {
            if (null == input.getTelemetryNode().get(i).getTelemetryNodeSubscription()
                    || input.getTelemetryNode().get(i).getTelemetryNodeSubscription().isEmpty()) {
                builder.setConfigureResult(getConfigResult(false, input.getTelemetryNode().get(i).getNodeId() + SUBSCR_NULL));
                return RpcResultBuilder.success(builder.build()).buildFuture();
            }
        }

        List<TelemetryNode> allNodeSubscriptionList = dataProcessor.getNodeSubscriptionFromDataStore(IidConstants.TELEMETRY_IID);
        if (null == allNodeSubscriptionList || allNodeSubscriptionList.isEmpty()) {
            builder.setConfigureResult(getConfigResult(false, NO_SUBSCR));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        for (int i = 0; i < input.getTelemetryNode().size(); i++) {
            dataProcessor.deleteNodeSubscriptionFromDataStore(input.getTelemetryNode().get(i).getNodeId(),
                    input.getTelemetryNode().get(i).getTelemetryNodeSubscription());
        }
        builder.setConfigureResult(getConfigResult(true, ""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<DeleteNodeTelemetrySubscriptionSensorOutput>> deleteNodeTelemetrySubscriptionSensor(DeleteNodeTelemetrySubscriptionSensorInput input) {
        DeleteNodeTelemetrySubscriptionSensorOutputBuilder builder = new DeleteNodeTelemetrySubscriptionSensorOutputBuilder();
        if (null == input) {
            builder.setConfigureResult(getConfigResult(false, INPUT_NULL));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        if (null == input.getTelemetryNode() || input.getTelemetryNode().isEmpty()) {
            builder.setConfigureResult(getConfigResult(false, NODE_NULL));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        if (!checkSubscriProvidedByDelSensorInput(input)) {
            builder.setConfigureResult(getConfigResult(false, NODE_SUBSCR_NULL));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        if (!checkSubscriSensorProvidedByDelSensorInput(input)) {
            builder.setConfigureResult(getConfigResult(false, NODE_SUBSCR_SENSOR_NULL));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        for (int i = 0; i < input.getTelemetryNode().size(); i++) {
            for (int j = 0; j < input.getTelemetryNode().get(i).getTelemetryNodeSubscription().size(); j++) {
                dataProcessor.deleteNodeSubscriptionSensorFromDataStore(input.getTelemetryNode().get(i).getNodeId(),
                        input.getTelemetryNode().get(i).getTelemetryNodeSubscription().get(j).getSubscriptionName(),
                        input.getTelemetryNode().get(i).getTelemetryNodeSubscription().get(j)
                                .getTelemetryNodeSubscriptionSensor());
            }
        }
        builder.setConfigureResult(getConfigResult(true, ""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<DeleteNodeTelemetrySubscriptionDestinationOutput>> deleteNodeTelemetrySubscriptionDestination(DeleteNodeTelemetrySubscriptionDestinationInput input) {
        DeleteNodeTelemetrySubscriptionDestinationOutputBuilder builder = new DeleteNodeTelemetrySubscriptionDestinationOutputBuilder();
        if (null == input) {
            builder.setConfigureResult(getConfigResult(false, INPUT_NULL));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        if (null == input.getTelemetryNode() || input.getTelemetryNode().isEmpty()) {
            builder.setConfigureResult(getConfigResult(false, NODE_NULL));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        if (!checkSubscriProvidedByDelDesInput(input)) {
            builder.setConfigureResult(getConfigResult(false, NODE_SUBSCR_NULL));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        if (!checkSubscriDesProvidedByDelDesInput(input)) {
            builder.setConfigureResult(getConfigResult(false, NODE_SUBSCR_DES_NULL));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        for (int i = 0; i < input.getTelemetryNode().size(); i++) {
            for (int j = 0; j < input.getTelemetryNode().get(i).getTelemetryNodeSubscription().size(); j++) {
                dataProcessor.deleteNodeSubscriptionDestinationFromDataStore(input.getTelemetryNode().get(i).getNodeId(),
                        input.getTelemetryNode().get(i).getTelemetryNodeSubscription().get(j).getSubscriptionName(),
                        input.getTelemetryNode().get(i).getTelemetryNodeSubscription().get(j)
                                .getTelemetryNodeSubscriptionDestination());
            }
        }

        builder.setConfigureResult(getConfigResult(true, ""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    private boolean checkSensorGroupExistedInDataStore(List<TelemetrySensorGroup> sensorGroupList) {
        LOG.info("Get sensor group from data store");
        List<TelemetrySensorGroup> allSensorGroupList = dataProcessor.getSensorGroupFromDataStore(IidConstants.TELEMETRY_IID);

        if (null == allSensorGroupList || allSensorGroupList.isEmpty()) {
            return false;
        }

        for (TelemetrySensorGroup sensorGroup : sensorGroupList) {
            for (TelemetrySensorGroup allSensorGroup : allSensorGroupList) {
                if (sensorGroup.getTelemetrySensorGroupId().equals(allSensorGroup.getTelemetrySensorGroupId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkDesGroupExistedInDataStore(List<TelemetryDestinationGroup> destinationGroupList) {
        LOG.info("Get destination group from data store");
        List<TelemetryDestinationGroup> allDestinationGroupList = dataProcessor.getDestinationGroupFromDataStore(IidConstants.TELEMETRY_IID);

        if (null == allDestinationGroupList || allDestinationGroupList.isEmpty()) {
            return false;
        }

        for (TelemetryDestinationGroup destinationGroup : destinationGroupList) {
            for (TelemetryDestinationGroup allDestinationGroup : allDestinationGroupList) {
                if (destinationGroup.getDestinationGroupId().equals(allDestinationGroup.getDestinationGroupId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkSubscriSensorProvidedByConfigSubscriInput(List<TelemetryNode> nodeGroupList) {
        for (TelemetryNode telemetryNodeGroup : nodeGroupList) {
            for (TelemetrySubscription subscription : telemetryNodeGroup.getTelemetrySubscription()) {
                if (null == subscription.getTelemetrySensor() || subscription.getTelemetrySensor().isEmpty()) {
                    return false;
                }

                for (TelemetrySensor sensor : subscription.getTelemetrySensor()) {
                    if (!(checkSensorExit(sensor.getSensorGroupId()) || checkParamsInSensorExist(sensor))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean checkSubscriDesProvidedByConfigSubscriInput(List<TelemetryNode> nodeGroupList) {
        for (TelemetryNode telemetryNodeGroup : nodeGroupList) {
            for (TelemetrySubscription subscription : telemetryNodeGroup.getTelemetrySubscription()) {
                if (null == subscription.getTelemetryDestination() || subscription.getTelemetryDestination().isEmpty()) {
                    return false;
                }

                for (TelemetryDestination destination : subscription.getTelemetryDestination()) {
                    if (!checkDestinationExit(destination.getDestinationGroupId())) {
                        return false;
                    }
                }
            }
        }
        return true;
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

    private boolean checkSubscriProvidedByDelSensorInput(DeleteNodeTelemetrySubscriptionSensorInput input) {
        for (int i = 0; i < input.getTelemetryNode().size(); i++) {
            if (null == input.getTelemetryNode().get(i).getTelemetryNodeSubscription()
                    || input.getTelemetryNode().get(i).getTelemetryNodeSubscription().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean checkSubscriSensorProvidedByDelSensorInput(DeleteNodeTelemetrySubscriptionSensorInput input) {
        for (int i = 0; i < input.getTelemetryNode().size(); i++) {
            for (int j = 0; j < input.getTelemetryNode().get(i).getTelemetryNodeSubscription().size(); j++) {
                if (null == input.getTelemetryNode().get(i).getTelemetryNodeSubscription().get(j)
                        .getTelemetryNodeSubscriptionSensor() || input.getTelemetryNode().get(i)
                        .getTelemetryNodeSubscription().get(j).getTelemetryNodeSubscriptionSensor()
                        .isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkSubscriProvidedByDelDesInput(DeleteNodeTelemetrySubscriptionDestinationInput input) {
        for (int i = 0; i < input.getTelemetryNode().size(); i++) {
            if (null == input.getTelemetryNode().get(i).getTelemetryNodeSubscription()
                    || input.getTelemetryNode().get(i).getTelemetryNodeSubscription().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean checkSubscriDesProvidedByDelDesInput(DeleteNodeTelemetrySubscriptionDestinationInput input) {
        for (int i = 0; i < input.getTelemetryNode().size(); i++) {
            for (int j = 0; j < input.getTelemetryNode().get(i).getTelemetryNodeSubscription().size(); j++) {
                if (null == input.getTelemetryNode().get(i).getTelemetryNodeSubscription().get(j)
                        .getTelemetryNodeSubscriptionDestination() || input.getTelemetryNode().get(i)
                        .getTelemetryNodeSubscription().get(j).getTelemetryNodeSubscriptionDestination()
                        .isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

}
