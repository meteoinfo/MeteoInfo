#version 330 core

layout(location = 0) in vec3 coordinates;
uniform mat4 MVP;

void main() {
    gl_Position = MVP*vec4(coordinates,1);
    //gl_Position = vec4(coordinates, 1);
}
