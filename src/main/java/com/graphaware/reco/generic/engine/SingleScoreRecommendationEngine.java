package com.graphaware.reco.generic.engine;

import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.policy.ParticipationPolicy;
import com.graphaware.reco.generic.result.Recommendations;
import com.graphaware.reco.generic.transform.NoTransformation;
import com.graphaware.reco.generic.transform.ScoreTransformer;

import java.util.Map;

import static org.springframework.util.Assert.*;

/**
 * Base class for {@link com.graphaware.reco.generic.engine.RecommendationEngine}s that compute recommendations using
 * a single criteria, thus producing one type of recommendation score. Intended as a base class for implementations
 * that are delegated to by {@link com.graphaware.reco.generic.engine.DelegatingRecommendationEngine} in all but the
 * most simple applications.
 * <p/>
 * There is an option to provide a {@link com.graphaware.reco.generic.transform.ScoreTransformer} at construction time,
 * which is used to transform all the produced scores.
 */
public abstract class SingleScoreRecommendationEngine<OUT, IN> implements RecommendationEngine<OUT, IN> {

    private final ScoreTransformer transformer;

    /**
     * Construct a recommendation engine that performs no score transformation.
     */
    protected SingleScoreRecommendationEngine() {
        this.transformer = NoTransformation.getInstance();
    }

    /**
     * Construct a recommendation engine that transforms all scores using the provided transformer.
     *
     * @param transformer for scores, must not be <code>null</code>.
     */
    protected SingleScoreRecommendationEngine(ScoreTransformer transformer) {
        notNull(transformer);

        this.transformer = transformer;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link com.graphaware.reco.generic.policy.ParticipationPolicy#ALWAYS} by default.
     */
    @Override
    public ParticipationPolicy<OUT, IN> participationPolicy(Context context) {
        //noinspection unchecked
        return ParticipationPolicy.ALWAYS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Recommendations<OUT> recommend(IN input, Context<OUT, IN> context) {
        Recommendations<OUT> result = new Recommendations<>();

        for (Map.Entry<OUT, Integer> entry : doRecommend(input, context).entrySet()) {
            if (context.allow(entry.getKey(), input)) {
                result.add(entry.getKey(), scoreName(), transformer.transform(entry.getKey(), entry.getValue()));
            }
        }

        return result;
    }

    /**
     * @return name of the score this engine produces.
     */
    protected abstract String scoreName();

    /**
     * Perform the computation of recommendations. Recommendations produced by this method have an associated {@link java.lang.Integer}
     * score, which is later transformed by the provided {@link com.graphaware.reco.generic.transform.ScoreTransformer}.
     * <p/>
     * Context is provided for information, but its {@link com.graphaware.reco.generic.context.Context#allow(Object, Object)}
     * method does not have to be used. I.e., implementations of this method should produce raw recommendations, expressing
     * core business logic of coming up with these recommendations, ignoring blacklists, filtering, etc, which is applied
     * by this class ({@link com.graphaware.reco.generic.engine.SingleScoreRecommendationEngine}).
     *
     * @param input   to the recommendation process.
     * @param context of the current computation.
     * @return a map of recommended items and their scores.
     */
    protected abstract Map<OUT, Integer> doRecommend(IN input, Context<OUT, IN> context);
}
