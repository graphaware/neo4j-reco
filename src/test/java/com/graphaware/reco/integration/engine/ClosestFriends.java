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

package com.graphaware.reco.integration.engine;

import com.graphaware.reco.neo4j.engine.SpatialRecommendationEngine;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;

public class ClosestFriends extends SpatialRecommendationEngine {

	final Index<Node> index;

	public ClosestFriends(Index<Node> index) {
		this.index = index;
	}

	@Override
	protected Index<Node> getSpatialIndex() {
		return index;
	}

	@Override
	protected double getDistanceInKm() {
		return 20;
	}

	@Override
	public String name() {
		return "Closest Friends";
	}
}
