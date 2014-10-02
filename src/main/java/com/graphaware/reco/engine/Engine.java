package com.graphaware.reco.engine;

import com.graphaware.common.util.Pair;
import com.graphaware.reco.score.CompositeScore;

import java.util.List;

/**
 * A recommendation engine. Nothing more, nothing less.
 *
 * @param <OUT> type of the recommendations produced.
 * @param <IN>  type of the item recommendations are for / based on.
 */
public interface Engine<OUT, IN> {

    /**
     * Produce recommendations.
     *
     * @param input    input to the recommendation engine. Typically the person or item recommendations are being computed for.
     * @param limit    desired maximum number of produced recommendations.
     * @param realTime an indication whether the recommendations being computed are meant to be real-time (<code>true</code>)
     *                 or not (<code>false</code>). Implementations can choose to ignore it, but they can also choose
     *                 to make the recommendation process faster and less accurate for real-time scenarios and slower
     *                 but more accurate for pre-computed scenarios.
     * @return a list of recommendations, where the first element of each pair is the recommended item and the second
     *         is the relevance score. The list is should sorted by decreasing score, i.e., decreasing relevance.
     */
    List<Pair<OUT, CompositeScore>> recommend(IN input, int limit, boolean realTime);
}
