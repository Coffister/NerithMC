# NERITHCORE.md
> Build specification for Claude Code — Nerith OneBlock Server  
> Do not skip sections. Read fully before writing any code.

---

## 1. Project Overview

**Server name:** Nerith  
**Type:** OneBlock — CZ/SK community server  
**Theme:** Floating city lore, epic/adventure tone, heroic rebirth narrative  
**Player role:** Explorer stranded on a void fragment, rebuilding the lost city of Nerith  
**Target version:** PaperMC 1.21.x  
**Java version:** Java 21  
**Language:** English codebase — Slovak localization deferred (messages.yml placeholder only for now)

---

## 2. Server Installation

### 2.1 Requirements
- Ubuntu 22.04 VPS
- Java 21 (`apt install openjdk-21-jdk`)
- Minimum 6 GB RAM allocated (8 GB recommended)
- Pterodactyl panel (optional but recommended for management)
- MySQL 8.x database server

### 2.2 PaperMC Setup
```bash
mkdir -p /opt/nerith
cd /opt/nerith
wget https://api.papermc.io/v2/projects/paper/versions/1.21.4/builds/LATEST/downloads/paper-1.21.4-LATEST.jar -O paper.jar
echo "eula=true" > eula.txt
java -Xms6G -Xmx6G -jar paper.jar --nogui
```

### 2.3 Recommended JVM flags
```
-Xms6G -Xmx6G
-XX:+UseG1GC
-XX:+ParallelRefProcEnabled
-XX:MaxGCPauseMillis=200
-XX:+UnlockExperimentalVMOptions
-XX:+DisableExplicitGC
-XX:G1NewSizePercent=30
-XX:G1MaxNewSizePercent=40
-XX:G1HeapRegionSize=8M
-XX:G1ReservePercent=20
-XX:G1HeapWastePercent=5
-XX:G1MixedGCCountTarget=4
-XX:InitiatingHeapOccupancyPercent=15
-XX:G1MixedGCLiveThresholdPercent=90
-XX:SurvivorRatio=32
-XX:+PerfDisableSharedMem
-XX:MaxTenuringThreshold=1
```

### 2.4 World structure
Create three worlds on first boot. Use Multiverse-Core or manual folder creation:

| World folder | Purpose |
|---|---|
| `hlavni_uzel` | Lobby world — hub, NPCs, mini-games |
| `oneblock_world` | OneBlock islands — void, per-player/team islands |
| `mesto_world` | Shared city district — market, guild hall, lore builds |

- `oneblock_world` must be a void world (no terrain generation)
- `hlavni_uzel` is a custom-built world (import from schematic later)
- `mesto_world` starts empty, expands as players progress

---

## 3. Third-Party Plugins

Install the following plugins into `/opt/nerith/plugins/` before building NerithCore.  
Download from the URLs listed — do not use outdated Spigot mirrors where noted.

| Plugin | Source | Purpose |
|---|---|---|
| **EssentialsX** | https://essentialsx.net | Core commands: /tpa /home /spawn /kit /msg |
| **LuckPerms** | https://luckperms.net | Permissions and rank groups |
| **Vault** | SpigotMC | Economy API bridge between plugins |
| **PlaceholderAPI** | https://hangar.papermc.io | Dynamic variable system for HUD, chat, scoreboards |
| **DiscordSRV** | https://discordsrv.com | Minecraft ↔ Discord chat bridge |
| **CoreProtect** | https://hangar.papermc.io | Block logging and grief rollback |
| **NoCheatPlus** | SpigotMC | Anti-cheat: movement, combat, speed |
| **FancyNPCs** | https://modrinth.com/plugin/fancynpcs | NPC entity rendering base for NerithNPC |
| **Multiverse-Core** | https://hangar.papermc.io | Multi-world management |

### 3.1 LuckPerms rank structure
Define the following groups in LuckPerms after installation:

```
hrac       → default rank, all players
vip        → donor tier 1
mvp        → donor tier 2
moderator  → staff
admin      → staff
owner      → highest
```

