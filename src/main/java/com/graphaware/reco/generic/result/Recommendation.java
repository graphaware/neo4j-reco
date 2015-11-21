/*
 * Copyright (c) 2013-2015 GraphAware
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

import com.graphaware.common.uuid.EaioUuidGenerator;
import com.graphaware.common.uuid.UuidGenerator;

import java.util.Map;

import static org.springframework.util.Assert.notNull;

/**
 * Encapsulates a recommended item together with its UUID and {@link com.graphaware.reco.generic.result.Score}.
 * <p/>
 * The UUID is intended to be used as a unique identifier of a particular recommendation served to a user, so that
 * response rates to recommendations can be measured.
 * <p/>
 * This class is thread-safe.
 *
 * @param <OUT> type of the recommended item.
 */
public class Recommendation<OUT> implements Comparable<Recommendation<OUT>> {

    private static final UuidGenerator uuidGenerator = new EaioUuidGenerator();

    private final String uuid;
    private final OUT item;
    private final Score score;

    /**
     * Construct a new recommendation with a blank (zero) score. Automatically gets a UUID assigned.
     *
     * @param item recommended item, must not be <code>null</code>.
     */
    public Recommendation(OUT item) {
        notNull(item);

        this.uuid = uuidGenerator.generateUuid();
        this.item = item;
        this.score = new Score();
    }

    /**
     * Add a score to a recommendation.
     *
     * @param score of the recommendation. Must not be <code>null</code>.
     */
    public void add(Score score) {
        notNull(score);

        this.score.add(score);
    }

    /**
     * Add a partial score to this recommendation's score.
     *
     * @param scoreName name of the partial score. Must not be <code>null</code> or empty.
     * @param partialScore partial score.
     */
    public void add(String scoreName, PartialScore partialScore) {
        score.add(scoreName, partialScore);
    }

    /**
     * Add a partial score to this recommendation's score.
     *
     * @param scoreName  name of the partial score. Must not be <code>null</code> or empty.
     * @param scoreValue partial score value.
     */
    public void add(String scoreName, float scoreValue) {
        score.add(scoreName, scoreValue);
    }

    /**
     * Add a partial score to this recommendation's score.
     *
     * @param scoreName  name of the partial score. Must not be <code>null</code> or empty.
     * @param scoreValue partial score value.
     * @param details    of the partial score value.
     */
    public void add(String scoreName, float scoreValue, Map<String, Object> details) {
        score.add(scoreName, scoreValue, details);
    }

    public String getUuid() {
        return uuid;
    }

    public OUT getItem() {
        return item;
    }

    public Score getScore() {
        return score;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Recommendation<OUT> o) {
        return this.score.compareTo(o.score);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Recommendation that = (Recommendation) o;

        if (!item.equals(that.item)) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return item.hashCode();
    }
}
