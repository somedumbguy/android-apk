package com.example.hello.domain.voice;

import com.example.hello.domain.catalog.Catalog;
import com.example.hello.domain.model.Exercise;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads spoken or typed logs like "squat 3 sets of 5 at 280", "bench 190 for 5", "3 by 5
 * squat" or "did 4". Deterministic: no AI, the same words always give the same result.
 */
public final class LogParser {
    private LogParser() {}

    /** What was understood; anything not mentioned is empty. */
    public static final class Parsed {
        public final Optional<Exercise> exercise;
        public final OptionalInt sets;
        public final OptionalInt reps;
        public final OptionalDouble weight;

        Parsed(Optional<Exercise> exercise, OptionalInt sets, OptionalInt reps, OptionalDouble weight) {
            this.exercise = exercise;
            this.sets = sets;
            this.reps = reps;
            this.weight = weight;
        }

        /** True when nothing useful was heard. */
        public boolean isEmpty() {
            return !exercise.isPresent() && !reps.isPresent() && !sets.isPresent() && !weight.isPresent();
        }
    }

    /** Spoken names, longest first so "bench press" wins over "press". */
    private static final Map<String, Exercise> ALIASES = new LinkedHashMap<>();

    static {
        ALIASES.put("incline dumbbell press", Catalog.INCLINE_DB);
        ALIASES.put("triceps pushdown", Catalog.PUSHDOWN);
        ALIASES.put("tricep pushdown", Catalog.PUSHDOWN);
        ALIASES.put("overhead press", Catalog.OVERHEAD_PRESS);
        ALIASES.put("shoulder press", Catalog.OVERHEAD_PRESS);
        ALIASES.put("military press", Catalog.OVERHEAD_PRESS);
        ALIASES.put("barbell row", Catalog.BARBELL_ROW);
        ALIASES.put("bench press", Catalog.BENCH);
        ALIASES.put("dead lift", Catalog.DEADLIFT);
        ALIASES.put("deadlift", Catalog.DEADLIFT);
        ALIASES.put("pushdown", Catalog.PUSHDOWN);
        ALIASES.put("incline", Catalog.INCLINE_DB);
        ALIASES.put("squats", Catalog.SQUAT);
        ALIASES.put("squat", Catalog.SQUAT);
        ALIASES.put("bench", Catalog.BENCH);
        ALIASES.put("rows", Catalog.BARBELL_ROW);
        ALIASES.put("row", Catalog.BARBELL_ROW);
        ALIASES.put("ohp", Catalog.OVERHEAD_PRESS);
        ALIASES.put("o h p", Catalog.OVERHEAD_PRESS);
        ALIASES.put("press", Catalog.OVERHEAD_PRESS);
    }

    private static final String[] NUMBER_WORDS = {
        "zero",
        "one",
        "two",
        "three",
        "four",
        "five",
        "six",
        "seven",
        "eight",
        "nine",
        "ten",
        "eleven",
        "twelve",
        "thirteen",
        "fourteen",
        "fifteen",
        "sixteen",
        "seventeen",
        "eighteen",
        "nineteen",
        "twenty"
    };

    private static final String NUM = "(\\d+(?:\\.\\d+)?)";
    private static final Pattern SETS_OF_REPS =
            Pattern.compile("\\b" + NUM + " sets? (?:of )?" + NUM + "(?: reps?)?\\b");
    private static final Pattern SETS_BY_REPS = Pattern.compile("\\b" + NUM + " ?(?:x|by|times) ?" + NUM + "\\b");
    private static final Pattern AT_WEIGHT = Pattern.compile("\\b(?:at|with|@) " + NUM + "(?: ?(?:lbs?|pounds?))?\\b");
    private static final Pattern WEIGHT_FOR_REPS =
            Pattern.compile("\\b" + NUM + "(?: ?(?:lbs?|pounds?))? for " + NUM + "\\b");
    private static final Pattern REPS_ONLY =
            Pattern.compile("\\b(?:did |got |hit )?" + NUM + " reps?\\b|\\b(?:did|got|hit) " + NUM + "\\b");
    private static final Pattern WEIGHT_UNIT = Pattern.compile("\\b" + NUM + " ?(?:lbs?|pounds?)\\b");

    public static Parsed parse(String said) {
        String text = normalize(said);
        Optional<Exercise> exercise = Optional.empty();
        for (Map.Entry<String, Exercise> a : ALIASES.entrySet()) {
            if (Pattern.compile("\\b" + Pattern.quote(a.getKey()) + "\\b")
                    .matcher(text)
                    .find()) {
                exercise = Optional.of(a.getValue());
                break;
            }
        }

        OptionalInt sets = OptionalInt.empty();
        OptionalInt reps = OptionalInt.empty();
        OptionalDouble weight = OptionalDouble.empty();

        Matcher m = SETS_OF_REPS.matcher(text);
        if (m.find()) {
            sets = OptionalInt.of(intOf(m.group(1)));
            reps = OptionalInt.of(intOf(m.group(2)));
        } else if ((m = SETS_BY_REPS.matcher(text)).find()) {
            sets = OptionalInt.of(intOf(m.group(1)));
            reps = OptionalInt.of(intOf(m.group(2)));
        }

        if ((m = AT_WEIGHT.matcher(text)).find()) {
            weight = OptionalDouble.of(Double.parseDouble(m.group(1)));
        } else if ((m = WEIGHT_FOR_REPS.matcher(text)).find()) {
            weight = OptionalDouble.of(Double.parseDouble(m.group(1)));
            if (!reps.isPresent()) reps = OptionalInt.of(intOf(m.group(2)));
        } else if ((m = WEIGHT_UNIT.matcher(text)).find()) {
            weight = OptionalDouble.of(Double.parseDouble(m.group(1)));
        }

        if (!reps.isPresent() && (m = REPS_ONLY.matcher(text)).find()) {
            reps = OptionalInt.of(intOf(m.group(1) != null ? m.group(1) : m.group(2)));
        }
        return new Parsed(exercise, sets, reps, weight);
    }

    /** Lower case, spoken numbers as digits, punctuation removed. */
    static String normalize(String said) {
        String text = said.toLowerCase(Locale.US).replaceAll("[^a-z0-9.@ ]", " ");
        List<String> words = new ArrayList<>();
        for (String w : text.trim().split("\\s+")) words.add(wordToDigits(w));
        return String.join(" ", words).replaceAll("(\\d)\\s*x\\s*(\\d)", "$1 x $2");
    }

    private static String wordToDigits(String w) {
        for (int i = 0; i < NUMBER_WORDS.length; i++) if (NUMBER_WORDS[i].equals(w)) return String.valueOf(i);
        return w;
    }

    private static int intOf(String s) {
        return (int) Math.round(Double.parseDouble(s));
    }
}
