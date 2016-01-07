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
package com.graphaware.reco.neo4j.engine;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.graphaware.common.util.IterableUtils;
import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.engine.SingleScoreRecommendationEngine;
import com.graphaware.reco.generic.result.PartialScore;
import org.neo4j.gis.spatial.indexprovider.LayerNodeIndex;
import org.neo4j.gis.spatial.indexprovider.SpatialIndexProvider;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

/**
 * {@link SingleScoreRecommendationEngine} that recommends {@link Node}s which are within a given geographic distance from the input.
 * <p/>
 * The distance in km is specified by {@link #getDistanceInKm()} and the spatial index is supplied by {@link #getSpatialIndex(GraphDatabaseService)}.
 * Only spatial indexes configured as {@see SpatialIndexProvider.SIMPLE_POINT_CONFIG} are supported.
 * <p/>
 * Every time a recommendation is found, it's score is incremented by {@link #scoreNode(Node, double)}
 */
public abstract class SpatialRecommendationEngine extends SingleScoreRecommendationEngine<Node, Node> {
	private final String LAT = SpatialIndexProvider.SIMPLE_POINT_CONFIG.get("lat");
	private final String LON = SpatialIndexProvider.SIMPLE_POINT_CONFIG.get("lon");
	private Index<Node> spatialIndex = null;

	@Override
	protected Map<Node, PartialScore> doRecommendSingle(Node input, Context<Node, Node> context) {

		if (spatialIndex() == null) { //Not going to synchronize this because getting the index is cheap
			spatialIndex = getSpatialIndex(input.getGraphDatabase());
		}

		Map<Node, PartialScore> results = new HashMap<>();
		double inputLatitude = (double) input.getProperty(LAT);
		double inputLongitude = (double) input.getProperty(LON);

		Map<String, Object> params = new HashMap<>();
		params.put(LayerNodeIndex.POINT_PARAMETER, new Double[]{inputLatitude, inputLongitude});
		params.put(LayerNodeIndex.DISTANCE_IN_KM_PARAMETER, getDistanceInKm());

		IndexHits<Node> hits = spatialIndex.query(LayerNodeIndex.WITHIN_DISTANCE_QUERY, params);
		for (Node hit : hits) {
			if((hit.getId() != input.getId()) && (getLabels().size() == 0 || !Collections.disjoint(getLabels(), IterableUtils.toList(hit.getLabels())))) {
				double distance = hits.currentScore();
				addToResult(results, hit, new PartialScore(scoreNode(hit, distance), Collections.<String, Object>singletonMap("Distance from input (km)", distance)));
			}
		}
		return results;
	}

	/**
	 * Get the spatial index to use. Only spatial indexes configured as {@see SpatialIndexProvider.SIMPLE_POINT_CONFIG} are supported.
	 * @param database the GraphDatabaseService
	 * @return spatial index
	 */
	protected abstract Index<Node> getSpatialIndex(GraphDatabaseService database);

	/**
	 * Get the radius (in km) within which items are recommended.
	 * @return the distance in km
	 */
	protected abstract double getDistanceInKm();

	/**
	 * Scores a recommended node
	 * @param recommendation 	the recommended node
	 * @param distance			the distance away from the input
	 * @return	the score of the recommended node
	 */
	protected float scoreNode(Node recommendation, double distance) {
		return (float) (getDistanceInKm() - distance);
	}

	/**
	 * Get a set of labels that the recommended node must have at least one of.
	 * The default is any label. This method must not return null.
	 * @return set of acceptable labels
	 */
	protected Set<Label> getLabels() {
		return Collections.EMPTY_SET;
	}

	private Index<Node> spatialIndex() {
		return spatialIndex;
	}
}
