package com.person98.imevoquest.util;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Cooldown {
   private final Table<UUID, String, Long> cooldowns = HashBasedTable.create();

   public void add(UUID uuid, String key, long delay, TimeUnit unit) {
      this.cooldowns.put(uuid, key, System.currentTimeMillis() + unit.toMillis(delay));
   }

   public boolean contains(UUID uuid, String key) {
      return this.get(uuid, key) > 0L;
   }

   public boolean remove(UUID uuid, String key) {
      if (!this.cooldowns.contains(uuid, key)) {
         return false;
      } else {
         this.cooldowns.remove(uuid, key);
         return true;
      }
   }

   public long get(UUID uuid, String key) {
      if (!this.cooldowns.contains(uuid, key)) {
         return -1L;
      } else {
         long cooldown = this.millisLeft(uuid, key);
         return cooldown > 0L ? cooldown : -1L;
      }
   }

   public long millisLeft(UUID player, String key) {
      if (!this.cooldowns.contains(player, key)) {
         return 0L;
      } else if ((Long)this.cooldowns.get(player, key) <= System.currentTimeMillis()) {
         this.cooldowns.remove(player, key);
         return 0L;
      } else {
         return (Long)this.cooldowns.get(player, key) - System.currentTimeMillis();
      }
   }

   public int secondsLeft(UUID uuid, String key) {
      return (int)TimeUnit.MILLISECONDS.toSeconds(this.millisLeft(uuid, key)) + 1;
   }

   public int minutesLeft(UUID uuid, String key) {
      return (int)TimeUnit.MILLISECONDS.toMinutes(this.millisLeft(uuid, key)) + 1;
   }

   public String timeLeft(UUID uuid, String key) {
      int totalSeconds = this.secondsLeft(uuid, key);
      return TimeUtils.timeLeft(totalSeconds);
   }

   public Table<UUID, String, Long> getCooldowns() {
      return this.cooldowns;
   }
}
