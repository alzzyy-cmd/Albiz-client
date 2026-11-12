vec3 ys_bloodmoon(vec3 dir, float t) {
	float y = dir.y;
	vec3 zenith = vec3(0.016, 0.003, 0.008);
	vec3 middle = vec3(0.090, 0.010, 0.016);
	vec3 horizon = vec3(0.400, 0.055, 0.035);
	vec3 col = mix(horizon, middle, smoothstep(0.0, 0.22, y));
	col = mix(col, zenith, smoothstep(0.20, 0.90, y));
	if (y < 0.0) col = mix(horizon * 0.7, vec3(0.05, 0.005, 0.008), smoothstep(0.0, 0.4, -y));
	col += ys_stars(dir, t, 0.45) * vec3(1.0, 0.70, 0.65) * smoothstep(0.12, 0.6, y);

	vec3 moonDir = normalize(vec3(0.45, 0.40, -0.80));
	float radius = 0.215;
	float d = ys_rayPointDistance(dir, moonDir);
	float facing = dot(dir, moonDir);
	float px = max(fwidth(d), 1e-4);
	vec3 n;
	float hit = ys_sphere(dir, moonDir, radius, n);
	col += vec3(0.95, 0.12, 0.05) * exp(-max(d - radius, 0.0) * 9.0) * 0.35 * step(0.0, facing);
	col += vec3(0.80, 0.08, 0.04) * pow(max(facing, 0.0), 8.0) * 0.18;
	if (facing > 0.0) {
		vec3 light = normalize(vec3(-0.6, 0.35, 0.72));
		vec3 p = n * 3.2 + vec3(0.0, 0.0, t * 0.002);
		float rough = ys_fbm3(p, 5);
		float craters = 0.0;
		for (int k = 0; k < 3; k++) {
			vec3 cell = n * (4.0 + float(k) * 3.0);
			vec3 c = floor(cell);
			vec3 f = fract(cell) - 0.5;
			vec3 h = ys_hash33(c + float(k) * 13.0);
			float cd = length(f - (h - 0.5) * 0.5);
			float rad = 0.12 + 0.18 * h.x;
			craters += (smoothstep(rad, rad * 0.55, cd) * 0.55 - smoothstep(rad * 1.15, rad, cd) * 0.25) * step(0.45, h.y);
		}
		float lambert = clamp(dot(n, light) * 0.7 + 0.45, 0.0, 1.0);
		vec3 surface = mix(vec3(0.42, 0.05, 0.03), vec3(0.95, 0.24, 0.10), rough);
		surface *= (1.0 - craters * 0.55) * lambert;
		surface += vec3(1.0, 0.30, 0.12) * pow(1.0 - max(dot(n, -dir), 0.0), 3.0) * 0.35;
		float mask = 1.0 - smoothstep(radius - px, radius + px, d);
		col = mix(col, surface * 1.3, mask * step(0.0, hit + 1.0));
	}

	if (y > -0.05) {
		vec2 uv = dir.xz / (y + 0.18) * 0.7;
		uv += vec2(t * 0.010, t * 0.004);
		float base = ys_fbm2(uv * 1.3, 6);
		float wisp = ys_fbm2(uv * 3.1 + base * 1.7, 5);
		float density = smoothstep(0.42, 0.78, base * 0.75 + wisp * 0.45);
		density *= smoothstep(-0.05, 0.12, y) * (1.0 - smoothstep(0.55, 0.95, y));
		float lit = pow(max(facing, 0.0), 6.0);
		vec3 cloud = mix(vec3(0.030, 0.004, 0.006), vec3(0.55, 0.07, 0.04), lit * (1.0 - density) * 1.6);
		cloud += vec3(0.90, 0.12, 0.05) * lit * smoothstep(0.35, 0.0, abs(density - 0.35)) * 0.35;
		col = mix(col, cloud, density * 0.92);
	}

	float mist = ys_fbm2(vec2(atan(dir.z, dir.x) * 3.0 + t * 0.01, y * 20.0), 4);
	col += vec3(0.55, 0.06, 0.03) * exp(-abs(y) * 14.0) * (0.25 + 0.35 * mist);
	return col;
}
