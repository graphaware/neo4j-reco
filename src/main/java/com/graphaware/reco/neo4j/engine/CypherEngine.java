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

package com.graphaware.reco.neo4j.engine;

import static org.springframework.util.Assert.hasLength;

/**
 * {@link BaseCypherEngine} that accepts its name and the Cypher query as constructor arguments.
 * <p/>
 * An example query can look like this:
 * <code>"MATCH (p:Person)-[:FRIEND_OF]-(f)-[:FRIEND_OF]-(reco) WHERE NOT (p)-[:FRIEND_OF]-(reco) AND id(p)={id} RETURN reco, f.name as name, count(*) as score ORDER BY score DESC limit {limit}"</code>
 * {id} and {limit} will be provided as parameters by the engine. "reco" and "score" will become the recommended nodes
 * and their respective scores. All other values returned by the query (e.g. "name" in this case) must be scalars and
 * will become reasons for the recommendation.
 */
public class CypherEngine extends BaseCypherEngine {

    private final String name;
    private final String query;

    /**
     * Construct a new blacklist builder.
     *
     * @param name  name of the engine for logging purposes. Must not be <code>null</code> or empty.
     * @param query the Cypher query that returns recommendations. Can have {@link #idParamName()} as a placeholder
     *              representing the ID of the input node. Must return a set of nodes named {@link #recoResultName()}.
     *              Should return a score, i.e. a numerical value for each recommendation named {@link #scoreResultName()}.
     *              Can use {@link #limitParamName()} as a placeholder for Cypher LIMIT value. Must not be <code>null</code> or empty.
     */
    public CypherEngine(String name, String query) {
        hasLength(name);
        hasLength(query);

        this.name = name;
        this.query = query;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String name() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final String query() {
        return query;
    }
}
