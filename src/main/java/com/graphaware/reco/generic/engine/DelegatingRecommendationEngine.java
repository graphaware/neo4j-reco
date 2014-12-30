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
public class DelegatingRecommendationEngine<OUT, IN> implements RecommendationEngine<OUT, IN> {

    private final List<RecommendationEngine<OUT, IN>> engines = new LinkedList<>();
    private final List<PostProcessor<OUT, IN>> postProcessors = new LinkedList<>();

    @SafeVarargs
    public final void addEngines(RecommendationEngine<OUT, IN>... engines) {
        Collections.addAll(this.engines, engines);
    }

    @SafeVarargs
    public final void addPostProcessors(PostProcessor<OUT, IN>... postProcessors) {
        Collections.addAll(this.postProcessors, postProcessors);
    }

    @Override
    public Recommendations<OUT> recommend(IN input, Context<OUT, IN> context) {
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

    @Override
    public ParticipationPolicy<OUT, IN> participationPolicy(Context context) {
        return ParticipationPolicy.ALWAYS;
    }
}
