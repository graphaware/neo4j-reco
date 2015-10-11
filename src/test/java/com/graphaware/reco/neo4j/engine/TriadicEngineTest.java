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

package com.graphaware.reco.neo4j.engine;

import com.graphaware.common.util.IterableUtils;
import com.graphaware.reco.generic.config.Config;
import com.graphaware.reco.generic.context.SimpleContext;
import com.graphaware.reco.generic.engine.RecommendationEngine;
import com.graphaware.reco.generic.result.Recommendation;
import com.graphaware.test.integration.DatabaseIntegrationTest;
import org.junit.Test;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class TriadicEngineTest extends DatabaseIntegrationTest {

    private RecommendationEngine<Node, Node> engine = new Triadic();

    @Override
    protected void populateDatabase(GraphDatabaseService database) {
        database.execute(
                "CREATE " +
                        "(m:Person:Male {name:'Michal', age:30})," +
                        "(v:Person:Male {name:'Vince', age:40})," +
                        "(l:Person:Female {name:'Luanne', age:25})," +
                        "(c:Person:Male {name:'Christophe', age:60})," +
                        "(a:Person:Male {name:'Alessandro', age: 34})," +

                        "(php:Skill {name:'PHP'})," +
                        "(java:Skill {name:'Java'})," +
                        "(neo:Skill {name:'Neo4j'})," +
                        "(cypher:Skill {name:'Cypher'})," +
                        "(maven:Skill {name:'Maven'})," +
                        "(git:Skill {name:'Git'})," +

                        "(m)-[:FRIEND_OF]->(l)," +
                        "(m)-[:FRIEND_OF]->(v)," +
                        "(m)-[:FRIEND_OF]->(c)," +
                        "(m)-[:FRIEND_OF]->(a)," +
                        "(c)-[:FRIEND_OF]->(m)," +
                        "(a)-[:FRIEND_OF]->(m)," +
                        "(c)-[:KNOWS {level:3}]->(php)," +
                        "(c)-[:KNOWS {level:2}]->(neo)," +
                        "(c)-[:KNOWS {level:3}]->(cypher)," +
                        "(m)-[:KNOWS {level:1}]->(java)," +
                        "(m)-[:KNOWS {level:3}]->(neo)," +
                        "(m)-[:KNOWS {level:2}]->(cypher)," +
                        "(v)-[:KNOWS {level:2}]->(neo)," +
                        "(v)-[:KNOWS {level:3}]->(cypher)," +
                        "(v)-[:KNOWS {level:3}]->(java)," +
                        "(v)-[:KNOWS {level:1}]->(git)," +
                        "(l)-[:KNOWS {level:3}]->(neo)");
    }

    @Test
    public void shouldRecommendPeople(){
        try (Transaction tx = getDatabase().beginTx()) {
            Node ales = getPersonByName("Alessandro");
            List<Recommendation<Node>> friendsForAles = engine.recommend(ales, new SimpleContext<Node, Node>(ales, Config.UNLIMITED)).get(Integer.MAX_VALUE);
            assertEquals(4, friendsForAles.size());
            tx.success();

        }
    }

    private Node getPersonByName(String name) {
        return IterableUtils.getSingle(getDatabase().findNodes(DynamicLabel.label("Person"), "name", name));
    }

}
