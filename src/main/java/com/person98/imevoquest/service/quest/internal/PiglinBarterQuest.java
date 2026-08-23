package com.person98.imevoquest.service.quest.internal;

import com.google.common.collect.Lists;
import com.person98.imevoquest.ImevoQuest;
import com.person98.imevoquest.service.quest.QuestContainer;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class PiglinBarterQuest extends QuestContainer {
   private static final List<Material> GOLDEN_MATERIALS = Lists.newArrayList();

   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onPlayerDropItem(PlayerDropItemEvent event) {
      if (GOLDEN_MATERIALS.contains(event.getItemDrop().getItemStack().getType())) {
         event.getItemDrop().getPersistentDataContainer().set(playerKey(), PersistentDataType.STRING, event.getPlayer().getUniqueId().toString());
      }
   }

   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onPiglinPickup(EntityPickupItemEvent event) {
      if (!(event.getEntity() instanceof Piglin piglin)) {
         return;
      }

      String playerId = event.getItem().getPersistentDataContainer().get(playerKey(), PersistentDataType.STRING);
      if (playerId != null) {
         piglin.getPersistentDataContainer().set(playerKey(), PersistentDataType.STRING, playerId);
      }
   }

   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onPiglinBarter(PiglinBarterEvent event) {
      String playerId = event.getEntity().getPersistentDataContainer().get(playerKey(), PersistentDataType.STRING);
      event.getEntity().getPersistentDataContainer().remove(playerKey());
      if (playerId == null) {
         return;
      }

      Player player;
      try {
         player = Bukkit.getPlayer(UUID.fromString(playerId));
      } catch (IllegalArgumentException ignored) {
         return;
      }
      if (player == null) {
         return;
      }

      // 26.2 creates new output item entities for a barter; metadata on the input entity
      // is not copied. PiglinBarterEvent is the authoritative source for every outcome.
      for (ItemStack outcome : event.getOutcome()) {
         this.executionBuilder("piglin-barter")
            .player(player)
            .root(outcome.getType())
            .progress(outcome.getAmount())
            .buildAndExecute();
      }
   }

   private NamespacedKey playerKey() {
      return new NamespacedKey(ImevoQuest.getInstance(), "piglin_barter_player");
   }

   static {
      Arrays.stream(Material.values()).filter(data -> data.name().contains("GOLD")).forEach(GOLDEN_MATERIALS::add);
      GOLDEN_MATERIALS.add(Material.CLOCK);
      GOLDEN_MATERIALS.add(Material.BELL);
      GOLDEN_MATERIALS.add(Material.GILDED_BLACKSTONE);
      GOLDEN_MATERIALS.add(Material.GLISTERING_MELON_SLICE);
      GOLDEN_MATERIALS.add(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
      GOLDEN_MATERIALS.add(Material.POWERED_RAIL);
   }
}
