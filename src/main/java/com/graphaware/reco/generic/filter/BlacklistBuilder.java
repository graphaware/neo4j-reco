package com.graphaware.reco.generic.filter;

import java.util.Set;

/**
 * Component that is able to build a blacklist of recommendations, i.e. items that must not be recommended for given input.
 * <p/>
 * Intended for filtering out recommendation that are already irrelevant, such as items a user has already bought, people
 * that are already friends, etc.
 *
 * @param <OUT> type of recommendations.
 * @param <IN>  type of input on which recommendations are based.
 */
public interface BlacklistBuilder<OUT, IN> {

    /**
     * Get a set of items that must not be used as a recommendation for given input.
     *
     * @param input for which recommendations are being computed. Must not be null.
     * @return set of blacklisted recommendations.
     */
    Set<OUT> buildBlacklist(IN input);
}
