package com.person98.imevoquest.data.stage;

public enum RootType {
   MATERIAL,
   ENTITY_TYPE,
   NONE;

   public static RootType get(String name) {
      for (RootType root : values()) {
         if (root.name().equalsIgnoreCase(name)) {
            return root;
         }
      }

      return NONE;
   }
}
