package com.graphaware.reco.filter;

import org.neo4j.graphdb.Node;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Set;

import static org.springframework.util.Assert.*;

/**
 * {@link Blacklist} and {@link Filter} that blacklists/excludes suggestions that are themselves.
 */
public class ExcludeSelf implements Blacklist<Node, Node>, Filter<Node, Node> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Node> getBlacklist(Node input) {
        notNull(input);

        return Collections.singleton(input);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(Node recommendation, Node input) {
        notNull(recommendation);
        notNull(input);

        return input.getId() != recommendation.getId();
    }
}