### 3.2 EssentialsX config notes
- Set default locale to `en`
- Disable EssentialsX economy — Vault will use NerithEconomy instead
- Set spawn to `hlavni_uzel` world spawn point

---

## 4. NerithCore Plugin Specification

**Plugin name:** `NerithCore`  
**Package:** `gg.nerith.core`  
**Folder:** `/plugins/NerithCore/`  
**Dependencies:** Vault, PlaceholderAPI, FancyNPCs (soft)  
**Database:** MySQL 8.x  
**Build tool:** Maven  
**Target API:** PaperMC 1.21.x

---

## 5. Architecture

NerithCore is the central plugin. It does NOT directly call other Nerith plugins.  
All cross-plugin communication is done via **custom Bukkit events** (API event system).

### 5.1 Custom events fired by NerithCore
Other plugins listen to these events — NerithCore never imports NerithEconomy, NerithNPC, etc.

| Event class | Fired when |
|---|---|
| `NerithPhaseUpEvent` | Island advances to next phase |
| `NerithBlockBreakEvent` | Player breaks the OneBlock (wraps BlockBreakEvent) |
| `NerithIslandResetEvent` | Island reset is confirmed |
| `NerithIslandCreateEvent` | New island is created |
| `NerithVoidDeathEvent` | Player dies in void |
| `NerithPowerupActivateEvent` | Player activates a void powerup |
| `NerithSpecialBlockEvent` | Treasure block or boss spawn triggers |
| `NerithMemberJoinEvent` | Player joins a coop island |
| `NerithMemberLeaveEvent` | Player leaves or is kicked from island |

All events must be cancellable where appropriate and carry relevant context (island ID, player UUID, phase, etc.).

---

## 6. Database Schema (MySQL)

### 6.1 Tables

```sql
CREATE TABLE islands (
  id              VARCHAR(36) PRIMARY KEY,   -- UUID
  owner_uuid      VARCHAR(36) NOT NULL,
  island_type     ENUM('solo','coop') NOT NULL DEFAULT 'solo',
  phase           INT NOT NULL DEFAULT 1,
  blocks_broken   BIGINT NOT NULL DEFAULT 0,
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  last_active     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  spawn_x         DOUBLE,
  spawn_y         DOUBLE,
  spawn_z         DOUBLE,
  world           VARCHAR(64) NOT NULL DEFAULT 'oneblock_world'
);

CREATE TABLE island_members (
  island_id       VARCHAR(36) NOT NULL,
  player_uuid     VARCHAR(36) NOT NULL,
  role            ENUM('owner','member') NOT NULL DEFAULT 'member',
  joined_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (island_id, player_uuid),
  FOREIGN KEY (island_id) REFERENCES islands(id) ON DELETE CASCADE
);

CREATE TABLE island_stats (
  island_id       VARCHAR(36) PRIMARY KEY,
  mobs_killed     BIGINT DEFAULT 0,
  bosses_killed   INT DEFAULT 0,
  events_triggered INT DEFAULT 0,
  treasure_found  INT DEFAULT 0,
  FOREIGN KEY (island_id) REFERENCES islands(id) ON DELETE CASCADE
);

CREATE TABLE island_resets (
  id              INT AUTO_INCREMENT PRIMARY KEY,
  island_id       VARCHAR(36) NOT NULL,
  player_uuid     VARCHAR(36) NOT NULL,
  phase_at_reset  INT NOT NULL,
  blocks_at_reset BIGINT NOT NULL,
  penalty_amount  DOUBLE NOT NULL,
  reset_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE island_permissions (
  island_id       VARCHAR(36) PRIMARY KEY,
  allow_break     BOOLEAN DEFAULT FALSE,
  allow_place     BOOLEAN DEFAULT FALSE,
  allow_interact  BOOLEAN DEFAULT FALSE,
  allow_chest     BOOLEAN DEFAULT FALSE,
  allow_kill_mobs BOOLEAN DEFAULT FALSE,
  FOREIGN KEY (island_id) REFERENCES islands(id) ON DELETE CASCADE
);

CREATE TABLE island_powerups (
  island_id       VARCHAR(36) NOT NULL,
  powerup_type    ENUM('void_shield','fragment_saver') NOT NULL,
  active          BOOLEAN DEFAULT TRUE,
  obtained_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (island_id, powerup_type),
  FOREIGN KEY (island_id) REFERENCES islands(id) ON DELETE CASCADE
);
```

