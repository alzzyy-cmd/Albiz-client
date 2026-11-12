const float SW_SUN_AZ = -1.5707963;
const float SW_SUN_EL = 0.17;
const float SW_SUN_R = 0.30;

float sw_wrap(float a) {
	return a - SKYFX_TAU * floor((a + SKYFX_PI) / SKYFX_TAU);
}

float sw_mountains(float az, float seed, float freq) {
	vec2 c = vec2(cos(az), sin(az)) * freq + vec2(seed, seed * 0.7);
	float body = skyfx_fbm2(c, 5);
	float peaks = 1.0 - abs(2.0 * skyfx_noise2(c * 2.3 + 11.0) - 1.0);
	float m = smoothstep(0.25, 0.9, body * 0.7 + peaks * 0.4);
	return m * m * (1.6 - 0.6 * m);
}

float sw_segment(vec2 p, vec2 a, vec2 b, out float h) {
	vec2 pa = p - a;
	vec2 ba = b - a;
	h = clamp(dot(pa, ba) / dot(ba, ba), 0.0, 1.0);
	return length(pa - ba * h);
}

float sw_palm(vec2 q, float t, float seed, float lean, float aa) {
	const float H = 0.34;
	if (q.y < -0.03 || q.y > H + 0.13 || q.x < -0.27 || q.x > 0.27 + lean * H) return 0.0;
	float s = clamp(q.y / H, 0.0, 1.0);
	float d = 1.0;
	if (q.y <= H) {
		float cx = lean * s * s * H;

		float w = mix(0.012, 0.0065, s) * (0.92 + 0.08 * smoothstep(0.3, 0.7, abs(fract(s * 14.0) - 0.5) * 2.0));
		d = abs(q.x - cx) - w;
	}
	vec2 top = vec2(lean * H, H);
	float sway = sin(t * 0.9 + seed) * 0.035;
	for (int k = 0; k < 8; k++) {
		float fk = float(k);
		float ang = mix(-0.30, SKYFX_PI + 0.30, fk / 7.0) + sway + sin(t * 1.7 + fk * 2.1 + seed) * 0.02;
		vec2 dirk = vec2(cos(ang), sin(ang));
		float len = 0.12 + 0.035 * fract(sin(fk * 12.9898 + seed) * 43758.5453);
		float droop = 0.9 + 0.5 * sin(fk * 1.3 + seed);
		vec2 prev = top;
		for (int j = 1; j <= 6; j++) {
			float sj = float(j) / 6.0;
			vec2 cur = top + dirk * (len * sj) - vec2(0.0, droop * len * sj * sj * 0.55);
			float h;
			float dd = sw_segment(q, prev, cur, h);
			float along = (float(j - 1) + h) / 6.0;

			float width = 0.011 * (1.0 - along) * (0.45 + 0.55 * abs(sin(along * 34.0 + fk)));
			d = min(d, dd - width);
			prev = cur;
		}
	}

	d = min(d, length(q - top - vec2(0.004, -0.010)) - 0.008);
	return 1.0 - smoothstep(-aa, aa, d);
}

