package com.person98.quests.util;

import com.person98.quests.Quests;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public class Util {
   public static String getMessage(String path, HashMap<String, String> placeholders) {
      String message = Quests.getInstance().getConfig().getString("messages." + path);
      if (message != null) {
         message = ChatColor.translateAlternateColorCodes('&', message);
         if (placeholders != null) {
            for (Entry<String, String> entry : placeholders.entrySet()) {
               message = message.replace(entry.getKey(), entry.getValue());
            }
         }
      }

      return message;
   }

   public static String getMessage(String path) {
      return getMessage(path, null);
   }

   public static void buildPattern(InventoryContents contents) {
      contents.fillBorders(ClickableItem.empty(new ItemStackBuilder(Material.LIGHT_BLUE_STAINED_GLASS_PANE).setName("§e").build()));
      contents.set(0, 3, ClickableItem.empty(new ItemStackBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("§e").build()));
      contents.set(0, 4, ClickableItem.empty(new ItemStackBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("§e").build()));
      contents.set(0, 5, ClickableItem.empty(new ItemStackBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("§e").build()));
      contents.set(1, 1, ClickableItem.empty(new ItemStackBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("§e").build()));
      contents.set(1, 3, ClickableItem.empty(new ItemStackBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("§e").build()));
      contents.set(1, 5, ClickableItem.empty(new ItemStackBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("§e").build()));
      contents.set(1, 7, ClickableItem.empty(new ItemStackBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("§e").build()));
      contents.set(2, 3, ClickableItem.empty(new ItemStackBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("§e").build()));
      contents.set(2, 4, ClickableItem.empty(new ItemStackBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("§e").build()));
      contents.set(2, 5, ClickableItem.empty(new ItemStackBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("§e").build()));
   }

   public static ItemStackBuilder buildItem(String path, final HashMap<String, String> placeholders) {
      final FileConfiguration configuration = Quests.getInstance().getConfig();
      final String fullPath = "items." + path + ".";
      Material material = Arrays.stream(Material.values())
         .filter(data -> data.name().equalsIgnoreCase(configuration.getString(fullPath + ".material")))
         .findFirst()
         .orElse(null);
      if (material == null) {
         return null;
      } else {
         ItemStackBuilder itemStackBuilder = new ItemStackBuilder(material);
         itemStackBuilder.setAmount(configuration.getInt(fullPath + ".amount"));
         String name = configuration.getString(fullPath + ".name");
         if (placeholders != null) {
            for (Entry<String, String> entry : placeholders.entrySet()) {
               assert name != null;

               name = name.replace(entry.getKey(), entry.getValue());
            }
         }

         itemStackBuilder.setName(name);
         itemStackBuilder.setGlow(configuration.getBoolean(fullPath + ".glow"));
         List<String> lore = new ArrayList<>();
         configuration.getStringList(fullPath + ".lore").forEach(originalLine -> {
            String line = originalLine;
            if (placeholders != null) {
               for (Entry<String, String> entry : placeholders.entrySet()) {
                  line = line.replace(entry.getKey(), entry.getValue());
               }
            }
            lore.add(line);
         });
         itemStackBuilder.addLore(lore);
         return itemStackBuilder;
      }
   }

   public static ItemStackBuilder buildItem(String path) {
      return buildItem(path, null);
   }
}
