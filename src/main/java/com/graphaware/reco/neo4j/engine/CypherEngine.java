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

package com.graphaware.reco.neo4j.engine;

import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.engine.SingleScoreRecommendationEngine;
import com.graphaware.reco.generic.result.PartialScore;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bachmanm on 14/06/2015.
 */
public abstract class CypherEngine extends SingleScoreRecommendationEngine<Node, Node> {

    private final String query;

    public CypherEngine(String query) {
        this.query = query;
    }

    @Override
    protected Map<Node, PartialScore> doRecommendSingle(Node input, Context<Node, Node> context) {
        Map<Node, PartialScore> result = new HashMap<>();

        Result queryResult = input.getGraphDatabase().execute(query, Collections.singletonMap("id", (Object) input.getId()));

        while (queryResult.hasNext()) {
            Map<String, Object> row = queryResult.next();

            addToResult(result, input.getGraphDatabase().getNodeById((Long) row.get(recoIdName())), new PartialScore(Float.valueOf(String.valueOf(row.get(scoreName())))));
        }

        return result;
    }

    protected String recoIdName() {
        return "reco";
    }

    protected String scoreName() {
        return "score";
    }
}