---

## 7. OneBlock Mechanic

### 7.1 Core concept
- Each island has exactly **one regenerating block** at its center
- When broken, the block regenerates immediately as a new block from the current phase pool
- The block pool (list of possible blocks and weights) is defined per phase in `config.yml`
- Block break increments `blocks_broken` counter on the island
- When `blocks_broken` reaches the phase threshold, `NerithPhaseUpEvent` fires

### 7.2 Phase thresholds
Rastúci počet blokov per fáza — konfigurovateľné v `config.yml`:

| Phase | Name | Blocks required (cumulative) |
|---|---|---|
| 1 | The Void Fragment | 0 (start) |
| 2 | The Overgrowth | 500 |
| 3 | The Deep Roots | 1 500 |
| 4 | The Awakening | 3 500 |
| 5 | The Forge | 7 500 |
| 6 | The Ascent | 15 000 |
| 7 | The Rebirth | 30 000 |

All thresholds must be configurable in `config.yml` under `phases:`.

### 7.3 Phase-up sequence
When `blocks_broken` reaches the next phase threshold:

1. Freeze the OneBlock for 3 seconds (cannot be broken)
2. Play particle effects around the block (configurable particle type per phase)
3. Shake the block visually (use block display entity or armorstand trick)
4. Fire `NerithPhaseUpEvent`
5. Send title + subtitle to all island members:
   - Title: phase name (e.g. `The Overgrowth`)
   - Subtitle: short lore line (configurable in `config.yml`)
6. Send chat message to island members with lore text
7. Give phase-up reward kit (configurable per phase in `config.yml`)
8. Update island phase in database
9. Trigger NPC lore message (via `NerithPhaseUpEvent` — NerithNPC listens)

### 7.4 Special block events
Within each phase pool, certain entries are tagged as **special events** with a rarity weight:

| Event type | Behavior |
|---|---|
| `TREASURE_BLOCK` | Drops rare loot defined in config. Fires `NerithSpecialBlockEvent` |
| `BOSS_SPAWN` | Spawns a custom boss mob on the island. Fires `NerithSpecialBlockEvent` |
| `LORE_BLOCK` | Plays a lore message/sound, no drop. Fires `NerithSpecialBlockEvent` |

Special event rarities and loot tables are defined in `config.yml` per phase.

---

## 8. Island Types and Coop

### 8.1 Solo island
- One owner, no members
- Created with `/is create`

### 8.2 Coop island
- One owner, up to N members (default: 4, configurable: `island.max-members` in config)
- Created with `/is create coop`
- Invite system: owner sends invite → member accepts
- Request system: player requests to join → owner accepts
- Owner can kick members at any time
- Owner can transfer ownership with `/is transfer <player>`
- Members can leave with `/is leave`
- When owner resets — all members are notified and removed

---

## 9. Island Reset

### 9.1 Reset flow
1. Player runs `/is reset`
2. GUI opens with confirmation screen:
   - Shows current phase, blocks broken, penalty preview
   - Two buttons: **Confirm Reset** (red) / **Cancel** (green)
3. On confirm:
   - Calculate penalty: `penalty = fragments_balance * phase_penalty_percent[current_phase]`
   - Fire `NerithIslandResetEvent` (NerithEconomy listens and deducts fragments)
   - Delete island world data
   - Delete island from database (cascade deletes members, stats, permissions, powerups)
   - Insert record into `island_resets` table
   - Teleport all members to `hlavni_uzel` spawn
   - Create new fresh island for owner

