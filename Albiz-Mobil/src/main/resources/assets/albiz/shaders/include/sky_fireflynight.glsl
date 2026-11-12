vec3 ys_fireflynight(vec3 dir, float t) {
	float y = dir.y;
	float el = asin(clamp(y, -1.0, 1.0));
	float az = atan(dir.z, dir.x);
	vec3 zenith = vec3(0.008, 0.016, 0.055);
	vec3 middle = vec3(0.020, 0.050, 0.110);
	vec3 horizon = vec3(0.045, 0.110, 0.140);
	vec3 col = mix(horizon, middle, smoothstep(0.0, 0.25, y));
	col = mix(col, zenith, smoothstep(0.22, 0.9, y));
	if (y < 0.0) col = mix(horizon, vec3(0.01, 0.03, 0.04), smoothstep(0.0, 0.3, -y));
	col += vec3(0.05, 0.20, 0.12) * exp(-abs(y) * 9.0) * 0.35;

	vec3 bandAxis = normalize(vec3(0.35, 0.25, 0.9));
	float band = exp(-pow(dot(dir, bandAxis) / 0.22, 2.0));
	float dustLane = ys_fbm3(dir * 6.0, 5);
	col += vec3(0.20, 0.22, 0.36) * band * smoothstep(0.3, 0.8, dustLane) * 0.45 * smoothstep(0.05, 0.4, y);
	col += ys_stars(dir, t, 1.0) * smoothstep(0.02, 0.3, y) * (1.0 + band);

	vec3 moonDir = normalize(vec3(-0.55, 0.45, -0.70));
	float radius = 0.06;
	float d = ys_rayPointDistance(dir, moonDir);
	float facing = dot(dir, moonDir);
	float px = max(fwidth(d), 1e-4);
	if (facing > 0.0) {
		vec3 m1 = normalize(cross(moonDir, vec3(0.0, 1.0, 0.0)));
		vec3 shadowDir = normalize(moonDir + m1 * 0.045 + vec3(0.0, 0.02, 0.0));
		float ds = ys_rayPointDistance(dir, shadowDir);
		float lit = (1.0 - smoothstep(radius - px, radius + px, d)) * smoothstep(radius - px, radius + px * 2.0, ds);
		col += vec3(0.95, 0.92, 0.75) * exp(-max(d - radius, 0.0) * 18.0) * 0.18;
		col = mix(col, vec3(1.0, 0.97, 0.84), lit);
	}

	if (el > -0.04 && el < 0.42) {
		for (int k = 0; k < 2; k++) {
			float fk = float(k);
			float scale = 15.0 + fk * 9.0;
			vec2 q = vec2(az * cos(el) * scale, el * scale * 1.5);
			vec2 cell = floor(q);
			vec2 f = fract(q) - 0.5;
			vec3 h = ys_hash33(vec3(cell, fk * 5.1 + 2.0));
			if (h.z > 0.22) continue;
			vec2 wander = vec2(sin(t * (0.4 + h.x * 0.6) + h.y * 6.28), cos(t * (0.3 + h.y * 0.5) + h.x * 6.28)) * 0.13;
			vec2 pp = f - (h.xy - 0.5) * 0.24 - wander;
			float dd = length(pp);
			float pulse = 0.15 + 0.85 * pow(0.5 + 0.5 * sin(t * (1.0 + h.z * 4.0) + h.x * 20.0), 3.0);
			float glow = exp(-dd * dd / 0.0025) + exp(-dd * dd / 0.012) * 0.35;
			float fade = 1.0 - smoothstep(0.18, 0.42, el);
			vec3 tint = mix(vec3(0.70, 1.0, 0.30), vec3(1.0, 0.92, 0.35), h.y);
			col += tint * glow * pulse * fade * (1.25 - fk * 0.45);
		}
	}

	float mist = ys_fbm2(vec2(az * 3.0 + t * 0.01, y * 25.0), 4);
	col += vec3(0.10, 0.18, 0.20) * exp(-abs(y) * 14.0) * mist * 0.45;
	return col;
}
