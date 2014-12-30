package com.graphaware.reco.generic.transform;

/**
 * A component that can transform recommendation scores.
 */
public interface ScoreTransformer {

    /**
     * Transform a score.
     *
     * @param recommendation recommended item.
     * @param score          score of the item.
     * @return transformed score.
     */
    <OUT> int transform(OUT recommendation, int score);
}
