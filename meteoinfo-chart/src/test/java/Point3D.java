public class Point3D {
    public double x;
    public double y;
    public double z;

    public Point3D() {

    }

    public Point3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double[] toArray() {
        return new double[]{x, y, z};
    }
}
