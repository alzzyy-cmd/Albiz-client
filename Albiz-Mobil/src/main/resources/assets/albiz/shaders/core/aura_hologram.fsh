#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <albiz:sky_common.glsl>
#moj_import <albiz:aura_styles.glsl>

in vec2 faceUv;
in vec3 local;
flat in float face;

out vec4 fragColor;

void main() {
    fragColor = ys_hologram(faceUv, local, face, ColorModulator.x, ColorModulator.yzw);
}