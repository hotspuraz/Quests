package com.person98.quests.service.sql;

import com.google.common.collect.Lists;
import com.person98.quests.Quests;
import com.person98.quests.data.SimpleUser;
import com.person98.quests.data.User;
import com.person98.quests.service.sql.impl.StorageImplementation;
import java.io.File;
import java.time.Duration;
import java.util.Collection;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Set;
import org.bukkit.configuration.file.YamlConfiguration;

public class Storage {
   private final Quests quests;
   protected final StorageImplementation implementation;
   private Long lastRecalculated;
   private final AtomicBoolean recalculating = new AtomicBoolean();
   private volatile List<SimpleUser> leaderboard = List.of();
   private final Set<String> excluded = ConcurrentHashMap.newKeySet();
   private final WriteQueue writes;

   public Storage(Quests quests) {
      this.quests = quests;
      this.writes = new WriteQueue(quests.getLogger(), Duration.ofSeconds(2));
      this.implementation = new StorageImplementation(this.quests);
   }

   public User loadUser(UUID uniqueId) {
      return this.implementation.loadUser(uniqueId);
   }

   public void saveUser(User... users) {
      long nextUpdate = System.currentTimeMillis() + 180000L;
      for (User user : users) {
         user.setLastUpdate(nextUpdate);
      }
      User[] snapshots = Arrays.stream(users).map(User::snapshot).toArray(User[]::new);
      this.writes.submit(
         "save " + users.length + " user(s)",
         () -> this.implementation.saveUser(snapshots),
         // Make the periodic saver pick these users up again on its next run instead of waiting three minutes.
         failure -> {
            for (User user : users) {
               user.setLastUpdate(0L);
            }
         }
      );
   }

   /** Returns false (after logging) if the save could not be completed. */
   public boolean saveUserSync(User... users) {
      User[] snapshots = Arrays.stream(users).map(User::snapshot).toArray(User[]::new);
      return this.writes.runNow("save " + users.length + " user(s)", () -> this.implementation.saveUser(snapshots));
   }

   /** Returns false (after logging) if the reset could not be completed. */
   public boolean resetUser(UUID uniqueId) {
      return this.writes.runNow("reset " + uniqueId, () -> this.implementation.reset(uniqueId));
   }

   public List<SimpleUser> getLeaderboard() {
      if ((this.lastRecalculated == null || this.lastRecalculated <= System.currentTimeMillis())
         && this.recalculating.compareAndSet(false, true)) {
         this.lastRecalculated = System.currentTimeMillis() + 60000L;
         this.implementation.recalculateLeaderboard(
            Set.copyOf(this.excluded),
            calculated -> {
               this.leaderboard = calculated;
               this.recalculating.set(false);
            },
            failure -> {
               this.quests.getLogger().log(java.util.logging.Level.WARNING, "Could not recalculate the quest leaderboard", failure);
               this.recalculating.set(false);
            }
         );
      }

      return this.leaderboard;
   }

   public void saveDailyQuestProgress(User user) {
      User snapshot = user.snapshot();
      this.writes.submit("daily progress for " + snapshot.getUniqueId(), () -> this.implementation.saveDailyQuestProgress(snapshot), failure -> { });
   }

   public void saveDailyQuestProgressBatch(Collection<User> users) {
      List<User> snapshots = users.stream().map(User::snapshot).toList();
      this.writes.submit("daily progress for " + snapshots.size() + " user(s)", () -> this.implementation.saveDailyQuestProgressBatch(snapshots), failure -> { });
   }

   public void loadDailyQuestProgress(User user) {
      this.implementation.loadDailyQuestProgress(user);
      if (user.getOngoingDailyQuestId() == null && user.getDailyQuestProgress().isEmpty()) {
         this.loadDailyQuestProgressFromYaml(user);
      }
   }

   private void loadDailyQuestProgressFromYaml(User user) {
      File file = new File(this.quests.getDataFolder(), "daily_progress/" + user.getUniqueId().toString() + ".yml");
      if (file.exists()) {
         try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            user.setOngoingDailyQuestId(config.getString("ongoing", null));
            user.getDailyQuestProgress().clear();
            if (config.isConfigurationSection("progress")) {
               for (String key : config.getConfigurationSection("progress").getKeys(false)) {
                  int stageIndex = config.getInt("progress." + key + ".stageIndex", 0);
                  String stageDataJson = config.getString("progress." + key + ".stageDataJson", "{}");
                  boolean completed = config.getBoolean("progress." + key + ".completed", false);
                  user.getDailyQuestProgress().put(key, new User.DailyQuestProgress(stageIndex, stageDataJson, completed));
               }
            }

            this.implementation.saveDailyQuestProgress(user);
            if (file.exists()) {
               file.delete();
            }
         } catch (Exception exception) {
            this.quests.getLogger().log(java.util.logging.Level.WARNING, "Could not migrate legacy daily progress file " + file.getName(), exception);
         }
      }
   }

   public void wipeDailyQuestProgress() {
      this.flushPendingWrites();
      this.implementation.wipeAllDailyQuestProgress();
   }

   public void flushPendingWrites() {
      this.writes.flush();
   }

   public void shutdown() {
      this.writes.shutdown(Duration.ofSeconds(10));
   }

   /** Number of database writes abandoned after retries; useful for diagnostics. */
   public long getFailedWriteCount() {
      return this.writes.getFailureCount();
   }

   public Quests getQuests() {
      return this.quests;
   }

   public StorageImplementation getImplementation() {
      return this.implementation;
   }

   public Long getLastRecalculated() {
      return this.lastRecalculated;
   }

   public boolean isRecalculating() {
      return this.recalculating.get();
   }

   public Set<String> getExcluded() {
      return this.excluded;
   }
}
