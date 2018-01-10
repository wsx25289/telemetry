/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.core.impl;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.InsufficientCapacityException;
import com.lmax.disruptor.PhasedBackoffWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.opendaylight.telemetry.core.api.TelemetryNotification;
import org.opendaylight.telemetry.core.api.TelemetryPacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TelemetryNotificationImpl implements TelemetryNotification {

    private static final Logger LOG = LoggerFactory.getLogger(TelemetryNotificationImpl.class);

    private ExecutorService executor;
    private Disruptor<TelemetryNotificationEvent> disruptor;
    private static final int BUFFER_SIZE = 512;
    private static final String TELEMETRY_DATA = "TD";
    private static final WaitStrategy DEFAULT_STRATEGY = PhasedBackoffWaitStrategy
            .withLock(1L, 30L, TimeUnit.MILLISECONDS);
    private static final EventHandler<TelemetryNotificationEvent> DISPATCH_NOTIFICATIONS =
            (event, sequence, endOfBatch) -> event.deliverNotification();
    private volatile Multimap<String, TelemetryPacketHandler> listeners = ArrayListMultimap.create();

    public TelemetryNotificationImpl() {
        initDisruptor();
    }

    @Override
    public void subscribe(TelemetryPacketHandler handler) {
        registerNotificationListener(handler);
    }

    @Override
    public <T> void publish(T data) {
        //initDisruptor();
        if (null == data) {
            return;
        }

        offerNotification(data);
    }

    private <T> void publish(final long seq, T data, Collection<TelemetryPacketHandler> subscribers) {
        TelemetryNotificationEvent event = disruptor.get(seq);
        event.initialize(data, subscribers);
        disruptor.getRingBuffer().publish(seq);
    }

    private void initDisruptor() {
        LOG.info("Disruptor init");
        executor = Executors.newCachedThreadPool();
        disruptor = new Disruptor<>(TelemetryNotificationEvent.FACTORY, BUFFER_SIZE, executor,
                ProducerType.MULTI, DEFAULT_STRATEGY);
        disruptor.handleEventsWith(DISPATCH_NOTIFICATIONS);
        disruptor.start();
    }

    private <T> void offerNotification(T data) {
        final Collection<TelemetryPacketHandler> subscribers = listeners.get(TELEMETRY_DATA);
        if (subscribers.isEmpty()) {
            LOG.info("No subscriber");
            return;
        }

        tryPublish(data, subscribers);
    }

    private <T> void tryPublish(T data, Collection<TelemetryPacketHandler> subscribers) {
        final long seq;
        try {
            seq = disruptor.getRingBuffer().tryNext();
        } catch (final InsufficientCapacityException e) {
            e.getCause();
            return;
        }

        publish(seq, data, subscribers);
    }

    private void registerNotificationListener(TelemetryPacketHandler handler) {
        listeners.put(TELEMETRY_DATA, handler);
    }

}
