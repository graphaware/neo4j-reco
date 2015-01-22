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

import com.graphaware.reco.generic.filter.BlacklistBuilder;
import com.graphaware.reco.generic.filter.Filter;

import java.util.*;

import static java.util.Collections.*;
import static org.springframework.util.Assert.*;

/**
 * A {@link com.graphaware.reco.generic.context.ContextFactory} that produces {@link com.graphaware.reco.generic.context.FilteringContext}s.
 * <p/>
 * The factory can be configured with a list of {@link com.graphaware.reco.generic.filter.BlacklistBuilder}s that produce
 * blacklists of items passed onto the context, and a list of {@link com.graphaware.reco.generic.filter.Filter}s, which
 * are passed to the produced contexts directly.
 * <p/>
 * The configuration can be either done by instantiating this class and calling {@link #addFilter(com.graphaware.reco.generic.filter.Filter)}
 * and {@link #addBlacklistBuilder(com.graphaware.reco.generic.filter.BlacklistBuilder)} (or their plural equivalents), or
 * by extending this class and overriding {@link #blacklistBuilders()} and {@link #filters()}.
 */
public class FilteringContextFactory<OUT, IN> implements ContextFactory<OUT, IN> {

    private final List<BlacklistBuilder<OUT, IN>> blacklistBuilders = new LinkedList<>();
    private final List<Filter<OUT, IN>> filters = new LinkedList<>();

    public FilteringContextFactory() {
        addBlacklistBuilders(blacklistBuilders());
        addFilters(filters());
    }

    /**
     * Get {@link com.graphaware.reco.generic.filter.BlacklistBuilder}s to be used by this factory. Designed to be overridden.
     *
     * @return empty list by default.
     */
    protected List<BlacklistBuilder<OUT, IN>> blacklistBuilders() {
        return Collections.emptyList();
    }

    /**
     * Get {@link com.graphaware.reco.generic.filter.Filter}s to be used by this factory. Designed to be overridden.
     *
     * @return empty list by default.
     */
    protected List<Filter<OUT, IN>> filters() {
        return Collections.emptyList();
    }

    /**
     * Add a {@link com.graphaware.reco.generic.filter.BlacklistBuilder} used by this factory to produce blacklists of items.
     *
     * @param blacklistBuilder to be used. Must not be <code>null</code>.
     * @return this instance.
     */
    public final FilteringContextFactory<OUT, IN> addBlacklistBuilder(BlacklistBuilder<OUT, IN> blacklistBuilder) {
        notNull(blacklistBuilder);

        blacklistBuilders.add(blacklistBuilder);

        return this;
    }

    /**
     * Add {@link com.graphaware.reco.generic.filter.BlacklistBuilder}s used by this factory to produce blacklists of items.
     *
     * @param blacklistBuilders to be used. Must not be <code>null</code> and all of the elements must not be <code>null</code>.
     * @return this instance.
     */
    public final FilteringContextFactory<OUT, IN> addBlacklistBuilders(List<BlacklistBuilder<OUT, IN>> blacklistBuilders) {
        notNull(blacklistBuilders);

        for (BlacklistBuilder<OUT, IN> blacklistBuilder : blacklistBuilders) {
            addBlacklistBuilder(blacklistBuilder);
        }

        return this;
    }

    /**
     * Add a {@link com.graphaware.reco.generic.filter.Filter} passed to the produced {@link com.graphaware.reco.generic.context.Context}s.
     *
     * @param filter to be used. Must not be <code>null</code>.
     * @return this instance.
     */
    public final FilteringContextFactory<OUT, IN> addFilter(Filter<OUT, IN> filter) {
        notNull(filter);

        filters.add(filter);

        return this;
    }

    /**
     * Add {@link com.graphaware.reco.generic.filter.Filter}s passed to the produced {@link com.graphaware.reco.generic.context.Context}s.
     *
     * @param filters to be used. Must not be <code>null</code> and all of the elements must not be <code>null</code>.
     * @return this instance.
     */
    public final FilteringContextFactory<OUT, IN> addFilters(List<Filter<OUT, IN>> filters) {
        notNull(filters);

        for (Filter<OUT, IN> filter : filters) {
            addFilter(filter);
        }

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Context<OUT, IN> produceContext(IN input, Mode mode, int limit) {
        Set<OUT> blacklist = new HashSet<>();
        for (BlacklistBuilder<OUT, IN> blacklistBuilder : blacklistBuilders) {
            blacklist.addAll(blacklistBuilder.buildBlacklist(input));
        }

        return new FilteringContext<>(input, mode, limit, unmodifiableList(filters), blacklist);
    }
}
