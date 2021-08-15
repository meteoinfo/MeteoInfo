import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Polygon {
    private Color color = Color.cyan;
    private List<Point3D> outline;
    private List<List<Point3D>> holes;

    public Polygon() {

    }

    public Polygon(Color color, List<Point3D> outline, List<List<Point3D>> holes) {
        this.color = color;
        this.outline = outline;
        this.holes = holes;
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color value) {
        this.color = value;
    }

    public List<Point3D> getOutline() {
        return this.outline;
    }

    public void setOutline(List<Point3D> outline) {
        this.outline = outline;
    }

    public List<List<Point3D>> getHoles() {
        return this.holes;
    }

    public void setHoles(List<List<Point3D>> value) {
        this.holes = value;
    }

    public List<Point3D> getHole(int idx) {
        return this.holes.get(idx);
    }

    public boolean hasHole() {
        return !(this.holes == null);
    }

    public int getHoleNumber() {
        return this.holes == null ? 0 : this.holes.size();
    }

    public void addHole(List<Point3D> hole) {
        if (this.holes == null)
            this.holes = new ArrayList<>();
        this.holes.add(hole);
    }
}
