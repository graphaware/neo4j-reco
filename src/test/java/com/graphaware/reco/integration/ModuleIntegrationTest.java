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

package com.graphaware.reco.integration;

import com.graphaware.common.util.IterableUtils;
import com.graphaware.reco.generic.config.SimpleConfig;
import com.graphaware.reco.generic.result.Recommendation;
import com.graphaware.reco.integration.log.RecommendationsRememberingLogger;
import com.graphaware.reco.neo4j.engine.Neo4jTopLevelDelegatingRecommendationEngine;
import com.graphaware.reco.neo4j.module.RecommendationModule;
import com.graphaware.reco.neo4j.module.RecommendationModuleConfiguration;
import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.GraphAwareRuntimeFactory;
import com.graphaware.runtime.config.FluentRuntimeConfiguration;
import com.graphaware.runtime.schedule.FixedDelayTimingStrategy;
import com.graphaware.test.integration.WrappingServerIntegrationTest;
import org.junit.Test;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ModuleIntegrationTest extends WrappingServerIntegrationTest {

    private Neo4jTopLevelDelegatingRecommendationEngine recommendationEngine;
    private RecommendationsRememberingLogger rememberingLogger = new RecommendationsRememberingLogger();

    @Override
    public void setUp() throws Exception {
        super.setUp();
        recommendationEngine = new FriendsRecommendationEngine();
        rememberingLogger.clear();
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
    public void shouldRecommendRealTime() {
        try (Transaction tx = getDatabase().beginTx()) {

            //verify Vince

            List<Recommendation<Node>> recoForVince = recommendationEngine.recommend(getPersonByName("Vince"), new SimpleConfig(2));

            String expectedForVince = "Computed recommendations for Vince: (Adam {total:41.99417, ageDifference:-5.527864, friendsInCommon:{value:27.522034, {value:1.0, name:Jim}, {value:1.0, name:Michal}, {value:25.522034, ParetoTransformationOf:2.0}}, sameGender:10.0, sameLocation:{value:10.0, {value:10.0, location:London}}}), (Luanne {total:7.856705, ageDifference:-7.0093026, friendsInCommon:{value:14.866008, {value:13.866008, ParetoTransformationOf:1.0}, {value:1.0, name:Michal}}})";

            assertEquals(expectedForVince, rememberingLogger.toString(getPersonByName("Vince"), recoForVince, null));
            assertEquals(expectedForVince, rememberingLogger.get(getPersonByName("Vince")));

            //verify Adam

            List<Recommendation<Node>> recoForAdam = recommendationEngine.recommend(getPersonByName("Adam"), new SimpleConfig(2));

            String expectedForAdam = "Computed recommendations for Adam: (Vince {total:41.99417, ageDifference:-5.527864, friendsInCommon:{value:27.522034, {value:1.0, name:Jim}, {value:1.0, name:Michal}, {value:25.522034, ParetoTransformationOf:2.0}}, sameGender:10.0, sameLocation:{value:10.0, {value:10.0, location:London}}}), (Daniela {total:19.338144, ageDifference:-5.527864, friendsInCommon:{value:14.866008, {value:13.866008, ParetoTransformationOf:1.0}, {value:1.0, name:Michal}}, sameLocation:{value:10.0, {value:10.0, location:London}}})";

            assertEquals(expectedForAdam, rememberingLogger.toString(getPersonByName("Adam"), recoForAdam, null));
            assertEquals(expectedForAdam, rememberingLogger.get(getPersonByName("Adam")));

            //verify Luanne

            List<Recommendation<Node>> recoForLuanne = recommendationEngine.recommend(getPersonByName("Luanne"), new SimpleConfig(4));

            assertEquals("Daniela", recoForLuanne.get(0).getItem().getProperty("name"));
            assertEquals(22, recoForLuanne.get(0).getScore().getTotalScore(), 0.5);

            assertEquals("Adam", recoForLuanne.get(1).getItem().getProperty("name"));
            assertEquals(12, recoForLuanne.get(1).getScore().getTotalScore(), 0.5);

            assertEquals("Jim", recoForLuanne.get(2).getItem().getProperty("name"));
            assertEquals(8, recoForLuanne.get(2).getScore().getTotalScore(), 0.5);

            assertEquals("Vince", recoForLuanne.get(3).getItem().getProperty("name"));
            assertEquals(8, recoForLuanne.get(3).getScore().getTotalScore(), 0.5);

            tx.success();
        }
    }

    @Test
    public void shouldRecommendWithLittleTime() {
        try (Transaction tx = getDatabase().beginTx()) {

            //verify Vince

            List<Recommendation<Node>> recoForVince = recommendationEngine.recommend(getPersonByName("Vince"), new SimpleConfig(10, 1));

            String expectedForVince = "Computed recommendations for Vince: (Adam {total:41.99417, ageDifference:-5.527864, friendsInCommon:{value:27.522034, {value:1.0, name:Jim}, {value:1.0, name:Michal}, {value:25.522034, ParetoTransformationOf:2.0}}, sameGender:10.0, sameLocation:{value:10.0, {value:10.0, location:London}}}), (Luanne {total:7.856705, ageDifference:-7.0093026, friendsInCommon:{value:14.866008, {value:13.866008, ParetoTransformationOf:1.0}, {value:1.0, name:Michal}}})";

            assertEquals(expectedForVince, rememberingLogger.toString(getPersonByName("Vince"), recoForVince, null));
            assertEquals(expectedForVince, rememberingLogger.get(getPersonByName("Vince")));

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
                RecommendationModuleConfiguration.defaultConfiguration(new FriendsComputingEngine()).withConfig(new SimpleConfig(2)),
                getDatabase()));

        runtime.start();

        Thread.sleep(2000);

        try (Transaction tx = getDatabase().beginTx()) {

            //verify Vince

            List<Recommendation<Node>> recoForVince = recommendationEngine.recommend(getPersonByName("Vince"), new SimpleConfig(2));

            String expectedForVince = "Computed recommendations for Vince: (Adam {total:41.99417, ageDifference:-5.527864, friendsInCommon:27.522034, sameGender:10.0, sameLocation:10.0}), (Luanne {total:7.856705, ageDifference:-7.0093026, friendsInCommon:14.866008})";

            assertEquals(expectedForVince, rememberingLogger.toString(getPersonByName("Vince"), recoForVince, null));
            assertEquals(expectedForVince, rememberingLogger.get(getPersonByName("Vince")));

            //verify Adam

            List<Recommendation<Node>> recoForAdam = recommendationEngine.recommend(getPersonByName("Adam"), new SimpleConfig(2));

            String expectedForAdam = "Computed recommendations for Adam: (Vince {total:41.99417, ageDifference:-5.527864, friendsInCommon:27.522034, sameGender:10.0, sameLocation:10.0}), (Daniela {total:19.338144, ageDifference:-5.527864, friendsInCommon:14.866008, sameLocation:10.0})";

            assertEquals(expectedForAdam, rememberingLogger.toString(getPersonByName("Adam"), recoForAdam, null));
            assertEquals(expectedForAdam, rememberingLogger.get(getPersonByName("Adam")));

            //verify Luanne

            List<Recommendation<Node>> recoForLuanne = recommendationEngine.recommend(getPersonByName("Luanne"), new SimpleConfig(4));

            assertEquals("Daniela", recoForLuanne.get(0).getItem().getProperty("name"));
            assertEquals(22, recoForLuanne.get(0).getScore().getTotalScore(), 0.5);

            assertEquals("Adam", recoForLuanne.get(1).getItem().getProperty("name"));
            assertEquals(12, recoForLuanne.get(1).getScore().getTotalScore(), 0.5);

            assertEquals("Jim", recoForLuanne.get(2).getItem().getProperty("name"));
            assertEquals(8, recoForLuanne.get(2).getScore().getTotalScore(), 0.5);

            assertEquals("Vince", recoForLuanne.get(3).getItem().getProperty("name"));
            assertEquals(8, recoForLuanne.get(3).getScore().getTotalScore(), 0.5);

            tx.success();
        }
    }

    private Node getPersonByName(String name) {
        return getDatabase().findNode(DynamicLabel.label("Person"), "name", name);
    }
}
