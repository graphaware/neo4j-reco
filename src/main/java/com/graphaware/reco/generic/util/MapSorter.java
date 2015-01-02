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

package com.graphaware.reco.generic.util;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Utility class for sorting a map by descending value.
 */
public final class MapSorter {

    private MapSorter() {
    }

    /**
     * Helper method that sorts a map by its values descending.
     *
     * @param map to sort
     * @param <K> key type
     * @param <V> value type
     * @return sorted map.
     */
    public static <K, V extends Comparable<V>> SortedMap<K, V> sortMapByDescendingValue(Map<K, V> map) {
        SortedMap<K, V> sortedMap = new TreeMap<>(new MapValueDescComparator<>(map));
        sortedMap.putAll(map);
        return sortedMap;
    }

    /**
     * Comparator based on values stored in the map
     * When used with {@code SortedMap}, the keys with higher values will be scored higher
     */
    private static class MapValueDescComparator<K, V extends Comparable<V>> implements Comparator<K> {

        private Map<K, V> theMap;

        public MapValueDescComparator(Map<K, V> theMap) {
            this.theMap = theMap;
        }

        public int compare(K key1, K key2) {
            V val1 = this.theMap.get(key1);
            V val2 = this.theMap.get(key2);
            if (val2 == null) {
                return -1;
            }
            if (val1 == null) {
                return 1;
            }

            //the following three lines are a hack!
            //they are here so if that 2 values are the same, they are both added to the map.
            if (val2.compareTo(val1) == 0) {
                return -1;
            }

            return val2.compareTo(val1);
        }
    }
}
