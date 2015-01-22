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

package com.graphaware.reco.generic.log;

import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.result.Recommendation;
import com.graphaware.reco.generic.result.Score;
import org.neo4j.graphdb.Node;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * A logger logging recommended items and their scores using {@link org.slf4j.Logger}.
 *
 * @param <OUT> type of the recommendations produced.
 * @param <IN>  type of the item recommendations are for / based on.
 */
public class Slf4jRecommendationLogger<OUT, IN> implements Logger<OUT, IN> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Slf4jRecommendationLogger.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void log(IN input, List<Recommendation<OUT>> recommendations, Context<OUT, IN> context) {
        LOG.info(toString(input, recommendations, context));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString(IN input, List<Recommendation<OUT>> recommendations, Context<OUT, IN> context) {
        StringBuilder builder = new StringBuilder("Computed recommendations for ").append(inputToString(input)).append(": ");
        for (Recommendation<OUT> recommendation : recommendations) {
            builder.append("(");
            if (logUuid()) {
                builder.append(recommendation.getUuid()).append(":");
            }
            builder.append(itemToString(recommendation.getItem())).append(" ");
            builder.append(scoreToString(recommendation.getScore()));
            builder.append("),");
        }
        builder.deleteCharAt(builder.length() - 1);

        return builder.toString();
    }

    /**
     * Convert an input to the recommendation-computing process to String.
     *
     * @param input to convert.
     * @return converted input. Uses the {@link Object#toString()} method by default.
     */
    protected String inputToString(IN input) {
        return input.toString();
    }

    /**
     * Convert a recommendation String.
     *
     * @param item recommended item.
     * @return converted input. Uses the {@link Object#toString()} method by default.
     */
    protected String itemToString(OUT item) {
        return item.toString();
    }

    /**
     * Convert a score to String.
     *
     * @param score to convert.
     * @return converted score.
     */
    protected String scoreToString(Score score) {
        StringBuilder builder = new StringBuilder();
        builder.append("{total:").append(score.getTotalScore());
        for (Map.Entry<String, Integer> entry : score.getScoreParts().entrySet()) {
            builder.append(",").append(entry.getKey()).append(":").append(entry.getValue());
        }
        builder.append("}");
        return builder.toString();
    }

    /**
     * Should UUID be logged for recommendations?
     *
     * @return true iff UUID should be logged. <code>true</code> by default.
     */
    protected boolean logUuid() {
        return true;
    }
}

