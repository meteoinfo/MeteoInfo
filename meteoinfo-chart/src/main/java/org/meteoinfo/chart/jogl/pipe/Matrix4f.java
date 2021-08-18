package org.meteoinfo.chart.jogl.pipe;

import org.joml.Vector3f;

public class Matrix4f extends org.joml.Matrix4f {
    final float EPSILON = 0.00001f;

    /**
     * Constructor
     */
    public Matrix4f() {
        super();
    }

    /**
     * Constructor
     * @param m JOML matrix
     */
    public Matrix4f(org.joml.Matrix4f m) {
        this.m00(m.m00()).
                m01(m.m01()).
                m02(m.m02()).
                m03(m.m03()).
                m10(m.m10()).
                m11(m.m11()).
                m12(m.m12()).
                m13(m.m13()).
                m20(m.m20()).
                m21(m.m21()).
                m22(m.m22()).
                m23(m.m23()).
                m30(m.m30()).
                m31(m.m31()).
                m32(m.m32()).
                m33(m.m33());
    }

    /**
     * Set the row at the given <code>row</code> index, starting with <code>0</code>.
     *
     * @param row the row index in <code>[0..3]</code>
     * @param src the row components to set
     * @return this
     * @throws IndexOutOfBoundsException if <code>row</code> is not in <code>[0..3]</code>
     */
    public Matrix4f setRow(int row, Vector3f src) throws IndexOutOfBoundsException {
        switch (row) {
            case 0:
                this.m00(src.x()).m10(src.y()).m20(src.z());
                return this;
            case 1:
                this.m01(src.x()).m11(src.y()).m21(src.z());
                return this;
            case 2:
                this.m02(src.x()).m12(src.y()).m22(src.z());
                return this;
            case 3:
                this.m03(src.x()).m13(src.y()).m23(src.z());
                return this;
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Set the column at the given <code>column</code> index, starting with <code>0</code>.
     *
     * @param column the column index in <code>[0..3]</code>
     * @param src the column components to set
     * @return this
     * @throws IndexOutOfBoundsException if <code>column</code> is not in <code>[0..3]</code>
     */
    public Matrix4f setColumn(int column, Vector3f src) throws IndexOutOfBoundsException {
        switch (column) {
            case 0:
                this.m00(src.x()).m01(src.y()).m02(src.z());
                return this;
            case 1:
                this.m10(src.x()).m11(src.y()).m12(src.z());
                return this;
            case 2:
                this.m20(src.x()).m21(src.y()).m22(src.z());
                return this;
            case 3:
                this.m30(src.x()).m31(src.y()).m32(src.z());
                return this;
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    /**
     * rotate matrix to face along the target direction
     * // NOTE: This function will clear the previous rotation and scale info and
     * // rebuild the matrix with the target vector. But it will keep the previous
     * // translation values.
     * // NOTE: It is for rotating object to look at the target, NOT for camera
     *
     * @param target Target direction
     * @return Rotated matrix
     */
    public Matrix4f lookAt(Vector3f target) {
        // compute forward vector and normalize
        Vector3f position = new Vector3f(m30(), m31(), m32());
        Vector3f forward = target.sub(position, new Vector3f());
        forward.normalize();
        Vector3f up = new Vector3f();             // up vector of object
        Vector3f left;           // left vector of object

        // compute temporal up vector
        // if forward vector is near Y-axis, use up vector (0,0,-1) or (0,0,1)
        if (Math.abs(forward.x) < EPSILON && Math.abs(forward.z) < EPSILON) {
            // forward vector is pointing +Y axis
            if (forward.y > 0)
                up.set(0, 0, -1);
                // forward vector is pointing -Y axis
            else
                up.set(0, 0, 1);
        } else {
            // assume up vector is +Y axis
            up.set(0, 1, 0);
        }

        // compute left vector
        left = up.cross(forward, new Vector3f());
        left.normalize();

        // re-compute up vector
        up = forward.cross(left, new Vector3f());
        //up.normalize();

        // NOTE: overwrite rotation and scale info of the current matrix
        this.setColumn(0, left);
        this.setColumn(1, up);
        this.setColumn(2, forward);

        return this;
    }

    /**
     * Multiply
     * @param rhs The Vector3f
     * @return Vector3f
     */
    public Vector3f mul(Vector3f rhs)
    {
        return new Vector3f(m00()*rhs.x + m10()*rhs.y + m20()*rhs.z + m30(),
                m01()*rhs.x + m11()*rhs.y + m21()*rhs.z + m31(),
                m02()*rhs.x + m12()*rhs.y + m22()*rhs.z + m32());
    }

    /**
     * Translate
     * @param x X
     * @param y Y
     * @param z Z
     * @return Translated matrix
     */
    public Matrix4f translate(float x, float y, float z) {
        m00(m00() + m03() * x);   m10(m10() + m13() * x);   m20(m20() + m23() * x);   m30(m30() + m33() * x);
        m01(m01() + m03() * y);   m11(m11() + m13() * y);   m21(m21() + m23() * y);   m31(m31() + m33() * y);
        m02(m02() + m03() * z);   m12(m12() + m13() * z);   m22(m22() + m23() * z);   m32(m32() + m33() * z);
        return this;
    }

    /**
     * Translate
     * @param offset Offset
     * @return Translated matrix
     */
    public Matrix4f translate(Vector3f offset) {
        return translate(offset.x, offset.y, offset.z);
    }
}
