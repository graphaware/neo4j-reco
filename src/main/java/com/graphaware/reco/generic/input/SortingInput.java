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

package com.graphaware.reco.generic.input;

import com.graphaware.reco.generic.result.Recommendation;

import java.util.Collection;

/**
 * An input to recommendation engines that are supposed to order recommendations supplied by an external system,
 * such as {@link com.graphaware.reco.generic.engine.TopLevelSortingEngine}.
 *
 * @param <OUT> type of supplied (and ultimately reordered) recommendations.
 * @param <IN>  type of the primary input to the recommendation engine, i.e. the context recommendations are computed / sorted in.
 *              For instance, this could be the user recommendations are sorted for, potentially bundled with their
 *              location, preferences, the weather outside, etc.
 */
public interface SortingInput<OUT, IN> {

    /**
     * @return input (context) to the sorting process.
     */
    IN input();

    /**
     * @return recommendations that should be (re)ordered.
     */
    Collection<Recommendation<OUT>> candidates();
}
