/*
 * Copyright (c) 2013-2015 GraphAware
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

import java.util.Map;

/**
 * Statistics about the recommendation-computing process. Implementations must be thread-safe.
 */
public interface Statistics {

    String TOTAL_TIME = "total time";

    String ELAPSED_TIME = "elapsed time";
    String CANDIDATE_ITEMS = "candidate items";
    String BLACKLISTED_ITEMS = "blacklisted items";
    String FILTERED_ITEMS = "filtered items";
    String TOTAL_ITEMS = "total items";

    /**
     * Start timing a task.
     *
     * @param task name of the task, must not be <code>null</code> or blank. Typically name of a {@link com.graphaware.reco.generic.engine.RecommendationEngine}.
     */
    void startTiming(String task);

    /**
     * Stop timing a task and record the time it took in statistics.
     *
     * @param task name of the task, must not be <code>null</code> or blank. Typically name of a {@link com.graphaware.reco.generic.engine.RecommendationEngine}.
     */
    void stopTiming(String task);

    /**
     * Return the number of milliseconds the task has been running so far.
     * @param task name of the task, must not be <code>null</code> or blank. Typically name of a {@link com.graphaware.reco.generic.engine.RecommendationEngine}.
     * @return number of milliseconds the task has been running so far.
     */
    long getTime(String task);

    /**
     * Collect a generic statistic.
     *
     * @param task  name of the task being measured, must not be <code>null</code> or blank. Typically name of a {@link com.graphaware.reco.generic.engine.RecommendationEngine}.
     * @param name  name of the measure / statistic, must not be <code>null</code> or blank.
     * @param value value, must not be <code>null</code>.
     */
    void addStatistic(String task, String name, Object value);

    /**
     * Increment an integer statistic by 1.
     *
     * @param task name of the task being measured, must not be <code>null</code> or blank. Typically name of a {@link com.graphaware.reco.generic.engine.RecommendationEngine}.
     * @param name name of the measure / statistic, must not be <code>null</code> or blank.
     */
    void incrementStatistic(String task, String name);

    /**
     * Get collected statistics.
     *
     * @return stats. <Task Name, <Measure Name, Measure Value>>
     */
    Map<String, ? extends Map<String, Object>> get();
}
