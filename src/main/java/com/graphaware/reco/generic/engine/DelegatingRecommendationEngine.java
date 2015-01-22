/*
 * Copyright (c) 2015 GraphAware
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

package com.graphaware.reco.generic.engine;

import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.policy.ParticipationPolicy;
import com.graphaware.reco.generic.post.PostProcessor;
import com.graphaware.reco.generic.result.Recommendations;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.springframework.util.Assert.*;

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

        for (PostProcessor<OUT, IN> postProcessor : postProcessors) {
            postProcessor.postProcess(recommendations, input);
        }

        return recommendations;
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