### 9.2 Penalty percentages (configurable in config.yml)
| Phase at reset | Penalty % of Fragments balance |
|---|---|
| 1 | 0% |
| 2 | 5% |
| 3 | 10% |
| 4 | 20% |
| 5 | 35% |
| 6 | 50% |
| 7 | 75% |

---

## 10. Island Visitor Permissions

### 10.1 Default visitor behavior
Any player not in the island's coop team who enters the island boundary is a **visitor**.  
Visitors are **read-only by default** — they can observe but cannot interact.

### 10.2 Permissions GUI
Owner opens island settings GUI (`/is` → Settings → Visitor Permissions).  
Toggle permissions with click — green = allowed, red = denied:

| Permission | Default | Description |
|---|---|---|
| Break blocks | ❌ | Visitors can break blocks |
| Place blocks | ❌ | Visitors can place blocks |
| Interact | ❌ | Use buttons, levers, doors |
| Open chests | ❌ | Access storage |
| Kill mobs | ❌ | Attack mobs on island |

Settings stored in `island_permissions` table per island.

---

## 11. Island Spawn and Layout

### 11.1 Starting state
- World: `oneblock_world` (void world, no terrain)
- Island spawns as a single block (stone or custom starter block) floating in void
- Nothing else — no platform, no chest, no extra blocks
- OneBlock position is the island center: `(island_offset_x, 64, island_offset_z)`
- Islands are spaced 1000 blocks apart on a grid to prevent overlap

### 11.2 Fragment Islands
Scattered around each OneBlock island at random positions between **300–400 blocks** from center.  
Generated from pre-built **NBT schematics** (`.nbt` structure files placed in `/plugins/NerithCore/structures/`).

Rules:
- 3–6 fragment islands spawn per OneBlock island on creation (configurable)
- Positions are random within the 300–400 block radius ring
- Each fragment island is selected randomly from the structure pool
- Fragment islands contain rare items not obtainable from the OneBlock (special saplings, lore items, unique blocks)
- Fragment islands do **not** regenerate after reset — new ones spawn on new island creation
- Fragment islands are protected — visitors and members cannot grief them (hardcoded, no GUI toggle)

### 11.3 Starter kit
Given once on first island creation, never again.  
Configurable in `config.yml` under `starter-kit:`:

- Stone pickaxe
- 1x Torch
- 3x Cooked Beef

---

## 12. Void Death System

### 12.1 Default void death
- Player falls into void → instant death
- **All items are dropped and destroyed** (not dropped as entities — gone permanently)
- Experience is lost
- Player respawns at island spawn point
- `NerithVoidDeathEvent` fires

### 12.2 Void Powerups
Powerups are stored on the **island**, not in player inventory.  
Visible in island GUI under "Island Perks" section.

**Display states:**
- 🟢 Green glow — active, ready to trigger
- 🔴 Red / gray — used or not owned

| Powerup | ID | Effect | After use |
|---|---|---|---|
| Void Shield | `void_shield` | Physically launches player upward (like slime block) back onto island — no death, no item loss | Consumed, removed from GUI |
| Fragment Saver | `fragment_saver` | Void death occurs but only 30% of items are destroyed (70% saved) | Consumed, removed from GUI |

**Activation logic:**
- Powerups are passive — they trigger automatically on void fall detection
- Priority: `void_shield` activates first if both are present
- Both are obtained exclusively via NPC quests (not craftable, not in shop)
- Stored in `island_powerups` table

---

## 13. Commands

### 13.1 Player commands
Alias: `/island` or `/is`

| Command | Description |
|---|---|
| `/is` | Opens main island GUI |
| `/is create` | Creates new solo island |
| `/is create coop` | Creates new coop island |
| `/is home` | Teleports to own island |
| `/is invite <player>` | Invites player to coop island |
| `/is join <player>` | Requests to join player's island |
| `/is accept` | Accepts pending invite or request |
| `/is deny` | Denies pending invite or request |
| `/is kick <player>` | Kicks member from island (owner only) |
| `/is leave` | Leave coop island (non-owners only) |
| `/is transfer <player>` | Transfers island ownership |
| `/is reset` | Opens reset confirmation GUI |
| `/is info` | Shows island info: phase, blocks, members, stats |
| `/is top` | Opens server island leaderboard |
| `/is setspawn` | Sets island spawn point (owner only) |

