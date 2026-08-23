package com.person98.quests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.Test;

class PluginDescriptorTest {
   @Test
   void pluginYmlIsAcceptedByThePurpurApiParser() throws Exception {
      try (InputStream descriptor = getClass().getResourceAsStream("/plugin.yml")) {
         PluginDescriptionFile plugin = new PluginDescriptionFile(descriptor);
         assertEquals("Quests", plugin.getName());
         assertEquals("com.person98.quests.Quests", plugin.getMain());
         assertEquals("26.2", plugin.getAPIVersion());
         assertTrue(plugin.getDepend().contains("SmartInvs"));
         assertTrue(plugin.getSoftDepend().contains("UltimateTimber"));
      }
   }
}
