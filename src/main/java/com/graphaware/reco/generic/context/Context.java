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

package com.graphaware.reco.generic.context;

import com.graphaware.reco.generic.config.Config;
import com.graphaware.reco.generic.stats.Statistics;

/**
 * Context holding information about the recommendation-computing process. All methods of implementing classes can be
 * called by multiple threads simultaneously, thus must be thread-safe.
 */
public interface Context<OUT, IN> {

    /**
     * Get config.
     *
     * @return configuration for the recommendation-computing process. Never <code>null</code>.
     */
    Config config();

    /**
     * @return for which recommendations are being computed. Must not be <code>null</code>.
     */
    IN input();

    /**
     * Get config in a type-safe manner.
     *
     * @param clazz of the config.
     * @return configuration for the recommendation-computing process. Never <code>null</code>.
     * @throws IllegalArgumentException if the config isn't of the specified type.
     */
    <C extends Config> C config(Class<C> clazz);

    /**
     * @return <code>true</code> if there's still time left for the computation, i.e. if less time has elapsed so far
     * than returned by {@link Config#maxTime()}.
     */
    boolean timeLeft();

    /**
     * Check whether a produced recommendation is allowed for the given input in the current context.
     *
     * @param recommendation produced. Must not be <code>null</code>.
     * @param task           name of the task that is asking the "allow?" question. Must not be <code>null</code>.
     *                       Used for statistics and logging.
     * @return true if the recommendation is allowed for the given input.
     */
    boolean allow(OUT recommendation, String task);

    /**
     * Disallow the given recommendation. Intended for {@link com.graphaware.reco.generic.engine.RecommendationEngine}s
     * to prevent other engines that follow from discovering the same recommendation.
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
