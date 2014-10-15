package com.graphaware.reco.test;

import com.graphaware.reco.post.PostProcessor;
import com.graphaware.reco.post.RewardSomethingShared;
import com.graphaware.reco.score.Recommendations;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;

import java.util.Arrays;

import static com.graphaware.reco.test.Relationships.*;
import static org.neo4j.graphdb.Direction.*;
import static org.neo4j.helpers.collection.Iterables.toArray;

/**
 * Rewards same location by 10 points.
 */
public class RewardSameLocation extends RewardSomethingShared {

    @Override
    protected RelationshipType getType() {
        return LIVES_IN;
    }

    @Override
    protected Direction getDirection() {
        return OUTGOING;
    }

    @Override
    protected int additionalScoreValue() {
        return 10;
    }

    @Override
    protected String additionalScoreName() {
        return "sameLocation";
    }
}
