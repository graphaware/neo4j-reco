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
public class DelegatingRecommendationEngine<OUT, IN> implements RecommendationEngine<OUT, IN> {

    private final List<RecommendationEngine<OUT, IN>> engines = new LinkedList<>();
    private final List<PostProcessor<OUT, IN>> postProcessors = new LinkedList<>();

    public DelegatingRecommendationEngine() {
        addEngines(engines());
        addPostProcessors(postProcessors());
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

    /**
     * {@inheritDoc}
     *
     * @return {@link com.graphaware.reco.generic.policy.ParticipationPolicy#ALWAYS} by default.
     */
    @Override
    public ParticipationPolicy<OUT, IN> participationPolicy(Context<OUT, IN> context) {
        //noinspection unchecked
        return ParticipationPolicy.ALWAYS;
    }
}
