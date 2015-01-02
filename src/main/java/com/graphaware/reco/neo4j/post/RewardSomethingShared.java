package com.graphaware.reco.neo4j.post;

import com.graphaware.reco.generic.post.PostProcessor;
import com.graphaware.reco.generic.result.Recommendations;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

/**
 * {@link com.graphaware.reco.generic.post.PostProcessor} that rewards something shared between the subject of the recommendation
 * (i.e., the input to the recommendation engine), and the recommended item.
 * <p/>
 * Note that this "something" shared is a concrete thing and it is expected for the purposes of this post processor that
 * each {@link Node} participating in the recommendation (i.e. the input and the recommendation itself) both have a maximum
 * of 1 relationship of type {@link #type()} with {@link #direction()} (from the input's/recommendation's point of
 * view).
 * <p/>
 * For example, this could be used on a dating site to reward matches that live in the same location,
 * which would be indicated by a single LIVES_IN relationship between people and locations.
 */
public abstract class RewardSomethingShared implements PostProcessor<Node, Node> {

    @Override
    public void postProcess(Recommendations<Node> recommendations, Node input) {
        Node inputSharedNode = sharedNode(input);

        if (inputSharedNode == null) {
            return;
        }

        for (Node recommendation : recommendations.getItems()) {
            Node recommendationSharedNode = sharedNode(recommendation);
            if (recommendationSharedNode == null) {
                continue;
            }

            if (recommendationSharedNode.getId() == inputSharedNode.getId()) {
                recommendations.add(recommendation, scoreName(), scoreValue(recommendation, input, recommendationSharedNode));
            }
        }
    }

    private Node sharedNode(Node input) {
        try {
            Relationship rel = input.getSingleRelationship(type(), direction());
            if (rel != null) {
                return rel.getOtherNode(input);
            }
        } catch (RuntimeException e) {
            //probably too many relationships
        }

        return null;
    }

    /**
     * Get the relationship type of the relationship that links the subject of the recommendation and the recommended
     * item with the thing in common.
     *
     * @return relationship type.
     */
    protected abstract RelationshipType type();

    /**
     * Get the direction of the relationship between the subject (input to the engine) and the thing in common.
     *
     * @return direction.
     */
    protected abstract Direction direction();

    /**
     * Get the name of the score added by this post processor.
     *
     * @return score name.
     */
    protected abstract String scoreName();

    /**
     * Get the score value this post processor adds if subject and recommendation have a thing in common.
     *
     * @param recommendation the recommendation.
     * @param input          the input (subject).
     * @param sharedThing    the node representing the thing in common.
     * @return score to add.
     */
    protected abstract int scoreValue(Node recommendation, Node input, Node sharedThing);
}