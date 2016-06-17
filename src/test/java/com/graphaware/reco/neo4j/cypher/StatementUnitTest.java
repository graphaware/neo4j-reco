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

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class StatementUnitTest {

    @Test
    public void statementWithQueryOnlyReturnsEmptyParamsMap(){
        Statement statement = new Statement("MATCH (n) RETURN count(n)");
        assertEquals("MATCH (n) RETURN count(n)", statement.getQuery());
        assertEquals(0, statement.getParameters().size());
    }

    @Test
    public void statementWithParamsReturnsParameters(){
        Map<String,Object> params = new HashMap<>();
        params.put("id", 3);
        Statement statement = new Statement("MATCH (n) WHERE id(n) = {id}", params);
        assertEquals(1, statement.getParameters().size());
        assertEquals(3, statement.getParameters().get("id"));
    }

    @Test
    public void statementAcceptParamsAfterInstanciation(){
        Statement statement = new Statement("MATCH (n) WHERE id(n) = {id}");
        statement.addParameter("id", 3);
        assertEquals(3, statement.getParameters().get("id"));
    }
}
