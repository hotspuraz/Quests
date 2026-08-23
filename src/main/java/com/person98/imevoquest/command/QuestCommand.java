package com.person98.imevoquest.command;

import com.person98.imevoquest.ImevoQuest;
import com.person98.imevoquest.data.Quest;
import com.person98.imevoquest.data.User;
import com.person98.imevoquest.inventory.QuestInventory;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class QuestCommand implements CommandExecutor, TabCompleter {
   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
      if (sender instanceof Player player) {
         if (args.length == 0) {
            QuestInventory.open(player);
         } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("bossbar")) {
               User user = ImevoQuest.getInstance().getUserController().find(player);
               if (user != null) {
                  user.setViewStatus(!user.isViewStatus());
                  user.updateBossBar();
               }
            }

            if (args[0].equalsIgnoreCase("cancel")) {
               User user = ImevoQuest.getInstance().getUserController().find(player);
               if (user != null) {
                  boolean daily = false;
                  Quest quest;
                  if (user.isAvailableDailyQuests()) {
                     daily = true;
                     quest = user.getCurrentDailyQuest();
                  } else {
                     quest = user.getCurrentStoryQuest();
                  }

                  if (quest != null) {
                     if (daily) {
                        user.cancelCurrentDailyQuest();
                     } else {
                        user.setStory(false);
                        user.setViewStatus(false);
                        user.updateBossBar();
                     }

                     player.sendMessage("§3§lSimpleSurvival §7» §aQuest successfully removed.");
                  }
               }
            }
         }

         return false;
      } else {
         sender.sendMessage("§cCommand exclusive to players.");
         return true;
      }
   }

   public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, final String[] args) {
      return args.length == 1 ? List.of("cancel", "bossbar") : new ArrayList<>();
   }
}
