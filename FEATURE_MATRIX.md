# FEATURE MATRIX — 43 features

Statuses: EXISTING, EXISTING_BUT_BROKEN, PARTIALLY_IMPLEMENTED, NOT_IMPLEMENTED, INTEGRATED,
IMPROVED, TESTED, BLOCKED.

**No feature is marked TESTED.** TESTED would mean it was run in the game, and no build was
possible here. Where pure-logic unit tests exist they are named in the last column.
INTEGRATED means: source written or repaired, registered, translated, mirrored to both projects,
and free of compile errors other than unresolved Minecraft/Fabric types — but not run.

"Before" is the state of the supplied source. Paths are under `src/main/java/net/albiz/client/`.

## A — ESP / world

| # | Feature | Before | Now | Where | Notes |
|---|---|---|---|---|---|
| 1 | Suspicious ESP | EXISTING_BUT_BROKEN | INTEGRATED | `port/sober/modules/SuspiciousESP` (id `suspicious-esp`) | Kelp marker now at the kelp's real Y range, with tracer. Not done: separate confirmed/suspicious/incomplete/unknown states and a pluggable detector interface — the new SusChunk module carries the evidence model instead. **2026-11-12:** four-state chunk verdict added (Clear / Weak / Suspicious / Strong; `suschunk/SuspicionVerdict`, 9 passing unit tests), off by default. Not seen in game. |
| 2 | SusChunk | NOT_IMPLEMENTED (stub) | INTEGRATED | `module/impl/render/SusChunkModule` (id `sus-chunk`), `suschunk/**` | 11 evidence types, per-type weight settings, score, NONE/LOW/MEDIUM/HIGH confidence, alert cooldown, per-chunk cache, rescan dedupe, clear, TSV export, budgeted scan, explanations in chat. Unit tests: `EvidenceBookTest`, `BlockEvidenceRulesTest`. Explanations are shown in chat and export, not yet in a GUI panel. |
| 3 | Motion Scanner | EXISTING_BUT_BROKEN | INTEGRATED | `module/impl/render/MotionScannerModule` | Three dead signal sources repaired; world events, radius, notify and duplicate suppression added. No export or per-event history list. |
| 4 | Chunk Scanner | EXISTING | EXISTING | `module/impl/render/ChunkScannerModule`, `esp/ChunkScanner` | Unchanged. |
| 5 | Bedrock Void ESP | NOT_IMPLEMENTED | INTEGRATED | `module/impl/render/BedrockVoidEspModule` | Uses the dimension's real bottom Y. Unit test for its queue: `ChunkWorkQueueTest`. |
| 6 | Chunk Finder | EXISTING | EXISTING | `port/sober/modules/ChunkFinder` (id `hive-finder`) | Unchanged. |
| 7 | Tunnel Hole ESP | EXISTING | EXISTING | `module/impl/render/HoleEspModule` (id `hole-esp`) | Albiz equivalent covers holes, tunnels and stairs. Sober's `TunnelHoleESP`/`Shafts` not ported. |
| 8 | Block Entity ESP | EXISTING | EXISTING | `port/sober/modules/BlockEntityESP` | Unchanged. |
| 9 | Storage ESP | EXISTING_BUT_BROKEN | INTEGRATED | `module/impl/render/StorageEspModule` | Registered again on the owner's request (2026-11-12); source unchanged. `tools/test_refresh_assets.py` used to require it to stay unregistered and now requires it to be registered. Names checked against the 1.21.11 mappings (0 problems). Not compiled, not run in game. |
| 10 | Block ESP | EXISTING | EXISTING | `module/impl/render/BlockEspModule` | Unchanged. |
| 11 | Light ESP | EXISTING | EXISTING | `module/impl/render/LightEspModule` | Unchanged. |
| 12 | Seed Chunk Finder | NOT_IMPLEMENTED | INTEGRATED | `module/impl/render/SeedChunkFinderModule`, `util/seedscan/DeepslateGradient` | Unit test: `DeepslateGradientTest`. The seeded-random calls come from Sober's decompile and appear nowhere else in Albiz, so they are the least verified lines in this work. |
| 13 | Spawner Beam | EXISTING | EXISTING | `port/sober/modules/SpawnerBeam` | Unchanged. |
| 14 | Advanced Finder | EXISTING_BUT_BROKEN | INTEGRATED | `module/impl/misc/{AdvancedFinder,NetheriteFinderModule}` | `NetheriteFinderModule` (owner of Advanced Finder) is registered again; source unchanged. Its Minecraft names were checked against the 1.21.11 mappings (0 problems). Not compiled, not run in game. |
| 15 | Activity Inspector | EXISTING | EXISTING | `module/impl/render/ActivityInspectorModule` | Missing description and setting translations added. |
| 16 | AntiVanish | EXISTING_BUT_BROKEN | INTEGRATED | `module/impl/misc/AntiVanishModule` | Was never registered. Registered; code unchanged. |

## B — Player / render

