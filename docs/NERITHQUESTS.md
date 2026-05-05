# NERITHQUESTS.md
> Build specification for Claude Code — Nerith Quest & Advancement System  
> Read NERITHCORE.md and NERITHNPC.md first.  
> NerithQuests depends on NerithCore events and NerithNPC quest completion events.  
> Do not skip sections. Read fully before writing any code.

---

## 1. Overview

**Plugin name:** `NerithQuests`  
**Package:** `gg.nerith.quests`  
**Folder:** `/plugins/NerithQuests/`  
**Dependencies:** NerithCore, NerithNPC, PlaceholderAPI, Vault  
**Database:** MySQL 8.x (shared server, separate tables)  
**Build tool:** Maven  
**Target API:** PaperMC 1.21.x

NerithQuests manages three quest systems:
1. **Daily Quests** — 3–5 random quests per player per day, phase-weighted, resets at midnight
2. **Milestone Quests** — permanent progression achievements (blocks, phases, bosses, exploration)
3. **Advancement Tree** — custom Minecraft advancement tabs replacing all vanilla advancements, driven by NPC quests (from NerithNPC), daily quests, and milestones

NerithQuests does NOT manage NPC story quests directly — those are owned by NerithNPC.  
NerithQuests listens to `NerithQuestCompleteEvent` from NerithNPC and grants advancements accordingly.

---

## 2. Architecture

### 2.1 Responsibilities
- Generate and assign daily quests per player based on island phase
- Track daily quest progress and reset at midnight
- Define and track milestone quests
- Manage daily streak counter
- Generate and register custom Minecraft advancement trees programmatically
- Grant advancements on quest/milestone completion
- Replace all vanilla advancements with Nerith tabs on player join

### 2.2 Events consumed
| Event | Source | Action |
|---|---|---|
| `NerithQuestCompleteEvent` | NerithNPC | Grant corresponding advancement, check milestone triggers |
| `NerithPhaseUpEvent` | NerithCore | Check phase milestone, regenerate daily quest pool for player |
| `NerithBlockBreakEvent` | NerithCore | Update block-count milestones |
| `NerithSpecialBlockEvent` | NerithCore | Update boss-kill / treasure milestones |
| `NerithNPCLiberated` | NerithNPC | Update liberation milestone |

### 2.3 Events fired by NerithQuests
| Event | Fired when |
|---|---|
| `NerithDailyQuestCompleteEvent` | Player completes a daily quest — NerithEconomy listens |
| `NerithMilestoneCompleteEvent` | Player completes a milestone — NerithEconomy listens |
| `NerithStreakRewardEvent` | Player hits 7-day streak — NerithEconomy listens |

---

## 3. Database Schema (MySQL)

```sql
CREATE TABLE daily_quests (
  id              INT AUTO_INCREMENT PRIMARY KEY,
  player_uuid     VARCHAR(36) NOT NULL,
  quest_id        VARCHAR(64) NOT NULL,
  assigned_date   DATE NOT NULL,
  status          ENUM('active','completed','expired') DEFAULT 'active',
  progress        JSON,
  completed_at    TIMESTAMP
);

CREATE TABLE daily_streaks (
  player_uuid     VARCHAR(36) PRIMARY KEY,
  current_streak  INT DEFAULT 0,
  longest_streak  INT DEFAULT 0,
  last_completion DATE,
  streak_bonus_claimed_week DATE
);

CREATE TABLE milestone_progress (
  player_uuid     VARCHAR(36) NOT NULL,
  milestone_id    VARCHAR(64) NOT NULL,
  progress        BIGINT DEFAULT 0,
  completed       BOOLEAN DEFAULT FALSE,
  completed_at    TIMESTAMP,
  PRIMARY KEY (player_uuid, milestone_id)
);

CREATE TABLE advancement_grants (
  player_uuid     VARCHAR(36) NOT NULL,
  advancement_key VARCHAR(128) NOT NULL,
  granted_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (player_uuid, advancement_key)
);
```

---

## 4. Advancement Tree

### 4.1 Vanilla advancement suppression
On `PlayerJoinEvent` and `PlayerAdvancementDoneEvent`:
- Revoke all vanilla advancements silently (no toast, no chat message)
- Use `Player.revokeAdvancement()` for all namespaced keys under `minecraft:*`
- This must run on every join to catch any that were granted between sessions

### 4.2 Custom advancement tabs
Two custom tabs registered programmatically on plugin startup:

