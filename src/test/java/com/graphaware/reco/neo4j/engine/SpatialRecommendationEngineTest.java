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

package com.graphaware.reco.neo4j.engine;

import static org.junit.Assert.*;

import java.util.List;

import com.graphaware.reco.generic.config.Config;
import com.graphaware.reco.generic.context.SimpleContext;
import com.graphaware.reco.generic.engine.RecommendationEngine;
import com.graphaware.reco.generic.result.Recommendation;
import com.graphaware.test.integration.DatabaseIntegrationTest;
import org.junit.Test;
import org.neo4j.gis.spatial.indexprovider.SpatialIndexProvider;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;

/**
 * Test for {@link SpatialRecommendationEngine}.
 */
public class SpatialRecommendationEngineTest extends DatabaseIntegrationTest{

	@Override
	protected void populateDatabase(GraphDatabaseService database) {
		Index<Node> index;
		IndexManager indexMan = getDatabase().index();
		try (Transaction tx = getDatabase().beginTx()) {
			index = indexMan.forNodes("locations", SpatialIndexProvider.SIMPLE_POINT_CONFIG);
			tx.success();
		}

		database.execute(
				"CREATE " +
						"(m:Person:Male {name:'Michal', age:30, lat:51.5002588, lon:-0.0986253})," +
						"(d:Person:Female {name:'Daniela', age:20, lat:51.5002588, lon:-0.0986253})," +
						"(v:Person:Male {name:'Vince', age:40, lat:51.5003456, lon:-0.0963615})," +
						"(a:Person:Male {name:'Adam', age:30, lat:52.954783, lon:-1.158109})," +
						"(l:Person:Female {name:'Luanne', age:25, lat:19.180237, lon:72.855415})," +
						"(b:Person:Male {name:'Christophe', age:60, lat:51.209348, lon:3.2247})," +
						"(borough:Station {name:'Borough Station', lat:51.5012, lon:-0.0955287})," +
						"(glad:Pub {name:'The Gladstone Arms', lat:51.5004457, lon:-0.0963616})");

		try (Transaction tx = database.beginTx()) {
			addPersonsToSpatialLater(index, database, DynamicLabel.label("Person"), "Michal", "Daniela", "Vince", "Adam", "Luanne", "Christophe");
			addPersonsToSpatialLater(index, database, DynamicLabel.label("Station"), "Borough Station");
			addPersonsToSpatialLater(index, database, DynamicLabel.label("Pub"), "The Gladstone Arms");
			tx.success();
		}
	}

	@Test
	public void shouldRecommendPeopleNearby() {
		RecommendationEngine<Node, Node> engine = new PeopleNearby();
		try (Transaction tx = getDatabase().beginTx()) {

			Node michal = getPersonByName("Michal");
			List<Recommendation<Node>> recoForMichal = engine.recommend(getPersonByName("Michal"), new SimpleContext<Node, Node>(michal, Config.UNLIMITED)).get(Integer.MAX_VALUE);

			assertEquals(2, recoForMichal.size());
			assertEquals("Daniela", recoForMichal.get(0).getItem().getProperty("name"));
			assertEquals("Vince", recoForMichal.get(1).getItem().getProperty("name"));
			tx.success();
		}
	}

	@Test
	public void shouldRecommendEverythingNearby() {
		RecommendationEngine<Node, Node> engine = new ThingsNearby();
		try (Transaction tx = getDatabase().beginTx()) {

			Node michal = getPersonByName("Michal");
			List<Recommendation<Node>> recoForMichal = engine.recommend(getPersonByName("Michal"), new SimpleContext<Node, Node>(michal, Config.UNLIMITED)).get(Integer.MAX_VALUE);

			assertEquals(4, recoForMichal.size());
			assertEquals("Daniela", recoForMichal.get(0).getItem().getProperty("name"));
			assertEquals("Vince", recoForMichal.get(1).getItem().getProperty("name"));
			assertEquals("The Gladstone Arms", recoForMichal.get(2).getItem().getProperty("name"));
			assertEquals("Borough Station", recoForMichal.get(3).getItem().getProperty("name"));
			tx.success();
		}
	}

	private Node getPersonByName(String name) {
		return getDatabase().findNode(DynamicLabel.label("Person"), "name", name);
	}

	private static void addPersonsToSpatialLater(Index spatialIndex, GraphDatabaseService database, Label label, String... names) {
		for (String name : names) {
			Node node = database.findNode(label, "name", name);
			spatialIndex.add(node, "", "");
		}
	}
}
