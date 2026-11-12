# Character asset provenance and terms

These notices cover art assets, not the GPL-licensed client code. They do not
replace, remove or relabel any existing LICENSE, NOTICE or upstream notices.

## Guts

- Original model: **Berserk Guts Black Swordsman**, by **gimora**.
- Author's article: https://reflectorange.net/archives/221.html
- Original listing: https://sketchfab.com/3d-models/berserk-guts-black-swordsman-0ad7c8ffbeef4cc196cbd217557fef60
- Official license metadata: https://api.sketchfab.com/v3/models/0ad7c8ffbeef4cc196cbd217557fef60
- License: **Creative Commons Attribution–NonCommercial–ShareAlike 4.0 International**.
- License text: `licenses/CC-BY-NC-SA-4.0.txt`, https://creativecommons.org/licenses/by-nc-sa/4.0/
- Public GLB copy retrieved from RigModels' anonymous download interface.
  The mirror's generic “Royalty Free” label is NOT the applicable license.
- Changes: detached Puck/display prop removed, source held-sword triangles removed,
  arms relaxed, head proportions adjusted, triangle strips expanded, base-color
  maps converted to PNG, three animation LODs baked, faulty source normals rebuilt,
  facial paint adapted using the task's supplied visual reference.
- Covered files: `artwork/characters/guts.glb`, its generated atlases/previews and
  `src/main/resources/assets/albiz/characters/guts.anc` (or their packaged paths).
  This adaptation is supplied under **CC BY-NC-SA 4.0**, not GPL or an original-Albiz-art claim.
- Keep attribution and license when sharing the asset/adaptation. Do not sell or
  commercially exploit this asset. The bundled client therefore is not offered
  with blanket permission for commercial redistribution of all its art.

## Subaru Natsuki

- New original fan-mesh topology and clothing atlas, authored locally for this task;
  no geometry from the rejected box rig, downloaded OBJ or game-ripped MMD ports.
- Face paint adapted from the reference image the user supplied for this task.
- Authoring: `tools/characters/make_subaru.py`; editable intermediate:
  `artwork/characters/subaru.glb` and `subaru-author-atlas.png`.
- Existing animation/baking code remains under the project's GPL terms. No claim
  of ownership over Subaru/Re:Zero, Guts/Berserk or supplied reference artwork is made;
  their underlying rights remain with the respective holders.

## Investigated, NOT included

Topjej's public Subaru OBJ/PNG download (linked from
https://www.youtube.com/watch?v=-ty2ivarNkI) was inspected, but has only 303 source
vertices and no verified redistribution license. Neither that OBJ nor that PNG
is shipped. Other login-only or non-downloadable models were not extracted.

## Rebuild

With JDK 21 and Python numpy/Pillow/scipy:

1. `python tools/characters/make_subaru.py` reuses the saved face/clothing atlas.
2. `bash gradlew bakeCharacters` bakes the two supplied GLBs using the same
   `CharacterBake`/`BakedDesign` pipeline used for imported textured characters.
3. `python tools/characters/repair_normals.py guts`
4. `python tools/characters/paint_guts_face.py` then
   `python tools/characters/retouch_guts.py` applies the reference-guided hand-painted face.
5. `python tools/characters/render_preview.py guts 0` (also 35 degrees, Subaru).
6. `bash gradlew clean build` packages the final ANC files and tests them.

Steps 3–4 are required after rebaking Guts. Original private uploads/logs are not
included. Preview renders are CPU views of the actual ANC geometry/UV atlas,
not Minecraft screenshots and not AI-generated promises of a different model.
