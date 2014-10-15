package com.graphaware.reco.part;

/**
 * Policy for each {@link EnginePart} that specifies what to do when there are enough recommendations.
 * <p/>
 * The first part of the policy is whether to actually perform computation or skip it if there are enough results
 * by the time the {@link EnginePart} is asked to take part of the recommendation process.
 * <p/>
 * The second part of the policy is whether to continue delegating to other {@link EnginePart}s or stop the search entirely,
 * if there are enough results at the end of the computation performed by the {@link EnginePart} with this policy.
 */
public enum EnoughResultsPolicy {

    COMPUTE_AND_STOP(true, false),
    COMPUTE_AND_CONTINUE(true, true),
    SKIP_AND_STOP(false, false),
    SKIP_AND_CONTINUE(false, true),;

    private final boolean compute;
    private final boolean carryOn;

    private EnoughResultsPolicy(boolean compute, boolean carryOn) {
        this.compute = compute;
        this.carryOn = carryOn;
    }

    public boolean compute() {
        return compute;
    }

    public boolean carryOn() {
        return carryOn;
    }
}
