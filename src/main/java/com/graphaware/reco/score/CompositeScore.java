package com.graphaware.reco.score;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.util.Assert.hasLength;
import static org.springframework.util.Assert.notNull;

/**
 * A recommendation score that is composed of multiple partial scores. Each partial score has a name and and integer value.
 * It is only allowed to register a each partial score one time per {@link CompositeScore} instance.
 * <p/>
 * This class is thread-safe.
 */
public class CompositeScore implements Comparable<CompositeScore> {

    private final AtomicInteger total = new AtomicInteger(0);
    private final ConcurrentHashMap<String, Integer> scoreParts = new ConcurrentHashMap<>();

    /**
     * Add a partial score to this composite score.
     *
     * @param scoreName name of the partial score. Must not be <code>null</code> or empty.
     * @param value     score value.
     */
    public void add(String scoreName, int value) {
        notNull(scoreName);
        hasLength(scoreName);

        if (scoreParts.putIfAbsent(scoreName, value) != null) {
            return;
//            throw new IllegalStateException("Score " + scoreName + " has already been added");
        }

        total.addAndGet(value);
    }

    /**
     * Get the total value of this composite score.
     *
     * @return total value.
     */
    public int get() {
        return total.get();
    }

    /**
     * Does this composite score contain a score with the given name?
     *
     * @param scoreName to check.
     * @return true iff contained.
     */
    public boolean contains(String scoreName) {
        return scoreParts.contains(scoreName);
    }

    /**
     * Get the value of a partial score.
     *
     * @param scoreName name of the partial score.
     * @return value of the score, 0 if no such partial score has been added.
     */
    public int get(String scoreName) {
        if (scoreParts.containsKey(scoreName)) {
            return scoreParts.get(scoreName);
        } else {
            return 0;
        }
    }

    /**
     * Get the names of the partial scores forming this composite score.
     *
     * @return partial score names.
     */
    public Set<String> getScores() {
        return new HashSet<>(scoreParts.keySet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(CompositeScore o) {
        return Integer.compare(get(), o.get());
    }
}
