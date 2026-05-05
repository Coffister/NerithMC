# NERITHGUI.md
> Build specification for Claude Code — Nerith Central GUI Engine  
> Read NERITHCORE.md first. All other Nerith* plugins call NerithGUI API instead of managing inventories themselves.  
> Do not skip sections. Read fully before writing any code.

---

## 1. Overview

**Plugin name:** `NerithGUI`  
**Package:** `gg.nerith.gui`  
**Folder:** `/plugins/NerithGUI/`  
**Dependencies:** HeadDatabase, PlaceholderAPI  
**Database:** MySQL 8.x (shared server, separate tables)  
**Build tool:** Maven  
**Target API:** PaperMC 1.21.x

NerithGUI is the centralized inventory GUI engine for the entire Nerith server.  
Every other Nerith* plugin opens GUIs through the NerithGUI API — no plugin manages its own inventory windows.  
All GUI layouts are stored in MySQL and editable live in-game via `/guieditor` without server restart.

---

## 2. Architecture

### 2.1 Responsibilities
- Store all GUI definitions in MySQL (layout, slots, actions, items)
- Render GUIs for players on demand with PlaceholderAPI variable substitution
- Provide a live in-game editor (`/guieditor`) for modifying GUIs without restart
- Expose a static Java API (`NerithGUI.open()`, `registerAction()`, `registerProvider()`) for other plugins
- Integrate HeadDatabase for player-head items in GUI slots
- Broadcast immediate GUI refresh to all online players when a layout is saved

### 2.2 Events fired by NerithGUI
| Event | Fired when |
|---|---|
| `NerithGuiOpenEvent` | Player opens any NerithGUI inventory |
| `NerithGuiActionEvent` | Player triggers a slot action — cancellable |
| `NerithGuiSavedEvent` | Admin saves a GUI layout via editor |

---

## 3. Database Schema (MySQL)

```sql
CREATE TABLE nerith_guis (
  gui_id      VARCHAR(64) PRIMARY KEY,
  title       VARCHAR(64) NOT NULL,        -- window title in Slovak
  rows        TINYINT NOT NULL DEFAULT 6,  -- 1-6 rows (9-54 slots)
  updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE nerith_gui_slots (
  gui_id       VARCHAR(64) NOT NULL,
  slot_index   TINYINT UNSIGNED NOT NULL,  -- 0-53
  type         ENUM('empty','decoration','action','info','tab') DEFAULT 'empty',
  material     VARCHAR(64) DEFAULT 'AIR',
  hdb_id       VARCHAR(32) DEFAULT NULL,   -- HeadDatabase ID, overrides material
  display_name VARCHAR(128) DEFAULT NULL,
  lore         JSON DEFAULT NULL,          -- array of strings, supports %placeholders%
  action       VARCHAR(128) DEFAULT NULL,  -- e.g. "island:home", "tab:island_stats", "close"
  glow         BOOLEAN DEFAULT FALSE,
  PRIMARY KEY (gui_id, slot_index),
  FOREIGN KEY (gui_id) REFERENCES nerith_guis(gui_id) ON DELETE CASCADE
);
```

---

## 4. Java API

All other Nerith* plugins interact with NerithGUI only through the static API class `NerithGUIAPI`.  
No direct database access from other plugins.

```java
// Open a registered GUI for a player
NerithGUIAPI.open(Player player, String guiId);

// Register a dynamic data provider — called every time the GUI is rendered for a player
// Returns a map of placeholder → value pairs
NerithGUIAPI.registerProvider(String guiId, Function<Player, Map<String, String>> provider);

// Register an action handler — called when player clicks a slot with that action string
NerithGUIAPI.registerAction(String actionId, Consumer<Player> handler);

// Refresh all open instances of a GUI (called after editor save)
NerithGUIAPI.refreshAll(String guiId);

// Check if a GUI exists in the registry
NerithGUIAPI.exists(String guiId);
```

### 4.1 Action naming convention

Actions are formatted as `namespace:action_name`:

