package com.graphaware.reco;

import com.graphaware.common.util.Pair;
import com.graphaware.reco.filter.Blacklist;
import com.graphaware.reco.part.EnginePart;
import com.graphaware.reco.post.PostProcessor;
import com.graphaware.reco.score.CompositeScore;
import com.graphaware.reco.score.Recommendations;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Base class for {@link Engine} implementations that delegate to {@link EnginePart}s but where the input/output
 * of the engine is not necessarily of the same type as the input/output of the parts.
 */
public abstract class Delegating<OUT, IN> {

    private final List<EnginePart<OUT, IN>> parts;
    private final List<Blacklist<OUT, IN>> blacklists;
    private final List<PostProcessor<OUT, IN>> postProcessors;

    protected Delegating(List<EnginePart<OUT, IN>> parts, List<Blacklist<OUT, IN>> blacklists, List<PostProcessor<OUT, IN>> postProcessors) {
        this.parts = parts;
        this.blacklists = blacklists;
        this.postProcessors = postProcessors;
    }

    /**
     * {@see {@link Engine#recommend(Object, int)}}.
     */
    public List<Pair<OUT, CompositeScore>> delegate(IN input, int limit) {
        Recommendations<OUT> recommendations = new Recommendations<>();

        Set<OUT> blacklist = buildBlacklist(input);

        for (EnginePart<OUT, IN> part : parts) {
            if (recommendations.hasEnough(limit) && part.isOptional()) {
                continue;
            }

            recommendations.add(part.name(), part.recommend(input, limit, blacklist));
        }

        for (PostProcessor<OUT, IN> postProcessor : postProcessors) {
            postProcessor.postProcess(recommendations, input);
        }

        return recommendations.get(limit);
    }

    /**
     * Build a single blacklist by consulting all constituent blacklists.
     *
     * @param input for which to build a blacklist.
     * @return single composite blacklist.
     */
    protected Set<OUT> buildBlacklist(IN input) {
        final Set<OUT> blackList = new HashSet<>();
        for (Blacklist<OUT, IN> blacklist : blacklists) {
            blackList.addAll(blacklist.getBlacklist(input));
        }
        return blackList;
    }
}
