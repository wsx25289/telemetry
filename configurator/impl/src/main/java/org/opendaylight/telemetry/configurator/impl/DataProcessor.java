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
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.sensor.specification.TelemetrySensorGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.delete.telemetry.destination.input.TelemetryDestination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.delete.telemetry.sensor.input.TelemetrySensor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.delete.telemetry.subscription.input.TelemetryNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.Telemetry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.destination.specification.TelemetryDestinationGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.node.subscription.TelemetryNodeGroup;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public List<TelemetryNodeGroup> getNodeSubscriptionFromDataStore(InstanceIdentifier<Telemetry> path) {
        final ReadTransaction readTransaction = dataBroker.newReadOnlyTransaction();
        Optional<Telemetry> telemetry = null;
        try {
            telemetry = readTransaction.read(LogicalDatastoreType.CONFIGURATION, path).checkedGet();
            if (telemetry.isPresent()) {
                LOG.info("Telemetry data from controller data store is not null");
                return telemetry.get().getTelemetryNodeGroup();
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

    public void addNodeSubscriptionToDataStore(List<TelemetryNodeGroup> nodeGroupList) {
        for (TelemetryNodeGroup nodeGroup : nodeGroupList) {
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

    //public boolean checkSensorGroupExist(List<TelemetrySensor> sensorList, List<TelemetrySensorGroup> sensorGroupList) {
    //    for (TelemetrySensor telemetrySensor : sensorList) {
    //        for (TelemetrySensorGroup telemetrySensorGroup : sensorGroupList) {
    //            if (telemetrySensor.getSensorGroupId().equals(telemetrySensorGroup.getTelemetrySensorGroupId())) {
    //                return true;
    //            }
    //        }
    //    }
    //    return false;
    //}

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

    public void deleteNodeSubscriptionFromDataStore(List<TelemetryNode> nodeList, List<TelemetryNodeGroup> nodeGroupList) {
        for (TelemetryNode telemetryNode : nodeList) {
            for (TelemetryNodeGroup telemetryNodeGroup : nodeGroupList) {
                if (telemetryNode.getNodeId().equals(telemetryNodeGroup.getNodeId())) {
                    operateDataStore(ConfigurationType.DELETE, null, IidConstants.getNodeGroupPath(telemetryNodeGroup.getNodeId()));
                }
            }
        }
    }

}
