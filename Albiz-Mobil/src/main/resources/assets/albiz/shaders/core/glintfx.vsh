#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec2 UV0;

out float sphericalVertexDistance;
out float cylindricalVertexDistance;
out vec2 texCoord0;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    sphericalVertexDistance = fog_spherical_distance(Position);
    cylindricalVertexDistance = fog_cylindrical_distance(Position);
    float scale = length(TextureMat[0].xy);
    vec2 uv = scale > 0.3 ? UV0 : UV0 * vec2(1.0, 0.5);
    float k = scale > 4.0 ? 0.125 : (scale > 0.3 ? 6.0 : 25.0);
    texCoord0 = (mat2(TextureMat) * uv) * k;
}
