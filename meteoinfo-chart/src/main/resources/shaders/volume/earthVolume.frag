#if __VERSION__ >= 130
    #define varying in
    out vec4 mgl_FragColor;
#else
    #define mgl_FragColor gl_FragColor
#endif

uniform mat4 MVP;
uniform vec2 viewSize;
uniform vec2 viewShift;
uniform mat4 iV;
uniform mat4 iP;

uniform sampler3D tex;
uniform sampler3D normals;
uniform sampler2D colorMap;
uniform int depthSampleCount;

uniform bool orthographic;
uniform float brightness;
uniform int castType;       // 0: max value, 1: basic composite, 2: specular composite

// The volume block is a spherical shell sector, described in earth (ECEF) model space:
// radius + altitude range, and longitude / latitude range (radians).
uniform float earthRadius;
uniform float altMin;
uniform float altMax;
uniform float lonMin;
uniform float lonMax;
uniform float latMin;
uniform float latMax;

// Axis aligned bounding box of the shell sector. It is only used to clip the ray
// marching range, so a small sector does not have to scan the whole earth chord.
uniform vec3 aabbMin;
uniform vec3 aabbMax;

#include volumeBase.glsl

const float TWO_PI = 6.28318530717959;

vec3 ambientLight = vec3(0.34, 0.32, 0.32);
vec3 directionalLight = vec3(0.5, 0.5, 0.5);
vec3 lightVector = normalize(vec3(-1.0, -1.0, 1.0));
vec3 specularColor = vec3(0.5, 0.5, 0.5);

// Maps an earth centred point to the texture coordinate of the lon/lat/alt volume.
// The returned w is negative when the point falls outside the shell sector, so the
// caller can skip the sample.
vec4 shellTexPos(vec3 p) {
    float rMin = earthRadius + min(altMin, altMax);
    float rMax = earthRadius + max(altMin, altMax);
    if (rMax <= rMin) {
        return vec4(0.0, 0.0, 0.0, -1.0);
    }

    float r = length(p);
    if (r < rMin || r > rMax) {
        return vec4(0.0, 0.0, 0.0, -1.0);
    }

    // Longitude: normalise the offset from lonMin into [0, 2*PI] so that blocks
    // crossing the date line (lonMax < lonMin) and 0..360 blocks both work.
    float lon = atan(p.y, p.x);
    float lonSpan = lonMax - lonMin;
    if (lonSpan < 0.0) {
        lonSpan = lonSpan + TWO_PI;
    }
    if (lonSpan < 0.000001) {
        return vec4(0.0, 0.0, 0.0, -1.0);
    }
    float dLon = lon - lonMin;
    if (dLon < 0.0) {
        dLon = dLon + TWO_PI;
    }
    if (dLon > lonSpan) {
        return vec4(0.0, 0.0, 0.0, -1.0);
    }

    // Latitude
    float lat = asin(clamp(p.z / max(r, 0.000001), -1.0, 1.0));
    float latSpan = latMax - latMin;
    if (latSpan < 0.000001) {
        return vec4(0.0, 0.0, 0.0, -1.0);
    }
    if (lat < latMin || lat > latMax) {
        return vec4(0.0, 0.0, 0.0, -1.0);
    }

    // Texture axis mapping: x = longitude, y = latitude, z = altitude
    return vec4(dLon / lonSpan,
                (lat - latMin) / latSpan,
                (r - rMin) / (rMax - rMin),
                1.0);
}

