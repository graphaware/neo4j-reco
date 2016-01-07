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

package com.graphaware.reco.neo4j.post;

import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.post.BasePostProcessor;
import com.graphaware.reco.generic.result.PartialScore;
import com.graphaware.reco.generic.result.Recommendation;
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
public abstract class RewardSomethingShared extends BasePostProcessor<Node, Node> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doPostProcess(Recommendations<Node> recommendations, Node input, Context<Node, Node> context) {
        Node inputSharedNode = sharedNode(input);

        if (inputSharedNode == null) {
            return;
        }

        for (Recommendation<Node> recommendation : recommendations.get()) {
            Node recommendationSharedNode = sharedNode(recommendation.getItem());
            if (recommendationSharedNode == null) {
                continue;
            }

            if (recommendationSharedNode.getId() == inputSharedNode.getId()) {
                recommendation.add(name(), partialScore(recommendation.getItem(), input, recommendationSharedNode));
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
     * Get the partial score this post processor adds if subject and recommendation have a thing in common.
     *
     * @param recommendation the recommendation.
     * @param input          the input (subject).
     * @param sharedThing    the node representing the thing in common.
     * @return score to add.
     */
    protected abstract PartialScore partialScore(Node recommendation, Node input, Node sharedThing);
}
