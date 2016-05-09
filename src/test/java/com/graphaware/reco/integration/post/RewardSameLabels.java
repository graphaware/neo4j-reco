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

package com.graphaware.reco.integration.post;

import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.post.BasePostProcessor;
import com.graphaware.reco.generic.result.Recommendation;
import com.graphaware.reco.generic.result.Recommendations;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import java.util.Arrays;

import static org.neo4j.helpers.collection.Iterables.asArray;


/**
 * Rewards same gender (exactly the same labels) by 10 points.
 */
public class RewardSameLabels extends BasePostProcessor<Node, Node> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected String name() {
        return "sameGender";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doPostProcess(Recommendations<Node> recommendations, Node input, Context<Node, Node> context) {
        Label[] inputLabels = asArray(Label.class, input.getLabels());

        for (Recommendation<Node> recommendation : recommendations.get()) {
            if (Arrays.equals(inputLabels, asArray(Label.class, recommendation.getItem().getLabels()))) {
                recommendation.add(name(), 10);
            }
        }
    }
}
