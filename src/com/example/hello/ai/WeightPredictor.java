package com.example.hello.ai;

import com.example.hello.data.SetRecord;

import java.util.List;

/**
 * Predicts the next weight for an exercise. The app only uses this interface, so the
 * rule-based placeholder can be replaced by a real AI model later.
 */
public interface WeightPredictor {
    Suggestion suggestNext(String exercise, List<SetRecord> lastSession);
}
