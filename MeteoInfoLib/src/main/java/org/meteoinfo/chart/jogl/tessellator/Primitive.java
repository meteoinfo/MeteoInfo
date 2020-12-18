package org.meteoinfo.chart.jogl.tessellator;

import com.jogamp.opengl.GL2;
import org.meteoinfo.shape.PointZ;

import java.util.ArrayList;

public class Primitive {
    public final int type;
    public final ArrayList<PointZ> vertices = new ArrayList<>();

    public Primitive(int type) {
        this.type = type;
    }

    public String getTypeString() {
        switch(type) {
            case GL2.GL_TRIANGLES: return "GL_TRIANGLES";
            case GL2.GL_TRIANGLE_STRIP: return "GL_TRIANGLE_STRIP";
            case GL2.GL_TRIANGLE_FAN: return "GL_TRIANGLE_FAN";
            default: return Integer.toString(type);
        }
    }

    @Override
    public String toString() {
        String s = "New Primitive " + getTypeString();
        for(int i = 0; i < vertices.size(); i++) {
            s += "\nIndex: " + vertices.get(i);
        }
        return s;
    }
}
