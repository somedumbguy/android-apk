package com.example.hello.ui.format;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import com.example.hello.domain.fakes.TestClocks;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;

public class FormatsTest {

    @Test public void relativeTime() {
        assertEquals("Today", Formats.relative(TestClocks.NOW, TestClocks.NOW));
        assertEquals("Yesterday", Formats.relative(TestClocks.daysAgo(1), TestClocks.NOW));
        assertEquals("6 days ago", Formats.relative(TestClocks.daysAgo(6), TestClocks.NOW));
        assertEquals("Last week", Formats.relative(TestClocks.daysAgo(9), TestClocks.NOW));
        assertEquals("3 weeks ago", Formats.relative(TestClocks.daysAgo(24), TestClocks.NOW));
    }

    @Test public void greetingByTimeOfDay() {
        assertEquals("Up late", Formats.greeting(LocalTime.of(2, 0)));
        assertEquals("Good morning", Formats.greeting(LocalTime.of(9, 0)));
        assertEquals("Good afternoon", Formats.greeting(LocalTime.of(14, 0)));
        assertEquals("Good evening", Formats.greeting(LocalTime.of(19, 0)));
    }

    @Test public void dates() {
        LocalDate d = LocalDate.of(2026, 11, 21);
        assertEquals("SATURDAY, NOV 21", Formats.headerDate(d, Locale.US));
        assertEquals("Jan 7", Formats.shortDate(LocalDate.of(2027, 1, 7), Locale.US));
        assertArrayEquals(new String[] {"S", "M", "T", "W", "T", "F", "S"}, Formats.last7DayLetters(d, Locale.US));
    }

    @Test public void numbers() {
        assertEquals("18,420", Formats.thousands(18420));
        assertEquals("192.5 lb", Formats.lb(192.5));
        assertEquals("2:05", Formats.minutesSeconds(125));
    }

    @Test public void deltas() {
        assertEquals("▲ +5 LB VS LAST SESSION", Formats.delta(5));
        assertEquals("▼ -20 LB · DELOAD", Formats.delta(-20));
        assertEquals("= SAME AS LAST SESSION", Formats.delta(0));
    }
}
