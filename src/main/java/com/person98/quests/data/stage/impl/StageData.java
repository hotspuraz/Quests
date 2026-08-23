package com.person98.quests.data.stage.impl;

import java.util.Locale;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class StageData {
   private String type;
   private String rootType;
   private Object root;
   private int amount;

   public StageData(String type, Object root, int amount, String rootType) {
      this.type = type;
      this.root = root;
      this.amount = amount;
      this.rootType = rootType.trim();
   }

   public boolean compareRoot(Player player, Object root) {
      if (this.root instanceof String && root instanceof String) {
         if (this.type.equalsIgnoreCase("server-command")) {
            return String.valueOf(this.root).replace("%player%", player.getName()).equalsIgnoreCase(String.valueOf(root));
         } else {
            return String.valueOf(this.root).endsWith("$")
               ? String.valueOf(root).startsWith(String.valueOf(this.root).replace("$", ""))
               : String.valueOf(this.root).equalsIgnoreCase(String.valueOf(root));
         }
      } else {
         if (this.rootType.equalsIgnoreCase("Material") && root instanceof Material eventMaterial) {
            if (this.root != null && this.root instanceof Material m2) {
               return eventMaterial.equals(m2);
            }

            if (this.root != null) {
               String configured = String.valueOf(this.root);
               if (normalizeRoot(configured).equals(normalizeRoot(eventMaterial))) {
                  return true;
               }

               // Preserve the original partial-material behavior (for roots such as LOG),
               // but normalize case and minecraft: names introduced in modern configs.
               return eventMaterial.name().contains(normalizeEnumName(configured));
            }
         }

         if (this.rootType.equalsIgnoreCase("ENTITY_TYPE") && root instanceof EntityType eventType) {
            if (this.root instanceof EntityType configuredType) {
               return eventType == configuredType;
            }

            // Compatibility fix: Gson deserializes an Object-typed enum as String. The
            // original code therefore made every resumed entity objective impossible.
            return this.root != null && normalizeRoot(this.root).equals(normalizeRoot(eventType));
         }

         return normalizeRoot(root).equals(normalizeRoot(this.root));
      }
   }

   public boolean identifiesSameObjective(StageData other) {
      return other != null
         && this.type.equalsIgnoreCase(other.type)
         && this.rootType.equalsIgnoreCase(other.rootType)
         && normalizeRoot(this.root).equals(normalizeRoot(other.root));
   }

   public StageData copy() {
      return new StageData(this.type, this.root, this.amount, this.rootType);
   }

   private static String normalizeRoot(Object value) {
      if (value == null) {
         return "";
      }
      if (value instanceof Material material) {
         return material.getKey().asString();
      }
      if (value instanceof EntityType entityType) {
         return entityType.getKey().asString();
      }

      String normalized = String.valueOf(value).trim().toLowerCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
      return normalized.contains(":") ? normalized : "minecraft:" + normalized;
   }

   private static String normalizeEnumName(String value) {
      String normalized = value.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
      int namespaceSeparator = normalized.indexOf(':');
      return namespaceSeparator >= 0 ? normalized.substring(namespaceSeparator + 1) : normalized;
   }

   public String getRootAsString() {
      if (this.root instanceof Material) {
         return ((Material)this.root).name();
      } else if (this.root instanceof EntityType) {
         return ((EntityType)this.root).name();
      } else {
         return this.root == null ? "" : String.valueOf(this.root);
      }
   }

   public String getType() {
      return this.type;
   }

   public String getRootType() {
      return this.rootType;
   }

   public Object getRoot() {
      return this.root;
   }

   public int getAmount() {
      return this.amount;
   }

   public void setType(String type) {
      this.type = type;
   }

   public void setRootType(String rootType) {
      this.rootType = rootType;
   }

   public void setRoot(Object root) {
      this.root = root;
   }

   public void setAmount(int amount) {
      this.amount = amount;
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof StageData other)) {
         return false;
      } else if (!other.canEqual(this)) {
         return false;
      } else if (this.getAmount() != other.getAmount()) {
         return false;
      } else {
         Object this$type = this.getType();
         Object other$type = other.getType();
         if (this$type == null ? other$type == null : this$type.equals(other$type)) {
            Object this$rootType = this.getRootType();
            Object other$rootType = other.getRootType();
            if (this$rootType == null ? other$rootType == null : this$rootType.equals(other$rootType)) {
               Object this$root = this.getRoot();
               Object other$root = other.getRoot();
               return this$root == null ? other$root == null : this$root.equals(other$root);
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   protected boolean canEqual(Object other) {
      return other instanceof StageData;
   }

   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * 59 + this.getAmount();
      Object $type = this.getType();
      result = result * 59 + ($type == null ? 43 : $type.hashCode());
      Object $rootType = this.getRootType();
      result = result * 59 + ($rootType == null ? 43 : $rootType.hashCode());
      Object $root = this.getRoot();
      return result * 59 + ($root == null ? 43 : $root.hashCode());
   }

   @Override
   public String toString() {
      return "StageData(type=" + this.getType() + ", rootType=" + this.getRootType() + ", root=" + this.getRoot() + ", amount=" + this.getAmount() + ")";
   }
}
