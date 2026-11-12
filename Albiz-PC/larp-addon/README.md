# Larp Addon 1.0 — revised Albiz implementation

Place beside ONE Albiz Client 1.0 JAR (PC or Mobile), with Fabric API for Minecraft Java 1.21.11 / Java21. Uses Albiz's `albiz` entrypoint, not Meteor. All tools start disabled; find them under Base Raid.

This is an independent implementation, NOT the original Meteor binary. The reference was re-audited after the previous overly simplified version. It is not advertised as perfectly identical. No CodeX ARR or Bassa implementation is included.

## V5
- Below-ceiling block clusters evaluated **per 16³ chunk section**, minimum3, default block weight5; spawner-block weight70. Isolated natural spawners filtered by default.
- Additional received evidence: spawner block-update packets, underground hostile-category positional sounds (+25), loot sync correlated to a recent outgoing block interaction (+60 for at least two signature stacks), chunk-data resends after the third receipt (+15).
- Type synergy: when at least two evidence types overlap, score × (1 + 0.25 × type count). Default threshold100, decay20/minute, chat alerts and tracers. Plates atY64; >=3 types use synergy color.
- Important safety differences: repeated unchanged structural snapshots do not add infinite score; signatures and sample counts are bounded, and loot correlation expires after5seconds. Current loot/block signature sets are independent heuristics, not a byte-identical copy of the original lists. Sound/packet resends may be ambient or server-driven; scores are not proof of a player/base.

## V6
- Both bee nests and beehives; inner/outer Chebyshev ring defaults0/12, max64 (only already received chunks).
- Default full-world-height pillars, using actual dimension bottom and height.
- Optional Player Render Distance Plates defaultOFF. Corrected to **8×8 CHUNKS = 128×128 blocks**, offsets[-4,3], atY64. NOT the previous mistaken8×8-block plate. Duplicate plates merge; global render budget can omit distant/late geometry.
- Range/Y changes trigger a shared rescan; no need to reconnect. Older addon configurations migrate once to the corrected pillar/plate defaults.

## V7 / Finder
- Default radius5, pillar mode; optional flat renderY64.
- Received spawner evidence is separate from player/entity evidence; no longer treated as a generic storage-cluster finder.
- Y0 spawners / underground spawners, received-block validation, optional nearby-player requirement.
- Visible players atbedrock/belowzero and entities on received bedrock/deepslate blocks produce separate colored chunk evidence. A player signal does NOT imply a spawner.
- No misleading Anti-ESP Bypass switches: the server cannot be forced to send hidden blocks. Original speculative packet retry/anti-ESP labels are deliberately not reproduced as promises.

## Performance / bounds
Uses Albiz's shared palette-filtered incremental scanner:2048 samples/tool on mobile,8192 on desktop. Section aggregation occurs at5-second intervals forV5; cached chunk summaries atmosttwice/second. Shared global Render3D frame limit across modules:1600 mobile /6000 desktop primitives. These caps may omit evidence/geometry in dense areas. Cooperative scan budget is not a hard frame-time guarantee. Detection depends on what chunks, states and packets the server actually sends.

## Verification
Compiled against Albiz's exact intermediary ABI for both profiles. Standalone CPU rule tests and metadata checks do not establish Minecraft/Android/server compatibility. No runtime injection, gameplay, shader, multiplayer, FPS or anti-cheat test was performed. Use only where server rules permit.
