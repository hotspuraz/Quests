package com.person98.imevoquest.data;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.person98.imevoquest.ImevoQuest;
import com.person98.imevoquest.data.stage.Stage;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class User {
   private final UUID uniqueId;
   private boolean viewStatus = true;
   private int points;
   private boolean story = true;
   private String lastDailyRewardDate;
   private final LinkedList<Quest> quests = Lists.newLinkedList();
   private final ConcurrentHashMap<Quest, Integer> currentQuests = new ConcurrentHashMap<>();
   private final ConcurrentHashMap<Quest, Stage> questsStage = new ConcurrentHashMap<>();
   private final LinkedList<Quest> completedTasks = Lists.newLinkedList();
   private final LinkedList<Quest> disabledQuests = Lists.newLinkedList();
   private transient long lastUpdate;
   private transient BossBar bossBar;
   private transient String nextQuest;
   private transient long storyDelay;
   private final Map<String, User.DailyQuestProgress> dailyQuestProgress = new ConcurrentHashMap<>();
   private String ongoingDailyQuestId;

   public User(UUID uniqueId) {
      this.uniqueId = uniqueId;
   }

   public Player getPlayer() {
      return Bukkit.getPlayer(this.uniqueId);
   }

   public Stage getCurrentStage(Quest quest) {
      try {
         for (Entry<Quest, Integer> entry : this.currentQuests.entrySet()) {
            if (entry.getKey().getName().equalsIgnoreCase(quest.getName())) {
               return entry.getKey().getStages().get(entry.getValue());
            }
         }
      } catch (Exception exception) {
         System.out.println("Error: " + exception);
      }

      return null;
   }

   public Stage getCurrentStageData(Quest quest) {
      return this.questsStage.containsKey(quest) ? this.questsStage.get(quest) : null;
   }

   public Quest getCurrentStoryQuest() {
      return this.quests.stream().filter(Quest::isStory).findFirst().orElse(null);
   }

   public Quest getCurrentDailyQuest() {
      if (this.ongoingDailyQuestId != null) {
         for (Quest quest : this.quests) {
            if (!quest.isStory() && quest.getIdentifier().equals(this.ongoingDailyQuestId) && !this.disabledQuests.contains(quest)) {
               return quest;
            }
         }
      }

      return this.quests.stream().filter(quest -> !quest.isStory() && !this.disabledQuests.contains(quest)).findFirst().orElse(null);
   }

   public List<Quest> getDailyQuests() {
      List<Quest> collect = this.quests.stream().filter(data -> !data.isStory()).collect(Collectors.toList());

      for (Entry<String, User.DailyQuestProgress> entry : this.dailyQuestProgress.entrySet()) {
         if (entry.getValue().completed) {
            Quest completedQuest = ImevoQuest.getInstance().getQuestController().find(entry.getKey());
            if (completedQuest != null && !completedQuest.isStory() && !collect.contains(completedQuest)) {
               collect.add(completedQuest);
            }
         }
      }

      return collect;
   }

   public boolean isAvailableDailyQuests() {
      return this.getCurrentStoryQuest() == null;
   }

   public void clearBossBar() {
      Player player = this.getPlayer();
      if (this.bossBar != null && player != null) {
         this.bossBar.removePlayer(player);
      }
   }

   public void updateBossBar() {
      Player player = this.getPlayer();
      if (player != null && player.isOnline()) {
         boolean remove = true;
         if (this.isViewStatus()) {
            Quest quest;
            if (this.isAvailableDailyQuests()) {
               quest = this.getCurrentDailyQuest();
            } else {
               quest = this.getCurrentStoryQuest();
            }

            if (quest != null) {
               Stage stage = this.getCurrentStage(quest);
               if (stage != null) {
                  if (this.bossBar != null && this.bossBar.isVisible()) {
                     this.bossBar.setTitle(stage.getBossTitle());
                  } else {
                     this.bossBar = Bukkit.createBossBar(stage.getBossTitle(), BarColor.BLUE, BarStyle.SOLID, new BarFlag[0]);
                  }

                  this.bossBar.setProgress(stage.getProgress(this.getCurrentStageData(quest)));
                  if (!this.bossBar.getPlayers().contains(player)) {
                     this.bossBar.addPlayer(player);
                  }

                  this.bossBar.setVisible(true);
                  remove = false;
               }
            }
         }

         if (remove && this.bossBar != null) {
            this.bossBar.removePlayer(player);
            this.bossBar.setVisible(false);
         }
      }
   }

   public void initializeQuest(Quest quest) {
      this.initializeQuest(quest, true);
   }

   public void initializeQuest(Quest quest, boolean notify) {
      if (quest != null) {
         if (!this.quests.contains(quest)) {
            this.quests.add(quest);
         }

         this.currentQuests.put(quest, 0);
         this.questsStage.put(quest, quest.getStages().getFirst().clone());
         Stage stage = this.getCurrentStage(quest);
         if (stage != null && notify) {
            Player player = this.getPlayer();
            stage.getStartMessages().forEach(message -> {
               if (player != null && player.isOnline()) {
                  player.sendMessage(message);
               }
            });
         }

         this.updateBossBar();
      }
   }

   public boolean isAvailableToUpdate() {
      return System.currentTimeMillis() >= this.lastUpdate;
   }

   public boolean isAvailableStory() {
      return System.currentTimeMillis() >= this.storyDelay;
   }

   public Map<String, User.DailyQuestProgress> getDailyQuestProgress() {
      return this.dailyQuestProgress;
   }

   public String getOngoingDailyQuestId() {
      return this.ongoingDailyQuestId;
   }

   public void setOngoingDailyQuestId(String id) {
      this.ongoingDailyQuestId = id;
   }

   public void switchToDailyQuest(Quest newQuest) {
      if (newQuest == null) {
         Quest current = this.getCurrentDailyQuest();
         if (current != null) {
            this.quests.remove(current);
            this.currentQuests.remove(current);
            this.questsStage.remove(current);
            if (!this.disabledQuests.contains(current)) {
               this.disabledQuests.add(current);
            }
         }

         this.ongoingDailyQuestId = null;
         this.updateBossBar();
      } else if (!newQuest.isStory()) {
         Gson gson = new Gson();
         Quest current = this.getCurrentDailyQuest();
         if (current != null && !current.equals(newQuest)) {
            User.DailyQuestProgress prog = this.dailyQuestProgress.get(current.getIdentifier());
            if (prog != null) {
               int stageIndex = this.currentQuests.getOrDefault(current, 0);
               prog.stageIndex = stageIndex;
               Stage stageData = this.questsStage.get(current);
               prog.stageDataJson = stageData != null ? gson.toJson(stageData) : "{}";
               prog.completed = false;
            }

            this.quests.remove(current);
            this.currentQuests.remove(current);
            this.questsStage.remove(current);
            if (!this.disabledQuests.contains(current)) {
               this.disabledQuests.add(current);
            }
         }

         this.disabledQuests.remove(newQuest);
         if (!this.quests.contains(newQuest)) {
            this.quests.add(newQuest);
         }

         User.DailyQuestProgress newProg = this.dailyQuestProgress.get(newQuest.getIdentifier());
         int stageIndex = 0;
         Stage stage;
         if (newProg != null && newProg.stageDataJson != null && !newProg.stageDataJson.equals("{}")) {
            try {
               stage = (Stage)gson.fromJson(newProg.stageDataJson, Stage.class);
               stageIndex = newProg.stageIndex;
               if (stageIndex < 0 || stageIndex >= newQuest.getStages().size()) {
                  // A changed quest definition must not leave persisted progress pointing
                  // at a stage that no longer exists.
                  stage = newQuest.getStages().getFirst().clone();
                  stageIndex = 0;
               }
            } catch (Exception exception) {
               stage = newQuest.getStages().getFirst().clone();
               stageIndex = 0;
            }
         } else {
            stage = newQuest.getStages().getFirst().clone();
            stageIndex = 0;
            this.dailyQuestProgress.put(newQuest.getIdentifier(), new User.DailyQuestProgress(0, gson.toJson(stage), false));
         }

         this.currentQuests.put(newQuest, stageIndex);
         this.questsStage.put(newQuest, stage);
         this.ongoingDailyQuestId = newQuest.getIdentifier();
         Player player = this.getPlayer();
         if (player != null && player.isOnline()) {
            for (String message : stage.getStartMessages()) {
               player.sendMessage(message);
            }
         }

         this.updateBossBar();
      }
   }

   public void cancelCurrentDailyQuest() {
      Quest current = this.getCurrentDailyQuest();
      if (current != null) {
         this.quests.remove(current);
         this.currentQuests.remove(current);
         this.questsStage.remove(current);
         if (!this.disabledQuests.contains(current)) {
            this.disabledQuests.add(current);
         }

         this.dailyQuestProgress.remove(current.getIdentifier());
      }

      this.ongoingDailyQuestId = null;
      this.clearBossBar();
   }

   public void forceShowBossBarFor(Quest quest) {
      Player player = this.getPlayer();
      if (player != null && player.isOnline() && quest != null) {
         Stage stage = this.getCurrentStage(quest);
         if (stage != null) {
            if (this.bossBar != null && this.bossBar.isVisible()) {
               this.bossBar.setTitle(stage.getBossTitle());
            } else {
               this.bossBar = Bukkit.createBossBar(stage.getBossTitle(), BarColor.BLUE, BarStyle.SOLID, new BarFlag[0]);
            }

            this.bossBar.setProgress(stage.getProgress(this.getCurrentStageData(quest)));
            if (!this.bossBar.getPlayers().contains(player)) {
               this.bossBar.addPlayer(player);
            }

            this.bossBar.setVisible(true);
         }
      }
   }

   /** Creates a stable persistence view before SQL work moves to the database thread. */
   public User snapshot() {
      User copy = new User(this.uniqueId);
      copy.viewStatus = this.viewStatus;
      copy.points = this.points;
      copy.story = this.story;
      copy.lastDailyRewardDate = this.lastDailyRewardDate;
      copy.quests.addAll(this.quests);
      copy.currentQuests.putAll(this.currentQuests);
      this.questsStage.forEach((quest, stage) -> copy.questsStage.put(quest, stage.snapshot()));
      copy.completedTasks.addAll(this.completedTasks);
      copy.disabledQuests.addAll(this.disabledQuests);
      this.dailyQuestProgress.forEach((id, progress) -> copy.dailyQuestProgress.put(
         id,
         new DailyQuestProgress(progress.stageIndex, progress.stageDataJson, progress.completed)
      ));
      copy.ongoingDailyQuestId = this.ongoingDailyQuestId;
      return copy;
   }

   public UUID getUniqueId() {
      return this.uniqueId;
   }

   public boolean isViewStatus() {
      return this.viewStatus;
   }

   public int getPoints() {
      return this.points;
   }

   public boolean isStory() {
      return this.story;
   }

   public String getLastDailyRewardDate() {
      return this.lastDailyRewardDate;
   }

   public LinkedList<Quest> getQuests() {
      return this.quests;
   }

   public ConcurrentHashMap<Quest, Integer> getCurrentQuests() {
      return this.currentQuests;
   }

   public ConcurrentHashMap<Quest, Stage> getQuestsStage() {
      return this.questsStage;
   }

   public LinkedList<Quest> getCompletedTasks() {
      return this.completedTasks;
   }

   public LinkedList<Quest> getDisabledQuests() {
      return this.disabledQuests;
   }

   public long getLastUpdate() {
      return this.lastUpdate;
   }

   public BossBar getBossBar() {
      return this.bossBar;
   }

   public String getNextQuest() {
      return this.nextQuest;
   }

   public long getStoryDelay() {
      return this.storyDelay;
   }

   public void setViewStatus(boolean viewStatus) {
      this.viewStatus = viewStatus;
   }

   public void setPoints(int points) {
      this.points = points;
   }

   public void setStory(boolean story) {
      this.story = story;
   }

   public void setLastDailyRewardDate(String lastDailyRewardDate) {
      this.lastDailyRewardDate = lastDailyRewardDate;
   }

   public void setLastUpdate(long lastUpdate) {
      this.lastUpdate = lastUpdate;
   }

   public void setBossBar(BossBar bossBar) {
      this.bossBar = bossBar;
   }

   public void setNextQuest(String nextQuest) {
      this.nextQuest = nextQuest;
   }

   public void setStoryDelay(long storyDelay) {
      this.storyDelay = storyDelay;
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof User other)) {
         return false;
      } else if (!other.canEqual(this)) {
         return false;
      } else if (this.isViewStatus() != other.isViewStatus()) {
         return false;
      } else if (this.getPoints() != other.getPoints()) {
         return false;
      } else if (this.isStory() != other.isStory()) {
         return false;
      } else {
         Object this$uniqueId = this.getUniqueId();
         Object other$uniqueId = other.getUniqueId();
         if (this$uniqueId == null ? other$uniqueId == null : this$uniqueId.equals(other$uniqueId)) {
            Object this$lastDailyRewardDate = this.getLastDailyRewardDate();
            Object other$lastDailyRewardDate = other.getLastDailyRewardDate();
            if (this$lastDailyRewardDate == null ? other$lastDailyRewardDate == null : this$lastDailyRewardDate.equals(other$lastDailyRewardDate)) {
               Object this$quests = this.getQuests();
               Object other$quests = other.getQuests();
               if (this$quests == null ? other$quests == null : this$quests.equals(other$quests)) {
                  Object this$currentQuests = this.getCurrentQuests();
                  Object other$currentQuests = other.getCurrentQuests();
                  if (this$currentQuests == null ? other$currentQuests == null : this$currentQuests.equals(other$currentQuests)) {
                     Object this$questsStage = this.getQuestsStage();
                     Object other$questsStage = other.getQuestsStage();
                     if (this$questsStage == null ? other$questsStage == null : this$questsStage.equals(other$questsStage)) {
                        Object this$completedTasks = this.getCompletedTasks();
                        Object other$completedTasks = other.getCompletedTasks();
                        if (this$completedTasks == null ? other$completedTasks == null : this$completedTasks.equals(other$completedTasks)) {
                           Object this$disabledQuests = this.getDisabledQuests();
                           Object other$disabledQuests = other.getDisabledQuests();
                           if (this$disabledQuests == null ? other$disabledQuests == null : this$disabledQuests.equals(other$disabledQuests)) {
                              Object this$dailyQuestProgress = this.getDailyQuestProgress();
                              Object other$dailyQuestProgress = other.getDailyQuestProgress();
                              if (this$dailyQuestProgress == null ? other$dailyQuestProgress == null : this$dailyQuestProgress.equals(other$dailyQuestProgress)
                                 )
                               {
                                 Object this$ongoingDailyQuestId = this.getOngoingDailyQuestId();
                                 Object other$ongoingDailyQuestId = other.getOngoingDailyQuestId();
                                 return this$ongoingDailyQuestId == null
                                    ? other$ongoingDailyQuestId == null
                                    : this$ongoingDailyQuestId.equals(other$ongoingDailyQuestId);
                              } else {
                                 return false;
                              }
                           } else {
                              return false;
                           }
                        } else {
                           return false;
                        }
                     } else {
                        return false;
                     }
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   protected boolean canEqual(Object other) {
      return other instanceof User;
   }

   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * 59 + (this.isViewStatus() ? 79 : 97);
      result = result * 59 + this.getPoints();
      result = result * 59 + (this.isStory() ? 79 : 97);
      Object $uniqueId = this.getUniqueId();
      result = result * 59 + ($uniqueId == null ? 43 : $uniqueId.hashCode());
      Object $lastDailyRewardDate = this.getLastDailyRewardDate();
      result = result * 59 + ($lastDailyRewardDate == null ? 43 : $lastDailyRewardDate.hashCode());
      Object $quests = this.getQuests();
      result = result * 59 + ($quests == null ? 43 : $quests.hashCode());
      Object $currentQuests = this.getCurrentQuests();
      result = result * 59 + ($currentQuests == null ? 43 : $currentQuests.hashCode());
      Object $questsStage = this.getQuestsStage();
      result = result * 59 + ($questsStage == null ? 43 : $questsStage.hashCode());
      Object $completedTasks = this.getCompletedTasks();
      result = result * 59 + ($completedTasks == null ? 43 : $completedTasks.hashCode());
      Object $disabledQuests = this.getDisabledQuests();
      result = result * 59 + ($disabledQuests == null ? 43 : $disabledQuests.hashCode());
      Object $dailyQuestProgress = this.getDailyQuestProgress();
      result = result * 59 + ($dailyQuestProgress == null ? 43 : $dailyQuestProgress.hashCode());
      Object $ongoingDailyQuestId = this.getOngoingDailyQuestId();
      return result * 59 + ($ongoingDailyQuestId == null ? 43 : $ongoingDailyQuestId.hashCode());
   }

   @Override
   public String toString() {
      return "User(uniqueId="
         + this.getUniqueId()
         + ", viewStatus="
         + this.isViewStatus()
         + ", points="
         + this.getPoints()
         + ", story="
         + this.isStory()
         + ", lastDailyRewardDate="
         + this.getLastDailyRewardDate()
         + ", quests="
         + this.getQuests()
         + ", currentQuests="
         + this.getCurrentQuests()
         + ", questsStage="
         + this.getQuestsStage()
         + ", completedTasks="
         + this.getCompletedTasks()
         + ", disabledQuests="
         + this.getDisabledQuests()
         + ", lastUpdate="
         + this.getLastUpdate()
         + ", bossBar="
         + this.getBossBar()
         + ", nextQuest="
         + this.getNextQuest()
         + ", storyDelay="
         + this.getStoryDelay()
         + ", dailyQuestProgress="
         + this.getDailyQuestProgress()
         + ", ongoingDailyQuestId="
         + this.getOngoingDailyQuestId()
         + ")";
   }

   public static class DailyQuestProgress {
      public int stageIndex;
      public String stageDataJson;
      public boolean completed;

      public DailyQuestProgress(int stageIndex, String stageDataJson, boolean completed) {
         this.stageIndex = stageIndex;
         this.stageDataJson = stageDataJson;
         this.completed = completed;
      }
   }
}
