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
