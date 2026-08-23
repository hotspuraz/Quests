package com.person98.imevoquest.data.stage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.person98.imevoquest.data.stage.impl.StageData;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.Test;

class StageCompatibilityTest {
   @Test
   void persistedEntityStringStillMatchesPurpurEntityType() {
      StageData persisted = new StageData("kill-mob", "COW", 1, "ENTITY_TYPE");

      assertTrue(persisted.compareRoot(null, EntityType.COW));
      assertFalse(persisted.compareRoot(null, EntityType.PIG));
   }

   @Test
   void gsonRoundTripUsedByDailyProgressKeepsEntityObjectiveUsable() {
      Gson gson = new Gson();
      Stage original = stage(new StageData("kill-mob", EntityType.COW, 1, "ENTITY_TYPE"));
      Stage restored = gson.fromJson(gson.toJson(original), Stage.class);

      assertTrue(restored.getData().getFirst().getRoot() instanceof String);
      assertTrue(restored.getData().getFirst().compareRoot(null, EntityType.COW));
   }

   @Test
   void namespacedRootsMatchCurrentRegistryValues() {
      StageData entity = new StageData("kill-mob", "minecraft:cow", 1, "ENTITY_TYPE");
      StageData material = new StageData("block-break", "minecraft:stone", 1, "MATERIAL");

      assertTrue(entity.compareRoot(null, EntityType.COW));
      assertTrue(material.compareRoot(null, Material.STONE));
   }

   @Test
   void multiObjectiveCompletionIsIndependentOfPersistedOrder() {
      Stage required = stage(
         new StageData("kill-mob", EntityType.COW, 2, "ENTITY_TYPE"),
         new StageData("block-break", Material.STONE, 3, "MATERIAL")
      );
      Stage progress = stage(
         new StageData("block-break", "STONE", 3, "MATERIAL"),
         new StageData("kill-mob", "COW", 2, "ENTITY_TYPE")
      );

      assertTrue(required.isFinished(progress));
      assertEquals(1.0, required.getProgress(progress));
   }

   @Test
   void missingPersistedObjectiveIsAddedWhenItReceivesProgress() {
      Stage required = stage(
         new StageData("kill-mob", EntityType.COW, 1, "ENTITY_TYPE"),
         new StageData("block-break", Material.STONE, 2, "MATERIAL")
      );
      Stage progress = stage(new StageData("kill-mob", "COW", 1, "ENTITY_TYPE"));

      assertFalse(required.isFinished(progress));
      progress.incrementObjective(null, required, "block-break", Material.STONE, 2);
      assertTrue(required.isFinished(progress));
   }

   private static Stage stage(StageData... objectives) {
      Stage stage = new Stage();
      stage.getData().addAll(java.util.List.of(objectives));
      return stage;
   }
}
