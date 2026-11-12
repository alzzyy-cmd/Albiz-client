#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec2 UV0;
in vec4 Color;

out vec2 faceUv;
out vec3 local;
flat out float face;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    faceUv = UV0;
    local = Color.rgb;
    face = floor(Color.a * 5.0 + 0.5);
}