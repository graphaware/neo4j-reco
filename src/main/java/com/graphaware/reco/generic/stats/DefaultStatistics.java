/*
 * Copyright (c) 2013-2016 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.reco.generic.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.util.Assert.hasLength;

/**
 * Default production implementation of {@link com.graphaware.reco.generic.stats.Statistics}.
 * <p/>
 * This class is thread-safe.
 *
 * @param <IN> type of the input into the recommendation-computing process.
 */
public class DefaultStatistics<IN> implements Statistics {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final Logger LOG = LoggerFactory.getLogger(DefaultStatistics.class);

    private final ConcurrentMap<String, ConcurrentMap<String, Object>> stats = new ConcurrentHashMap<>();
    private final TaskTimer timer = new DefaultTaskTimer();

    private final IN input;

    /**
     * Create a new object encapsulating statistics.
     *
     * @param input into the recommendation-computing process, for which statistics are being collected.
     */
    public DefaultStatistics(IN input) {
        this.input = input;

        startTiming(TOTAL_TIME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startTiming(String task) {
        hasLength(task);

        timer.startTiming(task);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopTiming(String task) {
        hasLength(task);

        addStatistic(task, ELAPSED_TIME, timer.getTime(task));
    }

    @Override
    public long getTime(String task) {
        hasLength(task);

        return timer.getTime(task);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addStatistic(String task, String name, Object value) {
        if (getStatistics(task).putIfAbsent(name, value) != null) {
            LOG.warn("Could not add statistic " + name + " for task " + task + ". There's already another value.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void incrementStatistic(String task, String name) {
        ConcurrentMap<String, Object> taskStats = getStatistics(task);

        AtomicInteger count = (AtomicInteger) taskStats.get(name);
        if (count == null) {
            taskStats.putIfAbsent(name, new AtomicInteger());
            count = (AtomicInteger) taskStats.get(name);
        }

        count.incrementAndGet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, ? extends Map<String, Object>> get() {
        return stats;
    }

    private ConcurrentMap<String, Object> getStatistics(String task) {
        ConcurrentMap<String, Object> taskStats = stats.get(task);

        if (taskStats == null) {
            stats.putIfAbsent(task, new ConcurrentHashMap<String, Object>());
            taskStats = stats.get(task);
        }
        return taskStats;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("=== Statistics for ").append(input).append(" ===").append(LINE_SEPARATOR);
        for (String task : stats.keySet()) {
            stringBuilder.append("=== ").append(task).append(" ===").append(LINE_SEPARATOR);
            for (Map.Entry<String, Object> entry : stats.get(task).entrySet()) {
                stringBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append(LINE_SEPARATOR);
            }
        }
        return stringBuilder.toString();
    }
}
