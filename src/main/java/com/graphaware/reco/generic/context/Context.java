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

package com.graphaware.reco.generic.context;

import com.graphaware.reco.generic.config.Config;
import com.graphaware.reco.generic.stats.Statistics;

/**
 * Context holding information about the recommendation-computing process.
 */
public interface Context<OUT, IN> {

    /**
     * Get config.
     *
     * @return configuration for the recommendation-computing process.
     */
    Config config();

    /**
     * Get config in a type-safe manner.
     *
     * @param clazz of the config.
     * @return configuration for the recommendation-computing process.
     * @throws IllegalArgumentException if the config isn't of the specified type.
     */
    <C extends Config> C config(Class<C> clazz);

    /**
     * @return <code>true</code> iff there's still time to compute more.
     */
    boolean hasEnoughTime();

    /**
     * Check whether a produced recommendation is allowed for the given input in the current context. Can be called by
     * multiple threads simultaneously, must be thus thread-safe.
     *
     * @param recommendation produced. Must not be <code>null</code>.
     * @param input          for which the recommendation was produced. Must not be <code>null</code>.
     * @param task           name of the task that is asking the "allow?" question. Must not be <code>null</code>.
     *                       Used for statistics and logging.
     * @return true iff the recommendation is allowed for the given input.
     */
    boolean allow(OUT recommendation, IN input, String task);

    /**
     * Disallow the given recommendation. Intended for {@link com.graphaware.reco.generic.engine.RecommendationEngine}s
     * to prevent other engines the follow from discovering the same recommendation.
     *
     * @param recommendation to disallow.
     */
    void disallow(OUT recommendation);

    /**
     * Get the statistics of the computation process that this context is for.
     *
     * @return stats.
     */
    Statistics statistics();
}
