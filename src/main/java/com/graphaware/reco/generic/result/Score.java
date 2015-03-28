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

import com.graphaware.reco.generic.util.AtomicFloat;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.springframework.util.Assert.hasLength;
import static org.springframework.util.Assert.notNull;

/**
 * A recommendation score that is composed of multiple named partial scores ({@link com.graphaware.reco.generic.result.ScorePart}s).
 * The {@link #getTotalScore()} is kept up-to-date at all times as the sum of {@link ScorePart#getValue()} of all
 * encapsulated {@link com.graphaware.reco.generic.result.ScorePart}s.
 * <p/>
 * This class is thread-safe.
 */
public class Score implements Comparable<Score> {

    private final AtomicFloat totalScore = new AtomicFloat(0);
    private final ConcurrentMap<String, ScorePart> scoreParts = new ConcurrentHashMap<>();

    /**
     * Add a partial score to this composite score.
     *
     * @param scoreName name of the partial score. Must not be <code>null</code> or empty.
     * @param value     score value.
     */
    public void add(String scoreName, float value) {
        add(scoreName, value, null);
    }

    /**
     * Add a partial score to this composite score.
     *
     * @param scoreName name of the partial score. Must not be <code>null</code> or empty.
     * @param value     score value.
     * @param details   of the value. Can be <code>null</code> or empty if no details are available.
     */
    public void add(String scoreName, float value, Map<String, Object> details) {
        add(scoreName, new ScorePart(value, details));
    }

    /**
     * Add a partial score to this composite score.
     *
     * @param scoreName name of the partial score. Must not be <code>null</code> or empty.
     * @param scorePart partial score. Must not be <code>null</code>.
     */
    public void add(String scoreName, ScorePart scorePart) {
        notNull(scoreName);
        hasLength(scoreName);
        notNull(scorePart);

        ScorePart score = scoreParts.get(scoreName);

        if (score == null) {
            score = scoreParts.putIfAbsent(scoreName, new ScorePart());
        }

        if (score == null) {
            score = scoreParts.get(scoreName);
        }

        score.add(scorePart);
        totalScore.addAndGet(scorePart.getValue());
    }

    /**
     * Add another score to this score.
     *
     * @param score to add. Must not be <code>null</code>.
     */
    public void add(Score score) {
        notNull(score);

        for (Map.Entry<String, ScorePart> entry : score.scoreParts.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Get the total value of this composite score.
     *
     * @return total value.
     */
    public float getTotalScore() {
        return totalScore.get();
    }

    /**
     * Get a copy of all the composite score parts.
     *
     * @return composite score parts.
     */
    public Map<String, ScorePart> getScoreParts() {
        Map<String, ScorePart> result = new TreeMap<>();

        for (Map.Entry<String, ScorePart> entry : scoreParts.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    /**
     * Get the value of a partial score.
     *
     * @param scoreName name of the partial score.
     * @return value of the score, 0 if no such partial score has been added.
     */
    public float get(String scoreName) {
        if (scoreParts.containsKey(scoreName)) {
            return scoreParts.get(scoreName).getValue();
        } else {
            return 0;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Score o) {
        return Float.compare(getTotalScore(), o.getTotalScore());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{total:").append(getTotalScore());

        for (Map.Entry<String, ScorePart> entry : getScoreParts().entrySet()) {
            builder.append(", ").append(entry.getKey()).append(":").append(entry.getValue());
        }

        builder.append("}");
        return builder.toString();
    }
}
