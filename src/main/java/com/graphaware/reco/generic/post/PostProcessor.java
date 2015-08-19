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

package com.graphaware.reco.generic.post;

import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.result.Recommendations;

/**
 * Recommendations post processor. Intended for boosting or penalizing scores of certain recommendations. For example,
 * you could boost the score of a recommended match on a dating site if the smoker/non-smoker preference of the
 * recommended person matches the preference of the person looking for recommendation.
 *
 * @param <OUT> type of the post-processed recommendations.
 * @param <IN>  type of the item recommendations are for / based on.
 */
public interface PostProcessor<OUT, IN> {

    /**
     * Post-process results.
     *
     * @param recommendations scored recommendations.
     * @param input           for whom the recommendation have been produced, must not be <code>null</code>.
     * @param context         for the recommendation computing process.
     */
    void postProcess(Recommendations<OUT> recommendations, IN input, Context<OUT, IN> context);

    /**
     * Get the maximum value by which this post processor can increase the overall score of a recommendation.
     * If it is not possible to say, this method should return {@link Float#POSITIVE_INFINITY}.
     *
     * @param input   for the recommendation are being post-processed, must not be <code>null</code>.
     * @param context for the recommendation computing process.
     * @return Maximum value by which this post processor can increase the overall score of a recommendation.
     * Must be positive and should be {@link Float#POSITIVE_INFINITY} if unknown.
     */
    float maxPositiveScore(IN input, Context<OUT, IN> context);

    /**
     * Get the maximum value by which this post processor can decrease the overall score of a recommendation.
     * If it is not possible to say, this method should return {@link Float#NEGATIVE_INFINITY}.
     *
     * @param input   for the recommendation are being post-processed, must not be <code>null</code>.
     * @param context for the recommendation computing process.
     * @return Maximum value by which this post processor can decrease the overall score of a recommendation.
     * Must be negative and should be {@link Float#NEGATIVE_INFINITY} if unknown.
     */
    float maxNegativeScore(IN input, Context<OUT, IN> context);
}
