package com.person98.quests.task;

import com.person98.quests.Quests;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
      LocalTime resetTimeParsed = LocalTime.parse(resetTime, DateTimeFormatter.ofPattern("HH:mm"));
      if (now.equals(resetTime) && (this.lastResetDate.isEmpty() || LocalDate.parse(this.lastResetDate).isBefore(today))) {
         this.plugin.getQuestController().selectNewDailyQuests(5);
         this.plugin.getQuestController().savePersistedDailyQuests(today.toString());
         this.plugin.getUserController().resetAllDailyQuestProgress();
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
