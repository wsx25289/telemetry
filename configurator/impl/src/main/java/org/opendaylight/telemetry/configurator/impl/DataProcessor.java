/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.configurator.impl;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.sensor.paths.TelemetrySensorPaths;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.sensor.specification.TelemetrySensorGroup;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.TelemetrySystem;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.TelemetrySystemBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.*;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.DestinationGroups;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.DestinationGroupsBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.destination.groups.DestinationGroup;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.destination.groups.DestinationGroupBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.destination.groups.DestinationGroupKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.destination.groups.destination.group.Destinations;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.destination.groups.destination.group.DestinationsBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.destination.groups.destination.group.destinations.Destination;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.destination.groups.destination.group.destinations.DestinationBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.destination.groups.destination.group.destinations.DestinationKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.sensor.groups.SensorGroup;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.sensor.groups.SensorGroupBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.sensor.groups.SensorGroupKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.sensor.groups.sensor.group.SensorPaths;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.sensor.groups.sensor.group.SensorPathsBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.sensor.groups.sensor.group.sensor.paths.SensorPath;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.sensor.groups.sensor.group.sensor.paths.SensorPathBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.sensor.groups.sensor.group.sensor.paths.SensorPathKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.subscriptions.PersistentBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.subscriptions.persistent.Subscription;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.subscriptions.persistent.SubscriptionBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.subscriptions.persistent.SubscriptionKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.subscriptions.persistent.subscription.*;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.subscriptions.persistent.subscription.Config;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.subscriptions.persistent.subscription.sensor.profiles.SensorProfile;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.subscriptions.persistent.subscription.sensor.profiles.SensorProfileBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.subscriptions.persistent.subscription.sensor.profiles.SensorProfileKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.types.rev170824.*;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.types.inet.rev170824.Dscp;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.types.inet.rev170824.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.delete.node.telemetry.subscription.destination.input.telemetry.node.telemetry.node.subscription.TelemetryNodeSubscriptionDestination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.delete.node.telemetry.subscription.input.telemetry.node.TelemetryNodeSubscription;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.delete.node.telemetry.subscription.sensor.input.telemetry.node.telemetry.node.subscription.TelemetryNodeSubscriptionSensor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.delete.telemetry.destination.input.TelemetryDestination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.delete.telemetry.sensor.input.TelemetrySensor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.Telemetry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.destination.specification.TelemetryDestinationGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.destination.specification.telemetry.destination.group.DestinationProfile;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.node.subscription.TelemetryNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.TelemetrySubscription;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class DataProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(DataProcessor.class);



    private final DataBroker dataBroker;

    public DataProcessor(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public List<TelemetrySensorGroup> getSensorGroupFromDataStore(InstanceIdentifier<Telemetry> path) {
        final ReadTransaction readTransaction = dataBroker.newReadOnlyTransaction();
        Optional<Telemetry> telemetry = null;
        try {
            telemetry = readTransaction.read(LogicalDatastoreType.CONFIGURATION, path).checkedGet();
            if (telemetry.isPresent()) {
                LOG.info("Telemetry data from controller data store is not null");
                return telemetry.get().getTelemetrySensorGroup();
            }
        } catch (ReadFailedException e) {
            LOG.warn("Failed to read {} ", path, e);
        }
        LOG.info("Telemetry data from controller data store is null");
        return null;
    }

    public List<TelemetryDestinationGroup> getDestinationGroupFromDataStore(InstanceIdentifier<Telemetry> path) {
        final ReadTransaction readTransaction = dataBroker.newReadOnlyTransaction();
        Optional<Telemetry> telemetry = null;
        try {
            telemetry = readTransaction.read(LogicalDatastoreType.CONFIGURATION, path).checkedGet();
            if (telemetry.isPresent()) {
                LOG.info("Telemetry data from controller data store is not null");
                return telemetry.get().getTelemetryDestinationGroup();
            }
        } catch (ReadFailedException e) {
            LOG.warn("Failed to read {} ", path, e);
        }
        LOG.info("Telemetry data from controller data store is null");
        return null;
    }

    public List<TelemetryNode> getNodeSubscriptionFromDataStore(InstanceIdentifier<Telemetry> path) {
        final ReadTransaction readTransaction = dataBroker.newReadOnlyTransaction();
        Optional<Telemetry> telemetry = null;
        try {
            telemetry = readTransaction.read(LogicalDatastoreType.CONFIGURATION, path).checkedGet();
            if (telemetry.isPresent()) {
                LOG.info("Telemetry data from controller data store is not null");
                return telemetry.get().getTelemetryNode();
            }
        } catch (ReadFailedException e) {
            LOG.warn("Failed to read {} ", path, e);
        }
        LOG.info("Telemetry data from controller data store is null");
        return null;
    }

    public void addSensorGroupToDataStore(List<TelemetrySensorGroup> sensorGroupList) {
        for (TelemetrySensorGroup sensorGroup : sensorGroupList) {
            operateDataStore(ConfigurationType.ADD, sensorGroup, IidConstants.getSensorGroupPath(sensorGroup.getTelemetrySensorGroupId()));
        }
    }

    public void addDestinationGroupToDataStore(List<TelemetryDestinationGroup> destinationGroupList) {
        for (TelemetryDestinationGroup destinationGroup : destinationGroupList) {
            operateDataStore(ConfigurationType.ADD, destinationGroup, IidConstants.getDestinationGroupPath(destinationGroup.getDestinationGroupId()));
        }
    }

    public void addNodeSubscriptionToDataStore(List<TelemetryNode> nodeGroupList) {
        for (TelemetryNode nodeGroup : nodeGroupList) {
            operateDataStore(ConfigurationType.MODIFY, nodeGroup, IidConstants.getNodeGroupPath(nodeGroup.getNodeId()));
        }
    }

    private <T extends DataObject> CheckedFuture<Void, TransactionCommitFailedException> operateDataStore(
            ConfigurationType type, T data, InstanceIdentifier<T> path) {
        final WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
        switch (type) {
            case ADD:
                writeTransaction.put(LogicalDatastoreType.CONFIGURATION, path, data, true);
                break;
            case MODIFY:
                writeTransaction.merge(LogicalDatastoreType.CONFIGURATION, path, data, true);
                break;
            case DELETE:
                writeTransaction.delete(LogicalDatastoreType.CONFIGURATION, path);
                break;
            default:
                break;
        }
        final CheckedFuture<Void, TransactionCommitFailedException> submitResult = writeTransaction.submit();
        return submitResult;
    }

    public void deleteSensorGroupFromDataStore(List<TelemetrySensor> sensorList, List<TelemetrySensorGroup> sensorGroupList) {
        for (TelemetrySensor telemetrySensor : sensorList) {
            for (TelemetrySensorGroup telemetrySensorGroup : sensorGroupList) {
                if (telemetrySensor.getSensorGroupId().equals(telemetrySensorGroup.getTelemetrySensorGroupId())) {
                    operateDataStore(ConfigurationType.DELETE, null, IidConstants.getSensorGroupPath(telemetrySensorGroup.getTelemetrySensorGroupId()));
                }
            }
        }
    }

    public void deleteDestinationGroupFromDataStore(List<TelemetryDestination> destinationList,
                                                    List<TelemetryDestinationGroup> destinationGroupList) {
        for (TelemetryDestination telemetryDestination : destinationList) {
            for (TelemetryDestinationGroup telemetryDestinationGroup : destinationGroupList) {
                if (telemetryDestination.getDestinationGroupId().equals(telemetryDestinationGroup.getDestinationGroupId())) {
                    operateDataStore(ConfigurationType.DELETE, null, IidConstants.getDestinationGroupPath(telemetryDestinationGroup.getDestinationGroupId()));
                }
            }
        }
    }

    public void deleteNodeSubscriptionFromDataStore(String nodeId, List<TelemetryNodeSubscription> list) {
        for (int i = 0; i < list.size(); i++) {
                operateDataStore(ConfigurationType.DELETE, null, IidConstants.getSubscriptionPath(nodeId,
                        list.get(i).getSubscriptionName()));
        }
    }

    public void deleteNodeSubscriptionSensorFromDataStore(String nodeId, String name,
                                                          List<TelemetryNodeSubscriptionSensor> list) {
        for (int i = 0; i < list.size(); i++) {
            operateDataStore(ConfigurationType.DELETE, null, IidConstants.getSubscriptionSensorPath(
                    nodeId, name, list.get(i).getSensorGroupId()));
        }
    }

    public void deleteNodeSubscriptionDestinationFromDataStore(String nodeId, String name,
                                                               List<TelemetryNodeSubscriptionDestination> list) {
        for (int i = 0; i < list.size(); i++) {
            operateDataStore(ConfigurationType.DELETE, null, IidConstants.getSubscriptionDestinationPath(
                    nodeId, name, list.get(i).getDestinationGroupId()));
        }
    }

    public List<TelemetrySensorGroup> getSensorGroupDetailById(List<TelemetrySubscription> list) {
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120
                .telemetry.subscription.specification.telemetry.subscription.TelemetrySensor> sensorList = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.get(i).getTelemetrySensor().size(); j++) {
                if (!checkSensorExist(list.get(i).getTelemetrySensor().get(j), sensorList)) {
                    sensorList.add(list.get(i).getTelemetrySensor().get(j));
                }
            }
        }

        return sensorDetail(sensorList, getSensorGroupFromDataStore(IidConstants.TELEMETRY_IID));
    }

    private boolean checkSensorExist(org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang
                                             .configurator.rev171120.telemetry.subscription.specification
                                             .telemetry.subscription.TelemetrySensor sensor, List<org.opendaylight
            .yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription
            .specification.telemetry.subscription.TelemetrySensor> sensorList) {
        for (int i = 0; i < sensorList.size(); i++) {
            if (sensorList.get(i).getSensorGroupId().equals(sensor.getSensorGroupId())) {
                return true;
            }
        }
        return false;
    }

    private List<TelemetrySensorGroup> sensorDetail(List<org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry
            .params.xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.telemetry
            .subscription.TelemetrySensor> sensorList, List<TelemetrySensorGroup> sensorGroupList) {
        List<TelemetrySensorGroup> list = new ArrayList<>();
        for (int i = 0; i < sensorList.size(); i++) {
            for (int j = 0; j < sensorGroupList.size(); j++) {
                if (sensorList.get(i).getSensorGroupId().equals(sensorGroupList.get(j).getTelemetrySensorGroupId())) {
                    list.add(sensorGroupList.get(j));
                }
            }
        }
        return list;
    }

    public List<TelemetryDestinationGroup> getDestinationGroupDetailById(List<TelemetrySubscription> list) {
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120
                .telemetry.subscription.specification.telemetry.subscription.TelemetryDestination> destinationList
                = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.get(i).getTelemetryDestination().size(); j++) {
                if (!checkDestinationExist(list.get(i).getTelemetryDestination().get(j), destinationList)) {
                    destinationList.add(list.get(i).getTelemetryDestination().get(j));
                }
            }
        }

        return destinationDetail(destinationList, getDestinationGroupFromDataStore(IidConstants.TELEMETRY_IID));
    }

    private boolean checkDestinationExist(org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang
                                                  .configurator.rev171120.telemetry.subscription.specification
                                                  .telemetry.subscription.TelemetryDestination destination,
                                          List<org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns
                                                  .yang.configurator.rev171120.telemetry.subscription.specification
                                                  .telemetry.subscription.TelemetryDestination> destinationList) {
        for (int i = 0; i < destinationList.size(); i++) {
            if (destinationList.get(i).getDestinationGroupId().equals(destination.getDestinationGroupId())) {
                return true;
            }
        }
        return false;
    }

    private List<TelemetryDestinationGroup> destinationDetail(List<org.opendaylight.yang.gen.v1.urn.opendaylight
            .telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.telemetry
            .subscription.TelemetryDestination> destinationList, List<TelemetryDestinationGroup> destinationGroupList) {
        List<TelemetryDestinationGroup> list = new ArrayList();
        for (int i = 0; i < destinationList.size(); i++) {
            for (int j = 0; j < destinationGroupList.size(); j++) {
                if (destinationList.get(i).getDestinationGroupId().equals(destinationGroupList.get(j)
                        .getDestinationGroupId())) {
                    list.add(destinationGroupList.get(j));
                }
            }
        }
        return list;
    }

    public TelemetrySystem convertDataToSouth(List<TelemetrySensorGroup> sensorGroupList,
                                              List<TelemetryDestinationGroup> destinationGroupList,
                                              List<TelemetrySubscription> subscriptionList) {
        TelemetrySystemBuilder systemBuilder = new TelemetrySystemBuilder();
        systemBuilder.setSensorGroups(convertSensor(sensorGroupList));
        systemBuilder.setDestinationGroups(convertDestination(destinationGroupList));
        systemBuilder.setSubscriptions(convertSubscription(subscriptionList));
        return systemBuilder.build();
    }

    private SensorGroups convertSensor(List<TelemetrySensorGroup> list) {
        List<SensorGroup> sensorGroupList = new ArrayList<>();
        for (TelemetrySensorGroup telemetrySensorGroup : list) {
            SensorGroupBuilder builder = new SensorGroupBuilder();
            builder.setKey(new SensorGroupKey(telemetrySensorGroup.getTelemetrySensorGroupId()));
            builder.setSensorGroupId(telemetrySensorGroup.getTelemetrySensorGroupId());
            //builder.setConfig(new ConfigBuilder().setSensorGroupId(telemetrySensorGroup.getTelemetrySensorGroupId()));
            builder.setSensorPaths(convertSensorPaths(telemetrySensorGroup.getTelemetrySensorPaths()));
            sensorGroupList.add(builder.build());
        }
        SensorGroupsBuilder sensorGroupsBuilder = new SensorGroupsBuilder();
        sensorGroupsBuilder.setSensorGroup(sensorGroupList);
        return sensorGroupsBuilder.build();
    }

    private SensorPaths convertSensorPaths(List<TelemetrySensorPaths> list) {
        List<SensorPath> sensorPathList = new ArrayList<>();
        for (TelemetrySensorPaths paths : list) {
            SensorPathBuilder sensorPathBuilder = new SensorPathBuilder();
            sensorPathBuilder.setKey(new SensorPathKey(paths.getTelemetrySensorPath()));
            sensorPathBuilder.setPath(paths.getTelemetrySensorPath());
//            sensorPathBuilder.setConfig(new ConfigBuilder().setPath(paths.getTelemetrySensorPath())
//                    .setExcludeFilter(paths.getSensorExcludeFilter()).build());
            sensorPathList.add(sensorPathBuilder.build());
        }
        SensorPathsBuilder builder = new SensorPathsBuilder();
        builder.setSensorPath(sensorPathList);
        return builder.build();
    }

    private DestinationGroups convertDestination(List<TelemetryDestinationGroup> list) {
        List<DestinationGroup> destinationGroupList = new ArrayList<>();
        for (TelemetryDestinationGroup telemetryDestinationGroup : list) {
            DestinationGroupBuilder builder = new DestinationGroupBuilder();
            builder.setKey(new DestinationGroupKey(telemetryDestinationGroup.getDestinationGroupId()));
            builder.setGroupId(telemetryDestinationGroup.getDestinationGroupId());
            //builder.setConfig();
            builder.setDestinations(convertDestinations(telemetryDestinationGroup.getDestinationProfile()));
            destinationGroupList.add(builder.build());
        }
        DestinationGroupsBuilder destinationGroupsBuilder = new DestinationGroupsBuilder();
        destinationGroupsBuilder.setDestinationGroup(destinationGroupList);
        return destinationGroupsBuilder.build();
    }

    private Destinations convertDestinations(List<DestinationProfile> list) {
        List<Destination> destinationList = new ArrayList<>();
        for (DestinationProfile destinationProfile : list) {
            DestinationBuilder destinationBuilder = new DestinationBuilder();
            destinationBuilder.setKey(new DestinationKey(destinationProfile.getDestinationAddress(), destinationProfile.getDestinationPort()));
            destinationBuilder.setDestinationAddress(destinationProfile.getDestinationAddress());
            destinationBuilder.setDestinationPort(destinationProfile.getDestinationPort());
            destinationList.add(destinationBuilder.build());
        }
        DestinationsBuilder builder = new DestinationsBuilder();
        builder.setDestination(destinationList);
        return builder.build();
    }

    private Subscriptions convertSubscription(List<TelemetrySubscription> list) {

        List<Subscription> subscriptionList = new ArrayList<>();
        for (TelemetrySubscription telemetrySubscription : list) {
            SubscriptionBuilder subscriptionBuilder = new SubscriptionBuilder();
            subscriptionBuilder.setKey(new SubscriptionKey(telemetrySubscription.getSubscriptionName()));
            subscriptionBuilder.setSubscriptionName(telemetrySubscription.getSubscriptionName());
            subscriptionBuilder.setConfig(convertLeafParamsOfSubscription(telemetrySubscription.getSubscriptionName(),
                    telemetrySubscription.getLocalSourceAddress(), telemetrySubscription.getOriginatedQosMarking(),
                    telemetrySubscription.getProtocolType(), telemetrySubscription.getEncodingType()));
            subscriptionBuilder.setSensorProfiles(convertSensorProfiles(telemetrySubscription.getTelemetrySensor()));
            subscriptionBuilder.setDestinationGroups(convertDestinationGroups(telemetrySubscription.getTelemetryDestination()));
            subscriptionList.add(subscriptionBuilder.build());
        }
        PersistentBuilder persistentBuilder = new PersistentBuilder();
        persistentBuilder.setSubscription(subscriptionList);
        SubscriptionsBuilder subscriptionsBuilder = new SubscriptionsBuilder();
        subscriptionsBuilder.setPersistent(persistentBuilder.build());
        return subscriptionsBuilder.build();
    }

    private Config convertLeafParamsOfSubscription(String name, Ipv4Address address, Dscp qos,
                                                   String protocolType, String encodingtype) {
        org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system
                .subscriptions.persistent.subscription.ConfigBuilder builder = new org.opendaylight.yang.gen.v1
                .http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.subscriptions
                .persistent.subscription.ConfigBuilder();
        builder.setSubscriptionName(name);
        builder.setLocalSourceAddress(address);
        if (null == qos) {
            builder.setOriginatedQosMarking(null);
        }
        builder.setOriginatedQosMarking(qos);

        if (null == protocolType) {
            builder.setProtocol(STREAMGRPC.class);
        } else {
            if (protocolType.equals("STREAM_SSH")) {
                builder.setProtocol(STREAMSSH.class);
            } else if (protocolType.equals("STREAM_WEBSOCKET_RPC")) {
                builder.setProtocol(STREAMWEBSOCKETRPC.class);
            } else if (protocolType.equals("STREAM_JSON_RPC")) {
                builder.setProtocol(STREAMJSONRPC.class);
            } else {
                builder.setProtocol(STREAMTHRIFTRPC.class);
            }
        }

        if (null == encodingtype) {
            builder.setEncoding(ENCPROTO3.class);
        } else {
            if (encodingtype.equals("ENC_XML")) {
                builder.setEncoding(ENCXML.class);
            } else {
                builder.setEncoding(ENCJSONIETF.class);
            }
        }

        return builder.build();
    }

    private SensorProfiles convertSensorProfiles(List<org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params
            .xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.telemetry.subscription
            .TelemetrySensor> list) {
        List<SensorProfile> sensorProfileList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            SensorProfileBuilder sensorProfileBuilder = new SensorProfileBuilder();
            sensorProfileBuilder.setKey(new SensorProfileKey(list.get(i).getSensorGroupId()));
            sensorProfileBuilder.setSensorGroup(list.get(i).getSensorGroupId());
            sensorProfileBuilder.setConfig(convertSensorProfilesConfig(list.get(i).getSensorGroupId(),
                    list.get(i).getSampleInterval(), list.get(i).getHeartbeatInterval(),
                    list.get(i).isSuppressRedundant()));
            sensorProfileList.add(sensorProfileBuilder.build());
        }
        SensorProfilesBuilder builder = new SensorProfilesBuilder();
        builder.setSensorProfile(sensorProfileList);
        return builder.build();
    }

    private org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system
            .subscriptions.persistent.subscription.sensor.profiles.sensor.profile.Config convertSensorProfilesConfig(
            String id, BigInteger sam, BigInteger hea, Boolean sup) {
        org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824
                .telemetry.top.telemetry.system.subscriptions.persistent.subscription.sensor.profiles.sensor.profile
                .ConfigBuilder builder = new org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824
                .telemetry.top.telemetry.system.subscriptions.persistent.subscription.sensor.profiles.sensor.profile
                .ConfigBuilder();
        if (null == id) {
            builder.setSensorGroup(null);
        } else {
            builder.setSensorGroup(id);
        }

        if (null == hea) {
            builder.setHeartbeatInterval(null);
        } else {
            builder.setHeartbeatInterval(hea);
        }

        if (null == sup) {
            builder.setSuppressRedundant(null);
        } else {
            builder.setSuppressRedundant(sup);
        }

        builder.setSampleInterval(sam);

        return builder.build();
    }

    private org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system
            .subscriptions.persistent.subscription.DestinationGroups convertDestinationGroups(List<org.opendaylight
            .yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription
            .specification.telemetry.subscription.TelemetryDestination> list) {
        List<org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system
                .subscriptions.persistent.subscription.destination.groups.DestinationGroup> destinationGroupList =
                new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system
                    .subscriptions.persistent.subscription.destination.groups.DestinationGroupBuilder
                    destinationGroupBuilder = new org.opendaylight.yang.gen.v1.http.openconfig.net.yang
                    .telemetry.rev170824.telemetry.top.telemetry.system.subscriptions.persistent
                    .subscription.destination.groups.DestinationGroupBuilder();
            destinationGroupBuilder.setKey(new org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry
                    .rev170824.telemetry.top.telemetry.system.subscriptions.persistent.subscription.destination
                    .groups.DestinationGroupKey(list.get(i).getDestinationGroupId()));
            destinationGroupBuilder.setGroupId(list.get(i).getDestinationGroupId());
            //destinationGroupBuilder.setConfig();
            destinationGroupList.add(destinationGroupBuilder.build());
        }
        org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system
                .subscriptions.persistent.subscription.DestinationGroupsBuilder builder = new org.opendaylight
                .yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system
                .subscriptions.persistent.subscription.DestinationGroupsBuilder();
        builder.setDestinationGroup(destinationGroupList);
        return builder.build();
    }
}
