package com.person98.quests.command;

import com.person98.quests.Quests;
import com.person98.quests.data.Quest;
import com.person98.quests.data.User;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class QueCommand implements CommandExecutor, TabCompleter {
   private final Quests quests;

   public QueCommand(Quests quests) {
      this.quests = quests;
   }

   public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
      if (sender.hasPermission("quests.admin")) {
         if (args.length == 0) {
            sender.sendMessage("§3§lSimpleQuests §7» §cUsage: /que <reload|reset|set> [player] [daily]");
            return true;
         } else if (args[0].equalsIgnoreCase("reload")) {
            try {
               this.quests.reloadConfig();
               this.quests.loadRewardsConfig();
               this.quests.getQuestController().reloadDailyQuestConfig();
               sender.sendMessage("§3§lSimpleQuests §7» §aConfiguration files reloaded successfully!");
               sender.sendMessage("§3§lSimpleQuests §7» §7- config.yml reloaded");
               sender.sendMessage("§3§lSimpleQuests §7» §7- rewards.yml reloaded");
               sender.sendMessage("§3§lSimpleQuests §7» §7- daily_quests.yml reloaded");
            } catch (Exception exception) {
               sender.sendMessage("§3§lSimpleQuests §7» §cError reloading configuration: " + exception.getMessage());
               this.quests.getLogger().severe("Error reloading configuration: " + exception.getMessage());
               exception.printStackTrace();
            }

            return true;
         } else if (args[0].equalsIgnoreCase("reset")) {
            if (args.length < 2) {
               sender.sendMessage("§3§lSimpleQuests §7» §cUsage: /que reset <player>");
               return true;
            } else {
               Player player = Bukkit.getPlayer(args[1]);
               UUID uniqueId;
               if (player == null) {
                  uniqueId = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
               } else {
                  uniqueId = player.getUniqueId();
               }

               sender.sendMessage("§3§lSimpleQuests §7» §aResetting player data §e" + Bukkit.getOfflinePlayer(uniqueId).getName());
               Quests.getInstance().getStorage().resetUser(uniqueId);
               return true;
            }
         } else if (args[0].equalsIgnoreCase("set")) {
            if (args.length < 3 || !args[2].equalsIgnoreCase("daily")) {
               sender.sendMessage("§3§lSimpleQuests §7» §cUsage: /que set <player> daily");
               return true;
            }

            Player player = Bukkit.getPlayerExact(args[1]);
            if (player == null) {
               sender.sendMessage("§3§lSimpleQuests §7» §cThe player must be online.");
               return true;
            }

            User user = this.quests.getUserController().find(player);
            Quest activeStory = user.getCurrentStoryQuest();
            if (activeStory == null) {
               sender.sendMessage("§3§lSimpleQuests §7» §e" + player.getName() + " already has access to daily quests.");
               return true;
            }

            // Temporary test command: finish and detach the active story so the daily
            // inventory can be exercised without manually completing the whole storyline.
            // Recording it as completed prevents loadUser() from restoring the stale
            // quest_current row after a relog or restart.
            user.getQuests().remove(activeStory);
            user.getCurrentQuests().remove(activeStory);
            user.getQuestsStage().remove(activeStory);
            if (!user.getCompletedTasks().contains(activeStory)) {
               user.getCompletedTasks().add(activeStory);
            }
            user.setStory(false);
            user.setNextQuest(null);
            user.setStoryDelay(0L);
            user.clearBossBar();
            user.updateBossBar();
            this.quests.getStorage().saveUserSync(user);
            sender.sendMessage(
               "§3§lSimpleQuests §7» §aDaily quests unlocked for §e"
                  + player.getName()
                  + "§a. Story marked complete for this test account: §e"
                  + activeStory.getIdentifier()
            );
            return true;
         } else {
            sender.sendMessage("§3§lSimpleQuests §7» §cUnknown subcommand. Use: reload, reset, or set");
            return true;
         }
      } else {
         sender.sendMessage("§3§lSimpleQuests §7» §cYou don't have permission to use this command!");
         return true;
      }
   }

   public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, final String[] args) {
      List<String> completions = new ArrayList<>();
      if (args.length == 1) {
         for (String argument : List.of("reload", "reset", "set")) {
            if (argument.toLowerCase().startsWith(args[0].toLowerCase())) {
               completions.add(argument);
            }
         }
      }

      if (args.length == 2) {
         Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
               completions.add(player.getName());
            }
         });
      }

      if (args.length == 3 && args[0].equalsIgnoreCase("set") && "daily".startsWith(args[2].toLowerCase())) {
         completions.add("daily");
      }
      return completions;
   }
}
