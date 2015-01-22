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

package com.graphaware.reco.integration.engine;

import com.graphaware.reco.generic.engine.RecommendationEngine;
import com.graphaware.reco.generic.log.Logger;
import com.graphaware.reco.generic.log.Slf4jRecommendationLogger;
import com.graphaware.reco.generic.log.Slf4jStatisticsLogger;
import com.graphaware.reco.integration.log.RecommendationsRememberingLogger;
import com.graphaware.reco.neo4j.engine.Neo4jPrecomputedEngine;
import com.graphaware.reco.neo4j.engine.Neo4jTopLevelDelegatingEngine;
import org.neo4j.graphdb.Node;

import java.util.Arrays;
import java.util.List;

/**
 * {@link com.graphaware.reco.neo4j.engine.Neo4jTopLevelDelegatingEngine} that recommends friends by first trying to
 * read pre-computed recommendations from the graph, then (if there aren't enough results) by computing the friends in
 * real-time using {@link com.graphaware.reco.integration.engine.FriendsComputingEngine}.
 */
public final class FriendsRecommendationEngine extends Neo4jTopLevelDelegatingEngine {

    public FriendsRecommendationEngine() {
        super(new FriendsContextFactory());
    }

    @Override
    protected List<RecommendationEngine<Node, Node>> engines() {
        return Arrays.<RecommendationEngine<Node, Node>>asList(
                new Neo4jPrecomputedEngine(),
                new FriendsComputingEngine()
        );
    }

    @Override
    protected List<Logger<Node, Node>> loggers() {
        return Arrays.<Logger<Node, Node>>asList(
                new RecommendationsRememberingLogger(),
                new Slf4jRecommendationLogger<Node, Node>(),
                new Slf4jStatisticsLogger<Node, Node>()
        );
    }
}
