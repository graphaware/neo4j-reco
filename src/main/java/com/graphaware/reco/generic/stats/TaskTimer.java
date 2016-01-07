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

package com.graphaware.reco.generic.stats;

/**
 * A component timing the execution of named tasks.
 */
public interface TaskTimer {

    /**
     * Start timing a task.
     *
     * @param task name of the task, must not be <code>null</code> or blank.
     */
    void startTiming(String task);

    /**
     * Get the time it has taken the given task to run up until now.
     *
     * @param task to find time for.
     * @return time in ms, -1 if the task hasn't been started.
     */
    long getTime(String task);
}
