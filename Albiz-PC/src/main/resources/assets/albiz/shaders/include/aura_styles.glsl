float ys_faceEdge(vec2 uv) {
	vec2 e = min(uv, 1.0 - uv);
	return min(e.x, e.y);
}

vec2 ys_voronoiEdge(vec2 p) {
	vec2 i = floor(p);
	vec2 f = fract(p);
	float d1 = 8.0;
	float d2 = 8.0;
	for (int y = -1; y <= 1; y++) {
		for (int x = -1; x <= 1; x++) {
			vec2 o = vec2(float(x), float(y));
			vec2 r = o + ys_hash33(vec3(i + o, 4.7)).xy - f;
			float d = dot(r, r);
			if (d < d1) {
				d2 = d1;
				d1 = d;
			} else if (d < d2) {
				d2 = d;
			}
		}
	}
	return vec2(sqrt(d1), sqrt(d2) - sqrt(d1));
}

vec4 ys_hologram(vec2 uv, vec3 local, float face, float t, vec3 tint) {
	float e = ys_faceEdge(uv);
	float flicker = 0.92 + 0.08 * ys_noise2(vec2(t * 17.0, face * 3.0));
	float scan = smoothstep(0.3, 1.0, 0.5 + 0.5 * sin(local.y * 150.0 - t * 6.0));
	float sweepPos = fract(t * 0.32) * 1.4 - 0.2;
	float dy = local.y - sweepPos;
	float sweep = exp(-dy * dy / 0.0010);
	float trail = dy < 0.0 ? exp(dy * 7.0) : 0.0;
	vec2 cell = fract(uv * 8.0) - 0.5;
	float dots = 1.0 - smoothstep(0.035, 0.075, length(cell));
	float rim = exp(-e * 12.0);
	float glitch = step(0.985, ys_noise2(vec2(floor(local.y * 40.0), floor(t * 9.0)))) * 0.6;
	vec3 bright = mix(tint, vec3(1.0), 0.55);
	vec3 col = tint * (0.75 + 0.45 * scan) + bright * sweep * 0.8 + tint * trail * 0.35 + bright * rim * 0.6 + bright * dots * 0.5 + tint * glitch;
	float alpha = (0.26 + 0.12 * scan + 0.55 * sweep + 0.22 * trail + 0.55 * rim + 0.35 * dots + glitch * 0.4) * flicker;
	return vec4(col, clamp(alpha, 0.0, 0.85));
}

vec4 ys_opal(vec2 uv, vec3 local, float face, float t, vec3 tint) {
	float e = ys_faceEdge(uv);
	vec3 milk = mix(vec3(0.93, 0.91, 1.0), tint, 0.25);
	vec2 p = uv * 4.5 + face * 3.7;
	p += vec2(ys_noise2(p * 0.8 + t * 0.1), ys_noise2(p * 0.8 - t * 0.1 + 5.0)) * 0.5;
	vec2 i = floor(p);
	vec2 f = fract(p);
	float best = 8.0;
	vec3 cell = vec3(0.0);
	for (int y = -1; y <= 1; y++) {
		for (int x = -1; x <= 1; x++) {
			vec2 o = vec2(float(x), float(y));
			vec3 h = ys_hash33(vec3(i + o, face + 8.0));
			vec2 r = o + h.xy - f;
			float d = dot(r, r);
			if (d < best) {
				best = d;
				cell = h;
			}
		}
	}
	float hue = fract(cell.z + dot(local, vec3(0.3, 0.5, 0.25)) + t * 0.05);
	float flash = pow(0.5 + 0.5 * sin(t * 1.4 + cell.x * 6.28 + dot(local, vec3(3.0, 2.0, 4.0))), 1.5);
	vec3 fire = ys_hsv2rgb(vec3(hue, 0.72, 1.0));
	float patchMask = smoothstep(0.85, 0.2, sqrt(best)) * (0.3 + 0.7 * flash);
	float sweepPos = fract(t * 0.22) * 3.0 - 1.0;
	float s = (uv.x + uv.y) * 0.5 - sweepPos;
	float shine = exp(-s * s / 0.003) + exp(-(s + 0.08) * (s + 0.08) / 0.0005) * 0.6;
	float rim = exp(-e * 16.0);
	vec3 col = mix(milk, fire, patchMask * 0.8) + vec3(1.0) * shine * 0.7 + mix(tint, vec3(1.0), 0.6) * rim * 0.5;
	float alpha = 0.24 + patchMask * 0.42 + shine * 0.40 + rim * 0.45;
	return vec4(col, clamp(alpha, 0.0, 0.85));
}

vec4 ys_molten(vec2 uv, vec3 local, float face, float t, vec3 tint) {
	float e = ys_faceEdge(uv);
	vec2 p = uv * 3.2 + face * 5.17;
	p += vec2(ys_fbm2(p * 1.3 + t * 0.05, 3), ys_fbm2(p * 1.3 - t * 0.05 + 9.0, 3)) * 0.35;
	vec2 v = ys_voronoiEdge(p);
	float crack = 1.0 - smoothstep(0.0, 0.08, v.y);
	float halo = exp(-v.y * 9.0);
	float heat = 0.55 + 0.45 * sin(t * 1.6 + ys_noise2(p * 0.7) * 6.28);
	vec3 hot = mix(vec3(1.0, 0.26, 0.04), vec3(1.0, 0.86, 0.38), crack * heat);
	hot = mix(hot, tint, 0.2);
	vec3 rock = vec3(0.10, 0.035, 0.025) * (0.7 + 0.6 * ys_fbm2(uv * 10.0 + face, 3));
	float rim = exp(-e * 20.0);
	vec3 col = rock + hot * (crack * 1.4 + halo * 0.45) * (0.7 + 0.3 * heat) + vec3(1.0, 0.45, 0.12) * rim * 0.6;
	float alpha = 0.36 + crack * 0.55 + halo * 0.15 + rim * 0.3;
	return vec4(col, clamp(alpha, 0.0, 0.92));
}
