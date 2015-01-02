package com.graphaware.reco.integration.engine;

import com.graphaware.reco.generic.engine.RecommendationEngine;
import com.graphaware.reco.neo4j.engine.Neo4jPrecomputedEngine;
import com.graphaware.reco.neo4j.engine.Neo4jTopLevelDelegatingEngine;
import org.neo4j.graphdb.Node;

import java.util.Arrays;
import java.util.List;

public final class FriendsRecommendationEngine extends Neo4jTopLevelDelegatingEngine {

    public FriendsRecommendationEngine() {
        super(new FriendsContextFactory());
    }

    @Override
    protected List<RecommendationEngine<Node, Node>> engines() {
        return Arrays.asList(
                new Neo4jPrecomputedEngine(),
                new Neo4jFriendsEngine()
        );
    }
}
