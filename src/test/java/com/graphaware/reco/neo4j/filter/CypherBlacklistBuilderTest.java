package com.graphaware.reco.neo4j.filter;

import com.graphaware.test.integration.DatabaseIntegrationTest;
import org.junit.Test;
import org.neo4j.graphdb.*;

import java.util.Set;

import static com.graphaware.common.util.IterableUtils.getSingle;
import static org.junit.Assert.assertEquals;

/**
 * Test for {@link com.graphaware.reco.neo4j.filter.CypherBlacklistBuilder}.
 */
public class CypherBlacklistBuilderTest extends DatabaseIntegrationTest {

    private static final Label PERSON = DynamicLabel.label("Person");
    private static final Label COMPANY = DynamicLabel.label("Company");
    private static final DynamicRelationshipType WORKS_FOR = DynamicRelationshipType.withName("WORKS_FOR");

    @Test
    public void shouldBlacklistMatchingNodes() {
        try (Transaction tx = getDatabase().beginTx()) {
            Node michal = getDatabase().createNode(PERSON);
            michal.setProperty("name", "Michal");

            Node emil = getDatabase().createNode(PERSON);
            emil.setProperty("name", "Emil");
            Node ga = getDatabase().createNode(COMPANY);
            ga.setProperty("name", "GraphAware");

            Node neo = getDatabase().createNode(COMPANY);
            neo.setProperty("name", "Neo Technology");

            michal.createRelationshipTo(ga, WORKS_FOR);
            emil.createRelationshipTo(neo, WORKS_FOR);
            tx.success();
        }

        CypherBlacklistBuilder cypherBlacklist = new CypherBlacklistBuilder(getDatabase()) {
            @Override
            protected String getQuery() {
                return "MATCH (p:Person)-[:WORKS_FOR]->(c) WHERE id(p)={id} RETURN c AS blacklist";
            }
        };

        try (Transaction tx = getDatabase().beginTx()) {
            Set<Node> blacklist = cypherBlacklist.buildBlacklist(getSingle(getDatabase().findNodesByLabelAndProperty(PERSON, "name", "Michal")));

            assertEquals(1, blacklist.size());
            assertEquals("GraphAware", blacklist.iterator().next().getProperty("name"));

            blacklist = cypherBlacklist.buildBlacklist(getSingle(getDatabase().findNodesByLabelAndProperty(PERSON, "name", "Emil")));

            assertEquals(1, blacklist.size());
            assertEquals("Neo Technology", blacklist.iterator().next().getProperty("name"));

            tx.success();
        }

    }

}
