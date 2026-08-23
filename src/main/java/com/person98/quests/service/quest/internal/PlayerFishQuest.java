package com.person98.quests.service.quest.internal;

import com.person98.quests.service.quest.QuestContainer;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;

public class PlayerFishQuest extends QuestContainer {
   @EventHandler
   public void onPlayerFish(PlayerFishEvent e) {
      if (e.getState() == State.CAUGHT_FISH) {
         Player player = e.getPlayer();
         Item item = (Item)e.getCaught();

         assert item != null;

         this.executionBuilder("fish").player(player).root(item.getItemStack().getType()).processSingle().buildAndExecute();
      }
   }
}
