package com.example.hello.data;

import com.example.hello.domain.model.SetRecord;

import java.util.ArrayList;
import java.util.List;

/** Encodes sets as "weight:reps:target;..." for simple key-value storage. */
final class SetRecordCodec {
    private SetRecordCodec() {}

    static String encode(List<SetRecord> sets) {
        StringBuilder sb = new StringBuilder();
        for (SetRecord s : sets) {
            if (sb.length() > 0) sb.append(';');
            sb.append(s.weight).append(':').append(s.reps).append(':').append(s.targetReps);
        }
        return sb.toString();
    }

    /** Returns an empty list for blank or malformed input rather than crashing the app. */
    static List<SetRecord> decode(String s) {
        List<SetRecord> out = new ArrayList<>();
        if (s == null || s.isEmpty()) return out;
        try {
            for (String part : s.split(";")) {
                String[] f = part.split(":");
                out.add(new SetRecord(Double.parseDouble(f[0]), Integer.parseInt(f[1]), Integer.parseInt(f[2])));
            }
        } catch (RuntimeException e) {
            return new ArrayList<>();
        }
        return out;
    }
}
