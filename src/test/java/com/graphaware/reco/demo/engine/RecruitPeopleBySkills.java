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

package com.graphaware.reco.demo.engine;

import com.graphaware.reco.generic.config.KeyValueConfig;
import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.engine.SingleScoreRecommendationEngine;
import com.graphaware.reco.generic.result.PartialScore;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.graphdb.DynamicRelationshipType.withName;

/**
 * Recommends people that have the skills a company is looking for. For each skill, a number of points will be added.
 * This number is passed in as a "pointsPerSkill" String configuration value ({@link Context#config()}), defaulting to "1.0".
 */
public class RecruitPeopleBySkills extends SingleScoreRecommendationEngine<Node, Node> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<Node, PartialScore> doRecommendSingle(Node company, Context<Node, Node> context) {
        Map<Node, PartialScore> result = new HashMap<>();

        for (Relationship looksFor : company.getRelationships(withName("LOOKS_FOR_SKILL"), OUTGOING)) {
            Node skill = looksFor.getEndNode();
            for (Relationship hasSkill : skill.getRelationships(withName("HAS_SKILL"), INCOMING)) {
                Node person = hasSkill.getStartNode();

                addToResult(result, person, produceScore(skill, context));
            }
        }

        return result;
    }

    private PartialScore produceScore(Node skill, Context<Node, Node> context) {
        return new PartialScore(Float.valueOf(context.config(KeyValueConfig.class).get("pointsPerSkill", "1.0", String.class)), Collections.singletonMap("skills", skill.getProperty("name", "unknown")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return "skills";
    }
}
