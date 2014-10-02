package com.graphaware.reco;

import com.graphaware.common.util.Pair;
import com.graphaware.reco.filter.Blacklist;
import com.graphaware.reco.part.EnginePart;
import com.graphaware.reco.post.PostProcessor;
import com.graphaware.reco.score.CompositeScore;

import java.util.List;

/**
 * {@link Delegating} {@link Engine}.
 * <p/>
 * Intended for {@link Delegating} {@link Engine} implementations in which the input/output types of the engine are the
 * same as the input/output types of the parts that the engine delegates to.
 */
public class DelegatingEngine<OUT, IN> extends Delegating<OUT, IN> implements Engine<OUT, IN> {

    public DelegatingEngine(List<EnginePart<OUT, IN>> engineParts, List<Blacklist<OUT, IN>> blacklists, List<PostProcessor<OUT, IN>> postProcessors) {
        super(engineParts, blacklists, postProcessors);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Pair<OUT, CompositeScore>> recommend(IN input, int limit, boolean realTime) {
        return delegate(input, limit, realTime);
    }
}
