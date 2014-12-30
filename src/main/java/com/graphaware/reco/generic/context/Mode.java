package com.graphaware.reco.generic.context;

/**
 * An indication whether the recommendations being computed are meant to be {@link #REAL_TIME}
 * or {@link #BATCH}. Implementations of {@link com.graphaware.reco.generic.engine.RecommendationEngine} can choose to
 * ignore it, but they can also choose to make the recommendation process faster and less accurate for real-time
 * scenarios and slower but more accurate for pre-computed (batch) scenarios.
 */
public enum Mode {

    REAL_TIME,

    BATCH,
}
