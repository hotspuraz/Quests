package com.person98.imevoquest.task;

import com.person98.imevoquest.ImevoQuest;
import com.person98.imevoquest.data.SimpleUser;
import com.person98.imevoquest.service.sql.Storage;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class LeaderboardExcludedRunnable extends BukkitRunnable {
   private boolean recalculating;
   private BukkitTask task;

   public void run() {
      if (!this.recalculating) {
         if (this.task != null) {
            Bukkit.getScheduler().cancelTask(this.task.getTaskId());
         }

         this.task = Bukkit.getScheduler().runTaskAsynchronously(ImevoQuest.getInstance(), () -> {
            this.recalculating = true;
            Storage storage = ImevoQuest.getInstance().getStorage();
            List<SimpleUser> users = storage.getLeaderboard();
            if (!users.isEmpty()) {
               for (SimpleUser user : users) {
                  OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(user.uniqueId());
                  if (offlinePlayer.hasPlayedBefore() && offlinePlayer.getName() != null) {
                     if (offlinePlayer.isOnline() || !this.isSpentDate(new Date(offlinePlayer.getLastPlayed()))) {
                        storage.getExcluded().remove(user.uniqueId().toString());
                     } else if (!storage.getExcluded().contains(user.uniqueId().toString())) {
                        storage.getExcluded().add(user.uniqueId().toString());
                     }
                  }
               }
            }

            this.recalculating = false;
         });
      }
   }

   private boolean isSpentDate(Date date) {
      Date now = new Date();
      long diffMillis = Math.abs(now.getTime() - date.getTime());
      long diff = TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS);
      return diff >= ImevoQuest.getInstance().getConfig().getInt("excluded_days");
   }
}
