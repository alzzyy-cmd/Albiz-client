vec3 ys_petaldusk(vec3 dir, float t) {
	float y = dir.y;
	float el = asin(clamp(y, -1.0, 1.0));
	float az = atan(dir.z, dir.x);
	vec3 zenith = vec3(0.280, 0.240, 0.560);
	vec3 middle = vec3(0.880, 0.520, 0.720);
	vec3 horizon = vec3(1.000, 0.740, 0.580);
	vec3 col = mix(horizon, middle, smoothstep(0.0, 0.25, y));
	col = mix(col, zenith, smoothstep(0.22, 0.95, y));
	if (y < 0.0) col = mix(horizon, vec3(0.62, 0.42, 0.62), smoothstep(0.0, 0.35, -y));
	col += ys_stars(dir, t, 0.35) * smoothstep(0.55, 0.95, y);

	vec3 sunDir = normalize(vec3(-0.70, 0.075, -0.70));
	float sd = max(dot(dir, sunDir), 0.0);
	col += vec3(1.00, 0.86, 0.62) * (smoothstep(0.99925, 0.9996, sd) * 1.3 + pow(sd, 120.0) * 0.45 + pow(sd, 10.0) * 0.18 + pow(sd, 3.0) * 0.08);

	if (y > -0.03) {
		vec2 uv = dir.xz / (y + 0.16) * 0.55;
		vec2 q = vec2(uv.x * 0.6, uv.y * 1.6) + vec2(t * 0.006, 0.0);
		float shape = ys_fbm2(q, 6);
		float detail = ys_fbm2(q * 2.7 + shape, 4);
		float density = smoothstep(0.48, 0.80, shape * 0.8 + detail * 0.35);
		density *= smoothstep(-0.03, 0.10, y) * (1.0 - smoothstep(0.45, 0.85, y));
		float glow = pow(sd, 4.0);
		vec3 shadow = vec3(0.72, 0.50, 0.74);
		vec3 lit = vec3(1.00, 0.80, 0.70);
		vec3 rim = vec3(1.00, 0.90, 0.55);
		vec3 cloud = mix(shadow, lit, smoothstep(0.3, 0.9, detail));
		cloud = mix(cloud, rim, glow * smoothstep(0.65, 0.25, density) * 0.9);
		col = mix(col, cloud, density * 0.85);
	}

	if (el > -0.05 && el < 0.85) {
		for (int k = 0; k < 3; k++) {
			float fk = float(k);
			float cells = 96.0 + fk * 56.0;
			vec2 q = vec2(az, el) * (cells / ys_TAU);
			q += vec2(t * (0.25 + fk * 0.08), t * (0.55 + fk * 0.15));
			vec2 cell = floor(q);
			vec2 f = fract(q) - 0.5;
			cell.x = mod(cell.x, cells);
			vec3 h = ys_hash33(vec3(cell, fk * 7.3));
			if (h.z > 0.16) continue;
			vec2 pp = f - (h.xy - 0.5) * 0.24;
			pp.x *= cos(el);
			float a = h.x * ys_TAU + t * (0.6 + h.y * 1.4);
			pp = ys_rot(a) * pp;
			float flutter = 0.55 + 0.45 * abs(sin(t * (1.5 + h.y * 2.5) + h.x * 9.0));
			float size = 0.19 + 0.06 * h.y;
			vec2 shape = vec2(pp.x / flutter, pp.y + 0.35 * size * (pp.x * pp.x) / (size * size)) / vec2(size * 0.62, size);
			float dd = length(shape);
			float aa = max(fwidth(dd), 1e-4) * 1.2;
			float petal = 1.0 - smoothstep(1.0 - aa, 1.0 + aa, dd);
			vec3 petalColor = mix(vec3(1.00, 0.86, 0.91), vec3(0.97, 0.60, 0.76), smoothstep(0.1, 1.0, dd + pp.y / size * 0.3));
			petalColor *= 0.9 + 0.2 * pow(sd, 2.0);
			float fade = (1.0 - smoothstep(0.42, 0.82, el)) * (0.55 + 0.45 * (1.0 - fk / 3.0));
			col = mix(col, petalColor, petal * fade);
		}
	}
	col += vec3(1.0, 0.70, 0.55) * exp(-abs(y) * 30.0) * 0.18;
	return col;
}
