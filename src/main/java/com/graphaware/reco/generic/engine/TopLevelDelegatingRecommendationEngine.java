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
import com.graphaware.reco.generic.context.ContextFactory;
import com.graphaware.reco.generic.context.Mode;
import com.graphaware.reco.generic.log.Logger;
import com.graphaware.reco.generic.result.Recommendation;
import com.graphaware.reco.generic.result.Recommendations;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.springframework.util.Assert.notNull;

/**
 * A {@link com.graphaware.reco.generic.engine.TopLevelRecommendationEngine} {@link com.graphaware.reco.generic.engine.DelegatingRecommendationEngine}.
 * It holds its own {@link com.graphaware.reco.generic.context.ContextFactory}, which is accepts at construction-time.
 * It also holds a list of {@link com.graphaware.reco.generic.log.Logger}s, to which it delegates logging of computed recommendations.
 */
public class TopLevelDelegatingRecommendationEngine<OUT, IN> extends DelegatingRecommendationEngine<OUT, IN> implements TopLevelRecommendationEngine<OUT, IN> {

    private final ContextFactory<OUT, IN> contextFactory;
    private final List<Logger<OUT, IN>> loggers = new LinkedList<>();

    /**
     * Create a new engine.
     *
     * @param contextFactory to use for producing contexts.
     */
    public TopLevelDelegatingRecommendationEngine(ContextFactory<OUT, IN> contextFactory) {
        super();
        this.contextFactory = contextFactory;
        addLoggers(loggers());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Recommendation<OUT>> recommend(IN input, Mode mode, int limit) {
        Context<OUT, IN> context = contextFactory.produceContext(input, mode, limit);

        List<Recommendation<OUT>> recommendations = recommend(input, context).get(limit);

        for (Logger<OUT, IN> logger : loggers) {
            logger.log(input, recommendations, context);
        }

        return recommendations;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Only delegates to subclass, overridden to be made <code>final</code>.
     */
    @Override
    public final Recommendations<OUT> doRecommend(IN input, Context<OUT, IN> context) {
        return super.doRecommend(input, context);
    }

    /**
     * Get {@link com.graphaware.reco.generic.log.Logger}s to use for recording / logging produced recommendations. Designed to be overridden.
     *
     * @return empty list by default.
     */
    protected List<Logger<OUT, IN>> loggers() {
        return Collections.emptyList();
    }

    /**
     * Add a {@link com.graphaware.reco.generic.log.Logger} that this engine logs to.
     *
     * @param logger to use. Must not be <code>null</code>.
     */
    public final void addLogger(Logger<OUT, IN> logger) {
        notNull(logger);
        loggers.add(logger);
    }

    /**
     * Add {@link com.graphaware.reco.generic.log.Logger}s that this engine logs to, in the order
     * in which they are added.
     *
     * @param loggers to use. Must not be <code>null</code> and all of the elements must not be <code>null</code>.
     */
    public final void addLoggers(List<Logger<OUT, IN>> loggers) {
        notNull(loggers);
        for (Logger<OUT, IN> logger : loggers) {
            addLogger(logger);
        }
    }
}

