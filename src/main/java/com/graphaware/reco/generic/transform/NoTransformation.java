package com.graphaware.reco.generic.transform;

/**
 * {@link ScoreTransformer} that performs no transformation. Singleton.
 */
public final class NoTransformation implements ScoreTransformer {

    private static final NoTransformation INSTANCE = new NoTransformation();

    public static NoTransformation getInstance() {
        return INSTANCE;
    }

    private NoTransformation() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <OUT> int transform(OUT recommendation, int score) {
        return score;
    }
}
