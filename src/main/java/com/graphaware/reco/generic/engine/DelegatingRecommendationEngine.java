package com.graphaware.reco.generic.engine;

import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.policy.ParticipationPolicy;
import com.graphaware.reco.generic.post.PostProcessor;
import com.graphaware.reco.generic.result.Recommendations;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A {@link RecommendationEngine} that delegates to other {@link RecommendationEngine}s. Once all interested {@link RecommendationEngine}s
 * have been consulted, results are tallied and post processed using provided {@link PostProcessor}s, before being returned
 * to the caller.
 */
public class DelegatingRecommendationEngine<OUT, IN, C extends Context<OUT, IN>> implements RecommendationEngine<OUT, IN, C> {

    private final List<RecommendationEngine<OUT, IN, ? super C>> engines = new LinkedList<>();
    private final List<PostProcessor<OUT, IN>> postProcessors = new LinkedList<>();

    /**
     * Add {@link com.graphaware.reco.generic.engine.RecommendationEngine}s that this engine delegates to, in the order
     * in which they are added.
     *
     * @param engines to delegate to.
     */
    @SafeVarargs
    public final void addEngines(RecommendationEngine<OUT, IN, ? super C>... engines) {
        Collections.addAll(this.engines, engines);
    }

    /**
     * Add {@link com.graphaware.reco.generic.post.PostProcessor}s that are used to post-process recommendations once
     * computed. The post-processors are applied in the order in which they are added.
     *
     * @param postProcessors to be used.
     */
    @SafeVarargs
    public final void addPostProcessors(PostProcessor<OUT, IN>... postProcessors) {
        Collections.addAll(this.postProcessors, postProcessors);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Recommendations<OUT> recommend(IN input, C context) {
        Recommendations<OUT> recommendations = new Recommendations<>();

        for (RecommendationEngine<OUT, IN, ? super C> engine : engines) {
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
     *
     * @return {@link com.graphaware.reco.generic.policy.ParticipationPolicy#ALWAYS} by default.
     */
    @Override
    public ParticipationPolicy<OUT, IN> participationPolicy(C context) {
        //noinspection unchecked
        return ParticipationPolicy.ALWAYS;
    }
}
