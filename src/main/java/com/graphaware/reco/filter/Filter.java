package com.graphaware.reco.filter;

/**
 * Component that filters recommendations.
 * <p/>
 * Intended for filtering out recommendation that are forbidden and this can be determined by looking at the recommended
 * node itself. For instance, this could be used to prevent suggesting items out of stock or people without public profile.
 *
 * @param <OUT> type of recommendations.
 * @param <IN>  type of input on which recommendations are based.
 */
public interface Filter<OUT, IN> {

    /**
     * Should the given recommendation actually be used?
     *
     * @param recommendation to decide on. Must not be <code>null</code>.
     * @param input          input based on which this recommendation was found. Must not be <code>null</code>.
     * @return true iff the recommendation should be used based on this filter's opinion.
     */
    boolean include(OUT recommendation, IN input);
}
