# Nerith — Project Status
> Auto-updated by Claude Code. Last: 2026-05-05

## Plugins

| Plugin | Spec | Code | Tests | Notes |
|--------|------|------|-------|-------|
| NerithCore | ✅ | 🔄 | ❌ | OneBlock engine, island GUI, scoreboard, PAPI expansion |
| NerithNPC | ✅ | ❌ | ❌ | Dialogue + quests |
| NerithEconomy | ❌ | ❌ | ❌ | Fragments, Vault |
| NerithLobby | ❌ | ❌ | ❌ | Lobby + minigames |
| NerithQuests | ✅ | ❌ | ❌ | Daily + milestones |
| NerithHUD | ✅ | ✅ | ❌ | Scoreboard, actionbar, bossbar — live on server |
| NerithGUI | ✅ | ❌ | ❌ | GUI engine, editor |

Legend: ✅ done | 🔄 in progress | ❌ not started

## Server

| Component | Status | Notes |
|-----------|--------|-------|
| PaperMC install | ✅ | 1.21.4 running locally |
| Worlds created | ✅ | hlavni_uzel, oneblock_world |
| MySQL setup | ✅ | XAMPP MySQL (nerith DB, nerith_user) |
| Third-party plugins | 🔄 | EssentialsX, LuckPerms, Vault, PAPI, Multiverse installed |
| Discord bot | ❌ | |

## Docs

| Doc | Status |
|-----|--------|
| NERITHCORE.md | ✅ |
| NERITHNPC.md | ✅ |
| NERITHQUESTS.md | ✅ |
| NERITHGUI.md | ✅ |
| NERITHHUD.md | ✅ |
| NERITHECONOMY.md | ❌ |
| NERITHLOBBY.md | ❌ |
| Lore document | ❌ |

## Last 5 changes
- 2026-05-05 — NerithHUD built: scoreboard, actionbar, bossbar + NerithCore PAPI expansion
- 2026-05-05 — NERITHGUI spec + GUI štruktúra island, docs sync
- 2026-05-05 — Init commit: project structure, docs, NerithCore source

## Known issues
- XAMPP MySQL musí byť spustený manuálne pred štartom servera (nie je Windows service)
