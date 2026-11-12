vec3 ys_ehBackground(vec3 dir, float t) {
	vec3 col = vec3(0.004, 0.003, 0.012);
	float neb = ys_fbm3(dir * 2.2 + vec3(0.0, 0.0, t * 0.004), 5);
	float wisp = ys_fbm3(dir * 5.0 + neb * 1.6, 4);
	col += vec3(0.20, 0.06, 0.36) * smoothstep(0.42, 0.85, neb);
	col += vec3(0.05, 0.20, 0.36) * smoothstep(0.48, 0.90, wisp) * 0.8;
	col += vec3(0.45, 0.20, 0.55) * pow(smoothstep(0.62, 0.95, neb * wisp * 1.9), 2.0) * 0.5;
	col += ys_stars(dir, t, 1.0);
	return col;
}

vec3 ys_eventhorizon(vec3 dir, float t) {
	vec3 center = normalize(vec3(-0.32, 0.36, -0.88));
	float shadow = 0.11;
	vec3 e1 = normalize(cross(center, vec3(0.0, 1.0, 0.0)));
	vec3 e2 = cross(e1, center);
	vec2 p = ys_rot(0.14) * vec2(dot(dir, e1), dot(dir, e2));
	float facing = dot(dir, center);
	float r = length(p);
	float px = max(fwidth(r), 1e-4);

	vec3 bent = dir;
	if (facing > 0.0 && r > 1e-4) {
		vec2 q = ys_rot(-0.14) * p;
		vec3 away = normalize(q.x * e1 + q.y * e2);
		float deflect = 0.010 / max(r, shadow * 0.6) * smoothstep(0.6, 0.0, r);
		bent = normalize(dir - away * deflect);
	}
	vec3 col = ys_ehBackground(bent, t);
	if (facing <= 0.0) return col;

	float ring = exp(-pow((r - shadow * 1.18) / (shadow * 0.07), 2.0));
	col += vec3(1.0, 0.72, 0.42) * ring * 1.6;
	col += vec3(1.0, 0.45, 0.15) * exp(-max(r - shadow, 0.0) / (shadow * 0.9)) * 0.35;

	float tilt = 0.24;
	vec2 dp = vec2(p.x, p.y / tilt);
	float dr = length(dp);
	float inner = shadow * 1.45, outer = shadow * 3.6;
	float band = smoothstep(inner - px * 3.0, inner + shadow * 0.15, dr) * (1.0 - smoothstep(outer - shadow * 0.8, outer, dr));
	float ang = atan(dp.y, dp.x);
	float swirl = ys_fbm2(vec2(ang * 3.0 - t * 0.35 + dr * 40.0, dr * 25.0), 5);
	float doppler = 0.55 + 0.45 * cos(ang);
	float heat = 1.0 - smoothstep(inner, outer, dr);
	vec3 disk = mix(vec3(0.85, 0.18, 0.04), vec3(1.0, 0.86, 0.55), heat);
	disk *= (0.55 + 0.9 * swirl) * (0.45 + 0.85 * doppler) * 1.4;
	float diskAlpha = band * (0.85 + 0.15 * swirl);

	float pa = atan(p.y, p.x);
	float lr = (r - shadow * 1.38) / (shadow * 0.26);
	float lensBand = exp(-lr * lr * 1.6) * smoothstep(shadow * 1.02, shadow * 1.12, r);
	float lensSwirl = ys_fbm2(vec2(pa * 4.0 + t * 0.3, r * 60.0), 4);
	float lensSide = 0.35 + 0.65 * abs(sin(pa));
	vec3 lensCol = mix(vec3(1.0, 0.42, 0.10), vec3(1.0, 0.90, 0.68), exp(-lr * lr * 4.0));
	col += lensCol * lensBand * lensSide * (0.45 + 0.8 * lensSwirl) * 1.1;

	if (p.y > 0.0) col = mix(col, disk, diskAlpha);
	float inside = 1.0 - smoothstep(shadow - px, shadow + px, r);
	col = mix(col, vec3(0.0), inside);
	if (p.y <= 0.0) col = mix(col, disk, diskAlpha);
	return col;
}
