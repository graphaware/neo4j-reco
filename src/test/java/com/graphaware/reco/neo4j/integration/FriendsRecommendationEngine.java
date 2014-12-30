package com.graphaware.reco.neo4j.integration;

import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.policy.ParticipationPolicy;
import com.graphaware.reco.neo4j.context.Neo4jContextFactory;
import com.graphaware.reco.neo4j.engine.Neo4jDelegatingEngine;
import com.graphaware.reco.neo4j.engine.Neo4jPrecomputedEngine;
import com.graphaware.reco.neo4j.engine.Neo4jRecommendationEngine;
import com.graphaware.reco.neo4j.filter.ExcludeSelf;
import com.graphaware.reco.neo4j.filter.ExistingRelationshipBlacklistBuilder;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;

import static com.graphaware.reco.neo4j.demo.Relationships.FRIEND_OF;
import static com.graphaware.reco.neo4j.demo.Relationships.RECOMMEND;
import static org.neo4j.graphdb.Direction.BOTH;

public class FriendsRecommendationEngine extends Neo4jRecommendationEngine {

    public FriendsRecommendationEngine() {
        super(contextFactory());

        addEngines(
                precomputedEngine(),
                delegatingEngine()
        );
    }

    private static Neo4jPrecomputedEngine precomputedEngine() {
        return new Neo4jPrecomputedEngine() {
            @Override
            protected RelationshipType getType() {
                return RECOMMEND;
            }
        };
    }

    private static Neo4jDelegatingEngine delegatingEngine() {
        Neo4jDelegatingEngine result = new Neo4jDelegatingEngine() {
            @Override
            public ParticipationPolicy<Node, Node> participationPolicy(Context<Node, Node> context) {
                //noinspection unchecked
                return ParticipationPolicy.IF_MORE_RESULTS_NEEDED;
            }
        };

        result.addEngines(
                new FriendsInCommon(),
                new RandomPeople()
        );

        result.addPostProcessors(
                new RewardSameLabels(),
                new RewardSameLocation(),
                new PenalizeAgeDifference()
        );

        return result;
    }

    private static Neo4jContextFactory contextFactory() {
        Neo4jContextFactory result = new Neo4jContextFactory();

        result.addBlacklistBuilders(
                new ExcludeSelf(),
                new ExistingRelationshipBlacklistBuilder(FRIEND_OF, BOTH));

        result.addFilters(
                new ExcludeSelf());

        return result;
    }
}
