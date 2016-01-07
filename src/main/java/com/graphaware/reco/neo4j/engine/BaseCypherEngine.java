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

import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.engine.SingleScoreRecommendationEngine;
import com.graphaware.reco.generic.result.PartialScore;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link SingleScoreRecommendationEngine} based on finding recommendations by executing a Cypher query.
 * <p/>
 * An example query can look like this:
 * <code>"MATCH (p:Person)-[:FRIEND_OF]-(f)-[:FRIEND_OF]-(reco) WHERE NOT (p)-[:FRIEND_OF]-(reco) AND id(p)={id} RETURN reco, f.name as name, count(*) as score ORDER BY score DESC limit {limit}"</code>
 * {id} and {limit} will be provided as parameters by the engine. "reco" and "score" will become the recommended nodes
 * and their respective scores. All other values returned by the query (e.g. "name" in this case) must be scalars and
 * will become reasons for the recommendation.
 */
public abstract class BaseCypherEngine extends SingleScoreRecommendationEngine<Node, Node> {

    /**
     * @return the Cypher query that returns recommendations. Can have {@link #idParamName()} as a placeholder
     * representing the ID of the input node. Must return a set of nodes named {@link #recoResultName()}.
     * Should return a score, i.e. a numerical value for each recommendation named {@link #scoreResultName()}.
     * Can use {@link #limitParamName()} as a placeholder for Cypher LIMIT value. Must not be <code>null</code> or empty.
     */
    protected abstract String query();

    /**
     * {@inheritDoc}
     */
    @Override
    protected final Map<Node, PartialScore> doRecommendSingle(Node input, Context<Node, Node> context) {
        Map<Node, PartialScore> result = new HashMap<>();

        Result queryResult = input.getGraphDatabase().execute(query(), buildParams(input, context));

        while (queryResult.hasNext()) {
            Map<String, Object> row = queryResult.next();

            addToResult(result, (Node) row.get(recoResultName()), buildScore(row));
        }

        return result;
    }

    /**
     * Build parameters that will be passed into the Cypher query.
     *
     * @param input   to the recommendation engine.
     * @param context recommendation context.
     * @return map of named parameters.
     */
    protected Map<String, Object> buildParams(Node input, Context<Node, Node> context) {
        Map<String, Object> params = new HashMap<>();
        params.put(idParamName(), input.getId());
        params.put(limitParamName(), context.config().limit());
        return params;
    }

    /**
     * Build a score for a particular recommendation from a row of Cypher query results.
     *
     * @param row of results from Cypher query.
     * @return score.
     */
    protected PartialScore buildScore(Map<String, Object> row) {
        if (row.containsKey(scoreResultName())) {
            return new PartialScore(Float.valueOf(String.valueOf(row.get(scoreResultName()))), reasons(row));
        }

        return new PartialScore(defaultScore(), reasons(row));
    }

    /**
     * Compute the reasons for a recommendation from all returned values, except {@link #scoreResultName()} and {@link #recoResultName()}.
     *
     * @param row of results from Cypher query.
     * @return reasons. Cna be en empty map, but will never be <code>null</code>.
     */
    protected Map<String, Object> reasons(Map<String, Object> row) {
        Map<String, Object> result = new HashMap<>();

        for (String key : row.keySet()) {
            if (!key.equals(scoreResultName()) && !key.equals(recoResultName())) {
                result.put(key, row.get(key));
            }
        }

        return result;
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
     * Get the name of the parameter that represents limit (to the number of results).
     *
     * @return limit parameter name, "limit" by default.
     */
    protected String limitParamName() {
        return "limit";
    }

    /**
     * Get the name of the result column that contains computed recommendations.
     *
     * @return recommended items result name, "reco" by default.
     */
    protected String recoResultName() {
        return "reco";
    }

    /**
     * Get the name of the result column that contains scores of computed recommendations.
     *
     * @return recommended items score result name, "score" by default.
     */
    protected String scoreResultName() {
        return "score";
    }

    /**
     * Get default score value assigned to each recommendation in case the Cypher query result does not contain a score
     * column.
     *
     * @return default score, 1.0 by default.
     */
    protected float defaultScore() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BaseCypherEngine that = (BaseCypherEngine) o;

        if (!name().equals(that.name())) {
            return false;
        }
        return query().equals(that.query());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = name().hashCode();
        result = 31 * result + query().hashCode();
        return result;
    }
}
