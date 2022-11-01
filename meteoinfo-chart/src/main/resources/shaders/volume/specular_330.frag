#version 330 core

out vec4 mgl_FragColor;

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

vec3 ambientLight = vec3(0.34, 0.32, 0.32);
vec3 directionalLight = vec3(0.5, 0.5, 0.5);
vec3 lightVector = normalize(vec3(-1.0, -1.0, 1.0));
vec3 specularColor = vec3(0.5, 0.5, 0.5);

vec3 aabb[2] = vec3[2](
    vec3(-1.0, -1.0, -1.0),
    vec3(1.0, 1.0, 1.0)
);

struct Ray {
    vec3 origin;
    vec3 direction;
    vec3 inv_direction;
    int sign[3];
};

Ray makeRay(vec3 origin, vec3 direction) {
    vec3 inv_direction = vec3(1.0) / direction;

    return Ray(
        origin,
        direction,
        inv_direction,
        int[3](
            ((inv_direction.x < 0.0) ? 1 : 0),
            ((inv_direction.y < 0.0) ? 1 : 0),
            ((inv_direction.z < 0.0) ? 1 : 0)
        )
    );
}

Ray createRayOrthographic(vec2 uv)
{
    float far = 5.0f;

    // Transform the camera origin to world space
    vec4 origin = iP * vec4(uv, 0.0f, 1.0f);
    origin = iV * origin;
    origin = origin / origin.w;

    // Invert the perspective projection of the view-space position
    vec4 image = iP * vec4(uv, far, 1.0f);
    // Transform the direction from camera to world space and normalize
    image = iV* image;
    vec4 direction = normalize(origin - image);
    return makeRay(origin.xyz, direction.xyz);
}

Ray createRayPerspective(vec2 uv)
{
    // Transform the camera origin to world space
    vec4 origin = iP * vec4(0.0f, 0.0f, 0.0f, 1.0f);
    origin = iV * origin;
    origin = origin / origin.w;

    // Invert the perspective projection of the view-space position
    vec4 image = iP * vec4(uv, 1.0f, 1.0f);
    // Transform the direction from camera to world space and normalize
    image = iV * image;
    image = image / image.w;
    vec4 direction = normalize(origin - image);
    return makeRay(origin.xyz, direction.xyz);
}

void intersect(
    in Ray ray, in vec3 aabb[2],
    out float tmin, out float tmax
){
    float tymin, tymax, tzmin, tzmax;
    tmin = (aabb[ray.sign[0]].x - ray.origin.x) * ray.inv_direction.x;
    tmax = (aabb[1-ray.sign[0]].x - ray.origin.x) * ray.inv_direction.x;
    tymin = (aabb[ray.sign[1]].y - ray.origin.y) * ray.inv_direction.y;
    tymax = (aabb[1-ray.sign[1]].y - ray.origin.y) * ray.inv_direction.y;
    tzmin = (aabb[ray.sign[2]].z - ray.origin.z) * ray.inv_direction.z;
    tzmax = (aabb[1-ray.sign[2]].z - ray.origin.z) * ray.inv_direction.z;
    tmin = max(max(tmin, tymin), tzmin);
    tmax = min(min(tmax, tymax), tzmax);
}

void main(){
    vec2 vUV = 2.0 * (gl_FragCoord.xy + vec2(0.5, 0.5) - viewShift) / viewSize - 1.0;
    Ray ray;
    if (orthographic) {
        ray = createRayOrthographic(vUV);
    } else {
        ray = createRayPerspective(vUV);
    }
    vec3 aabb[2] = vec3[2](aabbMin, aabbMax);
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

    for(int count = 0; count < sampleCount; count++){

        texCo = mix(end, start, float(count)/float(sampleCount));// - originOffset;

        //texCo = start + increment*float(count);
        px = texture(tex, texCo).r;


        //px = length(texture(normals, texCo).xyz - 0.5);
        //px = px * 1.5;

        pxColor = texture(colorMap, vec2(px, 0.0));

        normal = normalize(texture(normals, texCo).xyz - 0.5);
        float directional = clamp(dot(normal, lightVector), 0.0, 1.0);

        //vec3 R = -reflect(lightDirection, surfaceNormal);
        //return pow(max(0.0, dot(viewDirection, R)), shininess);

        float specular = max(dot(ray.direction.xyz, reflect(lightVector, normal)), 0.0);
        specular = pow(specular, 3.0);

        pxColor.rgb = ambientLight*pxColor.rgb + directionalLight*directional*pxColor.rgb + pxColor.a*specular*specularColor;


        //value = mix(value, pxColor, px);
        //value = (1.0-value.a)*pxColor + value;
        //value = mix(pxColor, zero, value.a) + value;

        value = value + pxColor - pxColor*value.a;

        if(value.a >= 0.95){
            break;
        }
    }

    mgl_FragColor = value * brightness;
}