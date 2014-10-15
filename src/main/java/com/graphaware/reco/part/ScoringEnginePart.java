package com.graphaware.reco.part;

import com.graphaware.reco.filter.Filter;
import com.graphaware.reco.score.Recommendations;
import com.graphaware.reco.transform.NoTransformation;
import com.graphaware.reco.transform.ScoreTransformer;

import java.util.*;

/**
 * Base class for Neo4j-based {@link EnginePart} implementations. Handles filtering of items using {@link Filter}s and
 * transformation of scores using {@link ScoreTransformer}s.
 *
 * @param <OUT> type of the recommendations produced.
 * @param <IN>  type of the item recommendations are for / based on.
 */
public abstract class ScoringEnginePart<OUT, IN> implements EnginePart<OUT, IN> {

    private final ScoreTransformer transformer;
    private final List<Filter<OUT, IN>> filters;

    protected ScoringEnginePart() {
        this(NoTransformation.getInstance());
    }

    protected ScoringEnginePart(ScoreTransformer transformer) {
        this(transformer, Collections.<Filter<OUT, IN>>emptyList());
    }

    protected ScoringEnginePart(List<Filter<OUT, IN>> filters) {
        this(NoTransformation.getInstance(), filters);
    }

    protected ScoringEnginePart(ScoreTransformer transformer, List<Filter<OUT, IN>> filters) {
        this.transformer = transformer;
        this.filters = filters;
    }

    /**
     * Get the name of this engine part, so that its recommendations (and their scores) can be distinguished from other
     * parts' recommendations if needed.
     *
     * @return part name.
     */
    protected abstract String name();

    /**
     * {@inheritDoc}
     */
    @Override
    public void recommend(Recommendations<OUT> output, IN input, int limit, Set<OUT> blacklist, boolean realTime) {
        output.add(name(), recommend(input, limit, blacklist, realTime));
    }

    /**
     * Produce recommendations.
     *
     * @param input     input to the recommendation engine part. Typically the person or item recommendations are being computed for.
     * @param limit     desired maximum number of produced recommendations for the whole engine. Parts can take this into
     *                  account in order not to produce too many recommendations, if they can traverse the graph best-first manner.
     * @param blacklist of items that must not be recommended.
     * @param realTime  an indication whether the recommendations being computed are meant to be real-time (<code>true</code>)
     *                  or not (<code>false</code>). Implementations can choose to ignore it, but they can also choose
     *                  to make the recommendation process faster and less accurate for real-time scenarios and slower
     *                  but more accurate for pre-computed scenarios.
     * @return a map of recommendations, where key is the recommended item and value if the relevance score.
     */
    public final Map<OUT, Integer> recommend(IN input, int limit, Set<OUT> blacklist, boolean realTime) {
        Map<OUT, Integer> result = new HashMap<>();

        if (realTime) {
            populateResultRealTime(result, input, limit, blacklist);
        } else {
            populateResult(result, input, limit, blacklist);
        }

        return transformer.transform(result);
    }

    /**
     * Populate the given empty result with recommendations. It is highly recommended to use
     * {@link #addToResult(java.util.Map, Object, java.util.Set, Object, int)} to do that.
     *
     * @param result    to populate.
     * @param input     to compute recommendations for.
     * @param limit     maximum number of total recommendations to produce by the whole engine (i.e. not just this part). Can be used to guide or terminate the search.
     * @param blacklist items that must not be recommended.
     */
    protected abstract void populateResult(Map<OUT, Integer> result, IN input, int limit, Set<OUT> blacklist);

    /**
     * Populate the given empty result with recommendations in real-time. Implementations can choose to override this
     * method to perform a faster but perhaps less accurate recommendation search. It is highly recommended to use
     * {@link #addToResult(java.util.Map, Object, java.util.Set, Object, int)} to populate the result.
     *
     * @param result    to populate.
     * @param input     to compute recommendations for.
     * @param limit     maximum number of total recommendations to produce by the whole engine (i.e. not just this part). Can be used to guide or terminate the search.
     * @param blacklist items that must not be recommended.
     */
    protected void populateResultRealTime(Map<OUT, Integer> result, IN input, int limit, Set<OUT> blacklist) {
        populateResult(result, input, limit, blacklist);
    }

    /**
     * Add a potential recommendation to the overall result. Perform checks that the recommendation should actually be
     * used based on the blacklist provided and filters configured. Nothing will happen if the item is found to be
     * blacklisted/filtered, i.e., it will be silently ignored.
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
    protected final void addToResult(Map<OUT, Integer> result, IN input, Set<OUT> blacklist, OUT recommendation, int score) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public EnoughResultsPolicy enoughResultsPolicy() {
        return EnoughResultsPolicy.COMPUTE_AND_CONTINUE;
    }
}
