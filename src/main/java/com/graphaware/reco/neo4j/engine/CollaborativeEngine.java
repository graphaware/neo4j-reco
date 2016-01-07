/*
 * Copyright (c) 2013-2016 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.reco.neo4j.engine;

import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.engine.SingleScoreRecommendationEngine;
import com.graphaware.reco.generic.result.PartialScore;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import java.util.HashMap;
import java.util.Map;

import static com.graphaware.reco.neo4j.util.DirectionUtils.reverse;

/**
 * {@link SingleScoreRecommendationEngine} that recommends {@link Node}s based on a collaborative filtering approach.
 * Expressed in pseudo-Cypher, the query executed when looking for items to recommend would be something like:
 * <code>MATCH (input)-[:REL_TYPE]->(throughNode)<-[:REL_TYPE]-(similar)-[:REL_TYPE]->(recommendation) RETURN recommendation</code>.
 * Of course, directions can be reversed, unless unless {@link #getDirection()} returns {@link Direction#BOTH}. The type
 * of the relationship traversed is determined by {@link #getType()}.
 * <p/>
 * Every time a recommendation is found, it's score is incremented by {@link #scoreNode(Node, Node, Node, Relationship, Relationship, Relationship)}.
 */
public abstract class CollaborativeEngine extends SingleScoreRecommendationEngine<Node, Node> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected final Map<Node, PartialScore> doRecommendSingle(Node input, Context<Node, Node> context) {
        Map<Node, PartialScore> result = new HashMap<>();

        for (Relationship r1 : input.getRelationships(getType(), getDirection())) {
            Node throughNode = r1.getOtherNode(input);
            if (acceptableThroughNode(throughNode)) {
                for (Relationship r2 : throughNode.getRelationships(getType(), reverse(getDirection()))) {
                    Node similar = r2.getOtherNode(throughNode);
                    if (similar.getId() != input.getId() && acceptableSimilarNode(similar)) {
                        for (Relationship r3 : similar.getRelationships(getType(), getDirection())) {
                            Node recommendation = r3.getOtherNode(similar);
                            if (recommendation.getId() != throughNode.getId()) {
                                addToResult(result, recommendation, new PartialScore(
                                        scoreNode(recommendation, throughNode, similar, r1, r2, r3),
                                        details(throughNode, similar, r1, r2, r3)));
                            }
                        }
                    }
                }
            }

        }

        return result;
    }

    /**
     * Score the recommended node.
     *
     * @param recommendation to score.
     * @param throughNode    first  node on path from the input to the recommendation.
     * @param similarNode    second  node on path from the input to the recommendation.
     * @param r1             first relationship on path from the input to the recommendation.
     * @param r2             second relationship on path from the input to the recommendation.
     * @param r3             third relationship on path from the input to the recommendation.
     * @return score, 1 by default.
     */
    protected int scoreNode(Node recommendation, Node throughNode, Node similarNode, Relationship r1, Relationship r2, Relationship r3) {
        return 1;
    }

    /**
     * Produce details about the way a recommendation was found to be stored as a {@link com.graphaware.reco.generic.result.Reason} inside a {@link com.graphaware.reco.generic.result.PartialScore}.
     *
     * @param throughNode first  node on path from the input to the recommendation.
     * @param similarNode second  node on path from the input to the recommendation.
     * @param r1          first relationship on path from the input to the recommendation.
     * @param r2          second relationship on path from the input to the recommendation.
     * @param r3          third relationship on path from the input to the recommendation.
     * @return details as a map of arbitrary key-value pairs. <code>null</code> by default.
     */
    protected Map<String, Object> details(Node throughNode, Node similarNode, Relationship r1, Relationship r2, Relationship r3) {
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
     * @return <code>true</code> iff a "through node" (first node on path from input to recommendation) is acceptable,
     * i.e., the traversal should continue. <code>true</code> by default.
     */
    protected boolean acceptableThroughNode(Node node) {
        return true;
    }

    /**
     * @return <code>true</code> iff a "similar node" (second node on path from input to recommendation) is acceptable,
     * i.e., the traversal should continue. <code>true</code> by default.
     */
    protected boolean acceptableSimilarNode(Node node) {
        return true;
    }
}
