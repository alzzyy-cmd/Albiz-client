const vec3 UW_FOG = vec3(0.035, 0.22, 0.32);

float uw_wrap(float a) {
	return a - SKYFX_TAU * floor((a + SKYFX_PI) / SKYFX_TAU);
}

float uw_caustics(vec2 p, float t) {
	vec2 i = floor(p);
	vec2 f = fract(p);
	float d1 = 8.0;
	float d2 = 8.0;
	for (int y = -1; y <= 1; y++) {
		for (int x = -1; x <= 1; x++) {
			vec2 g = vec2(float(x), float(y));
			vec3 h = skyfx_hash33(vec3(i + g, 4.0));
			vec2 o = 0.5 + 0.38 * sin(t * (0.6 + h.z) + h.xy * SKYFX_TAU);
			float d = length(g + o - f);
			if (d < d1) {
				d2 = d1;
				d1 = d;
			} else if (d < d2) {
				d2 = d;
			}
		}
	}
	return 1.0 - smoothstep(0.0, 0.10, d2 - d1);
}

float uw_ellipse(vec2 p, vec2 r) {
	float k = length(p / r);
	return (k - 1.0) * min(r.x, r.y);
}

float uw_whale(vec2 q, float t) {
	float tailSwing = sin(t * 0.7);
	float bend = 0.05 * tailSwing * smoothstep(0.1, -0.6, q.x) * smoothstep(0.1, -0.6, q.x);
	vec2 b = vec2(q.x, q.y - bend);
	float x = b.x;
	float r = 0.115 * smoothstep(-0.50, 0.05, x) + 0.018;
	if (x > 0.28) r *= sqrt(max(1.0 - pow((x - 0.28) / 0.24, 2.0), 0.0));
	float body = (x < -0.52 || x > 0.52) ? 1.0 : abs(b.y + 0.012 * x) - r;

	vec2 tq = b - vec2(-0.56, 0.0);
	float flukeUp = uw_ellipse(skyfx_rot(-0.65 - 0.25 * tailSwing) * tq - vec2(-0.07, 0.0), vec2(0.10, 0.022));
	float flukeDown = uw_ellipse(skyfx_rot(0.65 - 0.25 * tailSwing) * tq - vec2(-0.07, 0.0), vec2(0.10, 0.022));

	vec2 finDir = normalize(vec2(-0.55, -0.80 + 0.10 * sin(t * 0.5)));
	vec2 fq = b - (vec2(0.16, -0.085) + finDir * 0.13);
	float fin = uw_ellipse(vec2(dot(fq, finDir), dot(fq, vec2(-finDir.y, finDir.x))), vec2(0.14, 0.024));
	return min(min(body, fin), min(flukeUp, flukeDown));
}

