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

package com.graphaware.reco.integration;

import com.graphaware.reco.generic.config.SimpleConfig;
import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.engine.RecommendationEngine;
import com.graphaware.reco.generic.policy.ParticipationPolicy;
import com.graphaware.reco.integration.engine.FriendsInCommon;
import com.graphaware.reco.integration.engine.RandomPeople;
import com.graphaware.reco.neo4j.engine.Neo4jTopLevelDelegatingRecommendationEngine;
import com.graphaware.test.integration.DatabaseIntegrationTest;
import com.graphaware.test.integration.EmbeddedDatabaseIntegrationTest;
import org.junit.Test;
import org.neo4j.graphdb.*;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TimeLimitIntegrationTest extends EmbeddedDatabaseIntegrationTest {

    private Neo4jTopLevelDelegatingRecommendationEngine recommendationEngine;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        recommendationEngine = new FriendsComputingEngine() {
            @Override
            protected List<RecommendationEngine<Node, Node>> engines() {
                return Arrays.<RecommendationEngine<Node, Node>>asList(
                        new FriendsInCommon(),
                        new RandomPeople() {
                            @Override
                            public ParticipationPolicy<Node, Node> participationPolicy(Context context) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {}
                                return super.participationPolicy(context);
                            }
                        }
                );
            }
        };
    }

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
                        "(j:Person:Male {name:'Jim', age:40})," +
                        "(r1:Person:Male {name:'Random 1', age:40})," +
                        "(r2:Person:Male {name:'Random 2', age:30})," +
                        "(r3:Person:Male {name:'Random 3', age:50})," +
                        "(r4:Person:Male {name:'Random 4', age:60})," +

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
    public void shouldRecommendRandomPeopleOnlyIfThereIsTime() {
        try (Transaction tx = getDatabase().beginTx()) {

            assertEquals(6, recommendationEngine.recommend(getPersonByName("Vince"), new SimpleConfig(10)).size());

            //now limited to 100 ms
            assertEquals(2, recommendationEngine.recommend(getPersonByName("Vince"), new SimpleConfig(10, 100)).size());

            tx.success();
        }
    }

    private Node getPersonByName(String name) {
        return getDatabase().findNode(Label.label("Person"), "name", name);
    }
}
