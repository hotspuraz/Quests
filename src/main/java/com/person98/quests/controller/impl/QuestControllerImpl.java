package com.person98.quests.controller.impl;

import com.google.common.collect.Lists;
import com.person98.quests.Quests;
import com.person98.quests.controller.QuestController;
import com.person98.quests.data.Quest;
import com.person98.quests.data.stage.RootType;
import com.person98.quests.data.stage.Stage;
import com.person98.quests.data.stage.impl.StageData;
import com.person98.quests.util.ConfigurationHelper;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

public class QuestControllerImpl implements QuestController {
   private final HashSet<Quest> quests = new HashSet<>();
   private final List<Quest> dailyQuests = Lists.newArrayList();
   private File dailyQuestFile;
   private FileConfiguration dailyQuestConfig;

   public void loadPersistedDailyQuests() {
      this.dailyQuestFile = new File(Quests.getInstance().getDataFolder(), "daily_quests.yml");
      if (!this.dailyQuestFile.exists()) {
         try {
            this.dailyQuestFile.createNewFile();
         } catch (Exception exception) {
            exception.printStackTrace();
         }
      }

      this.dailyQuestConfig = YamlConfiguration.loadConfiguration(this.dailyQuestFile);
      String lastReset = this.dailyQuestConfig.getString("last_reset_date", "");
      List<String> questIds = this.dailyQuestConfig.getStringList("daily_quests");
      this.dailyQuests.clear();

      for (String id : questIds) {
         Quest q = this.find(id);
         if (q != null && !q.isStory()) {
            this.dailyQuests.add(q);
         }
      }
   }

   @Override
   public void savePersistedDailyQuests(String date) {
      List<String> questIds = this.dailyQuests.stream().map(Quest::getIdentifier).toList();
      this.dailyQuestConfig.set("last_reset_date", date);
      this.dailyQuestConfig.set("daily_quests", questIds);

      try {
         this.dailyQuestConfig.save(this.dailyQuestFile);
      } catch (Exception exception) {
         exception.printStackTrace();
      }
   }

   public void ensureDailyQuestsUpToDate() {
      String resetTime = Quests.getInstance().getConfig().getString("reset-time", "00:00");
      LocalDate today = LocalDate.now();
      LocalTime currentTime = LocalTime.now();
      LocalTime resetTimeParsed = LocalTime.parse(resetTime, DateTimeFormatter.ofPattern("HH:mm"));
      String lastReset = this.dailyQuestConfig.getString("last_reset_date", "");
      boolean shouldReset = false;
      if (lastReset.isEmpty()) {
         shouldReset = currentTime.isAfter(resetTimeParsed);
      } else {
         try {
            LocalDate lastResetDate = LocalDate.parse(lastReset);
            shouldReset = lastResetDate.isBefore(today) && currentTime.isAfter(resetTimeParsed);
         } catch (Exception exception) {
            shouldReset = currentTime.isAfter(resetTimeParsed);
         }
      }

      if (shouldReset) {
         this.selectNewDailyQuests(5);
         this.savePersistedDailyQuests(today.toString());
         if (Quests.getInstance().getUserController() != null) {
            Quests.getInstance().getUserController().resetAllDailyQuestProgress();
         }
      } else if (this.dailyQuests.isEmpty()) {
         this.loadPersistedDailyQuests();
      }
   }

   @Override
   public void constructor(Quests quests) {
      this.quests.clear();
      File folder = new File(quests.getDataFolder() + File.separator + "quests");
      if (folder.mkdirs() || folder.exists()) {
         this.load(folder);
      }

      System.out.println("Successfully loaded " + this.quests.size() + " quests.");
      if (this.firstStory() != null) {
         System.out.println("Initial quest story: " + this.firstStory().getName());
      } else {
         System.out.println("Impossible to start a new story, the initial quest was not found.");
      }

      this.loadPersistedDailyQuests();
      this.ensureDailyQuestsUpToDate();
   }

   @Override
   public Quest find(String name) {
      return this.stream().filter(data -> data.getName().equalsIgnoreCase(name) || data.getIdentifier().equalsIgnoreCase(name)).findFirst().orElse(null);
   }

   @Override
   public Quest firstStory() {
      return this.stream().filter(data -> data.isStory() && data.isFirst()).findFirst().orElse(null);
   }

   @Override
   public List<Quest> getDailyQuests() {
      return this.dailyQuests;
   }

   public FileConfiguration getDailyQuestConfig() {
      return this.dailyQuestConfig;
   }

