package com.person98.imevoquest.service.quest.internal;

import com.person98.imevoquest.service.quest.QuestContainer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityTameEvent;

public class TamingAnimalsQuest extends QuestContainer {
   @EventHandler
   public void tamingAnimals(EntityTameEvent event) {
      if (event.getOwner() instanceof Player player) {
         Entity entity = event.getEntity();
         this.executionBuilder("taminganimals").player(player).root(entity.getType()).processSingle().buildAndExecute();
      }
   }
}
