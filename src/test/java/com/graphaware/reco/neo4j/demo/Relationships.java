package com.graphaware.reco.neo4j.demo;

import org.neo4j.graphdb.RelationshipType;

/**
 * Relationships in the system.
 */
public enum Relationships implements RelationshipType {
    FRIEND_OF,
    RECOMMEND
}
