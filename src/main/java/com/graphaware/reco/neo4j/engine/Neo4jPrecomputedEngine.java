package com.graphaware.reco.neo4j.engine;

import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.engine.PrecomputedEngine;
import com.graphaware.reco.generic.engine.RecommendationEngine;
import com.graphaware.reco.generic.policy.ParticipationPolicy;
import com.graphaware.reco.generic.result.Recommendations;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import javax.management.relation.Relation;

import static com.graphaware.common.util.PropertyContainerUtils.getInt;
import static org.neo4j.graphdb.Direction.OUTGOING;

/**
 * A {@link com.graphaware.reco.generic.engine.RecommendationEngine} that reads pre-computed recommendations and their
 * scores from the graph.
 * <p/>
 * It assumes there is a relationship of type {@link #getType()} from the subject of the recommendation ({@link Node}
 * being recommended to) to the object being recommended. It further assumes that the only properties on this relationship
 * are scores, i.e. reasons why this recommendation has been precomputed.
 * <p/>
 * {@link com.graphaware.reco.generic.context.Context} blacklists and filters are used to filter out recommendations for
 * which the situation has changed since they were pre-computed.
 * <p/>
 * Please note that when using other engines after this one to potentially add more recommendations computed in real-time,
 * precomputed recommendations produced by this engine should be added to the blacklist to prevent them from being
 * "re-discovered" and thus presented with doubled scores.
 */
public abstract class Neo4jPrecomputedEngine extends PrecomputedEngine<Node, Node, Relationship> {

    @Override
    protected Iterable<Relationship> produce(Node input) {
        return input.getRelationships(getType(), OUTGOING);
    }

    @Override
    protected Node extract(Relationship source) {
        return source.getEndNode();
    }

    @Override
    protected void addToResult(Recommendations<Node> recommendations, Node recommendation, Relationship relationship) {
        for (String scoreName : relationship.getPropertyKeys()) {
            recommendations.add(recommendation, scoreName, getInt(relationship, scoreName, 0));
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
