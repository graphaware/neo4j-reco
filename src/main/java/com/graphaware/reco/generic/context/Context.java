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

/**
 * Context holding information about the recommendation-computing process. Contexts should be package-protected and
 * constructed using their respective public {@link com.graphaware.reco.generic.context.ContextFactory} implementations.
 */
public interface Context<OUT, IN> {

    /**
     * @return {@link Mode} in which recommendations are being computed.
     */
    Mode mode();

    /**
     * @return desired maximum number of produced recommendations.
     */
    int limit();

    /**
     * Initialize the context before computing recommendation for the given input. Must be called exactly once by the
     * corresponding {@link com.graphaware.reco.generic.context.ContextFactory}, thus doesn't need to be thread-safe.
     *
     * @param input for which to compute recommendations.
     */
    void initialize(IN input);

    /**
     * Check whether a produced recommendation is allowed for the given input in the current context. Can be called by
     * multiple threads simultaneously, must be thus thread-safe.
     *
     * @param recommendation produced. Must not be <code>null</code>.
     * @param input          for which the recommendation was produced. Must not be <code>null</code>.
     * @return true iff the recommendation is allowed for the given input.
     */
    boolean allow(OUT recommendation, IN input);

    /**
     * Disallow the given recommendation. Intended for {@link com.graphaware.reco.generic.engine.RecommendationEngine}s
     * to prevent other engines the follow from discovering the same recommendation.
     *
     * @param recommendation to disallow.
     */
    void disallow(OUT recommendation);
}