| Tab | Namespace | Icon | Title |
|---|---|---|---|
| Main Quests | `nerith:main` | NETHER_STAR | "Nerith — Quest Line" |
| Daily & Milestones | `nerith:progress` | COMPASS | "Nerith — Progress" |

### 4.3 Main Quests tab (`nerith:main`)
Mirrors the NPC story quest progression from NerithNPC.  
Structure: tree layout where each NPC quest is a node, unlocked sequentially per NPC.

**Root node** (always visible, granted on first join):
```
nerith:main/root
  title: "A Fragment in the Void"
  description: "You have arrived. The city waits."
  icon: NETHER_STAR
  frame: task
  position: x=0, y=0
```

**NPC quest nodes** auto-generated from NerithNPC YAML quest definitions:
```
nerith:main/guardian_aldric/quest_first_wood
  title: "The First Timber"
  description: "Bring 32 Oak Logs to Aldric."
  icon: OAK_LOG
  frame: task
  parent: nerith:main/root
  position: auto-calculated based on NPC tree layout
```

**Generation rule:**
- On plugin startup, NerithQuests reads all NPC YAML files via NerithNPC API
- For each NPC, creates a branch in the tree
- Quests within an NPC are chained — quest 2 requires quest 1 completion
- Branches arranged horizontally per NPC, root connects to first quest of each NPC

### 4.4 Daily & Milestones tab (`nerith:progress`)
Two sub-trees in one tab:

**Daily sub-tree root:**
```
nerith:progress/daily_root
  title: "Daily Tasks"
  description: "New tasks every day at midnight."
  icon: CLOCK
  frame: task
  position: x=0, y=0
```

Daily quest advancement nodes are **dynamically regenerated each day** — old ones revoked, new ones granted as player completes them.

**Milestone sub-tree root:**
```
nerith:progress/milestone_root
  title: "Milestones"
  description: "The marks of a true Builder."
  icon: BEACON
  frame: goal
  position: x=0, y=3
```

Milestone nodes are permanent — once granted, never revoked.

### 4.5 Advancement frames
| Frame type | Used for | Visual |
|---|---|---|
| `task` | Normal quests and daily quests | Standard square border |
| `goal` | Phase milestones | Rounded border |
| `challenge` | Rare milestones (all bosses, all fragments) | Gold spiky border |

### 4.6 Toast notifications
Vanilla advancement toast fires automatically when advancement is granted — no custom code needed.  
Do NOT send additional chat messages for quest completion — toast is the only notification for quest/milestone.  
Exception: streak reward — send a chat message in addition to toast.

---

## 5. Daily Quest System

### 5.1 Quest generation
On first login of the day (checked against `assigned_date`):
1. Determine player's current island phase via NerithCore API
2. Load daily quest pool for that phase from `daily_quests_pool.yml`
3. Select 3–5 quests randomly (configurable range per phase in config)
4. Insert into `daily_quests` table with `assigned_date = today`
5. Grant corresponding advancement nodes in `nerith:progress` tab (hidden until completed)

### 5.2 Quest pool YAML
`/plugins/NerithQuests/daily_quests_pool.yml`

```yaml
phase-1:
  min-daily: 3
  max-daily: 4
  quests:
    - id: daily_break_100
      title: "First Cracks"
      description: "Break 100 blocks from the OneBlock."
      type: block_break
      objective:
        count: 100
      rewards:
        fragments: 50
        xp: 20

    - id: daily_kill_5_mobs
      title: "Clear the Fragment"
      description: "Defeat 5 mobs on your island."
      type: kill
      objective:
        mob: ANY
        count: 5
      rewards:
        fragments: 75
        xp: 30

    - id: daily_craft_tools
      title: "Working Hands"
      description: "Craft any 3 tools."
      type: craft
      objective:
        category: TOOLS
        count: 3
      rewards:
        fragments: 60
        xp: 25

phase-2:
  min-daily: 3
  max-daily: 5
  quests:
    - id: daily_plant_sapling
      title: "Roots of Nerith"
      description: "Plant 5 saplings on your island."
      type: place
      objective:
        block: ANY_SAPLING
        count: 5
      rewards:
        fragments: 80
        xp: 35
    # ... more quests per phase
```

All phases (1–7) defined. Higher phases have harder objectives and higher rewards.

### 5.3 Progress tracking
Daily quest types and their tracking events:

