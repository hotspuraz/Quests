package com.person98.quests.service.quest.internal;

import com.person98.quests.service.quest.QuestContainer;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServerCommandEvent;

public class ServerCommandQuest extends QuestContainer {
   @EventHandler
   public void onServerCommand(ServerCommandEvent event) {
      if (event.getSender() instanceof ConsoleCommandSender) {
         for (Player player : Bukkit.getOnlinePlayers()) {
            this.executionBuilder("server-command").player(player).root(event.getCommand()).processSingle().buildAndExecute();
         }
      }
   }
}
