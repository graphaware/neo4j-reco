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

package com.graphaware.reco.generic.engine;

import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.policy.ParticipationPolicy;
import com.graphaware.reco.generic.result.Recommendations;
import com.graphaware.reco.generic.stats.Statistics;
import com.graphaware.reco.generic.transform.NoTransformation;
import com.graphaware.reco.generic.transform.ScoreTransformer;

import java.util.Map;

/**
 * Abstract base class for {@link com.graphaware.reco.generic.engine.RecommendationEngine} implementations. Takes care
 * of collecting statistics.
 *
 * @param <OUT> type of the recommendations produced.
 * @param <IN>  type of the item recommendations are for / based on.
 */
public abstract class BaseRecommendationEngine<OUT, IN> implements RecommendationEngine<OUT, IN> {

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
    public final Recommendations<OUT> recommend(IN input, Context<OUT, IN> context) {
        context.statistics().startTiming(name());

        Recommendations<OUT> result = doRecommend(input, context);

        context.statistics().stopTiming(name());
        context.statistics().addStatistic(name(), Statistics.TOTAL_ITEMS, result.size());

        return result;
    }

    /**
     * Produce recommendations.
     *
     * @param input   input to the recommendation engine. Typically the person or item recommendations are being
     *                computed for.
     * @param context additional information about the recommendation process useful to the engine.
     * @return recommendations.
     */
    protected abstract Recommendations<OUT> doRecommend(IN input, Context<OUT, IN> context);
}
