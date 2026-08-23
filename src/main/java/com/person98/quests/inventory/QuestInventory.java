package com.person98.quests.inventory;

import com.person98.quests.Quests;
import com.person98.quests.data.User;
import com.person98.quests.util.Util;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class QuestInventory implements InventoryProvider {
   public static void open(Player player) {
      SmartInventory.builder()
         .title(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Quests.getInstance().getConfig().getString("items.main.title"))))
         .size(3, 9)
         .provider(new QuestInventory())
         .build()
         .open(player);
   }

   public void init(Player player, InventoryContents contents) {
      Util.buildPattern(contents);
      User user = Quests.getInstance().getUserController().find(player);
      if (user != null) {
         contents.set(1, 2, ClickableItem.of(Util.buildItem("main.storyline").build(), e -> StorylineInventory.open(player, 0)));
         contents.set(1, 4, ClickableItem.of(Util.buildItem("main.leaderboard").build(), e -> {
            contents.inventory().close(player);
            Bukkit.getServer().dispatchCommand(player, "queststop 1");
         }));
         contents.set(1, 6, ClickableItem.of(Util.buildItem("main.daily").build(), e -> {
            if (user.isAvailableDailyQuests()) {
               DailyInventory.open(player);
            } else {
               player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
            }
         }));
      } else {
         contents.inventory().close(player);
      }
   }

   public void update(Player player, InventoryContents contents) {
   }
}
