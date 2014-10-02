package com.graphaware.reco.part;

import com.graphaware.reco.filter.Filter;
import com.graphaware.reco.transform.ScoreTransformer;
import org.neo4j.graphdb.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.neo4j.graphdb.Direction.*;

/**
 * {@link BaseEnginePart} that recommends {@link Node}s with which have something in common. In other words, there is a
 * path of length two between the subject node (the input to the recommendation) and the recommended node.
 * <p/>
 * Moreover, both relationships of the path have the same type (specified by {@link #getType()} and unless {@link #getDirection()}
 * is {@link Direction#BOTH}, the first relationship of the path is of the specified direction and the second one if of
 * the opposite direction.
 * <p/>
 * Every time a recommendation is found, it's score is incremented by one. The final scores can be transformed by providing
 * a {@link ScoreTransformer} to the constructor.
 */
public abstract class SomethingInCommon extends BaseEnginePart<Node, Node> {

    protected SomethingInCommon() {
        super();
    }

    protected SomethingInCommon(ScoreTransformer transformer) {
        super(transformer);
    }

    protected SomethingInCommon(List<Filter<Node, Node>> filters) {
        super(filters);
    }

    protected SomethingInCommon(ScoreTransformer transformer, List<Filter<Node, Node>> filters) {
        super(transformer, filters);
    }

    @Override
    protected void populateResult(Map<Node, Integer> result, Node input, int limit, Set<Node> blacklist) {
        for (Relationship r1 : input.getRelationships(getType(), getDirection())) {
            Node thingInCommon = r1.getOtherNode(input);
            for (Relationship r2 : thingInCommon.getRelationships(getType(), reverse(getDirection()))) {
                addToResult(result, input, blacklist, r2.getOtherNode(thingInCommon), 1);
            }
        }
    }

    /**
     * Get the relationship type of the relationship that links the subject of the recommendation and the recommended
     * item with the thing in common.
     *
     * @return relationship type.
     */
    protected abstract RelationshipType getType();

    /**
     * Get the direction of the relationship between the subject (input to the engine) and the thing in common.
     *
     * @return direction.
     */
    protected abstract Direction getDirection();

    /**
     * Reverse direction.
     *
     * @param direction to reverse.
     * @return reversed direction.
     */
    private Direction reverse(Direction direction) {
        switch (direction) {
            case BOTH:
                return BOTH;
            case OUTGOING:
                return INCOMING;
            case INCOMING:
                return OUTGOING;
            default:
                throw new IllegalArgumentException("Unknown direction " + direction);
        }
    }
}
