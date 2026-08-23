package com.person98.quests.task;

import com.person98.quests.Quests;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

public class DailyQuestResetTask extends BukkitRunnable {
   private final Quests plugin;
   private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
   private String lastResetDate = "";

   public DailyQuestResetTask(Quests plugin) {
      this.plugin = plugin;
   }

   public void run() {
      String resetTime = this.plugin.getConfig().getString("reset-time", "00:00");
      String now = LocalTime.now().format(this.timeFormatter);
      LocalDate today = LocalDate.now();
      try {
         LocalTime.parse(resetTime, this.timeFormatter);
      } catch (DateTimeParseException exception) {
         this.plugin.getLogger().warning("Invalid reset-time '" + resetTime + "'; expected HH:mm.");
         return;
      }
      if (now.equals(resetTime) && (this.lastResetDate.isEmpty() || LocalDate.parse(this.lastResetDate).isBefore(today))) {
         try {
            this.plugin.getUserController().resetAllDailyQuestProgress();
         } catch (RuntimeException exception) {
            this.plugin.getLogger().log(java.util.logging.Level.SEVERE, "Daily quest reset aborted because progress could not be cleared", exception);
            return;
         }
         this.plugin.getQuestController().selectNewDailyQuests(5);
         this.plugin.getQuestController().savePersistedDailyQuests(today.toString());
         this.lastResetDate = today.toString();
         Bukkit.getLogger().info("[Quests] Daily quests have been reset at " + now);
         List<String> msgLines = this.plugin.getConfig().getStringList("daily-quest-reset-message");
         if (!msgLines.isEmpty()) {
            for (String line : msgLines) {
               Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', line));
            }
         }
      }
   }
}
