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

import com.graphaware.common.util.Pair;
import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.context.Mode;
import com.graphaware.reco.generic.policy.ParticipationPolicy;
import com.graphaware.reco.generic.result.Recommendation;
import com.graphaware.reco.generic.result.Recommendations;
import com.graphaware.reco.generic.result.Score;

import java.util.List;

/**
 * A recommendation engine intended to be used as the "top-level" engine, i.e. the API for clients to get recommendations.
 *
 * @param <OUT> type of the recommendations produced.
 * @param <IN>  type of the item recommendations are for / based on.
 */
public interface TopLevelRecommendationEngine<OUT, IN> extends RecommendationEngine<OUT, IN> {

    /**
     * Produce recommendations.
     *
     * @param input for which recommendations are about to be computed.
     * @param mode  in which the computation takes place.
     * @param limit maximum number of recommendations desired.
     * @return recommendations sorted by decreasing relevance and trimmed to limit.
     */
    List<Recommendation<OUT>> recommend(IN input, Mode mode, int limit);
}
