vec3 ys_candyclouds(vec3 dir, float t) {
	float y = dir.y;
	vec3 sunDir = normalize(vec3(0.35, 0.30, 0.88));
	vec3 zenith = vec3(0.50, 0.72, 1.00);
	vec3 middle = vec3(0.88, 0.78, 0.98);
	vec3 horizon = vec3(1.00, 0.86, 0.90);
	vec3 col = mix(horizon, middle, smoothstep(0.0, 0.25, y));
	col = mix(col, zenith, smoothstep(0.22, 0.9, y));
	if (y < 0.0) col = mix(horizon, vec3(0.86, 0.80, 0.98), smoothstep(0.0, 0.3, -y));

	float sd = max(dot(dir, sunDir), 0.0);
	col += vec3(1.0, 0.97, 0.92) * (pow(sd, 260.0) * 0.9 + pow(sd, 16.0) * 0.16);

	float rang = acos(clamp(dot(dir, -sunDir), -1.0, 1.0));
	float rb = (rang - 0.733) / 0.05;
	if (abs(rb) < 1.0 && y > -0.02) {
		float hue = (rb * 0.5 + 0.5) * 0.78;
		vec3 bow = ys_hsv2rgb(vec3(hue, 0.42, 1.0));
		float edge = smoothstep(1.0, 0.55, abs(rb));
		col = mix(col, bow, edge * 0.55 * smoothstep(-0.02, 0.06, y));
	}
	col += vec3(1.0, 0.95, 1.0) * smoothstep(0.733, 0.60, rang) * 0.05 * step(-0.02, y);

	if (y > -0.02) {
		vec2 uv = dir.xz / (y + 0.24) * 0.85 + vec2(t * 0.008, t * 0.003);
		float base = ys_fbm2(uv * 0.8, 6);
		float puffs = ys_fbm2(uv * 2.2 + base * 1.4, 5);
		float shape = base * 0.72 + puffs * 0.42;
		float density = smoothstep(0.52, 0.64, shape);
		density *= smoothstep(-0.02, 0.10, y) * (1.0 - smoothstep(0.55, 0.9, y));
		vec2 toSun = normalize(sunDir.xz + 1e-4) * 0.12;
		float behind = ys_fbm2((uv + toSun) * 0.8, 6) * 0.72 + ys_fbm2((uv + toSun) * 2.2 + base * 1.4, 5) * 0.42;
		float shade = clamp(0.6 + (shape - behind) * 5.0, 0.0, 1.0);
		float tint = ys_noise2(uv * 0.5 + 3.0);
		vec3 deep = mix(vec3(0.86, 0.66, 0.92), vec3(0.70, 0.76, 1.00), smoothstep(0.4, 0.6, tint));
		vec3 body = mix(vec3(1.00, 0.76, 0.90), vec3(0.80, 0.88, 1.00), smoothstep(0.4, 0.6, tint));
		vec3 cotton = mix(deep, body, smoothstep(0.1, 0.6, shade));
		cotton = mix(cotton, vec3(1.0, 0.97, 0.98), smoothstep(0.65, 1.0, shade) * 0.8);
		float rim = smoothstep(0.52, 0.56, shape) * (1.0 - smoothstep(0.56, 0.62, shape));
		cotton += vec3(1.0, 0.95, 0.98) * rim * pow(sd, 2.0) * 0.5;
		col = mix(col, cotton, density * 0.96);
	}

	vec3 sparkle = ys_starLayer(dir, 140.0, 0.12, t * 1.8, 1.0);
	col += sparkle * vec3(1.0, 0.85, 0.95) * smoothstep(0.25, 0.7, y) * 0.8;
	return col;
}
