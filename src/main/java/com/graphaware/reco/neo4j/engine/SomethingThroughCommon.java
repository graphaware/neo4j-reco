/*
 * Copyright (c) 2015 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.reco.neo4j.engine;

import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.engine.SingleScoreRecommendationEngine;
import com.graphaware.reco.generic.result.PartialScore;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;

import java.util.HashMap;
import java.util.Map;

import static org.neo4j.graphdb.Direction.*;

public abstract class SomethingThroughCommon extends SingleScoreRecommendationEngine<Node, Node> {

    @Override
    protected final Map<Node, PartialScore> doRecommendSingle(Node input, Context<Node, Node> context)
    {
        Map<Node, PartialScore> result = new HashMap<>();

        for (Relationship r1 : input.getRelationships(getType(), getDirection()) ) {
            Node throughNode = r1.getOtherNode(input);
            for (Relationship r2 : throughNode.getRelationships(getType(), reverse(getDirection()))) {
                Node similar = r2.getOtherNode(throughNode);
                if (similar.hasLabel(getLabel())) {
                    for (Relationship r3 : similar.getRelationships(getType(), getDirection())) {
                        Node something = r3.getOtherNode(similar);
                        if (something.getId() != throughNode.getId()) {
                            addToResult(result, something, new PartialScore(scoreNode(something), details(throughNode, similar)));
                        }
                    }
                }
            }

        }

        return result;
    }

    /**
     * Produce details about something found through an item in common to be stored as a {@link com.graphaware.reco.generic.result.Reason} inside a {@link com.graphaware.reco.generic.result.PartialScore}.
     *
     * @param throughCommon node representing the thing thing in common.
     * @param similarNode node representing the similar node found by a depth 2 search through the Relationship
     * @return details as a map of arbitrary key-value pairs.
     */
    protected Map<String, Object> details(Node throughCommon, Node similarNode) {
        return null;
    }

    /**
     * Score the recommended node.
     *
     * @param node to score.
     * @return score, 1 by default.
     */
    protected int scoreNode(Node node) {
        return 1;
    }

    protected abstract RelationshipType getType();

    protected abstract Direction getDirection();

    protected abstract Label getLabel();

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
