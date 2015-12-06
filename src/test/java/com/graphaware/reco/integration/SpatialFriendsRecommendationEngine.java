/*
 * Copyright (c) 2013-2015 GraphAware
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

package com.graphaware.reco.integration;

import java.util.Arrays;
import java.util.List;

import com.graphaware.reco.generic.engine.RecommendationEngine;
import com.graphaware.reco.generic.filter.BlacklistBuilder;
import com.graphaware.reco.generic.filter.Filter;
import com.graphaware.reco.generic.log.Logger;
import com.graphaware.reco.generic.log.Slf4jRecommendationLogger;
import com.graphaware.reco.generic.log.Slf4jStatisticsLogger;
import com.graphaware.reco.integration.engine.ClosestFriends;
import com.graphaware.reco.integration.log.RecommendationsRememberingLogger;
import com.graphaware.reco.neo4j.engine.Neo4jTopLevelDelegatingRecommendationEngine;
import com.graphaware.reco.neo4j.filter.ExcludeSelf;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;


public class SpatialFriendsRecommendationEngine extends Neo4jTopLevelDelegatingRecommendationEngine {

	final Index<Node> index;
	final ClosestFriends closestFriends;

	public SpatialFriendsRecommendationEngine(Index<Node> index) {
		this.index = index;
		this.closestFriends = new ClosestFriends(index);
	}

	@Override
	protected List<RecommendationEngine<Node, Node>> engines() {
		return Arrays.<RecommendationEngine<Node, Node>>asList(
				closestFriends
		);
	}

	@Override
	protected List<BlacklistBuilder<Node, Node>> blacklistBuilders() {
		return Arrays.<BlacklistBuilder<Node, Node>>asList(
				new ExcludeSelf()
		);
	}

	@Override
	protected List<Filter<Node, Node>> filters() {
		return Arrays.<Filter<Node, Node>>asList(
				new ExcludeSelf()
		);
	}

	@Override
	protected List<Logger<Node, Node>> loggers() {
		return Arrays.asList(
				new RecommendationsRememberingLogger(),
				new Slf4jRecommendationLogger<Node, Node>(),
				new Slf4jStatisticsLogger<Node, Node>()
		);
	}
}
