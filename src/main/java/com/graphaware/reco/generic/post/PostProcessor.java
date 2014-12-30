package com.graphaware.reco.generic.post;

import com.graphaware.reco.generic.result.Recommendations;

/**
 * Recommendations post processor. Intended for boosting or penalizing scores of certain recommendations. For example,
 * you could boost the score of a recommended match on a dating site if the smoker/non-smoker preference of the
 * recommended person matches the preference of the person looking for recommendation.
 */
public interface PostProcessor<OUT, IN> {

    /**
     * Post-process results.
     *
     * @param recommendations scored recommendations.
     * @param input           for whom the recommendation have been produced, must not be <code>null</code>.
     */
    void postProcess(Recommendations<OUT> recommendations, IN input);
}
