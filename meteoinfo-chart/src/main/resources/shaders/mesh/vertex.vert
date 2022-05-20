#if __VERSION__ >= 130
    #define attribute in
    #define varying out
#endif

attribute vec3 coordinates;
attribute vec3 normal;

uniform mat4 MVP;
uniform mat4 MV;
uniform mat4 MVI;
uniform mat4 transMatrix;

varying vec3 Normal;
varying vec3 eyePosition;

void main() {

    // We transform the raw normal using the mvitMx, which provides for a more
    // general solution (anisotropic scaling).
    //mat4 normalMatrix = MVI;
    mat3 normalMatrix = mat3(MV);

    // And make sure it is a unit-length vector.
    //Normal = vec3(normalize(normalMatrix * vec4(normal, 0)));
    Normal = normalize(normalMatrix * normal);

    // Where is the vertex with respect to the viewer, i.e., without perspective
    // transformation.
    vec4 eyeCoords = MV * vec4(coordinates, 1.0);

    // Eye position.
    eyePosition = vec3(eyeCoords);

    // Backtransform hardcoded light position (50,75,0100) such that the light
    // remains fixed when the object(s) rotate.
    //lightPosition = inverse(MVP) * vec4(50, 75, -100, 1.0);
    //lightPosition = vec4(50, 75, 100, 1.0);

    gl_Position = MVP * vec4(coordinates, 1);
}
