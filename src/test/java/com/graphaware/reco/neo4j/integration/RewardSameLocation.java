package com.graphaware.reco.neo4j.integration;

import com.graphaware.reco.neo4j.post.RewardSomethingShared;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;

import static com.graphaware.reco.neo4j.integration.Relationships.*;
import static org.neo4j.graphdb.Direction.*;
import static org.neo4j.helpers.collection.Iterables.toArray;

/**
 * Rewards same location by 10 points.
 */
public class RewardSameLocation extends RewardSomethingShared {

    @Override
    protected RelationshipType type() {
        return LIVES_IN;
    }

    @Override
    protected Direction direction() {
        return OUTGOING;
    }

    @Override
    protected int scoreValue(Node recommendation, Node input, Node sharedThing) {
        return 10;
    }

    @Override
    protected String scoreName() {
        return "sameLocation";
    }
}
