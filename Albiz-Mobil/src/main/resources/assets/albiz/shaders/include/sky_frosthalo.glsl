vec3 ys_frosthalo(vec3 dir, float t) {
	float y = dir.y;
	vec3 sunDir = normalize(vec3(0.60, 0.13, -0.78));
	vec3 zenith = vec3(0.08, 0.22, 0.56);
	vec3 middle = vec3(0.36, 0.58, 0.90);
	vec3 horizon = vec3(0.78, 0.87, 0.97);
	vec3 col = mix(horizon, middle, smoothstep(0.0, 0.26, y));
	col = mix(col, zenith, smoothstep(0.24, 0.95, y));
	if (y < 0.0) col = mix(horizon, vec3(0.60, 0.70, 0.86), smoothstep(0.0, 0.35, -y));

	float sd = clamp(dot(dir, sunDir), -1.0, 1.0);
	float ang = acos(sd);
	col *= mix(0.84, 1.0, smoothstep(0.24, 0.375, ang));
	col = mix(col, vec3(1.0, 0.90, 0.88), pow(max(sd, 0.0), 8.0) * 0.18);
	col += vec3(1.0, 0.97, 0.92) * (smoothstep(0.9994, 0.99965, sd) * 1.6 + pow(max(sd, 0.0), 220.0) * 0.55 + pow(max(sd, 0.0), 30.0) * 0.12);

	float halo = (ang - 0.384) / 0.016;
	float ringMask = exp(-halo * halo);
	vec3 ringColor = ys_hsv2rgb(vec3(clamp((ang - 0.372) * 14.0, 0.0, 0.62), 0.6, 1.0));
	col += ringColor * ringMask * 0.5;
	col += vec3(0.85, 0.92, 1.0) * exp(-pow((ang - 0.41) / 0.05, 2.0)) * 0.06;

	vec3 side = normalize(cross(sunDir, vec3(0.0, 1.0, 0.0)));
	for (int k = -1; k <= 1; k += 2) {
		vec3 dogDir = normalize(sunDir * cos(0.395) + side * float(k) * sin(0.395));
		vec3 dd = dir - dogDir;
		float out_ = dot(dd, side) * float(k);
		float d = length(vec2(out_ * (out_ > 0.0 ? 0.9 : 3.5), (y - dogDir.y) * 1.2));
		float dog = exp(-d * d / 0.0009);
		float hue = clamp(out_ * 9.0 + 0.02, 0.0, 0.6);
		col += ys_hsv2rgb(vec3(hue, 0.55 * (1.0 - smoothstep(0.03, 0.08, out_)), 1.0)) * dog * 0.85;
	}

	vec3 topA = ys_rotAxis(sunDir, side, 0.384);
	vec3 topB = ys_rotAxis(sunDir, side, -0.384);
	vec3 top = topA.y > topB.y ? topA : topB;
	vec3 dt = dir - top;
	float arcD = length(vec2(dot(dt, side) * 0.4, dt.y + dot(dt, side) * dot(dt, side) * 3.0));
	col += vec3(0.95, 0.97, 1.0) * exp(-arcD * arcD / 0.0005) * 0.32;

	if (y > 0.0) {
		vec2 uv = dir.xz / (y + 0.3);
		vec2 q = ys_rot(2.1) * uv;
		float cirrus = ys_fbm2(vec2(q.x * 0.7, q.y * 3.2) + vec2(t * 0.006, 0.0), 5);
		float fine = ys_fbm2(vec2(q.x * 2.0, q.y * 9.0) + 4.0, 3);
		float c = smoothstep(0.55, 0.85, cirrus * 0.8 + fine * 0.3);
		col = mix(col, vec3(0.97, 0.98, 1.0), c * 0.42 * smoothstep(0.02, 0.25, y));
	}

	vec3 dust = ys_starLayer(dir, 160.0, 0.22, t * 2.2, 1.0) + ys_starLayer(ys_rotY(dir, 0.8), 300.0, 0.28, t * 2.8, 0.8);
	float dustMask = smoothstep(-0.02, 0.04, y) * (1.0 - smoothstep(0.30, 0.70, y));
	col += dust * vec3(0.92, 0.97, 1.0) * dustMask * (1.1 + 1.4 * pow(max(sd, 0.0), 4.0));
	return col;
}
