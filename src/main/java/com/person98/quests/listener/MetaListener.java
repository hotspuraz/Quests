package com.person98.quests.listener;

import com.person98.quests.Quests;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class MetaListener implements Listener {
   public MetaListener() {
      Quests.getInstance().getServer().getPluginManager().registerEvents(this, Quests.getInstance());
   }

   @EventHandler
   public void onFurnaceOpen(InventoryOpenEvent event) {
      Player player = (Player)event.getPlayer();
      NamespacedKey key = new NamespacedKey(Quests.getInstance(), "cooked");
      if (event.getInventory().getType() == InventoryType.FURNACE) {
         for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && this.isIngot(item.getType())) {
               ItemMeta meta = item.getItemMeta();
               meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "cooked");
               item.setItemMeta(meta);
            }
         }
      }
   }

   @EventHandler
   public void onFurnaceClose(InventoryCloseEvent event) {
      Player player = (Player)event.getPlayer();
      NamespacedKey key = new NamespacedKey(Quests.getInstance(), "cooked");
      if (event.getInventory().getType() == InventoryType.FURNACE) {
         for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && this.isIngot(item.getType())) {
               ItemMeta meta = item.getItemMeta();
               meta.getPersistentDataContainer().remove(key);
               item.setItemMeta(meta);
            }
         }
      }
   }

   private boolean isIngot(Material material) {
      return material == Material.IRON_INGOT
         || material == Material.GOLD_INGOT
         || material == Material.NETHERITE_SCRAP
         || material == Material.COPPER_INGOT
         || material == Material.EMERALD
         || material == Material.DIAMOND
         || material == Material.LAPIS_LAZULI
         || material == Material.REDSTONE
         || material == Material.COAL
         || material == Material.QUARTZ
         || material == Material.CHARCOAL
         || material == Material.IRON_NUGGET
         || material == Material.GOLD_NUGGET
         || material == Material.NETHERITE_INGOT
         || material == Material.BAKED_POTATO
         || material == Material.COOKED_BEEF
         || material == Material.COOKED_CHICKEN
         || material == Material.COOKED_COD
         || material == Material.COOKED_MUTTON
         || material == Material.COOKED_PORKCHOP
         || material == Material.COOKED_RABBIT
         || material == Material.COOKED_SALMON
         || material == Material.BREAD
         || material == Material.DRIED_KELP
         || material == Material.STONE
         || material == Material.SMOOTH_STONE
         || material == Material.STONE_BRICKS
         || material == Material.SMOOTH_STONE_SLAB
         || material == Material.DEEPSLATE
         || material == Material.POLISHED_DEEPSLATE
         || material == Material.COBBLED_DEEPSLATE
         || material == Material.DEEPSLATE_BRICKS
         || material == Material.DEEPSLATE_TILES
         || material == Material.CHISELED_DEEPSLATE
         || material == Material.GLASS
         || material == Material.TINTED_GLASS
         || material == Material.POPPED_CHORUS_FRUIT
         || material == Material.GREEN_DYE
         || material == Material.LIME_DYE
         || material == Material.BRICK
         || material == Material.NETHER_BRICK;
   }
}
