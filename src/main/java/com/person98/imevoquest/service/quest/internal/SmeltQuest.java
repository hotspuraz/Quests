package com.person98.imevoquest.service.quest.internal;

import com.person98.imevoquest.service.quest.QuestContainer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.FurnaceExtractEvent;

public class SmeltQuest extends QuestContainer {
   @EventHandler(priority = EventPriority.MONITOR)
   public void onFurnaceExtract(FurnaceExtractEvent event) {
      // Purpur 26.2 exposes the amount actually extracted. The old InventoryClickEvent
      // approximation missed/overcounted shift-clicks and depended on cursor semantics.
      if (event.getItemAmount() <= 0) {
         return;
      }
      this.executionBuilder("smelt")
         .player(event.getPlayer())
         .root(event.getItemType())
         .progress(event.getItemAmount())
         .buildAndExecute();
   }
}
