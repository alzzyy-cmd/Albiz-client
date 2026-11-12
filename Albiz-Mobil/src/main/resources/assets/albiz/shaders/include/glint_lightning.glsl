vec3 skyfx_glint(vec2 p, float t) {
	vec3 col = vec3(0.0);
	float tick = floor(t * 9.0);
	for (int layer = 0; layer < 2; layer++) {
		float fl = float(layer);
		vec2 q = p * (2.4 + fl * 1.3) + vec2(fl * 3.1, fl * 1.7);

		float seed = tick + fl * 17.0;
		vec2 warp = vec2(skyfx_noise2(q * 3.0 + seed * 1.37), skyfx_noise2(q * 3.0 + 7.0 + seed * 0.61)) - 0.5;
		float n = skyfx_fbm2(q + warp * 0.35 + vec2(t * 0.15, 0.0), 4);
		float ridge = abs(n - 0.5);
		float bolt = exp(-ridge * 70.0);
		float halo = exp(-ridge * 14.0);

		float on = smoothstep(0.35, 0.55, skyfx_noise2(q * 0.7 + vec2(seed * 3.7, seed * 1.3)));
		col += (vec3(0.80, 0.92, 1.00) * bolt * 1.3 + vec3(0.25, 0.45, 1.00) * halo * 0.35) * on;
	}

	float flash = step(0.93, skyfx_hash12(vec2(tick, 3.0)));
	col += vec3(0.15, 0.25, 0.70) * (0.10 + 0.25 * flash) * skyfx_noise2(p * 6.0 + tick);
	return col;
}