| Type | Tracking event |
|---|---|
| `block_break` | `NerithBlockBreakEvent` |
| `kill` | `EntityDeathEvent` |
| `craft` | `CraftItemEvent` |
| `place` | `BlockPlaceEvent` |
| `fish` | `PlayerFishEvent` |
| `trade` | Custom — NerithEconomy fires event on shop purchase |
| `explore` | `PlayerMoveEvent` — reach target location |

Progress stored in `daily_quests.progress` JSON column.  
Actionbar update every 40 ticks showing active daily quest progress (shared with NerithNPC actionbar — NerithHUD manages display priority).

### 5.4 Completion
On daily quest completion:
1. Update `daily_quests.status` to `completed`
2. Fire `NerithDailyQuestCompleteEvent` with reward payload
3. Grant advancement for completed quest node in `nerith:progress` tab
4. Update streak — see Section 6
5. Check if all daily quests for today are complete → bonus reward if so

### 5.5 Midnight reset
Scheduled task runs at server midnight (configurable timezone in config):
1. Set all `active` daily quests with `assigned_date < today` to `expired`
2. Revoke daily advancement nodes from previous day
3. New quests generated on next player login
4. Players online at midnight receive chat notification:
   `§6[Nerith] §fNové denné questy sú pripravené. Prihlás sa znova pre nové úlohy.`

---

## 6. Daily Streak System

### 6.1 Streak rules
- Streak increments when player completes **at least 1 daily quest** on a given calendar day
- Streak breaks if player misses a full day (no completion)
- Streak tracked in `daily_streaks` table

### 6.2 Streak counter display
- Shown in NerithHUD scoreboard (NerithHUD reads via PlaceholderAPI placeholder `%nerithquests_streak%`)
- Format: `§6🔥 Streak: §f7 days`

### 6.3 7-day streak bonus
On day 7 (and every 7th consecutive day):
1. Fire `NerithStreakRewardEvent` with bonus payload
2. Send chat message to player:
   ```
   §6[Nerith] §f7-dňový streak! Získavaš bonus §6500 Fragments§f a §62x XP§f na nasledujúcu hodinu.
   ```
3. Grant special streak advancement node in `nerith:progress` tab
4. Record `streak_bonus_claimed_week` to prevent duplicate reward same week

### 6.4 Streak milestones
Permanent milestone advancements for streak achievements:

| Streak | Advancement | Frame |
|---|---|---|
| 7 days | "Week of the Builder" | goal |
| 30 days | "Devoted to the Fragment" | challenge |
| 100 days | "Pillar of Nerith" | challenge |

---

## 7. Milestone Quest System

### 7.1 Milestone definition YAML
`/plugins/NerithQuests/milestones.yml`

