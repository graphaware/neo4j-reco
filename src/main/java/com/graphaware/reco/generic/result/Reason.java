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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.util.Assert.notNull;

/**
 * One of the potentially many reasons a recommendation has been discovered. Encapsulates the raw (pre-transformation)
 * score contribution as well as additional details about the reason.
 * <p/>
 * For example, when discovering people one should be friends with based on the number of friends in common, each friend
 * in common would be converted to a single {@link com.graphaware.reco.generic.result.Reason} object. The encapsulated
 * {@link #value} would be the number of points assigned to the total score because of a particular friend in common, and
 * the {@link #details} could be a map of "name" -> "common friend's name" and "id" -> common friends node ID.
 * <p/>
 * This class is immutable, this thread-safe.
 */
public class Reason {

    private final float value;
    private final Map<String, Object> details = new HashMap<>();

    /**
     * Construct a new reason.
     *
     * @param value   score contribution.
     * @param details details, i.e. arbitrary key-value pairs that reveal some details about why a particular recommendation
     *                has been discovered. Must not be <code>null</code>. If there aren't any details, there should be
     *                no point in constructing this object.
     */
    public Reason(float value, Map<String, ?> details) {
        notNull(details);

        this.value = value;
        this.details.putAll(details);
    }

    /**
     * Get the raw (untransformed) score contribution of this {@link com.graphaware.reco.generic.result.Reason}. For information only,
     * no computation should be done with this value.
     *
     * @return value passed in at construction time.
     */
    public float getValue() {
        return value;
    }

    /**
     * Get the details encapsulated by this reason.
     *
     * @return details as arbitrary key-value pairs. May be empty, but will never be <code>null</code>. For information only.
     */
    public Map<String, Object> getDetails() {
        return Collections.unmodifiableMap(details);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("{value:").append(value);

        for (Map.Entry<String, Object> entry : details.entrySet()) {
            result.append(", ").append(entry.getKey()).append(":").append(entry.getValue());
        }

        result.append("}");

        return result.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Reason reason = (Reason) o;

        if (Float.compare(reason.value, value) != 0) return false;
        if (!details.equals(reason.details)) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = (value != +0.0f ? Float.floatToIntBits(value) : 0);
        result = 31 * result + details.hashCode();
        return result;
    }
}
