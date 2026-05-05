# NERITHNPC.md
> Build specification for Claude Code — Nerith NPC Dialogue & Quest Engine  
> Read NERITHCORE.md first. NerithNPC depends on NerithCore API events.  
> Do not skip sections. Read fully before writing any code.

---

## 1. Overview

**Plugin name:** `NerithNPC`  
**Package:** `gg.nerith.npc`  
**Folder:** `/plugins/NerithNPC/`  
**Dependencies:** NerithCore, FancyNPCs, PlaceholderAPI  
**Database:** MySQL 8.x (shared server, separate tables)  
**Build tool:** Maven  
**Target API:** PaperMC 1.21.x

NerithNPC is the dialogue, quest, and NPC lifecycle engine for Nerith.  
It hooks into FancyNPCs for entity rendering and movement, and listens to NerithCore events for dynamic dialogue triggers.  
NPC characters, their dialogues, quests, and patrol paths are all defined in YAML — no hardcoding of content.

---

## 2. Architecture

### 2.1 Responsibilities
- Register and manage NPC instances via FancyNPCs API
- Handle player → NPC interaction (proximity detection, click handling)
- Render chat dialogue progressively with clickable options
- Manage quest assignment, tracking, and completion
- Handle NPC liberation cutscene and teleportation to mesto_world
- Fire and listen to NerithCore API events where relevant

### 2.2 Events consumed from NerithCore
| Event | What NerithNPC does |
|---|---|
| `NerithPhaseUpEvent` | Re-evaluates available dialogues and quests for all online island members |
| `NerithVoidDeathEvent` | No action (reserved for future NPC reactions) |
| `NerithIslandCreateEvent` | No action at launch |

### 2.3 Events fired by NerithNPC
| Event | Fired when |
|---|---|
| `NerithQuestCompleteEvent` | Player completes a quest — NerithEconomy listens for Fragments reward |
| `NerithNPCLiberated` | NPC liberation cutscene completes |
| `NerithLoreUnlockEvent` | Lore entry is revealed to player via NPC dialogue |

---

## 3. Database Schema (MySQL)

```sql
CREATE TABLE npc_definitions (
  npc_id          VARCHAR(64) PRIMARY KEY,   -- matches YAML key e.g. "guardian_aldric"
  display_name    VARCHAR(64) NOT NULL,
  npc_type        VARCHAR(32) NOT NULL,       -- merchant, guardian, chronicler, healer, etc. (set later)
  world           VARCHAR(64) NOT NULL,
  location_x      DOUBLE NOT NULL,
  location_y      DOUBLE NOT NULL,
  location_z      DOUBLE NOT NULL,
  location_yaw    FLOAT DEFAULT 0,
  fancynpc_uuid   VARCHAR(36),               -- FancyNPCs entity UUID after spawn
  patrol_enabled  BOOLEAN DEFAULT FALSE,
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE player_npc_state (
  player_uuid     VARCHAR(36) NOT NULL,
  npc_id          VARCHAR(64) NOT NULL,
  visited         BOOLEAN DEFAULT FALSE,
  visit_count     INT DEFAULT 0,
  last_visited    TIMESTAMP,
  last_dialogue   VARCHAR(64),               -- last dialogue node shown
  cooldown_until  TIMESTAMP,
  PRIMARY KEY (player_uuid, npc_id)
);

CREATE TABLE player_quests (
  id              INT AUTO_INCREMENT PRIMARY KEY,
  player_uuid     VARCHAR(36) NOT NULL,
  npc_id          VARCHAR(64) NOT NULL,
  quest_id        VARCHAR(64) NOT NULL,
  status          ENUM('active','completed','failed') DEFAULT 'active',
  progress        JSON,                      -- flexible per quest type
  assigned_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  completed_at    TIMESTAMP
);

CREATE TABLE player_lore (
  player_uuid     VARCHAR(36) NOT NULL,
  lore_id         VARCHAR(64) NOT NULL,
  unlocked_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (player_uuid, lore_id)
);
```

---

## 4. NPC Definition (YAML)

All NPC content is defined in `/plugins/NerithNPC/npcs/`.  
Each NPC has its own YAML file: `guardian_aldric.yml`, `blacksmith_vorn.yml`, etc.

