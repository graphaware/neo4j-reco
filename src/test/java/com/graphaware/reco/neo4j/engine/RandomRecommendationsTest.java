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

import com.graphaware.common.policy.inclusion.NodeInclusionPolicy;
import com.graphaware.common.policy.inclusion.all.IncludeAllNodes;
import com.graphaware.common.policy.inclusion.none.IncludeNoNodes;
import com.graphaware.reco.generic.config.SimpleConfig;
import com.graphaware.reco.generic.context.SimpleContext;
import com.graphaware.reco.generic.engine.RecommendationEngine;
import com.graphaware.reco.generic.result.Recommendation;
import com.graphaware.test.integration.EmbeddedDatabaseIntegrationTest;
import org.junit.Test;
import org.neo4j.graphdb.*;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link RandomRecommendations}.
 */
public class RandomRecommendationsTest extends EmbeddedDatabaseIntegrationTest {

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
    public void shouldReturnRandomRecommendation() {
        RecommendationEngine<Node, Node> engine = new RandomRecommendations() {

            @Override
            protected NodeInclusionPolicy getPolicy() {
                return IncludeAllNodes.getInstance();
            }

            @Override
            public String name() {
                return "test";
            }
        };

        List<Recommendation<Node>> result;

        try (Transaction tx = getDatabase().beginTx()) {
            Node vince = getDatabase().findNode(Label.label("Person"), "name", "Vince");
            result = engine.recommend(vince, new SimpleContext<Node, Node>(vince, new SimpleConfig(10))).get(Integer.MAX_VALUE);

            assertFalse(result.isEmpty());

            tx.success();
        }
    }

    @Test //issue #7
    public void shouldNotReturnAnythingIfImpossible() {
        RecommendationEngine<Node, Node> engine = new RandomRecommendations() {

            @Override
            protected NodeInclusionPolicy getPolicy() {
                return IncludeNoNodes.getInstance();
            }

            @Override
            public String name() {
                return "test";
            }
        };

        List<Recommendation<Node>> result;

        try (Transaction tx = getDatabase().beginTx()) {
            Node vince = getDatabase().findNode(Label.label("Person"), "name", "Vince");
            result = engine.recommend(vince, new SimpleContext<Node, Node>(vince, new SimpleConfig(10))).get(Integer.MAX_VALUE);

            assertTrue(result.isEmpty());

            tx.success();
        }
    }
}
