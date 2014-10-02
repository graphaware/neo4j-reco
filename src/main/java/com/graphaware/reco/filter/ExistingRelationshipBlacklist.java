package com.graphaware.reco.filter;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.util.Assert.notNull;

/**
 * {@link Blacklist} blacklisting items with which the subject of the recommendation (input) has a relationship.
 */
public class ExistingRelationshipBlacklist implements Blacklist<Node, Node> {

    private final RelationshipType type;
    private final Direction direction;

    public ExistingRelationshipBlacklist(RelationshipType type, Direction direction) {
        this.type = type;
        this.direction = direction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Node> getBlacklist(Node input) {
        notNull(input);

        Set<Node> excluded = new HashSet<>();

        for (Relationship r : input.getRelationships(type, direction)) {
            excluded.add(r.getEndNode());
        }

        return excluded;
    }
}
