package com.person98.imevoquest.controller.impl;

import com.person98.imevoquest.ImevoQuest;
import com.person98.imevoquest.controller.UserController;
import com.person98.imevoquest.data.Quest;
import com.person98.imevoquest.data.User;
import com.person98.imevoquest.util.Cache;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.bukkit.Bukkit;

public class UserControllerImpl implements UserController {
   private Cache<UUID, User> users;
   private ImevoQuest quests;

   @Override
   public void constructor(ImevoQuest quests) {
      this.quests = quests;
      this.users = new Cache<>(TimeUnit.DAYS.toMillis(1L));
      Bukkit.getOnlinePlayers().forEach(this::create);
   }

   @Override
   public void destructor(ImevoQuest quests) {
      Collection<User> values = this.users.values();
      User[] users = new User[values.size()];
      users = values.toArray(users);
      this.users.invalidateAll();
      if (!values.isEmpty()) {
         this.quests.getStorage().saveDailyQuestProgressBatch(values);
      }

      this.quests.getStorage().saveUserSync(users);
   }

   @Override
   public User find(UUID uniqueId) {
      User user = this.users.get(uniqueId);
      if (user == null) {
         this.create(uniqueId);
         return this.find(uniqueId);
      } else {
         return user;
      }
   }

   @Override
   public void create(UUID uniqueId) {
      User user = this.quests.getStorage().loadUser(uniqueId);
      this.quests.getStorage().loadDailyQuestProgress(user);
      if (user.getCurrentQuests().isEmpty() && user.getCompletedTasks().isEmpty()) {
         Quest quest = this.quests.getQuestController().firstStory();
         if (quest != null) {
            user.initializeQuest(quest);
         }
      }

      List<Quest> currentDaily = ImevoQuest.getInstance().getQuestController().getDailyQuests();
      Set<String> validIds = new HashSet<>();

      for (Quest q : currentDaily) {
         validIds.add(q.getIdentifier());
      }

      user.getDailyQuestProgress().keySet().removeIf(id -> !validIds.contains(id));
      user.getQuests().removeIf(q -> !q.isStory() && !validIds.contains(q.getIdentifier()));
      user.getCurrentQuests().keySet().removeIf(q -> !q.isStory() && !validIds.contains(q.getIdentifier()));
      user.getQuestsStage().keySet().removeIf(q -> !q.isStory() && !validIds.contains(q.getIdentifier()));
      user.getDisabledQuests().removeIf(q -> !q.isStory() && !validIds.contains(q.getIdentifier()));
      String ongoingId = user.getOngoingDailyQuestId();
      Quest ongoingQuest = null;
      if (ongoingId != null) {
         if (validIds.contains(ongoingId)) {
            ongoingQuest = ImevoQuest.getInstance().getQuestController().find(ongoingId);
            if (ongoingQuest != null) {
               if (!user.getQuests().contains(ongoingQuest)) {
                  user.switchToDailyQuest(ongoingQuest);
               }
            } else {
               user.setOngoingDailyQuestId(null);
               user.getDailyQuestProgress().remove(ongoingId);
            }
         } else {
            user.setOngoingDailyQuestId(null);
            user.getDailyQuestProgress().remove(ongoingId);
         }
      }

      for (Quest quest : currentDaily) {
         User.DailyQuestProgress progress = user.getDailyQuestProgress().get(quest.getIdentifier());
         boolean isCompleted = progress != null && progress.completed;
         if (ongoingQuest == null || !quest.getIdentifier().equals(ongoingQuest.getIdentifier())) {
            if (!isCompleted) {
               if (!user.getDisabledQuests().contains(quest)) {
                  user.getDisabledQuests().add(quest);
               }
            } else {
               user.getDisabledQuests().remove(quest);
            }

            if (!user.getQuests().contains(quest)) {
               user.getQuests().remove(quest);
               user.getCurrentQuests().remove(quest);
               user.getQuestsStage().remove(quest);
            }
         }
      }

      user.updateBossBar();
      user.setLastUpdate(System.currentTimeMillis() + 180000L);
      this.users.put(uniqueId, user);
   }

   @Override
   public void destroy(UUID uniqueId) {
      User user = this.find(uniqueId);
      if (user != null) {
         user.clearBossBar();
         if (user.getStoryDelay() > 0L && user.getNextQuest() != null) {
            Quest quest = ImevoQuest.getInstance().getQuestController().find(user.getNextQuest());
            if (quest != null) {
               user.getQuests().add(quest);
               user.getCurrentQuests().put(quest, 0);
               user.getQuestsStage().put(quest, quest.getStages().getFirst().clone());
            }
         }

         this.quests.getStorage().saveUser(user);
      }
   }

   @Override
   public void forEach(Consumer<User> consumer) {
      this.stream().forEach(consumer);
   }

   @Override
   public Stream<User> stream() {
      return this.users.values().stream();
   }

   @Override
   public void resetAllDailyQuestProgress() {
      List<Quest> currentDaily = ImevoQuest.getInstance().getQuestController().getDailyQuests();
      ImevoQuest.getInstance().getStorage().wipeDailyQuestProgress();
      this.forEach(user -> {
         user.getQuests().removeIf(q -> !q.isStory());
         user.getDisabledQuests().removeIf(q -> !q.isStory());
         user.getCurrentQuests().keySet().removeIf(q -> !q.isStory());
         user.getQuestsStage().keySet().removeIf(q -> !q.isStory());
         user.getDailyQuestProgress().clear();
         user.setOngoingDailyQuestId(null);
         user.setLastDailyRewardDate(null);
         user.clearBossBar();
      });
   }
}