```yaml
milestones:

  # Block milestones
  blocks_1000:
    title: "First Thousand"
    description: "Break 1,000 blocks from the OneBlock."
    icon: STONE
    frame: task
    type: block_break
    target: 1000
    rewards:
      fragments: 200
      xp: 100

  blocks_5000:
    title: "The Grind Begins"
    description: "Break 5,000 blocks from the OneBlock."
    icon: COBBLESTONE
    frame: task
    parent: blocks_1000
    type: block_break
    target: 5000
    rewards:
      fragments: 500
      xp: 250

  blocks_10000:
    title: "Ten Thousand Cracks"
    description: "Break 10,000 blocks from the OneBlock."
    icon: DEEPSLATE
    frame: goal
    parent: blocks_5000
    type: block_break
    target: 10000
    rewards:
      fragments: 1000
      xp: 500
      items:
        - DIAMOND:3

  blocks_50000:
    title: "The Shaper of Nerith"
    description: "Break 50,000 blocks."
    icon: OBSIDIAN
    frame: challenge
    parent: blocks_10000
    type: block_break
    target: 50000
    rewards:
      fragments: 5000
      xp: 2000
      items:
        - DIAMOND_PICKAXE:1

  # Phase milestones
  phase_3:
    title: "The Awakening"
    description: "Reach Phase 3 — The Awakening."
    icon: ENDER_EYE
    frame: goal
    type: phase_reach
    target: 3
    rewards:
      fragments: 750
      xp: 300

  phase_7:
    title: "The Rebirth"
    description: "Reach Phase 7 — The Rebirth. Nerith lives again."
    icon: NETHER_STAR
    frame: challenge
    type: phase_reach
    target: 7
    rewards:
      fragments: 10000
      xp: 5000
      items:
        - TOTEM_OF_UNDYING:1

  # Boss milestones
  first_boss:
    title: "Blood of the Fragment"
    description: "Defeat your first boss on the OneBlock."
    icon: WITHER_SKELETON_SKULL
    frame: goal
    type: boss_kill
    target: 1
    rewards:
      fragments: 500
      xp: 200

  bosses_10:
    title: "The Hunter"
    description: "Defeat 10 bosses."
    icon: DRAGON_HEAD
    frame: challenge
    parent: first_boss
    type: boss_kill
    target: 10
    rewards:
      fragments: 2000
      xp: 1000
      items:
        - GOLDEN_APPLE:5

  # Exploration milestones
  first_fragment_island:
    title: "Beyond the Void"
    description: "Discover your first Fragment Island."
    icon: MAP
    frame: task
    type: fragment_island_visit
    target: 1
    rewards:
      fragments: 300
      xp: 150

  all_fragment_islands:
    title: "Cartographer of the Sky"
    description: "Discover all Fragment Islands around your OneBlock."
    icon: FILLED_MAP
    frame: challenge
    parent: first_fragment_island
    type: fragment_island_visit
    target: all
    rewards:
      fragments: 1500
      xp: 750
      items:
        - SPYGLASS:1

  # NPC milestones
  first_npc_quest:
    title: "A City Speaks"
    description: "Complete your first NPC quest."
    icon: VILLAGER_SPAWN_EGG
    frame: task
    type: npc_quest_complete
    target: 1
    rewards:
      fragments: 100
      xp: 50

  npc_quests_25:
    title: "Voice of the Survivors"
    description: "Complete 25 NPC quests."
    icon: BOOK
    frame: goal
    parent: first_npc_quest
    type: npc_quest_complete
    target: 25
    rewards:
      fragments: 2000
      xp: 1000
```

### 7.2 Milestone tracking
Progress tracked in `milestone_progress` table.  
On relevant events (block break, phase up, boss kill, etc.) — update progress for all matching active milestones for that player.  
On target reached → fire `NerithMilestoneCompleteEvent`, grant advancement, insert `completed = true`.

### 7.3 Milestone rewards
| Component | Handled by |
|---|---|
| Fragments | NerithEconomy listens to `NerithMilestoneCompleteEvent` |
| XP levels | NerithQuests grants directly via `Player.giveExp()` |
| Special items | NerithQuests gives directly to player inventory |
| Advancement toast | Fires automatically on advancement grant |

---

## 8. Commands

### 8.1 Player commands

| Command | Description |
|---|---|
| `/daily` | Shows today's daily quests with progress in chat |
| `/daily info <quest_id>` | Details on a specific daily quest |
| `/milestones` | Lists all milestones — completed and in progress |
| `/streak` | Shows current streak, longest streak, next bonus |

### 8.2 Admin commands
Alias: `/questa`

| Command | Description |
|---|---|
| `/questa reload` | Reloads all YAML pool and milestone files |
| `/questa daily regenerate <player>` | Force regenerates daily quests for player |
| `/questa milestone complete <player> <id>` | Force completes a milestone |
| `/questa milestone reset <player> <id>` | Resets milestone progress |
| `/questa streak set <player> <days>` | Sets streak manually |
| `/questa advancement grant <player> <key>` | Manually grants an advancement |
| `/questa advancement revoke <player> <key>` | Manually revokes an advancement |

---

## 9. Configuration

`/plugins/NerithQuests/config.yml`
```yaml
database:
  host: localhost
  port: 3306
  name: nerith
  username: nerith_user
  password: CHANGE_ME

daily:
  reset-time: "00:00"
  timezone: "Europe/Bratislava"
  all-complete-bonus-fragments: 100

streak:
  bonus-interval-days: 7
  bonus-fragments: 500
  bonus-xp-multiplier-minutes: 60

advancement:
  suppress-vanilla: true
  main-tab-title: "Nerith — Quest Line"
  progress-tab-title: "Nerith — Progress"

placeholders:
  streak: "%nerithquests_streak%"
  daily-completed: "%nerithquests_daily_done%"
  daily-total: "%nerithquests_daily_total%"
  milestones-completed: "%nerithquests_milestones%"
```

