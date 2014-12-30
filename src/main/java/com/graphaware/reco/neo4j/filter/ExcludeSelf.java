package com.graphaware.reco.neo4j.filter;

import com.graphaware.reco.generic.filter.BlacklistBuilder;
import com.graphaware.reco.generic.filter.Filter;
import org.neo4j.graphdb.Node;

import java.util.Collections;
import java.util.Set;

import static org.springframework.util.Assert.*;

/**
 * {@link BlacklistBuilder} and {@link Filter} that blacklists/excludes suggestions that are themselves.
 */
public class ExcludeSelf implements BlacklistBuilder<Node, Node>, Filter<Node, Node> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Node> buildBlacklist(Node input) {
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
