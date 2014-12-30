package com.graphaware.reco.generic.policy;

import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.result.Recommendations;

/**
 * A participation policy for {@link com.graphaware.reco.generic.engine.RecommendationEngine}s deciding whether to participate
 * in the recommendation process for the given input and context.
 */
public interface ParticipationPolicy<OUT, IN> {

    static ParticipationPolicy ALWAYS = new ParticipationPolicy() {
        @Override
        public boolean participate(Object input, Context context, Recommendations recommendations) {
            return true;
        }
    };

    static ParticipationPolicy NEVER = new ParticipationPolicy() {
        @Override
        public boolean participate(Object input, Context context, Recommendations recommendations) {
            return false;
        }
    };

    static ParticipationPolicy IF_MORE_RESULTS_NEEDED = new ParticipationPolicy() {
        @Override
        public boolean participate(Object input, Context context, Recommendations recommendations) {
            return !recommendations.hasEnough(context.limit());
        }
    };

    /**
     * Decide whether to participate or not.
     *
     * @param input           for which recommendations are being computed.
     * @param context         in which recommendations are being computed.
     * @param recommendations recommendations produced so far.
     * @return true iff participate.
     */
    boolean participate(IN input, Context<OUT, IN> context, Recommendations<OUT> recommendations);
}
