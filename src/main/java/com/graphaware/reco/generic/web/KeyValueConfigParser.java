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

package com.graphaware.reco.generic.web;

import com.graphaware.reco.generic.config.KeyValueConfig;
import com.graphaware.reco.generic.config.MapBasedConfig;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.util.Assert.hasLength;

/**
 * {@link ConfigParser} producing {@link KeyValueConfig}. By default, it produces {@link MapBasedConfig} with {@link String}
 * values.
 */
public class KeyValueConfigParser implements ConfigParser<KeyValueConfig> {

    private final String separator;

    /**
     * @param separator used for separating keys, values, and key-value pair from each other. Must not be <code>null</code> or empty.
     */
    public KeyValueConfigParser(String separator) {
        hasLength(separator);

        this.separator = separator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KeyValueConfig produceConfig(int limit, String config) {
        return new MapBasedConfig(limit, produceConfigMap(config, separator));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KeyValueConfig produceConfig(int limit, long maxTime, String config) {
        return new MapBasedConfig(limit, maxTime, produceConfigMap(config, separator));
    }

    /**
     * Produce a map from a String config and separator.
     *
     * @param config    as String.
     * @param separator separator.
     * @return kay-value map.
     */
    protected static Map<String, Object> produceConfigMap(String config, String separator) {
        Map<String, Object> result = new HashMap<>();

        if (StringUtils.isEmpty(config)) {
            return result;
        }

        String[] keyValue = config.split(separator);

        if (keyValue.length % 2 != 0) {
            throw new IllegalArgumentException("Config must contain an even number of tokens separated by " + separator);
        }

        for (int i = 0; i < keyValue.length - 1; i = i + 2) {
            result.put(keyValue[i], keyValue[i + 1]);
        }

        return result;
    }
}
