package com.person98.imevoquest.service.quest.internal;

import com.person98.imevoquest.service.quest.QuestContainer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;

public class KillMobQuest extends QuestContainer {
   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onEntityDeath(EntityDeathEvent event) {
      LivingEntity entity = event.getEntity();
      Player killer = entity.getKiller();
      if (killer != null) {
         this.executionBuilder("kill-mob").player(killer).root(entity.getType()).processSingle().buildAndExecute();
      }
   }
}
