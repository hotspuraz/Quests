package com.person98.imevoquest.listener;

import com.person98.imevoquest.ImevoQuest;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class QuestListener implements Listener {
   @EventHandler
   public void onBlockBreak(BlockBreakEvent event) {
      Block block = event.getBlock();
      if (!block.hasMetadata("@ImevoQuests") && event.isDropItems()) {
         this.checkItems(event.getPlayer());
      }

      if (block.getState() instanceof InventoryHolder) {
         this.checkItems(event.getPlayer());
      }
   }

   private void checkItems(final Player player) {
      (new BukkitRunnable() {
         public void run() {
            for (Entity entity : player.getNearbyEntities(5.0, 5.0, 5.0)) {
               if (entity instanceof Item) {
                  entity.setMetadata("player", new FixedMetadataValue(ImevoQuest.getInstance(), player.getUniqueId()));
               }
            }
         }
      }).runTaskLater(ImevoQuest.getInstance(), 3L);
   }

   @EventHandler
   public void onEntityDeath(EntityDeathEvent event) {
      if (event.getEntity().getKiller() != null && event.getEntity() instanceof InventoryHolder) {
         this.checkItems(event.getEntity().getKiller());
      }
   }

   @EventHandler
   public void onVehicleDestroy(VehicleDestroyEvent event) {
      if (event.getAttacker() instanceof Player && event.getVehicle() instanceof InventoryHolder) {
         this.checkItems((Player)event.getAttacker());
      }
   }

   @EventHandler
   public void onBlockPlace(BlockPlaceEvent event) {
      event.getBlock().setMetadata("@ImevoQuests", new FixedMetadataValue(ImevoQuest.getInstance(), event.getPlayer().getUniqueId()));
   }

   @EventHandler
   public void onPlayerDropItem(PlayerDropItemEvent event) {
      event.getItemDrop().setMetadata("@ImevoQuests", new FixedMetadataValue(ImevoQuest.getInstance(), event.getPlayer().getUniqueId()));
   }

   @EventHandler
   public void onEntityPickupItem(EntityPickupItemEvent event) {
      event.getItem().setMetadata("@ImevoQuests", new FixedMetadataValue(ImevoQuest.getInstance(), event.getEntity().getUniqueId()));
   }
}
