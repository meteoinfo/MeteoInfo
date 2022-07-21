
struct Ray {
    vec3 origin;
    vec3 direction;
    vec3 inv_direction;
    int sign[3];
};

Ray makeRay(vec3 origin, vec3 direction) {
    vec3 inv_direction = vec3(1.0) / direction;
    int sign[3];
    sign[0] = inv_direction.x < 0.0 ? 1 : 0;
    sign[1] = inv_direction.y < 0.0 ? 1 : 0;
    sign[2] = inv_direction.z < 0.0 ? 1 : 0;

    return Ray(
        origin,
        direction,
        inv_direction,
        sign
    );
}

Ray createRay(vec2 uv)
{
    float far = 5.0;

    // Transform the camera origin to world space
    vec4 origin = iP * vec4(uv, 0.0, 1.0);
    origin = iV * origin;
    origin = origin / origin.w;

    // Invert the perspective projection of the view-space position
    vec4 image = iP * vec4(uv, far, 1.0);
    // Transform the direction from camera to world space and normalize
    image = iV* image;
    vec4 direction = normalize(origin - image);
    return makeRay(origin.xyz, direction.xyz);
}

Ray createRayOrthographic(vec2 uv)
{
    float far = 5.0;

    // Transform the camera origin to world space
    vec4 origin = iP * vec4(uv, 0.0, 1.0);
    origin = iV * origin;
    origin = origin / origin.w;

    // Invert the perspective projection of the view-space position
    vec4 image = iP * vec4(uv, far, 1.0);
    // Transform the direction from camera to world space and normalize
    image = iV* image;
    vec4 direction = normalize(origin - image);
    return makeRay(origin.xyz, direction.xyz);
}

Ray createRayPerspective(vec2 uv)
{
    // Transform the camera origin to world space
    vec4 origin = iP * vec4(0.0, 0.0, 0.0, 1.0);
    origin = iV * origin;
    origin = origin / origin.w;

    // Invert the perspective projection of the view-space position
    vec4 image = iP * vec4(uv, 1.0, 1.0);
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

vec4 premultiplyAlpha(vec4 color)
{
    //return vec4(color.rgb * color.a, color.a);
    return color * color.a;
}

// GL_ONE_MINUS_DST_ALPHA, GL_ONE
void blendToBack(inout vec4 accum, vec4 color)
{
    //accum = color * color.a * (1-accum.a) + accum;
    accum = premultiplyAlpha(color) * (1.0 - accum.a) + accum;
}

float calculateDepth(vec3 pos)
{
    vec4 ndc = MVP * vec4(pos, 1.0);
    ndc.xyz /= ndc.w;
    return 0.5 * (gl_DepthRange.diff * ndc.z + (gl_DepthRange.near + gl_DepthRange.far));
    //return ndc.z * 0.5 + 0.5;
}