vec3 skyfx_underwater(vec3 dir, float t) {
	float y = dir.y;
	float el = asin(clamp(y, -1.0, 1.0));
	float az = atan(dir.x, -dir.z);
	float px = max(length(fwidth(dir)), 1e-5);

	vec3 col = mix(vec3(0.004, 0.035, 0.08), UW_FOG, smoothstep(-1.2, 0.0, el));
	col = mix(col, vec3(0.06, 0.40, 0.52), smoothstep(0.0, 0.7, el));
	col = mix(col, vec3(0.12, 0.55, 0.66), smoothstep(0.55, 0.72, el));

	if (el > -0.3) {
		float a = az + 0.05 * sin(t * 0.21 + el * 3.0);
		float shafts = skyfx_noise2(vec2(a * 9.0, t * 0.06)) * skyfx_noise2(vec2(a * 23.0 + 3.0, t * 0.11 + 5.0));
		shafts = smoothstep(0.12, 0.55, shafts);
		float fall = smoothstep(-0.3, 0.35, el) * (1.0 - smoothstep(0.62, 0.74, el));
		col += vec3(0.35, 0.80, 0.85) * shafts * fall * 0.35;
	}

	float windowEdge = 0.723;
	if (el > windowEdge - 0.06) {
		vec2 sp = dir.xz / max(y, 0.05);
		vec2 warp = vec2(skyfx_noise2(sp * 1.3 + t * 0.25), skyfx_noise2(sp * 1.3 - t * 0.21 + 7.0)) - 0.5;
		float c1 = uw_caustics(sp * 3.0 + warp * 1.6 + vec2(t * 0.05, 0.0), t * 0.9);
		float c2 = uw_caustics(sp * 4.6 - warp * 1.2 + vec2(0.0, t * 0.04), t * 1.1 + 3.0);
		vec3 surf = mix(vec3(0.40, 0.80, 0.90), vec3(0.75, 0.95, 1.00), smoothstep(0.75, 1.4, el));
		surf += vec3(0.80, 1.00, 1.00) * (c1 * 0.28 + c2 * 0.20 + c1 * c2 * 0.4);

		vec3 sunDir = normalize(vec3(0.25, 1.0, 0.18));
		float sunA = acos(clamp(dot(dir, sunDir), -1.0, 1.0));
		surf += vec3(1.0, 0.98, 0.85) * (exp(-sunA * 14.0) * 1.4 + exp(-sunA * 4.0) * 0.35);
		float inside = smoothstep(windowEdge - 0.02, windowEdge + 0.02, el + (warp.x) * 0.03);

		float rim = exp(-pow((el - windowEdge) * 38.0, 2.0));
		col = mix(col, surf, inside);
		col += vec3(0.45, 0.95, 0.90) * rim * 0.35;
	}

	if (el < 0.24 && el > -0.05) {
		float k = az * 9.0;
		float ki = floor(k);
		float kh = skyfx_hash12(vec2(ki, 3.0));
		if (kh < 0.5) {
			float height = 0.07 + 0.16 * skyfx_hash12(vec2(ki, 7.0));
			float s = clamp(el / height, 0.0, 1.0);
			float sway = sin(t * 0.6 + ki * 1.7 + el * 14.0) * 0.10 * s;
			float center = 0.5 + (skyfx_hash12(vec2(ki, 11.0)) - 0.5) * 0.35 + sway;

			float leafPhase = el * 70.0 + ki;
			float leaf = max(sin(leafPhase), 0.0);
			float w = mix(0.035, 0.015, s) + 0.06 * leaf * leaf * (1.0 - s * 0.6);
			float off = 0.03 * leaf * sign(sin(leafPhase * 0.5));
			float kd = abs(fract(k) - center - off) - w;
			float aa = px * 9.0 * 1.5;
			float kelp = (1.0 - smoothstep(-aa, aa, kd)) * (1.0 - smoothstep(height - 0.01, height, el));
			vec3 kc = mix(vec3(0.03, 0.16, 0.10), vec3(0.08, 0.30, 0.18), leaf * 0.6);
			col = mix(col, mix(kc, UW_FOG, 0.30 + 0.3 * kh), kelp * 0.92);
		}
	}

	{
		float wAz = t * 0.035;
		float wEl = 0.20 + 0.035 * sin(t * 0.05);
		vec2 q = vec2(uw_wrap(az - wAz) * cos(el), el - wEl) / 0.42;
		if (abs(q.x) < 0.75 && abs(q.y) < 0.4) {
			float d = uw_whale(q, t);
			float body = 1.0 - smoothstep(-px / 0.42, px / 0.42, d);

			float belly = smoothstep(0.02, -0.08, q.y) * (0.6 + 0.4 * sin(q.y * 220.0));
			vec3 whale = mix(vec3(0.015, 0.075, 0.12), vec3(0.05, 0.17, 0.22), belly * smoothstep(-0.3, 0.3, q.x));
			whale = mix(whale, UW_FOG, 0.35);
			col = mix(col, whale, body);
		}
	}

	for (int s = 0; s < 4; s++) {
		float fs = float(s);
		float dirSign = fs < 1.5 ? 1.0 : -1.0;
		float sAz = fs * 1.7 + dirSign * t * (0.045 + 0.01 * fs);
		float sEl = 0.08 + 0.10 * fs + 0.06 * sin(t * 0.13 + fs * 2.0);
		vec2 c = vec2(uw_wrap(az - sAz) * cos(el), el - sEl);
		if (dot(c, c) > 0.08) continue;
		for (int f = 0; f < 26; f++) {
			float ff = float(f);
			vec3 h = skyfx_hash33(vec3(fs, ff, 5.0));
			float orbit = t * (0.5 + 0.4 * h.z) + h.x * SKYFX_TAU;
			vec2 pos = (h.xy - 0.5) * vec2(0.34, 0.13) + vec2(cos(orbit), sin(orbit * 1.3)) * 0.02;
			vec2 q = c - pos;
			q /= 1.8;
			if (dot(q, q) > 0.0004) continue;
			q.x *= dirSign;
			q = skyfx_rot(0.15 * sin(orbit * 1.3)) * q;
			float wiggle = sin(t * 9.0 + ff) * 0.0012;
			float bodyD = uw_ellipse(q, vec2(0.0075, 0.0028));
			vec2 tq = q - vec2(-0.0085, wiggle);
			float tail = max(abs(tq.y) - (-tq.x) * 0.9 - 0.0002, max(tq.x, -tq.x - 0.0045));
			float d = min(bodyD, tail);
			float fish = 1.0 - smoothstep(-px / 1.8, px / 1.8, d);
			vec3 fc = mix(vec3(0.02, 0.10, 0.15), vec3(0.30, 0.55, 0.60), 0.25 + 0.25 * sin(orbit * 2.0 + q.x * 400.0));
			col = mix(col, mix(fc, UW_FOG, 0.2), fish);
		}
	}

	for (int j = 0; j < 6; j++) {
		float fj = float(j);
		vec3 h = skyfx_hash33(vec3(fj, 2.0, 9.0));
		float cycle = 90.0 + 40.0 * h.z;
		float life = fract(t / cycle + h.x);
		float jAz = h.y * SKYFX_TAU + sin(t * 0.05 + fj) * 0.1;
		float jEl = mix(-0.05, 0.62, life);
		float size = 0.032 + 0.024 * h.z;
		vec2 q = vec2(uw_wrap(az - jAz) * cos(el), el - jEl) / size;
		if (abs(q.x) > 1.6 || q.y > 1.3 || q.y < -3.2) continue;
		float pulse = sin(t * (1.6 + h.x) + fj * 3.0);
		vec2 bq = vec2(q.x / (1.0 + 0.12 * pulse), q.y);

		float dome = length(vec2(bq.x, max(bq.y, 0.0) / 0.85)) - 1.0;
		float bell = max(dome, -bq.y - 0.15 - 0.06 * cos(bq.x * 9.0));
		float inBell = 1.0 - smoothstep(-0.05, 0.05, bell);
		float edgeGlow = exp(-abs(bell) * 6.0);

		float tent = 0.0;
		for (int k = 0; k < 4; k++) {
			float fk = float(k);
			float tx = -0.6 + fk * 0.4 + sin(q.y * 2.5 + t * 1.3 + fk) * 0.18;
			float along = clamp(-q.y / 3.0, 0.0, 1.0);
			tent += exp(-pow((q.x - tx) / (0.05 + 0.03 * along), 2.0)) * step(q.y, -0.05) * (1.0 - along);
		}
		vec3 jc = mix(vec3(1.0, 0.40, 0.80), vec3(0.50, 0.60, 1.0), h.x);
		jc = mix(jc, vec3(0.45, 1.0, 0.85), step(0.8, h.y));
		float fade = smoothstep(0.0, 0.08, life) * (1.0 - smoothstep(0.85, 1.0, life));
		col += jc * (inBell * 0.22 + edgeGlow * 0.30 * (1.0 - step(0.0, bell) * 0.5) + tent * 0.18) * fade * (0.85 + 0.15 * pulse);
		float boxFade = (1.0 - smoothstep(1.0, 1.6, abs(q.x))) * (1.0 - smoothstep(-2.4, -3.2, q.y)) * (1.0 - smoothstep(0.9, 1.3, q.y));
		col += jc * 0.10 * exp(-dot(q, q) * 0.8) * fade * boxFade;
	}

	{
		const float CELLS = 110.0;
		float cellAngle = SKYFX_TAU / CELLS;
		vec2 uv = vec2(az, el) / cellAngle - vec2(0.0, t * 0.9);
		vec2 id = floor(uv);
		id.x = mod(id.x, CELLS);
		vec2 f = fract(uv) - 0.5;
		vec3 h = skyfx_hash33(vec3(id, 13.0));
		if (h.z < 0.14 && el < 0.72) {
			vec2 q = (f - (h.xy - 0.5) * 0.5 - vec2(sin(t * 3.0 + h.x * 30.0) * 0.08, 0.0)) * cellAngle;
			q.x *= cos(el);
			float r = 0.0016 + 0.0035 * h.y;
			float d = length(q);
			float ring = exp(-pow((d - r) / max(px, r * 0.18), 2.0));
			float hi = exp(-dot(q - vec2(-r, r) * 0.45, q - vec2(-r, r) * 0.45) / (r * r * 0.05));
			col += vec3(0.75, 0.95, 1.0) * (ring * 0.35 + hi * 0.5) * smoothstep(-0.2, 0.1, el);
		}
	}

	col += skyfx_starLayer(skyfx_rotY(dir, t * 0.01), 140.0, 0.25, t, 0.25) * vec3(0.6, 0.85, 0.9);
	return col;
}
