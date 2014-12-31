/*
 * Copyright (c) 2014 GraphAware
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

import com.graphaware.reco.generic.context.FilteringContext;
import com.graphaware.reco.generic.policy.ParticipationPolicy;
import com.graphaware.reco.integration.post.PenalizeAgeDifference;
import com.graphaware.reco.integration.post.RewardSameLabels;
import com.graphaware.reco.integration.post.RewardSameLocation;
import com.graphaware.reco.neo4j.engine.Neo4jRecommendationEngine;
import org.neo4j.graphdb.Node;

public final class ComputingFriendsRecommendationEngine extends Neo4jRecommendationEngine {

    private static final ComputingFriendsRecommendationEngine INSTANCE = new ComputingFriendsRecommendationEngine();

    public static ComputingFriendsRecommendationEngine getInstance() {
        return INSTANCE;
    }

    private ComputingFriendsRecommendationEngine() {
        super(FriendsRecommendationContextFactory.getInstance());

        addEngines(
                new FriendsInCommon(),
                new RandomPeople()
        );

        addPostProcessors(
                new RewardSameLabels(),
                new RewardSameLocation(),
                new PenalizeAgeDifference()
        );
    }

    @Override
    public ParticipationPolicy<Node, Node> participationPolicy(FilteringContext<Node, Node> context) {
        //noinspection unchecked
        return ParticipationPolicy.IF_MORE_RESULTS_NEEDED;
    }
}
