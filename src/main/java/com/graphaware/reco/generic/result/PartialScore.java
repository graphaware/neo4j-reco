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

import com.graphaware.reco.generic.util.AtomicFloat;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A contribution to the total score of a particular recommendation that has been discovered. Typically, there will
 * be a single {@link PartialScore} for each {@link com.graphaware.reco.generic.engine.RecommendationEngine}
 * (i.e., logical reason) that has discovered a particular recommendation.
 * <p/>
 * For example, when building a "people you should be friends with" recommendation engine, for each recommendation discovered,
 * there would be one {@link PartialScore} representing "friends in common" score contribution,
 * and another {@link PartialScore} representing "common interests" score contribution.
 * <p/>
 * The total numerical value of the contribution can be obtained by {@link #getValue()} and is kept up-to-date at all times.
 * <p/>
 * Optionally, details about the actual contribution, such as the number of and the names of friends in common, are encapsulated
 * by {@link PartialScore} in the form of {@link com.graphaware.reco.generic.result.Reason}s.
 * <p/>
 * Please note that {@link com.graphaware.reco.generic.transform.ScoreTransformer}s can be applied to the {@link PartialScore}
 * and change the total score only. In these cases, the value returned by {@link #getValue()} could end up being different from
 * the sum of individual {@link com.graphaware.reco.generic.result.Reason}s values.
 * <p/>
 * This class is thread-safe.
 */
public class PartialScore {

    private final AtomicFloat value = new AtomicFloat(0);
    private final Set<Reason> reasons = Collections.newSetFromMap(new ConcurrentHashMap<Reason, Boolean>());

    /**
     * Create a new partial score with value = 0 and no further details.
     */
    public PartialScore() {
    }

    /**
     * Create a new partial score with the given value and no further details.
     *
     * @param value of this partial score's contribution to the total score. Can be negative in case of penalties, for instance.
     */
    public PartialScore(float value) {
        add(value, null);
    }

    /**
     * Create a new partial score with the given value and details about the value. The value and details will become an immutable
     * {@link com.graphaware.reco.generic.result.Reason}. Additionally, the value will become the total value of
     * this partial score and can be later modified, for example, by {@link com.graphaware.reco.generic.transform.ScoreTransformer}s.
     *
     * @param value   of this partial score's contribution to the total score. Can be negative in case of penalties, for instance.
     * @param details about the value as arbitrary key-value pairs.
     */
    public PartialScore(float value, Map<String, Object> details) {
        add(value, details);
    }

    /**
     * Add a number to the total value of this partial score with no additional details.
     *
     * @param value to add.
     */
    public void add(float value) {
        this.value.addAndGet(value);
    }

    /**
     * Add a number to the total value of this partial score with additional details about the value. The value and details
     * will become an immutable {@link com.graphaware.reco.generic.result.Reason}. Additionally, the value will be added to
     * the total value of this partial score, which can be later modified, for example, by {@link com.graphaware.reco.generic.transform.ScoreTransformer}s.
     *
     * @param value   to add.
     * @param details about the value as arbitrary key-value pairs.
     */
    public void add(float value, Map<String, ?> details) {
        add(value);
        addReason(value, details);
    }

    /**
     * Add the contents (value and details) of another partial score to this partial score.
     *
     * @param partialScore to add.
     */
    public void add(PartialScore partialScore) {
        add(partialScore.getValue());
        addReasons(partialScore.getReasons());
    }

    /**
     * Add a reason to this partial score. The reason is built from the provided value and details, iff the details aren't <code>null</code>.
     * Calling this method with <code>null</code> details has no effect. Please note that the total value of this partial score
     * remains unchanged, which is why this method is (and must remain) private.
     *
     * @param value   of the reason to add.
     * @param details of the reason to add.
     */
    private void addReason(float value, Map<String, ?> details) {
        if (details == null) {
            return;
        }

        add(new Reason(value, details));
    }

    /**
     * Add a reason to this partial score. Please note that the total value of this partial score remains unchanged,
     * which is why this method is (and must remain) private.
     *
     * @param reason to add.
     */
    private void add(Reason reason) {
        reasons.add(reason);
    }

    /**
     * Add reasons to this partial score. Please note that the total value of this partial score remains unchanged,
     * which is why this method is (and must remain) private.
     *
     * @param reasons to add.
     */
    private void addReasons(Set<Reason> reasons) {
        for (Reason reason : reasons) {
            add(reason);
        }
    }

    /**
     * Get the total value of this partial score.
     *
     * @return value.
     */
    public float getValue() {
        return value.get();
    }

    /**
     * Set a new total value of this partial score. Intended to be used by {@link com.graphaware.reco.generic.transform.ScoreTransformer}s.
     * Will not change anything about the already encapsulated {@link com.graphaware.reco.generic.result.Reason}s.
     *
     * @param value new total value.
     * @param details of the transformation.
     */
    public void setNewValue(float value, Map<String, ?> details) {
        add(-(this.value.get() - value), details);
    }

    /**
     * Get the reasons encapsulated by this partial score.
     *
     * @return reasons, may be empty but will never be <code>null</code>.
     */
    public Set<Reason> getReasons() {
        return reasons;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (reasons.isEmpty()) {
            return String.valueOf(value);
        }

        StringBuilder result = new StringBuilder("{value:").append(value);

        for (Reason reason : reasons) {
            result.append(", ").append(reason);
        }

        result.append("}");

        return result.toString();
    }
}
