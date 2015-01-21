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

import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.engine.RecommendationEngine;
import com.graphaware.reco.generic.log.RecommendationLogger;
import com.graphaware.reco.generic.policy.ParticipationPolicy;
import com.graphaware.reco.generic.post.PostProcessor;
import com.graphaware.reco.integration.log.RememberingLogger;
import com.graphaware.reco.integration.post.PenalizeAgeDifference;
import com.graphaware.reco.integration.post.RewardSameLabels;
import com.graphaware.reco.integration.post.RewardSameLocation;
import com.graphaware.reco.neo4j.engine.Neo4jTopLevelDelegatingEngine;
import org.neo4j.graphdb.Node;

import java.util.Arrays;
import java.util.List;

/**
 * {@link com.graphaware.reco.neo4j.engine.Neo4jTopLevelDelegatingEngine} that computes friend recommendations.
 */
public final class FriendsComputingEngine extends Neo4jTopLevelDelegatingEngine {

    public FriendsComputingEngine() {
        super(new FriendsContextFactory());
    }

    @Override
    protected List<RecommendationEngine<Node, Node>> engines() {
        return Arrays.<RecommendationEngine<Node, Node>>asList(
                new FriendsInCommon(),
                new RandomPeople()
        );
    }

    @Override
    protected List<PostProcessor<Node, Node>> postProcessors() {
        return Arrays.asList(
                new RewardSameLabels(),
                new RewardSameLocation(),
                new PenalizeAgeDifference()
        );
    }

    @Override
    protected List<RecommendationLogger<Node, Node>> loggers() {
        return Arrays.<RecommendationLogger<Node, Node>>asList(
                new RememberingLogger()
        );
    }

    @Override
    public ParticipationPolicy<Node, Node> participationPolicy(Context<Node, Node> context) {
        //noinspection unchecked
        return ParticipationPolicy.IF_MORE_RESULTS_NEEDED;
    }
}
