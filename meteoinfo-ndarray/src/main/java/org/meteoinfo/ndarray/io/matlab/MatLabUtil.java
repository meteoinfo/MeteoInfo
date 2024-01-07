package org.meteoinfo.ndarray.io.matlab;

import com.google.common.base.Charsets;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.Index;
import us.hebi.matlab.mat.types.MatlabType;
import us.hebi.matlab.mat.types.Matrix;
import us.hebi.matlab.mat.util.Bytes;

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
                    array.setInt(index, matArray.getInt(i));
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
                }
                break;
            case LONG:
            case ULONG:
                for (int i = 0; i < array.getSize(); i++) {
                    array.setLong(index, matArray.getLong(i));
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
                }
                break;
            case FLOAT:
                for (int i = 0; i < array.getSize(); i++) {
                    array.setFloat(index, matArray.getFloat(i));
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
                }
                break;
            case DOUBLE:
                for (int i = 0; i < array.getSize(); i++) {
                    array.setDouble(index, matArray.getDouble(i));
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
                }
                break;
        }

        if (ndim >= 3) {
            int[] shapeIdx = new int[ndim];
            int idx = 0;
            for (int i = ndim - 1; i > 1; i--) {
                shapeIdx[idx] = i;
                idx += 1;
            }
            shapeIdx[idx] = 0;
            shapeIdx[idx + 1] = 1;
            array = array.permute(shapeIdx);
        }

        return array;
    }

    public static String parseAsciiString(byte[] buffer) {
        return parseAsciiString(buffer, 0, buffer.length);
    }

    public static String parseAsciiString(byte[] buffer, int offset, int maxLength) {
        // Stop at String end character
        int length = Bytes.findFirst(buffer, offset, maxLength, (byte) '\0', maxLength);

        // Remove right-side trailing spaces
        while (length > 0 && buffer[length - 1] == ' ') {
            length--;
        }

        // Convert to String
        return length == 0 ? "" : new String(buffer, offset, length, Charsets.US_ASCII);
    }
}
