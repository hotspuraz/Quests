package com.person98.imevoquest.service.quest.internal;

import com.person98.imevoquest.service.quest.QuestContainer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;

public class KillPlayerQuest extends QuestContainer {
   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerDeath(PlayerDeathEvent event) {
      Player entity = event.getEntity();
      if (entity.getKiller() != null) {
         Player player = entity.getKiller();
         this.executionBuilder("kill-player").player(player).root(entity.getName()).processSingle().buildAndExecute();
      }
   }
}
