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

import com.graphaware.reco.generic.input.SortingInput;

/**
 * A {@link TopLevelRecommendationEngine} that uses the recommendation logic for sorting recommendations coming from an
 * external source, e.g. ElasticSearch, Solr, etc.
 *
 * It takes an instance of {@link SortingInput} as input. The input already contains pre-produced recommendations. The
 * job of this engine is to sort them, rather than discover new ones.
 *
 * {@link com.graphaware.reco.generic.filter.BlacklistBuilder}s, {@link com.graphaware.reco.generic.filter.Filter}s,
 * {@link com.graphaware.reco.generic.post.PostProcessor}s, and other similar components can still be used. *
 *
 * @param <OUT> type of the recommendations produced.
 * @param <IN>  type of the item recommendations are for / based on.
 */
public interface TopLevelSortingEngine<OUT, IN> extends TopLevelRecommendationEngine<OUT, SortingInput<OUT, IN>> {

}
