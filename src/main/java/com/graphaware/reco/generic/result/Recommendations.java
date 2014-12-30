package com.graphaware.reco.generic.result;

import com.graphaware.common.util.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.graphaware.reco.generic.util.MapSorter.sortMapByDescendingValue;

/**
 * Encapsulates recommendations and their {@link Score}s.
 * <p/>
 * This class is thread-safe.
 *
 * @param <OUT> type of the recommended item.
 */
public class Recommendations<OUT> {

    private final ConcurrentHashMap<OUT, Score> scoredItems = new ConcurrentHashMap<>();

    public Recommendations<OUT> merge(final Recommendations<OUT> recommendations) {
        for (Map.Entry<OUT, Score> scoredItem : recommendations.scoredItems.entrySet()) {
            this.add(scoredItem.getKey(), scoredItem.getValue());
        }

        return this;
    }

    public void add(OUT recommendation, Score score) {
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
     * @param recommendation to add.
     * @param scoreName      name of the partial score this recommendation is receiving.
     * @param score          value of the partial score.
     */
    public void add(OUT recommendation, String scoreName, int score) {
        scoredItems.putIfAbsent(recommendation, new Score());

        Score compositeScore = scoredItems.get(recommendation);

        if (compositeScore == null) {
            throw new IllegalStateException("Missing composite score - this is a bug");
        }

        compositeScore.add(scoreName, score);
    }

    /**
     * Add recommendations from a single component.
     *
     * @param scoreName       name of the partial score all the recommendations are receiving.
     * @param recommendations recommended items with their score to add.
     */
    public void add(String scoreName, Map<OUT, Integer> recommendations) {
        for (Map.Entry<OUT, Integer> reco : recommendations.entrySet()) {
            add(reco.getKey(), scoreName, reco.getValue());
        }
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
     * @param item to get score for.
     * @return score.
     * @throws IllegalArgumentException if the item hasn't been scored.
     */
    public Pair<OUT, Score> get(OUT item) {
        if (!scoredItems.containsKey(item)) {
            throw new IllegalArgumentException("Item " + item + " is not amongst the recommendations");
        }
        return new Pair<>(item, scoredItems.get(item));
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