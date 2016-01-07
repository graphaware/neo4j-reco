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

package com.graphaware.reco.integration.log;

import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.log.Slf4jRecommendationLogger;
import com.graphaware.reco.generic.result.Recommendation;
import org.neo4j.graphdb.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommendationsRememberingLogger extends Slf4jRecommendationLogger<Node, Node> {

    private static final Map<Node, String> loggedRecommendations = new HashMap<>();

    @Override
    public String toString(Node input, List<Recommendation<Node>> recommendations, Context<Node, Node> context) {
        String result = super.toString(input, recommendations, context);
        loggedRecommendations.put(input, result);
        return result;
    }

    @Override
    protected String inputToString(Node input) {
        return input.getProperty("name", "unknown").toString();
    }

    @Override
    protected String itemToString(Node item) {
        return item.getProperty("name", "unknown").toString();
    }

    @Override
    protected boolean logUuid() {
        return false;
    }

    public String get(Node node) {
        return loggedRecommendations.containsKey(node) ? loggedRecommendations.get(node) : "";
    }

    public void clear() {
        loggedRecommendations.clear();
    }
}
