package org.meteoinfo.math.spatial.transform;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;

public class TransformUtil {
    /**
     * Quaternion rotation of 3D axis
     * @param quaternionRotation The rotation
     * @param array Input array
     * @return Rotated array
     */
    public static Array rotation(QuaternionRotation quaternionRotation, Array array) {
        array = array.copyIfView();
        if (array.getRank() == 1) {
            Vector3D v1 = Vector3D.of(array.getDouble(0), array.getDouble(1), array.getDouble(2));
            Vector3D v2 = quaternionRotation.apply(v1);
            return Array.factory(DataType.DOUBLE, array.getShape(), v2.toArray());
        }
        int[] shape = array.getShape();
        int ni = shape[0];
        int nj = shape[1];
        Array r = Array.factory(DataType.DOUBLE, array.getShape());
        for (int i = 0; i < array.getSize(); i += 3) {
            Vector3D v1 = Vector3D.of(array.getDouble(i), array.getDouble(i + 1), array.getDouble(i + 2));
            Vector3D v2 = quaternionRotation.apply(v1);
            r.setDouble(i, v2.getX());
            r.setDouble(i + 1, v2.getY());
            r.setDouble(i + 2, v2.getZ());
        }
        return r;
    }
}
