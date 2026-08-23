package com.person98.quests.service.quest.internal;

import com.person98.quests.service.quest.QuestContainer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakQuest extends QuestContainer {
   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.MONITOR
   )
   public void onBlockBreak(BlockBreakEvent event) {
      Player player = event.getPlayer();
      Block block = event.getBlock();
      if (block.getType() != Material.FIRE && block.getType() != Material.SOUL_FIRE) {
         if (!this.hasInvalidMeta(block)) {
            Material type = block.getType();
            if (type == Material.DEEPSLATE_DIAMOND_ORE) {
               type = Material.DIAMOND_ORE;
            } else if (type == Material.DEEPSLATE_GOLD_ORE) {
               type = Material.GOLD_ORE;
            } else if (type == Material.DEEPSLATE_REDSTONE_ORE) {
               type = Material.REDSTONE_ORE;
            } else if (type == Material.DEEPSLATE_LAPIS_ORE) {
               type = Material.LAPIS_ORE;
            } else if (type == Material.DEEPSLATE_COAL_ORE) {
               type = Material.COAL_ORE;
            } else if (type == Material.DEEPSLATE_IRON_ORE) {
               type = Material.IRON_ORE;
            } else if (type == Material.DEEPSLATE_EMERALD_ORE) {
               type = Material.EMERALD_ORE;
            } else if (type == Material.DEEPSLATE_COPPER_ORE) {
               type = Material.COPPER_ORE;
            }

            this.executionBuilder("block-break").player(player).root(type).processSingle().buildAndExecute();
         }
      }
   }
}
