/*
 * Copyright (c) 2014 GraphAware
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

package com.graphaware.reco.generic.engine;

import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.policy.ParticipationPolicy;
import com.graphaware.reco.generic.result.Recommendations;

import static com.graphaware.common.util.PropertyContainerUtils.getInt;

/**
 * A {@link RecommendationEngine} that reads pre-computed recommendations and their scores from an external source.
 * <p/>
 * {@link com.graphaware.reco.generic.context.Context} blacklists and filters are still used to filter out recommendations
 * for which the situation has changed since they were pre-computed.
 * <p/>
 * Once a pre-computed recommendation has been read, it is blacklisted so that other recommendation engines do not
 * discover it again.
 *
 * @param <SOURCE> type of the precomputed recommendation source. Could be an object from cache, a graph relationship, etc.
 */
public abstract class PrecomputedEngine<OUT, IN, SOURCE> implements RecommendationEngine<OUT, IN> {

    /**
     * {@inheritDoc}
     *
     * @return {@link com.graphaware.reco.generic.policy.ParticipationPolicy#ALWAYS} by default.
     */
    @Override
    public ParticipationPolicy<OUT, IN> participationPolicy(Context<OUT, IN> context) {
        //noinspection unchecked
        return ParticipationPolicy.ALWAYS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Recommendations<OUT> recommend(IN input, Context<OUT, IN> context) {
        Recommendations<OUT> result = new Recommendations<>();

        for (SOURCE source : produce(input)) {
            OUT recommendation = extract(source);
            if (context.allow(recommendation, input)) {
                addToResult(result, recommendation, source);
                context.disallow(recommendation);
            }
        }

        return result;
    }

    /**
     * Produce pre-computed recommendation sources.
     *
     * @param input for which to read pre-computed recommendations.
     * @return sources.
     */
    protected abstract Iterable<SOURCE> produce(IN input);

    /**
     * Extract a recommendation out of a source.
     *
     * @param source to extract a recommendation from.
     * @return recommendation.
     */
    protected abstract OUT extract(SOURCE source);

    /**
     * Add a recommendation to the overall recommendations.
     *
     * @param recommendations to add to.
     * @param recommendation  to add.
     * @param source          source of the recommendation.
     */
    protected abstract void addToResult(Recommendations<OUT> recommendations, OUT recommendation, SOURCE source);
}