### 4.1 NPC YAML structure
```yaml
# /plugins/NerithNPC/npcs/guardian_aldric.yml

id: guardian_aldric
display-name: "§6Aldric §7— The Guardian"
skin: guardian_aldric          # skin name from /plugins/NerithNPC/skins/
world: mesto_world
location:
  x: 120.5
  y: 64.0
  z: 88.5
  yaw: 180.0

patrol:
  enabled: true
  points:
    - {x: 120.5, y: 64.0, z: 88.5}
    - {x: 125.5, y: 64.0, z: 88.5}
    - {x: 125.5, y: 64.0, z: 93.5}
  speed: 0.15
  pause-ticks: 40              # pause at each point

proximity:
  radius: 6                    # blocks — when NPC starts talking
  cooldown-ticks: 200          # per-NPC proximity cooldown

interaction:
  cooldown-seconds: 30         # per-NPC click cooldown

personality:
  tone: wise-and-weary         # used by dialogue writers as style guide
  backstory-summary: "Old guardian of Nerith who survived the Shattering. Carries guilt."

dialogues:
  # First ever visit
  first-visit:
    condition: visited == false
    lines:
      - "§7Aldric turns slowly, his eyes scanning you from head to toe."
      - "§f\"Another soul... I did not think the fragments would carry anyone else here.\""
      - "§f\"My name is Aldric. I have watched this city crumble for longer than I care to admit.\""
      - "§f\"If you mean to stay — and I suspect you do — then listen carefully.\""
    options:
      - label: "§aTell me about Nerith"
        next: lore_nerith_intro
      - label: "§aDo you have work for me?"
        next: quest_intro
      - label: "§7Farewell"
        next: farewell

  # Repeated visit — changes by phase
  repeat-visit-phase-1:
    condition: visited == true AND island_phase == 1
    lines:
      - "§f\"Still on that small fragment, I see. Keep breaking. Keep building.\""
      - "§f\"The city was not rebuilt in a day.\""
    options:
      - label: "§aDo you have work for me?"
        next: quest_intro
      - label: "§7Farewell"
        next: farewell

  repeat-visit-phase-3:
    condition: visited == true AND island_phase >= 3
    lines:
      - "§f\"You have come far. I can see it in your hands — the dust of the deep earth.\""
      - "§f\"The old foundations are waking. Be careful what you uncover down there.\""
    options:
      - label: "§aWhat do you mean?"
        next: lore_deep_roots
      - label: "§aDo you have work for me?"
        next: quest_intro
      - label: "§7Farewell"
        next: farewell

  lore_nerith_intro:
    lines:
      - "§f\"Nerith was not always like this. Once, it was whole — earth, sky, stone.\""
      - "§f\"Then the Shattering came. No one agrees on what caused it.\""
      - "§f\"What remains are the fragments. And us.\""
    lore-unlock: lore_nerith_origin
    options:
      - label: "§aWhat is the Shattering?"
        next: lore_shattering
      - label: "§7I understand. Thank you."
        next: farewell

  farewell:
    lines:
      - "§f\"Watch your step out there. The void does not forgive.\""
    close: true

quests:
  # Always available from phase 1
  quest_first_wood:
    unlock-condition: always
    title: "The First Timber"
    description: "Aldric needs wood to repair the old watchtower gate."
    type: fetch
    objective:
      item: OAK_LOG
      amount: 32
    rewards:
      fragments: 150
      items:
        - STONE_SWORD:1
      lore-unlock: lore_watchtower
    dialogue-on-assign: "quest_first_wood_assign"
    dialogue-on-complete: "quest_first_wood_complete"

  quest_kill_shadows:
    unlock-condition: island_phase >= 2
    title: "Shadows in the Overgrowth"
    description: "Strange creatures have been spotted near the city edge. Aldric wants them gone."
    type: kill
    objective:
      mob: PHANTOM
      amount: 10
    rewards:
      fragments: 300
      items:
        - GOLDEN_APPLE:2
    dialogue-on-assign: "quest_kill_shadows_assign"
    dialogue-on-complete: "quest_kill_shadows_complete"

  quest_explore_fragment:
    unlock-condition: island_phase >= 3
    title: "The Old Signal"
    description: "Aldric believes a fragment island 300 blocks north holds something important. Go and return."
    type: explore
    objective:
      location:
        world: oneblock_world
        radius: 50           # player must enter this radius around the target coords
        x: 0                 # filled per-player dynamically based on island center
        z: -350
    rewards:
      fragments: 500
      lore-unlock: lore_signal_fragment
    dialogue-on-assign: "quest_explore_fragment_assign"
    dialogue-on-complete: "quest_explore_fragment_complete"
```

---

## 5. Dialogue Engine

### 5.1 Proximity detection
- On player move, check if any NPC is within `proximity.radius` blocks
- If player enters radius and cooldown has passed:
  - NPC faces the player (FancyNPCs look-at API)
  - NPC sends a proximity line to the player in chat (first line of current dialogue node)
  - Set proximity cooldown