vec3 skyfx_synthwave(vec3 dir, float t) {
	float y = dir.y;
	float el = asin(clamp(y, -1.0, 1.0));
	float az = atan(dir.x, -dir.z);

	float px = max(length(fwidth(dir)), 1e-5);
	float floorDist = 1.0 / max(-y, 0.003);
	vec2 floorP = dir.xz * floorDist;
	vec2 floorW = fwidth(floorP);

	float daz = sw_wrap(az - SW_SUN_AZ);
	vec3 sunDir = vec3(-cos(SW_SUN_EL), sin(SW_SUN_EL), 0.0);
	float sunAngle = acos(clamp(dot(dir, sunDir), -1.0, 1.0));
	float e = max(el, 0.0);

	vec3 col = mix(vec3(1.00, 0.30, 0.50), vec3(0.46, 0.09, 0.46), smoothstep(0.0, 0.30, e));
	col = mix(col, vec3(0.11, 0.03, 0.24), smoothstep(0.22, 0.80, e));
	col = mix(col, vec3(0.02, 0.01, 0.08), smoothstep(0.75, 1.50, e));
	col += vec3(1.00, 0.42, 0.22) * 0.55 * exp(-sunAngle * 3.2);
	col += vec3(1.00, 0.22, 0.55) * 0.22 * exp(-sunAngle * 1.1) * (1.0 - smoothstep(0.0, 0.7, e));
	col += skyfx_stars(dir, t, 1.1) * smoothstep(0.30, 0.80, e);
	col += skyfx_meteors(dir, t, 0.80, vec3(1.00, 0.55, 0.95)) * smoothstep(0.35, 0.6, e);
	col += skyfx_meteors(dir, t * 1.29 + 17.0, 0.80, vec3(0.55, 0.95, 1.00)) * smoothstep(0.35, 0.6, e);

	vec2 sunQ = vec2(daz * cos(el), el - SW_SUN_EL);
	float r = length(sunQ);
	float sunMask = 1.0 - smoothstep(SW_SUN_R - px, SW_SUN_R + px, r);
	if (sunMask > 0.0) {
		float s = clamp((sunQ.y + SW_SUN_R) / (2.0 * SW_SUN_R), 0.0, 1.0);
		vec3 sc = mix(vec3(1.00, 0.10, 0.55), vec3(1.00, 0.48, 0.16), smoothstep(0.0, 0.55, s));
		sc = mix(sc, vec3(1.00, 0.93, 0.38), smoothstep(0.45, 1.0, s));
		float gap = 0.62 * smoothstep(0.09, -SW_SUN_R * 0.75, sunQ.y);
		float ph = fract((sunQ.y + t * 0.010) * 15.0);
		float aaB = px * 15.0;
		float stripe = smoothstep(gap * 0.5 - aaB, gap * 0.5 + aaB, abs(ph - 0.5));
		sunMask *= gap > 0.002 ? stripe : 1.0;
		col = mix(col, sc * 1.35, sunMask);
	}

	float streakBand = smoothstep(0.10, 0.20, e) * (1.0 - smoothstep(0.34, 0.50, e));
	if (streakBand > 0.0) {
		float n = skyfx_fbm2(vec2(az * 2.2 - t * 0.012, e * 34.0), 4);
		float streak = smoothstep(0.56, 0.74, n) * streakBand;
		vec3 streakCol = mix(vec3(0.36, 0.06, 0.38), vec3(0.95, 0.40, 0.45), exp(-sunAngle * 2.5) * 0.8);
		col = mix(col, streakCol, streak * 0.85);
	}

	col += vec3(1.00, 0.40, 0.90) * (exp(-abs(el) / (px * 1.5 + 0.0015)) * 0.9 + exp(-abs(el) * 40.0) * 0.15);

	float valley = smoothstep(0.10, 0.65, abs(daz));
	float hFar = (0.025 + 0.16 * sw_mountains(az, 7.3, 2.1)) * mix(0.30, 1.0, valley);
	float hNear = (0.010 + 0.085 * sw_mountains(az + 1.3, 1.7, 3.3)) * mix(0.10, 1.0, valley);
	vec3 haze = vec3(0.20, 0.05, 0.24);
	if (el < hFar && el > -0.02) {
		vec3 m = mix(vec3(0.17, 0.04, 0.24), vec3(0.09, 0.02, 0.16), smoothstep(0.0, hFar, el));
		float c = el / hFar * 6.0;
		float contour = 1.0 - smoothstep(0.0, px * 1.3, min(fract(c), 1.0 - fract(c)) * hFar / 6.0);
		float vert = 1.0 - smoothstep(0.0, px * 1.3, (0.5 - abs(fract(az * 9.549) - 0.5)) / 9.549);
		float wire = max(contour, vert) * 0.35 * smoothstep(0.0, hFar, el);
		float rim = exp(-(hFar - el) / (px * 1.6));
		m += vec3(1.00, 0.25, 0.80) * (wire + rim * 1.2);
		col = mix(m, haze, 0.35 * (1.0 - smoothstep(0.0, 0.03, el)));
	}
	if (el < hNear && el > -0.02) {
		vec3 m = vec3(0.045, 0.004, 0.075);
		float c = el / hNear * 4.0;
		float contour = 1.0 - smoothstep(0.0, px * 1.3, min(fract(c), 1.0 - fract(c)) * hNear / 4.0);
		float vert = 1.0 - smoothstep(0.0, px * 1.3, (0.5 - abs(fract(az * 14.32) - 0.5)) / 14.32);
		float wire = max(contour, vert) * 0.45 * smoothstep(0.0, hNear, el);
		float rim = exp(-(hNear - el) / (px * 1.6));
		m += vec3(0.20, 0.90, 1.00) * (wire + rim * 1.3);
		col = mix(m, haze, 0.25 * (1.0 - smoothstep(0.0, 0.02, el)));
	}

	vec2 q1 = vec2(sw_wrap(az - (SW_SUN_AZ - 0.44)) * cos(el), el + 0.012);
	vec2 q2 = vec2(sw_wrap(az - (SW_SUN_AZ + 0.50)) * cos(el), el + 0.012);
	float palms = max(sw_palm(q1, t, 1.0, -0.42, px), sw_palm(vec2(-q2.x, q2.y) * 1.12, t, 4.0, -0.36, px * 1.12));
	col = mix(col, vec3(0.05, 0.005, 0.08), palms);

	if (y < 0.0) {
		vec2 g = (floorP + vec2(-t * 0.9, 0.0)) * 0.8;
		vec2 w = floorW * 0.8;
		vec2 dl = 0.5 - abs(fract(g) - 0.5);
		vec2 line = 1.0 - smoothstep(vec2(0.025), vec2(0.025) + w * 1.5, dl);
		line *= clamp(1.0 - w * 1.6, 0.0, 1.0);
		float grid = max(line.x, line.y);
		vec3 floorCol = vec3(0.035, 0.0, 0.06) + vec3(1.00, 0.18, 0.78) * grid * 1.3;

		floorCol += vec3(1.00, 0.35, 0.40) * 0.45 * exp(-abs(daz) * 5.0) * exp(el * 7.0);
		float fogAmt = 1.0 - exp(-floorDist * 0.035);
		floorCol = mix(floorCol, haze * 1.3 + vec3(0.25, 0.04, 0.18) * exp(-abs(daz) * 2.0), fogAmt);
		col = mix(col, floorCol, smoothstep(0.0, px * 2.0 + 0.001, -el));
	}
	return col;
}
