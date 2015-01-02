package com.graphaware.reco.integration.domain;

import org.neo4j.graphdb.RelationshipType;

/**
 * Relationships in the system.
 */
public enum Relationships implements RelationshipType {
    FRIEND_OF,
    LIVES_IN,
}
