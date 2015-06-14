/*
 * Copyright (c) 2015 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.reco.generic.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.springframework.util.Assert.*;


/**
 * Default production implementation of {@link com.graphaware.reco.generic.stats.TaskTimer}.
 */
public class DefaultTaskTimer implements TaskTimer {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultTaskTimer.class);

    private final ConcurrentMap<String, Long> startTimes = new ConcurrentHashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void startTiming(String task) {
        hasLength(task);

        if (startTimes.putIfAbsent(task, System.currentTimeMillis()) != null) {
            LOG.warn("Task " + task + " already timing! Not restarting timer...");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getTime(String task) {
        hasLength(task);

        Long start = startTimes.get(task);

        if (start == null) {
            LOG.warn("Could not timing stop non-existing task " + task);
            return -1;
        }

        return System.currentTimeMillis() - start;
    }
}
