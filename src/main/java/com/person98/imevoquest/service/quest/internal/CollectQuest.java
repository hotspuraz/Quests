package com.person98.imevoquest.service.quest.internal;

import com.person98.imevoquest.ImevoQuest;
import com.person98.imevoquest.service.quest.QuestContainer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class CollectQuest extends QuestContainer {
   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onEntityPickupItem(EntityPickupItemEvent event) {
      if (!event.getItem().hasMetadata("collectQuest")) {
         if (event.getEntity() instanceof Player player) {
            this.executionBuilder("collect")
               .player(player)
               .root(event.getItem().getItemStack().getType())
               .progress(event.getItem().getItemStack().getAmount())
               .buildAndExecute();
         }
      }
   }

   @EventHandler
   public void death(PlayerDeathEvent event) {
      Player player = event.getEntity();

      for (Entity entity : player.getNearbyEntities(3.0, 3.0, 3.0)) {
         if (entity instanceof Item item) {
            item.setMetadata("collectQuest", new FixedMetadataValue(ImevoQuest.getInstance(), 10));
         }
      }
   }

   @EventHandler
   public void onBlockPlace(BlockPlaceEvent event) {
      event.getBlock().setMetadata("collectQuest", new FixedMetadataValue(ImevoQuest.getInstance(), 10));
   }

   @EventHandler
   public void drop2(BlockDropItemEvent event) {
      if (event.getBlock().hasMetadata("collectQuest")) {
         event.getItems().forEach(item -> item.setMetadata("collectQuest", new FixedMetadataValue(ImevoQuest.getInstance(), 10)));
      }
   }

   @EventHandler
   public void drop(PlayerDropItemEvent event) {
      event.getItemDrop().setMetadata("collectQuest", new FixedMetadataValue(ImevoQuest.getInstance(), 10));
   }
}
