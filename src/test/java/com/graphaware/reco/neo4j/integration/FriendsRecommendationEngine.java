package com.graphaware.reco.neo4j.integration;

import com.graphaware.reco.generic.context.ContextFactory;
import com.graphaware.reco.neo4j.context.Neo4jContextFactory;
import com.graphaware.reco.neo4j.engine.Neo4jRecommendationEngine;
import com.graphaware.reco.neo4j.filter.ExcludeSelf;
import com.graphaware.reco.neo4j.filter.ExistingRelationshipBlacklistBuilder;
import org.neo4j.graphdb.Node;

import static com.graphaware.reco.neo4j.demo.Relationships.FRIEND_OF;
import static org.neo4j.graphdb.Direction.BOTH;

public class FriendsRecommendationEngine extends Neo4jRecommendationEngine {

    public FriendsRecommendationEngine() {
        super(contextFactory());

        addEngines(
                new PrecomputedRecommendations(),
                new FriendsInCommon(),
                new RandomPeople());

        addPostProcessors(
                new RewardSameLabels(),
                new RewardSameLocation(),
                new PenalizeAgeDifference());
    }

    private static ContextFactory<Node, Node> contextFactory() {
        return
                new Neo4jContextFactory()
                        .addBlacklistBuilders(
                                new ExcludeSelf(),
                                new ExistingRelationshipBlacklistBuilder(FRIEND_OF, BOTH))
                        .addFilters(
                                new ExcludeSelf());
    }
}
