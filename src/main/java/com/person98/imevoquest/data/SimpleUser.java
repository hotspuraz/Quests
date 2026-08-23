package com.person98.imevoquest.data;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public record SimpleUser(UUID uniqueId, int points) {
   public String getName() {
      Player player = Bukkit.getPlayer(this.uniqueId);
      String name = player == null ? Bukkit.getOfflinePlayer(this.uniqueId).getName() : player.getName();
      return name == null ? "Unknown" : name;
   }
}
