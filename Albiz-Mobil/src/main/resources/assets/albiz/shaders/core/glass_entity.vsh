#version 330

#moj_import <minecraft:light.glsl>
#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler2;

out float sphericalVertexDistance;
out float cylindricalVertexDistance;
out vec4 vertexColor;
out vec4 lightMapColor;
out vec2 texCoord0;
out vec3 viewNormal;
out vec3 viewPos;
out float diffuse;

void main() {
    vec4 view = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * view;

    sphericalVertexDistance = fog_spherical_distance(Position);
    cylindricalVertexDistance = fog_cylindrical_distance(Position);
    vec2 light = minecraft_compute_light(Light0_Direction, Light1_Direction, Normal);
    diffuse = clamp(max(light.x, light.y), 0.0, 1.0);
    vertexColor = Color;
    lightMapColor = texelFetch(Sampler2, UV2 / 16, 0);
    texCoord0 = UV0;
    viewNormal = mat3(ModelViewMat) * Normal;
    viewPos = view.xyz;
}
