package com.graphaware.reco.part;

import com.graphaware.reco.filter.Filter;
import com.graphaware.reco.score.Recommendations;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.graphaware.common.util.PropertyContainerUtils.getInt;
import static org.neo4j.graphdb.Direction.OUTGOING;

/**
 * An {@link EnginePart} that reads pre-computed recommendations and their scores from the graph.
 * <p/>
 * It assumes there is a relationship of type {@link #getType()} from the subject of the recommendation ({@link Node}
 * being recommended to) to the object being recommended. It further assumes that the only properties on this relationship
 * are scores, i.e. reasons why this recommendation has been precomputed.
 * <p/>
 * Filters can be provided to this {@link EnginePart} to filter out recommendations for which a situation has changed
 * since they were pre-computed.
 */
public abstract class PrecomputedEnginePart implements EnginePart<Node, Node> {

    private final List<Filter<Node, Node>> filters;

    protected PrecomputedEnginePart() {
        this(Collections.<Filter<Node, Node>>emptyList());
    }

    protected PrecomputedEnginePart(List<Filter<Node, Node>> filters) {
        this.filters = filters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recommend(Recommendations<Node> output, Node input, int limit, Set<Node> blacklist, boolean realTime) {
        for (Relationship recommend : input.getRelationships(getType(), OUTGOING)) {
            addToResult(output, input, blacklist, recommend);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EnoughResultsPolicy enoughResultsPolicy() {
        return EnoughResultsPolicy.COMPUTE_AND_STOP;
    }

    /**
     * Add a potential recommendation to the overall result. Perform checks that the recommendation should actually be
     * used based on the blacklist provided and filters configured. Nothing will happen if the item is found to be
     * blacklisted/filtered, i.e., it will be silently ignored.
     *
     * @param result       to add to.
     * @param input        for which the recommendation has been computed.
     * @param blacklist    of recommendations.
     * @param relationship that points to the recommendation.
     */
    protected final void addToResult(Recommendations<Node> result, Node input, Set<Node> blacklist, Relationship relationship) {
        if (blacklist.contains(relationship.getEndNode())) {
            return;
        }

        boolean include = true;
        for (Filter<Node, Node> filter : filters) {
            if (!filter.include(relationship.getEndNode(), input)) {
                include = false;
                break;
            }
        }

        if (include) {
            addToResult(result, relationship);
        }
    }

    /**
     * Add a recommendation to the overall result.
     *
     * @param result         to add to.
     * @param recommendation to add.
     */
    protected void addToResult(Recommendations<Node> result, Relationship recommendation) {
        for (String scoreName : recommendation.getPropertyKeys()) {
            result.add(recommendation.getEndNode(), scoreName, getInt(recommendation, scoreName, 0));
        }
    }

    /**
     * Get the relationship type of the relationship that links the subject of the recommendation to the recommended
     * item.
     *
     * @return relationship type.
     */
    protected abstract RelationshipType getType();
}
