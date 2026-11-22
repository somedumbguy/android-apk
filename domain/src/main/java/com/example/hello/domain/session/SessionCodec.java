package com.example.hello.domain.session;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Saves a workout's progress as text and restores it onto a freshly built session.
 *
 * <p>Format: {@code 1|<startedAtMillis>|<plan name>|<sets>}, where sets are
 * {@code weight:target:reps} (reps -1 = not done), comma-separated within an exercise
 * (warm-ups first), and exercises are separated by '/'. Restoring fails, rather than
 * guessing, when the saved sets don't match the fresh session's prescription.
 */
public final class SessionCodec {
    static final String VERSION = "1";

    private SessionCodec() {}

    public static String encode(WorkoutSession s) {
        StringBuilder sb = new StringBuilder();
        sb.append(VERSION).append('|').append(s.startedAt.toEpochMilli()).append('|');
        sb.append(s.plan.name.replace("|", "")).append('|');
        for (int e = 0; e < s.entries.size(); e++) {
            if (e > 0) sb.append('/');
            List<SetEntry> sets = allSets(s.entries.get(e));
            for (int i = 0; i < sets.size(); i++) {
                if (i > 0) sb.append(',');
                SetEntry set = sets.get(i);
                sb.append(set.weight).append(':').append(set.target).append(':').append(set.reps());
            }
        }
        return sb.toString();
    }

    /**
     * The saved progress applied to {@code fresh}, or empty if it doesn't match. On success the
     * sets of {@code fresh} are updated, so pass a session nothing else is using.
     */
    public static Optional<WorkoutSession> restore(String saved, WorkoutSession fresh) {
        try {
            String[] parts = saved.split("\\|", -1);
            if (parts.length != 4 || !VERSION.equals(parts[0])) return Optional.empty();
            if (!parts[2].equals(fresh.plan.name.replace("|", ""))) return Optional.empty();
            Instant startedAt = Instant.ofEpochMilli(Long.parseLong(parts[1]));

            String[] exercises = parts[3].split("/", -1);
            if (exercises.length != fresh.entries.size()) return Optional.empty();

            List<int[]> reps = new ArrayList<>();
            for (int e = 0; e < exercises.length; e++) {
                List<SetEntry> sets = allSets(fresh.entries.get(e));
                String[] savedSets = exercises[e].isEmpty() ? new String[0] : exercises[e].split(",");
                if (savedSets.length != sets.size()) return Optional.empty();
                int[] r = new int[sets.size()];
                for (int i = 0; i < savedSets.length; i++) {
                    String[] f = savedSets[i].split(":");
                    SetEntry set = sets.get(i);
                    if (Double.parseDouble(f[0]) != set.weight || Integer.parseInt(f[1]) != set.target) {
                        return Optional.empty();
                    }
                    r[i] = Integer.parseInt(f[2]);
                }
                reps.add(r);
            }

            // Everything matched, so it's safe to apply.
            for (int e = 0; e < reps.size(); e++) {
                List<SetEntry> sets = allSets(fresh.entries.get(e));
                for (int i = 0; i < sets.size(); i++) {
                    int r = reps.get(e)[i];
                    if (r >= 0) sets.get(i).record(r);
                }
            }
            return Optional.of(new WorkoutSession(fresh.plan, fresh.entries, startedAt));
        } catch (RuntimeException malformed) {
            return Optional.empty();
        }
    }

    private static List<SetEntry> allSets(ExerciseEntry e) {
        List<SetEntry> out = new ArrayList<>(e.warmups);
        out.addAll(e.work);
        return out;
    }
}
