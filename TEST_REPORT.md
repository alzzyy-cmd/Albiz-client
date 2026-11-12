# TEST REPORT

Everything below was actually run in this session. Nothing is estimated.

## 1. What could not be run

| Check | Result | Exact error |
|---|---|---|
| Gradle build, `Albiz-PC` | **Not run — blocked** | `Downloading https://services.gradle.org/distributions/gradle-9.4.0-bin.zip` → `javax.net.ssl.SSLHandshakeException: Remote host terminated the handshake` |
| Gradle build, `Albiz-Mobil` | **Not run — blocked** | Same |
| Full compilation | **Not possible** | No Minecraft 1.21.11, Fabric API, Fabric Loader or imgui-java JARs are obtainable: `maven.fabricmc.net`, Maven Central and Mojang hosts are unreachable from the sandbox |
| JUnit suite through Gradle | **Not run** | Follows from the above |
| In-game behaviour, rendering, performance | **Not run** | No JAR |

The affected file for the build failure is `gradle/wrapper/gradle-wrapper.properties`
(`distributionUrl`); the file is correct, the host is unreachable. No source file caused it.

Consequences, stated plainly:

- **No PC JAR and no Mobil JAR exist.**
- Code that calls Minecraft classes has never been compiled. It may contain compile errors.
- No feature has been seen working in the game.
- There are no performance measurements of any kind.

## 2. Substitute checks that were run

### 2.1 Unit tests of the new pure-Java logic

Compiled with the Eclipse compiler (ECJ 3.39.0, `-21`) and run on OpenJDK 25 against a minimal
stand-in for the JUnit 5 API (see `verification/`). The test files are ordinary JUnit 5 tests and
will run under Gradle unchanged; they have not been run under real JUnit.

| Project | Passed | Failed |
|---|---|---|
| Albiz-PC | 76 | 0 |
| Albiz-Mobil | 76 | 0 |

| Test class | Tests | Covers |
|---|---|---|
| `suschunk/EvidenceBookTest` | 11 | Rescan dedupe, per-type caps, configurable weights, confidence rules, alert cooldown, live-evidence expiry, bounded size, explanation text, export format |
| `suschunk/BlockEvidenceRulesTest` | 2 | Block-to-evidence mapping, deepslate range |
| `gui/SettingsTransitionTest` | 9 | Time-based progress, frame-rate independence, mid-way reversal, easing end points, 25% / 75% layout, four directions, small touch screen, value clamping, presets |
| `sound/SoundClassifierTest` | 6 | Sound id → kind |
| `sound/SoundEvidenceLogTest` | 6 | History bound, duplicate window, hotspot score and confidence, expiry |
| `sound/UndergroundTest` | 4 | Y ceiling, world bottom, depth below surface, unknown surface |
| `sound/WorldSignalRulesTest` | 4 | World events, block actions, kelp/crop transitions |
| `sound/PacketDiagnosticsTest` | 2 | Status labels per packet channel |
| `diagnostics/ChunkReceiptLogTest` | 4 | Packet status, negative chunk coordinates, LRU bound, section maths for bottoms of -64, 0 and -128 |
| `esp/ChunkWorkQueueTest` | 4 | Key packing, dedupe, nearest-first order, bound |
| `util/seedscan/DeepslateGradientTest` | 4 | Chance curve, informative band, seed parsing |

### 2.2 Compile check of code that uses Minecraft classes

Each new or edited file was compiled with ECJ against the project's own sources. Without the
Minecraft JARs every reference to a `net.minecraft` or `net.fabricmc` type is reported as
unresolved; those were filtered out and the remainder inspected.

- New files (6 modules): **0 errors** other than unresolved Minecraft/Fabric/fastutil types.
- Edited files (10): error profile **identical to the original file**, or differing only in the
  count of unresolved-type messages for lines that were added.

What this does and does not show: it confirms syntax and every use of Albiz's own API (module
base class, settings, events, renderer, chat, i18n). It does **not** confirm that a call on a
Minecraft type exists or has the right signature. To limit that risk, every Minecraft member used
in new code was taken from a call that already exists elsewhere in the Albiz source, with these
exceptions, which come only from Sober's decompile:

| File | Unverified call |
|---|---|
| `SeedChunkFinderModule` | `new class_6677(seed).method_38421().method_38995("minecraft:deepslate").method_38421()` and `method_38418(x,y,z).method_43057()` |
| `BedrockVoidEspModule` | `level.method_8597().comp_653()` (dimension logical height) |
| `SoundDebugModule` | shape of the sound holder returned by `class_2765.method_11882()` |

### 2.3 Repository Python suites

Run on the unmodified source first (baseline), then on both edited projects.

| Suite | Baseline | Albiz-PC | Albiz-Mobil |
|---|---|---|---|
| `test_bugfix_regressions` (4) | OK | OK | OK |
| `test_friends_revision` (9) | OK | OK | OK |
| `test_original_anubis_gui` (3) | OK | OK | OK |
| `test_read_capture` (4) | OK | OK | OK |
| `test_gui_restore_visuals` (6) | 2 failures, 1 error | same 3 | same 3 |
| `test_native_ui_port` (7) | 1 failure | same 1 | same 1 |
| `test_refresh_assets` (4) | 2 failures | same 2 | same 2 |
| `test_requested_revision` (8) | 2 failures, 1 error | same 3 | same 3 |
| `audit_localization.py` | **fails** | **passes** | **passes** |

