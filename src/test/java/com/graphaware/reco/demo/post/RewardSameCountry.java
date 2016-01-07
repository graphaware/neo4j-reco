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

package com.graphaware.reco.demo.post;

import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.post.BasePostProcessor;
import com.graphaware.reco.generic.result.Recommendation;
import com.graphaware.reco.generic.result.Recommendations;
import org.neo4j.graphdb.Node;

import java.util.Collections;

import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.graphdb.DynamicRelationshipType.withName;

/**
 * Rewards people who live in the same country as the company by 10 (hardcoded) points.
 */
public class RewardSameCountry extends BasePostProcessor<Node, Node> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected String name() {
        return "same-country";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doPostProcess(Recommendations<Node> recommendations, Node company, Context<Node, Node> context) {
        Node companyCountry = company.getSingleRelationship(withName("LOCATED_IN"), OUTGOING).getEndNode();

        for (Recommendation<Node> reco : recommendations.get()) {
            Node personCountry = reco.getItem().getSingleRelationship(withName("LIVES_IN"), OUTGOING).getEndNode();
            if (personCountry.equals(companyCountry)) {
                reco.add(name(), 10, Collections.singletonMap("Country", personCountry.getProperty("name")));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float maxPositiveScore(Node input, Context<Node, Node> context) {
        return 10;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float maxNegativeScore(Node input, Context<Node, Node> context) {
        return 0;
    }
}

