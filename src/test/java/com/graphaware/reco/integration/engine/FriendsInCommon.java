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

package com.graphaware.reco.integration.engine;

import com.graphaware.reco.generic.transform.ParetoScoreTransformer;
import com.graphaware.reco.generic.transform.ScoreTransformer;
import com.graphaware.reco.integration.domain.Relationships;
import com.graphaware.reco.neo4j.engine.SomethingInCommon;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import java.util.Collections;
import java.util.Map;

import static org.neo4j.graphdb.Direction.BOTH;

/**
 * {@link com.graphaware.reco.generic.engine.RecommendationEngine} that finds recommendation based on friends in common.
 * <p/>
 * The score is increasing by Pareto function, achieving 80% score with 10 friends in common. The maximum score is 100.
 */
public class FriendsInCommon extends SomethingInCommon {

    private ScoreTransformer<Node> scoreTransformer =  ParetoScoreTransformer.create(100, 10);

    @Override
    public String name() {
        return "friendsInCommon";
    }

    @Override
    protected ScoreTransformer<Node> scoreTransformer() {
        return scoreTransformer;
    }

    @Override
    protected RelationshipType getType() {
        return Relationships.FRIEND_OF;
    }

    @Override
    protected Direction getDirection() {
        return BOTH;
    }

    @Override
    protected Map<String, Object> details(Node thingInCommon, Relationship withInput, Relationship withOutput) {
        return Collections.singletonMap("name", thingInCommon.getProperty("name"));
    }
}
