/*
 * Copyright (c) 2014 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.reco.integration.engine;

import com.graphaware.reco.integration.domain.Relationships;
import com.graphaware.reco.neo4j.engine.Neo4jPrecomputedEngine;
import org.neo4j.graphdb.RelationshipType;


public final class PrecomputedFriendsRecommendationEngine extends Neo4jPrecomputedEngine {

    private static final PrecomputedFriendsRecommendationEngine INSTANCE = new PrecomputedFriendsRecommendationEngine();

    public static PrecomputedFriendsRecommendationEngine getInstance() {
        return INSTANCE;
    }

    private PrecomputedFriendsRecommendationEngine() {
    }

    @Override
    protected RelationshipType getType() {
        return Relationships.RECOMMEND;
    }
}
