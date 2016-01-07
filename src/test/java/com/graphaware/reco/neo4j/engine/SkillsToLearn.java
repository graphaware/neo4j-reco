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

package com.graphaware.reco.neo4j.engine;


import com.graphaware.reco.integration.domain.Relationships;
import org.neo4j.graphdb.*;

import java.util.HashMap;
import java.util.Map;

import static com.graphaware.common.util.PropertyContainerUtils.getInt;

public class SkillsToLearn extends CollaborativeEngine {

    private static final String LEVEL = "level";
    private final Label PERSON = DynamicLabel.label("Person");
    private final Label SKILL = DynamicLabel.label("Skill");

    @Override
    public String name() {
        return "skills";
    }

    @Override
    protected RelationshipType getType() {
        return Relationships.KNOWS;
    }

    @Override
    protected Direction getDirection() {
        return Direction.OUTGOING;
    }

    @Override
    protected boolean acceptableThroughNode(Node node) {
        return node.hasLabel(SKILL);
    }

    @Override
    protected boolean acceptableSimilarNode(Node node) {
        return node.hasLabel(PERSON);
    }

    @Override
    protected int scoreNode(Node recommendation, Node throughNode, Node similarNode, Relationship r1, Relationship r2, Relationship r3) {
        return getInt(r1, LEVEL, 0) + getInt(r2, LEVEL, 0) + getInt(r3, LEVEL, 0);
    }

    @Override
    protected Map<String, Object> details(Node throughNode, Node similarNode, Relationship r1, Relationship r2, Relationship r3) {
        Map<String, Object> result = new HashMap<>();
        result.put("skill", throughNode.getProperty("name"));
        result.put("person", similarNode.getProperty("name"));
        return result;
    }
}
