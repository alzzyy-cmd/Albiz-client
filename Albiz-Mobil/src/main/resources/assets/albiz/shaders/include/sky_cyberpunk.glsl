const vec3 CP_FOG = vec3(0.13, 0.05, 0.17);

float cp_wrap(float a) {
	return a - SKYFX_TAU * floor((a + SKYFX_PI) / SKYFX_TAU);
}

vec3 cp_neon(float h) {

	if (h < 0.35) return vec3(1.00, 0.18, 0.62);
	if (h < 0.65) return vec3(0.15, 0.90, 1.00);
	if (h < 0.85) return vec3(0.68, 0.30, 1.00);
	return vec3(1.00, 0.62, 0.15);
}

vec4 cp_layer(float az, float el, float t, float px, float cells, float seed, float minH, float maxH, float gap,
		vec3 wall, float windowSize, float neonAmount) {
	if (el > maxH + 0.03) return vec4(0.0);
	float cellAngle = SKYFX_TAU / cells;
	float u = az / cellAngle;
	float id = mod(floor(u), cells);
	float f = fract(u);
	vec3 h = skyfx_hash33(vec3(id, seed, 1.0));
	vec3 h2 = skyfx_hash33(vec3(id, seed, 2.0));
	float w = mix(1.0 - gap, 1.0, h.x);
	float l = (1.0 - w) * h.y;
	float lx = (f - l) / w;
	float height = minH + (maxH - minH) * h.z * h.z;

	if (h2.x > 0.5 && abs(lx - 0.5) > 0.28) height *= 0.86;
	float bodyW = w * cellAngle * cos(el);
	float edgeX = min(lx, 1.0 - lx) * bodyW;
	float aaX = px;
	float inside = smoothstep(-aaX, aaX, edgeX) * (1.0 - smoothstep(height - px, height + px, el));
	vec3 col = vec3(0.0);
	float cover = inside;
	if (inside > 0.0) {

		col = wall * (0.75 + 0.35 * smoothstep(0.0, height, el));

		vec2 wq = vec2(lx * bodyW / windowSize, el / (windowSize * 1.35));
		vec2 wid = floor(wq);
		vec2 wf = fract(wq);
		vec3 wh = skyfx_hash33(vec3(wid + id * 17.0, seed + 3.0));
		float floorLit = step(0.25, skyfx_hash12(vec2(wid.y, id + seed)));
		float lit = step(wh.x, 0.42 * floorLit + 0.08) * step(0.7, wq.y);

		lit *= step(0.08, fract(wh.y * 13.0 + t * 0.02 * wh.z));
		float shape = step(0.18, wf.x) * step(wf.x, 0.82) * step(0.22, wf.y) * step(wf.y, 0.78);
		vec3 wc = wh.z < 0.6 ? vec3(1.0, 0.82, 0.55) : (wh.z < 0.82 ? vec3(0.45, 0.85, 1.0) : vec3(1.0, 0.45, 0.85));

		float detail = clamp(windowSize / px * 0.45 - 0.4, 0.0, 1.0);
		float windowLight = mix(0.42 * 0.36, shape, detail) * lit;
		col += wc * windowLight * 0.55 * step(el, height - windowSize * 0.6);

		if (neonAmount > 0.0) {
			vec3 nc = cp_neon(h2.y);
			float flicker = step(0.04, fract(t * (0.3 + h2.z) + h2.x * 7.0)) * (0.85 + 0.15 * sin(t * 40.0 + id));
			if (h2.z < 0.45 * neonAmount) {
				float sx = abs(lx - (0.12 + 0.1 * h2.x)) * bodyW;
				float sy0 = height * (0.30 + 0.2 * h2.y);
				float sy1 = height * (0.78 + 0.1 * h2.x);
				float sign = (1.0 - smoothstep(windowSize * 0.9, windowSize * 0.9 + px, sx)) * step(sy0, el) * step(el, sy1);

				float letters = step(0.35, skyfx_hash12(vec2(floor(el / (windowSize * 1.6)), id)));
				col = mix(col, nc * (0.9 + 0.6 * letters), sign * flicker);
			}
			if (h2.y > 1.0 - 0.5 * neonAmount) {
				float roof = 1.0 - smoothstep(0.0, px * 1.5, abs(el - (height - px * 1.2)));
				col += nc * roof * 1.2 * flicker;
			}
		}
	}

	if (h2.z > 0.72) {
		float ax = abs(lx - 0.5) * bodyW;
		float antennaTop = height + 0.012 + 0.02 * h.x;
		float antenna = (1.0 - smoothstep(0.0, px, ax)) * step(height, el) * step(el, antennaTop);
		col = mix(col, wall, antenna);
		cover = max(cover, antenna);
		float blink = step(0.5, fract(t * 0.6 + h.y * 3.0));
		vec2 lq = vec2(ax, el - antennaTop);
		col += vec3(1.0, 0.15, 0.10) * blink * exp(-dot(lq, lq) / (px * px * 2.5 + 2e-6)) * 1.6;
		cover = max(cover, blink * exp(-dot(lq, lq) / (px * px * 2.5 + 2e-6)));
	}
	return vec4(col * cover, cover);
}

