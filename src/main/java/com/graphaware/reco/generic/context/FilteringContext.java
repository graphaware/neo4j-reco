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
import com.graphaware.reco.generic.filter.Filter;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.graphaware.reco.generic.stats.Statistics.*;
import static org.springframework.util.Assert.hasLength;
import static org.springframework.util.Assert.notNull;

/**
 * A {@link com.graphaware.reco.generic.context.Context} that accepts a list of {@link com.graphaware.reco.generic.filter.Filter}s
 * and a set of blacklisted items, both of which it uses to disallow some recommendations.
 * <p/>
 * This class is thread-safe.
 */
public class FilteringContext<OUT, IN> extends SimpleContext<OUT, IN> {

    private final List<Filter<OUT, IN>> filters;
    private final Set<OUT> blacklist;

    /**
     * Construct a new context.
     *
     * @param input     for which recommendations are being computed. Must not be <code>null</code>.
     * @param config    for the computation. Must not be <code>null</code>.
     * @param filters   used to filter out items. Can be empty, but must not be <code>null</code>.
     * @param blacklist a set of blacklisted items. Can be empty, but must not be <code>null</code>.
     */
    public FilteringContext(IN input, Config config, List<Filter<OUT, IN>> filters, Set<OUT> blacklist) {
        super(input, config);

        notNull(filters);
        notNull(blacklist);

        this.filters = Collections.unmodifiableList(filters);
        this.blacklist = Collections.newSetFromMap(new ConcurrentHashMap<OUT, Boolean>());
        this.blacklist.addAll(blacklist);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Returns <code>false</code> for blacklisted items and items filtered out by at least one of the
     * {@link com.graphaware.reco.generic.filter.Filter}s.
     */
    @Override
    public boolean allow(OUT recommendation, String task) {
        notNull(recommendation);
        hasLength(task);

        statistics().incrementStatistic(task, CANDIDATE_ITEMS);

        if (!super.allow(recommendation, task)) {
            statistics().incrementStatistic(task, "NOT_ALLOWED_BY_SUPERCLASS");
            return false;
        }

        if (blacklist.contains(recommendation)) {
            statistics().incrementStatistic(task, BLACKLISTED_ITEMS);
            return false;
        }

        for (Filter<OUT, IN> filter : filters) {
            if (!filter.include(recommendation, input(), this)) {
                statistics().incrementStatistic(task, FILTERED_ITEMS);
                return false;
            }
        }

        return true;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Blacklist the given disallowed recommendation.
     */
    @Override
    public void disallow(OUT recommendation) {
        this.blacklist.add(recommendation);
    }
}

