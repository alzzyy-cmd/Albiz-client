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

Albiz-written bridges/UI changes include the bounded main module browser,
responsive multiplayer cards backed by vanilla server actions and pinger,
client-thread scan queue with chunk identity checks, budgets, generic configurable
Staff HUD, extra crosshair shapes and procedural glint presets. They are not
literal ports of all Sober GUI/HUD classes. Existing HUD and approved characters
remain in place. No second addon loader/client registry is bundled.

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