vec3 cp_hologram(vec2 q, float t, float seed) {

	float glitch = step(0.93, fract(sin(floor(t * 6.0) + seed) * 43758.5453));
	q.x += glitch * 0.06 * sin(q.y * 80.0 + t * 30.0);
	vec3 a = vec3(1.0, 0.20, 0.65);
	vec3 b = vec3(0.10, 0.85, 1.00);
	float wave = 0.5 + 0.5 * sin(q.x * 9.0 - t * 1.6 + sin(q.y * 6.0 + t) * 1.5);
	vec3 col = mix(a, b, wave) * 0.45;

	vec2 c = (q - vec2(0.24, 0.5)) * vec2(1.8, 1.0);
	float ring = exp(-pow((length(c) - 0.28) * 22.0, 2.0)) * (0.6 + 0.4 * sin(atan(c.y, c.x) * 3.0 - t * 3.0));
	col += mix(b, a, wave) * ring * 1.5;
	float row = floor(q.y * 7.0);
	float barLen = 0.55 + 0.4 * fract(sin(row * 12.9 + seed + floor(t * 0.5)) * 437.5);
	float bars = step(0.5, q.x) * step(q.x, 0.5 + 0.45 * barLen) * step(0.3, fract(q.y * 7.0)) * step(fract(q.y * 7.0), 0.7);
	col += vec3(0.9, 0.95, 1.0) * bars * 0.6 * step(0.12, q.y) * step(q.y, 0.88);
	col *= 0.75 + 0.25 * sin(q.y * 140.0 + t * 8.0);
	col *= 0.85 + 0.15 * sin(t * 23.0 + seed);
	return col;
}

