# Restored binary assets

The source tree was delivered without fonts, images, sounds, models and native libraries.
The files below were recovered on 2026-11-12 from `Albiz_Client_1_0_mobile.jar`
(inside `anubis.zip` in the `alzzyy-cmd/Anubis` repository, SHA-256
`df6811a7f4f2cbd17a9e8fbee32622a9cca3411bc01ddfd783bc2d5d475b07d9`). That JAR is an **older
revision**: its assets live under `assets/anubis/` and were copied to `assets/albiz/` with the
same relative path. Existing files were never overwritten. Images come from a mobile build and
may be lower resolution than the PC originals.

`natives/android-arm64/libimgui-java64.so` was taken from the nested
`imgui-java-natives-linux-1.90.0.jar` of the same JAR. Its SHA-256 equals the value hard-coded
in `platform/AlbizPreLaunch.java`, so it is exactly the file the code expects.

Still missing (not in the old JAR): character models `guts`, `subaru`, `banana`, `cat`,
`robot`; the `fortnite_pack` item images; 115 of the 116 sound recordings;
`textures/gui/albiz-wolf-sword.png`; `textures/gui/dusk_backdrop_mobile.jpg`; anything else
that only the newer `Albiz-1.0-Friends/*/Albiz-Client.jar` contains. Running
`restore_resources.py` with that JAR restores everything (see `DERLEME-REHBERI.md`).

## Files copied (same list in both projects)

- `assets/albiz/characters/cj.anc`
- `assets/albiz/characters/neighbor.anc`
- `assets/albiz/fonts/geist-medium.ttf`
- `assets/albiz/fonts/geist-semibold.ttf`
- `assets/albiz/fonts/jersey10-regular.ttf`
- `assets/albiz/fonts/manrope-bold.ttf`
- `assets/albiz/fonts/manrope-medium.ttf`
- `assets/albiz/fonts/rajdhani-bold.ttf`
- `assets/albiz/fonts/rajdhani-semibold.ttf`
- `assets/albiz/fonts/silkscreen-bold.ttf`
- `assets/albiz/fonts/silkscreen-regular.ttf`
- `assets/albiz/icons/icon_128.png`
- `assets/albiz/icons/icon_16.png`
- `assets/albiz/icons/icon_256.png`
- `assets/albiz/icons/icon_32.png`
- `assets/albiz/icons/icon_48.png`
- `assets/albiz/icons/icon_64.png`
- `assets/albiz/sounds/spawner_notify.ogg`
- `assets/albiz/textures/cosmetic/headdress_base.png`
- `assets/albiz/textures/gui/donutbets_banner.png`
- `assets/albiz/textures/gui/dusk_backdrop.png`
- `assets/albiz/textures/gui/head.png`
- `assets/albiz/textures/gui/hud_emblem.png`
- `assets/albiz/textures/gui/logo.png`
- `assets/albiz/textures/gui/menu/bg_mid.png`
- `assets/albiz/textures/gui/menu/bg_near.png`
- `assets/albiz/textures/gui/menu/bg_sky.jpg`
- `assets/albiz/textures/gui/menu/bg_sun.png`
- `assets/albiz/textures/misc/jump_circle_logo.png`
- `assets/albiz/textures/misc/jump_circle_logo_glow.png`
- `assets/albiz/textures/misc/knife_base.png`
- `assets/albiz/textures/peep/anarchy.png`
- `assets/albiz/textures/peep/cake.png`
- `assets/albiz/textures/peep/heart.png`
- `assets/albiz/textures/peep/hellboy.png`
- `assets/albiz/textures/peep/horseshoe.png`
- `assets/albiz/textures/peep/mom.png`
- `assets/albiz/textures/peep/rip.png`
- `assets/albiz/textures/peep/rose.png`
- `assets/albiz/textures/peep/star.png`
- `assets/albiz/textures/peep/wave.png`
- `assets/albiz/textures/sky_emoji/angry.png`
- `assets/albiz/textures/sky_emoji/broken_heart.png`
- `assets/albiz/textures/sky_emoji/cool.png`
- `assets/albiz/textures/sky_emoji/cry.png`
- `assets/albiz/textures/sky_emoji/devil.png`
- `assets/albiz/textures/sky_emoji/fire.png`
- `assets/albiz/textures/sky_emoji/heart.png`
- `assets/albiz/textures/sky_emoji/hug.png`
- `assets/albiz/textures/sky_emoji/kiss.png`
- `assets/albiz/textures/sky_emoji/laugh.png`
- `assets/albiz/textures/sky_emoji/love.png`
- `assets/albiz/textures/sky_emoji/music.png`
- `assets/albiz/textures/sky_emoji/sad.png`
- `assets/albiz/textures/sky_emoji/shock.png`
- `assets/albiz/textures/sky_emoji/shy.png`
- `assets/albiz/textures/sky_emoji/sleep.png`
- `assets/albiz/textures/sky_emoji/star.png`
- `assets/albiz/textures/sky_emoji/think.png`
- `assets/albiz/natives/android-arm64/libimgui-java64.so`
