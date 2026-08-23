package com.person98.quests.task;

import com.person98.quests.Quests;
import com.person98.quests.data.SimpleUser;
import com.person98.quests.service.sql.Storage;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

public class LeaderboardExcludedRunnable extends BukkitRunnable {
   private static final int PLAYERS_PER_RUN = 25;
   private int cursor;

   public void run() {
      Storage storage = Quests.getInstance().getStorage();
      List<SimpleUser> users = storage.getLeaderboard();
      if (users.isEmpty()) {
         this.cursor = 0;
         return;
      }

      int processed = 0;
      while (processed < PLAYERS_PER_RUN && this.cursor < users.size()) {
         SimpleUser user = users.get(this.cursor++);
         OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(user.uniqueId());
         if (offlinePlayer.hasPlayedBefore() && offlinePlayer.getName() != null) {
            if (offlinePlayer.isOnline() || !this.isSpentDate(new Date(offlinePlayer.getLastPlayed()))) {
               storage.getExcluded().remove(user.uniqueId().toString());
            } else {
               storage.getExcluded().add(user.uniqueId().toString());
            }
         }
         processed++;
      }

      if (this.cursor >= users.size()) {
         this.cursor = 0;
      }
   }

   private boolean isSpentDate(Date date) {
      Date now = new Date();
      long diffMillis = Math.abs(now.getTime() - date.getTime());
      long diff = TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS);
      return diff >= Quests.getInstance().getConfig().getInt("excluded_days");
   }
}
