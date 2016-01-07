/*
 * Copyright (c) 2013-2016 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.reco.generic.result;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.springframework.util.Assert.hasLength;
import static org.springframework.util.Assert.notNull;

/**
 * Encapsulates {@link com.graphaware.reco.generic.result.Recommendation}s.
 * <p/>
 * This class is thread-safe.
 *
 * @param <OUT> type of the recommended item.
 */
public class Recommendations<OUT> {

    private final ConcurrentMap<OUT, Recommendation<OUT>> scoredItems = new ConcurrentHashMap<>();

    /**
     * Get all recommendations.
     *
     * @return all recommendations.
     */
    public Set<Recommendation<OUT>> get() {
        return new HashSet<>(scoredItems.values());
    }

    /**
     * Get a list of recommendation ordered by decreasing score (relevance).
     *
     * @param limit the maximum number of recommendations to get.
     * @return list of recommendations paired with their composite scores, ordered by decreasing relevance.
     */
    public List<Recommendation<OUT>> get(int limit) {
        List<Recommendation<OUT>> result = new LinkedList<>(scoredItems.values());

        Collections.sort(result, Collections.reverseOrder());

        return result.subList(0, Math.min(limit, result.size()));
    }

    /**
     * Get the {@link com.graphaware.reco.generic.result.Recommendation} object for the given recommended item.
     *
     * @param item recommended item. Must not be <code>null</code>.
     * @return recommendation.
     * @throws IllegalArgumentException if the item hasn't been recommended.
     */
    public Recommendation<OUT> get(OUT item) {
        notNull(item);

        if (!scoredItems.containsKey(item)) {
            throw new IllegalArgumentException("Item " + item + " is not amongst the recommendations");
        }

        return scoredItems.get(item);
    }

    /**
     * Get or create the {@link com.graphaware.reco.generic.result.Recommendation} object for the given recommended item.
     *
     * @param item recommended item. Must not be <code>null</code>.
     * @return recommendation.
     */
    public Recommendation<OUT> getOrCreate(OUT item) {
        notNull(item);

        if (scoredItems.get(item) == null) {
            scoredItems.putIfAbsent(item, new Recommendation<>(item));
        }

        return scoredItems.get(item);
    }

    /**
     * Merge the given recommendations to this instance.
     *
     * @param recommendations to add.
     * @return merged recommendations, instance of this class. The returned object should be used after merging,
     * rather than the instance merged to.
     */
    public Recommendations<OUT> merge(final Recommendations<OUT> recommendations) {
        for (Recommendation<OUT> recommendation : recommendations.get()) {
            getOrCreate(recommendation.getItem()).add(recommendation.getScore());
        }

        return this;
    }

    /**
     * Add a recommendation.
     *
     * @param item  to add. Must not be <code>null</code>.
     * @param score score. Must not be <code>null</code>.
     */
    public void add(OUT item, Score score) {
        notNull(item);
        notNull(score);

        getOrCreate(item).add(score);
    }

    /**
     * Add a recommendation.
     *
     * @param item         to add. Must not be <code>null</code>.
     * @param scoreName    name of the partial score this recommendation is receiving. Must not be <code>null</code> or empty.
     * @param partialScore partial score.
     */
    public void add(OUT item, String scoreName, PartialScore partialScore) {
        notNull(item);
        notNull(scoreName);
        hasLength(scoreName);

        getOrCreate(item).add(scoreName, partialScore);
    }

    /**
     * Add a recommendation.
     *
     * @param item      to add. Must not be <code>null</code>.
     * @param scoreName name of the partial score this recommendation is receiving. Must not be <code>null</code> or empty.
     * @param score     value of the partial score.
     * @param details   of the partial score value.
     */
    public void add(OUT item, String scoreName, float score, Map<String, Object> details) {
        notNull(item);
        notNull(scoreName);
        hasLength(scoreName);

        getOrCreate(item).add(scoreName, score, details);
    }

    /**
     * Add a recommendation.
     *
     * @param item      to add. Must not be <code>null</code>.
     * @param scoreName name of the partial score this recommendation is receiving. Must not be <code>null</code> or empty.
     * @param score     value of the partial score.
     */
    public void add(OUT item, String scoreName, float score) {
        add(item, scoreName, score, null);
    }

    /**
     * Remove a recommendation.
     *
     * @param item recommended item to remove.
     */
    public void remove(OUT item) {
        notNull(item);
        scoredItems.remove(item);
    }

    /**
     * Are there enough recommendations?
     *
     * @param limit desired number.
     * @return true iff enough.
     */
    public boolean hasEnoughResults(int limit) {
        return size() >= limit;
    }

    /**
     * @return total number of recommendations.
     */
    public int size() {
        return scoredItems.size();
    }
}
