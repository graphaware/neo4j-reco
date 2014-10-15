package com.graphaware.reco.post;

import com.graphaware.reco.score.Recommendations;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

/**
 * {@link AdditionalScorePostProcessor} that rewards something shared between the subject of the recommendation
 * (i.e., the input to the recommendation engine), and the recommended item.
 * <p/>
 * Note that this "something" shared is a concrete thing and it is expected for the purposes of this post processor that
 * each {@link Node} participating in the recommendation (i.e. the input and the recommendation itself) both have a maximum
 * of 1 relationship of type {@link #getType()} with {@link #getDirection()} (from the input's/recommendation's point of
 * view).
 * <p/>
 * For example, this could be used on a dating site to reward matches that live in the same location,
 * which would be indicated by a single LIVES_IN relationship between people and locations.
 */
public abstract class RewardSomethingShared extends AdditionalScorePostProcessor<Long> {

    @Override
    protected Long prepare(Recommendations<Node> output, Node input) {
        return getThingId(input);
    }

    @Override
    protected void doPostProcess(Long thingId, Node node, Recommendations<Node> output, Node input) {
        if (thingId == null) {
            return;
        }

        for (Node n : output.getItems()) {
            Long thingId2 = getThingId(n);
            if (thingId2 == null) {
                continue;
            }

            if (thingId2.longValue() == thingId.longValue()) {
                output.add(node, additionalScoreName(), additionalScoreValue());
            }
        }
    }


    private Long getThingId(Node input) {
        Relationship rel = input.getSingleRelationship(getType(), getDirection());
        if (rel != null) {
            return rel.getEndNode().getId();
        }
        return null;
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
     * Get the score this post processor adds if subject and recommendation have a thing in common.
     *
     * @return score to add.
     */
    protected abstract int additionalScoreValue();
}