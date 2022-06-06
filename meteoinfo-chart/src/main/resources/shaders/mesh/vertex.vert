#if __VERSION__ >= 130
    #define attribute in
    #define varying out
#endif

// uniforms
uniform mat4 matrixModelView;
uniform mat4 matrixNormal;
uniform mat4 matrixModelViewProjection;

// vertex attribs (input)
attribute vec3 vertexPosition;
attribute vec3 vertexNormal;

// varyings (output)
varying vec3 esVertex, esNormal;

void main()
{
    esVertex = vec3(matrixModelView * vec4(vertexPosition, 1.0));
    esNormal = vec3(matrixNormal * vec4(vertexNormal, 1.0));
    gl_Position = matrixModelViewProjection * vec4(vertexPosition, 1.0);
}
