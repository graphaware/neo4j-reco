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
import com.graphaware.common.policy.inclusion.NodeInclusionPolicy;
import com.graphaware.reco.generic.config.SimpleConfig;
import com.graphaware.reco.neo4j.engine.Neo4jTopLevelDelegatingRecommendationEngine;
import com.graphaware.runtime.config.function.StringToNodeInclusionPolicy;
import com.graphaware.runtime.module.RuntimeModuleBootstrapper;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.logging.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static com.graphaware.reco.neo4j.module.RecommendationModuleConfiguration.defaultConfiguration;

/**
 * {@link RuntimeModuleBootstrapper} for {@link RecommendationModule}.
 */
public class RecommendationModuleBootstrapper implements RuntimeModuleBootstrapper {

    private static final Log LOG = LoggerFactory.getLogger(RecommendationModuleBootstrapper.class);

    private static final String ENGINE = "engine";
    private static final String NODE = "node";
    private static final String MAX_RECOMMENDATIONS = "maxRecommendations";
    private static final String REL_TYPE = "relationshipType";

    /**
     * {@inheritDoc}
     */
    @Override
    public RecommendationModule bootstrapModule(String moduleId, Map<String, String> config, GraphDatabaseService database) {
        LOG.info("Constructing new recommendation module with ID: %s", moduleId);
        LOG.debug("Configuration parameter map is: %s", config);

        RecommendationModuleConfiguration configuration = defaultConfiguration(createEngine(config));

        if (config.get(NODE) != null) {
            NodeInclusionPolicy policy = StringToNodeInclusionPolicy.getInstance().apply(config.get(NODE));
            LOG.info("Node Inclusion Policy set to %s", policy);
            configuration = configuration.with(policy);
        }

        //todo allow for an FQN of config class
        if (config.get(MAX_RECOMMENDATIONS) != null) {
            int maxRecommendations = Integer.valueOf(config.get(MAX_RECOMMENDATIONS));
            LOG.info("Max recommendations set to %s", maxRecommendations);
            configuration = configuration.withConfig(new SimpleConfig(maxRecommendations));
        }

        if (config.get(REL_TYPE) != null) {
            String type = config.get(REL_TYPE);
            LOG.info("Relationship type set to %s", type);
            configuration = configuration.withRelationshipType(DynamicRelationshipType.withName(type));
        }

        return new RecommendationModule(moduleId, configuration, database);
    }

    private Neo4jTopLevelDelegatingRecommendationEngine createEngine(Map<String, String> config) {
        return create(config, ENGINE, "Engine");
    }

    //todo replace this with framework class
    private <T> T create(Map<String, String> config, String configKey, String logString) {
        if (config.get(configKey) != null) {
            String className = config.get(configKey);

            LOG.info("Trying to instantiate class " + className);

            try {
                Class<?> cls = Class.forName(className);
                try {
                    LOG.info("Attempting to instantiate as a singleton...");
                    Method factoryMethod = cls.getDeclaredMethod("getInstance");
                    T result = (T) factoryMethod.invoke(null, null);
                    LOG.info("Success.");
                    return result;
                } catch (NoSuchMethodException | InvocationTargetException e) {
                    LOG.debug("Not a singleton.");
                }

                LOG.info("Attempting to instantiate using public no-arg constructor...");
                T result = (T) cls.newInstance();
                LOG.info("Success.");
                return result;
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
    }
}
