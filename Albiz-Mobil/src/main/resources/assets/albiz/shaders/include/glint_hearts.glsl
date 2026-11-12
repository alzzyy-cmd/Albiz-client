float glint_heart(vec2 q) {
	q.x = abs(q.x);
	if (q.y + q.x > 1.0) {
		return length(q - vec2(0.25, 0.75)) - 0.35355339;
	}
	vec2 a = q - vec2(0.0, 1.0);
	vec2 b = q - 0.5 * max(q.x + q.y, 0.0);
	return sqrt(min(dot(a, a), dot(b, b))) * sign(q.x - q.y);
}

vec3 skyfx_glint(vec2 p, float t) {
	vec3 col = vec3(1.0, 0.25, 0.55) * 0.07 * (0.6 + 0.4 * skyfx_noise2(p * 3.0 + vec2(0.0, t * 0.3)));
	for (int layer = 0; layer < 2; layer++) {
		float fl = float(layer);
		vec2 id;
		vec3 h;
		float cellSize = 0.40 - fl * 0.14;

		vec2 c = glint_cell(p + vec2(fl * 0.41, t * (0.16 + fl * 0.06)), cellSize, id, h, 21.0 + fl);
		if (h.z > 0.45) continue;
		float beat = 1.0 + 0.12 * pow(max(sin(t * 6.0 + h.x * SKYFX_TAU), 0.0), 8.0);
		float size = cellSize * (0.28 + 0.12 * h.y) * beat;
		vec2 q = c - (h.xy - 0.5) * cellSize * 0.25 - vec2(sin(t * 1.5 + h.x * 9.0) * cellSize * 0.08, 0.0);
		q = skyfx_rot((h.y - 0.5) * 0.6) * q;

		float d = glint_heart(vec2(q.x, -q.y) / size + vec2(0.0, 0.5)) * size;
		float heart = 1.0 - smoothstep(-size * 0.06, size * 0.06, d);
		float rim = exp(-abs(d) / (size * 0.08));
		vec3 hc = mix(vec3(1.00, 0.30, 0.60), vec3(1.00, 0.12, 0.25), h.x);
		col = mix(col, hc * (0.85 - fl * 0.2), heart);
		col += vec3(1.0, 0.75, 0.88) * rim * 0.25;

		vec2 hl = q - vec2(-0.25, -0.30) * size;
		col += vec3(1.0, 0.9, 0.95) * exp(-dot(hl, hl) / (size * size * 0.012)) * heart * 0.7;
	}

	vec2 id;
	vec3 h;
	vec2 c = glint_cell(p, 0.3, id, h, 29.0);
	if (h.z < 0.25) {
		float tw = pow(max(sin(t * 3.0 + h.y * SKYFX_TAU), 0.0), 8.0);
		col += vec3(1.0, 0.85, 0.95) * glint_sparkle(c - (h.xy - 0.5) * 0.14, 0.035) * tw;
	}
	return col;
}
