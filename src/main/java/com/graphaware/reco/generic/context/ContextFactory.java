package com.graphaware.reco.generic.context;

/**
 * A singleton component encapsulating any one-off configuration of a recommendation engine, which is responsible
 * for producing {@link com.graphaware.reco.generic.context.Context}s for the recommendation-computing process. A new
 * {@link com.graphaware.reco.generic.context.Context} should be produced every time recommendations are computed for
 * an input.
 * <p/>
 * Implementations must be thread-safe.
 */
public interface ContextFactory<OUT, IN> {

    /**
     * Produce a {@link com.graphaware.reco.generic.context.Context} for the recommendation-computing process.
     *
     * @param input for which recommendations are about to be computed.
     * @param mode  in which the computation takes place.
     * @param limit maximum number of recommendations desired.
     * @return context.
     */
    Context<OUT, IN> produceContext(IN input, Mode mode, int limit);
}
