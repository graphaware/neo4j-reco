package com.graphaware.reco.neo4j.integration;

import com.graphaware.reco.neo4j.engine.PrecomputedEngine;
import org.neo4j.graphdb.RelationshipType;

import static com.graphaware.reco.neo4j.demo.Relationships.RECOMMEND;

public class PrecomputedRecommendations extends PrecomputedEngine {

    @Override
    protected RelationshipType getType() {
        return RECOMMEND;
    }
}
