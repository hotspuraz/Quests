package com.person98.quests.inventory;

import com.google.common.collect.Lists;
import com.person98.quests.Quests;
import com.person98.quests.data.Quest;
import com.person98.quests.data.User;
import com.person98.quests.util.ItemStackBuilder;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class DailyConfirmInventory implements InventoryProvider {
   public static void open(Player player, String identifier) {
      SmartInventory.builder().provider(new DailyConfirmInventory()).title("§8Are you sure you want to reset?").size(1, 9).id(identifier).build().open(player);
   }

   public void init(Player player, InventoryContents contents) {
      Quest quest = Quests.getInstance().getQuestController().find(contents.inventory().getId());
      if (quest != null) {
         User user = Quests.getInstance().getUserController().find(player);
         if (user != null) {
            int[] confirm = new int[]{0, 1, 2, 3};
            int[] deny = new int[]{5, 6, 7, 8};

            for (int i : confirm) {
               contents.set(0, i, ClickableItem.of(new ItemStackBuilder(Material.GREEN_STAINED_GLASS_PANE).setName("§aConfirm").build(), e -> {
                  user.cancelCurrentDailyQuest();
                  Quests.getInstance().getStorage().saveDailyQuestProgress(user);
                  player.sendMessage("§3§lSimpleSurvival §7» §aQuest successfully cancelled.");
                  DailyInventory.open(player);
               }));
            }

            for (int i : deny) {
               contents.set(
                  0, i, ClickableItem.of(new ItemStackBuilder(Material.RED_STAINED_GLASS_PANE).setName("§cDeny").build(), e -> DailyInventory.open(player))
               );
            }

            contents.set(
               0,
               4,
               ClickableItem.empty(
                  new ItemStackBuilder(Material.CRAFTING_TABLE)
                     .setName("§7")
                     .setLore(
                        Lists.newArrayList(new String[]{"§7If you take this quest you will lose,", "§7any progress made on other unfinished daily quests."})
                     )
                     .build()
               )
            );
         } else {
            contents.inventory().close(player);
         }
      }
   }

   public void update(Player player, InventoryContents contents) {
   }
}
