# Nerith — Project Status
> Auto-updated by Claude Code. Last: 2026-05-06 (2)

## Plugins

| Plugin | Spec | Code | Tests | Notes |
|--------|------|------|-------|-------|
| NerithCore | ✅ | 🔄 | ❌ | OneBlock engine, island GUI, scoreboard, PAPI expansion — live ✅ |
| NerithNPC | ✅ | ❌ | ❌ | Dialogue + quests |
| NerithEconomy | ✅ | ✅ | ❌ | Fragments, Echo, Vault, shops, auction house — live ✅ |
| NerithLobby | ✅ | ✅ | ❌ | Lobby routing, world rules, boards, 2 mini-games — live ✅ |
| NerithQuests | ✅ | ❌ | ❌ | Daily + milestones |
| NerithHUD | ✅ | ✅ | ❌ | Scoreboard, actionbar, bossbar — live ✅ |
| NerithGUI | ✅ | ❌ | ❌ | GUI engine, editor |

Legend: ✅ done | 🔄 in progress | ❌ not started

## Server

| Component | Status | Notes |
|-----------|--------|-------|
| PaperMC install | ✅ | 1.21.4-232 running locally |
| Worlds created | ✅ | hlavni_uzel, oneblock_world |
| MySQL setup | ✅ | XAMPP MariaDB 10.4 — relay log corruption fixed, skip-slave-start added |
| Third-party plugins | ✅ | EssentialsX 2.21.2, LuckPerms, Vault, PAPI, Multiverse — all running |
| Discord bot | ❌ | |

## Docs

| Doc | Status |
|-----|--------|
| NERITHCORE.md | ✅ |
| NERITHNPC.md | ✅ |
| NERITHQUESTS.md | ✅ |
| NERITHGUI.md | ✅ |
| NERITHHUD.md | ✅ |
| NERITHECONOMY.md | ✅ |
| NERITHLOBBY.md | ❌ |
| Lore document | ❌ |

## Last 5 changes
- 2026-05-06 — NerithEconomy live: Fragments, Echo, Vault, login bonus, shops, auction house, PAPI placeholders, 7 commands
- 2026-05-06 — Fixed MySQL startup: cleaned corrupted relay/master-info files, added skip-slave-start + skip-log-bin to my.ini
- 2026-05-06 — Fixed EssentialsX duplicate JAR (removed old v2.21.0, kept v2.21.2)
- 2026-05-05 — NerithHUD built: scoreboard, actionbar, bossbar + NerithCore PAPI expansion
- 2026-05-05 — Init commit: project structure, docs, NerithCore source

## Known issues
- XAMPP MySQL musí byť spustený pred štartom servera — nie je Windows service, spúšťa sa cez `C:\xampp\mysql\bin\mysqld.exe`
- EssentialsX hlási "unsupported server version" pre Paper 1.21.4 — funguje správne, len varovanie
- MySQL skip-slave-start a skip-log-bin pridané do my.ini — bez toho sa MariaDB nespustí (relay log korupcia)
