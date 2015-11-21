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

package com.graphaware.reco.generic.engine;

import com.graphaware.reco.generic.config.SimpleConfig;
import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.context.SimpleContext;
import com.graphaware.reco.generic.post.PostProcessor;
import com.graphaware.reco.generic.result.PartialScore;
import com.graphaware.reco.generic.result.Recommendations;
import com.graphaware.test.integration.DatabaseIntegrationTest;
import org.junit.Test;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.Iterables;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Test for {@link com.graphaware.reco.generic.engine.DelegatingRecommendationEngine}, especially the optimisation
 * part that takes place before post processing.
 */
public class DelegatingRecommendationEngineTest extends DatabaseIntegrationTest {

    private DelegatingRecommendationEngine<Node, Node> engine;
    private Node mockNode;
    private PostProcessor<Node, Node> mockPP1, mockPP2;
    private Context<Node, Node> testContext;

    @Override
    protected void populateDatabase(GraphDatabaseService database) {
        database.execute(
                "CREATE " +
                        "(m:Person {name:'Michal', n:100})," +
                        "(d:Person {name:'Daniela', n:80})," +
                        "(v:Person {name:'Vince', n:60})," +
                        "(a:Person {name:'Adam', n:40})," +
                        "(l:Person {name:'Luanne', n:20})," +
                        "(b:Person {name:'Christophe', n:0})," +
                        "(j:Person {name:'Jim', n:-20})");
    }

    public void setUp() throws Exception {
        super.setUp();

        engine = new DelegatingRecommendationEngine<>();
        engine.addEngine(new TestEngine());

        mockNode = mock(Node.class);
        when(mockNode.getGraphDatabase()).thenReturn(getDatabase());

        mockPP1 = mock(PostProcessor.class);
        mockPP2 = mock(PostProcessor.class);

        testContext = new SimpleContext<>(mockNode, new SimpleConfig(4));
    }

    @Test
    public void verifyCorrectOptimisation1() {
        when(mockPP1.maxPositiveScore(mockNode, testContext)).thenReturn(Float.POSITIVE_INFINITY);
        when(mockPP2.maxPositiveScore(mockNode, testContext)).thenReturn(Float.POSITIVE_INFINITY);
        when(mockPP1.maxNegativeScore(mockNode, testContext)).thenReturn(Float.NEGATIVE_INFINITY);
        when(mockPP2.maxNegativeScore(mockNode, testContext)).thenReturn(Float.NEGATIVE_INFINITY);

        engine.addPostProcessors(Arrays.asList(mockPP1, mockPP2));

        Recommendations<Node> result;
        try (Transaction tx = getDatabase().beginTx()) {
            result = engine.recommend(mockNode, testContext);
            tx.success();
        }

        assertEquals(7, result.size());
        verify(mockPP1).postProcess(result, mockNode, testContext);
        verify(mockPP2).postProcess(result, mockNode, testContext);
    }

    @Test
    public void verifyCorrectOptimisation2() {
        when(mockPP1.maxPositiveScore(mockNode, testContext)).thenReturn(0f);
        when(mockPP2.maxPositiveScore(mockNode, testContext)).thenReturn(Float.POSITIVE_INFINITY);
        when(mockPP1.maxNegativeScore(mockNode, testContext)).thenReturn(0f);
        when(mockPP2.maxNegativeScore(mockNode, testContext)).thenReturn(Float.NEGATIVE_INFINITY);

        engine.addPostProcessors(Arrays.asList(mockPP1, mockPP2));

        Recommendations<Node> result;
        try (Transaction tx = getDatabase().beginTx()) {
            result = engine.recommend(mockNode, testContext);
            tx.success();
        }

        assertEquals(7, result.size());
        verify(mockPP1).postProcess(result, mockNode, testContext);
        verify(mockPP2).postProcess(result, mockNode, testContext);
    }

