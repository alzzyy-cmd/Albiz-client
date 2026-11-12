vec3 skyfx_glint(vec2 p, float t) {
	vec3 col = vec3(0.0);

	vec2 sw = skyfx_rot(t * 0.15) * p;
	float mist = skyfx_fbm2(sw * 2.2 + vec2(t * 0.1, -t * 0.07), 4);
	col += vec3(0.30, 0.06, 0.55) * smoothstep(0.35, 0.85, mist) * 0.55;

	for (int i = 0; i < 5; i++) {
		float fi = float(i);
		vec2 q = skyfx_rot(fi * 1.1 + 0.3) * p * (1.4 + fi * 0.55) + vec2(t * (0.03 + fi * 0.012), fi * 3.7);
		vec2 id = floor(q * 6.0);
		vec2 f = fract(q * 6.0) - 0.5;
		vec3 h = skyfx_hash33(vec3(id, fi + 40.0));
		if (h.z > 0.45) continue;
		vec2 o = f - (h.xy - 0.5) * 0.6;
		float speck = exp(-dot(o, o) * (90.0 + fi * 30.0));
		vec3 c = mix(vec3(0.10, 0.85, 0.70), vec3(0.55, 0.25, 1.00), fract(h.x * 3.0 + fi * 0.27));
		col += c * speck * (0.9 - fi * 0.12) * (0.6 + 0.4 * sin(t * 3.0 + h.y * 20.0));
	}

	vec2 id;
	vec3 h;
	vec2 c = glint_cell(p + vec2(0.0, t * 0.12), 0.25, id, h, 9.0);
	if (h.z < 0.3) {
		vec2 q = abs(c - (h.xy - 0.5) * 0.12);
		float sq = (1.0 - smoothstep(0.016, 0.024, max(q.x, q.y))) * (0.5 + 0.5 * sin(t * 4.0 + h.x * 30.0));
		col += vec3(0.85, 0.40, 1.00) * sq;
	}
	return col;
}
