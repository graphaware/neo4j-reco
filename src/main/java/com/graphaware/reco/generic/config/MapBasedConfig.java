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

package com.graphaware.reco.generic.config;

import java.util.Collections;
import java.util.Map;

import static org.springframework.util.Assert.notNull;

/**
 * {@link Map}-based implementation of {@link KeyValueConfig}. After construction, this class is read-only, this thread-safe.
 */
public class MapBasedConfig extends SimpleConfig implements KeyValueConfig {

    private final Map<String, Object> values;

    /**
     * Construct a new config.
     *
     * @param limit  desired maximum number of produced recommendations. Must be positive.
     * @param values additional config. Must not be <code>null</code>.
     */
    public MapBasedConfig(int limit, Map<String, Object> values) {
        super(limit);

        notNull(values);
        this.values = Collections.unmodifiableMap(values);
    }

    /**
     * Construct a new config.
     *
     * @param limit   desired maximum number of produced recommendations. Must be positive.
     * @param maxTime desired maximum time in ms that the recommendation-computing process should take. Must be positive.
     * @param values  additional config. Must not be <code>null</code>.
     */
    public MapBasedConfig(int limit, long maxTime, Map<String, Object> values) {
        super(limit, maxTime);

        notNull(values);
        this.values = Collections.unmodifiableMap(values);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(String key) {
        return values.containsKey(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(String key) {
        if (!contains(key)) {
            throw new IllegalArgumentException("Config does not contain key " + key);
        }
        return values.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(String key, Object defaultValue) {
        if (!contains(key)) {
            return defaultValue;
        }

        return values.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T get(String key, Class<T> clazz) {
        Object result = get(key);

        if (!clazz.isAssignableFrom(result.getClass())) {
            throw new IllegalArgumentException(result.getClass() + " is not assignable from " + clazz);
        }

        return (T) result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T get(String key, T defaultValue, Class<T> clazz) {
        Object result = get(key, defaultValue);

        if (!clazz.isAssignableFrom(result.getClass())) {
            throw new IllegalArgumentException(result.getClass() + " is not assignable from " + clazz);
        }

        return (T) result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        MapBasedConfig that = (MapBasedConfig) o;

        return values.equals(that.values);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + values.hashCode();
        return result;
    }
}
