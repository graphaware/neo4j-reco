package com.graphaware.reco.neo4j.demo;

import com.graphaware.reco.neo4j.engine.PrecomputedEngine;
import org.neo4j.graphdb.RelationshipType;

public class PrecomputedRecommendations extends PrecomputedEngine {

    @Override
    protected RelationshipType getType() {
        return Relationships.RECOMMEND;
    }
}
