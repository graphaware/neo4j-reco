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

import com.graphaware.reco.generic.config.SimpleConfig;
import com.graphaware.reco.generic.context.SimpleContext;
import com.graphaware.reco.generic.engine.RecommendationEngine;
import com.graphaware.reco.generic.result.Recommendation;
import com.graphaware.test.integration.DatabaseIntegrationTest;
import com.graphaware.test.integration.EmbeddedDatabaseIntegrationTest;
import org.junit.Test;
import org.neo4j.graphdb.*;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@link CypherEngine}.
 */
public class CypherEngineTest extends EmbeddedDatabaseIntegrationTest {

    @Override
    protected void populateDatabase(GraphDatabaseService database) {
        database.execute(
                "CREATE " +
                        "(m:Person:Male {name:'Michal', age:30})," +
                        "(d:Person:Female {name:'Daniela', age:20})," +
                        "(v:Person:Male {name:'Vince', age:40})," +
                        "(a:Person:Male {name:'Adam', age:30})," +
                        "(l:Person:Female {name:'Luanne', age:25})," +
                        "(b:Person:Male {name:'Christophe', age:60})," +
                        "(j:Person:Male {name:'Jim', age:38})," +

                        "(lon:City {name:'London'})," +
                        "(mum:City {name:'Mumbai'})," +
                        "(br:City {name:'Bruges'})," +

                        "(m)-[:FRIEND_OF]->(d)," +
                        "(m)-[:FRIEND_OF]->(l)," +
                        "(m)-[:FRIEND_OF]->(a)," +
                        "(m)-[:FRIEND_OF]->(v)," +
                        "(d)-[:FRIEND_OF]->(v)," +
                        "(b)-[:FRIEND_OF]->(v)," +
                        "(j)-[:FRIEND_OF]->(v)," +
                        "(j)-[:FRIEND_OF]->(m)," +
                        "(j)-[:FRIEND_OF]->(a)," +
                        "(a)-[:LIVES_IN]->(lon)," +
                        "(d)-[:LIVES_IN]->(lon)," +
                        "(v)-[:LIVES_IN]->(lon)," +
                        "(m)-[:LIVES_IN]->(lon)," +
                        "(j)-[:LIVES_IN]->(lon)," +
                        "(c)-[:LIVES_IN]->(br)," +
                        "(l)-[:LIVES_IN]->(mum)");
    }

    @Test
    public void shouldComputeRecommendationsFromCypher() {
        String query = "MATCH (p:Person)-[:FRIEND_OF]-()-[:FRIEND_OF]-(reco) WHERE NOT (p)-[:FRIEND_OF]-(reco) AND id(p)={id} RETURN reco, count(*) as score";
        RecommendationEngine<Node, Node> engine = new CypherEngine("test engine", query);

        List<Recommendation<Node>> result;

        try (Transaction tx = getDatabase().beginTx()) {
            Node vince = getDatabase().findNode(Label.label("Person"), "name", "Vince");
            result = engine.recommend(vince, new SimpleContext<Node, Node>(vince, new SimpleConfig(10))).get(Integer.MAX_VALUE);

            assertEquals(2, result.size());
            assertEquals("Adam", result.get(0).getItem().getProperty("name"));
            assertEquals("Luanne", result.get(1).getItem().getProperty("name"));
            assertEquals(2.0, result.get(0).getScore().getTotalScore(), 0.001);
            assertEquals(1.0, result.get(1).getScore().getTotalScore(), 0.001);

            tx.success();
        }
    }

    @Test
    public void shouldRespectLimit() {
        String query = "MATCH (p:Person)-[:FRIEND_OF]-()-[:FRIEND_OF]-(reco) WHERE NOT (p)-[:FRIEND_OF]-(reco) AND id(p)={id} RETURN reco, count(*) as score ORDER BY score DESC limit {limit}";

        RecommendationEngine<Node, Node> engine = new CypherEngine("test engine", query);

        List<Recommendation<Node>> result;

        try (Transaction tx = getDatabase().beginTx()) {
            Node vince = getDatabase().findNode(Label.label("Person"), "name", "Vince");
            result = engine.recommend(vince, new SimpleContext<Node, Node>(vince, new SimpleConfig(1))).get(Integer.MAX_VALUE);

            assertEquals(1, result.size());
            assertEquals("Adam", result.get(0).getItem().getProperty("name"));
            assertEquals(2.0, result.get(0).getScore().getTotalScore(), 0.001);

            tx.success();
        }
    }

