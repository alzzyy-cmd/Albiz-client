vec3 skyfx_glint(vec2 p, float t) {
	vec2 g = p * 4.5 + vec2(t * 0.15, -t * 0.55);
	vec2 cell = floor(g);
	vec2 d = abs(fract(g) - 0.5);
	vec2 dl = 0.5 - d;
	float line = max(exp(-dl.x * 26.0), exp(-dl.y * 26.0));
	float glow = max(exp(-dl.x * 7.0), exp(-dl.y * 7.0));
	float hue = 0.5 + 0.5 * sin(p.y * 3.0 + p.x * 1.5 + t * 0.8);
	vec3 neon = mix(vec3(1.00, 0.18, 0.75), vec3(0.15, 0.85, 1.00), hue);
	vec3 col = neon * (line * 1.0 + glow * 0.18);

	float py = fract(g.y * 0.25 + skyfx_hash12(vec2(cell.x, 2.0)) - t * 0.8);
	float pulseV = exp(-dl.x * 30.0) * exp(-pow((py - 0.5) * 6.0, 2.0)) * step(0.6, skyfx_hash12(vec2(cell.x, 9.0)));
	float pxx = fract(g.x * 0.25 + skyfx_hash12(vec2(cell.y, 4.0)) - t * 0.6);
	float pulseH = exp(-dl.y * 30.0) * exp(-pow((pxx - 0.5) * 6.0, 2.0)) * step(0.6, skyfx_hash12(vec2(cell.y, 5.0)));
	col += vec3(1.0, 0.95, 1.0) * (pulseV + pulseH) * 1.1;

	col += mix(vec3(0.35, 0.05, 0.30), vec3(0.40, 0.20, 0.05), hue) * 0.12;
	return col;
}
