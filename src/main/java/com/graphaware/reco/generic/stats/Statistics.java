package com.graphaware.reco.generic.stats;

import java.util.Map;

/**
 * Statistics about the recommendation-computing process. Implementations must be thread-safe.
 */
public interface Statistics {

    public static final String TOTAL_TIME = "total time";

    static final String ELAPSED_TIME = "elapsed time";
    static final String CANDIDATE_ITEMS = "candidate items";
    static final String BLACKLISTED_ITEMS = "blacklisted items";
    static final String FILTERED_ITEMS = "filtered items";
    static final String TOTAL_ITEMS = "total items";

    /**
     * Start timing a task.
     *
     * @param task name of the task, must not be <code>null</code> or blank. Typically name of a {@link com.graphaware.reco.generic.engine.RecommendationEngine}.
     */
    void startTiming(String task);

    /**
     * Stop timing a task and record the time it took in statistics.
     *
     * @param task name of the task, must not be <code>null</code> or blank. Typically name of a {@link com.graphaware.reco.generic.engine.RecommendationEngine}.
     */
    void stopTiming(String task);

    /**
     * Return the number of milliseconds the task has been running so far.
     * @param task name of the task, must not be <code>null</code> or blank. Typically name of a {@link com.graphaware.reco.generic.engine.RecommendationEngine}.
     * @return number of milliseconds the task has been running so far.
     */
    long getTime(String task);

    /**
     * Collect a generic statistic.
     *
     * @param task  name of the task being measured, must not be <code>null</code> or blank. Typically name of a {@link com.graphaware.reco.generic.engine.RecommendationEngine}.
     * @param name  name of the measure / statistic, must not be <code>null</code> or blank.
     * @param value value, must not be <code>null</code>.
     */
    void addStatistic(String task, String name, Object value);

    /**
     * Increment an integer statistic by 1.
     *
     * @param task name of the task being measured, must not be <code>null</code> or blank. Typically name of a {@link com.graphaware.reco.generic.engine.RecommendationEngine}.
     * @param name name of the measure / statistic, must not be <code>null</code> or blank.
     */
    void incrementStatistic(String task, String name);

    /**
     * Get collected statistics.
     *
     * @return stats. <Task Name, <Measure Name, Measure Value>>
     */
    Map<String, ? extends Map<String, Object>> get();
}
