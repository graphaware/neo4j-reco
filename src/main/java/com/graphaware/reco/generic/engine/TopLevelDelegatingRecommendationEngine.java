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
 * A {@link com.graphaware.reco.generic.engine.TopLevelRecommendationEngine} {@link com.graphaware.reco.generic.engine.DelegatingRecommendationEngine}.
 * It holds its own {@link com.graphaware.reco.generic.context.ContextFactory}, which is accepts at construction-time.
 */
public class TopLevelDelegatingRecommendationEngine<OUT, IN> extends DelegatingRecommendationEngine<OUT, IN> implements TopLevelRecommendationEngine<OUT, IN> {

    private final ContextFactory<OUT, IN> contextFactory;

    /**
     * Create a new engine.
     *
     * @param contextFactory to use for producing contexts.
     */
    public TopLevelDelegatingRecommendationEngine(ContextFactory<OUT, IN> contextFactory) {
        super();
        this.contextFactory = contextFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Pair<OUT, Score>> recommend(IN input, Mode mode, int limit) {
        return recommend(input, contextFactory.produceContext(input, mode, limit)).get(limit);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Only delegates to subclass, overridden to be made <code>final</code>.
     */
    @Override
    public final Recommendations<OUT> recommend(IN input, Context<OUT, IN> context) {
        return super.recommend(input, context);
    }
}

