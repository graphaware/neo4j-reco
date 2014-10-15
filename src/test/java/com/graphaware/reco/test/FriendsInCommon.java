package com.graphaware.reco.test;

import com.graphaware.reco.filter.Filter;
import com.graphaware.reco.part.SomethingInCommon;
import com.graphaware.reco.transform.ParetoScoreTransformer;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;

import java.util.List;

import static com.graphaware.reco.demo.Relationships.FRIEND_OF;
import static org.neo4j.graphdb.Direction.BOTH;

/**
 * {@link com.graphaware.reco.part.EnginePart} that finds recommendation based on friends in common.
 * <p/>
 * Less than 2 friends doesn't matter and the score if increasing by Pareto function, with 80% with 10 friends in common
 * and a maximum score of 100.
 */
public class FriendsInCommon extends SomethingInCommon {

    public FriendsInCommon(List<Filter<Node, Node>> filters) {
        super(new ParetoScoreTransformer(100, 10, 1), filters);
    }

    @Override
    protected RelationshipType getType() {
        return FRIEND_OF;
    }

    @Override
    protected Direction getDirection() {
        return BOTH;
    }

    @Override
    public String name() {
        return "friendInCommon";
    }
}