`/plugins/NerithQuests/messages.yml` — system messages  
`/plugins/NerithQuests/messages_sk.yml` — Slovak placeholder  
`/plugins/NerithQuests/daily_quests_pool.yml` — daily quest definitions per phase  
`/plugins/NerithQuests/milestones.yml` — milestone definitions

---

## 10. Plugin Structure (Maven)

```
NerithQuests/
├── pom.xml
└── src/main/java/gg/nerith/quests/
    ├── NerithQuests.java                     ← Main plugin class
    ├── api/
    │   └── events/
    │       ├── NerithDailyQuestCompleteEvent.java
    │       ├── NerithMilestoneCompleteEvent.java
    │       └── NerithStreakRewardEvent.java
    ├── advancement/
    │   ├── AdvancementManager.java           ← Register, grant, revoke advancements
    │   ├── AdvancementTreeBuilder.java       ← Builds nerith:main from NerithNPC YAML
    │   ├── VanillaSuppressor.java            ← Revokes all minecraft:* advancements
    │   └── AdvancementKey.java               ← Utility for namespaced keys
    ├── daily/
    │   ├── DailyQuestManager.java            ← Generate, assign, track daily quests
    │   ├── DailyQuestPool.java               ← Loads pool YAML per phase
    │   ├── DailyQuestTracker.java            ← Listens to game events for progress
    │   ├── DailyQuestResetTask.java          ← Scheduled midnight reset
    │   └── DailyQuestDefinition.java         ← POJO
    ├── milestone/
    │   ├── MilestoneManager.java             ← Load, track, complete milestones
    │   ├── MilestoneTracker.java             ← Listens to NerithCore + game events
    │   ├── MilestoneRewardDispatcher.java    ← Items, XP, fires event
    │   └── MilestoneDefinition.java          ← POJO
    ├── streak/
    │   └── StreakManager.java                ← Track streak, fire bonus event
    ├── placeholders/
    │   └── NerithQuestsPlaceholders.java     ← PlaceholderAPI expansion
    ├── commands/
    │   ├── DailyCommand.java                 ← /daily
    │   ├── MilestonesCommand.java            ← /milestones
    │   ├── StreakCommand.java                ← /streak
    │   └── QuestAdminCommand.java            ← /questa
    ├── database/
    │   ├── DailyQuestRepository.java
    │   ├── StreakRepository.java
    │   ├── MilestoneRepository.java
    │   └── AdvancementRepository.java
    └── config/
        └── ConfigManager.java
```

---

## 11. plugin.yml

```yaml
name: NerithQuests
version: 1.0.0
main: gg.nerith.quests.NerithQuests
api-version: '1.21'
description: Daily quests, milestones, and advancement tree for Nerith
authors: [Nerith Team]
depend: [NerithCore, NerithNPC, PlaceholderAPI, Vault]
commands:
  daily:
    description: View daily quests
    usage: /daily
  milestones:
    description: View milestone progress
    usage: /milestones
  streak:
    description: View your daily streak
    usage: /streak
  questa:
    description: Quest system admin tools
    usage: /questa
    permission: nerith.quests.admin
permissions:
  nerith.quests.admin:
    description: Access to /questa commands
    default: op
```

---

## 12. Build Notes for Claude Code

1. **Start with VanillaSuppressor** — must work on first join before anything else
2. **Then AdvancementManager** — registration and granting infrastructure
3. **Then MilestoneManager** — load YAML, track progress, simpler than daily
4. **Then DailyQuestPool + DailyQuestManager** — generation logic
5. **Then DailyQuestTracker** — event listeners for progress
6. **Then DailyQuestResetTask** — scheduled task, test with short interval first
7. **Then StreakManager** — depends on DailyQuestManager completion events
8. **Then AdvancementTreeBuilder** — reads NerithNPC YAML, builds nerith:main tree
9. **Then commands and placeholders** — last
10. **Advancement API note** — use Paper's `Advancement` and `AdvancementDisplay` API, not raw JSON datapacks. Paper 1.21 supports programmatic advancement registration via `Bukkit.getUnsafe().loadAdvancement()`
11. **Test vanilla suppression** — verify no minecraft:* advancements appear in tab after join
12. **Timezone** — all midnight resets use configured timezone, default `Europe/Bratislava`
13. **Never hardcode quest IDs** — all content loaded from YAML, hot-reloadable via `/questa reload`

---

*End of NERITHQUESTS.md — version 1.0*  
*Next: NERITHECONOMY.md, NERITHLOBBY.md, NERITHHUD.md*
