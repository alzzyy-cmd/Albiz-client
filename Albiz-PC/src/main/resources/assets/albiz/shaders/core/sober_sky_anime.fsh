#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <albiz:sky_common.glsl>
#moj_import <albiz:sky_anime.glsl>

in vec3 skyDir;

out vec4 fragColor;

void main() {
    vec3 dir = normalize(skyDir);
    vec3 col = ys_anime(dir, ColorModulator.x) * ColorModulator.y;
    fragColor = vec4(ys_tonemap(col), 1.0);
}