    @Test
    public void shouldRespectCustomParams() {
        String query = "MATCH (p:Person)-[:FRIEND_OF]-()-[:FRIEND_OF]-(customReco) WHERE NOT (p)-[:FRIEND_OF]-(customReco) AND id(p)={customId} RETURN customReco, count(*) as customScore ORDER BY customScore DESC limit {customLimit}";

        RecommendationEngine<Node, Node> engine = new CypherEngine("test engine", query) {
            @Override
            protected String idParamName() {
                return "customId";
            }

            @Override
            protected String limitParamName() {
                return "customLimit";
            }

            @Override
            protected String recoResultName() {
                return "customReco";
            }

            @Override
            protected String scoreResultName() {
                return "customScore";
            }
        };

        List<Recommendation<Node>> result;

        try (Transaction tx = getDatabase().beginTx()) {
            Node vince = getDatabase().findNode(Label.label("Person"), "name", "Vince");
            result = engine.recommend(vince, new SimpleContext<Node, Node>(vince, new SimpleConfig(1))).get(Integer.MAX_VALUE);

            assertEquals(1, result.size());
            assertEquals("Adam", result.get(0).getItem().getProperty("name"));
            assertEquals(2.0, result.get(0).getScore().getTotalScore(), 0.001);

            tx.success();
        }
    }

    @Test
    public void shouldBeAbleToUseDefaultScore() {
        String query = "MATCH (p:Person)-[:FRIEND_OF]-()-[:FRIEND_OF]-(reco) WHERE NOT (p)-[:FRIEND_OF]-(reco) AND id(p)={id} RETURN reco";
        RecommendationEngine<Node, Node> engine = new CypherEngine("test engine", query);

        List<Recommendation<Node>> result;

        try (Transaction tx = getDatabase().beginTx()) {
            Node vince = getDatabase().findNode(Label.label("Person"), "name", "Vince");
            result = engine.recommend(vince, new SimpleContext<Node, Node>(vince, new SimpleConfig(10))).get(Integer.MAX_VALUE);

            assertEquals(2, result.size());
            assertEquals("Adam", result.get(0).getItem().getProperty("name"));
            assertEquals("Luanne", result.get(1).getItem().getProperty("name"));
            assertEquals(2.0, result.get(0).getScore().getTotalScore(), 0.001);
            assertEquals(1.0, result.get(1).getScore().getTotalScore(), 0.001);

            tx.success();
        }
    }

    @Test(expected = QueryExecutionException.class)
    public void shouldFailWhenCypherQueryIsInvalid() {
        String query = "MATCH (p:Person)-[:FRIEND_OF]-(-[:FRIEND_OF]-(reco) WHERE NOT (p)-[:FRIEND_OF]-(reco) AND id(p)={id} RETURN reco";
        RecommendationEngine<Node, Node> engine = new CypherEngine("test engine", query);

        try (Transaction tx = getDatabase().beginTx()) {
            Node vince = getDatabase().findNode(Label.label("Person"), "name", "Vince");
            engine.recommend(vince, new SimpleContext<Node, Node>(vince, new SimpleConfig(10))).get(Integer.MAX_VALUE);
        }
    }

    @Test(expected = QueryExecutionException.class)
    public void shouldFailWhenCypherQueryUsesUnknownParam() {
        String query = "MATCH (p:Person)-[:FRIEND_OF]-()-[:FRIEND_OF]-(reco) WHERE NOT (p)-[:FRIEND_OF]-(reco) AND id(p)={unknown} RETURN reco";
        RecommendationEngine<Node, Node> engine = new CypherEngine("test engine", query);

        try (Transaction tx = getDatabase().beginTx()) {
            Node vince = getDatabase().findNode(Label.label("Person"), "name", "Vince");
            engine.recommend(vince, new SimpleContext<Node, Node>(vince, new SimpleConfig(10))).get(Integer.MAX_VALUE);
        }
    }

    @Test
    public void shouldGracefullyHandleNoResults() {
        String query = "MATCH (p:Person)-[:FRIEND_OF]-()-[:FRIEND_OF]-(reco) WHERE NOT (p)-[:FRIEND_OF]-(reco) AND id(p)=435234523 RETURN reco";
        RecommendationEngine<Node, Node> engine = new CypherEngine("test engine", query);

        List<Recommendation<Node>> result;

        try (Transaction tx = getDatabase().beginTx()) {
            Node vince = getDatabase().findNode(Label.label("Person"), "name", "Vince");
            result = engine.recommend(vince, new SimpleContext<Node, Node>(vince, new SimpleConfig(10))).get(Integer.MAX_VALUE);

            assertEquals(0, result.size());

            tx.success();
        }
    }
}
