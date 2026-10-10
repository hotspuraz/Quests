package com.person98.quests.task;

import com.person98.quests.Quests;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

/** Checks every minute whether the daily quests are due; the rule itself lives in the quest controller. */
public class DailyQuestResetTask extends BukkitRunnable {
   private final Quests plugin;

   public DailyQuestResetTask(Quests plugin) {
      this.plugin = plugin;
   }

   public void run() {
      boolean reset;
      try {
         reset = this.plugin.getQuestController().ensureDailyQuestsUpToDate();
      } catch (RuntimeException exception) {
         // Nothing was recorded as reset, so the next run retries.
         this.plugin.getLogger().log(Level.SEVERE, "Daily quest reset aborted; it will be retried next minute", exception);
         return;
      }
      if (!reset) {
         return;
      }
      this.plugin.getLogger().info("Daily quests have been reset.");
      List<String> msgLines = this.plugin.getConfig().getStringList("daily-quest-reset-message");
      for (String line : msgLines) {
         Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', line));
      }
   }
}
