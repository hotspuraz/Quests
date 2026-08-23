package com.person98.imevoquest.service.sql;

public interface SQLQuery {
   String[] TABLES = new String[]{
      "CREATE TABLE IF NOT EXISTS `quest_users` (`uniqueId` VARCHAR(36) NOT NULL, `points` INTEGER(12) NOT NULL, `viewStatus` BOOLEAN NOT NULL, `story` BOOLEAN NOT NULL, PRIMARY KEY(`uniqueId`));",
      "CREATE TABLE IF NOT EXISTS `quest_completed` (`id` INTEGER(36) NOT NULL AUTO_INCREMENT, `user_id` VARCHAR(36) NOT NULL, `quest` VARCHAR(36) NOT NULL, PRIMARY KEY(`id`));",
      "CREATE TABLE IF NOT EXISTS `quest_current` (`id` VARCHAR(80) NOT NULL, `user_id` VARCHAR(36) NOT NULL, `quest` VARCHAR(36) NOT NULL, `stage_id` INTEGER(12) NOT NULL, `stage_data` MEDIUMTEXT NOT NULL, PRIMARY KEY(`id`));",
      "CREATE TABLE IF NOT EXISTS `daily_quest_progress` (`user_id` VARCHAR(36) NOT NULL, `quest_id` VARCHAR(36) NOT NULL, `stage_index` INTEGER(12) NOT NULL DEFAULT 0, `stage_data_json` MEDIUMTEXT NOT NULL DEFAULT '{}', `completed` BOOLEAN NOT NULL DEFAULT FALSE, `last_updated` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, PRIMARY KEY(`user_id`, `quest_id`));",
      "CREATE TABLE IF NOT EXISTS `daily_quest_ongoing` (`user_id` VARCHAR(36) NOT NULL, `ongoing_quest_id` VARCHAR(36) NULL, `last_daily_reward_date` VARCHAR(10) NULL, PRIMARY KEY(`user_id`));"
   };

   public interface CompletedQuery {
      String INSERT = "INSERT INTO `quest_completed` (`user_id`, `quest`) SELECT * FROM(SELECT '%s', '%s') AS tmp WHERE NOT EXISTS (SELECT `user_id` FROM `quest_completed` WHERE `user_id` = '%s' AND `quest` = '%s') LIMIT 1;";
      String FIND = "SELECT * FROM `quest_completed` WHERE `user_id` = ?;";
      String DELETE_QUEST = "DELETE FROM `quest_completed` WHERE `user_id` = '%s' AND `quest` = '%s';";
      String DELETE = "DELETE FROM `quest_completed` WHERE `user_id` = ?";
   }

   public interface CurrentQuery {
      String INSERT_OR_UPDATE = "INSERT INTO `quest_current` (`id`, `user_id`, `quest`, `stage_id`, `stage_data`) VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE `stage_id` = ?, `stage_data` = ?;";
      String FIND = "SELECT * FROM `quest_current` WHERE `user_id` = ?;";
      String DELETE_QUEST = "DELETE FROM `quest_current` WHERE `user_id` = '%s' AND `quest` = '%s';";
      String DELETE = "DELETE FROM `quest_current` WHERE `user_id` = ?";
   }

   public interface DailyQuestOngoingQuery {
      String INSERT = "INSERT INTO `daily_quest_ongoing` (`user_id`, `ongoing_quest_id`, `last_daily_reward_date`) VALUES (?,?,?) ON DUPLICATE KEY UPDATE `ongoing_quest_id` = ?, `last_daily_reward_date` = ?;";
      String FIND = "SELECT * FROM `daily_quest_ongoing` WHERE `user_id` = ?;";
      String DELETE_USER = "DELETE FROM `daily_quest_ongoing` WHERE `user_id` = ?;";
   }

   public interface DailyQuestProgressQuery {
      String INSERT = "INSERT INTO `daily_quest_progress` (`user_id`, `quest_id`, `stage_index`, `stage_data_json`, `completed`) VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE `stage_index` = ?, `stage_data_json` = ?, `completed` = ?;";
      String FIND = "SELECT * FROM `daily_quest_progress` WHERE `user_id` = ?;";
      String DELETE_USER = "DELETE FROM `daily_quest_progress` WHERE `user_id` = ?;";
      String DELETE_QUEST = "DELETE FROM `daily_quest_progress` WHERE `user_id` = ? AND `quest_id` = ?;";
   }

   public interface UserQuery {
      String INSERT_OR_UPDATE = "INSERT INTO `quest_users` (`uniqueId`, `points`, `viewStatus`, `story`) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE `points` = ?, `viewStatus` = ?, `story` = ?;";
      String FIND = "SELECT * FROM `quest_users` WHERE `uniqueId` = ?;";
      String LEADERBOARD = "SELECT * FROM `quest_users` ORDER BY `points` DESC;";
   }
}
