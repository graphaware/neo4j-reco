package com.graphaware.reco.neo4j.filter;

import com.graphaware.reco.generic.filter.BlacklistBuilder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.util.Assert.notNull;

/**
 * {@link BlacklistBuilder} blacklisting items with which the subject of the recommendation (input) has a relationship.
 */
public class ExistingRelationshipBlacklistBuilder implements BlacklistBuilder<Node, Node> {

    private final RelationshipType type;
    private final Direction direction;

    /**
     * Construct a new blacklist builder.
     *
     * @param type      relationship type, must not be null.
     * @param direction relationship direction, must not be null.
     */
    public ExistingRelationshipBlacklistBuilder(RelationshipType type, Direction direction) {
        notNull(type);
        notNull(direction);

        this.type = type;
        this.direction = direction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Set<Node> buildBlacklist(Node input) {
        notNull(input);

        Set<Node> excluded = new HashSet<>();

        for (Relationship r : input.getRelationships(type, direction)) {
            if (blacklist(input, r)) {
                excluded.add(r.getOtherNode(input));
            }
        }

        return excluded;
    }

    /**
     * Should the given relationship really be used for blacklisting one of its nodes for the given input? Designed to
     * be overridden by subclasses that want to base the decision on something else besides the relationship direction and
     * type, such as relationship properties or some properties/labels of the other node participating in the relationship.
     *
     * @param input        for which recommendations are being computed.
     * @param relationship for which to decide whether it should be used for blacklisting a node.
     * @return true iff the given relationship should be used for blacklisting a node.  The default (non-overridden)
     * implementation always returns <code>true</code>.
     */
    protected boolean blacklist(Node input, Relationship relationship) {
        return true;
    }
}
