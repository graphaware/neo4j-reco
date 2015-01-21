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

package com.graphaware.reco.integration.module;

import com.graphaware.common.util.IterableUtils;
import com.graphaware.common.util.Pair;
import com.graphaware.common.util.PropertyContainerUtils;
import com.graphaware.reco.generic.context.Mode;
import com.graphaware.reco.generic.result.Recommendation;
import com.graphaware.reco.generic.result.Score;
import com.graphaware.reco.integration.domain.Relationships;
import com.graphaware.reco.integration.engine.FriendsComputingEngine;
import com.graphaware.reco.integration.engine.FriendsRecommendationEngine;
import com.graphaware.reco.integration.log.RememberingLogger;
import com.graphaware.reco.neo4j.engine.Neo4jTopLevelDelegatingEngine;
import com.graphaware.reco.neo4j.module.RecommendationModule;
import com.graphaware.reco.neo4j.module.RecommendationModuleConfiguration;
import com.graphaware.reco.neo4j.result.RecommendationsPrinter;
import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.GraphAwareRuntimeFactory;
import com.graphaware.runtime.config.FluentRuntimeConfiguration;
import com.graphaware.runtime.schedule.FixedDelayTimingStrategy;
import com.graphaware.test.integration.WrappingServerIntegrationTest;
import org.junit.Test;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.util.List;
import java.util.Map;

import static com.graphaware.reco.neo4j.result.RecommendationsPrinter.*;
import static org.junit.Assert.assertEquals;

public class ModuleIntegrationTest extends WrappingServerIntegrationTest {

    private Neo4jTopLevelDelegatingEngine recommendationEngine;
    private RememberingLogger logger = new RememberingLogger();

    @Override
    public void setUp() throws Exception {
        super.setUp();
        recommendationEngine = new FriendsRecommendationEngine();
        logger.clear();
    }

    @Override
    protected void populateDatabase(GraphDatabaseService database) {
        new ExecutionEngine(database).execute(
                "CREATE " +
                        "(m:Person:Male {name:'Michal', age:30})," +
                        "(d:Person:Female {name:'Daniela', age:20})," +
                        "(v:Person:Male {name:'Vince', age:40})," +
                        "(a:Person:Male {name:'Adam', age:30})," +
                        "(l:Person:Female {name:'Luanne', age:25})," +
                        "(b:Person:Male {name:'Bob', age:60})," +

                        "(lon:City {name:'London'})," +
                        "(mum:City {name:'Mumbai'})," +

                        "(m)-[:FRIEND_OF]->(d)," +
                        "(m)-[:FRIEND_OF]->(l)," +
                        "(m)-[:FRIEND_OF]->(a)," +
                        "(m)-[:FRIEND_OF]->(v)," +
                        "(d)-[:FRIEND_OF]->(v)," +
                        "(b)-[:FRIEND_OF]->(v)," +
                        "(d)-[:LIVES_IN]->(lon)," +
                        "(v)-[:LIVES_IN]->(lon)," +
                        "(m)-[:LIVES_IN]->(lon)," +
                        "(l)-[:LIVES_IN]->(mum)");
    }

    @Test
    public void shouldRecommendRealTime() {
        try (Transaction tx = getDatabase().beginTx()) {
            List<Recommendation<Node>> result;

            Node vince = getPersonByName("Vince");
            result = recommendationEngine.recommend(vince, Mode.REAL_TIME, 2);

            assertEquals("" +
                    "(:Male:Person {age: 30, name: Adam}): total:19, ageDifference:-6, friendsInCommon:15, sameGender:10" + LINE_SEPARATOR +
                    "(:Female:Person {age: 25, name: Luanne}): total:8, ageDifference:-7, friendsInCommon:15",
                    RecommendationsPrinter.toString(result));

            assertEquals(logger.get(vince), RecommendationsPrinter.toString(result));

            result = recommendationEngine.recommend(getPersonByName("Adam"), Mode.REAL_TIME, 2);

            assertEquals("" +
                    "(:Male:Person {age: 40, name: Vince}): total:19, ageDifference:-6, friendsInCommon:15, sameGender:10" + LINE_SEPARATOR +
                    "(:Female:Person {age: 25, name: Luanne}): total:12, ageDifference:-3, friendsInCommon:15",
                    RecommendationsPrinter.toString(result));

            result = recommendationEngine.recommend(getPersonByName("Luanne"), Mode.REAL_TIME, 4);

            assertEquals("Daniela", result.get(0).getItem().getProperty("name"));
            assertEquals(22, result.get(0).getScore().getTotalScore());

            assertEquals("Adam", result.get(1).getItem().getProperty("name"));
            assertEquals(12, result.get(1).getScore().getTotalScore());

            assertEquals("Vince", result.get(2).getItem().getProperty("name"));
            assertEquals(8, result.get(2).getScore().getTotalScore());

            assertEquals("Bob", result.get(3).getItem().getProperty("name"));
            assertEquals(-9, result.get(3).getScore().getTotalScore());

            tx.success();
        }
    }

    @Test
    public void shouldRecommendPreComputed() throws InterruptedException {
        GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(
                getDatabase(),
                FluentRuntimeConfiguration.defaultConfiguration()
                        .withTimingStrategy(
                                FixedDelayTimingStrategy.getInstance()
                                        .withDelay(100)
                                        .withInitialDelay(100)
                        ));

        runtime.registerModule(new RecommendationModule(
                "RECO",
                RecommendationModuleConfiguration.defaultConfiguration(new FriendsComputingEngine()).withMaxRecommendations(2),
                getDatabase()));

        runtime.start();

        Thread.sleep(2000);

        try (Transaction tx = getDatabase().beginTx()) {
            List<Recommendation<Node>> result;

            result = recommendationEngine.recommend(getPersonByName("Vince"), Mode.REAL_TIME, 2);
            assertEquals("" +
                    "(:Male:Person {age: 30, name: Adam}): total:19, ageDifference:-6, friendsInCommon:15, sameGender:10" + LINE_SEPARATOR +
                    "(:Female:Person {age: 25, name: Luanne}): total:8, ageDifference:-7, friendsInCommon:15",
                    RecommendationsPrinter.toString(result));

            result = recommendationEngine.recommend(getPersonByName("Adam"), Mode.REAL_TIME, 2);

            assertEquals("" +
                    "(:Male:Person {age: 40, name: Vince}): total:19, ageDifference:-6, friendsInCommon:15, sameGender:10" + LINE_SEPARATOR +
                    "(:Female:Person {age: 25, name: Luanne}): total:12, ageDifference:-3, friendsInCommon:15",
                    RecommendationsPrinter.toString(result));

            result = recommendationEngine.recommend(getPersonByName("Luanne"), Mode.REAL_TIME, 4);

            assertEquals("Daniela", result.get(0).getItem().getProperty("name"));
            assertEquals(22, result.get(0).getScore().getTotalScore());

            assertEquals("Adam", result.get(1).getItem().getProperty("name"));
            assertEquals(12, result.get(1).getScore().getTotalScore());

            assertEquals("Vince", result.get(2).getItem().getProperty("name"));
            assertEquals(8, result.get(2).getScore().getTotalScore());

            assertEquals("Bob", result.get(3).getItem().getProperty("name"));
            assertEquals(-9, result.get(3).getScore().getTotalScore());

            tx.success();
        }
    }

    private Node getPersonByName(String name) {
        return IterableUtils.getSingle(getDatabase().findNodesByLabelAndProperty(DynamicLabel.label("Person"), "name", name));
    }
}
