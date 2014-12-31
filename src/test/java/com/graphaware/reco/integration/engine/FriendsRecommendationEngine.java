package com.graphaware.reco.integration.engine;

import com.graphaware.reco.neo4j.engine.Neo4jRecommendationEngine;

public final class FriendsRecommendationEngine extends Neo4jRecommendationEngine {

    private static final FriendsRecommendationEngine INSTANCE = new FriendsRecommendationEngine();

    public static FriendsRecommendationEngine getInstance() {
        return INSTANCE;
    }

    private FriendsRecommendationEngine() {
        super(FriendsRecommendationContextFactory.getInstance());

        addEngines(
                PrecomputedFriendsRecommendationEngine.getInstance(),
                ComputingFriendsRecommendationEngine.getInstance()
        );
    }
}
