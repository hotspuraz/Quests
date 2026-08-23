package com.person98.imevoquest.service.quest.external;

import com.person98.imevoquest.ImevoQuest;
import com.person98.imevoquest.service.quest.QuestContainer;
import java.lang.reflect.Method;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;

/** Optional UltimateTimber 2.x integration without a hard dependency on its unpublished API JAR. */
public class UltimateTimberQuest extends QuestContainer {
   private static final String TREE_FALL_EVENT = "com.songoda.ultimatetimber.events.TreeFallEvent";

   public static void register(ImevoQuest plugin) {
      Plugin ultimateTimber = plugin.getServer().getPluginManager().getPlugin("UltimateTimber");
      if (ultimateTimber == null) {
         return;
      }

      try {
         Class<?> rawEventClass = ultimateTimber.getClass().getClassLoader().loadClass(TREE_FALL_EVENT);
         if (!Event.class.isAssignableFrom(rawEventClass)) {
            throw new IllegalStateException(TREE_FALL_EVENT + " is not a Bukkit event");
         }

         @SuppressWarnings("unchecked")
         Class<? extends Event> eventClass = (Class<? extends Event>)rawEventClass;
         UltimateTimberQuest listener = new UltimateTimberQuest();
         plugin.getServer().getPluginManager().registerEvent(
            eventClass,
            listener,
            EventPriority.MONITOR,
            (ignored, event) -> listener.onTreeFall(event),
            plugin,
            true
         );
      } catch (ReflectiveOperationException | LinkageError | IllegalStateException exception) {
         plugin.getLogger().warning("UltimateTimber integration could not be registered: " + exception.getMessage());
      }
   }

   private void onTreeFall(Event event) {
      try {
         Player player = (Player)invoke(event, "getPlayer");
         Object detectedTree = invoke(event, "getDetectedTree");
         Object treeBlocks = invoke(detectedTree, "getDetectedTreeBlocks");
         Iterable<?> blocks = (Iterable<?>)invoke(treeBlocks, "getAllTreeBlocks");

         for (Object treeBlock : blocks) {
            Block block = (Block)invoke(treeBlock, "getBlock");
            if (block.getType() == Material.FIRE || block.getType() == Material.SOUL_FIRE) {
               return;
            }
            if (!this.hasInvalidMeta(block)) {
               this.executionBuilder("block-break").player(player).root(block.getType()).processSingle().buildAndExecute();
            }
         }
      } catch (ReflectiveOperationException | ClassCastException exception) {
         ImevoQuest.getInstance().getLogger().warning("Could not process UltimateTimber tree fall: " + exception.getMessage());
      }
   }

   private static Object invoke(Object target, String methodName) throws ReflectiveOperationException {
      Method method = target.getClass().getMethod(methodName);
      return method.invoke(target);
   }
}
