vec3 skyfx_glint(vec2 p, float t) {
	const float COL_W = 0.10;
	const float ROW_H = 0.13;
	float column = floor(p.x / COL_W);
	vec3 ch = skyfx_hash33(vec3(column, 1.0, 5.0));
	float speed = 0.9 + 1.4 * ch.x;
	float loop = 2.5 + 2.0 * ch.y;
	float row = floor(p.y / ROW_H);

	float head = (t * speed / ROW_H) + ch.z * 100.0;
	float behind = mod(head - row, loop / ROW_H);
	float trail = exp(-behind * 0.22) * step(0.0, behind);

	vec2 cell = vec2(fract(p.x / COL_W), fract(p.y / ROW_H));
	vec2 bit = floor(cell * vec2(4.0, 6.0));
	float glyph = floor(t * (1.5 + 3.0 * ch.y) + skyfx_hash12(vec2(column, row)) * 10.0);
	float on = step(0.5, skyfx_hash13(vec3(bit + vec2(column * 4.0, row * 6.0), glyph)));
	float inside = step(0.12, cell.x) * step(cell.x, 0.88) * step(0.08, cell.y) * step(cell.y, 0.92);
	float pixel = on * inside;
	vec3 green = vec3(0.10, 1.00, 0.35);
	vec3 col = green * pixel * trail * 0.9;

	col += vec3(0.75, 1.00, 0.80) * pixel * exp(-behind * 2.5) * 0.8;

	col += green * 0.05 * trail;
	return col;
}
