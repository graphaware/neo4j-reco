package com.graphaware.reco.generic.context;

/**
 * Context holding information about the recommendation-computing process. Contexts should be package-protected and
 * constructed using their respective public {@link com.graphaware.reco.generic.context.ContextFactory} implementations.
 */
public interface Context<OUT, IN> {

    /**
     * @return {@link Mode} in which recommendations are being computed.
     */
    Mode mode();

    /**
     * @return desired maximum number of produced recommendations.
     */
    int limit();

    /**
     * Initialize the context before computing recommendation for the given input. Must be called exactly once by the
     * corresponding {@link com.graphaware.reco.generic.context.ContextFactory}, thus doesn't need to be thread-safe.
     *
     * @param input for which to compute recommendations.
     */
    void initialize(IN input);

    /**
     * Check whether a produced recommendation is allowed for the given input in the current context. Can be called by
     * multiple threads simultaneously, must be thus thread-safe.
     *
     * @param recommendation produced. Must not be <code>null</code>.
     * @param input          for which the recommendation was produced. Must not be <code>null</code>.
     * @return true iff the recommendation is allowed for the given input.
     */
    boolean allow(OUT recommendation, IN input);
}
