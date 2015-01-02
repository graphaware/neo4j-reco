package com.graphaware.reco.neo4j.module;

import com.graphaware.common.policy.NodeInclusionPolicy;
import com.graphaware.reco.generic.engine.PrecomputedEngine;
import com.graphaware.reco.generic.engine.TopLevelRecommendationEngine;
import com.graphaware.reco.neo4j.engine.Neo4jPrecomputedEngine;
import com.graphaware.reco.neo4j.engine.Neo4jTopLevelDelegatingEngine;
import com.graphaware.runtime.policy.all.IncludeAllBusinessNodes;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;

/**
 * Configuration settings for the {@link RecommendationModule} with fluent interface.
 */
public class RecommendationModuleConfiguration {

    public static final RelationshipType DEFAULT_RELATIONSHIP_TYPE = Neo4jPrecomputedEngine.RECOMMEND;

    private final TopLevelRecommendationEngine<Node, Node> engine;
    private final NodeInclusionPolicy nodeInclusionPolicy;
    private final int maxRecommendations;
    private final RelationshipType relationshipType;

    /**
     * Retrieves the default {@link RecommendationModuleConfiguration}, which computes 10 recommendations for all
     * (non-internal) nodes and connects them with the recommendations using a "RECOMMEND" relationship type.
     *
     * @param engine the recommendation engine that will be used to compute recommendations.
     * @return The default {@link RecommendationModuleConfiguration}
     */
    public static RecommendationModuleConfiguration defaultConfiguration(TopLevelRecommendationEngine<Node, Node> engine) {
        return new RecommendationModuleConfiguration(engine, IncludeAllBusinessNodes.getInstance(), 10, DEFAULT_RELATIONSHIP_TYPE);
    }

    /**
     * Construct a new configuration with the given node inclusion policy, i.e. the policy that determines for which nodes
     * recommendations will be computed.
     *
     * @param nodeInclusionPolicy The {@link com.graphaware.common.policy.NodeInclusionPolicy} to use for selecting nodes
     *                            for which to compute recommendations.
     * @return new config.
     */
    public RecommendationModuleConfiguration with(NodeInclusionPolicy nodeInclusionPolicy) {
        return new RecommendationModuleConfiguration(getEngine(), nodeInclusionPolicy, getMaxRecommendations(), getRelationshipType());
    }

    /**
     * Construct a new configuration with the given maximum number of recommendations to pre-compute per node.
     *
     * @param maxRecommendations maximum number of recommendations to pre-compute per node.
     * @return new config.
     */
    public RecommendationModuleConfiguration withMaxRecommendations(int maxRecommendations) {
        return new RecommendationModuleConfiguration(getEngine(), getNodeInclusionPolicy(), maxRecommendations, getRelationshipType());
    }

    /**
     * Construct a new configuration with the given maximum relationship type.
     *
     * @param type relationship type of the relationship between the subject and the pre-computed recommendations.
     * @return new config.
     */
    public RecommendationModuleConfiguration withRelationshipType(RelationshipType type) {
        return new RecommendationModuleConfiguration(getEngine(), getNodeInclusionPolicy(), getMaxRecommendations(), type);
    }

    /**
     * Constructs a new {@link RecommendationModuleConfiguration} based on the given inclusion policy.
     *
     * @param engine              the recommendation engine that will be used to compute recommendations.
     * @param nodeInclusionPolicy The {@link com.graphaware.common.policy.NodeInclusionPolicy} to use for selecting nodes to include in the rank algorithm.
     * @param maxRecommendations  maximum number of recommendations to pre-compute per node.
     * @param relationshipType    relationship type of the relationship between the subject and the pre-computed recommendations.
     */
    private RecommendationModuleConfiguration(TopLevelRecommendationEngine<Node, Node> engine, NodeInclusionPolicy nodeInclusionPolicy, int maxRecommendations, RelationshipType relationshipType) {
        this.engine = engine;
        this.nodeInclusionPolicy = nodeInclusionPolicy;
        this.maxRecommendations = maxRecommendations;
        this.relationshipType = relationshipType;
    }

    public TopLevelRecommendationEngine<Node, Node> getEngine() {
        return engine;
    }

    public NodeInclusionPolicy getNodeInclusionPolicy() {
        return nodeInclusionPolicy;
    }

    public int getMaxRecommendations() {
        return maxRecommendations;
    }

    public RelationshipType getRelationshipType() {
        return relationshipType;
    }
}
