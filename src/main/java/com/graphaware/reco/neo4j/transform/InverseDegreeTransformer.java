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

package com.graphaware.reco.neo4j.transform;

import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.result.PartialScore;
import com.graphaware.reco.generic.transform.ScoreTransformer;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

/**
 * {@link ScoreTransformer} that transforms the score of a recommended item ({@link Node}) by dividing the score by the degree of the
 * item.
 */
public class InverseDegreeTransformer implements ScoreTransformer<Node> {

    private static final Logger LOG = LoggerFactory.getLogger(InverseDegreeTransformer.class);

    private final RelationshipType relationshipType;
    private final Direction direction;

    /**
     * Construct a new transformer.
     *
     * @param relationshipType type of the relationships that determines the degree. Can be <code>null</code>, which means all relationship types.
     * @param direction        direction of the relationships that determine the degree. Can be <code>null</code>, which means {@link Direction#BOTH}.
     */
    public InverseDegreeTransformer(RelationshipType relationshipType, Direction direction) {
        this.relationshipType = relationshipType;
        this.direction = direction == null ? Direction.BOTH : direction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PartialScore transform(Node item, PartialScore score, Context<? extends Node, ?> context) {
        int degree = getDegree(item);

        if (degree > 0) {
            score.setNewValue(score.getValue() / degree, Collections.singletonMap("DivideByDegree", degree));
        } else {
            LOG.warn("Did not perform transformation because the degree of " + item + " is 0. This indicates a problem with your recommendation engine design, because such item should not have been recommended in the first place.");
        }

        return score;
    }

    private int getDegree(Node item) {
        if (relationshipType == null) {
            return item.getDegree(direction);
        } else {
            return item.getDegree(relationshipType, direction);
        }
    }
}
