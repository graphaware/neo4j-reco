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

import com.graphaware.common.policy.inclusion.NodeInclusionPolicy;
import com.graphaware.common.policy.role.InstanceRolePolicy;
import com.graphaware.common.policy.role.MasterOnly;
import com.graphaware.reco.generic.config.Config;
import com.graphaware.reco.generic.config.SimpleConfig;
import com.graphaware.reco.generic.engine.TopLevelRecommendationEngine;
import com.graphaware.reco.neo4j.engine.Neo4jPrecomputedEngine;
import com.graphaware.runtime.config.BaseTimerDrivenModuleConfiguration;
import com.graphaware.runtime.policy.all.IncludeAllBusinessNodes;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.kernel.ha.com.master.Master;

/**
 * Configuration settings for the {@link RecommendationModule} with fluent interface.
 * //todo null checks in constructor
 */
public class RecommendationModuleConfiguration extends BaseTimerDrivenModuleConfiguration<RecommendationModuleConfiguration> {

    public static final RelationshipType DEFAULT_RELATIONSHIP_TYPE = Neo4jPrecomputedEngine.RECOMMEND;

    private final TopLevelRecommendationEngine<Node, Node> engine;
    private final Config config;
    private final NodeInclusionPolicy nodeInclusionPolicy;
    private final RelationshipType relationshipType;

    /**
     * {@inheritDoc}
     */
    @Override
    protected RecommendationModuleConfiguration newInstance(InstanceRolePolicy instanceRolePolicy) {
        return new RecommendationModuleConfiguration(instanceRolePolicy, getEngine(), getConfig(), getNodeInclusionPolicy(), getRelationshipType());
    }

    /**
     * Retrieves the default {@link RecommendationModuleConfiguration}, which computes 10 recommendations for all
     * (non-internal) nodes and connects them with the recommendations using a "RECOMMEND" relationship type.
     *
     * @param engine the recommendation engine that will be used to compute recommendations.
     * @return The default {@link RecommendationModuleConfiguration}
     */
    public static RecommendationModuleConfiguration defaultConfiguration(TopLevelRecommendationEngine<Node, Node> engine) {
        return new RecommendationModuleConfiguration(MasterOnly.getInstance(), engine, new SimpleConfig(10), IncludeAllBusinessNodes.getInstance(), DEFAULT_RELATIONSHIP_TYPE);
    }

    /**
     * Construct a new configuration with the given node inclusion policy, i.e. the policy that determines for which nodes
     * recommendations will be computed.
     *
     * @param nodeInclusionPolicy The {@link NodeInclusionPolicy} to use for selecting nodes
     *                            for which to compute recommendations.
     * @return new config.
     */
    public RecommendationModuleConfiguration with(NodeInclusionPolicy nodeInclusionPolicy) {
        return new RecommendationModuleConfiguration(getInstanceRolePolicy(), getEngine(), getConfig(), nodeInclusionPolicy, getRelationshipType());
    }

    /**
     * Construct a new configuration with the given maximum number of recommendations to pre-compute per node.
     *
     * @param config configuration of the computing process.
     * @return new config.
     */
    public RecommendationModuleConfiguration withConfig(Config config) {
        return new RecommendationModuleConfiguration(getInstanceRolePolicy(), getEngine(), config, getNodeInclusionPolicy(), getRelationshipType());
    }

    /**
     * Construct a new configuration with the given maximum relationship type.
     *
     * @param type relationship type of the relationship between the subject and the pre-computed recommendations.
     * @return new config.
     */
    public RecommendationModuleConfiguration withRelationshipType(RelationshipType type) {
        return new RecommendationModuleConfiguration(getInstanceRolePolicy(), getEngine(), getConfig(), getNodeInclusionPolicy(), type);
    }

    /**
     * Constructs a new {@link RecommendationModuleConfiguration} based on the given inclusion policy.
     *
     * @param instanceRolePolicy  specifies which role a machine must have in order to run the module with this configuration. Must not be <code>null</code>.
     * @param engine              the recommendation engine that will be used to compute recommendations.
     * @param config              configuration of the computing process.
     * @param nodeInclusionPolicy The {@link NodeInclusionPolicy} to use for selecting nodes to include in the rank algorithm.
     * @param relationshipType    relationship type of the relationship between the subject and the pre-computed recommendations.
     */
    private RecommendationModuleConfiguration(InstanceRolePolicy instanceRolePolicy, TopLevelRecommendationEngine<Node, Node> engine, Config config, NodeInclusionPolicy nodeInclusionPolicy, RelationshipType relationshipType) {
        super(instanceRolePolicy);
        this.engine = engine;
        this.config = config;
        this.nodeInclusionPolicy = nodeInclusionPolicy;
        this.relationshipType = relationshipType;
    }

    public TopLevelRecommendationEngine<Node, Node> getEngine() {
        return engine;
    }

    public NodeInclusionPolicy getNodeInclusionPolicy() {
        return nodeInclusionPolicy;
    }

    public Config getConfig() {
        return config;
    }

    public RelationshipType getRelationshipType() {
        return relationshipType;
    }
}
