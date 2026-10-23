package org.meteoinfo.chart.graphic;

import org.joml.Vector3f;

import java.awt.*;

public class ColoredVector3f {
    private Vector3f vector;
    private Color color;

    public ColoredVector3f() {}

    public ColoredVector3f(Vector3f vector, Color color) {
        this.vector = vector;
        this.color = color;
    }

    public void set(Vector3f vector, Color color) {
        this.vector = vector;
        this.color = color;
    }

    public Vector3f getVector() {
        return vector;
    }

    public void setVector(Vector3f vector) {
        this.vector = vector;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ColoredVector3f other = (ColoredVector3f) obj;

        return vector.equals(other.vector) && color.equals(other.color);
    }
}
