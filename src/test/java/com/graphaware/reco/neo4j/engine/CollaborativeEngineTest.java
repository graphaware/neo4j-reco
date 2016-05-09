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

import com.graphaware.reco.generic.config.Config;
import com.graphaware.reco.generic.context.SimpleContext;
import com.graphaware.reco.generic.engine.RecommendationEngine;
import com.graphaware.reco.generic.result.PartialScore;
import com.graphaware.reco.generic.result.Recommendation;
import com.graphaware.reco.util.ScoreUtils;
import com.graphaware.test.integration.DatabaseIntegrationTest;
import com.graphaware.test.integration.EmbeddedDatabaseIntegrationTest;
import org.junit.Test;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.MapUtil;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class CollaborativeEngineTest extends EmbeddedDatabaseIntegrationTest {

    private RecommendationEngine<Node, Node> engine = new SkillsToLearn();

    @Override
    protected void populateDatabase(GraphDatabaseService database) {
        database.execute(
                "CREATE " +
                        "(m:Person:Male {name:'Michal', age:30})," +
                        "(v:Person:Male {name:'Vince', age:40})," +
                        "(l:Person:Female {name:'Luanne', age:25})," +
                        "(c:Person:Male {name:'Christophe', age:60})," +

                        "(php:Skill {name:'PHP'})," +
                        "(java:Skill {name:'Java'})," +
                        "(neo:Skill {name:'Neo4j'})," +
                        "(cypher:Skill {name:'Cypher'})," +
                        "(maven:Skill {name:'Maven'})," +
                        "(git:Skill {name:'Git'})," +

                        "(m)-[:FRIEND_OF]->(l)," +
                        "(m)-[:FRIEND_OF]->(v)," +
                        "(m)-[:FRIEND_OF]->(c)," +
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
    public void shouldRecommendSkills() {
        try (Transaction tx = getDatabase().beginTx()) {

            Node christophe = getPersonByName("Christophe");
            List<Recommendation<Node>> skillsForChris = engine.recommend(christophe, new SimpleContext<Node, Node>(christophe, Config.UNLIMITED)).get(Integer.MAX_VALUE);

            assertEquals(4, skillsForChris.size());
            ScoreUtils.assertScoresEqual(recommendedJava(), skillsForChris.get(0));
            ScoreUtils.assertScoresEqual(recommendedNeo4j(), skillsForChris.get(1));
            ScoreUtils.assertScoresEqual(recommendedCypher(), skillsForChris.get(2));
            ScoreUtils.assertScoresEqual(recommendedGit(), skillsForChris.get(3));
            tx.success();
        }
    }

    private Node getPersonByName(String name) {
        return getDatabase().findNode(Label.label("Person"), "name", name);
    }

    private Node getSkillByName(String name) {
        return getDatabase().findNode(Label.label("Skill"), "name", name);
    }

    private Recommendation<Node> recommendedJava() {
        Recommendation<Node> java = new Recommendation<>(getSkillByName("Java"));
        PartialScore skills = new PartialScore();
        skills.add(6.0f, MapUtil.map("person","Michal","skill","Cypher"));
        skills.add(6.0f, MapUtil.map("person","Michal","skill","Neo4j"));
        skills.add(9.0f, MapUtil.map("person","Vince","skill","Cypher"));
        skills.add(7.0f, MapUtil.map("person","Vince","skill","Neo4j"));
        java.add("skills",skills);
        return java;
    }

    private Recommendation<Node> recommendedNeo4j() {
        Recommendation<Node> neo4j = new Recommendation<>(getSkillByName("Neo4j"));
        PartialScore skills = new PartialScore();
        skills.add(8.0f, MapUtil.map("person","Michal","skill","Cypher"));
        skills.add(8.0f, MapUtil.map("person","Vince","skill","Cypher"));
        neo4j.add("skills",skills);
        return neo4j;
    }

    private Recommendation<Node> recommendedCypher() {
        Recommendation<Node> cypher = new Recommendation<>(getSkillByName("Cypher"));
        PartialScore skills = new PartialScore();
        skills.add(7.0f, MapUtil.map("person","Michal","skill","Neo4j"));
        skills.add(7.0f, MapUtil.map("person","Vince","skill","Neo4j"));
        cypher.add("skills",skills);
        return cypher;
    }

    private Recommendation<Node> recommendedGit() {
        Recommendation<Node> git = new Recommendation<>(getSkillByName("Git"));
        PartialScore skills = new PartialScore();
        skills.add(7.0f, MapUtil.map("person","Vince","skill","Cypher"));
        skills.add(5.0f, MapUtil.map("person","Vince","skill","Neo4j"));
        git.add("skills",skills);
        return git;
    }

}
