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

import com.graphaware.reco.neo4j.context.Neo4jContextFactory;
import com.graphaware.reco.neo4j.filter.ExcludeSelf;
import com.graphaware.reco.neo4j.filter.ExistingRelationshipBlacklistBuilder;

import static com.graphaware.reco.integration.domain.Relationships.FRIEND_OF;
import static org.neo4j.graphdb.Direction.BOTH;

/**
 *
 */
public final class FriendsRecommendationContextFactory extends Neo4jContextFactory {

    private static final FriendsRecommendationContextFactory INSTANCE = new FriendsRecommendationContextFactory();

    public static FriendsRecommendationContextFactory getInstance() {
        return INSTANCE;
    }

    private FriendsRecommendationContextFactory() {
        addBlacklistBuilders(
                new ExcludeSelf(),
                new ExistingRelationshipBlacklistBuilder(FRIEND_OF, BOTH));

        addFilters(
                new ExcludeSelf());
    }
}
