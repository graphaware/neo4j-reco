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
import com.graphaware.reco.generic.result.Recommendations;
import com.graphaware.reco.generic.result.PartialScore;
import com.graphaware.reco.generic.transform.NoTransformation;
import com.graphaware.reco.generic.transform.ScoreTransformer;

import java.util.Map;

/**
 * Base class for {@link com.graphaware.reco.generic.engine.RecommendationEngine}s that compute recommendations using
 * a single criteria, thus producing one type of recommendation score. Intended as a base class for implementations
 * that are delegated to by {@link com.graphaware.reco.generic.engine.DelegatingRecommendationEngine}.
 * <p/>
 * There is an option to provide a {@link com.graphaware.reco.generic.transform.ScoreTransformer} by overriding
 * {@link #scoreTransformer()}, which is used to transform all the produced scores.
 */
public abstract class SingleScoreRecommendationEngine<OUT, IN> extends BaseRecommendationEngine<OUT, IN> {

    private final ScoreTransformer transformer;

    /**
     * Construct a recommendation engine.
     */
    protected SingleScoreRecommendationEngine() {
        this.transformer = scoreTransformer();
    }

    /**
     * Get a score transformer used to transform all scores produced by this engine. Intended to be overridden.
     *
     * @return transformer, {@link com.graphaware.reco.generic.transform.NoTransformation} by default.
     */
    protected ScoreTransformer scoreTransformer() {
        return NoTransformation.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Recommendations<OUT> doRecommend(IN input, Context<OUT, IN> context) {
        Recommendations<OUT> result = new Recommendations<>();

        for (Map.Entry<OUT, PartialScore> entry : doRecommendSingle(input, context).entrySet()) {
            if (context.allow(entry.getKey(), input, name())) {
                result.add(entry.getKey(), name(), transformer.transform(entry.getKey(), entry.getValue(), context));
            }
        }

        return result;
    }

    /**
     * Perform the computation of recommendations. Recommendations produced by this method have an associated {@link com.graphaware.reco.generic.result.PartialScore},
     * which is later transformed by the provided {@link com.graphaware.reco.generic.transform.ScoreTransformer}.
     * <p/>
     * Context is provided for information, but its {@link com.graphaware.reco.generic.context.Context#allow(Object, Object, String)}
     * method does not have to be used. I.e., implementations of this method should produce raw recommendations, expressing
     * core business logic of coming up with these recommendations, ignoring blacklists, filtering, etc, which is applied
     * by this class ({@link com.graphaware.reco.generic.engine.SingleScoreRecommendationEngine}).
     *
     * @param input   to the recommendation process.
     * @param context of the current computation.
     * @return a map of recommended items and their scores.
     */
    protected abstract Map<OUT, PartialScore> doRecommendSingle(IN input, Context<OUT, IN> context);

    /**
     * A convenience method for subclasses for adding concrete recommendations to the result.
     *
     * @param result         to add to.
     * @param recommendation to add.
     * @param partialScore   recommendation's partial score.
     */
    protected final void addToResult(Map<OUT, PartialScore> result, OUT recommendation, PartialScore partialScore) {
        if (!result.containsKey(recommendation)) {
            result.put(recommendation, new PartialScore());
        }

        result.get(recommendation).add(partialScore);
    }
}
