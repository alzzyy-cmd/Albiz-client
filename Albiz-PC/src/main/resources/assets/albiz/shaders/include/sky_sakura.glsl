const float SK_SUN_AZ = -1.25;
const float SK_SUN_EL = 0.07;
const float SK_FUJI_AZ = -1.95;
const vec3 SK_HAZE = vec3(0.86, 0.62, 0.66);

float sk_wrap(float a) {
	return a - SKYFX_TAU * floor((a + SKYFX_PI) / SKYFX_TAU);
}

float sk_hills(float az, float seed, float freq) {
	return skyfx_fbm2(vec2(cos(az), sin(az)) * freq + vec2(seed, -seed), 4);
}

vec4 sk_petals(float az, float el, float t, float cells, float seed, float density, float px, vec3 light) {
	if (el > 1.0 || el < -0.05) return vec4(0.0);
	float cellAngle = SKYFX_TAU / cells;
	vec2 uv = vec2(az, el) / cellAngle;
	float fall = t * (0.30 + seed * 0.03);
	uv += vec2(t * 0.10, fall);
	vec2 id = floor(uv);
	vec2 f = fract(uv) - 0.5;
	id.x = mod(id.x, cells);
	vec3 h = skyfx_hash33(vec3(id, seed));
	if (h.z > density) return vec4(0.0);
	float phase = h.x * SKYFX_TAU;
	vec2 center = (h.xy - 0.5) * 0.22 + vec2(sin(t * 1.3 + phase) * 0.06, 0.0);
	vec2 q = f - center;
	q.x *= cos(el);
	float ang = phase + t * (0.6 + h.y * 1.4);
	q = skyfx_rot(ang) * q;

	float flip = 0.25 + 0.75 * abs(cos(t * (1.1 + h.z * 2.0) + phase));
	q.x /= flip;
	float size = 0.12 + 0.08 * h.y;

	vec2 e = q / vec2(size * 0.60 * (0.78 + 0.32 * clamp(q.y / size, -1.0, 1.0)), size);
	float d = (length(e) - 1.0) * size * 0.62;

	float notch = q.y - size * 0.72 - abs(q.x) * 1.4;
	d = max(d, notch * 0.7);
	float aa = px / cellAngle * 1.2;
	float cover = 1.0 - smoothstep(-aa, aa, d);
	if (cover <= 0.0) return vec4(0.0);
	float center01 = clamp(length(e), 0.0, 1.0);
	vec3 col = mix(vec3(1.00, 0.97, 0.98), vec3(1.00, 0.66, 0.80), smoothstep(0.15, 1.0, center01));
	col = mix(col, vec3(0.95, 0.52, 0.70), smoothstep(0.80, 1.0, center01) * 0.35);
	col *= light * (0.75 + 0.25 * flip);
	cover *= 0.92;
	return vec4(col * cover, cover);
}

