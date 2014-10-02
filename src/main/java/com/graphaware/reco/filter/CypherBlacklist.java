package com.graphaware.reco.filter;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.util.Assert.*;

/**
 * {@link Blacklist} based on finding blacklisted {@link Node}s by executing a Cypher query.
 */
public abstract class CypherBlacklist implements Blacklist<Node, Node> {

    private final ExecutionEngine executionEngine;

    protected CypherBlacklist(GraphDatabaseService database) {
        executionEngine = new ExecutionEngine(database);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Node> getBlacklist(Node input) {
        notNull(input);

        Set<Node> excluded = new HashSet<>();

        ResourceIterator<Node> it = executionEngine.execute(getQuery(), Collections.singletonMap("id", (Object) input.getId())).columnAs("blacklist");
        while (it.hasNext()) {
            excluded.add(it.next());
        }

        return excluded;
    }

    /**
     * Get the Cypher query that returns blacklisted nodes. Can have {id} as a placeholder representing the ID of the input node.
     * Must return a set of nodes named "blacklist".
     *
     * @return Cypher query.
     */
    protected abstract String getQuery();
}
