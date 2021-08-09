#if __VERSION__ >= 130
    #define attribute in
    #define varying out
#endif

attribute vec3 coordinates;
uniform mat4 MVP;

void main() {
    gl_Position = MVP*vec4(coordinates,1);
    //gl_Position = vec4(coordinates, 1);
}
