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

package com.graphaware.reco.generic.log;

import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.result.Recommendation;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * A logger logging recommendation statistics using {@link org.slf4j.Logger}.
 *
 * @param <OUT> type of the recommendations produced.
 * @param <IN>  type of the item recommendations are for / based on.
 */
public class Slf4jStatisticsLogger<OUT, IN> implements Logger<OUT, IN> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Slf4jStatisticsLogger.class);

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
        StringBuilder builder = new StringBuilder("Recommendation statistics for ").append(inputToString(input)).append(": ");
        for (Map.Entry<String, ? extends Map<String, Object>> statsForTask : context.statistics().get().entrySet()) {
            builder.append("(").append(statsForTask.getKey()).append(": {");
            for (Map.Entry<String, Object> entry : statsForTask.getValue().entrySet()) {
                builder.append(entry.getKey()).append(":").append(entry.getValue()).append(", ");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append("}),");
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
}

