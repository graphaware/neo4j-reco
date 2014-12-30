package com.graphaware.reco.generic.context;

import com.graphaware.reco.generic.filter.BlacklistBuilder;
import com.graphaware.reco.generic.filter.Filter;

import java.util.*;

import static java.util.Collections.*;
import static org.springframework.util.Assert.*;

/**
 * A {@link com.graphaware.reco.generic.context.ContextFactory} that produces {@link com.graphaware.reco.generic.context.FilteringContext}s.
 * <p/>
 * The factory can be configured with a list of {@link com.graphaware.reco.generic.filter.BlacklistBuilder}s that produce
 * blacklists of items passed onto the context, and a list of {@link com.graphaware.reco.generic.filter.Filter}s, which
 * are passed to the produced contexts directly.
 */
public class FilteringContextFactory<OUT, IN> implements ContextFactory<OUT, IN> {

    private final List<BlacklistBuilder<OUT, IN>> blacklistBuilders = new LinkedList<>();
    private final List<Filter<OUT, IN>> filters = new LinkedList<>();

    /**
     * Add {@link com.graphaware.reco.generic.filter.BlacklistBuilder}s used by this factory to produce blacklists
     * of items.
     *
     * @param blacklistBuilders to be used. All of the arguments must not be <code>null</code>.
     */
    @SafeVarargs
    public final FilteringContextFactory<OUT, IN> addBlacklistBuilders(BlacklistBuilder<OUT, IN>... blacklistBuilders) {
        notNull(blacklistBuilders);
        noNullElements(blacklistBuilders);

        addAll(this.blacklistBuilders, blacklistBuilders);

        return this;
    }

    /**
     * Add {@link com.graphaware.reco.generic.filter.Filter}s passed to the produced {@link com.graphaware.reco.generic.context.Context}s.
     *
     * @param filters to be used. All of the arguments must not be <code>null</code>.
     */
    @SafeVarargs
    public final FilteringContextFactory<OUT, IN> addFilters(Filter<OUT, IN>... filters) {
        notNull(filters);
        noNullElements(filters);

        addAll(this.filters, filters);

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Context<OUT, IN> produceContext(IN input, Mode mode, int limit) {
        Set<OUT> blacklist = new HashSet<>();
        for (BlacklistBuilder<OUT, IN> blacklistBuilder : blacklistBuilders) {
            blacklist.addAll(blacklistBuilder.buildBlacklist(input));
        }

        return new FilteringContext<>(mode, limit, unmodifiableList(filters), unmodifiableSet(blacklist));
    }
}