    @Test
    public void verifyCorrectOptimisation3() {
        when(mockPP1.maxPositiveScore(mockNode, testContext)).thenReturn(0f);
        when(mockPP2.maxPositiveScore(mockNode, testContext)).thenReturn(0f);
        when(mockPP1.maxNegativeScore(mockNode, testContext)).thenReturn(0f);
        when(mockPP2.maxNegativeScore(mockNode, testContext)).thenReturn(0f);

        engine.addPostProcessors(Arrays.asList(mockPP1, mockPP2));

        Recommendations<Node> result;
        try (Transaction tx = getDatabase().beginTx()) {
            result = engine.recommend(mockNode, testContext);
            tx.success();
        }

        assertEquals(4, result.size());
        verify(mockPP1).postProcess(result, mockNode, testContext);
        verify(mockPP2).postProcess(result, mockNode, testContext);
    }

    @Test
    public void verifyCorrectOptimisation4() {
        when(mockPP1.maxPositiveScore(mockNode, testContext)).thenReturn(5f);
        when(mockPP2.maxPositiveScore(mockNode, testContext)).thenReturn(5f);
        when(mockPP1.maxNegativeScore(mockNode, testContext)).thenReturn(-5f);
        when(mockPP2.maxNegativeScore(mockNode, testContext)).thenReturn(-5f);

        engine.addPostProcessors(Arrays.asList(mockPP1, mockPP2));

        Recommendations<Node> result;
        try (Transaction tx = getDatabase().beginTx()) {
            result = engine.recommend(mockNode, testContext);
            tx.success();
        }

        assertEquals(5, result.size());
        verify(mockPP1).postProcess(result, mockNode, testContext);
        verify(mockPP2).postProcess(result, mockNode, testContext);
    }

    @Test
    public void verifyCorrectOptimisation5() {
        when(mockPP1.maxPositiveScore(mockNode, testContext)).thenReturn(4f);
        when(mockPP2.maxPositiveScore(mockNode, testContext)).thenReturn(5f);
        when(mockPP1.maxNegativeScore(mockNode, testContext)).thenReturn(-5f);
        when(mockPP2.maxNegativeScore(mockNode, testContext)).thenReturn(-5f);

        engine.addPostProcessors(Arrays.asList(mockPP1, mockPP2));

        Recommendations<Node> result;
        try (Transaction tx = getDatabase().beginTx()) {
            result = engine.recommend(mockNode, testContext);
            tx.success();
        }

        assertEquals(4, result.size());
        verify(mockPP1).postProcess(result, mockNode, testContext);
        verify(mockPP2).postProcess(result, mockNode, testContext);
    }

    @Test(expected = IllegalStateException.class)
    public void verifyIncorrectParams1() {
        when(mockPP1.maxPositiveScore(mockNode, testContext)).thenReturn(-1f);
        when(mockPP2.maxPositiveScore(mockNode, testContext)).thenReturn(0f);
        when(mockPP1.maxNegativeScore(mockNode, testContext)).thenReturn(0f);
        when(mockPP2.maxNegativeScore(mockNode, testContext)).thenReturn(0f);

        engine.addPostProcessors(Arrays.asList(mockPP1, mockPP2));

        try (Transaction tx = getDatabase().beginTx()) {
            engine.recommend(mockNode, testContext);
            tx.success();
        }
    }

    @Test(expected = IllegalStateException.class)
    public void verifyIncorrectParams2() {
        when(mockPP1.maxPositiveScore(mockNode, testContext)).thenReturn(0f);
        when(mockPP2.maxPositiveScore(mockNode, testContext)).thenReturn(0f);
        when(mockPP1.maxNegativeScore(mockNode, testContext)).thenReturn(1f);
        when(mockPP2.maxNegativeScore(mockNode, testContext)).thenReturn(0f);

        engine.addPostProcessors(Arrays.asList(mockPP1, mockPP2));

        try (Transaction tx = getDatabase().beginTx()) {
            engine.recommend(mockNode, testContext);
            tx.success();
        }
    }

    private class TestEngine extends SingleScoreRecommendationEngine<Node, Node> {

        @Override
        protected Map<Node, PartialScore> doRecommendSingle(Node input, Context<Node, Node> context) {
            Map<Node, PartialScore> result = new HashMap<>();
            for (Node node : Iterables.asResourceIterable(input.getGraphDatabase().findNodes(DynamicLabel.label("Person")))) {
                result.put(node, new PartialScore(Float.valueOf(node.getProperty("n").toString())));
            }
            return result;
        }

        @Override
        public String name() {
            return "test";
        }
    }
}