### 13.2 Admin commands
Alias: `/isadmin` or `/isa`

| Command | Description |
|---|---|
| `/isa goto <player>` | Teleports admin to player's island |
| `/isa reset <player>` | Force reset island with no penalty |
| `/isa setphase <player> <phase>` | Manually set island phase |
| `/isa info <player>` | View full island data |
| `/isa reload` | Reload config without restart |

---

## 14. Metrics and Leaderboard

### 14.1 Tracked per island
All metrics stored in `islands` + `island_stats` tables:

- Current phase
- Total blocks broken
- Total mobs killed
- Total bosses killed
- Total events triggered
- Total treasure found
- Island creation date
- Last active timestamp
- Reset history (count + details)

### 14.2 Web API endpoint
NerithCore exposes a simple **read-only HTTP endpoint** on a configurable port (default: `8080`) for the website:

```
GET /api/leaderboard?type=phase&limit=10
GET /api/island/<uuid>
GET /api/stats/global
```

Returns JSON. This allows the Nerith website to display live leaderboards without direct DB access.

### 14.3 Weekly / monthly leaderboard rewards
- Leaderboard ranked by: phase first, then blocks broken as tiebreaker
- Top player at end of week/month receives a reward
- Reward is fired as a custom event `NerithLeaderboardRewardEvent` — NerithEconomy/LuckPerms listen
- Admin command `/isa leaderboard settle <weekly|monthly>` triggers manual settlement
- Automatic settlement via scheduled task (configurable day/time in config)

---

## 15. Configuration File Structure

`/plugins/NerithCore/config.yml` — main config  
`/plugins/NerithCore/messages.yml` — all player-facing strings (English default)  
`/plugins/NerithCore/messages_sk.yml` — Slovak strings (placeholder, fill later)  
`/plugins/NerithCore/structures/` — folder for `.nbt` fragment island schematics

### 15.1 config.yml outline
```yaml
database:
  host: localhost
  port: 3306
  name: nerith
  username: nerith_user
  password: CHANGE_ME
  pool-size: 10

island:
  max-members: 4
  spacing: 1000
  void-y-level: 0
  fragment-islands:
    min-count: 3
    max-count: 6
    min-radius: 300
    max-radius: 400

starter-kit:
  enabled: true
  items:
    - STONE_PICKAXE:1
    - TORCH:1
    - COOKED_BEEF:3

phases:
  1:
    name: "The Void Fragment"
    blocks-required: 0
    lore-line: "You have found the fragment. It is cold. It is alone."
    reward-kit: []
    phase-up-particle: SMOKE_NORMAL
  2:
    name: "The Overgrowth"
    blocks-required: 500
    lore-line: "Life finds a way. The fragment stirs."
    reward-kit:
      - OAK_SAPLING:4
    phase-up-particle: VILLAGER_HAPPY
  # ... phases 3-7

reset-penalties:
  1: 0
  2: 5
  3: 10
  4: 20
  5: 35
  6: 50
  7: 75

web-api:
  enabled: true
  port: 8080

leaderboard:
  weekly-settle-day: SUNDAY
  weekly-settle-time: "20:00"
  monthly-settle-day: 1
```

---

## 16. Plugin Structure (Maven)

