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

package com.graphaware.reco.generic.engine;

import com.graphaware.reco.generic.config.Config;
import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.context.FilteringContext;
import com.graphaware.reco.generic.filter.BlacklistBuilder;
import com.graphaware.reco.generic.filter.Filter;
import com.graphaware.reco.generic.log.Logger;
import com.graphaware.reco.generic.result.Recommendation;
import com.graphaware.reco.generic.result.Recommendations;

import java.util.*;

import static java.util.Collections.unmodifiableList;
import static org.springframework.util.Assert.notNull;

/**
 * A {@link com.graphaware.reco.generic.engine.TopLevelRecommendationEngine} {@link com.graphaware.reco.generic.engine.DelegatingRecommendationEngine}.
 * <p/>
 * The engine can be configured with a list of {@link com.graphaware.reco.generic.filter.BlacklistBuilder}s that produce
 * blacklists of items passed onto the context, and a list of {@link com.graphaware.reco.generic.filter.Filter}s, which
 * are passed to the produced contexts directly. By default, it uses a {@link com.graphaware.reco.generic.context.FilteringContext}.
 * <p/>
 * Configuration can be either done by instantiating this class and calling {@link #addFilter(com.graphaware.reco.generic.filter.Filter)}
 * and {@link #addBlacklistBuilder(com.graphaware.reco.generic.filter.BlacklistBuilder)} (or their plural equivalents), or
 * by extending this class and overriding {@link #blacklistBuilders()} and {@link #filters()}.
 * <p/>
 * This engine also holds a list of {@link com.graphaware.reco.generic.log.Logger}s, to which it delegates logging of computed recommendations.
 */
public class TopLevelDelegatingRecommendationEngine<OUT, IN> extends DelegatingRecommendationEngine<OUT, IN> implements TopLevelRecommendationEngine<OUT, IN> {

    private final List<BlacklistBuilder<OUT, IN>> blacklistBuilders = new LinkedList<>();
    private final List<Filter<OUT, IN>> filters = new LinkedList<>();
    private final List<Logger<OUT, IN>> loggers = new LinkedList<>();

    /**
     * Create a new engine.
     */
    public TopLevelDelegatingRecommendationEngine() {
        super();
        addBlacklistBuilders(blacklistBuilders());
        addFilters(filters());
        addLoggers(loggers());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Context<OUT, IN> produceContext(IN input, Config config) {
        Set<OUT> blacklist = new HashSet<>();

        for (BlacklistBuilder<OUT, IN> blacklistBuilder : blacklistBuilders) {
            blacklist.addAll(blacklistBuilder.buildBlacklist(input, config));
        }

        return produceContext(input, config, unmodifiableList(filters), blacklist);
    }

    /**
     * Produce a {@link com.graphaware.reco.generic.context.Context} for the recommendation-computing process.
     *
     * @param input     for which recommendations are about to be computed.
     * @param config    for the computation. Must not be <code>null</code>.
     * @param filters   for filtering out items.
     * @param blacklist for blaclisting items.
     * @return context.
     */
    protected FilteringContext<OUT, IN> produceContext(IN input, Config config, List<Filter<OUT, IN>> filters, Set<OUT> blacklist) {
        return new FilteringContext<>(input, config, filters, blacklist);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Recommendation<OUT>> recommend(IN input, Config config) {
        return produceAndLogRecommendations(input, produceContext(input, config));
    }

    private List<Recommendation<OUT>> produceAndLogRecommendations(IN input, Context<OUT, IN> context) {
        List<Recommendation<OUT>> recommendations = recommend(input, context).get(context.config().limit());

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
     * Add a {@link com.graphaware.reco.generic.filter.BlacklistBuilder} used by this factory to produce blacklists of items.
     *
     * @param blacklistBuilder to be used. Must not be <code>null</code>.
     */
    public final void addBlacklistBuilder(BlacklistBuilder<OUT, IN> blacklistBuilder) {
        notNull(blacklistBuilder);

        blacklistBuilders.add(blacklistBuilder);
    }

    /**
     * Add {@link com.graphaware.reco.generic.filter.BlacklistBuilder}s used by this factory to produce blacklists of items.
     *
     * @param blacklistBuilders to be used. Must not be <code>null</code> and all of the elements must not be <code>null</code>.
     */
    public final void addBlacklistBuilders(List<BlacklistBuilder<OUT, IN>> blacklistBuilders) {
        notNull(blacklistBuilders);

        for (BlacklistBuilder<OUT, IN> blacklistBuilder : blacklistBuilders) {
            addBlacklistBuilder(blacklistBuilder);
        }
    }

    /**
     * Add a {@link com.graphaware.reco.generic.filter.Filter} passed to the produced {@link com.graphaware.reco.generic.context.Context}s.
     *
     * @param filter to be used. Must not be <code>null</code>.
     */
    public final void addFilter(Filter<OUT, IN> filter) {
        notNull(filter);

        filters.add(filter);
    }

    /**
     * Add {@link com.graphaware.reco.generic.filter.Filter}s passed to the produced {@link com.graphaware.reco.generic.context.Context}s.
     *
     * @param filters to be used. Must not be <code>null</code> and all of the elements must not be <code>null</code>.
     */
    public final void addFilters(List<Filter<OUT, IN>> filters) {
        notNull(filters);

        for (Filter<OUT, IN> filter : filters) {
            addFilter(filter);
        }
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