| # | Feature | Before | Now | Where | Notes |
|---|---|---|---|---|---|
| 17 | Skeleton ESP | NOT_IMPLEMENTED | INTEGRATED | `esp/SkeletonPose`, `module/impl/render/SkeletonEspModule` | New implementation, not Sober's: the pose is computed from body yaw, head rotation, walk cycle and sneaking, so no model mixin is needed. Pose maths has 7 passing unit tests; the module is mapping-checked only. Does not follow arm swings, elytra, swimming or sleeping poses. Not seen in game. |
| 18 | Custom Models | EXISTING | EXISTING | `port/soberui/modules/CustomModels` | Requested 3D fixes (position, rotation, scale, state leakage) not done: they need the game running to see. |
| 19 | Player ESP | EXISTING | EXISTING | `module/impl/render/EspModule` | Unchanged. |
| 20 | Custom Totems | EXISTING | EXISTING | `module/impl/render/CustomTotemsModule` | Unchanged. |
| 21 | NameTags | EXISTING | EXISTING | `module/impl/render/NametagsModule` | Unchanged. |
| 22 | Swing Animations | EXISTING | EXISTING | `module/impl/render/SwingAnimationModule` | Unchanged. |
| 23 | Custom Crosshair | EXISTING | EXISTING | `module/impl/render/CustomCrosshairModule` | Unchanged. |
| 24 | Jump Circles | EXISTING | EXISTING | `module/impl/render/JumpCirclesModule` | Unchanged. |
| 25 | Tracers | EXISTING | EXISTING | `module/impl/render/TracersModule` | Unchanged. |

## C — GUI / visual

| # | Feature | Before | Now | Where | Notes |
|---|---|---|---|---|---|
| 26 | Sober-inspired GUI infrastructure | EXISTING | EXISTING | `port/soberui/**`, `gui/ClickGuiRenderer` | The requested ground-up ClickGUI redesign (left navigation, nine new categories) was **not** done. |
| 27 | SettingsPane | EXISTING | PARTIALLY_IMPLEMENTED | `gui/ClickGuiRenderer` (module card), `port/soberui/gui/SettingsPane` | The module settings card now uses the mandated layout: browser slides left with 25% left visible, settings fill the other 75%. |
| 28 | Smooth Animations | EXISTING | INTEGRATED | `gui/SettingsTransition`, `port/soberui/gui/Smooth` | Time-based settings transition with presets Smooth / Fast / Soft / Custom / Off and custom duration, easing, distance, opacity, scale, direction. Unit test: `SettingsTransitionTest`. |
| 29 | Theme Engine | EXISTING | IMPROVED | `config/ThemeStore` | Already had accent, presets, surfaces, import/export, reset, persistence. Transition settings added. **2026-11-12:** the 60 bare colour numbers in `ClickGuiRenderer` now refer to named constants in `GuiPalette` with the same values, so the look is unchanged; they are not yet user-themeable. |
| 30 | HudLayout | EXISTING | EXISTING | `port/soberui/gui/HudLayout`, `gui/HudLayoutStore` | Unchanged. |
| 31 | ColorWheel | EXISTING | EXISTING | `port/soberui/gui/ColorWheel` | Unchanged. |
| 32 | Notifications | EXISTING | EXISTING | `port/soberui/gui/Notifications` | Unchanged. |
| 33 | Inventory HUD | EXISTING | EXISTING | `port/soberui/modules/InventoryHud` | Unchanged. |
| 34 | Target HUD | EXISTING | EXISTING | `port/soberui/modules/TargetHud` | Unchanged. |
| 35 | Region Map | EXISTING | EXISTING | `module/impl/client/RegionMapModule`, `port/soberui/gui/RegionMap` | Radar now receives real flagged chunks from SusChunk. |
| 36 | FakeStats | EXISTING_BUT_BROKEN | INTEGRATED | `module/impl/misc/FakeStats` + 2 mixins | Was never registered. It replaces the sidebar and tab entries with your own name and stats on a client-side objective (`albiz_fs`); with the module off the mixins return early and the normal scoreboard is untouched. Registered; code unchanged. |

## D — Other

| # | Feature | Before | Now | Where | Notes |
|---|---|---|---|---|---|
| 37 | FPS Booster | EXISTING | IMPROVED | `module/impl/client/PerformanceModule`, `perf/PerformanceProfile` | Added a Performance Profile setting (Quality / Balanced / Performance / Custom). It sets the shared scan budget and scales chunks-per-tick of SusChunk, Bedrock Void ESP and Seed Chunk Finder. Default is Custom, so saved configs behave as before. 4 passing unit tests. No FPS was measured. Sober's `FpsBooster` not ported. |
| 38 | Freelook | EXISTING | EXISTING | `FreelookModule` | Unchanged. |
| 39 | Auto Respawn | NOT_IMPLEMENTED | INTEGRATED | `module/impl/misc/AutoRespawnModule` | Delay and death-coordinates settings. |
| 40 | Motion Blur | EXISTING | EXISTING | `MotionBlurModule` | Unchanged. |
| 41 | Freecam | EXISTING | EXISTING | `module/impl/misc/FreecamModule` | Unchanged. |
| 42 | Fullbright | EXISTING | EXISTING | `Fullbright` | Unchanged. |
| 43 | Zoom | EXISTING | EXISTING | `ZoomModule` | Unchanged. |

## Added on request (outside the 43)

| Feature | Now | Where | Notes |
|---|---|---|---|
| Sound Debug | INTEGRATED | `module/impl/render/SoundDebugModule`, `sound/**` | Unit tests: 5 classes in `src/test/java/net/albiz/client/sound/`. |
| Chunk Diagnostics | INTEGRATED | `module/impl/render/ChunkDiagnosticsModule`, `diagnostics/ChunkReceiptLog` | Unit test: `ChunkReceiptLogTest`. |

## Build

| Target | Status | Reason |
|---|---|---|
| PC JAR | BLOCKED | Gradle wrapper cannot download Gradle 9.4.0 (`services.gradle.org` unreachable). |
| Mobil JAR | BLOCKED | Same. |

## Totals for the 43

| Now | Count |
|---|---|
| EXISTING (unchanged, or translations/wiring only) | 28 |
| IMPROVED | 2 |
| INTEGRATED | 12 |
| PARTIALLY_IMPLEMENTED | 1 |
| EXISTING_BUT_BROKEN (not fixed) | 0 |
| NOT_IMPLEMENTED | 0 |

