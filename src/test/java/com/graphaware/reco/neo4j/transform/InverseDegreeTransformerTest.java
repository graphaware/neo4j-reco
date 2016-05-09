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

package com.graphaware.reco.neo4j.transform;

import com.graphaware.reco.generic.config.SimpleConfig;
import com.graphaware.reco.generic.context.SimpleContext;
import com.graphaware.reco.generic.result.PartialScore;
import com.graphaware.reco.generic.transform.ScoreTransformer;
import com.graphaware.test.integration.DatabaseIntegrationTest;
import com.graphaware.test.integration.EmbeddedDatabaseIntegrationTest;
import org.junit.Test;
import org.neo4j.graphdb.*;

import static org.junit.Assert.assertEquals;
import static org.neo4j.graphdb.Direction.*;

public class InverseDegreeTransformerTest extends EmbeddedDatabaseIntegrationTest {

    private static final RelationshipType FRIEND_OF = RelationshipType.withName("FRIEND_OF");
    private final ScoreTransformer<Node> transformer = new InverseDegreeTransformer(RelationshipType.withName("FRIEND_OF"), BOTH);

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
                        "(x:Person:Male {name:'X', age:22})," +

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
    public void shouldCorrectlyTransformRecommendations() {
        assertEquals(1.0 / 4, getPartialScore("Vince", "Luanne", FRIEND_OF, BOTH).getValue(), 0.001);
        assertEquals(1.0 / 5, getPartialScore("Vince", "Luanne", null, BOTH).getValue(), 0.001);
        assertEquals(1.0 / 4, getPartialScore("Vince", "Luanne", FRIEND_OF, INCOMING).getValue(), 0.001);
        assertEquals(1.0 / 4, getPartialScore("Vince", "Luanne", null, INCOMING).getValue(), 0.001);
        assertEquals(1.0 / 1, getPartialScore("Vince", "Luanne", FRIEND_OF, OUTGOING).getValue(), 0.001);
        assertEquals(1.0 / 1, getPartialScore("Vince", "Luanne", null, OUTGOING).getValue(), 0.001);
        assertEquals(1.0 / 4, getPartialScore("Vince", "Luanne", FRIEND_OF, null).getValue(), 0.001);
        assertEquals(1.0 / 5, getPartialScore("Vince", "Luanne", null, null).getValue(), 0.001);
        assertEquals(1.0, getPartialScore("Luanne", "Vince", FRIEND_OF, BOTH).getValue(), 0.001);
        assertEquals(1.0, getPartialScore("X", "Vince", FRIEND_OF, BOTH).getValue(), 0.001);
        assertEquals(1.0, getPartialScore("Luanne", "X", FRIEND_OF, BOTH).getValue(), 0.001);

        PartialScore partialScore = getPartialScore("Vince", "Luanne", null, null);
        assertEquals("{value:0.19999999, {value:-0.8, DivideByDegree:5}}", partialScore.toString());
    }

    private PartialScore getPartialScore(String reco, String input, RelationshipType relType, Direction direction) {
        PartialScore result;
        try (Transaction tx = getDatabase().beginTx()) {
            Node r = getDatabase().findNode(Label.label("Person"), "name", reco);
            Node i = getDatabase().findNode(Label.label("Person"), "name", input);
            result = new InverseDegreeTransformer(relType, direction).transform(r, new PartialScore(1), new SimpleContext<Node, Object>(i, new SimpleConfig(2)));
            tx.success();
        }
        return result;
    }
}
