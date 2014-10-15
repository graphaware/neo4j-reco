package com.graphaware.reco.part;

import com.graphaware.reco.score.Recommendations;

import java.util.Map;
import java.util.Set;

/**
 * A recommendation engine part. Can participate in computing recommendations.
 *
 * @param <OUT> type of the recommendations produced.
 * @param <IN>  type of the item recommendations are for / based on.
 */
public interface EnginePart<OUT, IN> {

    /**
     * Produce recommendations.
     *
     * @param output    output produced so far by the engine that this part should populate.
     * @param input     input to the recommendation engine part. Typically the person or item recommendations are being computed for.
     * @param limit     desired maximum number of produced recommendations for the whole engine. Parts can take this into
     *                  account in order not to produce too many recommendations, if they can traverse the graph best-first manner.
     * @param blacklist of items that must not be recommended.
     * @param realTime  an indication whether the recommendations being computed are meant to be real-time (<code>true</code>)
     *                  or not (<code>false</code>). Implementations can choose to ignore it, but they can also choose
     *                  to make the recommendation process faster and less accurate for real-time scenarios and slower
     *                  but more accurate for pre-computed scenarios.
     */
    void recommend(Recommendations<OUT> output, IN input, int limit, Set<OUT> blacklist, boolean realTime);

    /**
     * Get the {@link EnoughResultsPolicy} of this engine part.
     *
     * @return this parts policy.
     */
    EnoughResultsPolicy enoughResultsPolicy();
}
