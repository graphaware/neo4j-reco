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

import com.graphaware.common.policy.inclusion.NodeInclusionPolicy;
import com.graphaware.reco.generic.config.Config;
import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.engine.SingleScoreRecommendationEngine;
import com.graphaware.reco.generic.policy.ParticipationPolicy;
import com.graphaware.reco.generic.result.PartialScore;
import com.graphaware.runtime.walk.NodeSelector;
import com.graphaware.runtime.walk.RandomNodeSelector;
import org.neo4j.graphdb.Node;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link SingleScoreRecommendationEngine} that randomly recommends {@link org.neo4j.graphdb.Node}s which comply with
 * the provided {@link com.graphaware.common.policy.inclusion.NodeInclusionPolicy}.
 */
public abstract class RandomRecommendations extends SingleScoreRecommendationEngine<Node, Node> {

    private final NodeSelector selector;

    public RandomRecommendations() {
        this.selector = new RandomNodeSelector(getPolicy());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ParticipationPolicy<Node, Node> participationPolicy(Context context) {
        //noinspection unchecked
        return ParticipationPolicy.IF_MORE_RESULTS_NEEDED;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * A maximum of {@link Context#config()} {@link Config#limit()} number of nodes is returned, each with
     * a score determined by {@link #score(org.neo4j.graphdb.Node)}. The total number of attempts made to find a suitable
     * node is determined by {@link #numberOfAttempts(com.graphaware.reco.generic.context.Context)}.
     */
    @Override
    protected final Map<Node, PartialScore> doRecommendSingle(Node input, Context<Node, Node> context) {
        Map<Node, PartialScore> result = new HashMap<>();
        int attempts = 0;

        int numberOfAttempts = numberOfAttempts(context);
        int numberOfRecommendations = numberOfRecommendations(context);

        while (attempts++ < numberOfAttempts && result.size() < numberOfRecommendations) {
            Node node = selector.selectNode(input.getGraphDatabase());
            if (node != null) {
                result.put(node, score(node));
            }
        }

        return result;
    }

    /**
     * Score a randomly selected node.
     *
     * @param node to score.
     * @return score, 0 by default.
     */
    protected PartialScore score(Node node) {
        return new PartialScore();
    }

    /**
     * Determine the maximum total number of attempts to make when selecting random nodes to recommend.
     *
     * @param context of the current computation.
     * @return maximum number of attempts. By default 10 * {@link Context#config()} {@link Config#limit()}
     */
    protected int numberOfAttempts(Context<Node, Node> context) {
        return context.config().limit() * 10;
    }

    /**
     * Determine the maximum number of random nodes to recommend.
     * <p/>
     * The reason for this setting is the following: usually, this engine will be used as the last one to make up the
     * desired number of recommendations. If only {@link Context#config()} {@link Config#limit()} recommendations
     * were produced, there could be a possibility that the produced recommendations are the ones already computed by
     * previous engines, thus not making up the desired number. The higher the return value of this method, the lower
     * the chance of the desired number of recommendations not being satisfied.
     *
     * @param context of the current computation.
     * @return maximum number of recommendations. By default 2 * {@link Context#config()} {@link Config#limit()}
     */
    protected int numberOfRecommendations(Context<Node, Node> context) {
        return context.config().limit() * 2;
    }

    /**
     * Get the node inclusion policy of the nodes that can be used as recommendations.
     *
     * @return policy.
     */
    protected abstract NodeInclusionPolicy getPolicy();
}