| Prefix | Plugin | Example |
|---|---|---|
| `island:` | NerithCore | `island:home`, `island:reset`, `island:setspawn` |
| `tab:` | NerithGUI internal | `tab:island_stats` — switches to another GUI |
| `npc:` | NerithNPC | `npc:open_dialogue` |
| `quest:` | NerithQuests | `quest:daily_list` |
| `close` | NerithGUI internal | Closes inventory |
| `back` | NerithGUI internal | Returns to previous GUI in session stack |

### 4.2 Provider placeholder format

Placeholders in `display_name` and `lore` use `%key%` format:

```java
NerithGUIAPI.registerProvider("island_main", player -> {
    Island island = NerithCore.getIslandManager().getIslandByPlayer(player.getUniqueId()).orElse(null);
    Map<String, String> data = new HashMap<>();
    data.put("%phase%",     island != null ? String.valueOf(island.getPhase()) : "—");
    data.put("%broken%",    island != null ? String.valueOf(island.getBlocksBroken()) : "0");
    data.put("%events%",    island != null ? getEventCount(island) : "0");
    data.put("%treasures%", island != null ? getTreasureCount(island) : "0");
    data.put("%members%",   island != null ? getMemberCount(island) : "0");
    return data;
});
```

Standard PlaceholderAPI placeholders (`%player_name%`, etc.) are also resolved automatically.

---

## 5. Live In-Game Editor

### 5.1 Opening the editor
```
/guieditor <gui_id>
Permission: nerith.gui.editor
```

Opens the same inventory as the live GUI but in **edit mode** — a parallel copy that does not trigger player-facing actions.

### 5.2 Editor behavior

**Per slot:**
- Hovering shows tooltip: `§7[Klikni pre úpravu]`
- **Left-click** → opens slot editor sub-GUI (see 5.3)
- **Right-click** → instantly clears the slot (sets type to `empty`, material to `AIR`)

**Fixed editor controls (always present, override slot actions):**
| Slot | Item | Action |
|---|---|---|
| 45 | RED_WOOL — "✖ Zrušiť" | Close editor without saving |
| 49 | LIME_WOOL — "✔ Uložiť" | Write to MySQL, call `refreshAll()`, close editor |
| 53 | COMPASS — "⚙ Info" | Shows gui_id, row count, last updated |

### 5.3 Slot editor sub-GUI (27 slots)

When admin left-clicks a slot, a 27-slot sub-GUI opens:

| Slot | Item | Function |
|---|---|---|
| 10 | GRASS_BLOCK — "Zmeniť item" | Opens material picker (type item name in chat) |
| 11 | PLAYER_HEAD — "HDB hlava" | Opens HDB search: type search term in chat, shows results |
| 12 | NAME_TAG — "Zmeniť názov" | Next chat message sets display_name |
| 13 | BOOK — "Zmeniť lore" | Opens multi-line lore editor |
| 14 | REDSTONE — "Zmeniť akciu" | Next chat message sets action string |
| 15 | BLAZE_POWDER — "Glow: ON/OFF" | Toggles enchantment glow on the item |
| 16 | BARRIER — "Vymazať slot" | Clears slot |
| 22 | ARROW — "« Späť" | Returns to main editor without saving slot |

All chat input uses a `ChatInputSession` — plugin cancels the next chat message from that player, reads it as input, returns to editor.

---

## 6. GUI Definitions

### 6.1 Tab bar structure (row 0, slots 0–8)

All island GUIs share the same tab bar in row 0.  
Active tab uses a different material or HDB head to indicate current screen.  
Inactive tabs navigate to their respective GUI on click.

| Slot | Tab | GUI ID | Label |
|---|---|---|---|
| 0 | Ostrov | `island_main` | `🏝 Ostrov` |
| 1 | Tím | `island_team` | `👥 Tím` |
| 2 | Nastavenia | `island_settings` | `⚙️ Nastavenia` |
| 3 | Perky | `island_perks` | `✨ Perky` |
| 4 | Štatistiky | `island_stats` | `📊 Štatistiky` |
| 5 | Rebríček | `island_leaderboard` | `🏆 Rebríček` |
| 6–8 | — | — | BLACK_STAINED_GLASS_PANE decoration |

