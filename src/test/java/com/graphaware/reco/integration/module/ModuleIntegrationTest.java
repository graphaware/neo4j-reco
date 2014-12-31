package com.graphaware.reco.integration.module;

import com.graphaware.common.util.IterableUtils;
import com.graphaware.common.util.Pair;
import com.graphaware.common.util.PropertyContainerUtils;
import com.graphaware.reco.generic.context.Mode;
import com.graphaware.reco.generic.result.Score;
import com.graphaware.reco.integration.domain.Relationships;
import com.graphaware.reco.integration.engine.ComputingFriendsRecommendationEngine;
import com.graphaware.reco.integration.engine.FriendsRecommendationEngine;
import com.graphaware.reco.neo4j.engine.Neo4jRecommendationEngine;
import com.graphaware.reco.neo4j.module.RecommendationModule;
import com.graphaware.reco.neo4j.module.RecommendationModuleConfiguration;
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

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class ModuleIntegrationTest extends WrappingServerIntegrationTest {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private Neo4jRecommendationEngine recommendationEngine;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        recommendationEngine = FriendsRecommendationEngine.getInstance();
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
            List<Pair<Node, Score>> result;

            result = recommendationEngine.recommend(getPersonByName("Vince"), Mode.REAL_TIME, 2);
            assertEquals("" +
                            "(:Male:Person {age: 30, name: Adam}): total:19, ageDifference:-6, friendsInCommon:15, sameGender:10" + LINE_SEPARATOR +
                            "(:Female:Person {age: 25, name: Luanne}): total:8, ageDifference:-7, friendsInCommon:15",
                    toString(result));

            result = recommendationEngine.recommend(getPersonByName("Adam"), Mode.REAL_TIME, 2);

            assertEquals("" +
                            "(:Male:Person {age: 40, name: Vince}): total:19, ageDifference:-6, friendsInCommon:15, sameGender:10" + LINE_SEPARATOR +
                            "(:Female:Person {age: 25, name: Luanne}): total:12, ageDifference:-3, friendsInCommon:15",
                    toString(result));

            result = recommendationEngine.recommend(getPersonByName("Luanne"), Mode.REAL_TIME, 4);

            assertEquals("Daniela", result.get(0).first().getProperty("name"));
            assertEquals(22, result.get(0).second().get());

            assertEquals("Adam", result.get(1).first().getProperty("name"));
            assertEquals(12, result.get(1).second().get());

            assertEquals("Vince", result.get(2).first().getProperty("name"));
            assertEquals(8, result.get(2).second().get());

            assertEquals("Bob", result.get(3).first().getProperty("name"));
            assertEquals(-9, result.get(3).second().get());

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
                RecommendationModuleConfiguration.defaultConfiguration(ComputingFriendsRecommendationEngine.getInstance())
                        .withMaxRecommendations(2)
                        .withRelationshipType(Relationships.RECOMMEND),
                getDatabase()));

        runtime.start();

        Thread.sleep(2000);

        try (Transaction tx = getDatabase().beginTx()) {
            List<Pair<Node, Score>> result;

            result = recommendationEngine.recommend(getPersonByName("Vince"), Mode.REAL_TIME, 2);
            assertEquals("" +
                            "(:Male:Person {age: 30, name: Adam}): total:19, ageDifference:-6, friendsInCommon:15, sameGender:10" + LINE_SEPARATOR +
                            "(:Female:Person {age: 25, name: Luanne}): total:8, ageDifference:-7, friendsInCommon:15",
                    toString(result));

            result = recommendationEngine.recommend(getPersonByName("Adam"), Mode.REAL_TIME, 2);

            assertEquals("" +
                            "(:Male:Person {age: 40, name: Vince}): total:19, ageDifference:-6, friendsInCommon:15, sameGender:10" + LINE_SEPARATOR +
                            "(:Female:Person {age: 25, name: Luanne}): total:12, ageDifference:-3, friendsInCommon:15",
                    toString(result));

            result = recommendationEngine.recommend(getPersonByName("Luanne"), Mode.REAL_TIME, 4);

            assertEquals("Daniela", result.get(0).first().getProperty("name"));
            assertEquals(22, result.get(0).second().get());

            assertEquals("Adam", result.get(1).first().getProperty("name"));
            assertEquals(12, result.get(1).second().get());

            assertEquals("Vince", result.get(2).first().getProperty("name"));
            assertEquals(8, result.get(2).second().get());

            assertEquals("Bob", result.get(3).first().getProperty("name"));
            assertEquals(-9, result.get(3).second().get());

            tx.success();
        }
    }

    private Node getPersonByName(String name) {
        return IterableUtils.getSingle(getDatabase().findNodesByLabelAndProperty(DynamicLabel.label("Person"), "name", name));
    }

    private String toString(List<Pair<Node, Score>> recommendations) {
        StringBuilder s = new StringBuilder();
        for (Pair<Node, Score> pair : recommendations) {
            Node node = pair.first();
            Score score = pair.second();
            s.append(PropertyContainerUtils.nodeToString(node)).append(": ");
            s.append("total:").append(score.get());
            for (String scoreName : score.getScores()) {
                s.append(", ");
                s.append(scoreName).append(":").append(score.get(scoreName));
            }
            s.append(LINE_SEPARATOR);
        }

        String result = s.toString();
        if (result.isEmpty()) {
            return result;
        }
        return result.substring(0, result.length() - LINE_SEPARATOR.length());
    }
}
