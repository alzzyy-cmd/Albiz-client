vec2 glint_cell(vec2 p, float cellSize, out vec2 id, out vec3 h, float seed) {
	vec2 g = p / cellSize;
	id = floor(g);
	h = skyfx_hash33(vec3(id, seed));
	return (fract(g) - 0.5) * cellSize;
}

float glint_sparkle(vec2 q, float size) {
	vec2 a = abs(q) / size;
	float cross = exp(-a.x * 7.0) * exp(-a.y * 1.2) + exp(-a.y * 7.0) * exp(-a.x * 1.2);
	return cross * 0.6 + exp(-dot(a, a) * 6.0);
}
