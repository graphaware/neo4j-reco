/*
 * Copyright (c) 2013-2015 GraphAware
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

import static org.junit.Assert.*;

import java.util.List;

import com.graphaware.reco.generic.config.SimpleConfig;
import com.graphaware.reco.generic.result.Recommendation;
import com.graphaware.reco.integration.log.RecommendationsRememberingLogger;
import com.graphaware.reco.neo4j.engine.Neo4jTopLevelDelegatingRecommendationEngine;
import com.graphaware.test.integration.WrappingServerIntegrationTest;
import org.junit.Test;
import org.neo4j.gis.spatial.indexprovider.SpatialIndexProvider;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;


public class ModuleSpatialIntegrationTest extends WrappingServerIntegrationTest {

	private Neo4jTopLevelDelegatingRecommendationEngine recommendationEngine;
	private RecommendationsRememberingLogger rememberingLogger = new RecommendationsRememberingLogger();
	private Index<Node> index;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		rememberingLogger.clear();
	}

	@Override
	protected void populateDatabase(GraphDatabaseService database) {
		IndexManager indexMan = getDatabase().index();
		try (Transaction tx = getDatabase().beginTx()) {
			index = indexMan.forNodes("locations", SpatialIndexProvider.SIMPLE_POINT_CONFIG);
			tx.success();
		}

		database.execute(
				"CREATE " +
						"(m:Person:Male {name:'Michal', age:30, lat:51.383317, lon:0.162204})," +
						"(d:Person:Female {name:'Daniela', age:20, lat:51.383317, lon:0.162204})," +
						"(v:Person:Male {name:'Vince', age:40, lat:51.476029, lon:-0.047408})," +
						"(a:Person:Male {name:'Adam', age:30, lat:52.954783, lon:-1.158109})," +
						"(l:Person:Female {name:'Luanne', age:25, lat:19.180237, lon:72.855415})," +
						"(b:Person:Male {name:'Christophe', age:60, lat:51.209348, lon:3.2247})," +
						"(j:Person:Male {name:'Jim', age:38, lat:51.530962, lon:-0.130665})," +

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
		try (Transaction tx = database.beginTx()) {
			addPersonsToSpatialLater(index, database, "Michal", "Daniela", "Vince", "Adam", "Luanne", "Christophe", "Jim");
			tx.success();
		}
		recommendationEngine = new SpatialFriendsRecommendationEngine(index);
	}

	@Test
	public void shouldRecommendRealTime() {
		try (Transaction tx = getDatabase().beginTx()) {
			//verify Michal

			List<Recommendation<Node>> recoForMichal = recommendationEngine.recommend(getPersonByName("Michal"), new SimpleConfig(2));

			assertEquals(2, recoForMichal.size());
			assertEquals("Daniela", recoForMichal.get(0).getItem().getProperty("name"));
			assertEquals("Vince", recoForMichal.get(1).getItem().getProperty("name"));
			System.out.println("rememberingLogger = " + rememberingLogger.toString(getPersonByName("Michal"), recoForMichal, null));

			tx.success();
		}
	}

	private Node getPersonByName(String name) {
		return getDatabase().findNode(DynamicLabel.label("Person"), "name", name);
	}

	private static void addPersonsToSpatialLater(Index spatialIndex, GraphDatabaseService database, String... names) {
		for (String name : names) {
			Node person = database.findNode(DynamicLabel.label("Person"), "name", name);
			spatialIndex.add(person, "", "");
		}
	}
}