### 5.2 Click interaction
Player right-clicks NPC → start dialogue session.

**Dialogue session flow:**
1. Check per-NPC cooldown — if active, send cooldown message and stop
2. Evaluate which dialogue node to show based on conditions:
   - `visited == false` → `first-visit`
   - Match `island_phase` condition → phase-specific dialogue
   - Default → generic repeat dialogue
3. Begin rendering dialogue lines one by one with configurable delay (default: 40 ticks between lines)
4. After all lines rendered, show clickable options in chat:
   ```
   §8▶ §aTell me about Nerith
   §8▶ §aDo you have work for me?
   §8▶ §7Farewell
   ```
5. Player clicks option → navigate to next dialogue node
6. If `close: true` on node → session ends, cooldown starts
7. If `lore-unlock` on node → fire `NerithLoreUnlockEvent`, insert into `player_lore` table

### 5.3 Dialogue rendering rules
- All lines sent to player's chat only (not broadcast)
- Lines prefixed with NPC name: `§6[Aldric] §f"..."`
- Narration lines (non-speech) use italic gray: `§7§o*Aldric turns slowly...*`
- Distinguish speech from narration by YAML prefix:
  - `"§7` prefix → narration (italic gray)
  - `"§f` prefix → speech (white, quoted)
- Clickable options rendered using `ClickEvent.Action.RUN_COMMAND` pointing to internal command `/npcdialogue <session_id> <option_index>`
- Session ID ties the click to the active dialogue session — prevents stale clicks
- Sessions expire after 60 seconds of inactivity

### 5.4 Condition evaluation
Conditions are evaluated as simple expressions against player state:

| Variable | Source |
|---|---|
| `visited` | `player_npc_state.visited` |
| `visit_count` | `player_npc_state.visit_count` |
| `island_phase` | NerithCore `IslandManager.getPhase(playerUUID)` |
| `quest_complete:<quest_id>` | `player_quests` table |
| `lore_unlocked:<lore_id>` | `player_lore` table |

Conditions support: `==`, `>=`, `<=`, `>`, `<`, `AND`, `OR`, `!`  
Evaluated top-to-bottom — first matching dialogue node wins.

---

## 6. Quest Engine

### 6.1 Quest types

| Type | Objective | Completion trigger |
|---|---|---|
| `fetch` | Deliver X of item Y to NPC | Player clicks NPC with required items in inventory |
| `kill` | Kill X of mob type Y | Listen to `EntityDeathEvent`, check active quests |
| `explore` | Reach a location within radius | Listen to `PlayerMoveEvent`, check proximity to target |
| `liberate` | Find and interact with a locked NPC | Special liberation flow (see Section 7) |
| `condition` | Custom condition check | Evaluated on NPC click |

### 6.2 Quest flow
1. Player clicks quest option in dialogue → `quest_intro` node shown
2. Quest details displayed: title, description, objective, rewards preview
3. Player clicks `§a[Accept quest]` → quest inserted into `player_quests` with status `active`
4. Progress tracked in `progress` JSON column — structure varies by type:
   - fetch: `{"collected": 0, "required": 32}`
   - kill: `{"killed": 0, "required": 10}`
   - explore: `{"reached": false}`
5. On progress update → send actionbar notification: `§6Quest: §fShadows in the Overgrowth §7— §e7§7/§e10`
6. On completion → fire `NerithQuestCompleteEvent` with reward payload
7. NerithEconomy listens → grants Fragments
8. Item rewards given directly from NerithNPC
9. Lore unlock inserted if defined
10. NPC dialogue-on-complete shown on next interaction

### 6.3 Quest limits
- Player can have max 5 active quests simultaneously (configurable)
- One quest per NPC at a time — must complete or abandon before new one
- No daily reset — quests are progression-based, not repeatable (repeatable quests deferred to NerithQuests plugin)

### 6.4 Quest abandon
Player can abandon active quest via `/quest abandon <quest_id>` — no penalty, no cooldown.

---

## 7. NPC Liberation System

Some NPCs in future phases will be found "locked" — frozen, unable to speak.  
At launch all NPCs start in mesto_world and are active. Liberation mechanic is built now for future use.

### 7.1 Locked NPC state
- NPC entity exists in world but has `locked: true` in YAML
- On player click → single line: `§7§o*This figure is frozen, as if bound by something ancient.*`
- No dialogue, no quests available until liberated

