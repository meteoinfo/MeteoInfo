package org.meteoinfo.chart.jogl;

import com.jogamp.opengl.GL2;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.jogamp.opengl.GL2ES2.*;
import static com.jogamp.opengl.GL3ES3.GL_COMPUTE_SHADER;
import static com.jogamp.opengl.GL3ES3.GL_GEOMETRY_SHADER;

public class Program {
    static final List<Program> programs = new Vector<>();
    private final String name;

    Integer programId = null;
    private final Map<Integer, String> shaderCode = new HashMap<>();
    private final Map<Integer, Integer> shaderIds = new HashMap<>();

    final static Map<String, Integer> attributeLocations = new HashMap<>();
    final static Map<String, Integer> uniformLocations = new HashMap<>();
    final static Map<String, BiConsumer<GL2, Integer>> uniforms = new HashMap<>();


    public Program(File file) throws IOException {
        this.name = null;
        //file = Utils.getFilePath(file);
        final File directory = file.isDirectory() ? file : new File(file.getParent());
        final String mask = file.isDirectory() ? "" : file.getName();
        for (final File tmpFile : directory.listFiles()) {

            if (tmpFile.isDirectory() || !tmpFile.getName().startsWith(mask)) {
                continue;
            }
            switch (tmpFile.getName().substring(tmpFile.getName().lastIndexOf(".") + 1)) {
                case "frag":
                case "fs":
                    shaderCode.put(GL_FRAGMENT_SHADER, Files.readAllLines(Paths.get(tmpFile.getAbsolutePath())).stream().collect(Collectors.joining("\n")));
                    break;
                case "vert":
                case "vs":
                    shaderCode.put(GL_VERTEX_SHADER, Files.readAllLines(Paths.get(tmpFile.getAbsolutePath())).stream().collect(Collectors.joining("\n")));
                    break;
                case "cs":
                case "compute":
                    shaderCode.put(GL_COMPUTE_SHADER, Files.readAllLines(Paths.get(tmpFile.getAbsolutePath())).stream().collect(Collectors.joining("\n")));
                    break;
                case "gs":
                case "geom":
                    shaderCode.put(GL_GEOMETRY_SHADER, Files.readAllLines(Paths.get(tmpFile.getAbsolutePath())).stream().collect(Collectors.joining("\n")));
                    break;
                default:
                    throw new UnsupportedOperationException(String.format("Program cannot read type of shader from file extension of %s ", tmpFile));
            }
        }
    }

    public Program(String name, File vertexShaderFile, File fragmentShaderFile) throws IOException {
        this.name = name;
        //shaderCode.put(GL_VERTEX_SHADER, Files.readAllLines(Paths.get(Utils.getFilePath(vertexShaderFile).getAbsolutePath())).stream().collect(Collectors.joining("\n")));
        //shaderCode.put(GL_FRAGMENT_SHADER, Files.readAllLines(Paths.get(Utils.getFilePath(fragmentShaderFile).getAbsolutePath())).stream().collect(Collectors.joining("\n")));
        try {
            String vertexShaderCode = Utils.loadResource(vertexShaderFile
                    .getPath());
            String fragmentShaderCode = Utils.loadResource(fragmentShaderFile
                    .getPath());
            shaderCode.put(GL_VERTEX_SHADER, vertexShaderCode);
            shaderCode.put(GL_FRAGMENT_SHADER, fragmentShaderCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Program(String name, String vertexShaderCode, String fragmentShaderCode) {
        this.name = name;
        shaderCode.put(GL_VERTEX_SHADER, vertexShaderCode);
        shaderCode.put(GL_FRAGMENT_SHADER, fragmentShaderCode);
    }

    public Integer getProgramId() {
        return programId;
    }

    public void init(GL2 gl) {
        if (programId != null) {
            return;
        }
        final ByteBuffer infoLog = ByteBuffer.allocate(512);
        final IntBuffer success = IntBuffer.allocate(1);
        for (final Map.Entry<Integer, String> shader : shaderCode.entrySet()) {
            final int shaderId = gl.glCreateShader(shader.getKey());
            shaderIds.put(shader.getKey(), shaderId);
            gl.glShaderSource(shaderId, 1, new String[]{shader.getValue()}, null);
            gl.glCompileShader(shaderId);
            gl.glGetShaderiv(shaderId, GL_COMPILE_STATUS, success);
            if (success.get(0) != 1) {
                gl.glGetShaderInfoLog(shaderId, 512, null, infoLog);
                System.out.println(new String(infoLog.array()));
            }
        }


        programId = gl.glCreateProgram();
        for (final Map.Entry<Integer, Integer> shaderId : shaderIds.entrySet()) {

            gl.glAttachShader(programId, shaderId.getValue());
        }
        programs.add(this);

        if (shaderIds.size() == 0) {
            return;
        }
        gl.glLinkProgram(programId);

        gl.glGetProgramiv(programId, GL_LINK_STATUS, success);
        if (success.get(0) != 1) {
            gl.glGetProgramInfoLog(programId, 512, null, infoLog);
            System.out.println(new String(infoLog.array()));
        }
        for (final Map.Entry<Integer, Integer> shaderId : shaderIds.entrySet()) {

            gl.glDetachShader(programId, shaderId.getValue());
            gl.glDeleteShader(shaderId.getValue());
        }


    }

    public void destroy(GL2 gl) {
        if (programId != null) {
            for (final Map.Entry<Integer, Integer> shaderId : shaderIds.entrySet()) {
                gl.glDetachShader(programId, shaderId.getValue());
                gl.glDeleteShader(shaderId.getValue());
            }
            gl.glDeleteProgram(programId);
            programId = null;
        }
    }

    public void use(GL2 gl) {
        if (programId == null)
            init(gl);

        gl.glUseProgram(programId);
    }

    public void allocateUniform(GL2 gl, String uniformName, BiConsumer<GL2, Integer> function) {
        init(gl);
        final int uniformLocation = gl.glGetUniformLocation(programId, uniformName);
        uniformLocations.put(uniformName, uniformLocation);
        uniforms.put(uniformName, function);

    }

    public void setUniforms(GL2 gl) {

        for (final Map.Entry<String, BiConsumer<GL2, Integer>> uniform : uniforms.entrySet()) {
            uniform.getValue().accept(gl, uniformLocations.get(uniform.getKey()));
        }
    }

    public static void destroyAllPrograms(GL2 gl) {
        for (final Program program : programs) {
            program.destroy(gl);
        }
        programs.clear();
    }

    public static void unUse(GL2 gl) {
        gl.glUseProgram(0);
    }

    @Override
    public String toString() {
        if (name == null) {
            return super.toString();
        }
        return name;
    }
}
