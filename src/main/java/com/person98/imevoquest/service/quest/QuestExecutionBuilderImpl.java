package com.person98.imevoquest.service.quest;

import com.google.gson.Gson;
import com.person98.imevoquest.ImevoQuest;
import com.person98.imevoquest.data.Quest;
import com.person98.imevoquest.data.User;
import com.person98.imevoquest.data.stage.Stage;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class QuestExecutionBuilderImpl implements QuestExecutionBuilder {
   private final QuestContainer container;
   private final String type;
   private Player player;
   private Object root;
   private BigInteger progress;

   public QuestExecutionBuilderImpl(QuestContainer container, String type) {
      this.container = container;
      this.type = type;
   }

   @Override
   public void buildAndExecute() {
      User user = this.container.getPlugin().getUserController().find(this.player);
      Gson gson = new Gson();
      if (user != null) {
         for (Quest quest : new ArrayList<>(user.getQuests())) {
            if ((!quest.isStory() || user.isStory())
               && (quest.isStory() || !user.getDisabledQuests().contains(quest))
               && (quest.isStory() || user.isAvailableDailyQuests())) {
               Stage data = user.getCurrentStageData(quest);
               Stage stage = user.getCurrentStage(quest);
               if (data != null && stage != null) {
                  data.incrementObjective(this.player, stage, this.type, this.root, this.progress.intValue());
                  boolean isDaily = !quest.isStory();
                  if (isDaily) {
                     user.setOngoingDailyQuestId(quest.getIdentifier());
                     int stageIndex = user.getCurrentQuests().getOrDefault(quest, 0);
                     user.getDailyQuestProgress().put(quest.getIdentifier(), new User.DailyQuestProgress(stageIndex, gson.toJson(data), false));
                     ImevoQuest.getInstance().getStorage().saveDailyQuestProgress(user);
                  }

                  if (stage.isFinished(data)) {
                     stage.getFinishMessages().forEach(this.player::sendMessage);

                     for (String reward : stage.getRewards()) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), reward.replace("%player%", this.player.getName()));
                     }

                     if (!quest.isStory()) {
                        String today = LocalDate.now().toString();
                        if (!today.equals(user.getLastDailyRewardDate())) {
                           List<Quest> allDailies = ImevoQuest.getInstance().getQuestController().getDailyQuests();
                           boolean allComplete = true;

                           for (Quest daily : allDailies) {
                              User.DailyQuestProgress progress = user.getDailyQuestProgress().get(daily.getIdentifier());
                              if (progress == null || !progress.completed) {
                                 allComplete = false;
                                 break;
                              }
                           }

                           if (allComplete) {
                              ImevoQuest.getInstance().executeRewardCommands(this.player.getName());
                              user.setLastDailyRewardDate(today);
                           }
                        }
                     }

                     int position = user.getCurrentQuests().getOrDefault(quest, -1);
                     boolean finished = true;
                     if (position != -1 && quest.getStages().size() > position + 1) {
                        Stage next = quest.getStages().get(position + 1);
                        if (next != null) {
                           next.getStartMessages().forEach(this.player::sendMessage);
                           user.getCurrentQuests().put(quest, position + 1);
                           user.getQuestsStage().put(quest, next.clone());
                           if (isDaily) {
                              user.getDailyQuestProgress().put(quest.getIdentifier(), new User.DailyQuestProgress(position + 1, gson.toJson(next), false));
                              ImevoQuest.getInstance().getStorage().saveDailyQuestProgress(user);
                           } else {
                              // Persist a story-stage transition immediately. The original
                              // three-minute autosave window could resurrect the old stage
                              // after a crash/restart even though the player had completed it.
                              ImevoQuest.getInstance().getStorage().saveUser(user);
                           }

                           finished = false;
                        }
                     }

                     if (finished) {
                        user.setPoints(user.getPoints() + quest.getPoints());
                        user.getCurrentQuests().remove(quest);
                        user.getQuestsStage().remove(quest);
                        user.getQuests().remove(quest);
                        if (!isDaily) {
                           user.getCompletedTasks().add(quest);
                        }

                        if (isDaily) {
                           User.DailyQuestProgress prog = user.getDailyQuestProgress().get(quest.getIdentifier());
                           if (prog != null) {
                              prog.completed = true;
                           }

                           user.setOngoingDailyQuestId(null);
                           user.updateBossBar();
                           ImevoQuest.getInstance().getStorage().saveDailyQuestProgress(user);
                           String today = LocalDate.now().toString();
                           if (!today.equals(user.getLastDailyRewardDate())) {
                              List<Quest> allDailies = ImevoQuest.getInstance().getQuestController().getDailyQuests();
                              boolean allComplete = true;

                              for (Quest daily : allDailies) {
                                 User.DailyQuestProgress progress = user.getDailyQuestProgress().get(daily.getIdentifier());
                                 if (progress == null || !progress.completed) {
                                    allComplete = false;
                                    break;
                                 }
                              }

                              if (allComplete) {
                                 ImevoQuest.getInstance().executeRewardCommands(this.player.getName());
                                 user.setLastDailyRewardDate(today);
                              }
                           }
                        }

                        if (quest.isStory()) {
                           Quest next = quest.getNext();
                           if (next != null) {
                              user.setStoryDelay(System.currentTimeMillis() + next.getDelay());
                              user.setNextQuest(next.getIdentifier());
                           }
                        }

                        // Completion, points, daily reward date and the removal of the
                        // active quest must be captured as one ordered persistence snapshot.
                        ImevoQuest.getInstance().getStorage().saveUser(user);
                     }
                  }

                  user.updateBossBar();
               }
            }
         }
      }
   }

   @Override
   public QuestExecutionBuilder player(Player player) {
      this.player = player;
      return this;
   }

   @Override
   public QuestExecutionBuilder root(Object root) {
      this.root = root;
      return this;
   }

   @Override
   public QuestExecutionBuilder processSingle() {
      return this.progress(BigInteger.ONE);
   }

   @Override
   public QuestExecutionBuilder progress(BigInteger progress) {
      this.progress = progress;
      return this;
   }
}
