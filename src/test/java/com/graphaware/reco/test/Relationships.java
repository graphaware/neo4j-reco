package com.graphaware.reco.test;

import org.neo4j.graphdb.RelationshipType;

/**
 * Relationships in the system.
 */
public enum Relationships implements RelationshipType {
    FRIEND_OF,
    LIVES_IN,
    RECOMMEND
}
