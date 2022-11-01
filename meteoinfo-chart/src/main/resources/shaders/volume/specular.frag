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
uniform vec3 aabbMin;
uniform vec3 aabbMax;

uniform bool orthographic;

uniform float brightness;

#include volumeBase.glsl

vec3 ambientLight = vec3(0.34, 0.32, 0.32);
vec3 directionalLight = vec3(0.5, 0.5, 0.5);
vec3 lightVector = normalize(vec3(-1.0, -1.0, 1.0));
vec3 specularColor = vec3(0.5, 0.5, 0.5);

void main(){
    vec2 vUV = 2.0 * (gl_FragCoord.xy + vec2(0.5, 0.5) - viewShift) / viewSize - 1.0;
    Ray ray;
    if (orthographic) {
        ray = createRayOrthographic(vUV);
    } else {
        ray = createRayPerspective(vUV);
    }
    vec3 aabb[2];
    aabb[0] = aabbMin;
    aabb[1] = aabbMax;
    float tmin = 0.0;
    float tmax = 0.0;
    intersect(ray, aabb, tmin, tmax);

    vec4 value = vec4(0.0, 0.0, 0.0, 0.0);
    if (tmax < tmin){
        discard;
        return;
    }
    vec3 start = (ray.origin.xyz + tmin*ray.direction.xyz - aabb[0])/(aabb[1]-aabb[0]);
    vec3 end = (ray.origin.xyz + tmax*ray.direction.xyz - aabb[0])/(aabb[1]-aabb[0]);

    float len = distance(end, start);
    int sampleCount = int(float(depthSampleCount)*len);

    float s = 0.0;
    float px = 0.0;
    vec4 pxColor = vec4(0.0, 0.0, 0.0, 0.0);
    vec3 texCo = vec3(0.0, 0.0, 0.0);
    vec3 normal = vec3(0.0, 0.0, 0.0);
    vec4 zero = vec4(0.0);
    float tt = tmin;
    for(int count = 0; count < sampleCount; count++){

        texCo = mix(end, start, float(count)/float(sampleCount));// - originOffset;

        //texCo = start + increment*float(count);
        px = texture3D(tex, texCo).r;


        //px = length(texture(normals, texCo).xyz - 0.5);
        //px = px * 1.5;

        pxColor = texture2D(colorMap, vec2(px, 0.0));

        normal = normalize(texture3D(normals, texCo).xyz - 0.5);
        float directional = clamp(dot(normal, lightVector), 0.0, 1.0);

        //vec3 R = -reflect(lightDirection, surfaceNormal);
        //return pow(max(0.0, dot(viewDirection, R)), shininess);

        float specular = max(dot(ray.direction.xyz, reflect(lightVector, normal)), 0.0);
        specular = pow(specular, 3.0);

        pxColor.rgb = ambientLight*pxColor.rgb + directionalLight*directional*pxColor.rgb + pxColor.a*specular*specularColor;


        //value = mix(value, pxColor, px);
        //value = (1.0-value.a)*pxColor + value;
        //value = mix(pxColor, zero, value.a) + value;

        //value = value + pxColor - pxColor*value.a;
        blendToBack(value, pxColor);

        if (tt == tmin && pxColor.a > 0.01) {
            tt = tmax - float(count)/float(sampleCount)*(tmax - tmin);
        }

        if(value.a >= 0.95){
            break;
        }
    }

    mgl_FragColor = value * brightness;

    gl_FragDepth = calculateDepth(ray.origin + ray.direction * tt);
    if (value.a < 0.01) {
        gl_FragDepth = 1.0;
    }
}