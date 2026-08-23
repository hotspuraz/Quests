package com.person98.quests;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.person98.quests.command.QueCommand;
import com.person98.quests.command.QuestCommand;
import com.person98.quests.command.QuestsTopCommand;
import com.person98.quests.controller.QuestController;
import com.person98.quests.controller.UserController;
import com.person98.quests.controller.impl.QuestControllerImpl;
import com.person98.quests.controller.impl.UserControllerImpl;
import com.person98.quests.data.User;
import com.person98.quests.data.stage.Stage;
import com.person98.quests.data.stage.impl.StageData;
import com.person98.quests.listener.MetaListener;
import com.person98.quests.listener.QuestListener;
import com.person98.quests.listener.UserListener;
import com.person98.quests.service.quest.QuestContainer;
import com.person98.quests.service.sql.Storage;
import com.person98.quests.service.sql.adapters.StageAdapter;
import com.person98.quests.service.sql.adapters.StageDataAdapter;
import com.person98.quests.task.DailyQuestResetTask;
import com.person98.quests.task.LeaderboardExcludedRunnable;
import com.person98.quests.task.UserQuestSchedule;
import com.person98.quests.task.UserUpdateSchedule;
import com.person98.quests.util.QuestPlaceholder;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.util.List;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Quests extends JavaPlugin implements Listener {
   private QuestController questController;
   private UserController userController;
   private Storage storage;
   private HikariDataSource hikariDataSource;
   private File rewardsFile;
   private FileConfiguration rewardsConfig;
   private final Gson gson = new GsonBuilder()
      .registerTypeAdapter(Stage.class, new StageAdapter())
      .registerTypeAdapter((new TypeToken<List<StageData>>() {}).getType(), new StageDataAdapter())
      .create();

   public void onLoad() {
      this.saveDefaultConfig();
      this.hikariDataSource = this.getDataSourceFromConfig();
   }

   public void onEnable() {
      this.storage = new Storage(this);
      this.questController = new QuestControllerImpl();
      this.questController.constructor(this);
      this.userController = new UserControllerImpl();
      this.userController.constructor(this);
      this.getServer().getPluginManager().registerEvents(new UserListener(), this);
      this.getServer().getPluginManager().registerEvents(new QuestListener(), this);
      new MetaListener();
      QuestContainer.initializeListeners();
      this.registerCommand("quests", new QuestCommand());
      this.registerCommand("queststop", new QuestsTopCommand());
      this.registerCommand("que", new QueCommand(this));
      // Capture persistence snapshots on the main thread; SQL itself is serialized by Storage.
      new UserUpdateSchedule().runTaskTimer(this, 0L, 1200L);
      new LeaderboardExcludedRunnable().runTaskTimer(this, 0L, 1200L);
      new UserQuestSchedule().runTaskTimer(this, 0L, 1L);
      new DailyQuestResetTask(this).runTaskTimer(this, 0L, 1200L);
      new QuestPlaceholder(this).register();
      this.saveDefaultRewardsConfig();
      this.loadRewardsConfig();
      this.getServer().getScheduler().runTaskTimer(this, () -> {
         if (this.userController != null) {
            this.userController.forEach(user -> this.storage.saveDailyQuestProgress(user));
         }
      }, 6000L, 6000L);
   }

   private void saveDefaultRewardsConfig() {
      this.saveResource("rewards.yml", false);
   }

   public void loadRewardsConfig() {
      this.rewardsFile = new File(this.getDataFolder(), "rewards.yml");
      if (!this.rewardsFile.exists()) {
         this.rewardsFile.getParentFile().mkdirs();
         this.saveResource("rewards.yml", false);
      }

      this.rewardsConfig = YamlConfiguration.loadConfiguration(this.rewardsFile);
   }

   public void executeRewardCommands(String playerName) {
      Player player = Bukkit.getPlayerExact(playerName);
      if (player != null) {
         List<String> commandList = this.rewardsConfig.getStringList("rewards.commands");
         if (!commandList.isEmpty()) {
            commandList.forEach(command -> {
               command = command.replace("%player%", playerName);
               if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                  command = PlaceholderAPI.setPlaceholders(player, command);
               }

               Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            });
         }

         LegacyComponentSerializer serializer = LegacyComponentSerializer.builder().character('&').hexColors().useUnusualXRepeatedCharacterHexFormat().build();

         for (String message : this.rewardsConfig.getStringList("rewards.messages")) {
            String processedMessage = message.replace("%player%", playerName);
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
               processedMessage = PlaceholderAPI.setPlaceholders(player, processedMessage);
            }

            player.sendMessage(serializer.deserialize(processedMessage));
         }
      }
   }

   public void onDisable() {
      if (this.getUserController() != null) {
         this.getUserController().forEach(User::clearBossBar);
      }

      if (this.userController != null) {
         this.userController.destructor(this);
      }

      if (this.storage != null) {
         this.storage.shutdown();
      }

      if (this.hikariDataSource != null) {
         this.hikariDataSource.close();
      }
   }

   public static Quests getInstance() {
      return (Quests)getPlugin(Quests.class);
   }

   private HikariDataSource getDataSourceFromConfig() {
      FileConfiguration fileConfiguration = this.getConfig();
      HikariDataSource dataSource = new HikariDataSource();
      String host = fileConfiguration.getString("mysql.host");
      String database = fileConfiguration.getString("mysql.database");
      int port = fileConfiguration.getInt("mysql.port");
      String username = fileConfiguration.getString("mysql.username");
      String password = fileConfiguration.getString("mysql.password");
      dataSource.setMaximumPoolSize(20);
      dataSource.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
      dataSource.setUsername(username);
      dataSource.setPassword(password);
      dataSource.addDataSourceProperty("useSSL", "false");
      dataSource.addDataSourceProperty("autoReconnect", "true");
      dataSource.addDataSourceProperty("cachePrepStmts", "true");
      dataSource.addDataSourceProperty("prepStmtCacheSize", "250");
      dataSource.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
      dataSource.addDataSourceProperty("useServerPrepStmts", "true");
      return dataSource;
   }

   private void registerCommand(String command, CommandExecutor clazz) {
      PluginCommand pluginCommand = this.getCommand(command);
      if (pluginCommand != null) {
         pluginCommand.setExecutor(clazz);
      }
   }

   public QuestController getQuestController() {
      return this.questController;
   }

   public UserController getUserController() {
      return this.userController;
   }

   public Storage getStorage() {
      return this.storage;
   }

   public HikariDataSource getHikariDataSource() {
      return this.hikariDataSource;
   }

   public File getRewardsFile() {
      return this.rewardsFile;
   }

   public FileConfiguration getRewardsConfig() {
      return this.rewardsConfig;
   }

   public Gson getGson() {
      return this.gson;
   }
}
