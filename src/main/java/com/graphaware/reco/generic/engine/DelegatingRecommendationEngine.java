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

package com.graphaware.reco.generic.engine;

import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.filter.BlacklistBuilder;
import com.graphaware.reco.generic.filter.Filter;
import com.graphaware.reco.generic.post.PostProcessor;
import com.graphaware.reco.generic.result.Recommendation;
import com.graphaware.reco.generic.result.Recommendations;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.springframework.util.Assert.notNull;

/**
 * A {@link RecommendationEngine} that delegates to other {@link RecommendationEngine}s. Once all interested {@link RecommendationEngine}s
 * have been consulted, results are tallied and post processed using provided {@link PostProcessor}s, before being returned
 * to the caller.
 */
public class DelegatingRecommendationEngine<OUT, IN> extends BaseRecommendationEngine<OUT, IN> {

    private final List<RecommendationEngine<OUT, IN>> engines = new LinkedList<>();
    private final List<PostProcessor<OUT, IN>> postProcessors = new LinkedList<>();

    public DelegatingRecommendationEngine() {
        addEngines(engines());
        addPostProcessors(postProcessors());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return "Delegating Engine @" + this.hashCode();
    }

    /**
     * Get {@link com.graphaware.reco.generic.engine.RecommendationEngine}s to be delegated to. Designed to be overridden.
     *
     * @return empty list by default.
     */
    protected List<RecommendationEngine<OUT, IN>> engines() {
        return Collections.emptyList();
    }

    /**
     * Get {@link com.graphaware.reco.generic.post.PostProcessor}s to be used by this engine. Designed to be overridden.
     *
     * @return empty list by default.
     */
    protected List<PostProcessor<OUT, IN>> postProcessors() {
        return Collections.emptyList();
    }

    /**
     * Get {@link com.graphaware.reco.generic.filter.BlacklistBuilder}s to be used by this factory. Designed to be overridden.
     *
     * @return empty list by default.
     */
    protected List<BlacklistBuilder<OUT, IN>> blacklistBuilders() {
        return Collections.emptyList();
    }

    /**
     * Get {@link com.graphaware.reco.generic.filter.Filter}s to be used by this factory. Designed to be overridden.
     *
     * @return empty list by default.
     */
    protected List<Filter<OUT, IN>> filters() {
        return Collections.emptyList();
    }

    /**
     * Add a {@link com.graphaware.reco.generic.engine.RecommendationEngine} that this engine delegates to. Delegation
     * happens in the order in which engines are added.
     *
     * @param engine to delegate to. Must not be <code>null</code>.
     */
    public final void addEngine(RecommendationEngine<OUT, IN> engine) {
        notNull(engine);
        engines.add(engine);
    }

    /**
     * Add {@link com.graphaware.reco.generic.engine.RecommendationEngine}s that this engine delegates to, in the order
     * in which they are added.
     *
     * @param engines to delegate to. Must not be <code>null</code> and all of the elements must not be <code>null</code>.
     */
    public final void addEngines(List<RecommendationEngine<OUT, IN>> engines) {
        notNull(engines);
        for (RecommendationEngine<OUT, IN> engine : engines) {
            addEngine(engine);
        }
    }

    /**
     * Add a {@link com.graphaware.reco.generic.post.PostProcessor}s that is used to post-process recommendations once
     * computed. The post-processors are applied in the order in which they are added.
     *
     * @param postProcessor to be used. Must not be <code>null</code>.
     */
    public final void addPostProcessor(PostProcessor<OUT, IN> postProcessor) {
        notNull(postProcessor);
        postProcessors.add(postProcessor);
    }

    /**
     * Add {@link com.graphaware.reco.generic.post.PostProcessor}s that are used to post-process recommendations once
     * computed. The post-processors are applied in the order in which they are added.
     *
     * @param postProcessors to be used. Must not be <code>null</code> and all of the elements must not be <code>null</code>.
     */
    public final void addPostProcessors(List<PostProcessor<OUT, IN>> postProcessors) {
        notNull(postProcessors);
        for (PostProcessor<OUT, IN> postProcessor : postProcessors) {
            addPostProcessor(postProcessor);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Recommendations<OUT> doRecommend(IN input, Context<OUT, IN> context) {
        Recommendations<OUT> recommendations = new Recommendations<>();

        for (RecommendationEngine<OUT, IN> engine : engines) {
            if (engine.participationPolicy(context).participate(input, context, recommendations)) {
                recommendations.merge(engine.recommend(input, context));
            }
        }

        removeIrrelevant(input, context, recommendations);

        for (PostProcessor<OUT, IN> postProcessor : postProcessors) {
            postProcessor.postProcess(recommendations, input, context);
        }

        return recommendations;
    }

    /**
     * Remove recommendations that have no chance of making it to the final selection, because their score will always
     * be lower than the score of the last returned recommendation, even after post processing. This is a performance
     * optimisation, so that irrelevant recommendations don't have to be post-processed.
     *
     * @param input           input to the recommendation engine. Typically the person or item recommendations are being
     *                        computed for.
     * @param context         additional information about the recommendation process.
     * @param recommendations computed so far.
     */
    private void removeIrrelevant(IN input, Context<OUT, IN> context, Recommendations<OUT> recommendations) {
        float maxRelativeChange = maxRelativeChange(input, context);

        if (Float.POSITIVE_INFINITY == maxRelativeChange) {
            return;
        }

        int i = 0;
        float minScoreInLimit = 0;
        for (Recommendation<OUT> recommendation : recommendations.get(Integer.MAX_VALUE)) {
            if (++i == context.config().limit()) {
                minScoreInLimit = recommendation.getScore().getTotalScore() - maxRelativeChange;
            } else if (i > context.config().limit() && recommendation.getScore().getTotalScore() < minScoreInLimit) {
                recommendations.remove(recommendation.getItem());
            }
        }
    }

    /**
     * Get the maximum value by which score difference between any two recommendations can be changed by post processing.
     *
     * @param input   input to the recommendation engine. Typically the person or item recommendations are being
     *                computed for.
     * @param context additional information about the recommendation process.
     * @return maximum relative change. If unknown, {@link Float#POSITIVE_INFINITY} will be returned.
     */
    private float maxRelativeChange(IN input, Context<OUT, IN> context) {
        float result = 0f;

        for (PostProcessor<OUT, IN> postProcessor : postProcessors) {
            float posInfluence = postProcessor.maxPositiveScore(input, context);
            float negInfluence = postProcessor.maxNegativeScore(input, context);

            if (posInfluence < 0) {
                throw new IllegalStateException(postProcessor + " has a negative influence score (" + posInfluence + "), should not be negative");
            }

            if (negInfluence > 0) {
                throw new IllegalStateException(postProcessor + " has a positive influence score (" + negInfluence + "), should not be positive");
            }

            if (Float.isInfinite(negInfluence) || Float.isInfinite(posInfluence)) {
                result = Float.POSITIVE_INFINITY;
                break;
            }

            result += Math.abs(negInfluence) + Math.abs(posInfluence);
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DelegatingRecommendationEngine that = (DelegatingRecommendationEngine) o;

        if (!engines.equals(that.engines)) return false;
        if (!postProcessors.equals(that.postProcessors)) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = engines.hashCode();
        result = 31 * result + postProcessors.hashCode();
        return result;
    }
}
