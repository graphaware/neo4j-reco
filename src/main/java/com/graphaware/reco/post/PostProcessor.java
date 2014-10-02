package com.graphaware.reco.post;

import com.graphaware.reco.score.Recommendations;

/**
 * Recommendations post processor. Intended for boosting or penalizing scores of certain recommendations. For example,
 * you could boost the score of a recommended match on a dating site if the food taste of the recommended person matches
 * the taste of the person looking for recommendation.
 */
public interface PostProcessor<OUT, IN> {

    /**
     * Post-process results.
     *
     * @param output scored recommendations.
     * @param input  for whom the recommendation have been produced, must not be <code>null</code>.
     */
    public void postProcess(Recommendations<OUT> output, IN input);
}
