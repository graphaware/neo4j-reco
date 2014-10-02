package com.graphaware.reco.transform;

import java.util.Map;

/**
 * A component that can transform recommendation scores.
 */
public interface ScoreTransformer {

    /**
     * Transform scores.
     *
     * @param scored scored items (key=item, value=score)
     * @param <OUT>  item type.
     * @return scored items with transformed scores.
     */
    <OUT> Map<OUT, Integer> transform(Map<OUT, Integer> scored);
}
