package com.person98.imevoquest.service.quest.internal;

import com.person98.imevoquest.service.quest.QuestContainer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityBreedEvent;

public class BreedingAnimalsQuest extends QuestContainer {
   @EventHandler
   public void breedingAnimals(EntityBreedEvent event) {
      if (event.getBreeder() instanceof Player player) {
         EntityType entityType = event.getEntityType();
         this.executionBuilder("breeding").player(player).root(entityType).processSingle().buildAndExecute();
      }
   }
}
