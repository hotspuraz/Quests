package com.person98.quests.task;

import com.person98.quests.Quests;
import com.person98.quests.data.Quest;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class UserQuestSchedule extends BukkitRunnable {
   public void run() {
      Quests.getInstance().getUserController().forEach(user -> {
         if (user.getStoryDelay() > 0L && user.getNextQuest() != null && user.isAvailableStory()) {
            Quest quest = Quests.getInstance().getQuestController().find(user.getNextQuest());
            user.setStoryDelay(-1L);
            user.setNextQuest(null);
            if (quest != null) {
               user.getQuests().add(quest);
               user.getCurrentQuests().put(quest, 0);
               user.getQuestsStage().put(quest, quest.getStages().getFirst().clone());
               Player player = user.getPlayer();
               if (player != null && player.isOnline()) {
                  user.getCurrentStage(quest).getStartMessages().forEach(player::sendMessage);
               }
            }

            user.updateBossBar();
         }
      });
   }
}
