/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.configurator.impl;

import org.opendaylight.controller.md.sal.binding.api.*;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.sensor.specification.TelemetrySensorGroup;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.TelemetrySystem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.destination.specification.TelemetryDestinationGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.node.subscription.TelemetryNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

public class NodeSubscriptionChangeListener implements DataTreeChangeListener<TelemetryNode> {
    private static final Logger LOG = LoggerFactory.getLogger(NodeSubscriptionChangeListener.class);

    private DataBroker dataBroker;
    private ConfigurationWriter configurationWriter;
    private DataProcessor dataProcessor;

    public NodeSubscriptionChangeListener(DataBroker dataBroker, ConfigurationWriter configurationWriter, DataProcessor dataProcessor) {
        this.dataBroker = dataBroker;
        this.configurationWriter = configurationWriter;
        this.dataProcessor = dataProcessor;
    }

    public void init() {
        dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<TelemetryNode>(LogicalDatastoreType.CONFIGURATION, IidConstants.TELEMETRY_IID.child(TelemetryNode.class)), this);
        LOG.info("Begin to Listen the node subscription changes");
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<TelemetryNode>> changes) {
        for (DataTreeModification<TelemetryNode> change : changes) {
            DataObjectModification<TelemetryNode> rootNode = change.getRootNode();
            switch (rootNode.getModificationType()) {
                case WRITE:
                    LOG.info("The subscription of {} was created or replaced", rootNode.getDataAfter().getNodeId());
                    List<TelemetrySensorGroup> sensorGroupList = dataProcessor.getSensorGroupDetailById(
                            rootNode.getDataAfter().getTelemetrySubscription());
                    List<TelemetryDestinationGroup> destinationGroupList = dataProcessor.getDestinationGroupDetailById(
                            rootNode.getDataAfter().getTelemetrySubscription());
                    TelemetrySystem telemetrySystem = dataProcessor.convertDataToSouth(sensorGroupList,
                            destinationGroupList, rootNode.getDataAfter().getTelemetrySubscription());
                    configurationWriter.writeTelemetryConfig(ConfigurationType.ADD, rootNode.getDataAfter().getNodeId(), telemetrySystem);
                    break;
                case SUBTREE_MODIFIED:
                    LOG.info("process modify procedure");
                    break;
                case DELETE:
                    LOG.info("The all subscription of {} was deleted", rootNode.getDataBefore().getNodeId());
                    configurationWriter.writeTelemetryConfig(ConfigurationType.DELETE, rootNode.getDataBefore().getNodeId(), null);
                    break;
                default:
                    break;
            }
        }
    }

}