### 7.2 Liberation trigger
Liberation is triggered when:
- Player completes a `liberate` type quest assigned by another NPC
- OR admin runs `/npca liberate <npc_id> <player_uuid>`

### 7.3 Liberation cutscene sequence
1. Freeze player in place for duration of cutscene (apply slow + blindness briefly)
2. Play particle ring around NPC (configurable particle type)
3. Play sound: `ENTITY_ELDER_GUARDIAN_CURSE` fading into `BLOCK_ENCHANTMENT_TABLE_USE`
4. Send lines to player chat:
   ```
   §7§o*The air around the figure shimmers...*
   §7§o*The frost cracks. The silence breaks.*
   ```
5. NPC sends first-liberation dialogue (defined in YAML as `liberation-dialogue`)
6. After dialogue — teleport NPC to `mesto_world` at defined `liberation-destination` coords
7. Update `npc_definitions` table: world + location to mesto_world coords
8. Fire `NerithNPCLiberated` event
9. Unfreeze player

### 7.4 Liberation YAML fields
```yaml
locked: false                        # true = locked at spawn
liberation-destination:
  world: mesto_world
  x: 145.5
  y: 64.0
  z: 102.5
liberation-dialogue:
  lines:
    - "§7§o*She blinks slowly, as if waking from a long dream.*"
    - "§f\"I... where am I? How long has it been?\""
    - "§f\"You freed me. I won't forget that.\""
  options:
    - label: "§aWhat happened to you?"
      next: liberation_backstory
    - label: "§7Rest for now."
      next: farewell
```

---

## 8. NPC Movement (Patrol)

FancyNPCs handles entity movement. NerithNPC defines patrol paths in YAML.

- If `patrol.enabled: true` → on plugin load, register patrol path with FancyNPCs
- NPC walks between `patrol.points` in order, loops back to first point
- Pauses at each point for `pause-ticks` ticks
- Speed controlled by `patrol.speed`
- When player clicks NPC mid-patrol → NPC stops, faces player, dialogue starts
- After dialogue session ends → NPC resumes patrol

---

## 9. Commands

### 9.1 Player commands

| Command | Description |
|---|---|
| `/quest` | Opens active quest list in chat |
| `/quest info <quest_id>` | Shows quest details and current progress |
| `/quest abandon <quest_id>` | Abandons active quest |
| `/lore` | Lists all unlocked lore entries |
| `/lore read <lore_id>` | Displays full lore entry in chat |

### 9.2 Admin commands
Alias: `/npca`

| Command | Description |
|---|---|
| `/npca spawn <npc_id>` | Spawns NPC from YAML definition |
| `/npca remove <npc_id>` | Removes NPC entity from world |
| `/npca reload` | Reloads all NPC YAML files |
| `/npca goto <npc_id>` | Teleports admin to NPC location |
| `/npca setloc <npc_id>` | Sets NPC location to admin's current position |
| `/npca liberate <npc_id> <player>` | Force-triggers liberation cutscene |
| `/npca questgive <player> <npc_id> <quest_id>` | Force-assigns quest to player |
| `/npca questcomplete <player> <quest_id>` | Force-completes quest |
| `/npca info <npc_id>` | Prints NPC definition and state |

---

## 10. Configuration

`/plugins/NerithNPC/config.yml`
```yaml
database:
  host: localhost
  port: 3306
  name: nerith
  username: nerith_user
  password: CHANGE_ME

dialogue:
  line-delay-ticks: 40          # delay between dialogue lines
  session-timeout-seconds: 60   # how long before open session expires
  proximity-message: true       # NPC speaks on player approach

quests:
  max-active-per-player: 5
  actionbar-update-ticks: 40    # how often to refresh actionbar progress

liberation:
  freeze-player: true
  cutscene-duration-ticks: 100

npc-defaults:
  proximity-radius: 6
  interaction-cooldown-seconds: 30
```

`/plugins/NerithNPC/messages.yml` — all player-facing strings  
`/plugins/NerithNPC/messages_sk.yml` — Slovak placeholder  
`/plugins/NerithNPC/npcs/` — one YAML file per NPC  
`/plugins/NerithNPC/lore/` — lore entry YAML files  
`/plugins/NerithNPC/skins/` — skin definition files for FancyNPCs

---

## 11. Lore Entry YAML

```yaml
# /plugins/NerithNPC/lore/lore_nerith_origin.yml
id: lore_nerith_origin
title: "The Origin of Nerith"
entries:
  - "Before the Shattering, Nerith was a city of stone and sky."
  - "Its founders discovered a force they called the Aether — a magic older than the land."
  - "They built upward, always upward, believing the sky held answers."
  - "They were right. And that was their undoing."
```