### 6.2 Bottom bar structure (row 5, slots 45–53)

All island GUIs share the same bottom bar.

| Slot | Label | Action | HDB key |
|---|---|---|---|
| 46 | `🏠 Domov` | `island:home` | `home` |
| 52 | `✖ Zatvoriť` | `close` | `close` |
| 45, 47–51, 53 | — | — | decoration |

### 6.3 island_main — Záložka Ostrov

**Rows 1–4 inner layout:**

| Slot | Type | Content | Action / HDB |
|---|---|---|---|
| 10 | decoration | GRAY_STAINED_GLASS_PANE | — |
| 19 | info | HDB `phase` — `Fáza ostrova` / lore: `["%phase%"]` | — |
| 21 | info | HDB `chest` — `Eventy` / lore: `["%events%"]` | — |
| 23 | info | HDB `key` — `Poklady` / lore: `["%treasures%"]` | — |
| 38 | action | HDB `pin` — `Nastaviť spawn` | `island:setspawn` |
| 42 | action | HDB `reset` red — `Resetovať ostrov` | `island:reset` |
| all others | decoration | BLACK_STAINED_GLASS_PANE | — |

### 6.4 island_stats — Záložka Štatistiky

**Rows 1–4 inner layout:**

| Slot | Type | Content |
|---|---|---|
| 10 | info | HDB `cube` — `Rozbitých blokov` / lore: `["%broken%"]` |
| 12 | info | HDB `sword` — `Zabití mobi` / lore: `["%mobs_killed%"]` |
| 14 | info | HDB `skull` — `Zabití bossovia` / lore: `["%bosses_killed%"]` |
| 16 | info | HDB `clock` — `Čas online` / lore: `["%online_time%"]` |
| 28 | info | HDB `calendar` — `Dátum vytvorenia` / lore: `["%created_at%"]` |
| 30 | info | HDB `flag` — `Typ ostrova` / lore: `["%island_type%"]` |
| 32 | info | HDB `reset` — `Počet resetov` / lore: `["%reset_count%"]` |
| 34 | info | HDB `trophy` — `Pozícia v rebríčku` / lore: `["%rank%"]` |
| all others | decoration | BLACK_STAINED_GLASS_PANE | — |

### 6.5 island_team, island_settings, island_perks, island_leaderboard

All four GUIs share tab bar (row 0) and bottom bar (row 5).  
Rows 1–4 are placeholder decoration at launch — content filled in future sprints.

---

## 7. Commands

| Command | Permission | Description |
|---|---|---|
| `/guieditor <gui_id>` | `nerith.gui.editor` | Open live editor for a GUI |
| `/guia list` | `nerith.gui.admin` | List all registered GUI IDs |
| `/guia reload <gui_id>` | `nerith.gui.admin` | Force reload GUI from DB |
| `/guia reloadall` | `nerith.gui.admin` | Reload all GUIs from DB |
| `/guia info <gui_id>` | `nerith.gui.admin` | Print GUI definition summary |
| `/guia export <gui_id>` | `nerith.gui.admin` | Export GUI as SQL INSERT to console |

---

## 8. Configuration

`/plugins/NerithGUI/config.yml`
```yaml
database:
  host: localhost
  port: 3306
  name: nerith
  username: nerith_user
  password: CHANGE_ME

editor:
  chat-input-timeout-seconds: 30   # how long to wait for chat input before cancelling

cache:
  gui-ttl-seconds: 300             # how long to cache GUI definitions in memory
  refresh-on-save: true            # refreshAll() on every editor save
```

---

## 9. Plugin Structure (Maven)

