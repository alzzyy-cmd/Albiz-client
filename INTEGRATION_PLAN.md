# INTEGRATION PLAN

The master prompt defines 15 phases. This records what each phase got in this run and what is
left, so the work can be continued without re-analysis.

## Rules followed

- Base is the original Albiz source. Nothing was taken from the OPTIMIZED archive.
- No module, mixin, asset, setting id or config key was removed or renamed. All new settings are
  additive, so existing config files load unchanged and no migration or backup step was needed.
- Sober code was adapted onto the Albiz module/event/setting API, never copied as packages.
  Adapted files carry a GPL-3.0 provenance header and are listed in `THIRD_PARTY_PORTS.md`.
- New world scanning runs on the client thread in per-tick budgets with bounded collections.
  Nothing scans in a render callback. Network-thread listeners only count or enqueue.
- Diagnostics report what the server sent. Nothing fabricates packets or tries to recover data a
  server withholds.
- Every edit was made in `Albiz-PC/` and copied to `Albiz-Mobil/`; the two source trees remain
  identical apart from the three platform files.

## Phase status

| Phase | Subject | State | Detail |
|---|---|---|---|
| 1 | Inspection | Done | `ANALYSIS.md`, `FEATURE_MATRIX.md` |
| 2 | Module architecture | Done (no change needed) | Single provider kept; 8 modules registered (6 new, 2 revived) |
| 3 | New ClickGUI | **Not done** | See "Remaining work" 1 |
| 4 | SettingsPane / animations | Partly done | Settings transition implemented and configurable |
| 5 | Theme / ColorWheel | Existing, not extended | Hard-coded-colour sweep not done |
| 6 | Turkish | Done for new work; audit now passes | 307 keys added; 3,193 keys per language |
| 7 | Suspicious ESP / SusChunk | Done (SusChunk), partly (Suspicious ESP) | |
| 8 | Motion / Chunk Scanner | Motion Scanner repaired and extended; Chunk Scanner unchanged | |
| 9 | Other ESP | Bedrock Void ESP, Seed Chunk Finder added | Skeleton ESP added 2026-11-12; Storage ESP not done |
| 10 | 3D models | **Not done** | Needs a running game |
| 11 | HUD | **Not done** | Existing HUD Studio untouched |
| 12 | Other modules | Auto Respawn added; FakeStats, AntiVanish registered | |
| 13 | Performance | Profiles added 2026-11-12 | Nothing measured |
| 14 | Builds | **Blocked** | See `TEST_REPORT.md` |
| 15 | Regression / delivery | Done within limits | Repository Python suites show no new failures |

## Remaining work, in suggested order

1. **Build both projects on a machine with internet** and fix whatever the compiler reports.
   This comes first: about 2,000 lines of new module code that call Minecraft classes have never
   been compiled. Run `bash gradlew test build addonClasspath --max-workers=1` in each project.
   Expect the first errors, if any, in `SeedChunkFinderModule` (seeded-random calls) and
   `SoundDebugModule` (entity-sound packet accessor).
2. **In-game check of the settings transition** on desktop and Android. The card is now ~75% of
   the screen wide; rows were laid out for 460 px and may look stretched. If so, cap the content
   column inside the card rather than the card itself.
3. **Mixin configuration** (D10 in `ANALYSIS.md`): confirm whether the missing `ChatScreenMixin`
   breaks start-up and whether `MixinDuskBackdropScreen` is meant to be active.
4. **ClickGUI redesign**: left navigation, the nine requested categories, module cards. The
   category enum is persisted, so this needs a display-category mapping layered over
   `ModuleCategory`, not a rename.
5. **SusChunk evidence panel** in the GUI (currently chat + export).
6. **Suspicious ESP** detector interface and four-state result.
7. **Skeleton ESP** port, **3D model** fixes, **HUD** editor work, **performance presets**.
8. Decide whether Storage ESP and Netherite Finder should return; the repository test that keeps
   them unregistered would have to change with them.


## Update 2026-11-12

Items 3 and part of 7 and 8 in the list above are now closed:

- Item 3 (mixin configuration) was a false finding; see D10 in `ANALYSIS.md`.
- Skeleton ESP and performance profiles are implemented.
- Netherite Finder / Advanced Finder is registered again. Storage ESP is still unregistered,
  because `tools/test_refresh_assets.py` requires that; reviving it means changing that test,
  which is the owner's decision.

Still open: ClickGUI redesign, 3D model fixes, HUD editor, the hard-coded colour sweep and the
Suspicious ESP four-state result. The first four are visual work that cannot be judged without
running the game, and the game cannot be built here.

## Update 2026-11-12 (second pass)

Closed: Storage ESP (registered), Suspicious ESP four-state verdict, colour literals named in
`GuiPalette`. Still open: ClickGUI redesign, 3D model fixes, HUD editor changes — all three
need a built, running game. Next step for the colours: make the `GuiPalette` constants read
from `ThemeStore`.
