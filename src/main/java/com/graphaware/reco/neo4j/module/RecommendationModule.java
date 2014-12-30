package com.graphaware.reco.neo4j.module;

import com.graphaware.common.util.Pair;
import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.context.ContextFactory;
import com.graphaware.reco.generic.context.Mode;
import com.graphaware.reco.generic.engine.RecommendationEngine;
import com.graphaware.reco.generic.result.Recommendations;
import com.graphaware.reco.generic.result.Score;
import com.graphaware.runtime.RuntimeRegistry;
import com.graphaware.runtime.metadata.NodeBasedContext;
import com.graphaware.runtime.module.BaseRuntimeModule;
import com.graphaware.runtime.module.TimerDrivenModule;
import com.graphaware.runtime.walk.ContinuousNodeSelector;
import com.graphaware.runtime.walk.NodeSelector;
import com.graphaware.writer.DatabaseWriter;
import org.neo4j.graphdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * {@link TimerDrivenModule} that continuously pre-computes recommendations in the graph.
 */
public class RecommendationModule extends BaseRuntimeModule implements TimerDrivenModule<NodeBasedContext> {

    private static final Logger LOG = LoggerFactory.getLogger(RecommendationModule.class);

    private final RecommendationModuleConfiguration config;
    private NodeSelector selector;
    private final DatabaseWriter databaseWriter;

    public RecommendationModule(String moduleId, RecommendationModuleConfiguration config, GraphDatabaseService database) {
        super(moduleId);
        this.config = config;
        this.databaseWriter = RuntimeRegistry.getRuntime(database).getDatabaseWriter();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeBasedContext createInitialContext(GraphDatabaseService database) {
        initializeSelectorIfNeeded(null, database);

        Node node = selector.selectNode(database);

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

        List<Pair<Node, Score>> recommendations = config.getEngine().recommend(node, Mode.BATCH, config.getMaxRecommendations());

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
            LOG.warn("Node referenced in last context with ID: {} was not found in the database.  Will start from a random node.");
            return null;
        }
    }

    private Node determineNextNode(GraphDatabaseService database) {
        return selector.selectNode(database);
    }

    private void persistRecommendations(final Node node, final List<Pair<Node, Score>> recommendations) {
        databaseWriter.write(new Runnable() {
            @Override
            public void run() {
                for (Relationship existing : node.getRelationships(config.getRelationshipType(), Direction.OUTGOING)) {
                    existing.delete();
                }

                for (Pair<Node, Score> recommendation : recommendations) {
                    Relationship created = node.createRelationshipTo(recommendation.first(), config.getRelationshipType());
                    for (String score : recommendation.second().getScores()) {
                        created.setProperty(score, recommendation.second().get(score));
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
