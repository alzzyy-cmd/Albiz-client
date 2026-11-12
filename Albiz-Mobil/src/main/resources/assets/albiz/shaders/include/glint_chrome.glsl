vec3 skyfx_glint(vec2 p, float t) {
	vec2 screen = gl_FragCoord.xy / max(ScreenSize, vec2(1.0));
	float h = skyfx_fbm2(p * 0.9 + vec2(t * 0.04, t * 0.03), 2) * 0.8 + dot(p, vec2(0.45, 0.30));
	float phase = h * 1.6 + screen.x * 0.8 + screen.y * 0.5 + t * 0.06;

	vec3 film = 0.5 + 0.5 * cos(SKYFX_TAU * (phase + vec3(0.0, 0.33, 0.67)));

	float bands = 0.5 + 0.5 * sin(h * 10.0 + screen.y * 4.0 - t * 0.5);
	vec3 col = film * film * (0.22 + 0.25 * bands);
	col += vec3(0.95, 0.97, 1.0) * pow(bands, 7.0) * 0.40;
	return col;
}
