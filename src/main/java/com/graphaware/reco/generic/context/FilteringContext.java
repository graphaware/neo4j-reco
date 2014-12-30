package com.graphaware.reco.generic.context;

import com.graphaware.reco.generic.filter.Filter;

import java.util.List;
import java.util.Set;

import static org.springframework.util.Assert.notNull;

/**
 * A {@link com.graphaware.reco.generic.context.Context} that accepts a list of {@link com.graphaware.reco.generic.filter.Filter}s
 * and a set of blacklisted items, both of which it uses to exclude some recommendations.
 */
public class FilteringContext<OUT, IN> extends SimpleContext<OUT, IN> {

    private final List<Filter<OUT, IN>> filters;
    private final Set<OUT> blacklist;

    /**
     * Construct a new context.
     *
     * @param mode      in which recommendations are being computed. Must not be <code>null</code>.
     * @param limit     the maximum number of desired recommendations. Must be positive.
     * @param filters   used to filter out items. Can be empty, but must not be <code>null</code>.
     * @param blacklist a set of blacklisted items. Can be empty, but must not be <code>null</code>.
     */
    FilteringContext(Mode mode, int limit, List<Filter<OUT, IN>> filters, Set<OUT> blacklist) {
        super(mode, limit);

        notNull(filters);
        notNull(blacklist);

        this.filters = filters;
        this.blacklist = blacklist;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Returns <code>false</code> for blacklisted items and items filtered out by at least one of the {@link com.graphaware.reco.generic.filter.Filter}s.
     */
    @Override
    public boolean allow(OUT recommendation, IN input) {
        if (!super.allow(recommendation, input)) {
            return false;
        }

        if (blacklist.contains(recommendation)) {
            return false;
        }

        for (Filter<OUT, IN> filter : filters) {
            if (!filter.include(recommendation, input)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Blacklist the given recommendation. Intended for {@link com.graphaware.reco.generic.engine.RecommendationEngine}s
     * to prevent following engines from discovering the same recommendation.
     *
     * @param recommendation to blacklist.
     */
    public void blacklist(OUT recommendation) {
        this.blacklist.add(recommendation);
    }
}

