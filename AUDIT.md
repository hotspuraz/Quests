# Quests code audit

Branch `cleanup` (from `5bb3d76`). Read-only review so far: no behaviour has been changed.
Reviewed: storage/SQL, `User`, `Quest`, `Stage`, controllers, tasks, `UserListener`, `QueCommand`, `Cache`, build and `plugin.yml`.
Not yet reviewed: the 21 quest-type listeners, the inventories (SmartInvs UI), `ItemStackBuilder`, `QuestPlaceholder`, `StageData`.

Severity = likelihood x impact on a live server.

## High

1. **Daily reset can be skipped or doubled** - `task/DailyQuestResetTask.java`
   It runs every 1200 ticks and only resets when the clock string equals `reset-time` exactly (`now.equals(resetTime)`).
   Tick drift or a lag spike can miss the target minute, and then nothing resets until the next day.
   `lastResetDate` is kept only in memory, so a restart during the reset minute resets a second time.
   A more robust version already exists, `QuestControllerImpl.ensureDailyQuestsUpToDate()` (compares against the persisted `last_reset_date`), but it only runs at startup.
   The two code paths use different rules. The reset also does blocking SQL on the main thread (`wipeDailyQuestProgress` flushes the writer and runs DELETEs).
   Fix: one catch-up method (`lastReset < today && now >= resetTime`), called every minute, with the DB work off-thread.

2. **`Quest.equals/hashCode` walk the whole quest graph** - `data/Quest.java`
   `hashCode` includes `getNext()` (a controller lookup) and every stage, and `Quest` is the key of the `ConcurrentHashMap`s in `User`.
   Every map lookup re-hashes the stage list and the entire `next` chain.
   If two story quests ever point at each other (config typo), `hashCode`/`equals` recurse until `StackOverflowError`.
   It also hashes mutable fields while quests live in a `HashSet`.
   This is decompiled-Lombok output. Fix: equality by `identifier` only (same for `User`: `uniqueId`).

3. **Silent persistence failures** - `StorageImplementation`
   `saveUser`, `saveDailyQuestProgress*`, `reset`, `wipeDailyQuestProgress` catch `SQLException`/`Exception` and only `printStackTrace()`.
   A DB outage means lost progress with nothing in the logs and no retry. `flushPendingWrites()` cannot see the failure either.
   Fix: log through the plugin logger, and decide on a retry or re-queue policy.

## Medium

4. **SQL built with `String.format`** - `StorageImplementation.saveUser` (`quest_completed` insert; also the unused `CompletedQuery.INSERT/DELETE_QUEST` templates in `SQLQuery`).
   Quest ids come from admin-written YAML keys, so exploitability is low, but an id containing `'` breaks saving for every user in the batch.
   Fix: use a prepared statement, with a `UNIQUE(user_id, quest)` key so the `NOT EXISTS ... LIMIT 1` trick is not needed.
   `quest_completed` currently has no uniqueness constraint.

5. **Id column widths** - `SQLQuery.TABLES`
   `quest` / `quest_id` / `ongoing_quest_id` are `VARCHAR(36)`. A longer quest id fails or truncates, depending on SQL mode.
   `quest_current.id` is `VARCHAR(80)` (`uuid#questId`). Validate id length at load time, or widen the columns.

6. **Leaderboard loads the whole table every minute** - `recalculateLeaderboard`: `SELECT * FROM quest_users ORDER BY points DESC`, no `LIMIT`, and the `points` column is not indexed.
   One bad UUID row throws and discards the whole result. `lastRecalculated` is a non-volatile `Long` read from several threads.

7. **Async pre-login touches shared state** - `UserListener.onPlayerPreLogin` -> `UserControllerImpl.create`
   It runs on an async thread and reads `getDailyQuests()` (a plain `ArrayList` that the main thread replaces at reset).
   Players denied by another plugin after pre-login stay cached for the cache TTL (3650 days), which leaks memory and can serve stale data in a multi-server setup.

8. **`loadUser` null stage** - a `quest_current.stage_data` that deserializes to `null` goes into a `ConcurrentHashMap`, which throws NPE and blocks that player's login.

9. **Hardcoded config path hack** - `QuestControllerImpl.load`
   Builds `"plugins" + File.separator + "Quests" + File.separator` and strips it from `file.getParent()`. This breaks when the server's plugin folder is not literally `plugins/Quests` (custom `--plugins` dir, or a different relative layout).
   `ConfigurationHelper` is a 200-line wrapper around what `YamlConfiguration.loadConfiguration(file)` does directly.

10. **Permissions** - `plugin.yml` declares no permissions. `quests.admin` is checked in code but never registered, so it has no default or description.
    Player commands (`/quests`, `/queststop`) have no nodes.

## Low / cleanup

11. **Decompiled boilerplate** - `Quest`, `User` (and likely others) carry 100+ lines of generated `equals/canEqual/hashCode/toString` and plain getters/setters. `User.toString` even prints the BossBar and every stage. Candidates for records or deletion.
12. **Logging** - 18 uses of `System.out`/`printStackTrace` instead of the plugin logger (`User.getCurrentStage` swallows exceptions into `System.out`).
13. **Dead and redundant code** - unused imports in `Storage` (`Lists`, `Arrays`...), the unused `SQLQuery` inner query interfaces, `StorageImplementation.task`, redundant `close()` calls inside try-with-resources, `new Gson()` per call in `User.switchToDailyQuest` instead of the plugin's shared one.
14. **Duplication** - the same "remove quest from quests/currentQuests/questsStage/disabledQuests" block appears ~6 times across `User`, `UserListener`, `UserControllerImpl`; boss-bar update logic is duplicated in `updateBossBar` and `forceShowBossBarFor`.
15. **Hardcoded strings and numbers** - "Simple Survival"/"SimpleQuests" branding and `§` prefixes in `QueCommand`, daily count `5`, save interval `180000L`. Move to `config.yml` / a message file.
16. **Legacy APIs** - `ChatColor`, `ItemMeta.setLore` (41 deprecation warnings on build). Migrate to Adventure `Component`s.
17. **Dependency risk** - SmartInvs 1.2.7 is a hard dependency for the menus. Check it is maintained and works on current Paper before relying on it long-term.
18. **Tests** - only 3 test classes (stage compatibility, plugin descriptor, cache). Nothing covers reset logic, storage, `User` progress flows or the quest YAML loader.

## Suggested order

1. Reset logic (1) and `Quest`/`User` equality (2), with tests for both.
2. Logging and failure handling in storage (3, 12), then prepared statements and schema fixes (4, 5).
3. Loader path fix, permissions, config-driven messages (9, 10, 15).
4. Boilerplate and duplication removal (11, 13, 14) once the tests are in place.
5. Leaderboard and pre-login hardening (6, 7, 8), then API modernisation (16, 17).
