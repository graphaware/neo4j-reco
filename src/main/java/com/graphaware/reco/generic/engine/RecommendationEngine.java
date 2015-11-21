/*
 * Copyright (c) 2013-2015 GraphAware
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

import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.policy.ParticipationPolicy;
import com.graphaware.reco.generic.result.Recommendations;

/**
 * A recommendation engine.
 *
 * @param <OUT> type of the recommendations produced.
 * @param <IN>  type of the item recommendations are for / based on.
 */
public interface RecommendationEngine<OUT, IN> {

    /**
     * Get the name of this engine. This name should be unique within the overall recommendation engine structure and
     * will be used for naming scores produced by the engine, as well as for collecting {@link com.graphaware.reco.generic.stats.Statistics}.
     *
     * @return engine name.
     */
    String name();

    /**
     * Get this engine's participation / involvement in producing recommendations in a specific context.
     *
     * @param context the context in which recommendations are being produced.
     * @return participation policy.
     */
    ParticipationPolicy<OUT, IN> participationPolicy(Context<OUT, IN> context);

    /**
     * Produce recommendations.
     *
     * @param input   input to the recommendation engine. Typically the person or item recommendations are being
     *                computed for.
     * @param context additional information about the recommendation process useful to the engine.
     * @return recommendations.
     */
    Recommendations<OUT> recommend(IN input, Context<OUT, IN> context);
}
