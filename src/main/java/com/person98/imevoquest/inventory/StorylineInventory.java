package com.person98.imevoquest.inventory;

import com.google.common.collect.Lists;
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
import fr.minuskube.inv.content.SlotPos;
import fr.minuskube.inv.content.SlotIterator.Type;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class StorylineInventory implements InventoryProvider {
   public static void open(Player player, int page) {
      SmartInventory.builder()
         .title(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(ImevoQuest.getInstance().getConfig().getString("items.storyline.title"))))
         .size(3, 9)
         .provider(new StorylineInventory())
         .build()
         .open(player, page);
   }

   public void init(Player player, InventoryContents contents) {
      Util.buildPattern(contents);
      User user = ImevoQuest.getInstance().getUserController().find(player);
      if (user != null) {
         Quest currentQuest = user.getCurrentStoryQuest();
         Pagination pagination = contents.pagination();
         LinkedList<Quest> quests = Lists.newLinkedList();
         Quest first = ImevoQuest.getInstance().getQuestController().firstStory();
         if (first != null) {
            quests.add(first);
            Quest next = first.getNext();

            do {
               if (next != null) {
                  quests.add(next);
                  next = next.getNext();
               }
            } while (next != null);
         }

         ClickableItem[] items = new ClickableItem[quests.size()];

         for (int i = 0; i < items.length; i++) {
            final Quest quest = quests.get(i);
            if (quest != null) {
               String status;
               if (quest.isStory()) {
                  status = user.getCompletedTasks().contains(quest) ? "completed" : "non_completed";
               } else {
                  User.DailyQuestProgress prog = user.getDailyQuestProgress().get(quest.getIdentifier());
                  status = prog != null && prog.completed ? "completed" : "non_completed";
               }

               if (currentQuest != null && currentQuest == quest) {
                  status = "current";
               }

               HashMap<String, String> placeholders = new HashMap<>();
               placeholders.put("%name%", quest.getName());
               ItemStack display = Objects.requireNonNull(Util.buildItem("storyline." + status, placeholders))
                  .addLoreList(quest.getDescription())
                  .build();
               if (status.equalsIgnoreCase("current")) {
                  items[i] = ClickableItem.of(display, e -> {
                     if (e.getClick() == ClickType.RIGHT) {
                        Bukkit.dispatchCommand(player, "quest cancel story");
                     }

                     if (e.getClick() == ClickType.LEFT) {
                        user.setStory(true);
                        user.setViewStatus(true);
                        Quest currentStory = user.getCurrentStoryQuest();
                        if (currentStory != null) {
                           Stage stage = user.getCurrentStage(currentStory);
                           if (stage != null) {
                              stage.getStartMessages().forEach(player::sendMessage);
                           }
                        }

                        user.updateBossBar();
                     }
                  });
               } else {
                  items[i] = ClickableItem.empty(display);
               }
            }
         }

         pagination.setItems(items);
         pagination.setItemsPerPage(5);
         pagination.addToIterator(contents.newIterator(Type.HORIZONTAL, 1, 2));
         contents.set(
            new SlotPos(2, 5),
            ClickableItem.of(new ItemStackBuilder(Material.ARROW).setName("§aNext Page >>").build(), e -> open(player, pagination.next().getPage()))
         );
         contents.set(
            new SlotPos(2, 3),
            ClickableItem.of(new ItemStackBuilder(Material.ARROW).setName("§c<< Previous Page").build(), e -> open(player, pagination.previous().getPage()))
         );
         contents.set(2, 4, ClickableItem.of(new ItemStackBuilder(Material.BARRIER).setName("§cReturn").build(), e -> QuestInventory.open(player)));
      } else {
         contents.inventory().close(player);
      }
   }

   public void update(Player player, InventoryContents contents) {
      this.init(player, contents);
   }
}
