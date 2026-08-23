package com.person98.quests.service.sql.impl;

import com.google.common.collect.Lists;
import com.person98.quests.Quests;
import com.person98.quests.data.Quest;
import com.person98.quests.data.SimpleUser;
import com.person98.quests.data.User;
import com.person98.quests.data.stage.Stage;
import com.person98.quests.service.sql.SQLQuery;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class StorageImplementation implements SQLQuery {
   private static final Executor DIRECT_EXECUTOR = Runnable::run;
   private final Quests quests;
   private BukkitTask task;

   public StorageImplementation(Quests quests) {
      this.quests = quests;

      try (
         Connection connection = this.quests.getHikariDataSource().getConnection();
         Statement statement = connection.createStatement();
      ) {
         for (String table : TABLES) {
            statement.addBatch(table);
         }

         statement.executeBatch();
         statement.clearBatch();
         connection.close();
      } catch (SQLException exception) {
         throw new IllegalStateException("Could not initialize the Quests database schema", exception);
      }

      this.quests.getLogger().info("Database tables verified successfully.");
   }

   public User loadUser(UUID uniqueId) {
      User user = new User(uniqueId);

      try (Connection connection = this.quests.getHikariDataSource().getConnection()) {
         try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `quest_users` WHERE `uniqueId` = ?;")) {
            preparedStatement.setString(1, uniqueId.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
               user.setPoints(resultSet.getInt("points"));
               user.setViewStatus(resultSet.getBoolean("viewStatus"));
               user.setStory(resultSet.getBoolean("story"));
            }

            preparedStatement.close();
            resultSet.close();
         }

         try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `quest_completed` WHERE `user_id` = ?;")) {
            preparedStatement.setString(1, uniqueId.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
               Quest quest = this.quests.getQuestController().find(resultSet.getString("quest"));
               if (quest != null) {
                  user.getCompletedTasks().add(quest);
               }
            }

            preparedStatement.close();
            resultSet.close();
         }

         try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `quest_current` WHERE `user_id` = ?;")) {
            preparedStatement.setString(1, uniqueId.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
               Quest quest = this.quests.getQuestController().find(resultSet.getString("quest"));
               if (quest != null && !user.getCompletedTasks().contains(quest) && quest.isStory()) {
                  user.getQuests().add(quest);
                  user.getCurrentQuests().put(quest, resultSet.getInt("stage_id"));
                  user.getQuestsStage().put(quest, (Stage)this.quests.getGson().fromJson(resultSet.getString("stage_data"), Stage.class));
               }
            }
         }
      } catch (SQLException exception) {
         throw new IllegalStateException("Could not load quest progress for " + uniqueId, exception);
      }

      return user;
   }

   public void saveDailyQuestProgress(User user) {
      try (Connection connection = this.quests.getHikariDataSource().getConnection()) {
         connection.setNetworkTimeout(DIRECT_EXECUTOR, 10000);

         try (PreparedStatement preparedStatement = connection.prepareStatement(
               "INSERT INTO `daily_quest_ongoing` (`user_id`, `ongoing_quest_id`, `last_daily_reward_date`) VALUES (?,?,?) ON DUPLICATE KEY UPDATE `ongoing_quest_id` = ?, `last_daily_reward_date` = ?;"
            )) {
            preparedStatement.setString(1, user.getUniqueId().toString());
            preparedStatement.setString(2, user.getOngoingDailyQuestId());
            preparedStatement.setString(3, user.getLastDailyRewardDate());
            preparedStatement.setString(4, user.getOngoingDailyQuestId());
            preparedStatement.setString(5, user.getLastDailyRewardDate());
            preparedStatement.execute();
         }

         try (PreparedStatement preparedStatement = connection.prepareStatement(
               "INSERT INTO `daily_quest_progress` (`user_id`, `quest_id`, `stage_index`, `stage_data_json`, `completed`) VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE `stage_index` = ?, `stage_data_json` = ?, `completed` = ?;"
            )) {
            for (Entry<String, User.DailyQuestProgress> entry : user.getDailyQuestProgress().entrySet()) {
               String questId = entry.getKey();
               User.DailyQuestProgress progress = entry.getValue();
               preparedStatement.setString(1, user.getUniqueId().toString());
               preparedStatement.setString(2, questId);
               preparedStatement.setInt(3, progress.stageIndex);
               preparedStatement.setString(4, progress.stageDataJson);
               preparedStatement.setBoolean(5, progress.completed);
               preparedStatement.setInt(6, progress.stageIndex);
               preparedStatement.setString(7, progress.stageDataJson);
               preparedStatement.setBoolean(8, progress.completed);
               preparedStatement.execute();
            }
         }
      } catch (SQLException exception) {
         exception.printStackTrace();
      } catch (Exception exception) {
         exception.printStackTrace();
      }
   }

   public void saveDailyQuestProgressBatch(Collection<User> users) {
      try (Connection connection = this.quests.getHikariDataSource().getConnection()) {
         connection.setNetworkTimeout(DIRECT_EXECUTOR, 15000);

         try (PreparedStatement preparedStatement = connection.prepareStatement(
               "INSERT INTO `daily_quest_ongoing` (`user_id`, `ongoing_quest_id`, `last_daily_reward_date`) VALUES (?,?,?) ON DUPLICATE KEY UPDATE `ongoing_quest_id` = ?, `last_daily_reward_date` = ?;"
            )) {
            for (User user : users) {
               preparedStatement.setString(1, user.getUniqueId().toString());
               preparedStatement.setString(2, user.getOngoingDailyQuestId());
               preparedStatement.setString(3, user.getLastDailyRewardDate());
               preparedStatement.setString(4, user.getOngoingDailyQuestId());
               preparedStatement.setString(5, user.getLastDailyRewardDate());
               preparedStatement.addBatch();
               preparedStatement.clearParameters();
            }

            preparedStatement.executeBatch();
            preparedStatement.clearBatch();
         }

         try (PreparedStatement preparedStatement = connection.prepareStatement(
               "INSERT INTO `daily_quest_progress` (`user_id`, `quest_id`, `stage_index`, `stage_data_json`, `completed`) VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE `stage_index` = ?, `stage_data_json` = ?, `completed` = ?;"
            )) {
            for (User user : users) {
               for (Entry<String, User.DailyQuestProgress> entry : user.getDailyQuestProgress().entrySet()) {
                  String questId = entry.getKey();
                  User.DailyQuestProgress progress = entry.getValue();
                  preparedStatement.setString(1, user.getUniqueId().toString());
                  preparedStatement.setString(2, questId);
                  preparedStatement.setInt(3, progress.stageIndex);
                  preparedStatement.setString(4, progress.stageDataJson);
                  preparedStatement.setBoolean(5, progress.completed);
                  preparedStatement.setInt(6, progress.stageIndex);
                  preparedStatement.setString(7, progress.stageDataJson);
                  preparedStatement.setBoolean(8, progress.completed);
                  preparedStatement.addBatch();
                  preparedStatement.clearParameters();
               }
            }

            preparedStatement.executeBatch();
            preparedStatement.clearBatch();
         }
      } catch (SQLException exception) {
         exception.printStackTrace();
      } catch (Exception exception) {
         exception.printStackTrace();
      }
   }

   public void loadDailyQuestProgress(User user) {
      try (Connection connection = this.quests.getHikariDataSource().getConnection()) {
         try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `daily_quest_ongoing` WHERE `user_id` = ?;")) {
            preparedStatement.setString(1, user.getUniqueId().toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
               user.setOngoingDailyQuestId(resultSet.getString("ongoing_quest_id"));
               user.setLastDailyRewardDate(resultSet.getString("last_daily_reward_date"));
            }
         }

         try (PreparedStatement preparedStatementx = connection.prepareStatement("SELECT * FROM `daily_quest_progress` WHERE `user_id` = ?;")) {
            preparedStatementx.setString(1, user.getUniqueId().toString());
            ResultSet resultSet = preparedStatementx.executeQuery();
            user.getDailyQuestProgress().clear();

            while (resultSet.next()) {
               String questId = resultSet.getString("quest_id");
               int stageIndex = resultSet.getInt("stage_index");
               String stageDataJson = resultSet.getString("stage_data_json");
               boolean completed = resultSet.getBoolean("completed");
               user.getDailyQuestProgress().put(questId, new User.DailyQuestProgress(stageIndex, stageDataJson, completed));
            }
         }
      } catch (SQLException exception) {
         throw new IllegalStateException("Could not load daily quest progress for " + user.getUniqueId(), exception);
      }
   }

   public void wipeDailyQuestProgress(User user) {
      try (Connection connection = this.quests.getHikariDataSource().getConnection()) {
         try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM `daily_quest_progress` WHERE `user_id` = ?;")) {
            preparedStatement.setString(1, user.getUniqueId().toString());
            preparedStatement.execute();
         }

         try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM `daily_quest_ongoing` WHERE `user_id` = ?;")) {
            preparedStatement.setString(1, user.getUniqueId().toString());
            preparedStatement.execute();
         }
      } catch (SQLException exception) {
         exception.printStackTrace();
      }
   }

   public void wipeAllDailyQuestProgress() {
      try (Connection connection = this.quests.getHikariDataSource().getConnection()) {
         connection.setAutoCommit(false);
         try (Statement statement = connection.createStatement()) {
            statement.execute("DELETE FROM `daily_quest_progress`");
         }

         try (Statement statement = connection.createStatement()) {
            statement.execute("DELETE FROM `daily_quest_ongoing`");
         }
         connection.commit();
      } catch (SQLException exception) {
         throw new IllegalStateException("Could not reset daily quest progress", exception);
      }
   }

   public void saveUser(User... users) {
      try (Connection connection = this.quests.getHikariDataSource().getConnection()) {
         connection.setNetworkTimeout(DIRECT_EXECUTOR, 30000);

         try (PreparedStatement preparedStatement = connection.prepareStatement(
               "INSERT INTO `quest_users` (`uniqueId`, `points`, `viewStatus`, `story`) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE `points` = ?, `viewStatus` = ?, `story` = ?;"
            )) {
            for (User user : users) {
               user.setLastUpdate(System.currentTimeMillis() + 180000L);
               preparedStatement.setString(1, user.getUniqueId().toString());
               preparedStatement.setInt(2, user.getPoints());
               preparedStatement.setBoolean(3, user.isViewStatus());
               preparedStatement.setBoolean(4, user.isStory());
               preparedStatement.setInt(5, user.getPoints());
               preparedStatement.setBoolean(6, user.isViewStatus());
               preparedStatement.setBoolean(7, user.isStory());
               preparedStatement.addBatch();
               preparedStatement.clearParameters();
            }

            preparedStatement.executeBatch();
            preparedStatement.clearBatch();
         }

         try (Statement statement = connection.createStatement()) {
            for (User user : users) {
               if (!user.getCompletedTasks().isEmpty()) {
                  for (Quest quest : user.getCompletedTasks()) {
                     if (quest.isStory()) {
                        String query = "INSERT INTO `quest_completed` (`user_id`, `quest`) SELECT * FROM(SELECT '%s', '%s') AS tmp WHERE NOT EXISTS (SELECT `user_id` FROM `quest_completed` WHERE `user_id` = '%s' AND `quest` = '%s') LIMIT 1;";
                        statement.addBatch(
                           String.format(query, user.getUniqueId().toString(), quest.getIdentifier(), user.getUniqueId().toString(), quest.getIdentifier())
                        );
                     }
                  }
               }
            }

            statement.executeBatch();
         }

         try (PreparedStatement preparedStatement = connection.prepareStatement(
               "INSERT INTO `quest_current` (`id`, `user_id`, `quest`, `stage_id`, `stage_data`) VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE `stage_id` = ?, `stage_data` = ?;"
            )) {
            for (User user : users) {
               if (!user.getQuests().isEmpty()) {
                  for (Quest quest : user.getQuests()) {
                     if (quest.isStory()) {
                        preparedStatement.setString(1, user.getUniqueId().toString() + "#" + quest.getIdentifier());
                        preparedStatement.setString(2, user.getUniqueId().toString());
                        preparedStatement.setString(3, quest.getIdentifier());
                        int stageId = user.getCurrentQuests().get(quest);
                        String stageData = this.quests.getGson().toJson(user.getCurrentStageData(quest), Stage.class);
                        preparedStatement.setInt(4, stageId);
                        preparedStatement.setString(5, stageData);
                        preparedStatement.setInt(6, stageId);
                        preparedStatement.setString(7, stageData);
                        preparedStatement.addBatch();
                        preparedStatement.clearParameters();
                     }
                  }
               }
            }

            preparedStatement.executeBatch();
            preparedStatement.clearBatch();
         }

         for (User user : users) {
            if (!user.getDailyQuestProgress().isEmpty() || user.getOngoingDailyQuestId() != null) {
               this.saveDailyQuestProgress(user);
            }
         }
      } catch (SQLException exception) {
         exception.printStackTrace();
      } catch (Exception exception) {
         exception.printStackTrace();
      }
   }

   public void reset(UUID uniqueId) {
      try (Connection connection = this.quests.getHikariDataSource().getConnection()) {
         try (PreparedStatement preparedStatement = connection.prepareStatement(
               "INSERT INTO `quest_users` (`uniqueId`, `points`, `viewStatus`, `story`) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE `points` = ?, `viewStatus` = ?, `story` = ?;"
            )) {
            preparedStatement.setString(1, uniqueId.toString());
            preparedStatement.setInt(2, 0);
            preparedStatement.setBoolean(3, true);
            preparedStatement.setBoolean(4, true);
            preparedStatement.setInt(5, 0);
            preparedStatement.setBoolean(6, true);
            preparedStatement.setBoolean(7, true);
            preparedStatement.execute();
         }

         try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM `quest_completed` WHERE `user_id` = ?")) {
            preparedStatement.setString(1, uniqueId.toString());
            preparedStatement.execute();
         }

         try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM `quest_current` WHERE `user_id` = ?")) {
            preparedStatement.setString(1, uniqueId.toString());
            preparedStatement.execute();
         }

         User user = this.quests.getUserController().find(uniqueId);
         if (user != null) {
            user.setPoints(0);
            user.setViewStatus(true);
            user.getCompletedTasks().clear();
            user.getQuests().clear();
            user.getQuestsStage().clear();
            user.getCurrentQuests().clear();
            Quest quest = this.quests.getQuestController().firstStory();
            if (quest != null) {
               user.initializeQuest(quest);
            }
         }
      } catch (SQLException exception) {
         exception.printStackTrace();
      }
   }

   public void recalculateLeaderboard(Collection<String> excluded, Consumer<List<SimpleUser>> callback, Consumer<Throwable> failure) {
      this.task = Bukkit.getScheduler().runTaskAsynchronously(this.quests, () -> {
         List<SimpleUser> calculated = Lists.newArrayList();
         try (Connection connection = this.quests.getHikariDataSource().getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `quest_users` ORDER BY `points` DESC;")) {
               ResultSet resultSet = preparedStatement.executeQuery();

               while (resultSet.next()) {
                  UUID uniqueId = UUID.fromString(resultSet.getString("uniqueId"));
                  int points = resultSet.getInt("points");
                  if (!excluded.contains(uniqueId.toString())) {
                     calculated.add(new SimpleUser(uniqueId, points));
                  }
               }
            }
            callback.accept(List.copyOf(calculated));
         } catch (Exception exception) {
            failure.accept(exception);
         }
      });
   }

   public BukkitTask getTask() {
      return this.task;
   }
}
