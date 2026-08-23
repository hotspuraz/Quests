package com.person98.imevoquest.service.quest.internal;

import com.person98.imevoquest.ImevoQuest;
import com.person98.imevoquest.service.quest.QuestContainer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class ChatQuest extends QuestContainer {
   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onAsyncPlayerChat(AsyncChatEvent event) {
      Player player = event.getPlayer();
      String message = PlainTextComponentSerializer.plainText().serialize(event.message());

      // Paper/Purpur chat events are asynchronous. Quest completion dispatches commands,
      // updates boss bars and mutates shared user state, all of which must run on the main
      // server thread in 26.2.
      ImevoQuest.getInstance().getServer().getScheduler().runTask(ImevoQuest.getInstance(), () ->
         this.executionBuilder("chat").player(player).root(message).processSingle().buildAndExecute()
      );
   }
}
