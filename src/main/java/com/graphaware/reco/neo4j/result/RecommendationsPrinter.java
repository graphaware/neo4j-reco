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

package com.graphaware.reco.neo4j.result;

import com.graphaware.common.util.PropertyContainerUtils;
import com.graphaware.reco.generic.result.Recommendation;
import org.neo4j.graphdb.Node;

import java.util.List;
import java.util.Map;

/**
 * Utility class that converts {@link com.graphaware.reco.generic.result.Recommendation}s to {@link String}.
 */
public final class RecommendationsPrinter {

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public static String toString(List<Recommendation<Node>> recommendations) {
        return toString(recommendations, false);
    }

    public static String toString(List<Recommendation<Node>> recommendations, boolean includeUUID) {
        StringBuilder s = new StringBuilder();

        for (Recommendation<Node> reco : recommendations) {
            if (includeUUID) {
                s.append(reco.getUuid()).append(":");
            }

            s.append(PropertyContainerUtils.nodeToString(reco.getItem())).append(": ");
            s.append("total:").append(reco.getScore().getTotalScore());
            for (Map.Entry<String, Integer> part : reco.getScore().getScoreParts().entrySet()) {
                s.append(", ");
                s.append(part.getKey()).append(":").append(part.getValue());
            }
            s.append(LINE_SEPARATOR);
        }

        String result = s.toString();
        if (result.isEmpty()) {
            return result;
        }
        return result.substring(0, result.length() - LINE_SEPARATOR.length());
    }

    private RecommendationsPrinter() {
    }
}