   @Override
   public void reloadDailyQuestConfig() {
      this.loadPersistedDailyQuests();
   }

   @Override
   public void forEach(Consumer<Quest> consumer) {
      this.stream().forEach(consumer);
   }

   @Override
   public Stream<Quest> stream() {
      return this.quests.stream();
   }

   @Override
   public void selectNewDailyQuests(int count) {
      this.dailyQuests.clear();
      List<Quest> dailyCandidates = this.quests.stream().filter(data -> !data.isStory()).toList();
      if (!dailyCandidates.isEmpty()) {
         int size = Math.min(dailyCandidates.size(), count);

         for (int i = 0; i < size; i++) {
            Quest quest;
            do {
               quest = dailyCandidates.get(ThreadLocalRandom.current().nextInt(dailyCandidates.size()));
            } while (quest == null || this.dailyQuests.contains(quest));

            this.dailyQuests.add(quest);
         }
      }
   }

   private void load(File folder) {
      File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml") || dir.isDirectory());
      if (files != null) {
         for (File file : files) {
            if (file.isDirectory()) {
               this.load(file);
            } else {
               String parent = "plugins" + File.separator + "Quests" + File.separator;
               ConfigurationHelper configuration = new ConfigurationHelper(Quests.getInstance(), file.getParent().replace(parent, ""), file.getName());
               System.out.println("Loading quests: " + file.getParent().replace(parent, "") + File.separator + file.getName());
               ConfigurationSection section = configuration.getConfigurationSection("quests");
               if (section != null) {
                  for (String key : section.getKeys(false)) {
                     Quest quest = new Quest(section.getString(key + ".name"), section.getInt(key + ".points"), section.getBoolean(key + ".story"));
                     quest.setIdentifier(key);
                     if (quest.isStory()) {
                        quest.setNext(section.getString(key + ".next"));
                        quest.setFirst(section.getBoolean(key + ".first"));
                     }

                     quest.setDescription(section.getStringList(key + ".description"));
                     quest.setDelay(section.getLong(key + ".delay", 1000L));
                     ConfigurationSection stageSection = section.getConfigurationSection(key + ".stages");
                     if (stageSection != null) {
                        for (String stageKey : stageSection.getKeys(false)) {
                           Stage stage = new Stage();
                           stage.setBossTitle(
                              ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(stageSection.getString(stageKey + ".bossTitle")))
                           );
                           stage.setFinishMessages(stageSection.getStringList(stageKey + ".finishMessages"));
                           stage.setStartMessages(stageSection.getStringList(stageKey + ".startMessages"));
                           stage.setRewards(stageSection.getStringList(stageKey + ".rewards"));
                           ConfigurationSection dataSection = stageSection.getConfigurationSection(stageKey + ".data");
                           if (dataSection != null) {
                              for (String objectiveKey : dataSection.getKeys(false)) {
                                 String type = dataSection.getString(objectiveKey + ".type");
                                 int amount = dataSection.getInt(objectiveKey + ".amount");
                                 RootType rootType = RootType.get(Objects.requireNonNull(dataSection.getString(objectiveKey + ".rootType")).toUpperCase());
                                 stage.getData()
                                    .add(new StageData(type, findFirstRoot(rootType, dataSection.getString(objectiveKey + ".root")), amount, rootType.name()));
                              }
                           }

                           quest.getStages().add(stage);
                        }
                     }

                     this.quests.add(quest);
                  }
               }
            }
         }
      }
   }

   public static Object findFirstRoot(RootType type, String root) {
      if (root == null) {
         return null;
      }
      return switch (type) {
         // Purpur 26.2 accepts namespaced identifiers while older quest files generally
         // contain enum names. matchMaterial(..., true) preserves both representations.
         case MATERIAL -> {
            Material material = Material.matchMaterial(root, true);
            yield material != null ? material : root;
         }
         case ENTITY_TYPE -> {
            String enumName = root.trim().toUpperCase(Locale.ROOT).replace('-', '_');
            EntityType entityType = Arrays.stream(EntityType.values())
               .filter(data -> data.name().equalsIgnoreCase(enumName) || data.getKey().asString().equalsIgnoreCase(root))
               .findFirst()
               .orElse(null);
            // Keep the original text if 26.2 no longer exposes the enum name. This is
            // essential for loading old persisted progress without turning its root null.
            yield entityType != null ? entityType : root;
         }
         default -> root;
      };
   }
}
