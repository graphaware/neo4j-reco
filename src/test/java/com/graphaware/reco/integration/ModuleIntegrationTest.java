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
import com.graphaware.reco.generic.result.PartialScore;
import com.graphaware.reco.generic.result.Recommendation;
import com.graphaware.reco.integration.log.RecommendationsRememberingLogger;
import com.graphaware.reco.neo4j.engine.Neo4jTopLevelDelegatingRecommendationEngine;
import com.graphaware.reco.neo4j.module.RecommendationModule;
import com.graphaware.reco.neo4j.module.RecommendationModuleConfiguration;
import com.graphaware.reco.util.ScoreUtils;
import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.GraphAwareRuntimeFactory;
import com.graphaware.runtime.config.FluentRuntimeConfiguration;
import com.graphaware.runtime.schedule.FixedDelayTimingStrategy;
import com.graphaware.test.integration.GraphAwareIntegrationTest;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.helpers.collection.MapUtil;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ModuleIntegrationTest extends GraphAwareIntegrationTest {

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

			assertEquals(2, recoForVince.size());
			ScoreUtils.assertScoresEqual(recommendedAdam(), recoForVince.get(0));
			ScoreUtils.assertScoresEqual(recommendedLuanne(), recoForVince.get(1));

            //verify Adam

            List<Recommendation<Node>> recoForAdam = recommendationEngine.recommend(getPersonByName("Adam"), new SimpleConfig(2));

			assertEquals(2, recoForAdam.size());
			ScoreUtils.assertScoresEqual(recommendedVince(), recoForAdam.get(0));
			ScoreUtils.assertScoresEqual(recommendedDaniela(), recoForAdam.get(1));

            //verify Luanne

            List<Recommendation<Node>> recoForLuanne = recommendationEngine.recommend(getPersonByName("Luanne"), new SimpleConfig(4));

            assertEquals("Daniela", recoForLuanne.get(0).getItem().getProperty("name"));
            assertEquals(22, recoForLuanne.get(0).getScore().getTotalScore(), 0.5);
            assertEquals(0.5, recoForLuanne.get(0).getScore().get("shortestPath"), 0.5);

            assertEquals("Adam", recoForLuanne.get(1).getItem().getProperty("name"));
            assertEquals(12, recoForLuanne.get(1).getScore().getTotalScore(), 0.5);

            assertEquals("Jim", recoForLuanne.get(2).getItem().getProperty("name"));
            assertEquals(9, recoForLuanne.get(2).getScore().getTotalScore(), 0.5);

            assertEquals("Vince", recoForLuanne.get(3).getItem().getProperty("name"));
            assertEquals(8.5, recoForLuanne.get(3).getScore().getTotalScore(), 0.5);

            tx.success();
        }
    }

    @Test
    public void shouldRecommendWithLittleTime() {
        try (Transaction tx = getDatabase().beginTx()) {

            //verify Vince

            List<Recommendation<Node>> recoForVince = recommendationEngine.recommend(getPersonByName("Vince"), new SimpleConfig(10, 1));

			assertEquals(2, recoForVince.size());
			ScoreUtils.assertScoresEqual(recommendedAdam(), recoForVince.get(0));
			ScoreUtils.assertScoresEqual(recommendedLuanne(), recoForVince.get(1));

            tx.success();
        }
    }

    @Test
    public void shouldRecommendPreComputed() throws InterruptedException {
        GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(
                getDatabase(),
                FluentRuntimeConfiguration.defaultConfiguration(getDatabase())
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

			assertEquals(2, recoForVince.size());
			ScoreUtils.assertScoresEqual(recommendedAdamPrecomputed(), recoForVince.get(0));
			ScoreUtils.assertScoresEqual(recommendedLuannePrecomputed(), recoForVince.get(1));

            //verify Adam

            List<Recommendation<Node>> recoForAdam = recommendationEngine.recommend(getPersonByName("Adam"), new SimpleConfig(2));

			assertEquals(2, recoForAdam.size());
			ScoreUtils.assertScoresEqual(recommendedVincePrecomputed(), recoForAdam.get(0));
			ScoreUtils.assertScoresEqual(recommendedDanielaPrecomputed(), recoForAdam.get(1));

            //verify Luanne

            List<Recommendation<Node>> recoForLuanne = recommendationEngine.recommend(getPersonByName("Luanne"), new SimpleConfig(4));

            assertEquals("Daniela", recoForLuanne.get(0).getItem().getProperty("name"));
            assertEquals(23, recoForLuanne.get(0).getScore().getTotalScore(), 0.5);

            assertEquals("Adam", recoForLuanne.get(1).getItem().getProperty("name"));
            assertEquals(13, recoForLuanne.get(1).getScore().getTotalScore(), 0.5);

            assertEquals("Jim", recoForLuanne.get(2).getItem().getProperty("name"));
            assertEquals(9, recoForLuanne.get(2).getScore().getTotalScore(), 0.5);

            assertEquals("Vince", recoForLuanne.get(3).getItem().getProperty("name"));
            assertEquals(9, recoForLuanne.get(3).getScore().getTotalScore(), 0.5);

            tx.success();
        }
    }

	private Node getPersonByName(String name) {
		return getDatabase().findNode(Label.label("Person"), "name", name);
	}

	private Recommendation<Node> recommendedAdam() {
		Recommendation<Node> adam = new Recommendation<>(getPersonByName("Adam"));
		adam.add("ageDifference", -5.527864f);
		adam.add("sameGender", 10.0f);
		adam.add("shortestPath", 0.5f);
		PartialScore adamsFriends = new PartialScore();
		adamsFriends.add(1.0f, MapUtil.map("name", "Jim"));
		adamsFriends.add(1.0f, MapUtil.map("name", "Michal"));
		adamsFriends.add(25.522034f, MapUtil.map("ParetoTransformationOf", 2.0f));
		adam.add("friendsInCommon", adamsFriends);
		PartialScore adamsLocation = new PartialScore();
		adamsLocation.add(10.0f, MapUtil.map("location", "London"));
		adam.add("sameLocation", adamsLocation);
		return adam;
	}

	private Recommendation<Node> recommendedLuanne() {
		Recommendation<Node> luanne = new Recommendation<>(getPersonByName("Luanne"));
		luanne.add("ageDifference", -7.0093026f);
		luanne.add("shortestPath", 0.5f);
		PartialScore luannesFriends = new PartialScore();
		luannesFriends.add(1.0f, MapUtil.map("name", "Michal"));
		luannesFriends.add(13.866008f, MapUtil.map("ParetoTransformationOf", 1.0f));
		luanne.add("friendsInCommon", luannesFriends);
		return luanne;
	}

	private Recommendation<Node> recommendedVince() {
		Recommendation<Node> vince = new Recommendation<>(getPersonByName("Vince"));
		vince.add("ageDifference", -5.527864f);
		vince.add("sameGender", 10.0f);
		vince.add("shortestPath", 0.5f);
		PartialScore vincesFriends = new PartialScore();
		vincesFriends.add(1.0f, MapUtil.map("name", "Jim"));
		vincesFriends.add(1.0f, MapUtil.map("name", "Michal"));
		vincesFriends.add(25.522034f, MapUtil.map("ParetoTransformationOf", 2.0f));
		vince.add("friendsInCommon", vincesFriends);
		PartialScore vincesLocation = new PartialScore();
		vincesLocation.add(10.0f, MapUtil.map("location", "London"));
		vince.add("sameLocation", vincesLocation);
		return vince;
	}

	private Recommendation<Node> recommendedDaniela() {
		Recommendation<Node> daniela = new Recommendation<>(getPersonByName("Daniela"));
		daniela.add("ageDifference", -5.527864f);
		daniela.add("shortestPath", 0.5f);
		PartialScore danielasFriends = new PartialScore();
		danielasFriends.add(1.0f, MapUtil.map("name", "Michal"));
		danielasFriends.add(13.866008f, MapUtil.map("ParetoTransformationOf", 1.0f));
		daniela.add("friendsInCommon", danielasFriends);
		PartialScore danielasLocation = new PartialScore();
		danielasLocation.add(10.0f, MapUtil.map("location", "London"));
		daniela.add("sameLocation", danielasLocation);
		return daniela;
	}

	private Recommendation<Node> recommendedAdamPrecomputed() {
		Recommendation<Node> adam = new Recommendation<>(getPersonByName("Adam"));
		adam.add("ageDifference", -5.527864f);
		adam.add("sameGender", 10.0f);

		//since recommendations are pre-computed, the shortest path from Vince to Adam or Luanne is via the RECOMMEND relationship

		adam.add("shortestPath", 1.0f);
		adam.add("friendsInCommon", 27.522034f);
		adam.add("sameLocation", 10.0f);
		return adam;
	}

	private Recommendation<Node> recommendedLuannePrecomputed() {
		Recommendation<Node> luanne = new Recommendation<>(getPersonByName("Luanne"));
		luanne.add("ageDifference", -7.0093026f);

		//since recommendations are pre-computed, the shortest path from Vince to Adam or Luanne is via the RECOMMEND relationship

		luanne.add("shortestPath", 1.0f);
		luanne.add("friendsInCommon", 14.866008f);
		return luanne;
	}

	private Recommendation<Node> recommendedVincePrecomputed() {
		Recommendation<Node> vince = new Recommendation<>(getPersonByName("Vince"));
		vince.add("ageDifference", -5.527864f);
		vince.add("sameGender", 10.0f);
		vince.add("shortestPath", 1.0f);
		vince.add("friendsInCommon", 27.522034f);
		vince.add("sameLocation", 10.0f);
		return vince;
	}

	private Recommendation<Node> recommendedDanielaPrecomputed() {
		Recommendation<Node> daniela = new Recommendation<>(getPersonByName("Daniela"));
		daniela.add("ageDifference", -5.527864f);
		daniela.add("shortestPath", 1.0f);
		daniela.add("friendsInCommon", 14.866008f);
		daniela.add("sameLocation", 10.0f);
		return daniela;
	}
}
