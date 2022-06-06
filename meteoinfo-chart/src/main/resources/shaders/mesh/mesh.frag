#if __VERSION__ >= 130
    #define varying in
    out vec4 mgl_FragColor;
#else
    #define mgl_FragColor gl_FragColor
#endif

// uniforms
uniform vec4 color;
uniform vec4 lightPosition;             // should be in the eye space
uniform vec4 lightAmbient;              // light ambient color
uniform vec4 lightDiffuse;              // light diffuse color
uniform vec4 lightSpecular;             // light specular color

// varyings
varying vec3 esVertex, esNormal;

void main()
{
    vec3 normal = normalize(esNormal);
    vec3 light;
    if(lightPosition.w == 0.0)
    {
        light = normalize(lightPosition.xyz);
    }
    else
    {
        light = normalize(lightPosition.xyz - esVertex);
    }
    vec3 view = normalize(-esVertex);
    vec3 halfv = normalize(light + view);

    vec3 fragColor = lightAmbient.rgb * color.rgb;                  // begin with ambient
    float dotNL = max(dot(normal, light), 0.0);
    fragColor += lightDiffuse.rgb * color.rgb * dotNL;              // add diffuse
    float dotNH = max(dot(normal, halfv), 0.0);
    fragColor += pow(dotNH, 128.0) * lightSpecular.rgb * color.rgb; // add specular

    // set frag color
    mgl_FragColor = vec4(fragColor, color.a);  // set opaque
}