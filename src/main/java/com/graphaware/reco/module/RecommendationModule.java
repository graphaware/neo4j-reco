package com.graphaware.reco.module;

import com.graphaware.common.util.Pair;
import com.graphaware.reco.engine.Engine;
import com.graphaware.reco.score.CompositeScore;
import com.graphaware.runtime.metadata.NodeBasedContext;
import com.graphaware.runtime.module.BaseRuntimeModule;
import com.graphaware.runtime.module.TimerDrivenModule;
import com.graphaware.runtime.walk.ContinuousNodeSelector;
import com.graphaware.runtime.walk.NodeSelector;
import com.graphaware.runtime.walk.RandomNodeSelector;
import org.neo4j.graphdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * {@link TimerDrivenModule} that continuously computes recommendations in the graph.
 */
public class RecommendationModule extends BaseRuntimeModule implements TimerDrivenModule<NodeBasedContext> {

    private static final Logger LOG = LoggerFactory.getLogger(RecommendationModule.class);

    private final RecommendationModuleConfiguration config;
    private NodeSelector selector;

    public RecommendationModule(String moduleId, RecommendationModuleConfiguration config) {
        super(moduleId);
        this.config = config;
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
            LOG.warn("NodeRank did not find a node to continue with. There are no nodes matching the configuration.");
            return lastContext;
        }

        Engine<Node, Node> engine = config.getEngine();

        LOG.info("Computing for "+node.getId());

        List<Pair<Node, CompositeScore>> recommendations = engine.recommend(node, config.getMaxRecommendations(), false);

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

    private void persistRecommendations(Node node, List<Pair<Node, CompositeScore>> recommendations) {
        for (Relationship existing : node.getRelationships(config.getRelationshipType(), Direction.OUTGOING)) {
            existing.delete();
        }

        for (Pair<Node, CompositeScore> recommendation : recommendations) {
            Relationship created = node.createRelationshipTo(recommendation.first(), config.getRelationshipType());
            for (String score : recommendation.second().getScores()) {
                created.setProperty(score, recommendation.second().get(score));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        //no need to do anything
    }
}
