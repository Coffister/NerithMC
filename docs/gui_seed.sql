-- NerithGUI — Island GUI seed data
-- Version: 1.0  |  2026-05-05
-- Run ONCE on a fresh nerith DB after the tables are created by NerithGUI on startup.
-- All text is in Slovak as required by the spec.

-- ============================================================
-- 1. GUI headers
-- ============================================================
INSERT INTO nerith_guis (gui_id, title, rows) VALUES
  ('island_main',        '🏝 Ostrov',      6),
  ('island_team',        '👥 Tím',          6),
  ('island_settings',    '⚙️ Nastavenia',   6),
  ('island_perks',       '✨ Perky',         6),
  ('island_stats',       '📊 Štatistiky',   6),
  ('island_leaderboard', '🏆 Rebríček',      6)
ON DUPLICATE KEY UPDATE title = VALUES(title), rows = VALUES(rows);

-- ============================================================
-- 2. Shared tab bar  — row 0, slots 0–8  (all 6 GUIs)
-- ============================================================
-- Each GUI has its OWN copy of the tab bar so the active tab
-- can use a different material / HDB head from the inactive ones.

-- Helper macro (SQL doesn't have macros, so tab rows are explicit per GUI)

-- ── island_main tab bar ──────────────────────────────────────
INSERT INTO nerith_gui_slots (gui_id, slot_index, type, material, hdb_id, display_name, lore, action, glow) VALUES
  ('island_main', 0, 'tab',        'AIR', 'island_active', '§e§l🏝 Ostrov',       '["§7Aktuálna záložka"]',              NULL,                  TRUE),
  ('island_main', 1, 'tab',        'AIR', 'team',          '§f👥 Tím',             '["§7Otvor záložku Tím"]',             'tab:island_team',     FALSE),
  ('island_main', 2, 'tab',        'AIR', 'settings',      '§f⚙️ Nastavenia',     '["§7Otvor záložku Nastavenia"]',      'tab:island_settings', FALSE),
  ('island_main', 3, 'tab',        'AIR', 'perks',         '§f✨ Perky',           '["§7Otvor záložku Perky"]',           'tab:island_perks',    FALSE),
  ('island_main', 4, 'tab',        'AIR', 'stats',         '§f📊 Štatistiky',     '["§7Otvor záložku Štatistiky"]',      'tab:island_stats',    FALSE),
  ('island_main', 5, 'tab',        'AIR', 'leaderboard',   '§f🏆 Rebríček',        '["§7Otvor záložku Rebríček"]',        'tab:island_leaderboard', FALSE),
  ('island_main', 6, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_main', 7, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_main', 8, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE)
ON DUPLICATE KEY UPDATE type=VALUES(type), material=VALUES(material), hdb_id=VALUES(hdb_id),
  display_name=VALUES(display_name), lore=VALUES(lore), action=VALUES(action), glow=VALUES(glow);

-- ── island_team tab bar ──────────────────────────────────────
INSERT INTO nerith_gui_slots (gui_id, slot_index, type, material, hdb_id, display_name, lore, action, glow) VALUES
  ('island_team', 0, 'tab',        'AIR', 'island',        '§f🏝 Ostrov',         '["§7Otvor záložku Ostrov"]',          'tab:island_main',     FALSE),
  ('island_team', 1, 'tab',        'AIR', 'team_active',   '§e§l👥 Tím',           '["§7Aktuálna záložka"]',              NULL,                  TRUE),
  ('island_team', 2, 'tab',        'AIR', 'settings',      '§f⚙️ Nastavenia',     '["§7Otvor záložku Nastavenia"]',      'tab:island_settings', FALSE),
  ('island_team', 3, 'tab',        'AIR', 'perks',         '§f✨ Perky',           '["§7Otvor záložku Perky"]',           'tab:island_perks',    FALSE),
  ('island_team', 4, 'tab',        'AIR', 'stats',         '§f📊 Štatistiky',     '["§7Otvor záložku Štatistiky"]',      'tab:island_stats',    FALSE),
  ('island_team', 5, 'tab',        'AIR', 'leaderboard',   '§f🏆 Rebríček',        '["§7Otvor záložku Rebríček"]',        'tab:island_leaderboard', FALSE),
  ('island_team', 6, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_team', 7, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_team', 8, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE)
ON DUPLICATE KEY UPDATE type=VALUES(type), material=VALUES(material), hdb_id=VALUES(hdb_id),
  display_name=VALUES(display_name), lore=VALUES(lore), action=VALUES(action), glow=VALUES(glow);

-- ── island_settings tab bar ──────────────────────────────────
INSERT INTO nerith_gui_slots (gui_id, slot_index, type, material, hdb_id, display_name, lore, action, glow) VALUES
  ('island_settings', 0, 'tab',        'AIR', 'island',           '§f🏝 Ostrov',         '["§7Otvor záložku Ostrov"]',     'tab:island_main',        FALSE),
  ('island_settings', 1, 'tab',        'AIR', 'team',             '§f👥 Tím',             '["§7Otvor záložku Tím"]',        'tab:island_team',        FALSE),
  ('island_settings', 2, 'tab',        'AIR', 'settings_active',  '§e§l⚙️ Nastavenia',   '["§7Aktuálna záložka"]',         NULL,                     TRUE),
  ('island_settings', 3, 'tab',        'AIR', 'perks',            '§f✨ Perky',           '["§7Otvor záložku Perky"]',      'tab:island_perks',       FALSE),
  ('island_settings', 4, 'tab',        'AIR', 'stats',            '§f📊 Štatistiky',     '["§7Otvor záložku Štatistiky"]', 'tab:island_stats',       FALSE),
  ('island_settings', 5, 'tab',        'AIR', 'leaderboard',      '§f🏆 Rebríček',        '["§7Otvor záložku Rebríček"]',   'tab:island_leaderboard', FALSE),
  ('island_settings', 6, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_settings', 7, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_settings', 8, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE)
ON DUPLICATE KEY UPDATE type=VALUES(type), material=VALUES(material), hdb_id=VALUES(hdb_id),
  display_name=VALUES(display_name), lore=VALUES(lore), action=VALUES(action), glow=VALUES(glow);

-- ── island_perks tab bar ─────────────────────────────────────
INSERT INTO nerith_gui_slots (gui_id, slot_index, type, material, hdb_id, display_name, lore, action, glow) VALUES
  ('island_perks', 0, 'tab',        'AIR', 'island',        '§f🏝 Ostrov',         '["§7Otvor záložku Ostrov"]',     'tab:island_main',        FALSE),
  ('island_perks', 1, 'tab',        'AIR', 'team',          '§f👥 Tím',             '["§7Otvor záložku Tím"]',        'tab:island_team',        FALSE),
  ('island_perks', 2, 'tab',        'AIR', 'settings',      '§f⚙️ Nastavenia',     '["§7Otvor záložku Nastavenia"]', 'tab:island_settings',    FALSE),
  ('island_perks', 3, 'tab',        'AIR', 'perks_active',  '§e§l✨ Perky',         '["§7Aktuálna záložka"]',         NULL,                     TRUE),
  ('island_perks', 4, 'tab',        'AIR', 'stats',         '§f📊 Štatistiky',     '["§7Otvor záložku Štatistiky"]', 'tab:island_stats',       FALSE),
  ('island_perks', 5, 'tab',        'AIR', 'leaderboard',   '§f🏆 Rebríček',        '["§7Otvor záložku Rebríček"]',   'tab:island_leaderboard', FALSE),
  ('island_perks', 6, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_perks', 7, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_perks', 8, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE)
ON DUPLICATE KEY UPDATE type=VALUES(type), material=VALUES(material), hdb_id=VALUES(hdb_id),
  display_name=VALUES(display_name), lore=VALUES(lore), action=VALUES(action), glow=VALUES(glow);

-- ── island_stats tab bar ─────────────────────────────────────
INSERT INTO nerith_gui_slots (gui_id, slot_index, type, material, hdb_id, display_name, lore, action, glow) VALUES
  ('island_stats', 0, 'tab',        'AIR', 'island',        '§f🏝 Ostrov',         '["§7Otvor záložku Ostrov"]',     'tab:island_main',        FALSE),
  ('island_stats', 1, 'tab',        'AIR', 'team',          '§f👥 Tím',             '["§7Otvor záložku Tím"]',        'tab:island_team',        FALSE),
  ('island_stats', 2, 'tab',        'AIR', 'settings',      '§f⚙️ Nastavenia',     '["§7Otvor záložku Nastavenia"]', 'tab:island_settings',    FALSE),
  ('island_stats', 3, 'tab',        'AIR', 'perks',         '§f✨ Perky',           '["§7Otvor záložku Perky"]',      'tab:island_perks',       FALSE),
  ('island_stats', 4, 'tab',        'AIR', 'stats_active',  '§e§l📊 Štatistiky',   '["§7Aktuálna záložka"]',         NULL,                     TRUE),
  ('island_stats', 5, 'tab',        'AIR', 'leaderboard',   '§f🏆 Rebríček',        '["§7Otvor záložku Rebríček"]',   'tab:island_leaderboard', FALSE),
  ('island_stats', 6, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_stats', 7, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_stats', 8, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE)
ON DUPLICATE KEY UPDATE type=VALUES(type), material=VALUES(material), hdb_id=VALUES(hdb_id),
  display_name=VALUES(display_name), lore=VALUES(lore), action=VALUES(action), glow=VALUES(glow);

-- ── island_leaderboard tab bar ───────────────────────────────
INSERT INTO nerith_gui_slots (gui_id, slot_index, type, material, hdb_id, display_name, lore, action, glow) VALUES
  ('island_leaderboard', 0, 'tab',        'AIR', 'island',             '§f🏝 Ostrov',         '["§7Otvor záložku Ostrov"]',     'tab:island_main',     FALSE),
  ('island_leaderboard', 1, 'tab',        'AIR', 'team',               '§f👥 Tím',             '["§7Otvor záložku Tím"]',        'tab:island_team',     FALSE),
  ('island_leaderboard', 2, 'tab',        'AIR', 'settings',           '§f⚙️ Nastavenia',     '["§7Otvor záložku Nastavenia"]', 'tab:island_settings', FALSE),
  ('island_leaderboard', 3, 'tab',        'AIR', 'perks',              '§f✨ Perky',           '["§7Otvor záložku Perky"]',      'tab:island_perks',    FALSE),
  ('island_leaderboard', 4, 'tab',        'AIR', 'stats',              '§f📊 Štatistiky',     '["§7Otvor záložku Štatistiky"]', 'tab:island_stats',    FALSE),
  ('island_leaderboard', 5, 'tab',        'AIR', 'leaderboard_active', '§e§l🏆 Rebríček',      '["§7Aktuálna záložka"]',         NULL,                  TRUE),
  ('island_leaderboard', 6, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_leaderboard', 7, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_leaderboard', 8, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE)
ON DUPLICATE KEY UPDATE type=VALUES(type), material=VALUES(material), hdb_id=VALUES(hdb_id),
  display_name=VALUES(display_name), lore=VALUES(lore), action=VALUES(action), glow=VALUES(glow);

-- ============================================================
-- 3. Shared bottom bar — row 5, slots 45–53  (all 6 GUIs)
-- ============================================================
-- Slots 45, 47–51, 53 → decoration (BLACK_STAINED_GLASS_PANE)
-- Slot 46 → Home action
-- Slot 52 → Close action

-- Generate bottom bar for every GUI with a single multi-row INSERT per GUI

-- island_main bottom bar
INSERT INTO nerith_gui_slots (gui_id, slot_index, type, material, hdb_id, display_name, lore, action, glow) VALUES
  ('island_main', 45, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_main', 46, 'action',     'AIR', 'home', '§a🏠 Domov',  '["§7Teleportuj sa na ostrov"]',  'island:home', FALSE),
  ('island_main', 47, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_main', 48, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_main', 49, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_main', 50, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_main', 51, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_main', 52, 'action',     'AIR', 'close', '§c✖ Zatvoriť', '["§7Zatvor toto menu"]',        'close',       FALSE),
  ('island_main', 53, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE)
ON DUPLICATE KEY UPDATE type=VALUES(type), material=VALUES(material), hdb_id=VALUES(hdb_id),
  display_name=VALUES(display_name), lore=VALUES(lore), action=VALUES(action), glow=VALUES(glow);

-- island_team bottom bar
INSERT INTO nerith_gui_slots (gui_id, slot_index, type, material, hdb_id, display_name, lore, action, glow) VALUES
  ('island_team', 45, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_team', 46, 'action',     'AIR', 'home',  '§a🏠 Domov',   '["§7Teleportuj sa na ostrov"]', 'island:home', FALSE),
  ('island_team', 47, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_team', 48, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_team', 49, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_team', 50, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_team', 51, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_team', 52, 'action',     'AIR', 'close', '§c✖ Zatvoriť', '["§7Zatvor toto menu"]',        'close',       FALSE),
  ('island_team', 53, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE)
ON DUPLICATE KEY UPDATE type=VALUES(type), material=VALUES(material), hdb_id=VALUES(hdb_id),
  display_name=VALUES(display_name), lore=VALUES(lore), action=VALUES(action), glow=VALUES(glow);

-- island_settings bottom bar
INSERT INTO nerith_gui_slots (gui_id, slot_index, type, material, hdb_id, display_name, lore, action, glow) VALUES
  ('island_settings', 45, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_settings', 46, 'action',     'AIR', 'home',  '§a🏠 Domov',   '["§7Teleportuj sa na ostrov"]', 'island:home', FALSE),
  ('island_settings', 47, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_settings', 48, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_settings', 49, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_settings', 50, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_settings', 51, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_settings', 52, 'action',     'AIR', 'close', '§c✖ Zatvoriť', '["§7Zatvor toto menu"]',        'close',       FALSE),
  ('island_settings', 53, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE)
ON DUPLICATE KEY UPDATE type=VALUES(type), material=VALUES(material), hdb_id=VALUES(hdb_id),
  display_name=VALUES(display_name), lore=VALUES(lore), action=VALUES(action), glow=VALUES(glow);

-- island_perks bottom bar
INSERT INTO nerith_gui_slots (gui_id, slot_index, type, material, hdb_id, display_name, lore, action, glow) VALUES
  ('island_perks', 45, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_perks', 46, 'action',     'AIR', 'home',  '§a🏠 Domov',   '["§7Teleportuj sa na ostrov"]', 'island:home', FALSE),
  ('island_perks', 47, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_perks', 48, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_perks', 49, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_perks', 50, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_perks', 51, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_perks', 52, 'action',     'AIR', 'close', '§c✖ Zatvoriť', '["§7Zatvor toto menu"]',        'close',       FALSE),
  ('island_perks', 53, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE)
ON DUPLICATE KEY UPDATE type=VALUES(type), material=VALUES(material), hdb_id=VALUES(hdb_id),
  display_name=VALUES(display_name), lore=VALUES(lore), action=VALUES(action), glow=VALUES(glow);

-- island_stats bottom bar
INSERT INTO nerith_gui_slots (gui_id, slot_index, type, material, hdb_id, display_name, lore, action, glow) VALUES
  ('island_stats', 45, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_stats', 46, 'action',     'AIR', 'home',  '§a🏠 Domov',   '["§7Teleportuj sa na ostrov"]', 'island:home', FALSE),
  ('island_stats', 47, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_stats', 48, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_stats', 49, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_stats', 50, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_stats', 51, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_stats', 52, 'action',     'AIR', 'close', '§c✖ Zatvoriť', '["§7Zatvor toto menu"]',        'close',       FALSE),
  ('island_stats', 53, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE)
ON DUPLICATE KEY UPDATE type=VALUES(type), material=VALUES(material), hdb_id=VALUES(hdb_id),
  display_name=VALUES(display_name), lore=VALUES(lore), action=VALUES(action), glow=VALUES(glow);

-- island_leaderboard bottom bar
INSERT INTO nerith_gui_slots (gui_id, slot_index, type, material, hdb_id, display_name, lore, action, glow) VALUES
  ('island_leaderboard', 45, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_leaderboard', 46, 'action',     'AIR', 'home',  '§a🏠 Domov',   '["§7Teleportuj sa na ostrov"]', 'island:home', FALSE),
  ('island_leaderboard', 47, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_leaderboard', 48, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_leaderboard', 49, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_leaderboard', 50, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_leaderboard', 51, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_leaderboard', 52, 'action',     'AIR', 'close', '§c✖ Zatvoriť', '["§7Zatvor toto menu"]',        'close',       FALSE),
  ('island_leaderboard', 53, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE)
ON DUPLICATE KEY UPDATE type=VALUES(type), material=VALUES(material), hdb_id=VALUES(hdb_id),
  display_name=VALUES(display_name), lore=VALUES(lore), action=VALUES(action), glow=VALUES(glow);

-- ============================================================
-- 4. island_main — inner content  (rows 1–4, slots 9–44)
-- ============================================================
-- Slot 10: decoration gray glass
-- Slots 19, 21, 23: info items with dynamic placeholders
-- Slots 38, 42: action items
-- All others: BLACK_STAINED_GLASS_PANE decoration

INSERT INTO nerith_gui_slots (gui_id, slot_index, type, material, hdb_id, display_name, lore, action, glow) VALUES
  -- row 1
  ('island_main',  9, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_main', 10, 'decoration', 'GRAY_STAINED_GLASS_PANE',  NULL, '§r', NULL, NULL, FALSE),
  ('island_main', 11, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_main', 12, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_main', 13, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_main', 14, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_main', 15, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_main', 16, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_main', 17, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  -- row 2
  ('island_main', 18, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_main', 19, 'info', 'AIR', 'phase', '§e§lFáza ostrova',
      '["§7Aktuálna fáza: §f%phase%"]', NULL, FALSE),
  ('island_main', 20, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_main', 21, 'info', 'AIR', 'chest', '§e§lEventy',
      '["§7Dokončené eventy: §f%events%"]', NULL, FALSE),
  ('island_main', 22, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_main', 23, 'info', 'AIR', 'key', '§e§lPoklady',
      '["§7Nájdené poklady: §f%treasures%"]', NULL, FALSE),
  ('island_main', 24, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_main', 25, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_main', 26, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  -- row 3
  ('island_main', 27, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_main', 28, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_main', 29, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_main', 30, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_main', 31, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_main', 32, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_main', 33, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_main', 34, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_main', 35, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  -- row 4
  ('island_main', 36, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_main', 37, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_main', 38, 'action', 'AIR', 'pin', '§a§lNastaviť spawn',
      '["§7Nastav spawn bod ostrova","§7na svoju aktuálnu polohu."]', 'island:setspawn', FALSE),
  ('island_main', 39, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_main', 40, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_main', 41, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_main', 42, 'action', 'AIR', 'reset', '§c§lResetovať ostrov',
      '["§7Resetuje celý ostrov.","§c§lTáto akcia je nevratná!"]', 'island:reset', FALSE),
  ('island_main', 43, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_main', 44, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE)
ON DUPLICATE KEY UPDATE type=VALUES(type), material=VALUES(material), hdb_id=VALUES(hdb_id),
  display_name=VALUES(display_name), lore=VALUES(lore), action=VALUES(action), glow=VALUES(glow);

-- ============================================================
-- 5. island_stats — inner content  (rows 1–4, slots 9–44)
-- ============================================================
INSERT INTO nerith_gui_slots (gui_id, slot_index, type, material, hdb_id, display_name, lore, action, glow) VALUES
  -- row 1 (slots 9–17) — all decoration
  ('island_stats',  9, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_stats', 10, 'info', 'AIR', 'cube', '§e§lRozbitých blokov',
      '["§f%broken%"]', NULL, FALSE),
  ('island_stats', 11, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_stats', 12, 'info', 'AIR', 'sword', '§e§lZabití mobi',
      '["§f%mobs_killed%"]', NULL, FALSE),
  ('island_stats', 13, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_stats', 14, 'info', 'AIR', 'skull', '§e§lZabití bossovia',
      '["§f%bosses_killed%"]', NULL, FALSE),
  ('island_stats', 15, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_stats', 16, 'info', 'AIR', 'clock', '§e§lČas online',
      '["§f%online_time%"]', NULL, FALSE),
  ('island_stats', 17, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  -- row 2 (slots 18–26) — all decoration
  ('island_stats', 18, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_stats', 19, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_stats', 20, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_stats', 21, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_stats', 22, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_stats', 23, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_stats', 24, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_stats', 25, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_stats', 26, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  -- row 3 (slots 27–35)
  ('island_stats', 27, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_stats', 28, 'info', 'AIR', 'calendar', '§e§lDátum vytvorenia',
      '["§f%created_at%"]', NULL, FALSE),
  ('island_stats', 29, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_stats', 30, 'info', 'AIR', 'flag', '§e§lTyp ostrova',
      '["§f%island_type%"]', NULL, FALSE),
  ('island_stats', 31, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_stats', 32, 'info', 'AIR', 'reset', '§e§lPočet resetov',
      '["§f%reset_count%"]', NULL, FALSE),
  ('island_stats', 33, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_stats', 34, 'info', 'AIR', 'trophy', '§e§lPozícia v rebríčku',
      '["§f%rank%"]', NULL, FALSE),
  ('island_stats', 35, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  -- row 4 (slots 36–44) — all decoration
  ('island_stats', 36, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_stats', 37, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_stats', 38, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_stats', 39, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_stats', 40, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_stats', 41, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_stats', 42, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_stats', 43, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE),
  ('island_stats', 44, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE)
ON DUPLICATE KEY UPDATE type=VALUES(type), material=VALUES(material), hdb_id=VALUES(hdb_id),
  display_name=VALUES(display_name), lore=VALUES(lore), action=VALUES(action), glow=VALUES(glow);

-- ============================================================
-- 6. island_team, island_settings, island_perks, island_leaderboard
--    inner content — all placeholder decoration at launch
--    (rows 1–4, slots 9–44)
-- ============================================================

-- Macro-style: generate 36 decoration slots for each of the 4 GUIs

INSERT INTO nerith_gui_slots (gui_id, slot_index, type, material, hdb_id, display_name, lore, action, glow)
SELECT g.gui_id, s.slot_index, 'decoration', 'BLACK_STAINED_GLASS_PANE', NULL, '§r', NULL, NULL, FALSE
FROM
  (SELECT 'island_team'        AS gui_id UNION ALL
   SELECT 'island_settings'             UNION ALL
   SELECT 'island_perks'                UNION ALL
   SELECT 'island_leaderboard') AS g,
  (SELECT  9 AS slot_index UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL
   SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15 UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL
   SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20 UNION ALL SELECT 21 UNION ALL SELECT 22 UNION ALL
   SELECT 23 UNION ALL SELECT 24 UNION ALL SELECT 25 UNION ALL SELECT 26 UNION ALL SELECT 27 UNION ALL
   SELECT 28 UNION ALL SELECT 29 UNION ALL SELECT 30 UNION ALL SELECT 31 UNION ALL SELECT 32 UNION ALL
   SELECT 33 UNION ALL SELECT 34 UNION ALL SELECT 35 UNION ALL SELECT 36 UNION ALL SELECT 37 UNION ALL
   SELECT 38 UNION ALL SELECT 39 UNION ALL SELECT 40 UNION ALL SELECT 41 UNION ALL SELECT 42 UNION ALL
   SELECT 43 UNION ALL SELECT 44) AS s
ON DUPLICATE KEY UPDATE type=VALUES(type), material=VALUES(material), hdb_id=VALUES(hdb_id),
  display_name=VALUES(display_name), lore=VALUES(lore), action=VALUES(action), glow=VALUES(glow);

-- ============================================================
-- END OF SEED
-- Total rows inserted (approximate):
--   nerith_guis        :   6
--   nerith_gui_slots   : 324  (6 GUIs × 54 slots, minus AIR gaps)
-- ============================================================
