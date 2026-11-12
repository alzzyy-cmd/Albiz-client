const vec3 BH_DIR = vec3(0.4472136, 0.6708204, -0.5916080);
const float BH_DIST = 22.0;
const float BH_BOUND = 16.0;
const float BH_DISK_IN = 3.0;
const float BH_DISK_OUT = 13.0;

vec3 bh_space(vec3 d, float t) {
	vec3 col = vec3(0.006, 0.005, 0.016);
	float n = skyfx_fbm3(d * 2.6 + vec3(3.1, 0.0, 1.7), 5);
	float wisp = pow(1.0 - abs(2.0 * skyfx_fbm3(d * 4.2 + vec3(9.0, 2.0, 5.0), 4) - 1.0), 6.0);
	float hue = skyfx_noise3(d * 1.3 + vec3(7.0));
	vec3 neb = mix(vec3(0.16, 0.04, 0.26), vec3(0.03, 0.14, 0.30), smoothstep(0.3, 0.7, hue));
	neb = mix(neb, vec3(0.30, 0.06, 0.18), smoothstep(0.62, 0.85, hue));
	col += neb * smoothstep(0.36, 0.82, n) * 0.85;
	col += mix(vec3(0.55, 0.30, 0.85), vec3(0.95, 0.45, 0.60), hue) * wisp * 0.22 * smoothstep(0.3, 0.7, n);

	float band = exp(-pow(dot(d, normalize(vec3(0.2, 0.35, 0.9))) * 3.2, 2.0));
	col += vec3(0.10, 0.08, 0.14) * band * (0.4 + 0.6 * skyfx_fbm3(d * 7.0, 4));
	col += skyfx_starLayer(d, 90.0, 0.30, t, 1.3);
	col += skyfx_starLayer(skyfx_rotY(d, 1.7), 210.0, 0.40, t * 1.3, 0.9);
	col += skyfx_starLayer(skyfx_rotX(d, 0.9), 480.0, 0.45, t * 0.8, 0.55);
	return col;
}

vec3 bh_diskColor(float x) {
	vec3 c = mix(vec3(1.00, 0.97, 0.92), vec3(1.00, 0.78, 0.38), smoothstep(0.0, 0.25, x));
	c = mix(c, vec3(1.00, 0.42, 0.10), smoothstep(0.2, 0.6, x));
	return mix(c, vec3(0.55, 0.08, 0.04), smoothstep(0.55, 1.0, x));
}

vec3 skyfx_blackhole(vec3 dir, float t) {
	float cosA = dot(dir, BH_DIR);

	float angle = acos(clamp(cosA, -1.0, 1.0));
	float boundAngle = asin(BH_BOUND / BH_DIST);
	if (angle > boundAngle) {
		return bh_space(dir, t);
	}

	vec3 up = normalize(vec3(0.0, 1.0, 0.0) - BH_DIR * BH_DIR.y);
	vec3 side = normalize(cross(BH_DIR, up));
	vec3 normal = normalize(up + BH_DIR * 0.12);
	vec3 diskX = normalize(cross(normal, BH_DIR));
	vec3 diskY = cross(normal, diskX);

	vec3 pos = -BH_DIR * BH_DIST;
	vec3 vel = dir;

	float b = dot(pos, vel);
	float c = dot(pos, pos) - BH_BOUND * BH_BOUND;
	float h = b * b - c;
	float enter = -b - sqrt(max(h, 0.0));
	pos += vel * max(enter, 0.0);

	vec3 acc = vec3(0.0);
	float trans = 1.0;
	float h2 = dot(cross(pos, vel), cross(pos, vel));
	bool captured = false;
	float rMin = 1e3;
	float jitter = skyfx_hash12(floor(dir.xy * 4096.0)) * 0.3;
	for (int i = 0; i < 110; i++) {
		float r2 = dot(pos, pos);
		float r = sqrt(r2);
		rMin = min(rMin, r);
		if (r < 1.0) {
			captured = true;
			break;
		}
		if (r > BH_BOUND + 0.5 && dot(pos, vel) > 0.0) break;
		float dt = clamp(0.09 * r, 0.04, 1.2) * (i == 0 ? (1.0 - jitter) : 1.0);

		vec3 accel = -1.5 * h2 * pos / (r2 * r2 * r) * (1.0 - smoothstep(BH_BOUND * 0.45, BH_BOUND, r));
		vec3 next = pos + vel * dt;
		vel = normalize(vel + accel * dt);

		float s0 = dot(pos, normal);
		float s1 = dot(next, normal);
		if (s0 * s1 < 0.0) {
			vec3 hit = mix(pos, next, s0 / (s0 - s1));
			float hr = length(hit);
			if (hr > BH_DISK_IN && hr < BH_BOUND * 0.95) {
				float x = (hr - BH_DISK_IN) / (BH_DISK_OUT - BH_DISK_IN);
				float xc = min(x, 1.0);
				float phi = atan(dot(hit, diskY), dot(hit, diskX));

				float spin = t * 1.6 / pow(hr, 1.5);

				vec2 swirl = vec2(cos(phi + spin), sin(phi + spin)) * 2.4 + vec2(hr * 1.7, hr * 0.6);
				float n = skyfx_fbm2(swirl, 5);
				float dens = smoothstep(0.0, 0.06, x) * (1.0 - smoothstep(0.65, 1.0, x)) * (0.25 + 1.1 * n * n * 1.6);

				float arms = pow(0.5 + 0.5 * cos(2.0 * (phi + spin * 0.6) - log(hr) * 5.0), 6.0);
				dens += smoothstep(0.7, 1.0, x) * (1.0 - smoothstep(BH_BOUND * 0.6, BH_BOUND * 0.95, hr)) * arms * n * 0.55;

				vec3 orbit = normalize(cross(normal, hit));
				float doppler = 1.0 + 0.45 * dot(orbit, -vel);
				vec3 emit = bh_diskColor(clamp(xc + (1.0 - doppler) * 0.25, 0.0, 1.0)) * (2.6 / (0.3 + x * 3.0)) * doppler * doppler;
				float alpha = clamp(dens * 1.15, 0.0, 1.0);
				acc += trans * emit * alpha;
				trans *= 1.0 - alpha;
				if (trans < 0.02) break;
			}
		}
		pos = next;
	}

	vec3 col = acc;
	if (!captured) {
		col += trans * bh_space(vel, t);
	}

	if (!captured) {
		col += trans * vec3(1.0, 0.86, 0.68) * 0.9 * exp(-pow((rMin - 1.55) * 3.2, 2.0));
	}

	col += vec3(1.0, 0.55, 0.25) * 0.06 * exp(-angle * BH_DIST * 0.22);

	vec3 rel = dir * (BH_DIST * cosA) - BH_DIR * BH_DIST;
	float along = dot(rel, normal);
	float across = length(rel - normal * along);
	float jet = exp(-across * across / (0.25 + abs(along) * 0.08)) * exp(-abs(along) * 0.18) * smoothstep(1.0, 3.0, abs(along));
	jet *= 0.75 + 0.25 * sin(abs(along) * 2.0 - t * 4.0);
	col += vec3(0.35, 0.55, 1.0) * jet * 0.35 * trans;
	return col;
}
