package com.graphaware.reco.demo;

import com.graphaware.common.util.PropertyContainerUtils;
import com.graphaware.module.algo.generator.GraphGenerator;
import com.graphaware.module.algo.generator.Neo4jGraphGenerator;
import com.graphaware.module.algo.generator.config.BarabasiAlbertConfig;
import com.graphaware.module.algo.generator.config.BasicGeneratorConfig;
import com.graphaware.module.algo.generator.node.SocialNetworkNodeCreator;
import com.graphaware.module.algo.generator.relationship.BarabasiAlbertRelationshipGenerator;
import com.graphaware.module.algo.generator.relationship.SocialNetworkRelationshipCreator;
import com.graphaware.test.integration.WrappingServerIntegrationTest;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.neo4j.tooling.GlobalGraphOperations;

import java.util.Arrays;

import static com.graphaware.reco.demo.Relationships.*;
import static org.neo4j.graphdb.Direction.*;

/**
 *
 */
public class ModuleDemo extends WrappingServerIntegrationTest {

    @Test
    @Ignore
    public void demoRecommendations() throws InterruptedException {
        Thread.sleep(20000);

        try (Transaction tx = getDatabase().beginTx()) {
            for (Node person : GlobalGraphOperations.at(getDatabase()).getAllNodesWithLabel(DynamicLabel.label("Person"))) {
                printRecommendations(person);
            }
            tx.success();
        }
    }

    @Override
    protected GraphDatabaseService createDatabase() {
        return  new TestGraphDatabaseFactory().newImpermanentDatabaseBuilder()
                .loadPropertiesFromFile("src/test/resources/demo-neo4j.properties")
                .newGraphDatabase();
    }

    @Override
    protected void populateDatabase(GraphDatabaseService database) {
        GraphGenerator generator = new Neo4jGraphGenerator(database);
        generator.generateGraph(new BasicGeneratorConfig(
                new BarabasiAlbertRelationshipGenerator(new BarabasiAlbertConfig(1000, 3)),
                SocialNetworkNodeCreator.getInstance(),
                SocialNetworkRelationshipCreator.getInstance()
        ));
    }

    private void printRecommendations(Node node) {
        StringBuilder s = new StringBuilder(node.getProperty("name").toString()).append("(").append(Arrays.toString(Iterables.toArray(Label.class, node.getLabels()))).append("):");
        for (Relationship reco : node.getRelationships(RECOMMEND, OUTGOING)) {
            s.append(" ").append(reco.getEndNode().getProperty("name").toString()).append("(");
            s.append(PropertyContainerUtils.propertiesToString(reco));
            s.append("),");
        }
        System.out.println(s.toString()+" Degree: "+ node.getDegree());
    }
}
