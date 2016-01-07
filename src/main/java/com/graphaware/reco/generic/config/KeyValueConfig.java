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

package com.graphaware.reco.generic.config;

/**
 * {@link Config} with additional (arbitrary) key-value pairs.
 */
public interface KeyValueConfig extends Config {

    /**
     * Check if the config contains the given key.
     *
     * @param key to check for, must not be <code>null</code> or empty.
     * @return true iff there's a value associated with the key.
     */
    boolean contains(String key);

    /**
     * Get the value associated with the given key.
     *
     * @param key key, must not be <code>null</code> or empty.
     * @return value associated with the key.
     * @throws IllegalArgumentException in case {@link #contains(String)} for the same key returns <code>false</code>.
     */
    Object get(String key);

    /**
     * Get the value associated with the given key.
     *
     * @param key          key, must not be <code>null</code> or empty.
     * @param defaultValue value to be returned if {@link #contains(String)} for the same key returns <code>false</code>.
     * @return value associated with the key (or default if there is none).
     */
    Object get(String key, Object defaultValue);

    /**
     * Get the value associated with the given key.
     *
     * @param key   key, must not be <code>null</code> or empty.
     * @param clazz class of the value.
     * @return value associated with the key.
     * @throws IllegalArgumentException in case {@link #contains(String)} for the same key returns <code>false</code>,
     *                                  or if the value associated with the key cannot be cast to the provided class.
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * Get the value associated with the given key.
     *
     * @param key          key, must not be <code>null</code> or empty.
     * @param defaultValue value to be returned if {@link #contains(String)} for the same key returns <code>false</code>.
     * @param clazz        class of the value.
     * @return value associated with the key (or default if there is none).
     * @throws IllegalArgumentException in case the value associated with the key cannot be cast to the provided class.
     */
    <T> T get(String key, T defaultValue, Class<T> clazz);
}
