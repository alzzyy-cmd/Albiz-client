vec3 skyfx_glint(vec2 p, float t) {

	float brushed = skyfx_noise2(vec2(p.x * 3.0, p.y * 40.0)) * 0.5 + skyfx_noise2(p * 7.0) * 0.5;
	vec3 col = vec3(0.32, 0.20, 0.04) * (0.55 + 0.45 * brushed);

	float x = fract((dot(p, normalize(vec2(1.0, 0.55))) - t * 0.55) / 1.1);
	float gloss = exp(-pow((x - 0.5) / 0.045, 2.0)) + 0.35 * exp(-pow((x - 0.43) / 0.02, 2.0));
	col += vec3(1.00, 0.86, 0.48) * gloss * 1.1;

	vec2 id;
	vec3 h;
	vec2 c = glint_cell(p, 0.3, id, h, 7.0);
	if (h.z < 0.35) {
		float tw = pow(max(sin(t * (2.0 + h.x * 3.0) + h.y * SKYFX_TAU), 0.0), 6.0);
		vec2 q = c - (h.xy - 0.5) * 0.14;
		col += vec3(1.0, 0.92, 0.65) * glint_sparkle(q, 0.035 + 0.03 * h.y) * tw * 1.2;
	}
	return col;
}
