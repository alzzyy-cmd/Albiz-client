# ANALYSIS — Albiz Client (PC + Mobil)

Scope of this document: what was found in the supplied sources, with file evidence. What was
changed is in `CHANGELOG.md`; per-feature status is in `FEATURE_MATRIX.md`; what was and was not
verified is in `TEST_REPORT.md`.

## 1. Inputs

| Archive | Role | Notes |
|---|---|---|
| `Albiz-Guncel-PC-Mobil-Kaynak.zip` | Primary base | `PC/` and `Mobil/`, 2,942 files each. Extracted unchanged into `Albiz-PC/` and `Albiz-Mobil/`, then edited. |
| `Albiz-Guncel-PC-Mobil-Kaynak-OPTIMIZED (2).zip` | Reference only | Differs from the base in three files: `ModuleConfigStore`, `TriggerbotModule`, `EspModule`. Nothing was copied from it. |
| `Sober-JARdan-Cikarilmis-Kaynak.zip` | Authorised reference | CFR 0.152 decompile of SoberClient 1.0.0 (`dev.sober.*`, 525 Java files, GPL-3.0-only). No Gradle project; its own `PROVENANCE.json` says compilation was never tested and lists two classes CFR failed on. |

The three archives in the repository root are untouched.

## 2. Build configuration (identical in both projects)

Fabric Loom 1.16.3, Minecraft 1.21.11, intermediary mappings, Fabric Loader 0.18.4, Fabric API
0.140.0+1.21.11, imgui-java 1.90.0, JUnit 5.11.4, Java 21, Gradle wrapper 9.4.0.
Build command from the project docs: `bash gradlew test build addonClasspath --max-workers=1`.

`PC/` and `Mobil/` differ only in `KAYNAK-OKU.md`, `albiz-profile.properties` and
`fabric.mod.json`. All Java sources are byte-identical; the platform is chosen at run time through
`PlatformInfo.isMobile()`. There is one UI architecture, not two.

## 3. Architecture as found

- **Modules**: `module/ClientModule` (id, name, description, category, settings, keybind).
  A single registration point, `BuiltinModuleProvider.createModules()`.
- **Second module system**: `port/soberui/UiModules` + `SoberClient`, used by the ported Sober
  UI (HUD, menus, radar). Sober world modules are wrapped by `port/SoberAdapter`.
- **Events**: `event/EventBus` with pooled event types. Three packet hooks exist:
  - `PacketReceiveNettyEvent` — network thread, every clientbound packet, cancellable.
  - `PacketReceiveEvent` — client thread, every clientbound packet, through a 4,096-entry queue
    that drops when full.
  - `PacketAppliedEvent` — fired for only seven packet classes (block update, section update,
    chunk data, light update, and three others).
- **GUI**: `gui/ImGuiScreen` → `gui/ClickGuiRenderer` (3,404 lines; category panels, search,
  favourites, bottom tab bar, Theme Studio, HUD Studio, config profiles).
- **Theme**: `config/ThemeStore` — accent colour (default crimson `#E3262E`), presets, surfaces,
  import/export string, reset, persisted.
- **i18n**: `assets/albiz/lang/{en_us,tr_tr}.json`, identical key sets; run-time lookup by English
  text through `AlbizI18n.display`.

## 4. Defects found

| # | Finding | Evidence | Outcome |
|---|---|---|---|
| D1 | Motion Scanner listened on `PacketAppliedEvent` for block-action, sound and entity-spawn packets, which that event never carries. Three of its four signal sources could never fire. | `MotionScannerModule.packet` | Fixed |
| D2 | Same mistake in Abyz Debug (crafter-sound path). | `AbyzDebugModule.packet` | Fixed |
| D3 | Same mistake in the Sober UI listener for damage, sound and time packets. | `port/soberui/SoberUi.java` | Fixed |
| D4 | Suspicious ESP drew the kelp-chunk marker at a fixed Y 63, wherever the kelp was. An underground or below-zero kelp farm was marked at sea level. | `port/sober/modules/SuspiciousESP.onRender` | Fixed |
| D5 | The registered "SusChunk" was an empty stub returning an empty set. The two real implementations (`SusChunkFinderModule`, `BetterSusChunkModule`) exist but are not registered, and a repository test forbids registering them. `TunnelMinerModule.flagged()` therefore always saw nothing. | `port/soberui/modules/SusChunk.java`, `tools/test_refresh_assets.py` | New evidence module added and wired to both consumers |
| D6 | `FakeStats` and `AntiVanishModule` exist, have translations, and (FakeStats) two active mixins, but were never registered, so neither could be enabled. | `BuiltinModuleProvider` | Registered |
| D7 | Abyz Debug treated "underground" as a hard-coded `y < 0`. | `UndergroundRules.underground` | Configurable ceiling added; old overload kept |
| D8 | `tools/audit_localization.py` failed on the unmodified source: 3 modules without description keys and 64 setting strings without catalogue entries. | baseline run | Fixed; audit now passes |
| D9 | `PacketCaptureModule.onApplied` has branches for block-event and world-event packets that cannot fire in the "applied" phase. The "received" phase does capture them, so no data is lost. | `PacketCaptureModule.describe` | Left as is; documented |
| D10 | **Retracted (2026-11-12).** An earlier version of this row said `albiz.soberui.mixins.json` listed a missing `ChatScreenMixin`. That was wrong: `port/soberui/mixin/ChatScreenMixin.java` exists, and a re-check found every listed mixin has a file. `MixinDuskBackdropScreen` is deliberately unlisted; `tools/test_native_ui_port.py` asserts it. | resources vs `mixin/` | No defect; nothing changed |
| D11 | Several modules are present but unregistered: Storage ESP, Netherite Finder (owner of `AdvancedFinder`), Sus Chunk Finder, Better Sus Chunk, and others. For eight of them a repository test asserts they must stay unregistered. | `tools/test_refresh_assets.py` | Left as is |

On the "nothing is detected below Y 0" report: the chunk scanners already index sections from the
world's real bottom section (`method_32891`), so section maths was not the cause in the scanners
inspected. Causes that were real: D1–D4 and D7. Whether a given server withholds below-zero
blocks cannot be known from source; the new Chunk Diagnostics module reports it per chunk.

## 5. Environment limits that shaped this work

The sandbox has no Java or Gradle and can reach only GitHub, npm and PyPI. `services.gradle.org`,
`maven.fabricmc.net`, Maven Central and Mojang's servers are unreachable, so the Gradle wrapper
cannot download Gradle and Loom cannot resolve Minecraft. **No JAR could be built and no code
that touches Minecraft classes could be compiled.** Details and the substitute checks used are in
`TEST_REPORT.md`.
