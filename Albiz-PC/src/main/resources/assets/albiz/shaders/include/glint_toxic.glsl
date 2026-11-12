vec3 skyfx_glint(vec2 p, float t) {

	vec2 w = vec2(skyfx_noise2(p * 3.0 + vec2(0.0, t * 0.3)), skyfx_noise2(p * 3.0 + vec2(4.0, t * 0.25))) - 0.5;
	float goo = skyfx_fbm2(p * 3.5 + w * 0.8 + vec2(0.0, t * 0.18), 4);
	vec3 col = mix(vec3(0.05, 0.25, 0.0), vec3(0.35, 1.00, 0.08), smoothstep(0.35, 0.8, goo)) * smoothstep(0.25, 0.75, goo) * 0.65;
	col += vec3(0.75, 1.00, 0.25) * pow(smoothstep(0.62, 0.9, goo), 3.0) * 0.6;

	for (int layer = 0; layer < 2; layer++) {
		float fl = float(layer);
		vec2 id;
		vec3 h;
		float cellSize = 0.26 - fl * 0.09;
		vec2 c = glint_cell(p + vec2(fl * 0.3, t * (0.10 + fl * 0.05)), cellSize, id, h, 11.0 + fl);
		if (h.z > 0.5) continue;
		float life = fract(t * (0.35 + 0.4 * h.x) + h.y);
		float r = cellSize * (0.10 + 0.25 * life);
		vec2 q = c - (h.xy - 0.5) * cellSize * 0.3;
		float d = length(q);
		float ring = exp(-pow((d - r) / (r * 0.18 + 0.002), 2.0));
		float shine = exp(-dot(q - vec2(-r, -r) * 0.4, q - vec2(-r, -r) * 0.4) / (r * r * 0.04 + 1e-5));

		float pop = 1.0 - smoothstep(0.88, 1.0, life);
		col += (vec3(0.55, 1.00, 0.20) * ring * 0.9 + vec3(0.9, 1.0, 0.7) * shine * 0.6) * pop;
	}
	return col;
}
