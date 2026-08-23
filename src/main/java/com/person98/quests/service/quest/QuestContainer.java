package com.person98.quests.service.quest;

import com.person98.quests.Quests;
import com.person98.quests.service.quest.external.UltimateTimberQuest;
import com.person98.quests.service.quest.internal.BlockBreakQuest;
import com.person98.quests.service.quest.internal.BlockPlaceQuest;
import com.person98.quests.service.quest.internal.BreedingAnimalsQuest;
import com.person98.quests.service.quest.internal.ChatQuest;
import com.person98.quests.service.quest.internal.ClickQuest;
import com.person98.quests.service.quest.internal.CollectQuest;
import com.person98.quests.service.quest.internal.ConsumeQuest;
import com.person98.quests.service.quest.internal.CraftQuest;
import com.person98.quests.service.quest.internal.DamageQuest;
import com.person98.quests.service.quest.internal.ExecuteCommandQuest;
import com.person98.quests.service.quest.internal.KillMobQuest;
import com.person98.quests.service.quest.internal.KillPlayerQuest;
import com.person98.quests.service.quest.internal.PiglinBarterQuest;
import com.person98.quests.service.quest.internal.PlayerFishQuest;
import com.person98.quests.service.quest.internal.ServerCommandQuest;
import com.person98.quests.service.quest.internal.ShearSheepQuest;
import com.person98.quests.service.quest.internal.SmeltQuest;
import com.person98.quests.service.quest.internal.TamingAnimalsQuest;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;

public class QuestContainer implements Listener {
   protected Quests getPlugin() {
      return Quests.getInstance();
   }

   protected QuestExecutionBuilder executionBuilder(String type) {
      return QuestExecutionBuilder.of(this, type);
   }

   public static void initializeListeners() {
      Quests plugin = Quests.getInstance();
      plugin.getServer().getPluginManager().registerEvents(new BlockBreakQuest(), plugin);
      plugin.getServer().getPluginManager().registerEvents(new BlockPlaceQuest(), plugin);
      plugin.getServer().getPluginManager().registerEvents(new ChatQuest(), plugin);
      plugin.getServer().getPluginManager().registerEvents(new ClickQuest(), plugin);
      plugin.getServer().getPluginManager().registerEvents(new ConsumeQuest(), plugin);
      plugin.getServer().getPluginManager().registerEvents(new CraftQuest(), plugin);
      plugin.getServer().getPluginManager().registerEvents(new DamageQuest(), plugin);
      plugin.getServer().getPluginManager().registerEvents(new ExecuteCommandQuest(), plugin);
      plugin.getServer().getPluginManager().registerEvents(new KillMobQuest(), plugin);
      plugin.getServer().getPluginManager().registerEvents(new KillPlayerQuest(), plugin);
      plugin.getServer().getPluginManager().registerEvents(new PiglinBarterQuest(), plugin);
      plugin.getServer().getPluginManager().registerEvents(new SmeltQuest(), plugin);
      plugin.getServer().getPluginManager().registerEvents(new ServerCommandQuest(), plugin);
      plugin.getServer().getPluginManager().registerEvents(new PlayerFishQuest(), plugin);
      plugin.getServer().getPluginManager().registerEvents(new CollectQuest(), plugin);
      plugin.getServer().getPluginManager().registerEvents(new ShearSheepQuest(), plugin);
      plugin.getServer().getPluginManager().registerEvents(new TamingAnimalsQuest(), plugin);
      plugin.getServer().getPluginManager().registerEvents(new BreedingAnimalsQuest(), plugin);
      if (plugin.getServer().getPluginManager().getPlugin("UltimateTimber") != null) {
         UltimateTimberQuest.register(plugin);
      }
   }

   public boolean hasInvalidMeta(Block block) {
      return block.hasMetadata("@Quests");
   }
}
