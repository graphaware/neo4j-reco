/*
 * Copyright (c) 2015 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

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
