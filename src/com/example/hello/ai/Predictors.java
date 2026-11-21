package com.example.hello.ai;

/** The one place that decides which predictor the app uses. Swap in a real AI model here. */
public final class Predictors {
    private Predictors() {}

    public static WeightPredictor create() {
        return new RuleBasedPredictor();
    }
}
