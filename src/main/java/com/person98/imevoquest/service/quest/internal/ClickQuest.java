package com.person98.imevoquest.service.quest.internal;

import com.person98.imevoquest.service.quest.QuestContainer;
import com.person98.imevoquest.service.quest.QuestExecutionBuilder;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;

public class ClickQuest extends QuestContainer {
   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerInteract(PlayerInteractEvent event) {
      Player player = event.getPlayer();
      Block clickedBlock = event.getClickedBlock();
      QuestExecutionBuilder executionBuilder = null;
      switch (event.getAction()) {
         case RIGHT_CLICK_AIR:
         case RIGHT_CLICK_BLOCK:
            executionBuilder = this.executionBuilder("right-click");
            if (clickedBlock != null && clickedBlock.getType() != Material.AIR) {
               this.executionBuilder("right-click-block").player(player).root(clickedBlock.getType()).processSingle().buildAndExecute();
            }
            break;
         case LEFT_CLICK_AIR:
         case LEFT_CLICK_BLOCK:
            executionBuilder = this.executionBuilder("left-click");
            if (clickedBlock != null && clickedBlock.getType() != Material.AIR) {
               this.executionBuilder("left-click-block").player(player).root(clickedBlock.getType()).processSingle().buildAndExecute();
            }
      }

      if (executionBuilder != null) {
         executionBuilder.player(player).processSingle().buildAndExecute();
      }
   }
}
