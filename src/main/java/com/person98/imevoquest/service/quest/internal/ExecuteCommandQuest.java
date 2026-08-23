package com.person98.imevoquest.service.quest.internal;

import com.person98.imevoquest.service.quest.QuestContainer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class ExecuteCommandQuest extends QuestContainer {
   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerCommandPreProcess(PlayerCommandPreprocessEvent event) {
      Player player = event.getPlayer();
      String command = event.getMessage();
      this.executionBuilder("execute-command").player(player).root(command).processSingle().buildAndExecute();
   }
}
