package com.graphaware.reco.transform;

import java.util.Map;

/**
 * {@link ScoreTransformer} that performs no transformation. Singleton.
 */
public final class NoTransformation implements ScoreTransformer {

    private static final NoTransformation INSTANCE = new NoTransformation();

    public static NoTransformation getInstance() {
        return INSTANCE;
    }

    protected NoTransformation() {
    }

    @Override
    public <OUT> Map<OUT, Integer> transform(Map<OUT, Integer> scored) {
        return scored;
    }
}
