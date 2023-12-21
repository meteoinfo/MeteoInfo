package org.meteoinfo.data.meteodata.matlab;

import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.Index;
import us.hebi.matlab.mat.types.MatlabType;
import us.hebi.matlab.mat.types.Matrix;

public class MatLabUtil {

    /**
     * Convert MatLab data type to MeteoInfo data type
     * @param matlabType MatLab data type
     * @return MeteoInfo data type
     */
    public static DataType fromMatLabDataType(MatlabType matlabType) {
        switch (matlabType) {
            case Int8:
            case Int16:
            case Int32:
                return DataType.INT;
            case Int64:
                return DataType.LONG;
            case UInt8:
            case UInt16:
            case UInt32:
                return DataType.UINT;
            case UInt64:
                return DataType.ULONG;
            case Single:
                return DataType.FLOAT;
            case Double:
                return DataType.DOUBLE;
            default:
                return DataType.OBJECT;
        }
    }

    /**
     * Convert MatLab array to MeteoInfo array
     * @param matArray MatLab array
     * @return MeteoInfo Array
     */
    public static Array fromMatLabArray(Matrix matArray) {
        DataType dataType = fromMatLabDataType(matArray.getType());
        int[] shape = matArray.getDimensions();
        int ndim = shape.length;
        Array array = Array.factory(dataType, shape);
        Index index = array.getIndex();
        int[] current;
        switch (dataType) {
            case INT:
            case UINT:
                for (int i = 0; i < array.getSize(); i++) {
                    current = index.getCurrentCounter();
                    for (int j = 0; j < ndim; j++) {
                        if (current[j] < shape[j] - 1) {
                            current[j] = current[j] + 1;
                            break;
                        } else {
                            current[j] = 0;
                        }
                    }
                    index.set(current);
                    array.setInt(index, matArray.getInt(i));
                }
                break;
            case LONG:
            case ULONG:
                for (int i = 0; i < matArray.getNumElements(); i++) {
                    array.setLong(i, matArray.getLong(i));
                }
                break;
            case FLOAT:
                for (int i = 0; i < matArray.getNumElements(); i++) {
                    array.setFloat(i, matArray.getFloat(i));
                }
                break;
            case DOUBLE:
                for (int i = 0; i < matArray.getNumElements(); i++) {
                    array.setDouble(i, matArray.getDouble(i));
                }
                break;
        }

        return array;
    }
}
