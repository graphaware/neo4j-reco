/*
 * Copyright (c) 2013-2016 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.reco.neo4j.filter;

import com.graphaware.reco.generic.config.Config;
import com.graphaware.reco.generic.filter.BlacklistBuilder;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.util.Assert.notNull;

/**
 * {@link BlacklistBuilder} based on finding blacklisted {@link Node}s by executing a Cypher query.
 */
public class CypherBlacklistBuilder implements BlacklistBuilder<Node, Node> {

    private final String query;

    /**
     * Construct a new blacklist builder.
     *
     * @param query the Cypher query that returns blacklisted nodes. Can have {@link #idParamName()} as a placeholder
     *              representing the ID of the input node. Must return a set of nodes named {@link #blacklistResultName()}.
     */
    public CypherBlacklistBuilder(String query) {
        this.query = query;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Set<Node> buildBlacklist(Node input, Config config) {
        notNull(input);

        Set<Node> excluded = new HashSet<>();

        ResourceIterator<Node> it = input.getGraphDatabase().execute(query, Collections.singletonMap(idParamName(), (Object) input.getId())).columnAs(blacklistResultName());

        while (it.hasNext()) {
            excluded.add(it.next());
        }

        return excluded;
    }

    /**
     * Get the name of the parameter that represents input node ID.
     *
     * @return input node ID parameter name, "id" by default.
     */
    protected String idParamName() {
        return "id";
    }

    /**
     * Get the name of the result column that contains blacklisted items.
     *
     * @return blacklisted items result name, "blacklist" by default.
     */
    protected String blacklistResultName() {
        return "blacklist";
    }
}