vec3 skyfx_cyberpunk(vec3 dir, float t) {
	float y = dir.y;
	float el = asin(clamp(y, -1.0, 1.0));
	float az = atan(dir.x, -dir.z);
	float px = max(length(fwidth(dir)), 1e-5);
	float e = max(el, 0.0);

	float smog = skyfx_noise2(vec2(az * 3.0, 1.0));
	vec3 glow = mix(vec3(0.55, 0.14, 0.38), vec3(0.62, 0.26, 0.16), smoothstep(0.55, 0.8, smog));
	vec3 col = mix(glow, vec3(0.16, 0.05, 0.22), smoothstep(0.0, 0.30, e));
	col = mix(col, vec3(0.03, 0.015, 0.07), smoothstep(0.25, 1.1, e));
	col += skyfx_stars(dir, t, 0.35) * smoothstep(0.5, 1.0, e);

	for (int k = 0; k < 3; k++) {
		float fk = float(k);
		float baseAz = fk * 2.1 + 0.6;
		float tilt = sin(t * (0.21 + fk * 0.05) + fk * 2.0) * 0.55;
		float along = max(el - 0.05, 0.0);
		float beamAz = baseAz + along * tan(tilt) / max(cos(el), 0.2);
		float dx = abs(cp_wrap(az - beamAz)) * cos(el);
		float width = 0.006 + along * 0.05;
		float beam = exp(-dx * dx / (width * width)) * exp(-along * 2.2) * step(0.0, el - 0.05);
		col += vec3(0.75, 0.80, 1.00) * beam * 0.22;
	}

	if (y > 0.0) {
		vec2 cp = dir.xz / (y + 0.12) * 0.7 + vec2(t * 0.015, t * 0.006);
		float n = skyfx_fbm2(cp, 5);
		float cloud = smoothstep(0.45, 0.80, n) * smoothstep(0.03, 0.15, e) * (1.0 - smoothstep(0.5, 1.0, e));
		vec3 cloudCol = mix(vec3(0.30, 0.08, 0.30), vec3(0.75, 0.22, 0.55), smoothstep(0.6, 0.0, e) * (1.0 - n * 0.6));
		col = mix(col, cloudCol, cloud * 0.75);
	}

	for (int m = 0; m < 2; m++) {
		float fm = float(m);
		float tAz = fm * 3.3 - 1.2;
		float dx = cp_wrap(az - tAz) * cos(el);
		float halfW = 0.045 - el * 0.04;
		float top = 0.52 - fm * 0.07;
		if (abs(dx) < 0.26 && el < top + 0.08) {
			float tower = (1.0 - smoothstep(halfW - px, halfW + px, abs(dx))) * step(el, top);
			float spire = (1.0 - smoothstep(0.0, px * 1.2, abs(dx))) * step(top, el) * step(el, top + 0.06);
			vec3 tc = vec3(0.035, 0.025, 0.07);

			float strip = (1.0 - smoothstep(0.0, px * 1.5, abs(abs(dx) - halfW * 0.55))) * (0.6 + 0.4 * sin(el * 60.0 - t * 3.0));
			tc += vec3(0.15, 0.85, 1.0) * strip * 0.8;
			col = mix(col, tc, max(tower, spire));
			float blink = step(0.5, fract(t * 0.8 + fm * 0.3));
			vec2 lq = vec2(dx, el - top - 0.06);
			col += vec3(1.0, 0.1, 0.1) * blink * exp(-dot(lq, lq) / (px * px * 3.0 + 2e-6)) * 1.5;

			vec2 bq = vec2((dx - (0.11 - fm * 0.22)) / 0.13 + 0.5, (el - (top - 0.15)) / 0.08 + 0.5);
			if (bq.x > 0.0 && bq.x < 1.0 && bq.y > 0.0 && bq.y < 1.0) {
				vec3 holo = cp_hologram(bq, t, fm * 3.0);
				float frame = step(min(min(bq.x, 1.0 - bq.x), min(bq.y, 1.0 - bq.y)), 0.03);
				col = col * 0.45 + holo + vec3(0.2, 0.9, 1.0) * frame * 0.5;
			}
			col += vec3(0.25, 0.65, 1.0) * 0.07 * exp(-abs(dx - (0.11 - fm * 0.22)) * 18.0) * exp(-abs(el - (top - 0.15)) * 18.0);
		}
	}

	vec4 far = cp_layer(az, el, t, px, 200.0, 1.0, 0.030, 0.140, 0.05, vec3(0.16, 0.08, 0.24), 0.0040, 0.0);
	col = mix(col, far.rgb / max(far.a, 1e-4), far.a);
	col = mix(col, glow * 0.55, far.a * 0.35 * (1.0 - smoothstep(0.0, 0.05, el)));

	for (int lane = 0; lane < 4; lane++) {
		float fl = float(lane);
		float laneEl = 0.10 + fl * 0.06 + 0.006 * sin(az * 3.0 + fl);
		float dy = el - laneEl;
		if (abs(dy) > 0.02) continue;
		float dirSign = mod(fl, 2.0) < 0.5 ? 1.0 : -1.0;
		const float CARS = 40.0;
		float cellAngle = SKYFX_TAU / CARS;
		float u = (az - dirSign * t * (0.05 + 0.012 * fl)) / cellAngle;
		float id = mod(floor(u), CARS);
		float f = fract(u);
		vec3 h = skyfx_hash33(vec3(id, fl, 21.0));
		if (h.z > 0.55) continue;
		float carX = (f - (0.2 + 0.6 * h.x)) * cellAngle * cos(el) * dirSign;
		float carY = dy - (h.y - 0.5) * 0.008;

		vec2 head = vec2(carX - 0.0018, carY);
		vec2 tail = vec2(carX + 0.0018, carY);
		float sharp = px * px * 0.9 + 4e-7;
		col += vec3(1.0, 0.95, 0.80) * exp(-dot(head, head) / sharp) * 1.1;
		col += vec3(1.0, 0.12, 0.20) * exp(-dot(tail, tail) / sharp) * 1.0;
		float trail = exp(-carY * carY / (sharp * 1.5)) * step(0.0, carX) * exp(-carX * 28.0);
		col += mix(vec3(1.0, 0.15, 0.45), vec3(0.2, 0.8, 1.0), h.y) * trail * 0.45;
	}

	vec4 mid = cp_layer(az + 0.37, el, t, px, 120.0, 2.0, 0.035, 0.210, 0.25, vec3(0.085, 0.045, 0.14), 0.0055, 0.5);
	col = mix(col, mid.rgb / max(mid.a, 1e-4), mid.a);
	vec4 near = cp_layer(az + 1.21, el, t, px, 64.0, 3.0, 0.020, 0.290, 0.45, vec3(0.035, 0.020, 0.065), 0.0075, 1.0);
	col = mix(col, near.rgb / max(near.a, 1e-4), near.a);

	if (y < 0.0) {
		vec2 sp = dir.xz / max(-y, 0.02);
		vec2 cell = floor(sp * 3.0);
		vec3 h = skyfx_hash33(vec3(cell, 5.0));
		vec2 f = fract(sp * 3.0) - 0.5 - (h.xy - 0.5) * 0.6;
		float lights = step(h.z, 0.5) * exp(-dot(f, f) * 200.0);
		vec3 street = vec3(0.04, 0.02, 0.06) + cp_neon(h.x) * lights * 0.6 * exp(-length(sp) * 0.02);
		col = mix(col, mix(street, CP_FOG, 1.0 - exp(-length(sp) * 0.012)), smoothstep(0.0, px * 2.0 + 0.002, -el));
	}

	{
		const float RAIN = 160.0;
		float cellAngle = SKYFX_TAU / RAIN;
		vec2 uv = vec2(az / cellAngle, el / (cellAngle * 2.5) + t * 3.0);
		vec2 id = floor(uv);
		id.x = mod(id.x, RAIN);
		vec2 f = fract(uv);
		vec3 h = skyfx_hash33(vec3(id, 31.0));
		float rx = abs(f.x - h.x) * cellAngle * cos(el);
		float streak = (1.0 - smoothstep(0.0, px * 1.2, rx)) * smoothstep(0.0, 0.3, f.y - h.y * 0.5) * (1.0 - smoothstep(0.3, 0.9, f.y - h.y * 0.5));
		col += vec3(0.55, 0.60, 0.80) * streak * step(h.z, 0.22) * 0.05;
	}
	return col;
}
