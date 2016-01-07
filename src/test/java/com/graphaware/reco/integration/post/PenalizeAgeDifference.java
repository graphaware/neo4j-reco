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
import com.graphaware.reco.generic.transform.ParetoFunction;
import com.graphaware.reco.generic.transform.TransformationFunction;
import org.neo4j.graphdb.Node;

import static com.graphaware.common.util.PropertyContainerUtils.getInt;

/**
 * Subtracts points for difference in age. The maximum number of points subtracted is 10 and 80% of that is achieved
 * when the difference is 20 years.
 */
public class PenalizeAgeDifference extends BasePostProcessor<Node, Node> {

    private final TransformationFunction function = new ParetoFunction(10, 20);

    /**
     * {@inheritDoc}
     */
    @Override
    protected String name() {
        return "ageDifference";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doPostProcess(Recommendations<Node> recommendations, Node input, Context<Node, Node> context) {
        int age = getInt(input, "age", 40);

        for (Recommendation<Node> reco : recommendations.get()) {
            int diff = Math.abs(getInt(reco.getItem(), "age", 40) - age);
            reco.add(name(), -function.transform(diff));
        }
    }
}
