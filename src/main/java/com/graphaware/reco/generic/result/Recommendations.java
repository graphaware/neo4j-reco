package com.graphaware.reco.generic.result;

import com.graphaware.common.util.Pair;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.graphaware.reco.generic.util.MapSorter.sortMapByDescendingValue;
import static org.springframework.util.Assert.*;

/**
 * Encapsulates recommendations and their {@link Score}s.
 * <p/>
 * This class is thread-safe.
 *
 * @param <OUT> type of the recommended item.
 */
public class Recommendations<OUT> {

    private final ConcurrentHashMap<OUT, Score> scoredItems = new ConcurrentHashMap<>();

    /**
     * Merge the given recommendations to this instance.
     *
     * @param recommendations to merge.
     * @return merged recommendations, instance of this class. The returned object should be used after merging,
     * rather than the instance merged to.
     */
    public Recommendations<OUT> merge(final Recommendations<OUT> recommendations) {
        for (Map.Entry<OUT, Score> scoredItem : recommendations.scoredItems.entrySet()) {
            add(scoredItem.getKey(), scoredItem.getValue());
        }

        return this;
    }

    /**
     * Add a recommendation and its score.
     *
     * @param recommendation to add. Must not be <code>null</code>.
     * @param score          of the recommendation. Must not be <code>null</code>.
     */
    public void add(OUT recommendation, Score score) {
        notNull(recommendation);
        notNull(score);

        Score existingScore = scoredItems.get(recommendation);

        if (existingScore != null) {
            existingScore.merge(score);
            return;
        }

        existingScore = scoredItems.putIfAbsent(recommendation, score);

        if (existingScore != null) {
            existingScore.merge(score);
        }
    }

    /**
     * Add a recommendation.
     *
     * @param recommendation to add. Must not be <code>null</code>.
     * @param scoreName      name of the partial score this recommendation is receiving. Must not be <code>null</code> or empty.
     * @param score          value of the partial score.
     */
    public void add(OUT recommendation, String scoreName, int score) {
        notNull(recommendation);
        notNull(scoreName);
        hasLength(scoreName);

        Score existingScore = scoredItems.get(recommendation);

        if (existingScore == null) {
            scoredItems.putIfAbsent(recommendation, new Score());
            existingScore = scoredItems.get(recommendation);
        }

        existingScore.add(scoreName, score);
    }

    /**
     * Get a list of recommendation ordered by decreasing score (relevance).
     *
     * @param limit the maximum number of recommendations to get.
     * @return list of recommendations paired with their composite scores, ordered by decreasing relevance.
     */
    public List<Pair<OUT, Score>> get(int limit) {
        List<Pair<OUT, Score>> result = new LinkedList<>();

        for (Map.Entry<OUT, Score> item : sortMapByDescendingValue(scoredItems).entrySet()) {
            result.add(new Pair<>(item.getKey(), item.getValue()));
            if (result.size() >= limit) {
                break;
            }
        }

        return result;
    }

    /**
     * Get a {@link Score} for the given item.
     *
     * @param item to get score for. Must not be <code>null</code>.
     * @return score.
     * @throws IllegalArgumentException if the item hasn't been scored.
     */
    public Score get(OUT item) {
        notNull(item);

        if (!scoredItems.containsKey(item)) {
            throw new IllegalArgumentException("Item " + item + " is not amongst the recommendations");
        }

        return scoredItems.get(item);
    }

    /**
     * Are there enough recommendations?
     *
     * @param limit desired number.
     * @return true iff enough.
     */
    public boolean hasEnough(int limit) {
        return scoredItems.size() >= limit;
    }

    /**
     * Get all recommended items.
     *
     * @return all items.
     */
    public Set<OUT> getItems() {
        return new HashSet<>(scoredItems.keySet());
    }
}