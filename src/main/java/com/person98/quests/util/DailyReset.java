package com.person98.quests.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/** Decides whether the daily quests are due for rotation. */
public final class DailyReset {
   private DailyReset() {
   }

   /**
    * A reset is due once the configured time of day has been reached and no reset has been recorded for today.
    * Because this is a catch-up rule rather than an exact-minute match, a missed minute (lag, downtime)
    * is repaired on the next check, and an already-recorded reset can never fire twice.
    *
    * @param lastResetDate ISO date of the last persisted reset; blank or unparsable counts as "never"
    */
   public static boolean isDue(String lastResetDate, LocalDate today, LocalTime now, LocalTime resetTime) {
      if (now.isBefore(resetTime)) {
         return false;
      }
      if (lastResetDate == null || lastResetDate.isBlank()) {
         return true;
      }
      try {
         return LocalDate.parse(lastResetDate.trim()).isBefore(today);
      } catch (DateTimeParseException exception) {
         return true;
      }
   }
}
