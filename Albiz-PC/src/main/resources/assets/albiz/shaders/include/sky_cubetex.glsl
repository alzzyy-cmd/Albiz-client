vec3 ys_cubeSky(sampler2D atlas, vec3 dir, float t) {
    dir = ys_rotY(dir, 3.14159265 + t * 0.004);
    vec3 a = abs(dir);
    vec2 uv;
    int cell;
    if (a.y >= a.x && a.y >= a.z) {
        if (dir.y > 0.0) {
            cell = 1;
            uv = vec2(0.5 - 0.5 * dir.x / a.y, 0.5 + 0.5 * dir.z / a.y);
        } else {
            cell = 0;
            uv = vec2(0.5 - 0.5 * dir.x / a.y, 0.5 - 0.5 * dir.z / a.y);
        }
    } else if (a.x >= a.z) {
        if (dir.x > 0.0) {
            cell = 3;
            uv = vec2(0.5 + 0.5 * dir.z / a.x, 0.5 - 0.5 * dir.y / a.x);
        } else {
            cell = 5;
            uv = vec2(0.5 - 0.5 * dir.z / a.x, 0.5 - 0.5 * dir.y / a.x);
        }
    } else {
        if (dir.z < 0.0) {
            cell = 2;
            uv = vec2(0.5 + 0.5 * dir.x / a.z, 0.5 - 0.5 * dir.y / a.z);
        } else {
            cell = 4;
            uv = vec2(0.5 - 0.5 * dir.x / a.z, 0.5 - 0.5 * dir.y / a.z);
        }
    }
    float faceSize = float(textureSize(atlas, 0).x) / 3.0;
    uv = clamp(uv, vec2(0.75 / faceSize), vec2(1.0 - 0.75 / faceSize));
    vec2 origin = vec2(float(cell - (cell / 3) * 3), float(cell / 3));
    return texture(atlas, (origin + uv) / vec2(3.0, 2.0)).rgb;
}
