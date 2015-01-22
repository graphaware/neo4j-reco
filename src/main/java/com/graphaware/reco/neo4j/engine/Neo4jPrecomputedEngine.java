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

import com.graphaware.reco.generic.engine.PrecomputedEngine;
import com.graphaware.reco.generic.result.Recommendations;
import com.graphaware.reco.generic.result.Score;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import static com.graphaware.common.util.PropertyContainerUtils.getFloat;
import static org.neo4j.graphdb.Direction.OUTGOING;

/**
 * A {@link com.graphaware.reco.generic.engine.PrecomputedEngine} that reads pre-computed recommendations and their
 * scores from the Neo4j graph.
 * <p/>
 * It assumes there is a relationship of type {@link #getType()} from the subject of the recommendation ({@link Node}
 * being recommended to) to the object being recommended. It further assumes that the only properties on this relationship
 * are scores, i.e. reasons why this recommendation has been precomputed.
 */
public class Neo4jPrecomputedEngine extends PrecomputedEngine<Node, Node, Relationship> {

    public static final RelationshipType RECOMMEND = DynamicRelationshipType.withName("RECOMMEND");

    /**
     * {@inheritDoc}
     */
    @Override
    protected final Iterable<Relationship> produce(Node input) {
        return input.getRelationships(getType(), OUTGOING);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final Node extract(Relationship source) {
        return source.getEndNode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void addToResult(Recommendations<Node> recommendations, Node recommendation, Relationship relationship) {
        Score score = new Score();

        for (String scoreName : relationship.getPropertyKeys()) {
            score.add(scoreName, getFloat(relationship, scoreName, 0));
        }

        recommendations.add(recommendation, score);
    }

    /**
     * Get the relationship type of the relationship that links the subject of the recommendation to the recommended
     * item. Intended to be overridden.
     *
     * @return relationship type. {@link #RECOMMEND} by default.
     */
    protected RelationshipType getType() {
        return RECOMMEND;
    }
}
