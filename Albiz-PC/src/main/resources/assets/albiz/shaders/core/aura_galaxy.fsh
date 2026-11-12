#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <albiz:sky_common.glsl>
#moj_import <albiz:aura_galaxy.glsl>

in vec3 viewDir;

out vec4 fragColor;

void main() {
    fragColor = ys_galaxyAura(normalize(viewDir), ColorModulator.x, ColorModulator.yzw);
}