// Dither offset in [0, 1) used to hide the banding of the regular sampling
float dither() {
    return fract(sin(dot(gl_FragCoord.xy, vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {
    vec2 vUV = 2.0 * (gl_FragCoord.xy + vec2(0.5, 0.5) - viewShift) / viewSize - 1.0;
    Ray ray;
    if (orthographic) {
        ray = createRayOrthographic(vUV);
    } else {
        ray = createRayPerspective(vUV);
    }

    // ray.origin of the helper ray is the true eye point in model space (iV * iP *
    // (0,0,0,1)); ray.direction points from the far plane back to the eye, so flip it
    // to march from the eye into the scene. For an orthographic view the helper origin
    // sits on the mid depth plane, so it is pushed upstream beyond the sphere.
    vec3 eye = ray.origin.xyz;
    vec3 o = eye;
    vec3 d = -normalize(ray.direction.xyz);

    float rMax = earthRadius + max(altMin, altMax);
    float rMin = earthRadius + min(altMin, altMax);
    if (orthographic) {
        o = eye - d * (2.0 * rMax);
    }

    // Intersect the ray with the outer sphere of the shell sector (d is normalised).
    float b = dot(o, d);
    float c = dot(o, o) - rMax * rMax;
    float disc = b * b - c;
    if (disc <= 0.0) {
        discard;
        return;
    }
    float sq = sqrt(disc);
    float t0 = -b - sq;
    float t1 = -b + sq;
    if (t1 <= 0.0) {
        // The whole outer sphere sits behind the ray origin
        discard;
        return;
    }
    t0 = max(t0, 0.0);

    // Clip the marching range with the sector bounding box - most of the earth chord
    // is empty space when a sector covers only a part of the globe.
    vec3 invD = 1.0 / d;
    vec3 tlo = (aabbMin - o) * invD;
    vec3 thi = (aabbMax - o) * invD;
    t0 = max(t0, max(max(min(tlo.x, thi.x), min(tlo.y, thi.y)), min(tlo.z, thi.z)));
    t1 = min(t1, min(min(max(tlo.x, thi.x), max(tlo.y, thi.y)), max(tlo.z, thi.z)));

    // Clip with the inner sphere so only the altitude range of the volume is sampled
    // instead of the whole sphere chord.
    float cIn = dot(o, o) - rMin * rMin;
    if (cIn > 0.0) {
        // Ray origin outside the inner sphere - stop at its near intersection
        float discIn = b * b - cIn;
        if (discIn > 0.0) {
            t1 = min(t1, -b - sqrt(discIn));
        }
    } else {
        // Ray origin inside the inner sphere - start at its far intersection
        t0 = max(t0, -b + sqrt(b * b - cIn));
    }
    if (t1 <= t0) {
        discard;
        return;
    }

    int sampleCount = max(depthSampleCount, 8);
    float dt = (t1 - t0) / float(sampleCount);
    float start = t0 + dt * dither();

    vec4 value = vec4(0.0, 0.0, 0.0, 0.0);
    float px = 0.0;
    float tt = t1;
    bool hasHit = false;

    // March front to back, from the camera side end of the sector to the far end.
    for (int count = 0; count < sampleCount; count++) {
        float t = start + float(count) * dt;
        if (t > t1) {
            break;
        }
        vec3 p = o + d * t;
        vec4 tp = shellTexPos(p);
        if (tp.w > 0.0) {
            float v = texture3D(tex, tp.xyz).r;
            vec4 pxColor = texture2D(colorMap, vec2(v, 0.0));

            if (castType == 0) {
                // Maximum value projection
                if (v > px) {
                    px = v;
                    tt = t;
                }
            } else {
                if (castType == 2) {
                    vec3 viewDir = normalize(p - eye);
                    vec3 normal = normalize(texture3D(normals, tp.xyz).xyz - 0.5);
                    float directional = clamp(dot(normal, lightVector), 0.0, 1.0);
                    float specular = max(dot(-viewDir, reflect(lightVector, normal)), 0.0);
                    specular = pow(specular, 3.0);
                    pxColor.rgb = ambientLight * pxColor.rgb
                            + directionalLight * directional * pxColor.rgb
                            + pxColor.a * specular * specularColor;
                }

                if (!hasHit && pxColor.a > 0.01) {
                    tt = t;
                    hasHit = true;
                }

                // Front to back compositing
                value = pxColor * pxColor.a * (1.0 - value.a) + value;
                if (value.a > 0.98) {
                    break;
                }
            }
        }
    }

    if (castType == 0) {
        value = texture2D(colorMap, vec2(px, 0.0));
    }

    mgl_FragColor = value * brightness;

    gl_FragDepth = calculateDepth(o + d * tt);
    if (value.a < 0.01) {
        gl_FragDepth = 1.0;
    }
}
