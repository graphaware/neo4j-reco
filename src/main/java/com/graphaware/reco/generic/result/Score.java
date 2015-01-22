/*
 * Copyright (c) 2015 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.reco.generic.result;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.util.Assert.hasLength;
import static org.springframework.util.Assert.notNull;

/**
 * A recommendation score that is composed of multiple partial scores. Each partial score has a name and and integer value.
 * <p/>
 * This class is thread-safe.
 */
public class Score implements Comparable<Score> {

    private final AtomicInteger totalScore = new AtomicInteger(0);
    private final ConcurrentHashMap<String, AtomicInteger> scoreParts = new ConcurrentHashMap<>();

    /**
     * Add a partial score to this composite score.
     *
     * @param scoreName name of the partial score. Must not be <code>null</code> or empty.
     * @param value     score value.
     */
    public void add(String scoreName, int value) {
        notNull(scoreName);
        hasLength(scoreName);

        AtomicInteger score = scoreParts.get(scoreName);

        if (score == null) {
            score = scoreParts.putIfAbsent(scoreName, new AtomicInteger(0));
        }

        if (score == null) {
            score = scoreParts.get(scoreName);
        }

        score.addAndGet(value);
        totalScore.addAndGet(value);
    }

    /**
     * Merge another score into this score.
     *
     * @param score to merge.
     * @return merged score (this instance). The returned object should be used after merging, rather than the instance
     * merged to.
     */
    public Score merge(Score score) {
        for (Map.Entry<String, AtomicInteger> entry : score.scoreParts.entrySet()) {
            this.add(entry.getKey(), entry.getValue().get());
        }

        return this;
    }

    /**
     * Get the total value of this composite score.
     *
     * @return total value.
     */
    public int getTotalScore() {
        return totalScore.get();
    }

    /**
     * Get a copy of all the composite score parts.
     *
     * @return composite score parts.
     */
    public Map<String, Integer> getScoreParts() {
        Map<String, Integer> result = new TreeMap<>();

        for (Map.Entry<String, AtomicInteger> entry : scoreParts.entrySet()) {
            result.put(entry.getKey(), entry.getValue().get());
        }

        return result;
    }

    /**
     * Get the value of a partial score.
     *
     * @param scoreName name of the partial score.
     * @return value of the score, 0 if no such partial score has been added.
     */
    public int get(String scoreName) {
        if (scoreParts.containsKey(scoreName)) {
            return scoreParts.get(scoreName).get();
        } else {
            return 0;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Score o) {
        return Integer.compare(getTotalScore(), o.getTotalScore());
    }
}
