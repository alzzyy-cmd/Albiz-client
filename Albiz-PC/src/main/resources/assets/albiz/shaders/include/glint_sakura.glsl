vec4 glint_petal(vec2 p, float t, float cellSize, float seed) {
	vec2 id;
	vec3 h;
	vec2 drift = vec2(-t * 0.07, -t * 0.16);
	vec2 c = glint_cell(p + drift, cellSize, id, h, seed);
	if (h.z > 0.55) return vec4(0.0);
	float phase = h.x * SKYFX_TAU;
	vec2 q = c - (h.xy - 0.5) * cellSize * 0.3 - vec2(sin(t * 1.4 + phase) * cellSize * 0.1, 0.0);
	q = skyfx_rot(phase + t * (0.8 + h.y)) * q;
	float flip = 0.3 + 0.7 * abs(cos(t * (1.2 + h.z * 2.0) + phase));
	q.x /= flip;
	float size = cellSize * (0.17 + 0.07 * h.y);
	vec2 e = q / vec2(size * 0.62 * (0.78 + 0.32 * clamp(-q.y / size, -1.0, 1.0)), size);
	float d = (length(e) - 1.0) * size * 0.6;
	float notch = -q.y - size * 0.72 - abs(q.x) * 1.4;
	d = max(d, notch * 0.7);
	float cover = 1.0 - smoothstep(-size * 0.08, size * 0.08, d);
	vec3 col = mix(vec3(1.00, 0.90, 0.95), vec3(1.00, 0.45, 0.68), smoothstep(0.1, 1.0, length(e)));
	return vec4(col * (0.75 + 0.25 * flip), cover);
}

vec3 skyfx_glint(vec2 p, float t) {
	vec3 col = vec3(1.0, 0.35, 0.60) * 0.10 * (0.6 + 0.4 * skyfx_noise2(p * 4.0 + vec2(t * 0.2, 0.0)));
	vec4 a = glint_petal(p, t, 0.45, 1.0);
	vec4 b = glint_petal(p + vec2(0.37, 0.11), t * 1.3, 0.30, 2.0);
	col = mix(col, b.rgb * 0.8, b.a);
	col = mix(col, a.rgb, a.a);
	return col;
}
