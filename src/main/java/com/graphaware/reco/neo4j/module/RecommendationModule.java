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

package com.graphaware.reco.neo4j.module;

import com.graphaware.common.log.LoggerFactory;
import com.graphaware.reco.generic.result.PartialScore;
import com.graphaware.reco.generic.result.Recommendation;
import com.graphaware.runtime.RuntimeRegistry;
import com.graphaware.runtime.config.TimerDrivenModuleConfiguration;
import com.graphaware.runtime.metadata.NodeBasedContext;
import com.graphaware.runtime.module.BaseTimerDrivenModule;
import com.graphaware.runtime.module.TimerDrivenModule;
import com.graphaware.runtime.walk.ContinuousNodeSelector;
import com.graphaware.runtime.walk.NodeSelector;
import com.graphaware.writer.neo4j.Neo4jWriter;
import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;

import java.util.List;
import java.util.Map;

/**
 * {@link TimerDrivenModule} that continuously pre-computes recommendations in the graph.
 */
public class RecommendationModule extends BaseTimerDrivenModule<NodeBasedContext> {

    private static final Log LOG = LoggerFactory.getLogger(RecommendationModule.class);

    private final RecommendationModuleConfiguration config;
    private NodeSelector selector;
    private final Neo4jWriter writer;

    public RecommendationModule(String moduleId, RecommendationModuleConfiguration config, GraphDatabaseService database) {
        super(moduleId);
        this.config = config;
        this.writer = RuntimeRegistry.getRuntime(database).getDatabaseWriter();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimerDrivenModuleConfiguration getConfiguration() {
        return config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeBasedContext createInitialContext(GraphDatabaseService database) {
        Node node;
        try (Transaction tx = database.beginTx()) {
            initializeSelectorIfNeeded(null, database);
            node = selector.selectNode(database);
            tx.success();
        }

        if (node == null) {
            LOG.warn("RecommendationModule did not find a node to start with. There are no nodes matching the configuration.");
            return null;
        }

        LOG.info("Starting RecommendationModule with node " + node);
        return new NodeBasedContext(node.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeBasedContext doSomeWork(NodeBasedContext lastContext, GraphDatabaseService database) {
        initializeSelectorIfNeeded(lastContext, database);

        Node node = determineNextNode(database);

        if (node == null) {
            LOG.warn("RecommendationModule did not find a node to continue with. There are no nodes matching the configuration.");
            return lastContext;
        }

        LOG.info("Computing for " + node.getId());

        List<Recommendation<Node>> recommendations = config.getEngine().recommend(node, config.getConfig());

        persistRecommendations(node, recommendations);

        return new NodeBasedContext(node);
    }

    private void initializeSelectorIfNeeded(NodeBasedContext lastContext, GraphDatabaseService database) {
        if (selector == null) {
            long lastId = -1;
            Node lastNode = determineLastNode(lastContext, database);
            if (lastNode != null) {
                lastId = lastNode.getId();
            }

            selector = new ContinuousNodeSelector(config.getNodeInclusionPolicy(), lastId);
        }
    }

    private Node determineLastNode(NodeBasedContext lastContext, GraphDatabaseService database) {
        if (lastContext == null) {
            LOG.debug("No context found. Will start from a random node.");
            return null;
        }

        try {
            return lastContext.find(database);
        } catch (NotFoundException e) {
            LOG.warn("Node referenced in last context with ID: %s was not found in the database.  Will start from a random node.");
            return null;
        }
    }

    private Node determineNextNode(GraphDatabaseService database) {
        return selector.selectNode(database);
    }

    private void persistRecommendations(final Node node, final List<Recommendation<Node>> recommendations) {
        writer.write(new Runnable() {
            @Override
            public void run() {
                for (Relationship existing : node.getRelationships(config.getRelationshipType(), Direction.OUTGOING)) {
                    existing.delete();
                }

                for (Recommendation<Node> recommendation : recommendations) {
                    Relationship created = node.createRelationshipTo(recommendation.getItem(), config.getRelationshipType());
                    for (Map.Entry<String, PartialScore> entry : recommendation.getScore().getScoreParts().entrySet()) {
                        created.setProperty(entry.getKey(), entry.getValue().getValue());
                    }
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        //no need to do anything
    }
}
