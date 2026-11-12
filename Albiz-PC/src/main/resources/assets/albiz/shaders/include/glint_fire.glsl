vec3 skyfx_glint(vec2 p, float t) {
	vec2 q = p * vec2(5.5, 4.0);

	vec2 warp = vec2(skyfx_fbm2(q * 0.8 + vec2(0.0, t * 1.1), 3), skyfx_fbm2(q * 0.8 + vec2(5.2, t * 1.3), 3)) - 0.5;
	float f = skyfx_fbm2(q + warp * 1.4 + vec2(0.0, t * 2.2), 4);
	float tongues = 0.55 + 0.45 * sin(q.x * 2.6 + warp.x * 4.0 + t * 0.7);
	float heat = smoothstep(0.38, 0.85, f * (0.8 + 0.35 * tongues));
	vec3 col = mix(vec3(0.55, 0.04, 0.00), vec3(1.00, 0.38, 0.03), smoothstep(0.0, 0.55, heat));
	col = mix(col, vec3(1.00, 0.82, 0.25), smoothstep(0.55, 0.85, heat));
	col = mix(col, vec3(1.00, 0.97, 0.80), smoothstep(0.85, 1.0, heat));
	col *= heat * 1.25;

	vec2 id;
	vec3 h;
	vec2 c = glint_cell(p + vec2(sin(p.y * 6.0 + t) * 0.03, t * 0.45), 0.2, id, h, 3.0);
	if (h.z < 0.35) {
		float life = fract(t * (0.5 + h.x) + h.y);
		vec2 e = c - (h.xy - 0.5) * 0.1;
		float ember = exp(-dot(e, e) / 0.0004) * (1.0 - life) * (0.6 + 0.4 * sin(t * 20.0 + h.z * 40.0));
		col += vec3(1.0, 0.65, 0.2) * ember * 1.4;
	}
	return col;
}
