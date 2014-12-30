package com.graphaware.reco.neo4j.filter;

import com.graphaware.reco.generic.filter.BlacklistBuilder;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.util.Assert.*;

/**
 * {@link BlacklistBuilder} based on finding blacklisted {@link Node}s by executing a Cypher query.
 */
public abstract class CypherBlacklistBuilder implements BlacklistBuilder<Node, Node> {

    private final ExecutionEngine executionEngine;
    private final String query;

    protected CypherBlacklistBuilder(GraphDatabaseService database) {
        executionEngine = new ExecutionEngine(database);
        query = getQuery();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Set<Node> buildBlacklist(Node input) {
        notNull(input);

        Set<Node> excluded = new HashSet<>();

        ResourceIterator<Node> it = executionEngine.execute(query, Collections.singletonMap("id", (Object) input.getId())).columnAs("blacklist");

        while (it.hasNext()) {
            excluded.add(it.next());
        }

        return excluded;
    }

    /**
     * Get the Cypher query that returns blacklisted nodes. Can have {id} as a placeholder representing the ID of the
     * input node. Must return a set of nodes named "blacklist".
     *
     * @return Cypher query.
     */
    protected abstract String getQuery();
}
