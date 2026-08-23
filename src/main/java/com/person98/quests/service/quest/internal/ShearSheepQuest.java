package com.person98.quests.service.quest.internal;

import com.person98.quests.service.quest.QuestContainer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerShearEntityEvent;

public class ShearSheepQuest extends QuestContainer {
   @EventHandler
   public void onPlayerShearEntity(PlayerShearEntityEvent event) {
      Player player = event.getPlayer();
      Entity entity = event.getEntity();
      this.executionBuilder("shear").player(player).root(entity.getType()).processSingle().buildAndExecute();
   }
}
