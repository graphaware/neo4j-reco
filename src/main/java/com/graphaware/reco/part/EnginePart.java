package com.graphaware.reco.part;

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
     * Get the name of this engine part, so that its recommendations can be distinguished from other parts' recommendations
     * if needed.
     *
     * @return part name.
     */
    String name();

    /**
     * Produce recommendations.
     *
     * @param input     input to the recommendation engine part. Typically the person or item recommendations are being computed for.
     * @param limit     desired maximum number of produced recommendations for the whole engine. Parts can take this into
     *                  account in order not to produce too many recommendations, if they can traverse the graph best-first manner.
     * @param blacklist of items that must not be recommended.
     * @param realTime  an indication whether the recommendations being computed are meant to be real-time (<code>true</code>)
     *                  or not (<code>false</code>). Implementations can choose to ignore it, but they can also choose
     *                  to make the recommendation process faster and less accurate for real-time scenarios and slower
     *                  but more accurate for pre-computed scenarios.
     * @return a map of recommendations, where key is the recommended item and value if the relevance score.
     */
    Map<OUT, Integer> recommend(IN input, int limit, Set<OUT> blacklist, boolean realTime);

    /**
     * Is this recommendation engine part optional? If it is, it can be skipped if there are enough recommendations
     * or there isn't enough time.
     *
     * @return true iff optional.
     */
    boolean isOptional();
}