vec3 sk_lanterns(float az, float el, float t, float px) {
	if (el > 0.85 || el < 0.0) return vec3(0.0);
	const float CELLS = 64.0;
	float cellAngle = SKYFX_TAU / CELLS;
	vec2 uv = vec2(az, el) / cellAngle;
	uv += vec2(t * 0.012, -t * 0.045);
	vec2 id = floor(uv);
	vec2 f = fract(uv) - 0.5;
	id.x = mod(id.x, CELLS);
	vec3 h = skyfx_hash33(vec3(id, 91.7));

	float side = 0.5 - 0.5 * cos(sk_wrap(az - SK_SUN_AZ));
	if (h.z > 0.05 + 0.22 * side) return vec3(0.0);
	vec2 center = (h.xy - 0.5) * 0.30 + vec2(sin(t * 0.4 + h.x * 9.0) * 0.05, 0.0);
	vec2 q = (f - center) * cellAngle;
	q.x *= cos(el);
	float s = 0.0060 * (0.75 + 0.5 * h.y);
	vec2 box = abs(q) - vec2(s * 0.75, s);
	float d = length(max(box, 0.0)) + min(max(box.x, box.y), 0.0) - s * 0.35;
	float flicker = 0.80 + 0.20 * sin(t * (7.0 + h.x * 5.0) + h.y * 20.0) * sin(t * 3.1 + h.z * 30.0);
	float body = 1.0 - smoothstep(-px, px, d);
	float edge = 1.0 - smoothstep(0.38, 0.5, max(abs(f.x), abs(f.y)));
	float glow = (exp(-max(d, 0.0) / (s * 0.8)) * 0.30 + exp(-max(d, 0.0) / (s * 2.6)) * 0.10) * edge;

	float life = smoothstep(0.02, 0.08, el) * (1.0 - smoothstep(0.55, 0.85, el));

	float core = exp(-dot(q / s, q / s) * 1.4);
	vec3 paper = mix(vec3(0.85, 0.24, 0.06), vec3(1.0, 0.55, 0.16), core);
	paper = mix(paper, vec3(1.0, 0.80, 0.40), core * core * 0.6);
	paper *= 1.0 - 0.45 * smoothstep(s * 0.65, s * 1.0, abs(q.y));
	return (paper * body * 0.80 + vec3(1.0, 0.42, 0.12) * glow) * flicker * life;
}

