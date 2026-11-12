# Reference ports — Albız 1.0 refresh

Existing LICENSE, NOTICE.md, 4ldenz and PayGuard notices remain unchanged.
Alzzyy maintains this Albız adaptation; Luvzi and Rosy are supporters. This file
adds provenance; it does not claim authorship of the upstream work.

## SoberClient

Reference supplied in the user's canonical repository:
https://github.com/alzzyy-cmd/Anubis
`SoberClient (3).zip.001` + `.002`, ordered concatenation.
Metadata: SoberClient 1.0.0, Violet, MC 1.21.11, Java 21, GPL-3.0-only.
The archive contains compiled classes/resources, not an original source tree.
The selected classes were recovered with CFR 0.152 and adapted. These are not
claimed to be the original author's source or official mappings. This project
uses Fabric intermediary mappings, not Yarn source names.

Adapted code:
- `port/sober/modules/{SpawnerBeam,BlockEntityESP,ChunkFinder,SuspiciousESP}`,
  selected setting/color support, and `port/sober/sky/`.
- `MixinCustomTotems` follows the reference's item-model-component interception.
  The Albiz CustomTotems module itself uses the existing Albiz settings framework.
- Sky shader/include resources and two sky images are from the reference.
- 97 totem item definitions/models/textures are namespaced to `albiz`.
- 116 recorded key/mouse samples were converted from signed 16-bit little-endian,
  48 kHz stereo PCM to Ogg Vorbis. Playback uses Minecraft's normal audio engine;
  the reference's javax.sound.sampled/dynamic native audio hook is NOT included.
- `sober_banana.glb`, `sober_bot.glb`, `sober_cat.glb` are included in source as
  `artwork/characters/{banana,robot,cat}.glb`, baked to Albiz ANC with three LODs.
  The source's static meshes have no usable skeleton; articulation is approximate.

The upstream archive's declared GPL license is the basis for these selected
materials; separate authorship/asset terms were not supplied in that archive.
This is not an assertion that Violet independently authored every referenced
recording/model/texture. Retain this provenance and honor any applicable
additional rights. The complete GPL text is retained as LICENSE.

## Native UI revision (supersedes the first refresh's approximate cards)

The selected `port/soberui/` presentation code now adapts the actual Sober
Panel/Dropdown, settings/pickers, Sober/Home/VanillaPlus/Island menu variants,
MultiplayerGridScreen, HudRenderer, TargetCard, font/texture/shape primitives,
UI modules, chat editing and styling-only widget mixins. Target display shield
observations are a small read-only CombatFx subset, not its world/combat effects.
CFR artifacts (generics, resource exception paths and definite assignment) were
repaired. The original running Albiz modules are accessed through view adapters.
Brand PNGs were replaced with the approved Albiz wolf/name and red accent.
UI/menu/HUD/font/shader resources are from the supplied reference; its
FONT-LICENSES.txt is retained as assets/albiz/licenses/SOBER-FONT-LICENSES.txt.
The reference does not supply the Consolas TTFs: its optional choice/provider
aliases use the supplied Roboto Mono instead; no Microsoft TTF is redistributed.

Accounts/Credits and approved special feature/world overlays remain available.
The existing loading lifecycle was restyled, NOT copied from a nonexistent
reference loading screen. Remote reference config sharing is disabled in favor
of local clipboard export/import, and secret settings are not exported/applied
by native profiles. No second addon loader/client initializer is included.
Native UI reset deliberately leaves canonical gameplay modules/keys/secrets alone.
Some new help strings remain English. See README for deviations and validation
limits: compilation/static tests passed, but Minecraft/OpenGL/Android/server
flows and screenshot parity were not runtime-verified.

## Larp reference / ownership limit

`Download.zip` contains a V7-labeled addon whose actual classes are LarpDebugV5,
LarpDebugV6 and Finder. Its CC0 template file is not treated as conclusive proof
of ownership of all leaked third-party code. No Larp/Bassa/CodeX bytecode or source
is copied into this build. Hive Finder's independently implemented inner/outer
ring controls and optional any-hive behavior are inspired by the observed V6
behavior. Do NOT call this a complete V7 port or a hidden-chunk/packet bypass.
CodeX's All Rights Reserved metadata is respected; it was not imported.

## Existing character assets

Guts/CC-BY-NC-SA-4.0 and all prior character notices remain separately applicable;
see THIRD_PARTY_CHARACTERS.md and assets/albiz/licenses/CHARACTERS.md. The existing
four ANC assets, approved sword implementation, keybinding source and Android
native were verified unchanged against the previous delivery.

## Category/card GUI + item visual revision

The primary settings GUI is again the original Albız category/card interface;
the native menu/multiplayer/HUD renderers remain. The earlier "supersedes cards"
heading describes history, not the current default.

Additional adapted Sober classes: CsPickaxe; CustomModels; the model package
(ModelLoader, ModelLibrary, ModelMesh, ModelIcon, ModelManager, SpriteModel,
WaterModels, WaterIcon, WaterMesh); KnifeMaterial, KnifeMesh, KnifeMotion,
KnifeRenderer, KnifeSkin, KnifeTrail; and held-item appearance/inspect hooks.
The supplied Fortnite Pack PNGs/items.txt and white model textures were remapped
to assets/albiz. See the Sober GPL declaration and asset-authorship caveat above.
Fortnite/Watery/CS names are retained as upstream preset labels, not endorsements
or claims of ownership. VisualPresetsScreen, KnifePreview, bounded caches and
path/input validation are adaptation work. The reference's combat/movement,
advanced glint, skins and accessories were not added in this revision.

## Hotfix 2
Sprite contour helpers were extracted into CPU-only SpriteGeometryBuilder and
SpriteSegments, replacing an unrecovered decompiler body with explicit endpoint
adjacency stitching. UiResourceNames is locale-independent. Menu branding changes
are presentation-only: upstream license and authorship notices remain intact.

## Version 1.0 Glass extension
Glass, GlassLayers, GlassGear and their narrowly scoped mixin hooks and glass_entity shader resources are adapted from the supplied SoberClient (Violet), GPL-3.0-only. The corresponding modified source is included in the release source archive. Motion Scanner, Activity Inspector and the Larp-compatible addon are independent implementations. No CodeX ARR module code is incorporated.

## SoberClient behaviour adapted in the 2026-11 integration pass

Source: the same SoberClient 1.0.0 JAR (Violet, GPL-3.0-only), decompiled with CFR 0.152.
These files are new Albiz implementations on the Albiz module/event/setting API; only the
detection rules were taken from the named Sober classes. Each carries a header comment.

- `module/impl/misc/AutoRespawnModule.java` — from `dev.sober.modules.AutoRespawn`.
- `module/impl/render/BedrockVoidEspModule.java` — hole rule and floor/roof selection from
  `dev.sober.modules.BedrockVoidESP`; scanning rewritten as a budgeted client-thread walk.
- `module/impl/render/SeedChunkFinderModule.java` + `util/seedscan/DeepslateGradient.java` —
  deepslate-gradient rule from `dev.sober.modules.SeedChunkFinder`.
- `port/sober/modules/SuspiciousESP.java` — existing port; kelp marker now drawn at the Y range
  of the kelp that was received instead of a fixed Y 63.

Not derived from Sober (original Albiz work): `sound/**`, `suschunk/**`,
`diagnostics/ChunkReceiptLog.java`, `esp/ChunkWorkQueue.java`, `gui/SettingsTransition.java`,
`SoundDebugModule`, `ChunkDiagnosticsModule`, `SusChunkModule`.
