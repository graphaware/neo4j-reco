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

package com.graphaware.reco.neo4j.cypher;

import java.util.HashMap;
import java.util.Map;

/**
 * Class representing a Cypher statement with a query String and a Map of parameters
 */
public class Statement {

    private String query;
    private Map<String, Object> parameters;

    public Statement(String query){
        Map<String,Object> parameters = new HashMap<>();
        this.query = query;
        this.parameters = parameters;
    }

    public Statement(String query, Map<String,Object> parameters){
        this.query = query;
        this.parameters = parameters;
    }

    public String getQuery() {
        return query;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void addParameter(String key, Object value) {
        parameters.put(key, value);
    }
}