vec3 skyfx_sakura(vec3 dir, float t) {
	float y = dir.y;
	float el = asin(clamp(y, -1.0, 1.0));
	float az = atan(dir.x, -dir.z);
	float px = max(length(fwidth(dir)), 1e-5);
	float e = max(el, 0.0);
	float daz = sk_wrap(az - SK_SUN_AZ);
	vec3 sunDir = vec3(sin(SK_SUN_AZ) * cos(SK_SUN_EL), sin(SK_SUN_EL), -cos(SK_SUN_AZ) * cos(SK_SUN_EL));
	float sunAngle = acos(clamp(dot(dir, sunDir), -1.0, 1.0));
	float away = 0.5 - 0.5 * cos(daz);

	vec3 col = mix(vec3(1.00, 0.72, 0.56), vec3(0.98, 0.58, 0.68), smoothstep(0.0, 0.18, e));
	col = mix(col, vec3(0.62, 0.48, 0.80), smoothstep(0.12, 0.55, e));
	col = mix(col, vec3(0.20, 0.20, 0.48), smoothstep(0.50, 1.45, e));
	vec3 belt = mix(vec3(0.52, 0.50, 0.70), vec3(0.98, 0.62, 0.74), smoothstep(0.02, 0.12, e));
	belt = mix(belt, col, smoothstep(0.12, 0.35, e));
	col = mix(col, belt, away * 0.8);
	col += vec3(1.00, 0.62, 0.30) * 0.65 * exp(-sunAngle * 4.0);
	col += vec3(1.00, 0.55, 0.45) * 0.20 * exp(-sunAngle * 1.3) * (1.0 - smoothstep(0.0, 0.6, e));
	col += skyfx_stars(dir, t, 0.6) * smoothstep(0.75, 1.35, e);

	float sunDisk = 1.0 - smoothstep(0.045 - px, 0.045 + px, sunAngle);
	col = mix(col, vec3(1.00, 0.86, 0.62) * 1.4, sunDisk);

	if (y > -0.02) {
		vec2 cp = dir.xz / (max(y, 0.0) + 0.09) * 0.55 + vec2(t * 0.012, t * 0.004);
		float n = skyfx_fbm2(cp * vec2(1.0, 2.6), 5);
		float band = smoothstep(0.02, 0.10, e) * (1.0 - smoothstep(0.30, 0.60, e));
		float cloud = smoothstep(0.50, 0.78, n) * band;
		float lit = exp(-sunAngle * 1.6);
		vec3 cloudCol = mix(vec3(0.72, 0.50, 0.70), vec3(1.00, 0.70, 0.58), lit);
		cloudCol = mix(cloudCol, vec3(1.00, 0.88, 0.70), smoothstep(0.70, 0.95, n) * lit);
		col = mix(col, cloudCol, cloud * 0.85);
	}

	col += sk_lanterns(az, el, t, px);

	float fx = sk_wrap(az - SK_FUJI_AZ);
	float fuji = 0.205 * pow(max(1.0 - abs(fx) / 0.70, 0.0), 1.7);
	fuji = min(fuji, 0.186 + 0.004 * sin(fx * 90.0));
	if (el < fuji) {
		float snowLine = 0.128 + 0.020 * skyfx_noise2(vec2(fx * 55.0, 1.0)) - 0.012 * abs(sin(fx * 40.0));
		vec3 rock = mix(vec3(0.36, 0.28, 0.50), vec3(0.48, 0.36, 0.58), smoothstep(-0.3, 0.3, fx));
		vec3 snow = mix(vec3(0.92, 0.80, 0.90), vec3(1.00, 0.82, 0.78), smoothstep(-0.2, 0.25, fx));
		float snowMask = smoothstep(snowLine - px, snowLine + px, el);
		vec3 m = mix(rock, snow, snowMask);
		col = mix(m, SK_HAZE, 0.30 * (1.0 - smoothstep(0.0, 0.08, el)));
	}

	float hFar = 0.012 + 0.050 * sk_hills(az, 3.7, 2.4);
	if (el < hFar) {
		col = mix(vec3(0.66, 0.50, 0.68), SK_HAZE, 0.45 * (1.0 - smoothstep(0.0, hFar, el)));
	}
	float hNear = 0.004 + 0.032 * sk_hills(az + 2.0, 9.1, 3.8);
	float crowns = hNear + 0.020 * smoothstep(0.30, 0.75, skyfx_fbm2(vec2(az * 30.0, 2.0), 3));
	if (el < crowns) {
		float trees = smoothstep(hNear - px, hNear + px, el);
		vec3 ground = mix(vec3(0.40, 0.28, 0.46), vec3(0.62, 0.46, 0.62), 0.4 * (1.0 - smoothstep(0.0, 0.02, el)));
		float blossom = skyfx_noise2(vec2(az * 140.0, el * 140.0));
		vec3 pink = mix(vec3(0.86, 0.48, 0.66), vec3(1.00, 0.72, 0.82), blossom) * (0.85 + 0.25 * exp(-sunAngle));
		col = mix(ground, pink, trees);
	}

	if (y < 0.0) {
		float ripple = skyfx_fbm2(vec2(dir.x, dir.z) / max(-y, 0.01) * vec2(0.6, 2.4) + vec2(0.0, t * 0.15), 3);
		float me = -el + (ripple - 0.5) * 0.02;
		vec3 lake = mix(vec3(0.98, 0.62, 0.62), vec3(0.62, 0.48, 0.78), smoothstep(0.0, 0.4, me));
		lake = mix(lake, vec3(0.22, 0.20, 0.42), smoothstep(0.35, 1.2, me));
		lake += vec3(1.0, 0.65, 0.35) * 0.55 * exp(-abs(daz) * 9.0) * exp(-me * 6.0) * (0.6 + 0.8 * ripple);
		lake *= 0.78;
		col = mix(col, mix(SK_HAZE, lake, smoothstep(0.0, 0.08, -el)), smoothstep(0.0, px * 2.0 + 0.002, -el));
	}

	vec3 light = mix(vec3(1.0, 0.92, 0.97), vec3(1.08, 0.94, 0.84), exp(-sunAngle));
	vec4 p = sk_petals(az, el, t, 120.0, 1.0, 0.16, px, light * 0.85);
	col = col * (1.0 - p.a) + p.rgb;
	p = sk_petals(az, el, t * 1.25, 64.0, 2.0, 0.16, px, light * 0.95);
	col = col * (1.0 - p.a) + p.rgb;
	p = sk_petals(az + 1.0, el, t * 1.5, 34.0, 3.0, 0.20, px, light);
	col = col * (1.0 - p.a) + p.rgb;
	return col;
}