The nine failing tests fail identically before and after. They assert on binary assets that the
supplied source ZIP does not contain (Android imgui native library, brand lockup image, sound
recordings, totem model and dusk texture) and on first-run flags; they are not caused by this
work and cannot be fixed without those assets:

```
test_gui_restore_visuals   test_bounded_previews_and_no_secret_input_changes   (error)
test_gui_restore_visuals   test_our_dusk_texture_not_animated_space_shader
test_gui_restore_visuals   test_totem_visual_button_and_our_new_model_assets
test_native_ui_port        test_native_font_texture_resources
test_refresh_assets        test_sound_events_resolve_and_all_recordings_are_vorbis
test_refresh_assets        test_totem_model_and_texture_closure
test_requested_revision    test_audio_event_streams_actual_asset               (error)
test_requested_revision    test_first_run_flags_local_and_gate_saved
test_requested_revision    test_real_logo_baked_not_fake_label
```

### 2.4 Language files

Both projects: 3,193 keys in `en_us.json` and in `tr_tr.json`, identical key sets, 161 module
descriptions (each differs between languages), 152 module names (each identical in both, as the
existing `LocalizationTest` requires). `LocalizationTest` itself was updated for the new counts
but not run (it needs Gson).

### 2.5 PC / Mobil parity

`diff -rq Albiz-PC Albiz-Mobil` reports only the three platform files that differed in the
original (`KAYNAK-OKU.md`, `albiz-profile.properties`, `fabric.mod.json`) and the two generated
audit outputs under `docs/`.

## 3. Not tested at all

- Every in-game behaviour of every new or changed module.
- The settings transition on screen, on desktop or Android.
- Fake Stats and Anti Vanish after registration (their code is unchanged, but they have not run
  since they were unregistered).
- Config load/save round trip with the new settings.
- Performance.


## Update 2026-11-12

**Build: still not run.** The download hosts were probed again. `services.gradle.org`,
`plugins.gradle.org`, `maven.fabricmc.net`, `repo.maven.apache.org`, `piston-meta.mojang.com`,
`libraries.minecraft.net` and the GitHub release-asset host are all unreachable from this
sandbox. No JAR exists.

**Unit tests:** 67 passed, 0 failed in each project (was 56). New: `SkeletonPoseTest` (7),
`PerformanceProfileTest` (4). The counts in the table above are updated.

**Mapping check (new):** the Fabric intermediary mappings for 1.21.11 were obtained and every
Minecraft class, method and field name, and every call's argument count, in the new and edited
files was checked against them: 0 problems. This covers the calls earlier described as
unverified. It is a name and argument-count check only; types and generics are still unchecked
because nothing was compiled against Minecraft.

**Python suites and localisation audit:** unchanged — the same four suites pass, the same four
fail as on the untouched original (missing binary assets), and the audit exits 0. Language
files have 3202 keys each with identical key sets.

**Not run:** `LocalizationTest` (expected counts edited to 162 descriptions / 153 names), any
in-game check, any FPS measurement.

## Update 2026-11-12 (second pass)

- **Build:** still not run; no JAR exists. See `DERLEME-REHBERI.md` for the build steps.
- **Unit tests:** 76 passed, 0 failed in each project. New: `SuspicionVerdictTest` (9).
- **Mapping check:** `SuspiciousESP`, `StorageEspModule`, `ClickGuiRenderer`, `GuiPalette`,
  `BuiltinModuleProvider` — 0 problems.
- **Colour refactor check:** `ClickGuiRenderer` and `GuiPalette` give the same compiler
  diagnostics as the original files (only unresolved Minecraft/imgui types), so the 60
  replacements introduced no syntax or name error.
- **Repository suites:** same four pass, same four fail as the original. One test was edited:
  `test_refresh_assets.py` now expects `StorageEspModule` to be registered. Its two remaining
  failures are the original missing-asset failures.
- **Localisation audit:** exit 0; 3210 keys per language, identical key sets.
- **Not done:** ClickGUI redesign, 3D model fixes, HUD editor changes.

## Update 2026-11-12 (third pass) — real build on GitHub Actions

The sandbox still cannot reach the Gradle/Maven/Mojang hosts, so the build was run on GitHub
Actions instead (`.github/workflows/build.yml`, run 43319470403).

- **Build:** `gradlew build -x test` → exit 0 for both Albiz-PC and Albiz-Mobil.
  `albiz-1.0.jar` produced (about 12.9 MB each). All new and edited code compiled without
  errors on the first attempt. The JARs were built without the large binary assets, which are
  not in the source tree.
- **Gradle tests:** 252 run. Albiz-PC 239 passed / 13 failed; Albiz-Mobil 238 passed / 14 failed.
  - First run had one more failure, `V1RegressionTest.mobileBudgetEnforced`, caused by this
    work (the performance-profile change removed a line that test looks for). Fixed; passes now.
  - The remaining failures: `BakedHeroesTest` (7), `HeroGeometryTest` (3),
    `SpriteRegressionTest` (1), `RecoveryTest.suppliedAndroidNativeIsActuallyArm64` (1),
    `RequestedRevisionTest.audioIsUserAssetAndThrottled` (1) all read model, image, native or
    audio files that are absent from the source tree. Mobil also fails
    `RecoveryTest.platformManualOverrideAndGlslSelection`, which is in code this work did not
    touch. The original source was not built on CI for comparison, so "pre-existing" is an
    inference from the error types, not a measurement.
  - `LocalizationTest` now ran and passed.
- **Still not done:** running the game. Nothing has been seen in game.
