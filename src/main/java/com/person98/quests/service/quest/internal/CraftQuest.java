package com.person98.quests.service.quest.internal;

import com.person98.quests.service.quest.QuestContainer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CraftQuest extends QuestContainer {
   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onCraftITem(CraftItemEvent event) {
      Player player = (Player)event.getWhoClicked();
      ItemStack result = this.calculateResult(event);
      if (result != null) {
         this.executionBuilder("craft").player(player).root(result.getType()).progress(result.getAmount()).buildAndExecute();
      }
   }

   private ItemStack calculateResult(CraftItemEvent event) {
      ItemStack item = event.getCurrentItem();
      if (item == null) {
         return null;
      } else if (event.getAction() == InventoryAction.NOTHING) {
         return null;
      } else {
         ClickType click = event.getClick();
         int recipeAmount = item.getAmount();
         switch (click) {
            case NUMBER_KEY:
               if (event.getWhoClicked().getInventory().getItem(event.getHotbarButton()) != null) {
                  recipeAmount = 0;
               }
               break;
            case DROP:
            case CONTROL_DROP:
               ItemStack cursor = event.getCursor();
               if (isAir(cursor)) {
                  recipeAmount = 0;
               }
               break;
            case SHIFT_RIGHT:
            case SHIFT_LEFT:
               if (recipeAmount != 0) {
                  int maxCraftable = this.getMaxCraftAmount(event.getInventory());
                  int capacity = this.fits(item, event.getView().getBottomInventory());
                  if (capacity < maxCraftable) {
                     maxCraftable = (capacity + recipeAmount - 1) / recipeAmount * recipeAmount;
                  }

                  recipeAmount = maxCraftable;
               }
         }

         ItemStack value = item.clone();
         value.setAmount(recipeAmount);
         return value;
      }
   }

   private static boolean isAir(ItemStack item) {
      return item != null && item.getType() != Material.AIR;
   }

   private int getMaxCraftAmount(CraftingInventory inv) {
      if (inv.getResult() == null) {
         return 0;
      } else {
         int resultCount = inv.getResult().getAmount();
         int materialCount = 2147483647;

         for (ItemStack is : inv.getMatrix()) {
            if (isAir(is) && is.getAmount() < materialCount) {
               materialCount = is.getAmount();
            }
         }

         return resultCount * materialCount;
      }
   }

   private int fits(ItemStack stack, Inventory inv) {
      ItemStack[] contents = inv.getContents();
      int result = 0;

      for (ItemStack is : contents) {
         if (is == null) {
            result += stack.getMaxStackSize();
         } else if (is.isSimilar(stack)) {
            result += Math.max(stack.getMaxStackSize() - is.getAmount(), 0);
         }
      }

      return result;
   }
}
