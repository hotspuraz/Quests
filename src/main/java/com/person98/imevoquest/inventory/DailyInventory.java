package com.person98.imevoquest.inventory;

import com.person98.imevoquest.ImevoQuest;
import com.person98.imevoquest.data.Quest;
import com.person98.imevoquest.data.User;
import com.person98.imevoquest.data.stage.Stage;
import com.person98.imevoquest.util.ItemStackBuilder;
import com.person98.imevoquest.util.Util;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DailyInventory implements InventoryProvider {
   public static void open(Player player) {
      SmartInventory.builder()
         .title(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(ImevoQuest.getInstance().getConfig().getString("items.daily.title"))))
         .size(3, 9)
         .provider(new DailyInventory())
         .build()
         .open(player, 0);
   }

   public void init(Player player, InventoryContents contents) {
      Util.buildPattern(contents);
      User user = ImevoQuest.getInstance().getUserController().find(player);
      if (user != null) {
         List<Quest> quests = ImevoQuest.getInstance().getQuestController().getDailyQuests();
         ClickableItem[] items = new ClickableItem[quests.size()];

         for (int i = 0; i < items.length; i++) {
            final Quest quest = quests.get(i);
            User.DailyQuestProgress prog = user.getDailyQuestProgress().get(quest.getIdentifier());
            String status;
            if (prog != null && prog.completed) {
               status = "completed";
            } else if (user.getOngoingDailyQuestId() != null && user.getOngoingDailyQuestId().equals(quest.getIdentifier())) {
               status = "current";
            } else if (user.getDisabledQuests().contains(quest)) {
               status = "disabled";
            } else {
               status = "non_completed";
            }

            HashMap<String, String> placeholders = new HashMap<>();
            placeholders.put("%name%", quest.getName());
            ItemStack display = Objects.requireNonNull(Util.buildItem("daily." + status, placeholders))
               .addLoreList(quest.getDescription())
               .build();
            if (!status.equalsIgnoreCase("completed")) {
               items[i] = ClickableItem.of(display, e -> {
                  boolean wasNoQuest = user.getCurrentDailyQuest() == null;
                  if (status.equals("current") && e.getClick().isRightClick()) {
                     DailyConfirmInventory.open(player, quest.getIdentifier());
                  } else {
                     user.switchToDailyQuest(quest);
                     ImevoQuest.getInstance().getStorage().saveDailyQuestProgress(user);
                     if (wasNoQuest) {
                        Stage stage = user.getCurrentStage(quest);
                        Player p = player;
                        if (stage != null && player != null && player.isOnline()) {
                           for (String message : stage.getStartMessages()) {
                              p.sendMessage(message);
                           }
                        }
                     }

                     user.forceShowBossBarFor(quest);
                  }
               });
            } else {
               items[i] = ClickableItem.empty(display);
            }
         }

         Pagination pagination = contents.pagination();
         pagination.setItems(items);
         pagination.setItemsPerPage(5);
         if (user.isAvailableDailyQuests()) {
            pagination.addToIterator(contents.newIterator(Type.HORIZONTAL, 1, 2));
         }

         contents.set(2, 4, ClickableItem.of(new ItemStackBuilder(Material.BARRIER).setName("§cReturn").build(), e -> QuestInventory.open(player)));
      } else {
         contents.inventory().close(player);
      }
   }

   public void update(Player player, InventoryContents contents) {
      this.init(player, contents);
   }
}
