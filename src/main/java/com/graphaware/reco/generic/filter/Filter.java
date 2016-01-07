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

package com.graphaware.reco.generic.filter;

import com.graphaware.reco.generic.context.Context;

/**
 * Component that filters recommendations.
 * <p/>
 * Intended for filtering out recommendation that are forbidden and this can be determined by looking at the recommended
 * item itself. For instance, this could be used to prevent suggesting items out of stock or people without a public profile.
 *
 * @param <OUT> type of recommendations.
 * @param <IN>  type of input on which recommendations are based.
 */
public interface Filter<OUT, IN> {

    /**
     * Should the given recommendation actually be used?
     *
     * @param item    to decide on. Must not be <code>null</code>.
     * @param input   input based on which this recommendation was found. Must not be <code>null</code>.
     * @param context for the recommendation computing process.
     * @return true iff the recommendation should be used based on this filter's opinion.
     */
    boolean include(OUT item, IN input, Context<OUT, IN> context);
}
