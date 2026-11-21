package com.example.hello.domain.progression;

import com.example.hello.domain.model.Exercise;
import com.example.hello.domain.model.SetRecord;
import com.example.hello.domain.model.Suggestion;
import java.util.List;

/**
 * One progression scheme. Implementations must be deterministic: the same inputs always give
 * the same prescription. Add a new scheme (5/3/1, GZCLP, ...) by adding a new strategy.
 */
public interface ProgressionStrategy {

    /**
     * @param last              the previous session's work sets; never empty
     * @param consecutiveFails  sessions in a row that missed the target
     */
    Suggestion next(Exercise exercise, List<SetRecord> last, Intensity intensity, int consecutiveFails);
}
