package com.graphaware.reco.neo4j.module;

import com.graphaware.common.policy.NodeInclusionPolicy;
import com.graphaware.reco.neo4j.engine.Neo4jRecommendationEngine;
import com.graphaware.runtime.config.function.StringToNodeInclusionPolicy;
import com.graphaware.runtime.module.RuntimeModuleBootstrapper;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.graphaware.reco.neo4j.module.RecommendationModuleConfiguration.defaultConfiguration;

/**
 * {@link RuntimeModuleBootstrapper} for {@link RecommendationModule}.
 */
public class RecommendationModuleBootstrapper implements RuntimeModuleBootstrapper {

    private static final Logger LOG = LoggerFactory.getLogger(RecommendationModuleBootstrapper.class);

    private static final String ENGINE = "engine";
    private static final String NODE = "node";
    private static final String MAX_RECOMMENDATIONS = "maxRecommendations";
    private static final String REL_TYPE = "relationshipType";

    /**
     * {@inheritDoc}
     */
    @Override
    public RecommendationModule bootstrapModule(String moduleId, Map<String, String> config, GraphDatabaseService database) {
        LOG.info("Constructing new recommendation module with ID: {}", moduleId);
        LOG.trace("Configuration parameter map is: {}", config);

        RecommendationModuleConfiguration configuration = defaultConfiguration(createEngine(config));

        if (config.get(NODE) != null) {
            NodeInclusionPolicy policy = StringToNodeInclusionPolicy.getInstance().apply(config.get(NODE));
            LOG.info("Node Inclusion Policy set to {}", policy);
            configuration = configuration.with(policy);
        }

        if (config.get(MAX_RECOMMENDATIONS) != null) {
            int maxRecommendations = Integer.valueOf(config.get(MAX_RECOMMENDATIONS));
            LOG.info("Max recommendations set to {}", maxRecommendations);
            configuration = configuration.withMaxRecommendations(maxRecommendations);
        }

        if (config.get(REL_TYPE) != null) {
            String type = config.get(REL_TYPE);
            LOG.info("Relationship type set to {}", type);
            configuration = configuration.withRelationshipType(DynamicRelationshipType.withName(type));
        }

        return new RecommendationModule(moduleId, configuration, database);
    }

    private Neo4jRecommendationEngine createEngine(Map<String, String> config) {
        return create(config, ENGINE, "Engine");
    }

    private <T> T create(Map<String, String> config, String configKey, String logString) {
        T result;
        if (config.get(configKey) != null) {
            String className = config.get(configKey);
            try {
                Class<?> cls = Class.forName(className);
                result = (T) cls.newInstance();
            } catch (ClassNotFoundException e) {
                LOG.error(logString + " " + className + " wasn't found on the classpath. Will not pre-compute recommendations", e);
                throw new RuntimeException(logString + " " + className + " wasn't found on the classpath. Will not pre-compute recommendations", e);
            } catch (InstantiationException | IllegalAccessException | ClassCastException e) {
                LOG.error("Could not instantiate " + logString + " " + className + ". Will not pre-compute recommendations.", e);
                throw new RuntimeException("Could not instantiate " + logString + " " + className + ". Will not pre-compute recommendations.", e);
            }
        } else {
            LOG.error("Recommendation will not be pre-computed. No " + logString + " specified!");
            throw new RuntimeException("Recommendation will not be pre-computed. No " + logString + " specified!");
        }
        return result;
    }
}
