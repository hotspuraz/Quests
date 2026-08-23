package com.person98.quests.service.sql;

import com.google.common.collect.Lists;
import com.person98.quests.Quests;
import com.person98.quests.data.SimpleUser;
import com.person98.quests.data.User;
import com.person98.quests.service.sql.impl.StorageImplementation;
import java.io.File;
import java.util.Collection;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
   private final ExecutorService writes = Executors.newSingleThreadExecutor(runnable -> {
      Thread thread = new Thread(runnable, "Quests-SQL-Writer");
      thread.setDaemon(true);
      return thread;
   });

   public Storage(Quests quests) {
      this.quests = quests;
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
      this.writes.execute(() -> this.implementation.saveUser(snapshots));
   }

   public void saveUserSync(User... users) {
      this.flushPendingWrites();
      this.implementation.saveUser(Arrays.stream(users).map(User::snapshot).toArray(User[]::new));
   }

   public void resetUser(UUID uniqueId) {
      this.flushPendingWrites();
      this.implementation.reset(uniqueId);
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
      this.writes.execute(() -> this.implementation.saveDailyQuestProgress(snapshot));
   }

   public void saveDailyQuestProgressBatch(Collection<User> users) {
      List<User> snapshots = users.stream().map(User::snapshot).toList();
      this.writes.execute(() -> this.implementation.saveDailyQuestProgressBatch(snapshots));
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
            exception.printStackTrace();
         }
      }
   }

   public void wipeDailyQuestProgress() {
      this.flushPendingWrites();
      this.implementation.wipeAllDailyQuestProgress();
   }

   public void flushPendingWrites() {
      try {
         this.writes.submit(() -> { }).get();
      } catch (InterruptedException exception) {
         Thread.currentThread().interrupt();
         throw new IllegalStateException("Interrupted while flushing quest progress", exception);
      } catch (ExecutionException exception) {
         throw new IllegalStateException("Could not flush quest progress", exception.getCause());
      }
   }

   public void shutdown() {
      this.flushPendingWrites();
      this.writes.shutdown();
      try {
         if (!this.writes.awaitTermination(10, TimeUnit.SECONDS)) {
            this.quests.getLogger().warning("Quest database writer did not stop within 10 seconds.");
         }
      } catch (InterruptedException exception) {
         Thread.currentThread().interrupt();
      }
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