```
NerithCore/
├── pom.xml
├── src/main/java/gg/nerith/core/
│   ├── NerithCore.java               ← Main plugin class
│   ├── api/
│   │   └── events/
│   │       ├── NerithPhaseUpEvent.java
│   │       ├── NerithBlockBreakEvent.java
│   │       ├── NerithIslandResetEvent.java
│   │       ├── NerithIslandCreateEvent.java
│   │       ├── NerithVoidDeathEvent.java
│   │       ├── NerithPowerupActivateEvent.java
│   │       ├── NerithSpecialBlockEvent.java
│   │       ├── NerithMemberJoinEvent.java
│   │       ├── NerithMemberLeaveEvent.java
│   │       └── NerithLeaderboardRewardEvent.java
│   ├── island/
│   │   ├── IslandManager.java         ← CRUD, caching, lifecycle
│   │   ├── IslandCreator.java         ← World gen, schematic placement
│   │   ├── IslandResetter.java        ← Reset flow, penalty calc
│   │   └── IslandPermissionManager.java
│   ├── oneblock/
│   │   ├── OneBlockListener.java      ← Listens to BlockBreakEvent
│   │   ├── PhaseManager.java          ← Phase logic, thresholds
│   │   ├── BlockPoolManager.java      ← Loads phase pools from config
│   │   ├── SpecialEventManager.java   ← Treasure, boss, lore events
│   │   └── PhaseUpAnimator.java       ← Cutscene, particles, title
│   ├── coop/
│   │   ├── CoopManager.java           ← Invite/request/kick/transfer
│   │   └── CoopListener.java
│   ├── void/
│   │   ├── VoidDeathListener.java
│   │   └── PowerupManager.java        ← Shield, saver logic
│   ├── commands/
│   │   ├── IslandCommand.java         ← /is handler + subcommands
│   │   └── IslandAdminCommand.java    ← /isa handler
│   ├── gui/
│   │   ├── MainIslandGUI.java
│   │   ├── ResetConfirmGUI.java
│   │   ├── VisitorPermissionsGUI.java
│   │   ├── IslandPerksGUI.java
│   │   └── LeaderboardGUI.java
│   ├── database/
│   │   ├── DatabaseManager.java       ← HikariCP connection pool
│   │   ├── IslandRepository.java
│   │   ├── MemberRepository.java
│   │   ├── StatsRepository.java
│   │   └── ResetRepository.java
│   ├── metrics/
│   │   ├── MetricsCollector.java
│   │   └── WebApiServer.java          ← Simple HTTP server (NanoHTTPD or Javalin)
│   ├── leaderboard/
│   │   └── LeaderboardManager.java
│   └── config/
│       └── ConfigManager.java
└── src/main/resources/
    ├── plugin.yml
    ├── config.yml
    ├── messages.yml
    └── messages_sk.yml
```

---

## 17. plugin.yml

```yaml
name: NerithCore
version: 1.0.0
main: gg.nerith.core.NerithCore
api-version: '1.21'
description: Core OneBlock engine for Nerith server
authors: [Nerith Team]
depend: [Vault, PlaceholderAPI]
softdepend: [FancyNPCs]
commands:
  island:
    description: Island management
    aliases: [is]
    usage: /is
  islandadmin:
    description: Admin island management
    aliases: [isa]
    usage: /isa
    permission: nerith.admin
permissions:
  nerith.admin:
    description: Access to /isa commands
    default: op
  nerith.island.create:
    description: Create an island
    default: true
  nerith.island.reset:
    description: Reset own island
    default: true
```

---

## 18. Build Notes for Claude Code

1. **Start with database layer** — `DatabaseManager.java` with HikariCP, schema auto-creation on first boot
2. **Then island CRUD** — `IslandManager.java` + repositories
3. **Then OneBlock listener** — `OneBlockListener.java` + `PhaseManager.java`
4. **Then commands** — `/is create` first, then full command tree
5. **Then GUIs** — main GUI last, after all logic is functional
6. **Web API last** — use NanoHTTPD (lightweight, no extra server needed)
7. **Do not hardcode any strings** — all player-facing text goes through `messages.yml`
8. **All config values must have defaults** — never crash on missing config key
9. **Log plugin startup** with version, DB connection status, phase count loaded
10. **Test world:** use `oneblock_world` as void world — ensure island spacing prevents overlap at 1000 block grid

---

*End of NERITHCORE.md — version 1.0*
*Next spec files: NERITHNPC.md, NERITHECONOMY.md, NERITHLOBBY.md, NERITHQUESTS.md, NERITHHUD.md*
