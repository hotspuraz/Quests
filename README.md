# Quests

Internal reference for the Simple Survival storyline and daily quest system.

Current version: `1.0.3`

## Requirements

- Purpur `26.2`
- Java 25
- MySQL
- SmartInvs `1.2.7` or compatible
- PlaceholderAPI `2.11.6` or compatible
- Optional: UltimateTimber

SmartInvs and PlaceholderAPI are hard dependencies: Purpur will not enable Quests unless both plugins are present. The plugin uses HikariCP for its MySQL connection pool; HikariCP is included in the Quests JAR.

## Features

- Sequential storyline quests.
- Rotating daily quests with configurable reset time.
- Inventory menus for storyline, daily, and leaderboard views.
- Boss-bar progress display that players can toggle.
- MySQL-backed user, quest, stage, and leaderboard data.
- Persistent daily progress and scheduled asynchronous database writes.
- Command-based quest and global completion rewards.
- PlaceholderAPI points and leaderboard placeholders.
- Optional UltimateTimber quest progress.

## Commands

### Player commands

| Command | Aliases | Description |
| --- | --- | --- |
| `/quests` | `/quest` | Opens the main quest menu. Players only. |
| `/quests bossbar` | `/quest bossbar` | Toggles the active quest progress boss bar. |
| `/quests cancel` | `/quest cancel` | Cancels the active daily quest, or stops tracking the current storyline quest. |
| `/queststop [page]` | `/questtop [page]` | Displays the points leaderboard, ten entries per page. Console-compatible. |

### Administrator commands

| Command | Description |
| --- | --- |
| `/que` | Shows administrator command usage. |
| `/que reload` | Validates new MySQL settings, replaces the connection pool, and reloads `config.yml`, `rewards.yml`, and `daily_quests.yml`. |
| `/que reset <player>` | Resets stored quest data for an online or offline player. |
| `/que set <player> daily` | Test utility that marks an online player's current storyline quest complete and unlocks daily quests. |

The `set ... daily` command is explicitly a test/administration utility. It permanently changes the selected player's storyline state and should not be used casually.

## Permissions

| Permission | Default | Description |
| --- | --- | --- |
| `quests.admin` | Not declared in `plugin.yml` | Required for every `/que` subcommand. Grant it explicitly through the server permission manager. Operators do not automatically receive this node from the plugin descriptor. |

The player commands do not check permissions.

## PlaceholderAPI placeholders

The internal `quests` expansion is registered on startup and persists through PlaceholderAPI reloads.

| Placeholder | Result |
| --- | --- |
| `%quests_points%` | The selected player's current leaderboard points. Returns an empty string if the player is not present. |
| `%quests_name_<position>%` | Player name at the one-based leaderboard position. |
| `%quests_points_<position>%` | Point total at the one-based leaderboard position. |

Examples:

- `%quests_name_1%`
- `%quests_points_1%`
- `%quests_name_10%`

Out-of-range leaderboard positions return an empty string. Position parameters must be valid integers; malformed values may throw an error and should not be used.

## Internal substitutions

These are configuration substitutions rather than placeholders supplied by the Quests PlaceholderAPI expansion.

| Token | Location | Result |
| --- | --- | --- |
| `%player%` | Quest stage rewards, `rewards.yml` commands, and reward messages | Completing player's name. |
| `%name%` | Quest inventory item names in `config.yml` | Quest display name. |

After replacing `%player%`, global commands and messages in `rewards.yml` are also passed through PlaceholderAPI. This allows placeholders from other installed expansions to be used there.

## Configuration and data

| Path | Purpose |
| --- | --- |
| `config.yml` | MySQL connection, menu items, reset scheduling, and daily-reset messages. |
| `rewards.yml` | Global completion reward commands and player messages. |
| `daily_quests.yml` | IDs selected for the active daily quest rotation. Created and maintained at runtime. |
| `quests/*.yml` | Storyline and daily quest definitions supplied by the server. |
| `daily_progress/*.yml` | Compatibility/migration copies of per-player daily progress. Current progress is also persisted to MySQL. |

No quest-definition files are bundled in the repository. The live server must retain its existing `plugins/Quests/quests` directory.

## MySQL

```yaml
mysql:
  host: localhost
  database: quests
  port: 3306
  username: quests
  password: change_me
```

The plugin creates and maintains its tables automatically. It disables itself if the database is unavailable during startup. `/que reload` validates a replacement connection before closing the working pool.

The HikariCP pool is configured with a maximum of 20 connections and a ten-second connection timeout.

## Daily quest schedule

```yaml
reset-time: "00:00"
excluded_days: 1
```

- `reset-time` uses server-local 24-hour `HH:mm` format.
- The reset task checks the schedule every 1,200 ticks.
- `daily-quest-reset-message` is broadcast when the daily rotation refreshes.
- Daily progress is periodically persisted, including a five-minute save cycle.

## Quest definition structure

Quest files are recursively loaded from `plugins/Quests/quests`. Each top-level key represents a quest and supplies its name, points, storyline state, stages, objectives, and reward commands.

Objective data uses:

- `type`: registered quest event type.
- `rootType`: `MATERIAL`, `ENTITY_TYPE`, or `NONE`.
- `root`: the relevant material/entity/command/chat value.
- `amount`: required progress.

Supported built-in progress listeners include:

- Block break and block place
- Chat and click interactions
- Item consumption and collection
- Crafting and smelting
- Damage
- Player-command and server-command execution
- Mob and player kills
- Piglin bartering
- Fishing
- Sheep shearing
- Animal taming and breeding
- UltimateTimber activity when the integration is available

Quest-specific reward commands execute as console and support `%player%`.

## Menus

`config.yml` defines display items for:

- Main menu
- Storyline menu
- Daily menu
- Completed, incomplete, disabled, and current quest states

Materials must use valid Bukkit/Purpur material names. Item names and lore support legacy `&` colour codes; reward messages also support hex colours.

## Operational notes

- Preserve the live `quests` directory; definitions are not packaged in the JAR.
- Back up MySQL and the complete plugin data directory before resets or migrations.
- `/que reset` affects offline players as well as online players.
- Database writes are serialized through a dedicated `Quests-SQL-Writer` thread.
- Player state snapshots are captured on the server thread before asynchronous persistence.
- Leaderboard recalculation can temporarily return an empty/busy result.
- Changing MySQL settings requires `/que reload` or a restart.

## Building

Windows: `gradlew.bat clean build`

Linux/macOS: `./gradlew clean build`

The server-ready artifact is `build/libs/Quests-1.0.3.jar`. It includes HikariCP and its required SLF4J API classes.
