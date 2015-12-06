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
package com.graphaware.reco.neo4j.engine;

import java.util.HashMap;
import java.util.Map;

import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.engine.SingleScoreRecommendationEngine;
import com.graphaware.reco.generic.result.PartialScore;
import org.apache.commons.collections4.map.HashedMap;
import org.neo4j.gis.spatial.indexprovider.LayerNodeIndex;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

/**
 * {@link SingleScoreRecommendationEngine} that recommends {@link Node}s which are within a given geographic distance from the input.
 * <p/>
 * The distance in km is specified by {@link #getDistanceInKm()} and the spatial index is supplied by {@link #getSpatialIndex()}.
 * Only spatial indexes configured as {@see SpatialIndexProvider.SIMPLE_POINT_CONFIG} are supported.
 * <p/>
 * Every time a recommendation is found, it's score is incremented by {@link #scoreNode(Node, double)}
 */
public abstract class SpatialRecommendationEngine extends SingleScoreRecommendationEngine<Node, Node> {

	@Override
	protected Map<Node, PartialScore> doRecommendSingle(Node input, Context<Node, Node> context) {
		Map<Node, PartialScore> results = new HashedMap<>();
		double inputLatitude = (double) input.getProperty("lat");
		double inputLongitude = (double) input.getProperty("lon");
		Map<String, Object> params = new HashMap<>();
		params.put(LayerNodeIndex.POINT_PARAMETER, new Double[]{inputLatitude, inputLongitude});
		params.put(LayerNodeIndex.DISTANCE_IN_KM_PARAMETER, getDistanceInKm());

		IndexHits<Node> hits = getSpatialIndex().query(LayerNodeIndex.WITHIN_DISTANCE_QUERY, params);
		for (Node hit : hits) {
			double distance = hits.currentScore();
			addToResult(results, hit, new PartialScore(scoreNode(hit, distance)));
		}
		return results;
	}

	protected abstract Index<Node> getSpatialIndex();

	protected abstract double getDistanceInKm();

	protected float scoreNode(Node recommendation, double distance) {
		return (float) (getDistanceInKm() - distance);
	}
}
