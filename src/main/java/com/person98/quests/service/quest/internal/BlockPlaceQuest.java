package com.person98.quests.service.quest.internal;

import com.person98.quests.Quests;
import com.person98.quests.service.quest.QuestContainer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class BlockPlaceQuest extends QuestContainer {
   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onBlockPlace(BlockPlaceEvent event) {
      Player player = event.getPlayer();
      Block block = event.getBlock();
      if (block.getType() != Material.FIRE && block.getType() != Material.SOUL_FIRE) {
         event.getBlock().setMetadata("collectQuest", new FixedMetadataValue(Quests.getInstance(), 10));
         this.executionBuilder("block-place").player(player).root(block.getType()).processSingle().buildAndExecute();
      }
   }
}
