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

package com.graphaware.reco.neo4j.filter;

import com.graphaware.reco.generic.config.Config;
import com.graphaware.reco.generic.filter.BlacklistBuilder;
import com.graphaware.test.integration.DatabaseIntegrationTest;
import com.graphaware.test.integration.EmbeddedDatabaseIntegrationTest;
import org.junit.Test;
import org.neo4j.graphdb.*;

import java.util.Set;

import static com.graphaware.common.util.IterableUtils.getSingle;
import static org.junit.Assert.assertEquals;

/**
 * Test for {@link com.graphaware.reco.neo4j.filter.CypherBlacklistBuilder}.
 */
public class CypherBlacklistBuilderTest extends EmbeddedDatabaseIntegrationTest {

    private static final Label PERSON = Label.label("Person");
    private static final Label COMPANY = Label.label("Company");
    private static final RelationshipType WORKS_FOR = RelationshipType.withName("WORKS_FOR");

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

        String query = "MATCH (p:Person)-[:WORKS_FOR]->(c) WHERE id(p)={id} RETURN c AS blacklist";
        BlacklistBuilder<Node, Node> blacklistBuilder = new CypherBlacklistBuilder(query);

        try (Transaction tx = getDatabase().beginTx()) {
            Set<Node> blacklist = blacklistBuilder.buildBlacklist(getSingle(getDatabase().findNodes(PERSON, "name", "Michal")), Config.UNLIMITED);

            assertEquals(1, blacklist.size());
            assertEquals("GraphAware", blacklist.iterator().next().getProperty("name"));

            blacklist = blacklistBuilder.buildBlacklist(getSingle(getDatabase().findNodes(PERSON, "name", "Emil")), Config.UNLIMITED);

            assertEquals(1, blacklist.size());
            assertEquals("Neo Technology", blacklist.iterator().next().getProperty("name"));

            tx.success();
        }
    }
}
