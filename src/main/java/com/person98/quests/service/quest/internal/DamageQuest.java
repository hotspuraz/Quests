package com.person98.quests.service.quest.internal;

import com.person98.quests.service.quest.QuestContainer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageQuest extends QuestContainer {
   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
      if (event.getEntity() instanceof Player && event.getDamager() instanceof Player player) {
         int damage = (int)Math.round(event.getDamage());
         this.executionBuilder("damage-player").player(player).progress(damage).buildAndExecute();
      }
   }
}
