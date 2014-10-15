package com.graphaware.reco.module;

import com.graphaware.common.policy.NodeInclusionPolicy;
import com.graphaware.common.policy.RelationshipInclusionPolicy;
import com.graphaware.reco.engine.Engine;
import com.graphaware.runtime.policy.all.IncludeAllBusinessNodes;
import com.graphaware.runtime.policy.all.IncludeAllBusinessRelationships;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;

/**
 * Configuration settings for the {@link RecommendationModule} with fluent interface.
 */
public class RecommendationModuleConfiguration {

    public static final DynamicRelationshipType DEFAULT_RELATIONSHIP_TYPE = DynamicRelationshipType.withName("_RECOMMEND_");

    private final Engine<Node, Node> engine;
    private final NodeInclusionPolicy nodeInclusionPolicy;
    private final int maxRecommendations;
    private final RelationshipType relationshipType;

    /**
     * Retrieves the default {@link RecommendationModuleConfiguration}, which computes 10 recommendations for all
     * (non-internal) nodes and connects them with the recommendations using a "_RECOMMEND_" relationship type.
     *
     * @param engine the recommendation engine that will be used to compute recommendations.
     * @return The default {@link RecommendationModuleConfiguration}
     */
    public static RecommendationModuleConfiguration defaultConfiguration(Engine<Node, Node> engine) {
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
    private RecommendationModuleConfiguration(Engine<Node, Node> engine, NodeInclusionPolicy nodeInclusionPolicy, int maxRecommendations, RelationshipType relationshipType) {
        this.engine = engine;
        this.nodeInclusionPolicy = nodeInclusionPolicy;
        this.maxRecommendations = maxRecommendations;
        this.relationshipType = relationshipType;
    }

    public Engine<Node, Node> getEngine() {
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
