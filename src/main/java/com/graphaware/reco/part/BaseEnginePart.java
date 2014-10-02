package com.graphaware.reco.part;

import com.graphaware.reco.filter.Filter;
import com.graphaware.reco.transform.NoTransformation;
import com.graphaware.reco.transform.ScoreTransformer;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.*;

/**
 * Base class for Neo4j-based {@link EnginePart} implementations. Handles filtering of items using {@link Filter}s and
 * transformation of scores using {@link ScoreTransformer}s.
 *
 * @param <OUT> type of the recommendations produced.
 * @param <IN>  type of the item recommendations are for / based on.
 */
public abstract class BaseEnginePart<OUT, IN> implements EnginePart<OUT, IN> {

    protected final GraphDatabaseService database;
    private final ScoreTransformer transformer;
    private final List<Filter<OUT, IN>> filters;

    protected BaseEnginePart(GraphDatabaseService database) {
        this(database, NoTransformation.getInstance());
    }

    protected BaseEnginePart(GraphDatabaseService database, ScoreTransformer transformer) {
        this(database, transformer, Collections.<Filter<OUT, IN>>emptyList());
    }

    protected BaseEnginePart(GraphDatabaseService database, List<Filter<OUT, IN>> filters) {
        this(database, NoTransformation.getInstance(), filters);
    }

    protected BaseEnginePart(GraphDatabaseService database, ScoreTransformer transformer, List<Filter<OUT, IN>> filters) {
        this.database = database;
        this.transformer = transformer;
        this.filters = filters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Map<OUT, Integer> recommend(IN input, int limit, Set<OUT> blacklist) {
        Map<OUT, Integer> result = new HashMap<>();

        populateResult(result, input, limit, blacklist);

        return transformer.transform(result);
    }

    /**
     * Populate the given empty result with recommendations. It is highly recommended to use {@link #addToResult(java.util.Map, Object, java.util.Set, Object, int)}
     * to do that.
     *
     * @param result    to populate.
     * @param input     to compute recommendations for.
     * @param limit     maximum number of total recommendations to produce by the whole engine (i.e. not just this part). Can be used to guide or terminate the search.
     * @param blacklist items that must not be recommended.
     */
    protected abstract void populateResult(Map<OUT, Integer> result, IN input, int limit, Set<OUT> blacklist);

    /**
     * Add a potential recommendation to the overall result. Perform checks that the recommendation should actually be
     * used based on the blacklist provided and filters configured. Nothing will happen if the item is found to be
     * blacklisted/filtered, i.e., it will be silently ignore.
     *
     * @param result         to add to.
     * @param input          for which the recommendation has been computed.
     * @param blacklist      of recommendations.
     * @param recommendation that has been computed.
     * @param score          score associated with this recommendation. This could be (and in many cases will) just 1 for
     *                       each occurrence of the recommended item. There will be cases where the score changes for each
     *                       recommendation computed by the engine part, for instance, when strengths of friendships or
     *                       product likes are known.
     */
    protected void addToResult(Map<OUT, Integer> result, IN input, Set<OUT> blacklist, OUT recommendation, int score) {
        if (blacklist.contains(recommendation)) {
            return;
        }

        boolean include = true;
        for (Filter<OUT, IN> filter : filters) {
            if (!filter.include(recommendation, input)) {
                include = false;
                break;
            }
        }
        if (include) {
            if (!result.containsKey(recommendation)) {
                result.put(recommendation, 0);
            }
            result.put(recommendation, result.get(recommendation) + score);
        }
    }

    @Override
    public boolean isOptional() {
        return false;
    }
}
