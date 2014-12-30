package com.graphaware.reco.generic.engine;

import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.policy.ParticipationPolicy;
import com.graphaware.reco.generic.result.Recommendations;

/**
 * A recommendation engine.
 *
 * @param <OUT> type of the recommendations produced.
 * @param <IN>  type of the item recommendations are for / based on.
 */
public interface RecommendationEngine<OUT, IN, C extends Context<OUT, IN>> {

    /**
     * Get this engine's participation / involvement in producing recommendations in a specific context.
     *
     * @param context the context in which recommendations are being produced.
     * @return participation policy.
     */
    ParticipationPolicy<OUT, IN> participationPolicy(C context);

    /**
     * Produce recommendations.
     *
     * @param input   input to the recommendation engine. Typically the person or item recommendations are being
     *                computed for.
     * @param context additional information about the recommendation process useful to the engine.
     * @return recommendations.
     */
    Recommendations<OUT> recommend(IN input, C context);
}
