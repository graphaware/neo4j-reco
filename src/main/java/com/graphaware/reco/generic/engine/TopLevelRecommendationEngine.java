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

import com.graphaware.common.util.Pair;
import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.context.ContextFactory;
import com.graphaware.reco.generic.context.Mode;
import com.graphaware.reco.generic.result.Recommendations;
import com.graphaware.reco.generic.result.Score;

import java.util.List;

/**
 * A {@link com.graphaware.reco.generic.engine.DelegatingRecommendationEngine} intended to be used as the single top-level
 * {@link com.graphaware.reco.generic.engine.RecommendationEngine}. It accepts a single {@link com.graphaware.reco.generic.context.ContextFactory}
 * at construction-time, which it then uses to produce {@link com.graphaware.reco.generic.context.Context}s.
 */
public class TopLevelRecommendationEngine<OUT, IN, C extends Context<OUT, IN>> extends DelegatingRecommendationEngine<OUT, IN, C> {

    private final ContextFactory<OUT, IN, C> contextFactory;

    /**
     * Create a new engine.
     *
     * @param contextFactory to use for producing contexts.
     */
    public TopLevelRecommendationEngine(ContextFactory<OUT, IN, C> contextFactory) {
        super();
        this.contextFactory = contextFactory;
    }

    /**
     * Produce recommendations.
     *
     * @param input for which recommendations are about to be computed.
     * @param mode  in which the computation takes place.
     * @param limit maximum number of recommendations desired.
     * @return recommendations sorted by decreasing relevance and trimmed to limit.
     */
    public List<Pair<OUT, Score>> recommend(IN input, Mode mode, int limit) {
        return recommend(input, contextFactory.produceContext(input, mode, limit)).get(limit);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Only delegates to subclass, overridden to be made <code>final</code>.
     */
    @Override
    public final Recommendations<OUT> recommend(IN input, C context) {
        return super.recommend(input, context);
    }
}