---

## 12. Plugin Structure (Maven)

```
NerithNPC/
├── pom.xml
└── src/main/java/gg/nerith/npc/
    ├── NerithNPC.java                    ← Main plugin class
    ├── api/
    │   └── events/
    │       ├── NerithQuestCompleteEvent.java
    │       ├── NerithNPCLiberated.java
    │       └── NerithLoreUnlockEvent.java
    ├── npc/
    │   ├── NPCManager.java               ← Load, spawn, track all NPCs
    │   ├── NPCDefinition.java            ← POJO for NPC YAML data
    │   ├── NPCLoader.java                ← Parses YAML files from /npcs/
    │   ├── NPCListener.java              ← FancyNPCs click/interact events
    │   └── PatrolManager.java            ← Patrol path registration
    ├── dialogue/
    │   ├── DialogueEngine.java           ← Core dialogue rendering
    │   ├── DialogueSession.java          ← Per-player active session
    │   ├── DialogueNode.java             ← Single dialogue node POJO
    │   ├── ConditionEvaluator.java       ← Evaluates YAML conditions
    │   └── ProximityListener.java        ← Player move → proximity check
    ├── quest/
    │   ├── QuestManager.java             ← Assign, track, complete quests
    │   ├── QuestDefinition.java          ← Quest POJO
    │   ├── QuestProgressTracker.java     ← kill/fetch/explore listeners
    │   └── QuestRewardDispatcher.java    ← Fires NerithQuestCompleteEvent
    ├── liberation/
    │   └── LiberationHandler.java        ← Cutscene, teleport, event
    ├── lore/
    │   ├── LoreManager.java              ← Load and serve lore entries
    │   └── LoreEntry.java               ← Lore POJO
    ├── commands/
    │   ├── QuestCommand.java             ← /quest
    │   ├── LoreCommand.java              ← /lore
    │   └── NPCAdminCommand.java          ← /npca
    ├── database/
    │   ├── NPCRepository.java
    │   ├── PlayerNPCStateRepository.java
    │   ├── QuestRepository.java
    │   └── LoreRepository.java
    └── config/
        └── ConfigManager.java
```

---

## 13. plugin.yml

```yaml
name: NerithNPC
version: 1.0.0
main: gg.nerith.npc.NerithNPC
api-version: '1.21'
description: NPC dialogue, quest, and liberation engine for Nerith
authors: [Nerith Team]
depend: [NerithCore, FancyNPCs, PlaceholderAPI]
commands:
  quest:
    description: Quest management
    usage: /quest
  lore:
    description: View unlocked lore
    usage: /lore
  npcadmin:
    description: NPC admin tools
    aliases: [npca]
    usage: /npca
    permission: nerith.npc.admin
permissions:
  nerith.npc.admin:
    description: Access to /npca commands
    default: op
```

---

## 14. Build Notes for Claude Code

1. **Start with YAML loader** — `NPCLoader.java` + `NPCDefinition.java`, parse all YAML files on startup
2. **Then database layer** — repositories, schema auto-creation
3. **Then dialogue engine** — `DialogueEngine.java` + `DialogueSession.java` + `ConditionEvaluator.java`
4. **Then proximity listener** — `ProximityListener.java`, throttle with cooldown map
5. **Then quest engine** — `QuestProgressTracker.java` listens to kill/move/interact events
6. **Then liberation handler** — `LiberationHandler.java`, cutscene sequence
7. **Then commands** — `/quest`, `/lore`, `/npca`
8. **FancyNPCs integration** — use FancyNPCs API for spawn, look-at, and patrol. Do not manually spawn entities.
9. **Session management** — DialogueSessions are per-player, stored in memory Map, cleaned up on expire or server shutdown
10. **Never broadcast** dialogue lines — send only to the interacting player
11. **All content strings in messages.yml** — system messages only, NPC dialogue stays in NPC YAML files
12. **Log on startup:** NPC count loaded, quest count loaded, DB connection status

---

## 15. Launch NPC Roster (Placeholder)

At launch, 10–15 NPC YAML files will be created.  
Names, roles, personalities, and dialogue content are defined separately in the **Nerith Lore Document** (not yet written).  
Plugin must support any number of NPC YAML files dropped into `/npcs/` folder and loaded automatically on startup or `/npca reload`.

---

*End of NERITHNPC.md — version 1.0*  
*Next: NERITHECONOMY.md, NERITHLOBBY.md, NERITHQUESTS.md, NERITHHUD.md*
