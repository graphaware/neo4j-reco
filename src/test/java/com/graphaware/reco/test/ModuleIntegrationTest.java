package com.graphaware.reco.test;

import com.graphaware.common.util.IterableUtils;
import com.graphaware.common.util.Pair;
import com.graphaware.common.util.PropertyContainerUtils;
import com.graphaware.reco.engine.Engine;
import com.graphaware.reco.module.RecommendationModule;
import com.graphaware.reco.module.RecommendationModuleConfiguration;
import com.graphaware.reco.score.CompositeScore;
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

import static com.graphaware.reco.demo.Relationships.RECOMMEND;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class ModuleIntegrationTest extends WrappingServerIntegrationTest {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private Engine<Node, Node> recommendationEngine;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        recommendationEngine = new FriendsRecommendationEngine();
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
            List<Pair<Node, CompositeScore>> result;

            result = recommendationEngine.recommend(getPersonByName("Vince"), 2, true);
            assertEquals("" +
                    "(:Male:Person {age: 30, name: Adam}): total:19, friendInCommon:15, ageDifference:-6, sameGender:10" + LINE_SEPARATOR +
                    "(:Female:Person {age: 25, name: Luanne}): total:8, friendInCommon:15, ageDifference:-7",
                    toString(result));

            result = recommendationEngine.recommend(getPersonByName("Adam"), 2, true);

            assertEquals("" +
                    "(:Male:Person {age: 40, name: Vince}): total:19, friendInCommon:15, ageDifference:-6, sameGender:10" + LINE_SEPARATOR +
                    "(:Female:Person {age: 25, name: Luanne}): total:12, friendInCommon:15, ageDifference:-3",
                    toString(result));

            result = recommendationEngine.recommend(getPersonByName("Luanne"), 4, true);

            assertEquals("" +
                    "(:Female:Person {age: 20, name: Daniela}): total:22, friendInCommon:15, random:0, ageDifference:-3, sameGender:10" + LINE_SEPARATOR +
                    "(:Male:Person {age: 30, name: Adam}): total:12, friendInCommon:15, random:0, ageDifference:-3" + LINE_SEPARATOR +
                    "(:Male:Person {age: 40, name: Vince}): total:8, friendInCommon:15, random:0, ageDifference:-7" + LINE_SEPARATOR +
                    "(:Male:Person {age: 60, name: Bob}): total:-9, random:0, ageDifference:-9", toString(result));

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
                RecommendationModuleConfiguration.defaultConfiguration(new FriendsRecommendationEngine())
                        .withMaxRecommendations(2)
                        .withRelationshipType(RECOMMEND),
                getDatabase()));

        runtime.start();

        Thread.sleep(2000);

        try (Transaction tx = getDatabase().beginTx()) {
            List<Pair<Node, CompositeScore>> result;

            result = recommendationEngine.recommend(getPersonByName("Vince"), 2, true);
            assertEquals("" +
                    "(:Male:Person {age: 30, name: Adam}): total:19, friendInCommon:15, ageDifference:-6, preComputed:0, sameGender:10" + LINE_SEPARATOR +
                    "(:Female:Person {age: 25, name: Luanne}): total:8, friendInCommon:15, ageDifference:-7, preComputed:0",
                    toString(result));

            result = recommendationEngine.recommend(getPersonByName("Adam"), 2, true);

            assertEquals("" +
                    "(:Male:Person {age: 40, name: Vince}): total:19, friendInCommon:15, ageDifference:-6, preComputed:0, sameGender:10" + LINE_SEPARATOR +
                    "(:Female:Person {age: 25, name: Luanne}): total:12, friendInCommon:15, ageDifference:-3, preComputed:0",
                    toString(result));

            result = recommendationEngine.recommend(getPersonByName("Luanne"), 4, true);

            assertEquals("" +
                    "(:Female:Person {age: 20, name: Daniela}): total:22, friendInCommon:15, ageDifference:-3, preComputed:0, sameGender:10" + LINE_SEPARATOR +
                    "(:Male:Person {age: 30, name: Adam}): total:12, friendInCommon:15, ageDifference:-3, preComputed:0" + LINE_SEPARATOR +
                    "(:Male:Person {age: 40, name: Vince}): total:8, friendInCommon:15, random:0, ageDifference:-7" + LINE_SEPARATOR +
                    "(:Male:Person {age: 60, name: Bob}): total:-9, random:0, ageDifference:-9",
                    toString(result));

            tx.success();
        }
    }

    private Node getPersonByName(String name) {
        return IterableUtils.getSingle(getDatabase().findNodesByLabelAndProperty(DynamicLabel.label("Person"), "name", name));
    }

    private String toString(List<Pair<Node, CompositeScore>> recommendations) {
        StringBuilder s = new StringBuilder();
        for (Pair<Node, CompositeScore> pair : recommendations) {
            Node node = pair.first();
            CompositeScore score = pair.second();
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
