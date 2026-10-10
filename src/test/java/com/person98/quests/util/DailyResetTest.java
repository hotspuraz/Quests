package com.person98.quests.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class DailyResetTest {
   private static final LocalDate TODAY = LocalDate.of(2026, 10, 10);
   private static final LocalTime RESET = LocalTime.of(4, 0);

   @Test
   void notDueBeforeResetTime() {
      assertFalse(DailyReset.isDue("2026-10-09", TODAY, LocalTime.of(3, 59), RESET));
   }

   @Test
   void dueAtTheResetMinuteAndAfterIfNotYetResetToday() {
      assertTrue(DailyReset.isDue("2026-10-09", TODAY, LocalTime.of(4, 0), RESET));
      assertTrue(DailyReset.isDue("2026-10-09", TODAY, LocalTime.of(4, 1), RESET));
   }

   @Test
   void missedMinuteIsCaughtUpHoursLater() {
      assertTrue(DailyReset.isDue("2026-10-09", TODAY, LocalTime.of(17, 30), RESET));
   }

   @Test
   void neverFiresTwiceInOneDay() {
      assertFalse(DailyReset.isDue("2026-10-10", TODAY, LocalTime.of(4, 0), RESET));
      assertFalse(DailyReset.isDue("2026-10-10", TODAY, LocalTime.of(23, 59), RESET));
   }

   @Test
   void dueAfterServerWasDownForSeveralDays() {
      assertTrue(DailyReset.isDue("2026-10-05", TODAY, LocalTime.of(9, 0), RESET));
   }

   @Test
   void missingOrCorruptHistoryCountsAsNeverReset() {
      assertTrue(DailyReset.isDue("", TODAY, LocalTime.of(5, 0), RESET));
      assertTrue(DailyReset.isDue(null, TODAY, LocalTime.of(5, 0), RESET));
      assertTrue(DailyReset.isDue("not-a-date", TODAY, LocalTime.of(5, 0), RESET));
      assertFalse(DailyReset.isDue("", TODAY, LocalTime.of(3, 0), RESET));
   }

   @Test
   void midnightResetIsDueAllDay() {
      assertTrue(DailyReset.isDue("2026-10-09", TODAY, LocalTime.MIDNIGHT, LocalTime.MIDNIGHT));
   }
}
