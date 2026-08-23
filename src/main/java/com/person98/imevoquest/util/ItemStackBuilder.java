package com.person98.imevoquest.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class ItemStackBuilder implements Serializable {
   private final ItemStack item;
   private ItemMeta meta;
   private EnchantmentStorageMeta storage;
   private List<String> lore;
   private boolean glow = false;
   private SkullMeta skullMeta;

   public ItemStackBuilder(ItemStack item) {
      this.item = item;
      if (item.getType() == Material.ENCHANTED_BOOK) {
         this.storage = (EnchantmentStorageMeta)item.getItemMeta();
         this.lore = copyLore(this.storage);
      } else if (item.getType() == Material.PLAYER_HEAD) {
         this.skullMeta = (SkullMeta)item.getItemMeta();
         this.lore = copyLore(this.skullMeta);
      } else {
         this.meta = item.getItemMeta();
         this.lore = copyLore(this.meta);
      }
   }

   private static List<String> copyLore(ItemMeta meta) {
      return meta != null && meta.hasLore() && meta.getLore() != null
         ? new ArrayList<>(meta.getLore())
         : new ArrayList<>();
   }

   public ItemStackBuilder(Material material) {
      this(new ItemStack(material));
   }

   public ItemStackBuilder setLore(List<String> lore) {
      this.lore = lore.stream().map(string -> ChatColor.translateAlternateColorCodes('&', string)).collect(Collectors.toList());
      return this;
   }

   public Map<Enchantment, Integer> getEnchants() {
      return this.meta.getEnchants();
   }

   public boolean hasEnchants() {
      return this.meta.hasEnchants();
   }

   public ItemStackBuilder setType(Material type) {
      this.item.setType(type);
      return this;
   }

   public ItemStackBuilder setOwner(String owner) {
      if (this.item.getType() == Material.PLAYER_HEAD) {
         this.skullMeta.setOwner(owner);
         return this;
      } else {
         return this;
      }
   }

   public ItemStackBuilder setName(String name) {
      name = ChatColor.translateAlternateColorCodes('&', name);
      if (this.item.getType() == Material.ENCHANTED_BOOK) {
         this.storage.setDisplayName(name);
         return this;
      } else if (this.item.getType() == Material.PLAYER_HEAD) {
         this.skullMeta.setDisplayName(name);
         return this;
      } else {
         this.meta.setDisplayName(name);
         return this;
      }
   }

   public ItemStackBuilder addLore(String... l) {
      for (String x : l) {
         this.lore.add(x);
      }

      return this;
   }

   public ItemStackBuilder addLore(List<String> l) {
      for (String x : l) {
         this.lore.add(ChatColor.translateAlternateColorCodes('&', x));
      }

      return this;
   }

   public ItemStackBuilder addLoreList(List<String> l) {
      for (String s : l) {
         this.lore.add(ChatColor.translateAlternateColorCodes('&', s));
      }

      return this;
   }

   public ItemStackBuilder addStoredEnchantment(Enchantment e, int level) {
      this.storage.addStoredEnchant(e, level, true);
      return this;
   }

   public ItemStackBuilder addEnchantment(Enchantment e, int level) {
      this.meta.addEnchant(e, level, true);
      return this;
   }

   public ItemStackBuilder addEnchantments(HashMap<Enchantment, Integer> enchants) {
      enchants.forEach(this::addEnchantment);
      return this;
   }

   public ItemStackBuilder setDurability(int durability) {
      this.item.setDurability((short)durability);
      return this;
   }

   public ItemStackBuilder setAmount(int amount) {
      this.item.setAmount(amount);
      return this;
   }

   public ItemStackBuilder clearLore() {
      this.lore = new ArrayList<>();
      return this;
   }

   public ItemStackBuilder replaceLore(String oldLore, String newLore) {
      for (int i = 0; i < this.lore.size(); i++) {
         if (this.lore.get(i).contains(oldLore)) {
            this.lore.remove(i);
            this.lore.add(i, newLore);
            break;
         }
      }

      return this;
   }

   public ItemStack build() {
      if (this.item.getType() == Material.ENCHANTED_BOOK) {
         if (!this.lore.isEmpty()) {
            this.storage.setLore(this.lore);
            this.lore.clear();
         }

         this.item.setItemMeta(this.storage);
         return this.item;
      } else if (this.item.getType() == Material.PLAYER_HEAD) {
         if (!this.lore.isEmpty()) {
            this.skullMeta.setLore(this.lore);
            this.lore.clear();
         }

         this.item.setItemMeta(this.skullMeta);
         return this.item;
      } else {
         if (!this.lore.isEmpty()) {
            this.meta.setLore(this.lore);
            this.lore.clear();
         }

         this.item.setItemMeta(this.meta);
         if (this.glow) {
            this.setGlow(this.glow);
         }

         return this.item;
      }
   }

   public int getAmount() {
      return this.item.getAmount();
   }

   public ItemStackBuilder setGlow(boolean glow) {
      if (glow) {
         // Purpur 26.2 renamed the legacy Bukkit constant DURABILITY to UNBREAKING.
         this.addEnchantment(Enchantment.UNBREAKING, 1);
         this.addFlag(ItemFlag.HIDE_ENCHANTS);
      } else {
         this.removeFlag(ItemFlag.HIDE_ENCHANTS);
      }

      return this;
   }

   public ItemStackBuilder addFlag(ItemFlag... flags) {
      if (this.item.getType() == Material.ENCHANTED_BOOK) {
         this.storage.addItemFlags(flags);
      } else if (this.item.getType() == Material.PLAYER_HEAD) {
         this.skullMeta.addItemFlags(flags);
      } else {
         this.meta.addItemFlags(flags);
      }

      return this;
   }

   public ItemStackBuilder removeFlag(ItemFlag... flags) {
      if (this.item.getType() == Material.ENCHANTED_BOOK) {
         this.storage.removeItemFlags(flags);
      } else if (this.item.getType() == Material.PLAYER_HEAD) {
         this.skullMeta.removeItemFlags(flags);
      } else {
         this.meta.removeItemFlags(flags);
      }

      return this;
   }
}
