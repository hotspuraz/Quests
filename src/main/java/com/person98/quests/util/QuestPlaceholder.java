package com.person98.quests.util;

import com.person98.quests.Quests;
import com.person98.quests.data.SimpleUser;
import java.util.List;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class QuestPlaceholder extends PlaceholderExpansion {
   private final Quests plugin;

   public QuestPlaceholder(Quests plugin) {
      this.plugin = plugin;
   }

   @NotNull
   public String getAuthor() {
      return "imevo";
   }

   @NotNull
   public String getIdentifier() {
      return "quests";
   }

   @NotNull
   public String getVersion() {
      return "1.0.1";
   }

   public boolean persist() {
      return true;
   }

   public String onRequest(OfflinePlayer player, String params) {
      if (params.startsWith("name_")) {
         String[] arr = params.split("_");
         if (arr.length == 1) {
            return "";
         } else {
            int index = Integer.parseInt(arr[1]) - 1;
            SimpleUser topUser = this.getTopAt(index);
            return topUser == null ? "" : topUser.getName();
         }
      } else if (params.equalsIgnoreCase("points")) {
         SimpleUser user = this.plugin.getStorage().getLeaderboard().stream().filter(u -> u.uniqueId().equals(player.getUniqueId())).findFirst().orElse(null);
         return user == null ? "" : String.valueOf(user.points());
      } else if (params.startsWith("points_")) {
         String[] arr = params.split("_");
         if (arr.length == 1) {
            return "";
         } else {
            int index = Integer.parseInt(arr[1]) - 1;
            SimpleUser topUser = this.getTopAt(index);
            return topUser == null ? "" : topUser.points() + "";
         }
      } else {
         return null;
      }
   }

   private SimpleUser getTopAt(int index) {
      List<SimpleUser> leaderboard = this.plugin.getStorage().getLeaderboard();
      return index >= leaderboard.size() ? null : leaderboard.get(index);
   }
}