```
NerithGUI/
├── pom.xml
└── src/main/java/gg/nerith/gui/
    ├── NerithGUIPlugin.java              ← Main plugin class
    ├── api/
    │   ├── NerithGUIAPI.java             ← Static API (open, register, refresh)
    │   └── events/
    │       ├── NerithGuiOpenEvent.java
    │       ├── NerithGuiActionEvent.java
    │       └── NerithGuiSavedEvent.java
    ├── engine/
    │   ├── GuiRegistry.java              ← In-memory cache of loaded GUIs
    │   ├── GuiRenderer.java              ← Builds Bukkit Inventory from GuiDefinition
    │   ├── GuiSession.java               ← Per-player open GUI + navigation stack
    │   ├── GuiClickHandler.java          ← Bukkit InventoryClickEvent router
    │   ├── ActionDispatcher.java         ← Routes action strings to registered handlers
    │   └── PlaceholderResolver.java      ← Resolves %key% and PAPI placeholders
    ├── editor/
    │   ├── GuiEditor.java                ← Editor mode inventory + controls
    │   ├── SlotEditorGUI.java            ← 27-slot slot editor sub-GUI
    │   ├── ChatInputSession.java         ← Captures next player chat message as input
    │   └── HdbSearchHelper.java          ← HeadDatabase search integration
    ├── model/
    │   ├── GuiDefinition.java            ← POJO: gui_id, title, rows, List<SlotDefinition>
    │   └── SlotDefinition.java           ← POJO: index, type, material, hdb_id, name, lore, action, glow
    ├── database/
    │   ├── GuiRepository.java            ← CRUD for nerith_guis + nerith_gui_slots
    │   └── DatabaseManager.java          ← HikariCP connection pool
    ├── commands/
    │   ├── GuiEditorCommand.java         ← /guieditor
    │   └── GuiAdminCommand.java          ← /guia
    └── config/
        └── ConfigManager.java
```

---

## 10. plugin.yml

```yaml
name: NerithGUI
version: 1.0.0
main: gg.nerith.gui.NerithGUIPlugin
api-version: '1.21'
description: Centralized GUI engine for Nerith — database-driven, live-editable
authors: [Nerith Team]
softdepend: [HeadDatabase, PlaceholderAPI]
commands:
  guieditor:
    description: Open the live GUI editor
    usage: /guieditor <gui_id>
    permission: nerith.gui.editor
  guia:
    description: GUI admin tools
    usage: /guia
    permission: nerith.gui.admin
permissions:
  nerith.gui.editor:
    description: Access to /guieditor
    default: op
  nerith.gui.admin:
    description: Access to /guia commands
    default: op
```

---

## 11. Build Notes for Claude Code

1. **Start with DB layer** — `GuiRepository.java`, schema auto-creation, HikariCP pool
2. **Then model classes** — `GuiDefinition`, `SlotDefinition` (pure POJOs, no Bukkit)
3. **Then GuiRegistry** — in-memory cache with TTL, loads on startup
4. **Then GuiRenderer** — converts `GuiDefinition` → Bukkit `Inventory`, resolves placeholders via `PlaceholderResolver`
5. **Then GuiClickHandler** — `InventoryClickEvent`, reads `GuiSession`, routes to `ActionDispatcher`
6. **Then ActionDispatcher** — `registerAction()` map, built-in handlers for `tab:`, `close`, `back`
7. **Then NerithGUIAPI** — static facade, registers with Bukkit ServicesManager so other plugins can retrieve it
8. **Then GuiEditor** — parallel inventory, fixed control slots, delegates to SlotEditorGUI
9. **Then ChatInputSession** — cancels next player chat event, passes string to callback
10. **Then commands** — `/guieditor`, `/guia`
11. **Seed DB on first run** — if `nerith_guis` table is empty, load from `/plugins/NerithGUI/seed.sql`
12. **HeadDatabase** — softdepend; if not present, fall back to material. Never hard-fail on missing HDB.
13. **Never store GUI state in files** — DB is single source of truth. Config.yml is plugin config only.
14. **Thread safety** — `GuiRegistry` cache uses `ConcurrentHashMap`. All DB writes on async thread, all Bukkit calls on main thread.

---

*End of NERITHGUI.md — version 1.0*  
*Next: NERITHECONOMY.md, NERITHLOBBY.md, NERITHHUD.md*
