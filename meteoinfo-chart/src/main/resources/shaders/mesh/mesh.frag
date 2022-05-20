#if __VERSION__ >= 130
    #define varying in
    out vec4 mgl_FragColor;
#else
    #define mgl_FragColor gl_FragColor
#endif

uniform vec4 color;
uniform vec4 lightPosition;

varying vec3 Normal;
varying vec3 eyePosition;

void main(){

    // Normalize the transformed normal vector - general code, unnecessary
    // in this case as the vector should already be normalized.
    vec3 n = normalize(Normal);

    // Where is the light relative to the fragment?
    vec3 s = normalize(vec3(lightPosition) - eyePosition);

    // Where is the viewer relative to the fragment?
    vec3 v = normalize(-eyePosition);

    // Direction of reflected light
    vec3 r = reflect(-s, n);

    // Half-vector to reduce numerical operations since processing many
    // fragments.
    vec3 h = normalize(v + s);

    // Hard code - ambient surface reflectivity and light color/intensity.
    vec3 Ka = vec3(0.3, 0.3, 0.3);
    vec3 La = vec3(color);

    // Hard code - diffuse surface reflectivity and light color/intensity.
    vec3 Kd = vec3(0.75, 0.75, 0.75);
    vec3 Ld = vec3(color);

    // Hard code - specular surface reflectivity and light color/intensity.
    vec3 Ks = vec3(0.2, 0.2, 0.2);
    vec3 Ls = vec3(1.0,1.0,1.0);

    // Compute ambient and diffuse color and intensity.
    vec3 ambient = La * Ka;
    float sDotN = max( dot(s, n), 0.0);
    vec3 diffuse = Ld * Kd * sDotN;

    // Compute specular component.
    vec3 spec = vec3(0.0);
    float Shininess = 25;
    if (sDotN > 0.0)
    {
        spec = Ls * Ks * pow(max(dot(h,n), 0.0), Shininess);
    }

    // Compute overall fragment illumination.
    vec3 res = ambient + diffuse + spec;

    // And combine with alpha value for final fragment color.
    mgl_FragColor = vec4(res, color.a);
}