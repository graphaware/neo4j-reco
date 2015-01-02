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

package com.graphaware.reco.integration.post;

import com.graphaware.reco.generic.post.PostProcessor;
import com.graphaware.reco.generic.result.Recommendations;
import com.graphaware.reco.generic.transform.ParetoScoreTransformer;
import org.neo4j.graphdb.Node;

import static com.graphaware.common.util.PropertyContainerUtils.getInt;

/**
 * Subtracts a point of each year of difference in age.
 */
public class PenalizeAgeDifference implements PostProcessor<Node, Node> {

    private final ParetoScoreTransformer transformer = new ParetoScoreTransformer(10, 20, 0);

    @Override
    public void postProcess(Recommendations<Node> recommendations, Node input) {
        int age = getInt(input, "age", 40);

        for (Node reco : recommendations.getItems()) {
            int diff = Math.abs(getInt(reco, "age", 40) - age);
            recommendations.add(reco, "ageDifference", -transformer.transform(reco, diff));
        }
    }
}
