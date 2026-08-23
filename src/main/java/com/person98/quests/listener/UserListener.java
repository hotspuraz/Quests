package com.person98.quests.listener;

import com.person98.quests.Quests;
import com.person98.quests.data.Quest;
import com.person98.quests.data.User;
import com.person98.quests.data.stage.Stage;
import java.util.List;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class UserListener implements Listener {
   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      User user = Quests.getInstance().getUserController().find(event.getPlayer());
      if (user != null) {
         Quest activeQuest = null;
         if (user.getQuests().isEmpty() && user.getCompletedTasks().isEmpty()) {
            Quest storyQuest = Quests.getInstance().getQuestController().firstStory();
            if (storyQuest != null) {
               user.initializeQuest(storyQuest);
               activeQuest = storyQuest;
            }
         } else {
            if (user.isAvailableDailyQuests()) {
               activeQuest = user.getCurrentDailyQuest();
            } else {
               Quest currentStory = user.getCurrentStoryQuest();
               if (currentStory != null) {
                  activeQuest = currentStory;
               } else {
                  Quest firstStory = Quests.getInstance().getQuestController().firstStory();
                  if (firstStory != null) {
                     Quest current = firstStory;

                     while (current != null && user.getCompletedTasks().contains(current)) {
                        current = current.getNext();
                     }

                     if (current != null) {
                        user.initializeQuest(current);
                        activeQuest = current;
                     }
                  }
               }
            }

            if (activeQuest != null) {
               Stage stage = user.getCurrentStage(activeQuest);
               if (stage != null) {
                  stage.getStartMessages().forEach(event.getPlayer()::sendMessage);
               }
            }
         }

         List<Quest> todaysDailyQuests = Quests.getInstance().getQuestController().getDailyQuests();
         boolean needsDailyInit = user.getDailyQuests().isEmpty();
         if (!needsDailyInit) {
            for (Quest userDaily : user.getDailyQuests()) {
               if (!todaysDailyQuests.contains(userDaily)) {
                  needsDailyInit = true;
                  break;
               }
            }
         }

         if (needsDailyInit) {
            user.getQuests().removeIf(q -> !q.isStory());
            user.getCurrentQuests().keySet().removeIf(q -> !q.isStory());
            user.getQuestsStage().keySet().removeIf(q -> !q.isStory());
            user.getDisabledQuests().removeIf(q -> !q.isStory());
            todaysDailyQuests.forEach(q -> {
               user.getQuests().remove(q);
               user.getCurrentQuests().remove(q);
               user.getQuestsStage().remove(q);
               user.getDisabledQuests().add(q);
            });
         }

         if (activeQuest != null) {
            boolean isValidActiveQuest = false;
            if (activeQuest.isStory() && user.getCurrentStoryQuest() == activeQuest) {
               isValidActiveQuest = true;
            } else if (!activeQuest.isStory() && user.getCurrentDailyQuest() == activeQuest) {
               isValidActiveQuest = todaysDailyQuests.contains(activeQuest);
            }

            if (isValidActiveQuest) {
               user.forceShowBossBarFor(activeQuest);
            } else {
               user.clearBossBar();
            }
         } else {
            user.clearBossBar();
         }
      }

      Quests.getInstance().getStorage().getExcluded().remove(event.getPlayer().getUniqueId().toString());
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      Quests.getInstance().getUserController().destroy(event.getPlayer());
   }
}
