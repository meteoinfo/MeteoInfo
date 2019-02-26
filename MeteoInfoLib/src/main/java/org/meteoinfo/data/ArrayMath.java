/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.meteoinfo.math.meteo.MeteoMath;
import org.meteoinfo.data.analysis.Statistics;
import org.meteoinfo.geoprocess.GeoComputation;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointD;
import org.meteoinfo.layer.VectorLayer;
import org.meteoinfo.ma.ArrayBoolean;
import org.meteoinfo.math.Complex;
import org.meteoinfo.shape.PolygonShape;
import org.python.core.PyComplex;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.ma2.Range;

/**
 *
 * @author wyq
 */
public class ArrayMath {

    public static double fill_value = -9999.0;

    // <editor-fold desc="Data type">
    /**
     * Get data type
     *
     * @param o Object
     * @return Data type
     */
    public static DataType getDataType(Object o) {
        if (o instanceof Integer) {
            return DataType.INT;
        } else if (o instanceof Float) {
            return DataType.FLOAT;
        } else if (o instanceof Double) {
            return DataType.DOUBLE;
        } else if (o instanceof Boolean) {
            return DataType.BOOLEAN;
        } else {
            return DataType.OBJECT;
        }
    }

    private static DataType commonType(DataType aType, DataType bType) {
        if (aType == bType) {
            return aType;
        }

        if (aType == DataType.OBJECT || bType == DataType.OBJECT) {
            return DataType.OBJECT;
        }

        short anb = ArrayMath.typeToNBytes(aType);
        short bnb = ArrayMath.typeToNBytes(bType);
        if (anb == bnb) {
            switch (aType) {
                case INT:
                case LONG:
                    return bType;
                case FLOAT:
                case DOUBLE:
                    return aType;
            }
        }

        return (anb > bnb) ? aType : bType;
    }

    /**
     * Return the number of bytes per element for the given typecode.
     *
     * @param dataType Data type
     * @return Bytes number
     */
    public static short typeToNBytes(final DataType dataType) {
        switch (dataType) {
            case BYTE:
                return 1;
            case SHORT:
                return 2;
            case INT:
            case FLOAT:
                return 4;
            case LONG:
            case DOUBLE:
                return 8;
            default:
                return 0;
        }
    }

    /**
     * Check if an array is complex data type
     *
     * @param a The array
     * @return Complex data type or not
     */
    public static boolean isComplex(Array a) {
        Object a0 = a.getObject(0);
        return a0 instanceof Complex;
    }

    /**
     * Check if an array is numeric array
     *
     * @param a The array
     * @return Numeric or not
     */
    public static boolean isNumeric(Array a) {
        boolean r = a.getDataType().isNumeric();
        if (!r) {
            r = isComplex(a);
        }
        return r;
    }

    // </editor-fold>
    // <editor-fold desc="Arithmetic">
    /**
     * Broadcast check for two arrays.
     *
     * @param a Array a
     * @param b Array b
     * @return Can broadcast (1), can not broadcast (-1), same dimensions (0)
     */
    public static int broadcastCheck(Array a, Array b) {
        int[] ashape = a.getShape();
        int[] bshape = b.getShape();
        int n = ashape.length;
        int m = bshape.length;
        if (n != m) {
            int len = Math.min(n, m);
            int na, nb;
            for (int i = 0; i < len; i++) {
                na = ashape[n - i - 1];
                nb = bshape[m - i - 1];
                if (na != nb && na != 1 && nb != 1) {
                    return -1;
                }
            }
            return 1;
        } else {
            boolean sameDim = true;
            for (int i = 0; i < n; i++) {
                if (ashape[i] != bshape[i]) {
                    sameDim = false;
                    break;
                }
            }
            if (sameDim) {
                return 0;
            } else {
                for (int i = 0; i < n; i++) {
                    if (ashape[i] != bshape[i] && ashape[i] != 1 && bshape[i] != 1) {
                        return -1;
                    }
                }
                return 1;
            }
        }
    }

    /**
     * Get broadcast shape from two arrays
     *
     * @param a Array a
     * @param b Array b
     * @return Broadcast shape
     */
    public static int[] broadcast(Array a, Array b) {
        int[] ashape = a.getShape();
        int[] bshape = b.getShape();
        int n = ashape.length;
        int m = bshape.length;
        if (n == m) {
            int[] shape = new int[n];
            for (int i = 0; i < n; i++) {
                shape[i] = Math.max(ashape[i], bshape[i]);
            }
            return shape;
        } else {
            int len = Math.max(n, m);
            int[] shape = new int[len];
            int na, nb;
            for (int i = 0; i < len; i++) {
                if (m < n) {
                    na = ashape[n - i - 1];
                    if (m - i - 1 >= 0) {
                        nb = bshape[m - i - 1];
                        shape[n - i - 1] = Math.max(na, nb);
                    } else {
                        shape[n - i - 1] = na;
                    }
                } else {
                    nb = bshape[m - i - 1];
                    if (n - i - 1 >= 0) {
                        na = ashape[n - i - 1];
                        shape[m - i - 1] = Math.max(na, nb);
                    } else {
                        shape[m - i - 1] = nb;
                    }
                }
            }
            return shape;
        }
    }

    private static void setIndex(int broadcast, Index aindex, Index bindex, int[] current, int n, int na, int nb) {
        if (broadcast == 0) {
            aindex.set(current);
            bindex.set(current);
        } else {
            int ia, ib;
            for (int j = 0; j < n; j++) {
                ia = na - j - 1;
                if (ia >= 0) {
                    if (aindex.getShape(ia) == 1) {
                        aindex.setDim(ia, 0);
                    } else {
                        aindex.setDim(ia, current[n - j - 1]);
                    }
                }
                ib = nb - j - 1;
                if (ib >= 0) {
                    if (bindex.getShape(ib) == 1) {
                        bindex.setDim(ib, 0);
                    } else {
                        bindex.setDim(ib, current[n - j - 1]);
                    }
                }
            }
        }
    }

    private static void setIndex(Index aindex, Index bindex, int[] current, int n, int na, int nb) {
        int ia, ib;
        for (int j = 0; j < n; j++) {
            ia = na - j - 1;
            if (ia >= 0) {
                if (aindex.getShape(ia) == 1) {
                    aindex.setDim(ia, 0);
                } else {
                    aindex.setDim(ia, current[n - j - 1]);
                }
            }
            ib = nb - j - 1;
            if (ib >= 0) {
                if (bindex.getShape(ib) == 1) {
                    bindex.setDim(ib, 0);
                } else {
                    bindex.setDim(ib, current[n - j - 1]);
                }
            }
        }
    }

    /**
     * Array add
     *
     * @param a Array a
     * @param b Array b
     * @return Added array
     */
    public static Array add(Array a, Array b) {
        DataType type = ArrayMath.commonType(a.getDataType(), b.getDataType());
        switch (type) {
            case SHORT:
            case INT:
            case BOOLEAN:
                return ArrayMath.addInt(a, b);
            case FLOAT:
                return ArrayMath.addFloat(a, b);
            case DOUBLE:
                return ArrayMath.addDouble(a, b);
            case OBJECT:
                if (isComplex(a) || isComplex(b)) {
                    return ArrayMath.addComplex(a, b);
                }
                break;
        }
        return null;
    }

    /**
     * Array add
     *
     * @param a Array a
     * @param b Number b
     * @return Added array
     */
    public static Array add(Array a, Number b) {
        DataType bType = ArrayMath.getDataType(b);
        DataType type = ArrayMath.commonType(a.getDataType(), bType);
        switch (type) {
            case SHORT:
            case INT:
            case BOOLEAN:
                return ArrayMath.addInt(a, b.intValue());
            case FLOAT:
                return ArrayMath.addFloat(a, b.floatValue());
            case DOUBLE:
                return ArrayMath.addDouble(a, b.doubleValue());
            case OBJECT:
                if (isComplex(a)) {
                    return ArrayMath.addComplex(a, b.doubleValue());
                }
                break;
        }
        return null;
    }

    /**
     * Array add
     *
     * @param a Array a
     * @param b Complex number b
     * @return Added array
     */
    public static Array add(Array a, Complex b) {
        return addComplex(a, b);
    }

    /**
     * Array add
     *
     * @param a Array a
     * @param b Complex number b
     * @return Added array
     */
    public static Array add(Array a, PyComplex b) {
        return addComplex(a, new Complex(b.real, b.imag));
    }

    private static Array addInt_bak(Array a, Array b) {
        int broadcast = broadcastCheck(a, b);
        if (broadcast != -1) {
            int[] shape;
            if (broadcast == 0) {
                shape = a.getShape();
            } else {
                shape = broadcast(a, b);
            }
            Array r = Array.factory(DataType.INT, shape);
            Index index = r.getIndex();
            Index aindex = a.getIndex();
            Index bindex = b.getIndex();
            int n = r.getRank();
            int na = a.getRank();
            int nb = b.getRank();
            int[] current;
            for (int i = 0; i < r.getSize(); i++) {
                current = index.getCurrentCounter();
                setIndex(broadcast, aindex, bindex, current, n, na, nb);
                if (a.getInt(aindex) == Integer.MIN_VALUE || b.getInt(bindex) == Integer.MIN_VALUE) {
                    r.setInt(i, Integer.MIN_VALUE);
                } else {
                    r.setInt(i, a.getInt(aindex) + b.getInt(bindex));
                }
                index.incr();
            }
            return r;
        } else {
            return null;
        }
    }

    private static Array addInt(Array a, Array b) {
        int broadcast = broadcastCheck(a, b);
        switch (broadcast) {
            case 0:
                Array r = Array.factory(DataType.INT, a.getShape());
                for (int i = 0; i < a.getSize(); i++) {
                    if (a.getInt(i) == Integer.MIN_VALUE || b.getInt(i) == Integer.MIN_VALUE) {
                        r.setInt(i, Integer.MIN_VALUE);
                    } else {
                        r.setInt(i, a.getInt(i) + b.getInt(i));
                    }
                }
                return r;
            case 1:
                int[] shape = broadcast(a, b);
                r = Array.factory(DataType.INT, shape);
                Index index = r.getIndex();
                Index aindex = a.getIndex();
                Index bindex = b.getIndex();
                int n = r.getRank();
                int na = a.getRank();
                int nb = b.getRank();
                int[] current;
                for (int i = 0; i < r.getSize(); i++) {
                    current = index.getCurrentCounter();
                    setIndex(aindex, bindex, current, n, na, nb);
                    if (a.getInt(aindex) == Integer.MIN_VALUE || b.getInt(bindex) == Integer.MIN_VALUE) {
                        r.setInt(i, Integer.MIN_VALUE);
                    } else {
                        r.setInt(i, a.getInt(aindex) + b.getInt(bindex));
                    }
                    index.incr();
                }
                return r;
            default:
                return null;
        }
    }

    private static Array addInt(Array a, int b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getInt(i) == Integer.MIN_VALUE) {
                r.setInt(i, Integer.MIN_VALUE);
            } else {
                r.setInt(i, a.getInt(i) + b);
            }
        }

        return r;
    }

    private static Array addFloat(Array a, Array b) {
        int broadcast = broadcastCheck(a, b);
        switch (broadcast) {
            case 0:
                Array r = Array.factory(DataType.FLOAT, a.getShape());
                for (int i = 0; i < a.getSize(); i++) {
                    if (Float.isNaN(a.getFloat(i)) || Float.isNaN(b.getFloat(i))) {
                        r.setFloat(i, Float.NaN);
                    } else {
                        r.setFloat(i, a.getFloat(i) + b.getFloat(i));
                    }
                }
                return r;
            case 1:
                int[] shape = broadcast(a, b);
                r = Array.factory(DataType.FLOAT, shape);
                Index index = r.getIndex();
                Index aindex = a.getIndex();
                Index bindex = b.getIndex();
                int n = r.getRank();
                int na = a.getRank();
                int nb = b.getRank();
                int[] current;
                for (int i = 0; i < r.getSize(); i++) {
                    current = index.getCurrentCounter();
                    setIndex(aindex, bindex, current, n, na, nb);
                    if (Float.isNaN(a.getFloat(aindex)) || Float.isNaN(b.getFloat(bindex))) {
                        r.setFloat(i, Float.NaN);
                    } else {
                        r.setFloat(i, a.getFloat(aindex) + b.getFloat(bindex));
                    }
                    index.incr();
                }
                return r;
            default:
                return null;
        }
    }

    private static Array addFloat(Array a, float b) {
        Array r = Array.factory(DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (Float.isNaN(a.getFloat(i))) {
                r.setFloat(i, Float.NaN);
            } else {
                r.setFloat(i, a.getFloat(i) + b);
            }
        }

        return r;
    }

    private static Array addDouble(Array a, Array b) {
        int broadcast = broadcastCheck(a, b);
        switch (broadcast) {
            case 0:
                Array r = Array.factory(DataType.DOUBLE, a.getShape());
                for (int i = 0; i < a.getSize(); i++) {
                    if (Double.isNaN(a.getDouble(i)) || Double.isNaN(b.getDouble(i))) {
                        r.setDouble(i, Double.NaN);
                    } else {
                        r.setDouble(i, a.getDouble(i) + b.getDouble(i));
                    }
                }
                return r;
            case 1:
                int[] shape = broadcast(a, b);
                r = Array.factory(DataType.DOUBLE, shape);
                Index index = r.getIndex();
                Index aindex = a.getIndex();
                Index bindex = b.getIndex();
                int n = r.getRank();
                int na = a.getRank();
                int nb = b.getRank();
                int[] current;
                for (int i = 0; i < r.getSize(); i++) {
                    current = index.getCurrentCounter();
                    setIndex(aindex, bindex, current, n, na, nb);
                    if (Double.isNaN(a.getDouble(aindex)) || Double.isNaN(b.getDouble(bindex))) {
                        r.setDouble(i, Double.NaN);
                    } else {
                        r.setDouble(i, a.getDouble(aindex) + b.getDouble(bindex));
                    }
                    index.incr();
                }
                return r;
            default:
                return null;
        }
    }

    private static Array addDouble(Array a, double b) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (Double.isNaN(a.getDouble(i))) {
                r.setDouble(i, Double.NaN);
            } else {
                r.setDouble(i, a.getDouble(i) + b);
            }
        }

        return r;
    }

    private static Array addComplex(Array a, Array b) {
        int broadcast = broadcastCheck(a, b);
        switch (broadcast) {
            case 0:
                Array r = Array.factory(DataType.OBJECT, a.getShape());
                if (isComplex(a)) {
                    if (isComplex(b)) {
                        Complex v1, v2;
                        for (int i = 0; i < a.getSize(); i++) {
                            v1 = (Complex) a.getObject(i);
                            v2 = (Complex) b.getObject(i);
                            if (v1.isNaN() || v2.isNaN()) {
                                r.setObject(i, v1);
                            } else {
                                r.setObject(i, v1.add(v2));
                            }
                        }
                    } else {
                        Complex v;
                        for (int i = 0; i < a.getSize(); i++) {
                            v = (Complex) a.getObject(i);
                            if (v.isNaN() || Double.isNaN(b.getDouble(i))) {
                                r.setObject(i, new Complex(Double.NaN));
                            } else {
                                r.setObject(i, v.add(b.getDouble(i)));
                            }
                        }
                    }
                } else {
                    Complex v;
                    for (int i = 0; i < a.getSize(); i++) {
                        v = (Complex) b.getObject(i);
                        if (v.isNaN() || Double.isNaN(a.getDouble(i))) {
                            r.setObject(i, new Complex(Double.NaN));
                        } else {
                            r.setObject(i, v.add(a.getDouble(i)));
                        }
                    }
                }
                return r;
            case 1:
                int[] shape = broadcast(a, b);
                r = Array.factory(DataType.OBJECT, shape);
                Index index = r.getIndex();
                Index aindex = a.getIndex();
                Index bindex = b.getIndex();
                int n = r.getRank();
                int na = a.getRank();
                int nb = b.getRank();
                int[] current;
                if (isComplex(a)) {
                    if (isComplex(b)) {
                        Complex v1, v2;
                        for (int i = 0; i < r.getSize(); i++) {
                            current = index.getCurrentCounter();
                            setIndex(aindex, bindex, current, n, na, nb);
                            v1 = (Complex) a.getObject(aindex);
                            v2 = (Complex) b.getObject(bindex);
                            if (v1.isNaN() || v2.isNaN()) {
                                r.setObject(i, v1);
                            } else {
                                r.setObject(i, v1.add(v2));
                            }
                            index.incr();
                        }
                    } else {
                        Complex v;
                        for (int i = 0; i < r.getSize(); i++) {
                            current = index.getCurrentCounter();
                            setIndex(aindex, bindex, current, n, na, nb);
                            v = (Complex) a.getObject(aindex);
                            if (v.isNaN() || Double.isNaN(b.getDouble(bindex))) {
                                r.setObject(i, new Complex(Double.NaN));
                            } else {
                                r.setObject(i, v.add(b.getDouble(bindex)));
                            }
                            index.incr();
                        }
                    }
                } else {
                    Complex v;
                    for (int i = 0; i < a.getSize(); i++) {
                        current = index.getCurrentCounter();
                        setIndex(aindex, bindex, current, n, na, nb);
                        v = (Complex) b.getObject(bindex);
                        if (v.isNaN() || Double.isNaN(a.getDouble(aindex))) {
                            r.setObject(i, new Complex(Double.NaN));
                        } else {
                            r.setObject(i, v.add(a.getDouble(aindex)));
                        }
                    }
                }
                return r;
            default:
                return null;
        }
    }

    private static Array addComplex(Array a, double b) {
        Array r = Array.factory(DataType.OBJECT, a.getShape());
        Complex v;
        for (int i = 0; i < a.getSize(); i++) {
            v = (Complex) a.getObject(i);
            if (v.isNaN()) {
                r.setObject(i, v);
            } else {
                r.setObject(i, v.add(b));
            }
        }

        return r;
    }

    private static Array addComplex(Array a, Complex b) {
        Array r = Array.factory(DataType.OBJECT, a.getShape());
        Complex v;
        for (int i = 0; i < a.getSize(); i++) {
            v = (Complex) a.getObject(i);
            if (v.isNaN()) {
                r.setObject(i, v);
            } else {
                r.setObject(i, v.add(b));
            }
        }

        return r;
    }

    /**
     * Array subtract
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array sub(Array a, Array b) {
        DataType type = ArrayMath.commonType(a.getDataType(), b.getDataType());
        switch (type) {
            case SHORT:
            case INT:
            case BOOLEAN:
                return ArrayMath.subInt(a, b);
            case FLOAT:
                return ArrayMath.subFloat(a, b);
            case DOUBLE:
                return ArrayMath.subDouble(a, b);
            case OBJECT:
                if (isComplex(a) || isComplex(b)) {
                    return ArrayMath.subComplex(a, b);
                }
                break;
        }
        return null;
    }

    /**
     * Array subtract
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array sub(Array a, Number b) {
        DataType bType = ArrayMath.getDataType(b);
        DataType type = ArrayMath.commonType(a.getDataType(), bType);
        switch (type) {
            case SHORT:
            case INT:
            case BOOLEAN:
                return ArrayMath.subInt(a, b.intValue());
            case FLOAT:
                return ArrayMath.subFloat(a, b.floatValue());
            case DOUBLE:
                return ArrayMath.subDouble(a, b.doubleValue());
            case OBJECT:
                if (isComplex(a)) {
                    return ArrayMath.subComplex(a, b.doubleValue());
                }
                break;
        }
        return null;
    }

    /**
     * Array subtract
     *
     * @param a Array a
     * @param b Complex number b
     * @return Result array
     */
    public static Array sub(Array a, Complex b) {
        return subComplex(a, b);
    }

    /**
     * Array subtract
     *
     * @param a Array a
     * @param b Complex number b
     * @return Result array
     */
    public static Array sub(Array a, PyComplex b) {
        return subComplex(a, new Complex(b.real, b.imag));
    }

    /**
     * Array subtract
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array sub(Number b, Array a) {
        DataType bType = ArrayMath.getDataType(b);
        DataType type = ArrayMath.commonType(a.getDataType(), bType);
        switch (type) {
            case SHORT:
            case INT:
            case BOOLEAN:
                return ArrayMath.subInt(b.intValue(), a);
            case FLOAT:
                return ArrayMath.subFloat(b.floatValue(), a);
            case DOUBLE:
                return ArrayMath.subDouble(b.doubleValue(), a);
            case OBJECT:
                if (isComplex(a)) {
                    return ArrayMath.subComplex(b.doubleValue(), a);
                }
                break;
        }
        return null;
    }

    /**
     * Array subtract
     *
     * @param a Array a
     * @param b Complex number b
     * @return Result array
     */
    public static Array sub(Complex b, Array a) {
        return subComplex(b, a);
    }

    /**
     * Array subtract
     *
     * @param a Array a
     * @param b Complex number b
     * @return Result array
     */
    public static Array sub(PyComplex b, Array a) {
        return subComplex(new Complex(b.real, b.imag), a);
    }

    private static Array subInt(Array a, Array b) {
        int broadcast = broadcastCheck(a, b);
        switch (broadcast) {
            case 0:
                Array r = Array.factory(DataType.INT, a.getShape());
                for (int i = 0; i < a.getSize(); i++) {
                    r.setInt(i, a.getInt(i) - b.getInt(i));
                }
                return r;
            case 1:
                int[] shape = broadcast(a, b);
                r = Array.factory(DataType.INT, shape);
                Index index = r.getIndex();
                Index aindex = a.getIndex();
                Index bindex = b.getIndex();
                int n = r.getRank();
                int na = a.getRank();
                int nb = b.getRank();
                int[] current;
                for (int i = 0; i < r.getSize(); i++) {
                    current = index.getCurrentCounter();
                    setIndex(aindex, bindex, current, n, na, nb);
                    r.setInt(i, a.getInt(aindex) - b.getInt(bindex));
                    index.incr();
                }
                return r;
            default:
                return null;
        }
    }

    private static Array subInt(Array a, int b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setInt(i, a.getInt(i) - b);
        }

        return r;
    }

    private static Array subInt(int b, Array a) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setInt(i, b - a.getInt(i));
        }

        return r;
    }

    private static Array subFloat(Array a, Array b) {
        int broadcast = broadcastCheck(a, b);
        switch (broadcast) {
            case 0:
                Array r = Array.factory(DataType.FLOAT, a.getShape());
                for (int i = 0; i < a.getSize(); i++) {
                    if (Float.isNaN(a.getFloat(i)) || Float.isNaN(b.getFloat(i))) {
                        r.setFloat(i, Float.NaN);
                    } else {
                        r.setFloat(i, a.getFloat(i) - b.getFloat(i));
                    }
                }
                return r;
            case 1:
                int[] shape = broadcast(a, b);
                r = Array.factory(DataType.FLOAT, shape);
                Index index = r.getIndex();
                Index aindex = a.getIndex();
                Index bindex = b.getIndex();
                int n = r.getRank();
                int na = a.getRank();
                int nb = b.getRank();
                int[] current;
                for (int i = 0; i < r.getSize(); i++) {
                    current = index.getCurrentCounter();
                    setIndex(aindex, bindex, current, n, na, nb);
                    if (Float.isNaN(a.getFloat(aindex)) || Float.isNaN(b.getFloat(bindex))) {
                        r.setFloat(i, Float.NaN);
                    } else {
                        r.setFloat(i, a.getFloat(aindex) - b.getFloat(bindex));
                    }
                    index.incr();
                }
                return r;
            default:
                return null;
        }
    }

    private static Array subFloat(Array a, float b) {
        Array r = Array.factory(DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setFloat(i, a.getFloat(i) - b);
        }

        return r;
    }

    private static Array subFloat(float b, Array a) {
        Array r = Array.factory(DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setFloat(i, b - a.getFloat(i));
        }

        return r;
    }

    private static Array subDouble(Array a, Array b) {
        int broadcast = broadcastCheck(a, b);
        switch (broadcast) {
            case 0:
                Array r = Array.factory(DataType.DOUBLE, a.getShape());
                for (int i = 0; i < a.getSize(); i++) {
                    if (Double.isNaN(a.getDouble(i)) || Double.isNaN(b.getDouble(i))) {
                        r.setDouble(i, Double.NaN);
                    } else {
                        r.setDouble(i, a.getDouble(i) - b.getDouble(i));
                    }
                }
                return r;
            case 1:
                int[] shape = broadcast(a, b);
                r = Array.factory(DataType.DOUBLE, shape);
                Index index = r.getIndex();
                Index aindex = a.getIndex();
                Index bindex = b.getIndex();
                int n = r.getRank();
                int na = a.getRank();
                int nb = b.getRank();
                int[] current;
                for (int i = 0; i < r.getSize(); i++) {
                    current = index.getCurrentCounter();
                    setIndex(aindex, bindex, current, n, na, nb);
                    if (Double.isNaN(a.getDouble(aindex)) || Double.isNaN(b.getDouble(bindex))) {
                        r.setDouble(i, Double.NaN);
                    } else {
                        r.setDouble(i, a.getDouble(aindex) - b.getDouble(bindex));
                    }
                    index.incr();
                }
                return r;
            default:
                return null;
        }
    }

    private static Array subDouble(Array a, double b) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, a.getDouble(i) - b);
        }

        return r;
    }

    private static Array subDouble(double b, Array a) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, b - a.getDouble(i));
        }

        return r;
    }

    private static Array subComplex(Array a, Array b) {
        int broadcast = broadcastCheck(a, b);
        switch (broadcast) {
            case 0:
                Array r = Array.factory(DataType.OBJECT, a.getShape());
                if (isComplex(a)) {
                    if (isComplex(b)) {
                        Complex v1, v2;
                        for (int i = 0; i < a.getSize(); i++) {
                            v1 = (Complex) a.getObject(i);
                            v2 = (Complex) b.getObject(i);
                            if (v1.isNaN() || v2.isNaN()) {
                                r.setObject(i, v1);
                            } else {
                                r.setObject(i, v1.subtract(v2));
                            }
                        }
                    } else {
                        Complex v;
                        for (int i = 0; i < a.getSize(); i++) {
                            v = (Complex) a.getObject(i);
                            if (v.isNaN() || Double.isNaN(b.getDouble(i))) {
                                r.setObject(i, new Complex(Double.NaN));
                            } else {
                                r.setObject(i, v.subtract(b.getDouble(i)));
                            }
                        }
                    }
                } else {
                    Complex v;
                    for (int i = 0; i < a.getSize(); i++) {
                        v = (Complex) b.getObject(i);
                        if (v.isNaN() || Double.isNaN(a.getDouble(i))) {
                            r.setObject(i, new Complex(Double.NaN));
                        } else {
                            r.setObject(i, v.subtract(a.getDouble(i)));
                        }
                    }
                }
                return r;
            case 1:
                int[] shape = broadcast(a, b);
                r = Array.factory(DataType.OBJECT, shape);
                Index index = r.getIndex();
                Index aindex = a.getIndex();
                Index bindex = b.getIndex();
                int n = r.getRank();
                int na = a.getRank();
                int nb = b.getRank();
                int[] current;
                if (isComplex(a)) {
                    if (isComplex(b)) {
                        Complex v1, v2;
                        for (int i = 0; i < r.getSize(); i++) {
                            current = index.getCurrentCounter();
                            setIndex(aindex, bindex, current, n, na, nb);
                            v1 = (Complex) a.getObject(aindex);
                            v2 = (Complex) b.getObject(bindex);
                            if (v1.isNaN() || v2.isNaN()) {
                                r.setObject(i, v1);
                            } else {
                                r.setObject(i, v1.subtract(v2));
                            }
                            index.incr();
                        }
                    } else {
                        Complex v;
                        for (int i = 0; i < r.getSize(); i++) {
                            current = index.getCurrentCounter();
                            setIndex(aindex, bindex, current, n, na, nb);
                            v = (Complex) a.getObject(aindex);
                            if (v.isNaN() || Double.isNaN(b.getDouble(bindex))) {
                                r.setObject(i, new Complex(Double.NaN));
                            } else {
                                r.setObject(i, v.subtract(b.getDouble(bindex)));
                            }
                            index.incr();
                        }
                    }
                } else {
                    Complex v;
                    for (int i = 0; i < a.getSize(); i++) {
                        current = index.getCurrentCounter();
                        setIndex(aindex, bindex, current, n, na, nb);
                        v = (Complex) b.getObject(bindex);
                        if (v.isNaN() || Double.isNaN(a.getDouble(aindex))) {
                            r.setObject(i, new Complex(Double.NaN));
                        } else {
                            r.setObject(i, v.subtract(a.getDouble(aindex)));
                        }
                    }
                }
                return r;
            default:
                return null;
        }
    }

    private static Array subComplex(Array a, double b) {
        Array r = Array.factory(DataType.OBJECT, a.getShape());
        Complex v;
        for (int i = 0; i < a.getSize(); i++) {
            v = (Complex) a.getObject(i);
            if (v.isNaN()) {
                r.setObject(i, v);
            } else {
                r.setObject(i, v.subtract(b));
            }
        }

        return r;
    }

    private static Array subComplex(Array a, Complex b) {
        Array r = Array.factory(DataType.OBJECT, a.getShape());
        Complex v;
        for (int i = 0; i < a.getSize(); i++) {
            v = (Complex) a.getObject(i);
            if (v.isNaN()) {
                r.setObject(i, v);
            } else {
                r.setObject(i, v.subtract(b));
            }
        }

        return r;
    }

    private static Array subComplex(double b, Array a) {
        Array r = Array.factory(DataType.OBJECT, a.getShape());
        Complex v;
        for (int i = 0; i < a.getSize(); i++) {
            v = (Complex) a.getObject(i);
            if (v.isNaN()) {
                r.setObject(i, v);
            } else {
                r.setObject(i, v.rSubtract(b));
            }
        }

        return r;
    }

    private static Array subComplex(Complex b, Array a) {
        Array r = Array.factory(DataType.OBJECT, a.getShape());
        Complex v;
        for (int i = 0; i < a.getSize(); i++) {
            v = (Complex) a.getObject(i);
            if (v.isNaN()) {
                r.setObject(i, v);
            } else {
                r.setObject(i, b.subtract(v));
            }
        }

        return r;
    }

    /**
     * Array mutiply
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array mul(Array a, Array b) {
        DataType type = ArrayMath.commonType(a.getDataType(), b.getDataType());
        switch (type) {
            case SHORT:
            case INT:
            case BOOLEAN:
                return ArrayMath.mulInt(a, b);
            case FLOAT:
                return ArrayMath.mulFloat(a, b);
            case DOUBLE:
                return ArrayMath.mulDouble(a, b);
            case OBJECT:
                if (isComplex(a) || isComplex(b)) {
                    return ArrayMath.mulComplex(a, b);
                }
                break;
        }
        return null;
    }

    /**
     * Array multiply
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array mul(Array a, Number b) {
        DataType bType = ArrayMath.getDataType(b);
        DataType type = ArrayMath.commonType(a.getDataType(), bType);
        switch (type) {
            case SHORT:
            case INT:
            case BOOLEAN:
                return ArrayMath.mulInt(a, b.intValue());
            case FLOAT:
                return ArrayMath.mulFloat(a, b.floatValue());
            case DOUBLE:
                return ArrayMath.mulDouble(a, b.doubleValue());
            case OBJECT:
                if (isComplex(a)) {
                    return ArrayMath.mulComplex(a, b.doubleValue());
                }
                break;
        }
        return null;
    }

    /**
     * Array multiply
     *
     * @param a Array a
     * @param b Complex number b
     * @return Result array
     */
    public static Array mul(Array a, Complex b) {
        return ArrayMath.mulComplex(a, b);
    }

    /**
     * Array multiply
     *
     * @param a Array a
     * @param b Complex number b
     * @return Result array
     */
    public static Array mul(Array a, PyComplex b) {
        return ArrayMath.mulComplex(a, new Complex(b.real, b.imag));
    }

    private static Array mulInt(Array a, Array b) {
        int broadcast = broadcastCheck(a, b);
        switch (broadcast) {
            case 0:
                Array r = Array.factory(DataType.INT, a.getShape());
                for (int i = 0; i < a.getSize(); i++) {
                    if (a.getInt(i) == Integer.MIN_VALUE || b.getInt(i) == Integer.MIN_VALUE) {
                        r.setInt(i, Integer.MIN_VALUE);
                    } else {
                        r.setInt(i, a.getInt(i) * b.getInt(i));
                    }
                }
                return r;
            case 1:
                int[] shape = broadcast(a, b);
                r = Array.factory(DataType.INT, shape);
                Index index = r.getIndex();
                Index aindex = a.getIndex();
                Index bindex = b.getIndex();
                int n = r.getRank();
                int na = a.getRank();
                int nb = b.getRank();
                int[] current;
                for (int i = 0; i < r.getSize(); i++) {
                    current = index.getCurrentCounter();
                    setIndex(aindex, bindex, current, n, na, nb);
                    if (a.getInt(aindex) == Integer.MIN_VALUE || b.getInt(bindex) == Integer.MIN_VALUE) {
                        r.setInt(i, Integer.MIN_VALUE);
                    } else {
                        r.setInt(i, a.getInt(aindex) * b.getInt(bindex));
                    }
                    index.incr();
                }
                return r;
            default:
                return null;
        }
    }

    private static Array mulInt(Array a, int b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getInt(i) == Integer.MIN_VALUE) {
                r.setInt(i, Integer.MIN_VALUE);
            } else {
                r.setInt(i, a.getInt(i) * b);
            }
        }

        return r;
    }

    private static Array mulFloat(Array a, Array b) {
        int broadcast = broadcastCheck(a, b);
        switch (broadcast) {
            case 0:
                Array r = Array.factory(DataType.FLOAT, a.getShape());
                for (int i = 0; i < a.getSize(); i++) {
                    if (Float.isNaN(a.getFloat(i)) || Float.isNaN(b.getFloat(i))) {
                        r.setFloat(i, Float.NaN);
                    } else {
                        r.setFloat(i, a.getFloat(i) * b.getFloat(i));
                    }
                }
                return r;
            case 1:
                int[] shape = broadcast(a, b);
                r = Array.factory(DataType.FLOAT, shape);
                Index index = r.getIndex();
                Index aindex = a.getIndex();
                Index bindex = b.getIndex();
                int n = r.getRank();
                int na = a.getRank();
                int nb = b.getRank();
                int[] current;
                for (int i = 0; i < r.getSize(); i++) {
                    current = index.getCurrentCounter();
                    setIndex(aindex, bindex, current, n, na, nb);
                    if (Float.isNaN(a.getFloat(aindex)) || Float.isNaN(b.getFloat(bindex))) {
                        r.setFloat(i, Float.NaN);
                    } else {
                        r.setFloat(i, a.getFloat(aindex) * b.getFloat(bindex));
                    }
                    index.incr();
                }
                return r;
            default:
                return null;
        }
    }

    private static Array mulFloat(Array a, float b) {
        Array r = Array.factory(DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (Float.isNaN(a.getFloat(i))) {
                r.setFloat(i, Float.NaN);
            } else {
                r.setFloat(i, a.getFloat(i) * b);
            }
        }

        return r;
    }

    private static Array mulDouble(Array a, Array b) {
        int broadcast = broadcastCheck(a, b);
        switch (broadcast) {
            case 0:
                Array r = Array.factory(DataType.DOUBLE, a.getShape());
                for (int i = 0; i < a.getSize(); i++) {
                    if (Double.isNaN(a.getDouble(i)) || Double.isNaN(b.getDouble(i))) {
                        r.setDouble(i, Double.NaN);
                    } else {
                        r.setDouble(i, a.getDouble(i) * b.getDouble(i));
                    }
                }
                return r;
            case 1:
                int[] shape = broadcast(a, b);
                r = Array.factory(DataType.DOUBLE, shape);
                Index index = r.getIndex();
                Index aindex = a.getIndex();
                Index bindex = b.getIndex();
                int n = r.getRank();
                int na = a.getRank();
                int nb = b.getRank();
                int[] current;
                for (int i = 0; i < r.getSize(); i++) {
                    current = index.getCurrentCounter();
                    setIndex(aindex, bindex, current, n, na, nb);
                    if (Double.isNaN(a.getDouble(aindex)) || Double.isNaN(b.getDouble(bindex))) {
                        r.setDouble(i, Double.NaN);
                    } else {
                        r.setDouble(i, a.getDouble(aindex) * b.getDouble(bindex));
                    }
                    index.incr();
                }
                return r;
            default:
                return null;
        }
    }

    private static Array mulDouble(Array a, double b) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (Double.isNaN(a.getDouble(i))) {
                r.setDouble(i, Double.NaN);
            } else {
                r.setDouble(i, a.getDouble(i) * b);
            }
        }

        return r;
    }

    private static Array mulComplex(Array a, Array b) {
        int broadcast = broadcastCheck(a, b);
        switch (broadcast) {
            case 0:
                Array r = Array.factory(DataType.OBJECT, a.getShape());
                if (isComplex(a)) {
                    if (isComplex(b)) {
                        Complex v1, v2;
                        for (int i = 0; i < a.getSize(); i++) {
                            v1 = (Complex) a.getObject(i);
                            v2 = (Complex) b.getObject(i);
                            if (v1.isNaN() || v2.isNaN()) {
                                r.setObject(i, v1);
                            } else {
                                r.setObject(i, v1.multiply(v2));
                            }
                        }
                    } else {
                        Complex v;
                        for (int i = 0; i < a.getSize(); i++) {
                            v = (Complex) a.getObject(i);
                            if (v.isNaN() || Double.isNaN(b.getDouble(i))) {
                                r.setObject(i, new Complex(Double.NaN));
                            } else {
                                r.setObject(i, v.multiply(b.getDouble(i)));
                            }
                        }
                    }
                } else {
                    Complex v;
                    for (int i = 0; i < a.getSize(); i++) {
                        v = (Complex) b.getObject(i);
                        if (v.isNaN() || Double.isNaN(a.getDouble(i))) {
                            r.setObject(i, new Complex(Double.NaN));
                        } else {
                            r.setObject(i, v.multiply(a.getDouble(i)));
                        }
                    }
                }
                return r;
            case 1:
                int[] shape = broadcast(a, b);
                r = Array.factory(DataType.OBJECT, shape);
                Index index = r.getIndex();
                Index aindex = a.getIndex();
                Index bindex = b.getIndex();
                int n = r.getRank();
                int na = a.getRank();
                int nb = b.getRank();
                int[] current;
                if (isComplex(a)) {
                    if (isComplex(b)) {
                        Complex v1, v2;
                        for (int i = 0; i < r.getSize(); i++) {
                            current = index.getCurrentCounter();
                            setIndex(aindex, bindex, current, n, na, nb);
                            v1 = (Complex) a.getObject(aindex);
                            v2 = (Complex) b.getObject(bindex);
                            if (v1.isNaN() || v2.isNaN()) {
                                r.setObject(i, v1);
                            } else {
                                r.setObject(i, v1.multiply(v2));
                            }
                            index.incr();
                        }
                    } else {
                        Complex v;
                        for (int i = 0; i < r.getSize(); i++) {
                            current = index.getCurrentCounter();
                            setIndex(aindex, bindex, current, n, na, nb);
                            v = (Complex) a.getObject(aindex);
                            if (v.isNaN() || Double.isNaN(b.getDouble(bindex))) {
                                r.setObject(i, new Complex(Double.NaN));
                            } else {
                                r.setObject(i, v.multiply(b.getDouble(bindex)));
                            }
                            index.incr();
                        }
                    }
                } else {
                    Complex v;
                    for (int i = 0; i < a.getSize(); i++) {
                        current = index.getCurrentCounter();
                        setIndex(aindex, bindex, current, n, na, nb);
                        v = (Complex) b.getObject(bindex);
                        if (v.isNaN() || Double.isNaN(a.getDouble(aindex))) {
                            r.setObject(i, new Complex(Double.NaN));
                        } else {
                            r.setObject(i, v.multiply(a.getDouble(aindex)));
                        }
                    }
                }
                return r;
            default:
                return null;
        }
    }

    private static Array mulComplex(Array a, double b) {
        Array r = Array.factory(DataType.OBJECT, a.getShape());
        Complex v;
        for (int i = 0; i < a.getSize(); i++) {
            v = (Complex) a.getObject(i);
            if (v.isNaN()) {
                r.setObject(i, v);
            } else {
                r.setObject(i, v.multiply(b));
            }
        }

        return r;
    }

    private static Array mulComplex(Array a, Complex b) {
        Array r = Array.factory(DataType.OBJECT, a.getShape());
        Complex v;
        for (int i = 0; i < a.getSize(); i++) {
            v = (Complex) a.getObject(i);
            if (v.isNaN()) {
                r.setObject(i, v);
            } else {
                r.setObject(i, v.multiply(b));
            }
        }

        return r;
    }

    /**
     * Array divide
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array div(Array a, Array b) {
        DataType type = ArrayMath.commonType(a.getDataType(), b.getDataType());
        switch (type) {
            case SHORT:
            case INT:
            case BOOLEAN:
                return ArrayMath.divInt(a, b);
            case FLOAT:
                return ArrayMath.divFloat(a, b);
            case DOUBLE:
                return ArrayMath.divDouble(a, b);
            case OBJECT:
                if (isComplex(a) || isComplex(b)) {
                    return ArrayMath.divComplex(a, b);
                }
                break;
        }
        return null;
    }

    /**
     * Array divide
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array div(Array a, Number b) {
        DataType bType = ArrayMath.getDataType(b);
        DataType type = ArrayMath.commonType(a.getDataType(), bType);
        switch (type) {
            case SHORT:
            case INT:
            case BOOLEAN:
                return ArrayMath.divInt(a, b.intValue());
            case FLOAT:
                return ArrayMath.divFloat(a, b.floatValue());
            case DOUBLE:
                return ArrayMath.divDouble(a, b.doubleValue());
            case OBJECT:
                if (isComplex(a)) {
                    return ArrayMath.divComplex(a, b.doubleValue());
                }
                break;
        }
        return null;
    }

    /**
     * Array divide
     *
     * @param a Array a
     * @param b Complex number b
     * @return Result array
     */
    public static Array div(Array a, Complex b) {
        return divComplex(a, b);
    }

    /**
     * Array divide
     *
     * @param a Array a
     * @param b Complex number b
     * @return Result array
     */
    public static Array div(Array a, PyComplex b) {
        return divComplex(a, new Complex(b.real, b.imag));
    }

    /**
     * Array divide
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array div(Number b, Array a) {
        DataType bType = ArrayMath.getDataType(b);
        DataType type = ArrayMath.commonType(a.getDataType(), bType);
        switch (type) {
            case SHORT:
            case INT:
            case BOOLEAN:
                return ArrayMath.divInt(b.intValue(), a);
            case FLOAT:
                return ArrayMath.divFloat(b.floatValue(), a);
            case DOUBLE:
                return ArrayMath.divDouble(b.doubleValue(), a);
            case OBJECT:
                if (isComplex(a)) {
                    return ArrayMath.divComplex(b.doubleValue(), a);
                }
                break;
        }
        return null;
    }

    /**
     * Array divide
     *
     * @param a Array a
     * @param b Complex number b
     * @return Result array
     */
    public static Array div(Complex b, Array a) {
        return divComplex(b, a);
    }

    /**
     * Array divide
     *
     * @param a Array a
     * @param b Complex number b
     * @return Result array
     */
    public static Array div(PyComplex b, Array a) {
        return divComplex(new Complex(b.real, b.imag), a);
    }

    private static Array divInt(Array a, Array b) {
        int broadcast = broadcastCheck(a, b);
        switch (broadcast) {
            case 0:
                Array r = Array.factory(DataType.INT, a.getShape());
                for (int i = 0; i < a.getSize(); i++) {
                    if (a.getInt(i) == Integer.MIN_VALUE || b.getInt(i) == Integer.MIN_VALUE) {
                        r.setInt(i, Integer.MIN_VALUE);
                    } else {
                        r.setInt(i, a.getInt(i) / b.getInt(i));
                    }
                }
                return r;
            case 1:
                int[] shape = broadcast(a, b);
                r = Array.factory(DataType.INT, shape);
                Index index = r.getIndex();
                Index aindex = a.getIndex();
                Index bindex = b.getIndex();
                int n = r.getRank();
                int na = a.getRank();
                int nb = b.getRank();
                int[] current;
                for (int i = 0; i < r.getSize(); i++) {
                    current = index.getCurrentCounter();
                    setIndex(aindex, bindex, current, n, na, nb);
                    if (a.getInt(aindex) == Integer.MIN_VALUE || b.getInt(bindex) == Integer.MIN_VALUE) {
                        r.setInt(i, Integer.MIN_VALUE);
                    } else {
                        r.setInt(i, a.getInt(aindex) / b.getInt(bindex));
                    }
                    index.incr();
                }
                return r;
            default:
                return null;
        }
    }

    private static Array divInt(Array a, int b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setInt(i, a.getInt(i) / b);
        }

        return r;
    }

    private static Array divInt(int b, Array a) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setInt(i, b / a.getInt(i));
        }

        return r;
    }

    private static Array divFloat(Array a, Array b) {
        int broadcast = broadcastCheck(a, b);
        switch (broadcast) {
            case 0:
                Array r = Array.factory(DataType.FLOAT, a.getShape());
                for (int i = 0; i < a.getSize(); i++) {
                    if (Float.isNaN(a.getFloat(i)) || Float.isNaN(b.getFloat(i))) {
                        r.setFloat(i, Float.NaN);
                    } else {
                        r.setFloat(i, a.getFloat(i) / b.getFloat(i));
                    }
                }
                return r;
            case 1:
                int[] shape = broadcast(a, b);
                r = Array.factory(DataType.FLOAT, shape);
                Index index = r.getIndex();
                Index aindex = a.getIndex();
                Index bindex = b.getIndex();
                int n = r.getRank();
                int na = a.getRank();
                int nb = b.getRank();
                int[] current;
                for (int i = 0; i < r.getSize(); i++) {
                    current = index.getCurrentCounter();
                    setIndex(aindex, bindex, current, n, na, nb);
                    if (Float.isNaN(a.getFloat(aindex)) || Float.isNaN(b.getFloat(bindex))) {
                        r.setFloat(i, Float.NaN);
                    } else {
                        r.setFloat(i, a.getFloat(aindex) / b.getFloat(bindex));
                    }
                    index.incr();
                }
                return r;
            default:
                return null;
        }
    }

    private static Array divFloat(Array a, float b) {
        Array r = Array.factory(DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setFloat(i, a.getFloat(i) / b);
        }

        return r;
    }

    private static Array divFloat(float b, Array a) {
        Array r = Array.factory(DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setFloat(i, b / a.getFloat(i));
        }

        return r;
    }

    private static Array divDouble(Array a, Array b) {
        int broadcast = broadcastCheck(a, b);
        switch (broadcast) {
            case 0:
                Array r = Array.factory(DataType.DOUBLE, a.getShape());
                for (int i = 0; i < a.getSize(); i++) {
                    if (Double.isNaN(a.getDouble(i)) || Double.isNaN(b.getDouble(i))) {
                        r.setDouble(i, Double.NaN);
                    } else {
                        r.setDouble(i, a.getDouble(i) / b.getDouble(i));
                    }
                }
                return r;
            case 1:
                int[] shape = broadcast(a, b);
                r = Array.factory(DataType.DOUBLE, shape);
                Index index = r.getIndex();
                Index aindex = a.getIndex();
                Index bindex = b.getIndex();
                int n = r.getRank();
                int na = a.getRank();
                int nb = b.getRank();
                int[] current;
                for (int i = 0; i < r.getSize(); i++) {
                    current = index.getCurrentCounter();
                    setIndex(aindex, bindex, current, n, na, nb);
                    if (Double.isNaN(a.getDouble(aindex)) || Double.isNaN(b.getDouble(bindex))) {
                        r.setDouble(i, Double.NaN);
                    } else {
                        r.setDouble(i, a.getDouble(aindex) / b.getDouble(bindex));
                    }
                    index.incr();
                }
                return r;
            default:
                return null;
        }
    }

    private static Array divDouble(Array a, double b) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, a.getDouble(i) / b);
        }

        return r;
    }

    private static Array divDouble(double b, Array a) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, b / a.getDouble(i));
        }

        return r;
    }

    private static Array divComplex(Array a, Array b) {
        int broadcast = broadcastCheck(a, b);
        switch (broadcast) {
            case 0:
                Array r = Array.factory(DataType.OBJECT, a.getShape());
                if (isComplex(a)) {
                    if (isComplex(b)) {
                        Complex v1, v2;
                        for (int i = 0; i < a.getSize(); i++) {
                            v1 = (Complex) a.getObject(i);
                            v2 = (Complex) b.getObject(i);
                            if (v1.isNaN() || v2.isNaN()) {
                                r.setObject(i, v1);
                            } else {
                                r.setObject(i, v1.divide(v2));
                            }
                        }
                    } else {
                        Complex v;
                        for (int i = 0; i < a.getSize(); i++) {
                            v = (Complex) a.getObject(i);
                            if (v.isNaN() || Double.isNaN(b.getDouble(i))) {
                                r.setObject(i, new Complex(Double.NaN));
                            } else {
                                r.setObject(i, v.divide(b.getDouble(i)));
                            }
                        }
                    }
                } else {
                    Complex v;
                    for (int i = 0; i < a.getSize(); i++) {
                        v = (Complex) b.getObject(i);
                        if (v.isNaN() || Double.isNaN(a.getDouble(i))) {
                            r.setObject(i, new Complex(Double.NaN));
                        } else {
                            r.setObject(i, v.divide(a.getDouble(i)));
                        }
                    }
                }
                return r;
            case 1:
                int[] shape = broadcast(a, b);
                r = Array.factory(DataType.OBJECT, shape);
                Index index = r.getIndex();
                Index aindex = a.getIndex();
                Index bindex = b.getIndex();
                int n = r.getRank();
                int na = a.getRank();
                int nb = b.getRank();
                int[] current;
                if (isComplex(a)) {
                    if (isComplex(b)) {
                        Complex v1, v2;
                        for (int i = 0; i < r.getSize(); i++) {
                            current = index.getCurrentCounter();
                            setIndex(aindex, bindex, current, n, na, nb);
                            v1 = (Complex) a.getObject(aindex);
                            v2 = (Complex) b.getObject(bindex);
                            if (v1.isNaN() || v2.isNaN()) {
                                r.setObject(i, v1);
                            } else {
                                r.setObject(i, v1.divide(v2));
                            }
                            index.incr();
                        }
                    } else {
                        Complex v;
                        for (int i = 0; i < r.getSize(); i++) {
                            current = index.getCurrentCounter();
                            setIndex(aindex, bindex, current, n, na, nb);
                            v = (Complex) a.getObject(aindex);
                            if (v.isNaN() || Double.isNaN(b.getDouble(bindex))) {
                                r.setObject(i, new Complex(Double.NaN));
                            } else {
                                r.setObject(i, v.divide(b.getDouble(bindex)));
                            }
                            index.incr();
                        }
                    }
                } else {
                    Complex v;
                    for (int i = 0; i < a.getSize(); i++) {
                        current = index.getCurrentCounter();
                        setIndex(aindex, bindex, current, n, na, nb);
                        v = (Complex) b.getObject(bindex);
                        if (v.isNaN() || Double.isNaN(a.getDouble(aindex))) {
                            r.setObject(i, new Complex(Double.NaN));
                        } else {
                            r.setObject(i, v.divide(a.getDouble(aindex)));
                        }
                    }
                }
                return r;
            default:
                return null;
        }
    }

    private static Array divComplex(Array a, double b) {
        Array r = Array.factory(DataType.OBJECT, a.getShape());
        Complex v;
        for (int i = 0; i < a.getSize(); i++) {
            v = (Complex) a.getObject(i);
            if (v.isNaN()) {
                r.setObject(i, v);
            } else {
                r.setObject(i, v.divide(b));
            }
        }

        return r;
    }

    private static Array divComplex(Array a, Complex b) {
        Array r = Array.factory(DataType.OBJECT, a.getShape());
        Complex v;
        for (int i = 0; i < a.getSize(); i++) {
            v = (Complex) a.getObject(i);
            if (v.isNaN()) {
                r.setObject(i, v);
            } else {
                r.setObject(i, v.divide(b));
            }
        }

        return r;
    }

    private static Array divComplex(double b, Array a) {
        Array r = Array.factory(DataType.OBJECT, a.getShape());
        Complex v;
        for (int i = 0; i < a.getSize(); i++) {
            v = (Complex) a.getObject(i);
            if (v.isNaN()) {
                r.setObject(i, v);
            } else {
                r.setObject(i, v.rDivide(b));
            }
        }

        return r;
    }

    private static Array divComplex(Complex b, Array a) {
        Array r = Array.factory(DataType.OBJECT, a.getShape());
        Complex v;
        for (int i = 0; i < a.getSize(); i++) {
            v = (Complex) a.getObject(i);
            if (v.isNaN()) {
                r.setObject(i, v);
            } else {
                r.setObject(i, b.divide(v));
            }
        }

        return r;
    }

    /**
     * Array pow function
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array pow(Array a, Number b) {
        DataType bType = ArrayMath.getDataType(b);
        DataType type = ArrayMath.commonType(a.getDataType(), bType);
        switch (type) {
            case SHORT:
            case INT:
            case BOOLEAN:
                return ArrayMath.powInt(a, b.intValue());
            case FLOAT:
            case DOUBLE:
                return ArrayMath.powDouble(a, b.doubleValue());
            case OBJECT:
                if (isComplex(a)) {
                    return ArrayMath.powComplex(a, b.doubleValue());
                }
                break;
        }
        return null;
    }

    /**
     * Array pow function
     *
     * @param a Array a
     * @param b Complex number b
     * @return Result array
     */
    public static Array pow(Array a, Complex b) {
        return powComplex(a, b);
    }

    /**
     * Array pow function
     *
     * @param a Array a
     * @param b Complex number b
     * @return Result array
     */
    public static Array pow(Array a, PyComplex b) {
        return powComplex(a, new Complex(b.real, b.imag));
    }

    /**
     * Array pow function
     *
     * @param a Number a
     * @param b Array b
     * @return Result array
     */
    public static Array pow(Number a, Array b) {
        DataType bType = ArrayMath.getDataType(a);
        DataType type = ArrayMath.commonType(b.getDataType(), bType);
        switch (type) {
            case SHORT:
            case INT:
            case BOOLEAN:
                return ArrayMath.powInt(a.intValue(), b);
            case FLOAT:
            case DOUBLE:
                return ArrayMath.powDouble(a.doubleValue(), b);
            case OBJECT:
                if (isComplex(b)) {
                    return ArrayMath.powComplex(a.doubleValue(), b);
                }
                break;
        }
        return null;
    }

    /**
     * Array pow function
     *
     * @param a Array a
     * @param b Complex number b
     * @return Result array
     */
    public static Array pow(Complex b, Array a) {
        return powComplex(b, a);
    }

    /**
     * Array pow function
     *
     * @param a Array a
     * @param b Complex number b
     * @return Result array
     */
    public static Array pow(PyComplex b, Array a) {
        return powComplex(new Complex(b.real, b.imag), a);
    }

    /**
     * Array pow function
     *
     * @param a Number a
     * @param b Array b
     * @return Result array
     */
    public static Array pow(Array a, Array b) {
        DataType type = ArrayMath.commonType(a.getDataType(), b.getDataType());
        switch (type) {
            case SHORT:
            case INT:
                return ArrayMath.powInt(a, b);
            case FLOAT:
            case DOUBLE:
                return ArrayMath.powDouble(a, b);
            case OBJECT:
                if (isComplex(a) || isComplex(b)) {
                    return ArrayMath.powComplex(a, b);
                }
                break;
        }
        return null;
    }

    private static Array powInt(Array a, int b) {
        Array r = Array.factory(DataType.INT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setInt(i, (int) Math.pow(a.getInt(i), b));
        }

        return r;
    }

    private static Array powInt(int a, Array b) {
        Array r = Array.factory(DataType.INT, b.getShape());
        for (int i = 0; i < b.getSize(); i++) {
            r.setInt(i, (int) Math.pow(a, b.getInt(i)));
        }

        return r;
    }

    private static Array powInt(Array a, Array b) {
        int broadcast = broadcastCheck(a, b);
        switch (broadcast) {
            case 0:
                Array r = Array.factory(DataType.INT, a.getShape());
                for (int i = 0; i < a.getSize(); i++) {
                    r.setInt(i, (int) Math.pow(a.getInt(i), b.getInt(i)));
                }
                return r;
            case 1:
                int[] shape = broadcast(a, b);
                r = Array.factory(DataType.INT, shape);
                Index index = r.getIndex();
                Index aindex = a.getIndex();
                Index bindex = b.getIndex();
                int n = r.getRank();
                int na = a.getRank();
                int nb = b.getRank();
                int[] current;
                for (int i = 0; i < r.getSize(); i++) {
                    current = index.getCurrentCounter();
                    setIndex(aindex, bindex, current, n, na, nb);
                    r.setInt(i, (int) Math.pow(a.getInt(aindex), b.getInt(bindex)));
                    index.incr();
                }
                return r;
            default:
                return null;
        }
    }

    private static Array powDouble(Array a, double b) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, Math.pow(a.getDouble(i), b));
        }

        return r;
    }

    private static Array powDouble(double a, Array b) {
        Array r = Array.factory(DataType.DOUBLE, b.getShape());
        for (int i = 0; i < b.getSize(); i++) {
            r.setDouble(i, Math.pow(a, b.getDouble(i)));
        }

        return r;
    }

    private static Array powDouble(Array a, Array b) {
        int broadcast = broadcastCheck(a, b);
        switch (broadcast) {
            case 0:
                Array r = Array.factory(DataType.DOUBLE, a.getShape());
                for (int i = 0; i < a.getSize(); i++) {
                    if (Double.isNaN(a.getDouble(i)) || Double.isNaN(b.getDouble(i))) {
                        r.setDouble(i, Double.NaN);
                    } else {
                        r.setDouble(i, Math.pow(a.getDouble(i), b.getDouble(i)));
                    }
                }
                return r;
            case 1:
                int[] shape = broadcast(a, b);
                r = Array.factory(DataType.DOUBLE, shape);
                Index index = r.getIndex();
                Index aindex = a.getIndex();
                Index bindex = b.getIndex();
                int n = r.getRank();
                int na = a.getRank();
                int nb = b.getRank();
                int[] current;
                for (int i = 0; i < r.getSize(); i++) {
                    current = index.getCurrentCounter();
                    setIndex(aindex, bindex, current, n, na, nb);
                    if (Double.isNaN(a.getDouble(aindex)) || Double.isNaN(b.getDouble(bindex))) {
                        r.setDouble(i, Double.NaN);
                    } else {
                        r.setDouble(i, Math.pow(a.getDouble(aindex), b.getDouble(bindex)));
                    }
                    index.incr();
                }
                return r;
            default:
                return null;
        }
    }

    private static Array powComplex(Array a, Array b) {
        int broadcast = broadcastCheck(a, b);
        switch (broadcast) {
            case 0:
                Array r = Array.factory(DataType.OBJECT, a.getShape());
                if (isComplex(a)) {
                    if (isComplex(b)) {
                        Complex v1, v2;
                        for (int i = 0; i < a.getSize(); i++) {
                            v1 = (Complex) a.getObject(i);
                            v2 = (Complex) b.getObject(i);
                            if (v1.isNaN() || v2.isNaN()) {
                                r.setObject(i, v1);
                            } else {
                                r.setObject(i, v1.pow(v2));
                            }
                        }
                    } else {
                        Complex v;
                        for (int i = 0; i < a.getSize(); i++) {
                            v = (Complex) a.getObject(i);
                            if (v.isNaN() || Double.isNaN(b.getDouble(i))) {
                                r.setObject(i, new Complex(Double.NaN));
                            } else {
                                r.setObject(i, v.pow(b.getDouble(i)));
                            }
                        }
                    }
                } else {
                    Complex v;
                    for (int i = 0; i < a.getSize(); i++) {
                        v = (Complex) b.getObject(i);
                        if (v.isNaN() || Double.isNaN(a.getDouble(i))) {
                            r.setObject(i, new Complex(Double.NaN));
                        } else {
                            r.setObject(i, v.pow(a.getDouble(i)));
                        }
                    }
                }
                return r;
            case 1:
                int[] shape = broadcast(a, b);
                r = Array.factory(DataType.OBJECT, shape);
                Index index = r.getIndex();
                Index aindex = a.getIndex();
                Index bindex = b.getIndex();
                int n = r.getRank();
                int na = a.getRank();
                int nb = b.getRank();
                int[] current;
                if (isComplex(a)) {
                    if (isComplex(b)) {
                        Complex v1, v2;
                        for (int i = 0; i < r.getSize(); i++) {
                            current = index.getCurrentCounter();
                            setIndex(aindex, bindex, current, n, na, nb);
                            v1 = (Complex) a.getObject(aindex);
                            v2 = (Complex) b.getObject(bindex);
                            if (v1.isNaN() || v2.isNaN()) {
                                r.setObject(i, v1);
                            } else {
                                r.setObject(i, v1.pow(v2));
                            }
                            index.incr();
                        }
                    } else {
                        Complex v;
                        for (int i = 0; i < r.getSize(); i++) {
                            current = index.getCurrentCounter();
                            setIndex(aindex, bindex, current, n, na, nb);
                            v = (Complex) a.getObject(aindex);
                            if (v.isNaN() || Double.isNaN(b.getDouble(bindex))) {
                                r.setObject(i, new Complex(Double.NaN));
                            } else {
                                r.setObject(i, v.pow(b.getDouble(bindex)));
                            }
                            index.incr();
                        }
                    }
                } else {
                    Complex v;
                    for (int i = 0; i < a.getSize(); i++) {
                        current = index.getCurrentCounter();
                        setIndex(aindex, bindex, current, n, na, nb);
                        v = (Complex) b.getObject(bindex);
                        if (v.isNaN() || Double.isNaN(a.getDouble(aindex))) {
                            r.setObject(i, new Complex(Double.NaN));
                        } else {
                            r.setObject(i, v.pow(a.getDouble(aindex)));
                        }
                    }
                }
                return r;
            default:
                return null;
        }
    }

    private static Array powComplex(Array a, double b) {
        Array r = Array.factory(DataType.OBJECT, a.getShape());
        Complex v;
        for (int i = 0; i < a.getSize(); i++) {
            v = (Complex) a.getObject(i);
            if (v.isNaN()) {
                r.setObject(i, v);
            } else {
                r.setObject(i, v.pow(b));
            }
        }

        return r;
    }

    private static Array powComplex(Array a, Complex b) {
        Array r = Array.factory(DataType.OBJECT, a.getShape());
        Complex v;
        for (int i = 0; i < a.getSize(); i++) {
            v = (Complex) a.getObject(i);
            if (v.isNaN()) {
                r.setObject(i, v);
            } else {
                r.setObject(i, v.pow(b));
            }
        }

        return r;
    }

    private static Array powComplex(double b, Array a) {
        Array r = Array.factory(DataType.OBJECT, a.getShape());
        Complex v;
        for (int i = 0; i < a.getSize(); i++) {
            v = (Complex) a.getObject(i);
            if (v.isNaN()) {
                r.setObject(i, v);
            } else {
                r.setObject(i, v.rPow(b));
            }
        }

        return r;
    }

    private static Array powComplex(Complex b, Array a) {
        Array r = Array.factory(DataType.OBJECT, a.getShape());
        Complex v;
        for (int i = 0; i < a.getSize(); i++) {
            v = (Complex) a.getObject(i);
            if (v.isNaN()) {
                r.setObject(i, v);
            } else {
                r.setObject(i, b.pow(v));
            }
        }

        return r;
    }

    /**
     * Sqrt function
     *
     * @param a Array a
     * @return Result array
     */
    public static Array sqrt(Array a) {
        return ArrayMath.pow(a, 0.5);
    }

    /**
     * Exponent function
     *
     * @param a Array a
     * @return Result array
     */
    public static Array exp(Array a) {
        Array r;
        if (isComplex(a)) {
            r = Array.factory(DataType.OBJECT, a.getShape());
            for (int i = 0; i < a.getSize(); i++) {
                r.setObject(i, ((Complex) a.getObject(i)).exp());
            }
        } else {
            r = Array.factory(DataType.DOUBLE, a.getShape());
            for (int i = 0; i < a.getSize(); i++) {
                r.setDouble(i, Math.exp(a.getDouble(i)));
            }
        }

        return r;
    }

    /**
     * Log function
     *
     * @param a Array a
     * @return Result array
     */
    public static Array log(Array a) {
        Array r;
        if (isComplex(a)) {
            r = Array.factory(DataType.OBJECT, a.getShape());
            for (int i = 0; i < a.getSize(); i++) {
                r.setObject(i, ((Complex) a.getObject(i)).log());
            }
        } else {
            r = Array.factory(DataType.DOUBLE, a.getShape());
            for (int i = 0; i < a.getSize(); i++) {
                r.setDouble(i, Math.log(a.getDouble(i)));
            }
        }

        return r;
    }

    /**
     * Log10 function
     *
     * @param a Array a
     * @return Result array
     */
    public static Array log10(Array a) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, Math.log10(a.getDouble(i)));
        }

        return r;
    }

    /**
     * Array absolute
     *
     * @param a Array a
     * @return Result array
     */
    public static Array abs(Array a) {
        Array r;
        if (isComplex(a)) {
            r = Array.factory(DataType.DOUBLE, a.getShape());
            for (int i = 0; i < a.getSize(); i++) {
                r.setDouble(i, ((Complex) a.getObject(i)).abs());
            }
        } else {
            r = Array.factory(a.getDataType(), a.getShape());
            for (int i = 0; i < a.getSize(); i++) {
                r.setDouble(i, Math.abs(a.getDouble(i)));
            }
        }

        return r;
    }

    /**
     * Array equal
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array equal(Array a, Array b) {
        //Array r = Array.factory(DataType.BOOLEAN, a.getShape());
        Array r = new ArrayBoolean(a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getDouble(i) == b.getDouble(i)) {
                r.setBoolean(i, true);
            } else {
                r.setBoolean(i, false);
            }
        }

        return r;
    }

    /**
     * Array equal
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array equal(Array a, Number b) {
        //Array r = Array.factory(DataType.BOOLEAN, a.getShape());
        Array r = new ArrayBoolean(a.getShape());
        double v = b.doubleValue();
        if (Double.isNaN(v)) {
            for (int i = 0; i < a.getSize(); i++) {
                if (Double.isNaN(a.getDouble(i))) {
                    r.setBoolean(i, true);
                } else {
                    r.setBoolean(i, false);
                }
            }
        } else {
            for (int i = 0; i < a.getSize(); i++) {
                if (a.getDouble(i) == v) {
                    r.setBoolean(i, true);
                } else {
                    r.setBoolean(i, false);
                }
            }
        }

        return r;
    }

    /**
     * Array less than
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array lessThan(Array a, Array b) {
        //Array r = Array.factory(DataType.BOOLEAN, a.getShape());
        Array r = new ArrayBoolean(a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getDouble(i) < b.getDouble(i)) {
                r.setBoolean(i, true);
            } else {
                r.setBoolean(i, false);
            }
        }

        return r;
    }

    /**
     * Array less than
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array lessThan(Array a, Number b) {
        //Array r = Array.factory(DataType.BOOLEAN, a.getShape());
        Array r = new ArrayBoolean(a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getDouble(i) < b.doubleValue()) {
                r.setBoolean(i, true);
            } else {
                r.setBoolean(i, false);
            }
        }

        return r;
    }

    /**
     * Array less than or equal
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array lessThanOrEqual(Array a, Array b) {
        //Array r = Array.factory(DataType.BOOLEAN, a.getShape());
        Array r = new ArrayBoolean(a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getDouble(i) <= b.getDouble(i)) {
                r.setBoolean(i, true);
            } else {
                r.setBoolean(i, false);
            }
        }

        return r;
    }

    /**
     * Array less than or equal
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array lessThanOrEqual(Array a, Number b) {
        //Array r = Array.factory(DataType.BOOLEAN, a.getShape());
        Array r = new ArrayBoolean(a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getDouble(i) <= b.doubleValue()) {
                r.setBoolean(i, true);
            } else {
                r.setBoolean(i, false);
            }
        }

        return r;
    }

    /**
     * Array greater than
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array greaterThan(Array a, Array b) {
        //Array r = Array.factory(DataType.BOOLEAN, a.getShape());
        Array r = new ArrayBoolean(a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getDouble(i) > b.getDouble(i)) {
                r.setBoolean(i, true);
            } else {
                r.setBoolean(i, false);
            }
        }

        return r;
    }

    /**
     * Array greater than
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array greaterThan(Array a, Number b) {
        //Array r = Array.factory(DataType.BOOLEAN, a.getShape());
        Array r = new ArrayBoolean(a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getDouble(i) > b.doubleValue()) {
                r.setBoolean(i, true);
            } else {
                r.setBoolean(i, false);
            }
        }

        return r;
    }

    /**
     * Array greater than or equal
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array greaterThanOrEqual(Array a, Array b) {
        //Array r = Array.factory(DataType.BOOLEAN, a.getShape());
        Array r = new ArrayBoolean(a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getDouble(i) >= b.getDouble(i)) {
                r.setBoolean(i, true);
            } else {
                r.setBoolean(i, false);
            }
        }

        return r;
    }

    /**
     * Array greater than or equal
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array greaterThanOrEqual(Array a, Number b) {
        //Array r = Array.factory(DataType.BOOLEAN, a.getShape());
        Array r = new ArrayBoolean(a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getDouble(i) >= b.doubleValue()) {
                r.setBoolean(i, true);
            } else {
                r.setBoolean(i, false);
            }
        }

        return r;
    }

    /**
     * Array not equal
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array notEqual(Array a, Array b) {
        //Array r = Array.factory(DataType.BOOLEAN, a.getShape());
        Array r = new ArrayBoolean(a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (a.getDouble(i) != b.getDouble(i)) {
                r.setBoolean(i, true);
            } else {
                r.setBoolean(i, false);
            }
        }

        return r;
    }

    /**
     * Array not equal
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array notEqual(Array a, Number b) {
        //Array r = Array.factory(DataType.BOOLEAN, a.getShape());
        Array r = new ArrayBoolean(a.getShape());
        double v = b.doubleValue();
        if (Double.isNaN(v)) {
            for (int i = 0; i < a.getSize(); i++) {
                if (Double.isNaN(a.getDouble(i))) {
                    r.setBoolean(i, false);
                } else {
                    r.setBoolean(i, true);
                }
            }
        } else {
            for (int i = 0; i < a.getSize(); i++) {
                if (a.getDouble(i) != v) {
                    r.setBoolean(i, true);
                } else {
                    r.setBoolean(i, false);
                }
            }
        }

        return r;
    }
    
    /**
     * Test whether any array element evaluates to True.
     * @param a The array
     * @return Boolean
     */
    public static boolean any(Array a) {
        IndexIterator ii = a.getIndexIterator();
        while (ii.hasNext()) {
            if (ii.getBooleanNext()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Test whether any array element along a given axis evaluates to True.
     *
     * @param a Array a
     * @param axis Axis
     * @return Boolean array
     * @throws ucar.ma2.InvalidRangeException
     */
    public static Array any(Array a, int axis) throws InvalidRangeException {
        int[] dataShape = a.getShape();
        int[] shape = new int[dataShape.length - 1];
        int idx;
        for (int i = 0; i < dataShape.length; i++) {
            idx = i;
            if (idx == axis) {
                continue;
            } else if (idx > axis) {
                idx -= 1;
            }
            shape[idx] = dataShape[i];
        }
        Array r = new ArrayBoolean(shape);
        boolean b;
        Index indexr = r.getIndex();
        int[] current;
        for (int i = 0; i < r.getSize(); i++) {
            current = indexr.getCurrentCounter();
            List<Range> ranges = new ArrayList<>();
            for (int j = 0; j < dataShape.length; j++) {
                if (j == axis) {
                    ranges.add(new Range(0, dataShape[j] - 1, 1));
                } else {
                    idx = j;
                    if (idx > axis) {
                        idx -= 1;
                    }
                    ranges.add(new Range(current[idx], current[idx], 1));
                }
            }
            IndexIterator ii = a.getRangeIterator(ranges);
            b = false;
            while (ii.hasNext()) {
                if (ii.getBooleanNext()) {
                    b = true;
                    break;
                }
            }
            r.setBoolean(i, b);
            indexr.incr();
        }

        return r;
    }
    
    /**
     * Test whether all array element evaluates to True.
     * @param a The array
     * @return Boolean
     */
    public static boolean all(Array a) {
        IndexIterator ii = a.getIndexIterator();
        while (ii.hasNext()) {
            if (!ii.getBooleanNext()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Test whether all array element along a given axis evaluates to True.
     *
     * @param a Array a
     * @param axis Axis
     * @return Boolean array
     * @throws ucar.ma2.InvalidRangeException
     */
    public static Array all(Array a, int axis) throws InvalidRangeException {
        int[] dataShape = a.getShape();
        int[] shape = new int[dataShape.length - 1];
        int idx;
        for (int i = 0; i < dataShape.length; i++) {
            idx = i;
            if (idx == axis) {
                continue;
            } else if (idx > axis) {
                idx -= 1;
            }
            shape[idx] = dataShape[i];
        }
        Array r = new ArrayBoolean(shape);
        boolean b;
        Index indexr = r.getIndex();
        int[] current;
        for (int i = 0; i < r.getSize(); i++) {
            current = indexr.getCurrentCounter();
            List<Range> ranges = new ArrayList<>();
            for (int j = 0; j < dataShape.length; j++) {
                if (j == axis) {
                    ranges.add(new Range(0, dataShape[j] - 1, 1));
                } else {
                    idx = j;
                    if (idx > axis) {
                        idx -= 1;
                    }
                    ranges.add(new Range(current[idx], current[idx], 1));
                }
            }
            IndexIterator ii = a.getRangeIterator(ranges);
            b = true;
            while (ii.hasNext()) {
                if (!ii.getBooleanNext()) {
                    b = false;
                    break;
                }
            }
            r.setBoolean(i, b);
            indexr.incr();
        }

        return r;
    }

    /**
     * Return the array with the value of 1 when the input array element value
     * in the list b, otherwise set value as 0.
     *
     * @param a Array a
     * @param b List b
     * @return Result array
     */
    public static Array inValues(Array a, List b) {
        //Array r = Array.factory(DataType.BOOLEAN, a.getShape());
        Array r = new ArrayBoolean(a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (b.contains(a.getObject(i))) {
                r.setBoolean(i, true);
            } else {
                r.setBoolean(i, false);
            }
        }

        return r;
    }

    /**
     * Check if the array contains NaN value
     *
     * @param a Input array
     * @return Boolean
     */
    public static boolean containsNaN(Array a) {
        boolean hasNaN = false;
        for (int i = 0; i < a.getSize(); i++) {
            if (Double.isNaN(a.getDouble(i))) {
                hasNaN = true;
                break;
            }
        }

        return hasNaN;
    }
    
    /**
     * Remove NaN values in an array
     * @param a The array
     * @return The array withou NaN values
     */
    public static Array removeNaN(Array a) {
        List d = new ArrayList<>();
        for (int i = 0; i < a.getSize(); i++) {
            if (!Double.isNaN(a.getDouble(i))) {
                d.add(a.getObject(i));
            }
        }
        
        if (d.isEmpty()) {
            return null;
        }
        
        Array r = ArrayUtil.factory(a.getDataType(), new int[]{d.size()});
        for (int i = 0; i < d.size(); i++) {
            r.setObject(i, d.get(i));
        }
        
        return r;
    }
    
    /**
     * Remove NaN values in arrays
     * @param a The arrays
     * @return The array withou NaN values
     */
    public static Array[] removeNaN(Array... a) {
        if (a.length == 1) {
            Array r0 = removeNaN(a[0]);            
            return r0 == null ? null : new Array[]{removeNaN(a[0])};
        }
        
        List d = new ArrayList<>();
        int n = (int)a[0].getSize();
        int m = a.length;
        boolean isNan;
        for (int i = 0; i < n; i++) {
            isNan = false;
            for (Array aa : a) {
                if (Double.isNaN(aa.getDouble(i))) {
                    isNan = true;
                    break;
                }
            }
            if (!isNan) {
                for (Array aa :a) {
                    d.add(aa.getObject(i));
                }
            }
        }
        
        if (d.isEmpty()) {
            return null;
        }
        
        int len = d.size() / m;
        Array[] r = new Array[m];
        for (int i = 0; i < m; i++) {
            r[i] = ArrayUtil.factory(a[i].getDataType(), new int[]{len});
            int jj = i;
            for (int j = 0; j < len; j++) {
                r[i].setObject(j, d.get(jj));
                jj += m;
            }
        }
        
        return r;
    }
    
    /**
     * Return the indices of the elements that are non-zero.
     *
     * @param a Input array
     * @return Indices
     */
    public static List<Array> nonzero(Array a) {
        List<List<Integer>> r = new ArrayList<>();
        int ndim = a.getRank();
        for (int i = 0; i < ndim; i++) {
            r.add(new ArrayList<Integer>());
        }
        Index index = a.getIndex();
        int[] counter;
        double v;
        for (int i = 0; i < a.getSize(); i++) {
            v = a.getDouble(i);
            if (!Double.isNaN(v) && v != 0) {
                counter = index.getCurrentCounter();
                for (int j = 0; j < ndim; j++) {
                    r.get(j).add(counter[j]);
                }
            }
            index.incr();
        }

        if (r.get(0).isEmpty()) {
            return null;
        }

        List<Array> ra = new ArrayList<>();
        for (int i = 0; i < ndim; i++) {
            ra.add(ArrayUtil.array(r.get(i)));
        }
        return ra;
    }

    /**
     * Bit and & operation
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array bitAnd(Array a, Number b) {
        Array r = ArrayUtil.factory(a.getDataType(), a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setObject(i, a.getInt(i) & b.intValue());
        }

        return r;
    }

    /**
     * Bit and & operation
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array bitAnd(Array a, Array b) {
        Array r = ArrayUtil.factory(a.getDataType(), a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setObject(i, a.getInt(i) & b.getInt(i));
        }

        return r;
    }

    /**
     * Bit or | operation
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array bitOr(Array a, Number b) {
        Array r = ArrayUtil.factory(a.getDataType(), a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setObject(i, a.getInt(i) | b.intValue());
        }

        return r;
    }

    /**
     * Bit or | operation
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array bitOr(Array a, Array b) {
        Array r = ArrayUtil.factory(a.getDataType(), a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setObject(i, a.getInt(i) | b.getInt(i));
        }

        return r;
    }

    /**
     * Bit exclusive or ^ operation
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array bitXor(Array a, Number b) {
        Array r = ArrayUtil.factory(a.getDataType(), a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setObject(i, a.getInt(i) ^ b.intValue());
        }

        return r;
    }

    /**
     * Bit exclusive or | operation
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array bitXor(Array a, Array b) {
        Array r = ArrayUtil.factory(a.getDataType(), a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setObject(i, a.getInt(i) ^ b.getInt(i));
        }

        return r;
    }

    /**
     * Bit inversion ~ operation
     *
     * @param a Array a
     * @return Result array
     */
    public static Array bitInvert(Array a) {
        Array r = ArrayUtil.factory(a.getDataType(), a.getShape());
        if (a.getDataType() == DataType.BOOLEAN) {
            for (int i = 0; i < a.getSize(); i++) {
                r.setObject(i, !a.getBoolean(i));
            }
        } else {
            for (int i = 0; i < a.getSize(); i++) {
                r.setObject(i, ~a.getInt(i));
            }
        }

        return r;
    }

    /**
     * Bit left shift << operation
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array leftShift(Array a, Number b) {
        Array r = ArrayUtil.factory(a.getDataType(), a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setObject(i, a.getInt(i) << b.intValue());
        }

        return r;
    }

    /**
     * Bit left shift << operation
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array leftShift(Array a, Array b) {
        Array r = ArrayUtil.factory(a.getDataType(), a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setObject(i, a.getInt(i) << b.getInt(i));
        }

        return r;
    }

    /**
     * Bit right shift >> operation
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array rightShift(Array a, Number b) {
        Array r = ArrayUtil.factory(a.getDataType(), a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setObject(i, a.getInt(i) >> b.intValue());
        }

        return r;
    }

    /**
     * Bit right shift >> operation
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array rightShift(Array a, Array b) {
        Array r = ArrayUtil.factory(a.getDataType(), a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setObject(i, a.getInt(i) >> b.getInt(i));
        }

        return r;
    }

    /**
     * Integrate vector array using the composite trapezoidal rule.
     *
     * @param y Vecotr array
     * @param dx Spacing between all y elements
     * @return Definite integral as approximated by trapezoidal rule
     */
    public static double trapz(Array y, double dx) {
        int n = (int) y.getSize() - 1;
        double a = 1;
        double b = n * dx + a;
        double r = 0, v;
        int nn = 0;
        for (int i = 0; i < y.getSize(); i++) {
            v = y.getDouble(i);
            if (Double.isNaN(v)) {
                continue;
            }
            r += y.getDouble(i);
            if (i > 0 && i < n) {
                r += y.getDouble(i);
            }
            nn += 1;
        }
        if (nn >= 2) {
            r = r * ((b - a) / (2 * n));
        } else {
            r = Double.NaN;
        }
        return r;
    }

    /**
     * Integrate vector array using the composite trapezoidal rule.
     *
     * @param y Vecotr array
     * @param dx Spacing between all y elements
     * @param ranges
     * @return Definite integral as approximated by trapezoidal rule
     * @throws ucar.ma2.InvalidRangeException
     */
    public static double trapz(Array y, double dx, List<Range> ranges) throws InvalidRangeException {
        int n = 1;
        for (Range range : ranges) {
            n = n * range.length();
        }
        n -= 1;
        double a = 1;
        double b = n * dx + a;
        double r = 0;
        double v;
        IndexIterator ii = y.getRangeIterator(ranges);
        int i = 0;
        while (ii.hasNext()) {
            v = ii.getDoubleNext();
            if (Double.isNaN(v)) {
                continue;
            }
            r += v;
            if (i > 0 && i < n) {
                r += v;
            }
            i += 1;
        }
        if (i >= 2) {
            r = r * ((b - a) / (2 * n));
        } else {
            r = Double.NaN;
        }
        return r;
    }

    /**
     * Integrate vector array using the composite trapezoidal rule.
     *
     * @param y Vecotr array
     * @param x Spacing array between all y elements
     * @return Definite integral as approximated by trapezoidal rule
     */
    public static double trapz(Array y, Array x) {
        int n = (int) y.getSize() - 1;
        double r = 0;
        double v;
        int nn = 0;
        for (int i = 0; i < n; i++) {
            v = y.getDouble(i);
            if (Double.isNaN(v)) {
                continue;
            }
            r += (x.getDouble(i + 1) - x.getDouble(i)) * (y.getDouble(i + 1) + v);
            nn += 1;
        }
        if (nn >= 2) {
            r = r / 2;
        } else {
            r = Double.NaN;
        }
        return r;
    }

    /**
     * Integrate vector array using the composite trapezoidal rule.
     *
     * @param y Vecotr array
     * @param x Spacing array between all y elements
     * @param ranges
     * @return Definite integral as approximated by trapezoidal rule
     * @throws ucar.ma2.InvalidRangeException
     */
    public static double trapz(Array y, Array x, List<Range> ranges) throws InvalidRangeException {
        double r = 0;
        double v;
        double v0 = Double.NEGATIVE_INFINITY;
        IndexIterator ii = y.getRangeIterator(ranges);
        int i = 0, n = 0;
        while (ii.hasNext()) {
            v = ii.getDoubleNext();
            if (Double.isNaN(v)) {
                i += 1;
                continue;
            }
            if (Double.isInfinite(v0)) {
                v0 = v;
                continue;
            }
            r += (x.getDouble(i + 1) - x.getDouble(i)) * (v + v0);
            v0 = v;
            i += 1;
            n += 1;
        }
        if (n >= 2) {
            r = r / 2;
        } else {
            r = Double.NaN;
        }
        return r;
    }

    /**
     * Integrate vector array using the composite trapezoidal rule.
     *
     * @param a Array a
     * @param dx
     * @param axis Axis
     * @return Mean value array
     * @throws ucar.ma2.InvalidRangeException
     */
    public static Array trapz(Array a, double dx, int axis) throws InvalidRangeException {
        int[] dataShape = a.getShape();
        int[] shape = new int[dataShape.length - 1];
        int idx;
        for (int i = 0; i < dataShape.length; i++) {
            idx = i;
            if (idx == axis) {
                continue;
            } else if (idx > axis) {
                idx -= 1;
            }
            shape[idx] = dataShape[i];
        }
        Array r = Array.factory(DataType.DOUBLE, shape);
        double mean;
        Index indexr = r.getIndex();
        int[] current;
        for (int i = 0; i < r.getSize(); i++) {
            current = indexr.getCurrentCounter();
            List<Range> ranges = new ArrayList<>();
            for (int j = 0; j < dataShape.length; j++) {
                if (j == axis) {
                    ranges.add(new Range(0, dataShape[j] - 1, 1));
                } else {
                    idx = j;
                    if (idx > axis) {
                        idx -= 1;
                    }
                    ranges.add(new Range(current[idx], current[idx], 1));
                }
            }
            mean = trapz(a, dx, ranges);
            r.setDouble(i, mean);
            indexr.incr();
        }

        return r;
    }

    /**
     * Integrate vector array using the composite trapezoidal rule.
     *
     * @param a Array a
     * @param x Array x
     * @param axis Axis
     * @return Mean value array
     * @throws ucar.ma2.InvalidRangeException
     */
    public static Array trapz(Array a, Array x, int axis) throws InvalidRangeException {
        int[] dataShape = a.getShape();
        int[] shape = new int[dataShape.length - 1];
        int idx;
        for (int i = 0; i < dataShape.length; i++) {
            idx = i;
            if (idx == axis) {
                continue;
            } else if (idx > axis) {
                idx -= 1;
            }
            shape[idx] = dataShape[i];
        }
        Array r = Array.factory(DataType.DOUBLE, shape);
        double mean;
        Index indexr = r.getIndex();
        int[] current;
        for (int i = 0; i < r.getSize(); i++) {
            current = indexr.getCurrentCounter();
            List<Range> ranges = new ArrayList<>();
            for (int j = 0; j < dataShape.length; j++) {
                if (j == axis) {
                    ranges.add(new Range(0, dataShape[j] - 1, 1));
                } else {
                    idx = j;
                    if (idx > axis) {
                        idx -= 1;
                    }
                    ranges.add(new Range(current[idx], current[idx], 1));
                }
            }
            mean = trapz(a, x, ranges);
            r.setDouble(i, mean);
            indexr.incr();
        }

        return r;
    }
 
    /**
     * Returns an element-wise indication of the sign of a number.
     * The sign function returns -1 if x < 0, 0 if x==0, 1 if x > 0. nan is returned for nan inputs.
     * @param x Input array
     * @return The sign of x array
     */
    public static Array sign(Array x) {
        Array r = Array.factory(DataType.FLOAT, x.getShape());
        for (int i = 0; i < r.getSize(); i++) {
            r.setFloat(i, Math.signum(x.getFloat(i)));
        }
        
        return r;
    }

    // </editor-fold>
    // <editor-fold desc="Matrix">
    /**
     * Matrix multiplication
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array dot(Array a, Array b) {
        DataType type = ArrayMath.commonType(a.getDataType(), b.getDataType());
        int[] shape;
        if (a.getRank() == 2) {
            if (a.getShape()[1] != b.getShape()[0]) {
                return null;
            }
            if (b.getRank() == 2) {
                shape = new int[2];
                shape[0] = a.getShape()[0];
                shape[1] = b.getShape()[1];
            } else {
                shape = new int[1];
                shape[0] = a.getShape()[0];
            }
            Array r = Array.factory(type, shape);
            Index aIndex = a.getIndex();
            Index bIndex = b.getIndex();
            Index rIndex = r.getIndex();
            int n = a.getShape()[1];
            double v;
            if (b.getRank() == 2) {
                for (int i = 0; i < shape[0]; i++) {
                    for (int j = 0; j < shape[1]; j++) {
                        v = 0;
                        for (int m = 0; m < n; m++) {
                            v = v + a.getDouble(aIndex.set(i, m)) * b.getDouble(bIndex.set(m, j));
                        }
                        r.setDouble(rIndex.set(i, j), v);
                    }
                }
            } else {
                for (int i = 0; i < shape[0]; i++) {
                    v = 0;
                    for (int m = 0; m < n; m++) {
                        v = v + a.getDouble(aIndex.set(i, m)) * b.getDouble(bIndex.set(m));
                    }
                    r.setDouble(rIndex.set(i), v);
                }
            }
            return r;
        } else {
            if (a.getShape()[0] != b.getShape()[0]) {
                return null;
            }
            shape = new int[1];
            shape[0] = b.getShape()[1];
            Array r = Array.factory(type, shape);
            double v;
            int n = a.getShape()[0];
            Index bIndex = b.getIndex();
            for (int i = 0; i < shape[0]; i++) {
                v = 0;
                for (int j = 0; j < n; j++) {
                    v += a.getDouble(j) * b.getDouble(bIndex.set(j, i));
                }
                r.setDouble(i, v);
            }
            return r;
        }
    }

    /**
     * Return the dot product of two vectors.
     *
     * @param a Vector a
     * @param b Vector b
     * @return Result
     */
    public static double vdot(Array a, Array b) {
        double r = 0;
        for (int i = 0; i < a.getSize(); i++) {
            r += a.getDouble(i) * b.getDouble(i);
        }

        return r;
    }

    // </editor-fold>
    // <editor-fold desc="Circular function">
    /**
     * Convert radians to degrees function
     *
     * @param a Array a
     * @return Result array
     */
    public static Array toDegrees(Array a) {
        Array r = Array.factory(a.getDataType() == DataType.DOUBLE ? DataType.DOUBLE : DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, Math.toDegrees(a.getDouble(i)));
        }

        return r;
    }

    /**
     * Convert radians to degrees function
     *
     * @param a Array a
     * @return Result array
     */
    public static Array toRadians(Array a) {
        Array r = Array.factory(a.getDataType() == DataType.DOUBLE ? DataType.DOUBLE : DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, Math.toRadians(a.getDouble(i)));
        }

        return r;
    }

    /**
     * Sine function
     *
     * @param a Array a
     * @return Result array
     */
    public static Array sin(Array a) {
        Array r;
        if (isComplex(a)) {
            r = Array.factory(DataType.OBJECT, a.getShape());
            for (int i = 0; i < a.getSize(); i++) {
                r.setObject(i, ((Complex) a.getObject(i)).sin());
            }
        } else {
            r = Array.factory(a.getDataType() == DataType.DOUBLE ? DataType.DOUBLE : DataType.FLOAT, a.getShape());
            for (int i = 0; i < a.getSize(); i++) {
                r.setDouble(i, Math.sin(a.getDouble(i)));
            }
        }

        return r;
    }

    /**
     * Cosine function
     *
     * @param a Array a
     * @return Result array
     */
    public static Array cos(Array a) {
        Array r;
        if (isComplex(a)) {
            r = Array.factory(DataType.OBJECT, a.getShape());
            for (int i = 0; i < a.getSize(); i++) {
                r.setObject(i, ((Complex) a.getObject(i)).cos());
            }
        } else {
            r = Array.factory(a.getDataType() == DataType.DOUBLE ? DataType.DOUBLE : DataType.FLOAT, a.getShape());
            for (int i = 0; i < a.getSize(); i++) {
                r.setDouble(i, Math.cos(a.getDouble(i)));
            }
        }

        return r;
    }

    /**
     * Tangent function
     *
     * @param a Array a
     * @return Result array
     */
    public static Array tan(Array a) {
        Array r;
        if (isComplex(a)) {
            r = Array.factory(DataType.OBJECT, a.getShape());
            for (int i = 0; i < a.getSize(); i++) {
                r.setObject(i, ((Complex) a.getObject(i)).tan());
            }
        } else {
            r = Array.factory(a.getDataType() == DataType.DOUBLE ? DataType.DOUBLE : DataType.FLOAT, a.getShape());
            for (int i = 0; i < a.getSize(); i++) {
                r.setDouble(i, Math.tan(a.getDouble(i)));
            }
        }

        return r;
    }

    /**
     * Arc sine function
     *
     * @param a Array a
     * @return Result array
     */
    public static Array asin(Array a) {
        Array r;
        if (isComplex(a)) {
            r = Array.factory(DataType.OBJECT, a.getShape());
            for (int i = 0; i < a.getSize(); i++) {
                r.setObject(i, ((Complex) a.getObject(i)).asin());
            }
        } else {
            r = Array.factory(a.getDataType() == DataType.DOUBLE ? DataType.DOUBLE : DataType.FLOAT, a.getShape());
            for (int i = 0; i < a.getSize(); i++) {
                r.setDouble(i, Math.asin(a.getDouble(i)));
            }
        }

        return r;
    }

    /**
     * Arc cosine function
     *
     * @param a Array a
     * @return Result array
     */
    public static Array acos(Array a) {
        Array r;
        if (isComplex(a)) {
            r = Array.factory(DataType.OBJECT, a.getShape());
            for (int i = 0; i < a.getSize(); i++) {
                r.setObject(i, ((Complex) a.getObject(i)).acos());
            }
        } else {
            r = Array.factory(a.getDataType() == DataType.DOUBLE ? DataType.DOUBLE : DataType.FLOAT, a.getShape());
            for (int i = 0; i < a.getSize(); i++) {
                r.setDouble(i, Math.acos(a.getDouble(i)));
            }
        }

        return r;
    }

    /**
     * Arc tangen function
     *
     * @param a Array a
     * @return Result array
     */
    public static Array atan(Array a) {
        Array r;
        if (isComplex(a)) {
            r = Array.factory(DataType.OBJECT, a.getShape());
            for (int i = 0; i < a.getSize(); i++) {
                r.setObject(i, ((Complex) a.getObject(i)).atan());
            }
        } else {
            r = Array.factory(a.getDataType() == DataType.DOUBLE ? DataType.DOUBLE : DataType.FLOAT, a.getShape());
            for (int i = 0; i < a.getSize(); i++) {
                r.setDouble(i, Math.atan(a.getDouble(i)));
            }
        }

        return r;
    }

    /**
     * Arc tangen function
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array atan2(Array a, Array b) {
        Array r = Array.factory(a.getDataType() == DataType.DOUBLE ? DataType.DOUBLE : DataType.FLOAT, a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            r.setDouble(i, Math.atan2(a.getDouble(i), b.getDouble(i)));
        }

        return r;
    }

    /**
     * Convert cartesian to polar coordinate
     *
     * @param x X array
     * @param y Y array
     * @return Angle and radius
     */
    public static Array[] cartesianToPolar(Array x, Array y) {
        Array r = Array.factory(DataType.DOUBLE, x.getShape());
        Array B = Array.factory(DataType.DOUBLE, x.getShape());
        double[] rr;
        for (int i = 0; i < x.getSize(); i++) {
            rr = cartesianToPolar(x.getDouble(i), y.getDouble(i));
            r.setDouble(i, rr[1]);
            B.setDouble(i, rr[0]);
        }

        return new Array[]{B, r};
    }

    /**
     * Convert poar to cartesian coordinate
     *
     * @param r Radius
     * @param B Angle in radians
     * @return X and y in cartesian coordinate
     */
    public static Array[] polarToCartesian(Array B, Array r) {
        Array x = Array.factory(DataType.DOUBLE, r.getShape());
        Array y = Array.factory(DataType.DOUBLE, r.getShape());
        double[] rr;
        for (int i = 0; i < r.getSize(); i++) {
            rr = polarToCartesian(B.getDouble(i), r.getDouble(i));
            x.setDouble(i, rr[0]);
            y.setDouble(i, rr[1]);
        }

        return new Array[]{x, y};
    }

    /**
     * Convert cartesian to polar coordinate
     *
     * @param x X
     * @param y Y
     * @return Angle and radius
     */
    public static double[] cartesianToPolar(double x, double y) {
        double r;     // Radius
        double B;     // Angle in radians
        r = Math.hypot(x, y);
        if (y >= 0) {
            if (x == 0) {
                B = Math.PI / 2;// 90°
            } else {
                B = Math.asin(x / y);
            }
        } else if (x == 0) {
            B = 3 * Math.PI / 2;// 270°
        } else {
            B = Math.asin(x / y);
        }
        return new double[]{B, r};
    }

    /**
     * Convert poar to cartesian coordinate
     *
     * @param r Radius
     * @param B Angle in radians
     * @return X and y in cartesian coordinate
     */
    public static double[] polarToCartesian(double B, double r) {
        double x = Math.cos(B) * r;
        double y = Math.sin(B) * r;

        return new double[]{x, y};
    }

    // </editor-fold>
    // <editor-fold desc="Section/Flip/Transpos...">
    /**
     * Copy array
     *
     * @param a Input array
     * @return Copied array
     */
    public static Array copy(Array a) {
        Array r = Array.factory(a.getDataType(), a.getShape());
        MAMath.copy(r, a);
        return r;
    }

    /**
     * Section array
     *
     * @param a Array a
     * @param origin Origin array
     * @param size Size array
     * @param stride Stride array
     * @return Result array
     * @throws InvalidRangeException
     */
    public static Array section(Array a, int[] origin, int[] size, int[] stride) throws InvalidRangeException {
        Array r = a.section(origin, size, stride);
        Array rr = Array.factory(r.getDataType(), r.getShape());
        MAMath.copy(rr, r);
        return rr;
    }

    /**
     * Section array
     *
     * @param a Array a
     * @param ranges Ranges
     * @return Result array
     * @throws InvalidRangeException
     */
    public static Array section(Array a, List<Range> ranges) throws InvalidRangeException {
        Array r = a.section(ranges);
        Array rr = Array.factory(r.getDataType(), r.getShape());
        MAMath.copy(rr, r);
        return rr;
    }

    /**
     * Take elements from an array along an axis.
     *
     * @param a The array
     * @param ranges The indices of the values to extract.
     * @return The returned array has the same type as a.
     */
    public static Array take(Array a, List<Object> ranges) {
        int n = a.getRank();
        int[] shape = new int[n];
        List<List<Integer>> indexlist = new ArrayList<>();
        List<Integer> list;
        for (int i = 0; i < n; i++) {
            Object k = ranges.get(i);
            if (k instanceof Range) {
                Range kr = (Range) k;
                shape[i] = kr.length();
                list = new ArrayList<>();
                for (int j = kr.first(); j <= kr.last(); j += kr.stride()) {
                    list.add(j);
                }
                indexlist.add(list);
            } else {
                List<Integer> kl = (List<Integer>) k;
                shape[i] = kl.size();
                indexlist.add(kl);
            }
        }

        Array r = Array.factory(a.getDataType(), shape);
        IndexIterator ii = r.getIndexIterator();
        int[] current, acurrent = new int[n];
        Index index = a.getIndex();
        while (ii.hasNext()) {
            ii.next();
            current = ii.getCurrentCounter();
            for (int i = 0; i < n; i++) {
                acurrent[i] = indexlist.get(i).get(current[i]);
            }
            index.set(acurrent);
            ii.setObjectCurrent(a.getObject(index));
        }

        return r;
    }

    /**
     * Take elements from an array.
     *
     * @param a The array
     * @param ranges The indices of the values to extract.
     * @return The returned array has the same type as a.
     */
    public static Array takeValues(Array a, List<List<Integer>> ranges) {
        int n = a.getRank();
        int nn = ranges.get(0).size();
        int[] shape = new int[]{nn};
        Array r = Array.factory(a.getDataType(), shape);
        Index index = a.getIndex();
        int[] current = new int[n];
        for (int i = 0; i < nn; i++) {
            for (int j = 0; j < n; j++) {
                current[j] = ranges.get(j).get(i);
            }
            index.set(current);
            r.setObject(i, a.getObject(index));
        }

        return r;
    }

    /**
     * Set section
     *
     * @param a Array a
     * @param ranges Ranges
     * @param v Number value
     * @return Result array
     * @throws InvalidRangeException
     */
    public static Array setSection(Array a, List<Range> ranges, Number v) throws InvalidRangeException {
        Array r = a.section(ranges);
        IndexIterator iter = r.getIndexIterator();
        if (a.getDataType() == DataType.BOOLEAN) {
            boolean b = true;
            if (v.doubleValue() == 0) {
                b = false;
            }
            while (iter.hasNext()) {
                iter.setObjectNext(b);
            }
        } else {
            while (iter.hasNext()) {
                iter.setObjectNext(v);
            }
        }
        r = ArrayUtil.factory(a.getDataType(), a.getShape(), r.getStorage());
        return r;
    }

    /**
     * Set section
     *
     * @param a Array a
     * @param ranges Ranges
     * @param v Array value
     * @return Result array
     * @throws InvalidRangeException
     */
    public static Array setSection(Array a, List<Range> ranges, Array v) throws InvalidRangeException {
        Array r = a.section(ranges);
        IndexIterator iter = r.getIndexIterator();
        //int[] current;
        Index index = v.getIndex();
        while (iter.hasNext()) {
            iter.next();
            //current = iter.getCurrentCounter();
            //index.set(current);
            iter.setObjectCurrent(v.getObject(index));
            index.incr();
        }
        r = ArrayUtil.factory(a.getDataType(), a.getShape(), r.getStorage());
        return r;
    }

    /**
     * Set section
     *
     * @param a Array a
     * @param ranges Ranges
     * @param v Number value
     * @return Result array
     */
    public static Array setSection_Mix(Array a, List<Object> ranges, Number v) {
        Array r = ArrayUtil.factory(a.getDataType(), a.getShape());
        int n = a.getRank();
        IndexIterator iter = r.getIndexIterator();
        Index aidx = a.getIndex();
        int[] current;
        boolean isIn;
        while (iter.hasNext()) {
            iter.next();
            current = iter.getCurrentCounter();
            isIn = true;
            for (int i = 0; i < n; i++) {
                Object k = ranges.get(i);
                if (k instanceof Range) {
                    if (!((Range) k).contains(current[i])) {
                        isIn = false;
                        break;
                    }
                } else {
                    if (!((List<Integer>) k).contains(current[i])) {
                        isIn = false;
                        break;
                    }
                }
            }
            aidx.set(current);
            if (isIn) {
                iter.setObjectCurrent(v);
            } else {
                iter.setObjectCurrent(a.getObject(aidx));
            }
        }

        return r;
    }

    /**
     * Set section
     *
     * @param a Array a
     * @param ranges Ranges
     * @param v Array value
     * @return Result array
     */
    public static Array setSection_Mix(Array a, List<Object> ranges, Array v) {
        Array r = Array.factory(a.getDataType(), a.getShape());
        int n = a.getRank();
        IndexIterator iter = r.getIndexIterator();
        Index aidx = a.getIndex();
        Index vidx = v.getIndex();
        int[] current;
        boolean isIn;
        while (iter.hasNext()) {
            iter.next();
            current = iter.getCurrentCounter();
            isIn = true;
            for (int i = 0; i < n; i++) {
                Object k = ranges.get(i);
                if (k instanceof Range) {
                    if (!((Range) k).contains(current[i])) {
                        isIn = false;
                        break;
                    }
                } else {
                    if (!((List<Integer>) k).contains(current[i])) {
                        isIn = false;
                        break;
                    }
                }
            }
            if (isIn) {
                iter.setObjectCurrent(v.getObject(vidx));
                vidx.incr();
            } else {
                aidx.set(current);
                iter.setObjectCurrent(a.getObject(aidx));
            }
        }

        return r;
    }

    /**
     * Set section
     *
     * @param a Array a
     * @param ranges Ranges
     * @param v Number value
     * @return Result array
     */
    public static Array setSection_List(Array a, List<List<Integer>> ranges, Number v) {
        Array r = copy(a);
        int n = r.getRank();
        int[] count = new int[n];
        Index index = Index.factory(count);
        int m = ranges.get(0).size();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                count[j] = ranges.get(j).get(i);
            }
            index.set(count);
            r.setObject(index, v);
        }

        return r;
    }

    /**
     * Set section
     *
     * @param a Array a
     * @param ranges Ranges
     * @param v Array value
     * @return Result array
     */
    public static Array setSection_List(Array a, List<List<Integer>> ranges, Array v) {
        Array r = copy(a);
        int n = r.getRank();
        int[] count = new int[n];
        Index index = Index.factory(count);
        Index vIndex = v.getIndex();
        int m = ranges.get(0).size();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                count[j] = ranges.get(j).get(i);
            }
            index.set(count);
            r.setObject(index, v.getObject(vIndex));
            vIndex.incr();
        }

        return r;
    }

    /**
     * Flip array
     *
     * @param a Array a
     * @param idxs Dimension index list
     * @return Result array
     */
    public static Array flip(Array a, List<Integer> idxs) {
        Array r = Array.factory(a.getDataType(), a.getShape());
        for (int i : idxs) {
            r = a.flip(i);
        }
        Array rr = Array.factory(r.getDataType(), r.getShape());
        MAMath.copy(rr, r);
        return rr;
    }

    /**
     * Flip array
     *
     * @param a Array a
     * @param idx Dimension idex
     * @return Result array
     */
    public static Array flip(Array a, int idx) {
        Array r = a.flip(idx);
        Array rr = Array.factory(r.getDataType(), r.getShape());
        MAMath.copy(rr, r);
        return rr;
    }

    /**
     * Transpose array
     *
     * @param a Array a
     * @param dim1 Dimension index 1
     * @param dim2 Dimension index 2
     * @return Result array
     */
    public static Array transpose(Array a, int dim1, int dim2) {
        Array r = a.transpose(dim1, dim2);
        Array rr = Array.factory(r.getDataType(), r.getShape());
        MAMath.copy(rr, r);
        return rr;
    }

    /**
     * Rotate an array by 90 degrees in counter-clockwise direction.
     *
     * @param a The array
     * @param k Rotate times
     * @return Rotated array
     */
    public static Array rot90(Array a, int k) {
        int[] shape = new int[a.getRank()];
        if (Math.abs(k) % 2 == 1) {
            shape[0] = a.getShape()[1];
            shape[1] = a.getShape()[0];
        } else {
            shape[0] = a.getShape()[0];
            shape[1] = a.getShape()[1];
        }
        if (a.getRank() > 2) {
            for (int i = 2; i < a.getRank(); i++) {
                shape[i] = a.getShape()[i];
            }
        }
        Array r = Array.factory(a.getDataType(), shape);
        Index indexa = a.getIndex();
        Index indexr = r.getIndex();
        int[] countera, counterr;
        switch (k) {
            case 1:
            case -3:
                for (int i = 0; i < r.getSize(); i++) {
                    countera = indexa.getCurrentCounter();
                    counterr = indexa.getCurrentCounter();
                    counterr[0] = shape[0] - countera[1] - 1;
                    counterr[1] = countera[0];
                    indexr.set(counterr);
                    r.setObject(indexr, a.getObject(indexa));
                    indexa.incr();
                }
                break;
            case 2:
            case -2:
                for (int i = 0; i < r.getSize(); i++) {
                    countera = indexa.getCurrentCounter();
                    counterr = indexa.getCurrentCounter();
                    counterr[0] = shape[0] - countera[0] - 1;
                    counterr[1] = shape[1] - countera[1] - 1;
                    indexr.set(counterr);
                    r.setObject(indexr, a.getObject(indexa));
                    indexa.incr();
                }
                break;
            case 3:
            case -1:
                for (int i = 0; i < r.getSize(); i++) {
                    countera = indexa.getCurrentCounter();
                    counterr = indexa.getCurrentCounter();
                    counterr[0] = countera[1];
                    counterr[1] = shape[1] - countera[0] - 1;
                    indexr.set(counterr);
                    r.setObject(indexr, a.getObject(indexa));
                    indexa.incr();
                }
                break;
            default:
                r = null;
        }

        return r;
    }

    /**
     * Join two arrays by a dimension
     *
     * @param a Array a
     * @param b Array b
     * @param dim Dimension for join
     * @return Joined array
     */
    public static Array join(Array a, Array b, int dim) {
        int[] shape = a.getShape();
        int na = shape[dim];
        shape[dim] = shape[dim] + b.getShape()[dim];
        int n = shape[dim];
        Array r = Array.factory(a.getDataType(), shape);
        IndexIterator iter = r.getIndexIterator();
        IndexIterator itera = a.getIndexIterator();
        IndexIterator iterb = b.getIndexIterator();
        int[] current;
        int i = 0;
        while (iter.hasNext()) {
            if (i > 0) {
                current = iter.getCurrentCounter();
                if (current[dim] < na - 1 || current[dim] == n - 1) {
                    iter.setObjectNext(itera.getObjectNext());
                } else {
                    iter.setObjectNext(iterb.getObjectNext());
                }
            } else {
                iter.setObjectNext(itera.getObjectNext());
            }
            i += 1;
        }

        return r;
    }

    // </editor-fold>
    // <editor-fold desc="Statistics">
    /**
     * Get minimum value
     *
     * @param a Array a
     * @return Minimum value
     */
    public static double getMinimum(Array a) {
        IndexIterator iter = a.getIndexIterator();
        double min = 1.7976931348623157E+308D;
        while (iter.hasNext()) {
            double val = iter.getDoubleNext();
            if (!Double.isNaN(val)) {
                if (val < min) {
                    min = val;
                }
            }
        }
        if (min == 1.7976931348623157E+308D) {
            return Double.NaN;
        } else {
            return min;
        }
    }

    /**
     * Get maximum value
     *
     * @param a Array a
     * @return Maximum value
     */
    public static double getMaximum(Array a) {
        IndexIterator iter = a.getIndexIterator();
        double max = -1.797693134862316E+307D;
        while (iter.hasNext()) {
            double val = iter.getDoubleNext();
            if (!Double.isNaN(val)) {
                if (val > max) {
                    max = val;
                }
            }
        }
        if (max == -1.797693134862316E+307D) {
            return Double.NaN;
        } else {
            return max;
        }
    }

    /**
     * Get minimum value
     *
     * @param a Array a
     * @param missingv Missing value
     * @return Minimum value
     */
    public static double getMinimum(Array a, double missingv) {
        IndexIterator iter = a.getIndexIterator();
        double min = 1.7976931348623157E+308D;
        while (iter.hasNext()) {
            double val = iter.getDoubleNext();
            if (!MIMath.doubleEquals(val, missingv)) {
                if (val < min) {
                    min = val;
                }
            }
        }
        return min;
    }

    /**
     * Get maximum value
     *
     * @param a Array a
     * @param missingv Missing value
     * @return Maximum value
     */
    public static double getMaximum(Array a, double missingv) {
        IndexIterator iter = a.getIndexIterator();
        double max = -1.797693134862316E+307D;
        while (iter.hasNext()) {
            double val = iter.getDoubleNext();
            if (!MIMath.doubleEquals(val, missingv)) {
                if (val > max) {
                    max = val;
                }
            }
        }
        return max;
    }

    /**
     * Compute minimum value of an array
     *
     * @param a Array a
     * @return Minimum value
     */
    public static double min(Array a) {
        double min = 1.7976931348623157E+308D;
        double v;
        IndexIterator ii = a.getIndexIterator();
        while (ii.hasNext()) {
            v = ii.getDoubleNext();
            if (!Double.isNaN(v)) {
                if (v < min) {
                    min = v;
                }
            }
        }
        if (min == 1.7976931348623157E+308D) {
            return Double.NaN;
        } else {
            return min;
        }
    }

    /**
     * Compute minimum value of an array along an axis (dimension)
     *
     * @param a Array a
     * @param axis Axis
     * @return Minimum value array
     * @throws ucar.ma2.InvalidRangeException
     */
    public static Array min(Array a, int axis) throws InvalidRangeException {
        int[] dataShape = a.getShape();
        int[] shape = new int[dataShape.length - 1];
        int idx;
        for (int i = 0; i < dataShape.length; i++) {
            idx = i;
            if (idx == axis) {
                continue;
            } else if (idx > axis) {
                idx -= 1;
            }
            shape[idx] = dataShape[i];
        }
        Array r = Array.factory(DataType.DOUBLE, shape);
        double s;
        Index indexr = r.getIndex();
        int[] current;
        for (int i = 0; i < r.getSize(); i++) {
            current = indexr.getCurrentCounter();
            List<Range> ranges = new ArrayList<>();
            for (int j = 0; j < dataShape.length; j++) {
                if (j == axis) {
                    ranges.add(new Range(0, dataShape[j] - 1, 1));
                } else {
                    idx = j;
                    if (idx > axis) {
                        idx -= 1;
                    }
                    ranges.add(new Range(current[idx], current[idx], 1));
                }
            }
            s = min(a, ranges);
            r.setDouble(i, s);
            indexr.incr();
        }

        return r;
    }

    /**
     * Compute minimum value of an array
     *
     * @param a Array a
     * @param ranges Range list
     * @return Minimum value
     * @throws ucar.ma2.InvalidRangeException
     */
    public static double min(Array a, List<Range> ranges) throws InvalidRangeException {
        double min = 1.7976931348623157E+308D;
        double v;
        IndexIterator ii = a.getRangeIterator(ranges);
        while (ii.hasNext()) {
            v = ii.getDoubleNext();
            if (!Double.isNaN(v)) {
                if (v < min) {
                    min = v;
                }
            }
        }
        if (min == 1.7976931348623157E+308D) {
            return Double.NaN;
        } else {
            return min;
        }
    }

    /**
     * Compute maximum value of an array
     *
     * @param a Array a
     * @return Maximum value
     */
    public static double max(Array a) {
        double max = -1.797693134862316E+307D;
        double v;
        IndexIterator ii = a.getIndexIterator();
        while (ii.hasNext()) {
            v = ii.getDoubleNext();
            if (!Double.isNaN(v)) {
                if (v > max) {
                    max = v;
                }
            }
        }
        if (max == -1.797693134862316E+307D) {
            return Double.NaN;
        } else {
            return max;
        }
    }

    /**
     * Compute maximum value of an array along an axis (dimension)
     *
     * @param a Array a
     * @param axis Axis
     * @return Maximum value array
     * @throws ucar.ma2.InvalidRangeException
     */
    public static Array max(Array a, int axis) throws InvalidRangeException {
        int[] dataShape = a.getShape();
        int[] shape = new int[dataShape.length - 1];
        int idx;
        for (int i = 0; i < dataShape.length; i++) {
            idx = i;
            if (idx == axis) {
                continue;
            } else if (idx > axis) {
                idx -= 1;
            }
            shape[idx] = dataShape[i];
        }
        Array r = Array.factory(DataType.DOUBLE, shape);
        double s;
        Index indexr = r.getIndex();
        int[] current;
        for (int i = 0; i < r.getSize(); i++) {
            current = indexr.getCurrentCounter();
            List<Range> ranges = new ArrayList<>();
            for (int j = 0; j < dataShape.length; j++) {
                if (j == axis) {
                    ranges.add(new Range(0, dataShape[j] - 1, 1));
                } else {
                    idx = j;
                    if (idx > axis) {
                        idx -= 1;
                    }
                    ranges.add(new Range(current[idx], current[idx], 1));
                }
            }
            s = max(a, ranges);
            r.setDouble(i, s);
            indexr.incr();
        }

        return r;
    }

    /**
     * Compute maximum value of an array
     *
     * @param a Array a
     * @param ranges Range list
     * @return Maximum value
     * @throws ucar.ma2.InvalidRangeException
     */
    public static double max(Array a, List<Range> ranges) throws InvalidRangeException {
        double max = -1.797693134862316E+307D;
        double v;
        IndexIterator ii = a.getRangeIterator(ranges);
        while (ii.hasNext()) {
            v = ii.getDoubleNext();
            if (!Double.isNaN(v)) {
                if (v > max) {
                    max = v;
                }
            }
        }
        if (max == -1.797693134862316E+307D) {
            return Double.NaN;
        } else {
            return max;
        }
    }

    /**
     * Compute sum value of an array along an axis (dimension)
     *
     * @param a Array a
     * @param axis Axis
     * @return Sum value array
     * @throws ucar.ma2.InvalidRangeException
     */
    public static Array sum(Array a, int axis) throws InvalidRangeException {
        int[] dataShape = a.getShape();
        int[] shape = new int[dataShape.length - 1];
        int idx;
        for (int i = 0; i < dataShape.length; i++) {
            idx = i;
            if (idx == axis) {
                continue;
            } else if (idx > axis) {
                idx -= 1;
            }
            shape[idx] = dataShape[i];
        }
        Array r = Array.factory(DataType.DOUBLE, shape);
        double s;
        Index indexr = r.getIndex();
        int[] current;
        for (int i = 0; i < r.getSize(); i++) {
            current = indexr.getCurrentCounter();
            List<Range> ranges = new ArrayList<>();
            for (int j = 0; j < dataShape.length; j++) {
                if (j == axis) {
                    ranges.add(new Range(0, dataShape[j] - 1, 1));
                } else {
                    idx = j;
                    if (idx > axis) {
                        idx -= 1;
                    }
                    ranges.add(new Range(current[idx], current[idx], 1));
                }
            }
            s = sum(a, ranges);
            r.setDouble(i, s);
            indexr.incr();
        }

        return r;
    }

    /**
     * Compute sum value of an array
     *
     * @param a Array a
     * @param ranges Range list
     * @return Sum value
     * @throws ucar.ma2.InvalidRangeException
     */
    public static double sum(Array a, List<Range> ranges) throws InvalidRangeException {
        double s = 0.0, v;
        int n = 0;
        IndexIterator ii = a.getRangeIterator(ranges);
        while (ii.hasNext()) {
            v = ii.getDoubleNext();
            if (!Double.isNaN(v)) {
                s += v;
                n += 1;
            }
        }
        if (n == 0) {
            s = Double.NaN;
        }
        return s;
    }

    /**
     * Compute the sum arry from a list of arrays
     *
     * @param alist list of arrays
     * @return Sum array
     */
    public static Array sum(List<Array> alist) {
        Array r = Array.factory(DataType.DOUBLE, alist.get(0).getShape());
        double sum, v;
        int n;
        for (int i = 0; i < r.getSize(); i++) {
            sum = 0.0;
            n = 0;
            for (Array a : alist) {
                v = a.getDouble(i);
                if (!Double.isNaN(v)) {
                    sum += v;
                    n += 1;
                }
            }
            if (n == 0) {
                sum = Double.NaN;
            }
            r.setDouble(i, sum);
        }

        return r;
    }

    /**
     * Summarize array
     *
     * @param a Array a
     * @return Summarize value
     */
    public static double sum(Array a) {
        double sum = 0.0D;
        double v;
        int n = 0;
        IndexIterator iterA = a.getIndexIterator();
        while (iterA.hasNext()) {
            v = iterA.getDoubleNext();
            if (!Double.isNaN(v)) {
                sum += v;
                n += 1;
            }
        }
        if (n == 0) {
            return Double.NaN;
        } else {
            return sum;
        }
    }

    /**
     * Summarize array skip missing value
     *
     * @param a Array a
     * @param missingValue Missing value
     * @return Summarize value
     */
    public static double sum(Array a, double missingValue) {
        double sum = 0.0D;
        IndexIterator iterA = a.getIndexIterator();
        while (iterA.hasNext()) {
            double val = iterA.getDoubleNext();
            if ((val != missingValue) && (!Double.isNaN(val))) {
                sum += val;
            }
        }
        return sum;
    }

    /**
     * Produce array
     *
     * @param a Array a
     * @return Produce value
     */
    public static double prodDouble(Array a) {
        double prod = 1.0D;
        double v;
        IndexIterator iterA = a.getIndexIterator();
        while (iterA.hasNext()) {
            v = iterA.getDoubleNext();
            if (!Double.isNaN(v)) {
                prod *= v;
            }
        }
        return prod;
    }

    /**
     * Average array
     *
     * @param a Array a
     * @return Average value
     */
    public static double aveDouble(Array a) {
        double sum = 0.0D;
        double v;
        int n = 0;
        IndexIterator iterA = a.getIndexIterator();
        while (iterA.hasNext()) {
            v = iterA.getDoubleNext();
            if (!Double.isNaN(v)) {
                sum += v;
                n += 1;
            }
        }
        if (n == 0) {
            return Double.NaN;
        } else {
            return sum / n;
        }
    }

    /**
     * Average array skip missing value
     *
     * @param a Array a
     * @param missingValue Missing value
     * @return Average value
     */
    public static double aveDouble(Array a, double missingValue) {
        double sum = 0.0D;
        int n = 0;
        IndexIterator iterA = a.getIndexIterator();
        while (iterA.hasNext()) {
            double val = iterA.getDoubleNext();
            if ((val != missingValue) && (!Double.isNaN(val))) {
                sum += val;
                n += 1;
            }
        }
        return sum / n;
    }

    /**
     * Get the index of the minimum value into the flattened array.
     *
     * @param a Array a
     * @return Minimum value index
     * @throws ucar.ma2.InvalidRangeException
     */
    public static int argMin(Array a) throws InvalidRangeException {
        double min = Double.MAX_VALUE, v;
        int idx = 0;
        IndexIterator iterator = a.getIndexIterator();
        int i = 0;
        while (iterator.hasNext()) {
            v = iterator.getDoubleNext();
            if (!Double.isNaN(v)) {
                if (min > v) {
                    min = v;
                    idx = i;
                }
            }
            i += 1;
        }
        return idx;
    }

    /**
     * Get the indices of the minimum values along an axis.
     *
     * @param a Array a
     * @param axis Axis
     * @return Indices
     * @throws ucar.ma2.InvalidRangeException
     */
    public static Array argMin(Array a, int axis) throws InvalidRangeException {
        int[] dataShape = a.getShape();
        int[] shape = new int[dataShape.length - 1];
        int idx;
        for (int i = 0; i < dataShape.length; i++) {
            idx = i;
            if (idx == axis) {
                continue;
            } else if (idx > axis) {
                idx -= 1;
            }
            shape[idx] = dataShape[i];
        }
        Array r = Array.factory(DataType.INT, shape);
        Index indexr = r.getIndex();
        int[] current;
        for (int i = 0; i < r.getSize(); i++) {
            current = indexr.getCurrentCounter();
            List<Range> ranges = new ArrayList<>();
            for (int j = 0; j < dataShape.length; j++) {
                if (j == axis) {
                    ranges.add(new Range(0, dataShape[j] - 1, 1));
                } else {
                    idx = j;
                    if (idx > axis) {
                        idx -= 1;
                    }
                    ranges.add(new Range(current[idx], current[idx], 1));
                }
            }
            idx = argMin(a.section(ranges));
            r.setInt(i, idx);
            indexr.incr();
        }

        return r;
    }

    /**
     * Get the index of the maximum value into the flattened array.
     *
     * @param a Array a
     * @return Maximum value index
     * @throws ucar.ma2.InvalidRangeException
     */
    public static int argMax(Array a) throws InvalidRangeException {
        double max = Double.MIN_VALUE, v;
        int idx = 0;
        IndexIterator iterator = a.getIndexIterator();
        int i = 0;
        while (iterator.hasNext()) {
            v = iterator.getDoubleNext();
            if (!Double.isNaN(v)) {
                if (max < v) {
                    max = v;
                    idx = i;
                }
            }
            i += 1;
        }
        return idx;
    }

    /**
     * Get the indices of the maximum values along an axis.
     *
     * @param a Array a
     * @param axis Axis
     * @return Indices
     * @throws ucar.ma2.InvalidRangeException
     */
    public static Array argMax(Array a, int axis) throws InvalidRangeException {
        int[] dataShape = a.getShape();
        int[] shape = new int[dataShape.length - 1];
        int idx;
        for (int i = 0; i < dataShape.length; i++) {
            idx = i;
            if (idx == axis) {
                continue;
            } else if (idx > axis) {
                idx -= 1;
            }
            shape[idx] = dataShape[i];
        }
        Array r = Array.factory(DataType.INT, shape);
        Index indexr = r.getIndex();
        int[] current;
        for (int i = 0; i < r.getSize(); i++) {
            current = indexr.getCurrentCounter();
            List<Range> ranges = new ArrayList<>();
            for (int j = 0; j < dataShape.length; j++) {
                if (j == axis) {
                    ranges.add(new Range(0, dataShape[j] - 1, 1));
                } else {
                    idx = j;
                    if (idx > axis) {
                        idx -= 1;
                    }
                    ranges.add(new Range(current[idx], current[idx], 1));
                }
            }
            idx = argMax(a.section(ranges));
            r.setInt(i, idx);
            indexr.incr();
        }

        return r;
    }

    /**
     * Compute mean value of an array along an axis (dimension)
     *
     * @param a Array a
     * @param axis Axis
     * @return Mean value array
     * @throws ucar.ma2.InvalidRangeException
     */
    public static Array mean(Array a, int axis) throws InvalidRangeException {
        int[] dataShape = a.getShape();
        int[] shape = new int[dataShape.length - 1];
        int idx;
        for (int i = 0; i < dataShape.length; i++) {
            idx = i;
            if (idx == axis) {
                continue;
            } else if (idx > axis) {
                idx -= 1;
            }
            shape[idx] = dataShape[i];
        }
        Array r = Array.factory(DataType.DOUBLE, shape);
        double mean;
        Index indexr = r.getIndex();
        int[] current;
        for (int i = 0; i < r.getSize(); i++) {
            current = indexr.getCurrentCounter();
            List<Range> ranges = new ArrayList<>();
            for (int j = 0; j < dataShape.length; j++) {
                if (j == axis) {
                    ranges.add(new Range(0, dataShape[j] - 1, 1));
                } else {
                    idx = j;
                    if (idx > axis) {
                        idx -= 1;
                    }
                    ranges.add(new Range(current[idx], current[idx], 1));
                }
            }
            mean = mean(a, ranges);
            r.setDouble(i, mean);
            indexr.incr();
        }

        return r;
    }

    /**
     * Compute mean value of an array
     *
     * @param a Array a
     * @return Mean value
     */
    public static double mean(Array a) {
        double mean = 0.0, v;
        int n = 0;
        for (int i = 0; i < a.getSize(); i++) {
            v = a.getDouble(i);
            if (!Double.isNaN(v)) {
                mean += v;
                n += 1;
            }
        }
        if (n > 0) {
            mean = mean / n;
        } else {
            mean = Double.NaN;
        }
        return mean;
    }

    /**
     * Compute the arithmetic mean arry from a list of arrays
     *
     * @param alist list of arrays
     * @return Mean array
     */
    public static Array mean(List<Array> alist) {
        Array r = Array.factory(DataType.DOUBLE, alist.get(0).getShape());
        double sum, v;
        int n;
        for (int i = 0; i < r.getSize(); i++) {
            sum = 0.0;
            n = 0;
            for (Array a : alist) {
                v = a.getDouble(i);
                if (!Double.isNaN(v)) {
                    sum += v;
                    n += 1;
                }
            }
            if (n > 0) {
                sum = sum / n;
            } else {
                sum = Double.NaN;
            }
            r.setDouble(i, sum);
        }

        return r;
    }

    /**
     * Compute mean value of an array
     *
     * @param a Array a
     * @param ranges Range list
     * @return Mean value
     * @throws ucar.ma2.InvalidRangeException
     */
    public static double mean(Array a, List<Range> ranges) throws InvalidRangeException {
        double mean = 0.0, v;
        int n = 0;
        IndexIterator ii = a.getRangeIterator(ranges);
        while (ii.hasNext()) {
            v = ii.getDoubleNext();
            if (!Double.isNaN(v)) {
                mean += v;
                n += 1;
            }
        }
        if (n > 0) {
            mean = mean / n;
        } else {
            mean = Double.NaN;
        }
        return mean;
    }

    /**
     * Compute standard deviation value of an array along an axis (dimension)
     *
     * @param a Array a
     * @param axis Axis
     * @return Standard deviation value array
     * @throws ucar.ma2.InvalidRangeException
     */
    public static Array std(Array a, int axis) throws InvalidRangeException {
        int[] dataShape = a.getShape();
        int[] shape = new int[dataShape.length - 1];
        int idx;
        for (int i = 0; i < dataShape.length; i++) {
            idx = i;
            if (idx == axis) {
                continue;
            } else if (idx > axis) {
                idx -= 1;
            }
            shape[idx] = dataShape[i];
        }
        Array r = Array.factory(DataType.DOUBLE, shape);
        double mean;
        Index indexr = r.getIndex();
        int[] current;
        for (int i = 0; i < r.getSize(); i++) {
            current = indexr.getCurrentCounter();
            List<Range> ranges = new ArrayList<>();
            for (int j = 0; j < dataShape.length; j++) {
                if (j == axis) {
                    ranges.add(new Range(0, dataShape[j] - 1, 1));
                } else {
                    idx = j;
                    if (idx > axis) {
                        idx -= 1;
                    }
                    ranges.add(new Range(current[idx], current[idx], 1));
                }
            }
            mean = std(a, ranges);
            r.setDouble(i, mean);
            indexr.incr();
        }

        return r;
    }

    /**
     * Compute standard deviation value of an array
     *
     * @param a Array a
     * @param ranges Range list
     * @return Standard deviation value
     * @throws ucar.ma2.InvalidRangeException
     */
    public static double std(Array a, List<Range> ranges) throws InvalidRangeException {
        double mean = 0.0, v;
        int n = 0;
        IndexIterator ii = a.getRangeIterator(ranges);
        while (ii.hasNext()) {
            v = ii.getDoubleNext();
            if (!Double.isNaN(v)) {
                mean += v;
                n += 1;
            }
        }
        if (n > 0) {
            mean = mean / n;
        } else {
            mean = Double.NaN;
        }

        if (Double.isNaN(mean)) {
            return Double.NaN;
        }

        double sum = 0;
        ii = a.getRangeIterator(ranges);
        while (ii.hasNext()) {
            v = ii.getDoubleNext();
            if (!Double.isNaN(v)) {
                sum += Math.pow((v - mean), 2);
            }
        }
        sum = Math.sqrt(sum / n);

        return sum;
    }

    /**
     * Compute standard deviation value of an array
     *
     * @param a Array a
     * @return Standard deviation value
     * @throws ucar.ma2.InvalidRangeException
     */
    public static double std(Array a) throws InvalidRangeException {
        double mean = 0.0, v;
        int n = 0;
        IndexIterator ii = a.getIndexIterator();
        while (ii.hasNext()) {
            v = ii.getDoubleNext();
            if (!Double.isNaN(v)) {
                mean += v;
                n += 1;
            }
        }
        if (n > 0) {
            mean = mean / n;
        } else {
            mean = Double.NaN;
            return mean;
        }

        double sum = 0;
        ii = a.getIndexIterator();
        while (ii.hasNext()) {
            v = ii.getDoubleNext();
            if (!Double.isNaN(v)) {
                sum += Math.pow((v - mean), 2);
            }
        }
        sum = Math.sqrt(sum / n);

        return sum;
    }

    /**
     * Compute variance value of an array along an axis (dimension)
     *
     * @param a Array a
     * @param axis Axis
     * @return Variance value array
     * @throws ucar.ma2.InvalidRangeException
     */
    public static Array var(Array a, int axis) throws InvalidRangeException {
        int[] dataShape = a.getShape();
        int[] shape = new int[dataShape.length - 1];
        int idx;
        for (int i = 0; i < dataShape.length; i++) {
            idx = i;
            if (idx == axis) {
                continue;
            } else if (idx > axis) {
                idx -= 1;
            }
            shape[idx] = dataShape[i];
        }
        Array r = Array.factory(DataType.DOUBLE, shape);
        double mean;
        Index indexr = r.getIndex();
        int[] current;
        for (int i = 0; i < r.getSize(); i++) {
            current = indexr.getCurrentCounter();
            List<Range> ranges = new ArrayList<>();
            for (int j = 0; j < dataShape.length; j++) {
                if (j == axis) {
                    ranges.add(new Range(0, dataShape[j] - 1, 1));
                } else {
                    idx = j;
                    if (idx > axis) {
                        idx -= 1;
                    }
                    ranges.add(new Range(current[idx], current[idx], 1));
                }
            }
            mean = std(a, ranges);
            r.setDouble(i, mean);
            indexr.incr();
        }

        return r;
    }

    /**
     * Compute variance value of an array
     *
     * @param a Array a
     * @param ranges Range list
     * @return Variance value
     * @throws ucar.ma2.InvalidRangeException
     */
    public static double var(Array a, List<Range> ranges) throws InvalidRangeException {
        double mean = 0.0, v;
        int n = 0;
        IndexIterator ii = a.getRangeIterator(ranges);
        while (ii.hasNext()) {
            v = ii.getDoubleNext();
            if (!Double.isNaN(v)) {
                mean += v;
                n += 1;
            }
        }
        if (n > 0) {
            mean = mean / n;
        } else {
            mean = Double.NaN;
        }

        if (Double.isNaN(mean)) {
            return Double.NaN;
        }

        double sum = 0;
        ii = a.getRangeIterator(ranges);
        while (ii.hasNext()) {
            v = ii.getDoubleNext();
            if (!Double.isNaN(v)) {
                sum += Math.pow((v - mean), 2);
            }
        }
        sum = sum / n;

        return sum;
    }

    /**
     * Compute median value of an array along an axis (dimension)
     *
     * @param a Array a
     * @param axis Axis
     * @return Median value array
     * @throws ucar.ma2.InvalidRangeException
     */
    public static Array median(Array a, int axis) throws InvalidRangeException {
        int[] dataShape = a.getShape();
        int[] shape = new int[dataShape.length - 1];
        int idx;
        for (int i = 0; i < dataShape.length; i++) {
            idx = i;
            if (idx == axis) {
                continue;
            } else if (idx > axis) {
                idx -= 1;
            }
            shape[idx] = dataShape[i];
        }
        Array r = Array.factory(DataType.DOUBLE, shape);
        double mean;
        Index indexr = r.getIndex();
        int[] current;
        for (int i = 0; i < r.getSize(); i++) {
            current = indexr.getCurrentCounter();
            List<Range> ranges = new ArrayList<>();
            for (int j = 0; j < dataShape.length; j++) {
                if (j == axis) {
                    ranges.add(new Range(0, dataShape[j] - 1, 1));
                } else {
                    idx = j;
                    if (idx > axis) {
                        idx -= 1;
                    }
                    ranges.add(new Range(current[idx], current[idx], 1));
                }
            }
            mean = median(a, ranges);
            r.setDouble(i, mean);
            indexr.incr();
        }

        return r;
    }

    /**
     * Compute median value of an array
     *
     * @param a Array a
     * @param ranges Range list
     * @return Median value
     * @throws ucar.ma2.InvalidRangeException
     */
    public static double median(Array a, List<Range> ranges) throws InvalidRangeException {
        Array b = a.section(ranges);
        double median = Statistics.quantile(b, 2);
        return median;
    }

    /**
     * Compute median value of an array
     *
     * @param a Array a
     * @return Median value
     */
    public static double median(Array a) {
        return Statistics.quantile(a, 2);
    }

    /**
     * Element-wise maximum of array elements.
     *
     * @param x1 Array 1
     * @param x2 Array 2
     * @return The maximum of x1 and x2, element-wise.
     */
    public static Array maximum(Array x1, Array x2) {
        DataType dt = commonType(x1.getDataType(), x2.getDataType());
        Array r = Array.factory(dt, x1.getShape());
        for (int i = 0; i < r.getSize(); i++) {
            r.setObject(i, Math.max(x1.getDouble(i), x2.getDouble(i)));
        }

        return r;
    }

    /**
     * Element-wise maximum of array elements, ignores NaNs.
     *
     * @param x1 Array 1
     * @param x2 Array 2
     * @return The maximum of x1 and x2, element-wise.
     */
    public static Array fmax(Array x1, Array x2) {
        DataType dt = commonType(x1.getDataType(), x2.getDataType());
        Array r = Array.factory(dt, x1.getShape());
        for (int i = 0; i < r.getSize(); i++) {
            if (Double.isNaN(x1.getDouble(i))) {
                r.setObject(i, x2.getDouble(i));
            } else if (Double.isNaN(x2.getDouble(i))) {
                r.setObject(i, x1.getDouble(i));
            } else {
                r.setObject(i, Math.max(x1.getDouble(i), x2.getDouble(i)));
            }
        }

        return r;
    }

    /**
     * Element-wise minimum of array elements.
     *
     * @param x1 Array 1
     * @param x2 Array 2
     * @return The minimum of x1 and x2, element-wise.
     */
    public static Array minimum(Array x1, Array x2) {
        DataType dt = commonType(x1.getDataType(), x2.getDataType());
        Array r = Array.factory(dt, x1.getShape());
        for (int i = 0; i < r.getSize(); i++) {
            r.setObject(i, Math.min(x1.getDouble(i), x2.getDouble(i)));
        }

        return r;
    }

    /**
     * Element-wise minimum of array elements, ignores NaNs.
     *
     * @param x1 Array 1
     * @param x2 Array 2
     * @return The minimum of x1 and x2, element-wise.
     */
    public static Array fmin(Array x1, Array x2) {
        DataType dt = commonType(x1.getDataType(), x2.getDataType());
        Array r = Array.factory(dt, x1.getShape());
        for (int i = 0; i < r.getSize(); i++) {
            if (Double.isNaN(x1.getDouble(i))) {
                r.setObject(i, x2.getDouble(i));
            } else if (Double.isNaN(x2.getDouble(i))) {
                r.setObject(i, x1.getDouble(i));
            } else {
                r.setObject(i, Math.min(x1.getDouble(i), x2.getDouble(i)));
            }
        }

        return r;
    }

    /**
     * Moving average function
     *
     * @param x The data array
     * @param window Size of moving window
     * @param center Set the data in center moving window
     * @return Moving averaged array
     */
    public static Array rolling_mean(Array x, int window, boolean center) {
        int n = (int) x.getSize();
        Array r = Array.factory(DataType.DOUBLE, new int[]{n});
        double v, vv;
        int dn;
        if (center) {
            int halfn = (window - 1) / 2;
            int idx;
            for (int i = 0; i < n; i++) {
                v = 0;
                dn = 0;
                for (int j = 0; j < window; j++) {
                    if (j < halfn) {
                        idx = i - j;
                    } else if (j == halfn) {
                        idx = i;
                    } else {
                        idx = i + (j - halfn);
                    }
                    if (idx < 0 || idx >= n) {
                        break;
                    }
                    vv = x.getDouble(idx);
                    if (!Double.isNaN(vv)) {
                        v += vv;
                        dn += 1;
                    }
                }
                if (dn > 0) {
                    v = v / dn;
                } else {
                    v = Double.NaN;
                }
                r.setDouble(i, v);
            }
        } else {
            for (int i = 0; i < n; i++) {
                v = 0;
                dn = 0;
                for (int j = 0; j < window; j++) {
                    if (i - j < 0) {
                        break;
                    }
                    vv = x.getDouble(i - j);
                    if (!Double.isNaN(vv)) {
                        v += vv;
                        dn += 1;
                    }
                }
                if (dn > 0) {
                    v = v / dn;
                } else {
                    v = Double.NaN;
                }
                r.setDouble(i, v);
            }
        }

        return r;
    }

    // </editor-fold>
    // <editor-fold desc="Convert">
    /**
     * Set missing value to NaN
     *
     * @param a Array a
     * @param missingv Missing value
     */
    public static void missingToNaN(Array a, Number missingv) {
        if (!a.getDataType().isNumeric()) {
            return;
        }

        IndexIterator iterA = a.getIndexIterator();
        switch (a.getDataType()) {
            case INT:
            case FLOAT:
                while (iterA.hasNext()) {
                    float val = iterA.getFloatNext();
                    if (val == missingv.floatValue()) {
                        iterA.setFloatCurrent(Float.NaN);
                    }
                }
            default:
                while (iterA.hasNext()) {
                    double val = iterA.getDoubleNext();
                    if (MIMath.doubleEquals(val, missingv.doubleValue())) {
                        iterA.setDoubleCurrent(Double.NaN);
                    }
                }
        }
    }

    /**
     * Set value
     *
     * @param a Array a
     * @param b Array b - 0/1 data
     * @param value Value
     */
    public static void setValue(Array a, Array b, Number value) {
        if (b.getDataType() == DataType.BOOLEAN) {
            for (int i = 0; i < a.getSize(); i++) {
                if (b.getBoolean(i)) {
                    a.setObject(i, value);
                }
            }
        } else {
            for (int i = 0; i < a.getSize(); i++) {
                if (b.getInt(i) == 1) {
                    a.setObject(i, value);
                }
            }
        }
    }

    /**
     * Set value
     *
     * @param a Array a
     * @param b Array b - 0/1 data
     * @param value Value array
     */
    public static void setValue(Array a, Array b, Array value) {
        if (b.getDataType() == DataType.BOOLEAN) {
            for (int i = 0; i < a.getSize(); i++) {
                if (b.getBoolean(i)) {
                    a.setObject(i, value.getObject(i));
                }
            }
        } else {
            for (int i = 0; i < a.getSize(); i++) {
                if (b.getInt(i) == 1) {
                    a.setObject(i, value.getObject(i));
                }
            }
        }
    }

    /**
     * As number list
     *
     * @param a Array a
     * @return Result number list
     */
    public static List<Object> asList(Array a) {
        IndexIterator iterA = a.getIndexIterator();
        List<Object> r = new ArrayList<>();
        switch (a.getDataType()) {
            case SHORT:
            case INT:
                while (iterA.hasNext()) {
                    r.add(iterA.getIntNext());
                }
                break;
            case FLOAT:
                while (iterA.hasNext()) {
                    r.add(iterA.getFloatNext());
                }
                break;
            case DOUBLE:
                while (iterA.hasNext()) {
                    r.add(iterA.getDoubleNext());
                }
                break;
            case BOOLEAN:
                while (iterA.hasNext()) {
                    r.add(iterA.getBooleanNext());
                }
                break;
            case OBJECT:
                while (iterA.hasNext()) {
                    r.add(iterA.getObjectNext());
                }
                break;
        }
        return r;
    }

    /**
     * Get wind direction and wind speed from U/V
     *
     * @param u U component
     * @param v V component
     * @return Wind direction and wind speed
     */
    public static Array[] uv2ds(Array u, Array v) {
        Array windSpeed = ArrayMath.sqrt(ArrayMath.add(ArrayMath.mul(u, u), ArrayMath.mul(v, v)));
        Array windDir = Array.factory(windSpeed.getDataType(), windSpeed.getShape());
        double ws, wd, U, V;
        for (int i = 0; i < windSpeed.getSize(); i++) {
            U = u.getDouble(i);
            V = v.getDouble(i);
            if (Double.isNaN(U) || Double.isNaN(V)) {
                windDir.setDouble(i, Double.NaN);
                continue;
            }
            ws = windSpeed.getDouble(i);
            if (ws == 0) {
                wd = 0;
            } else {
                wd = Math.asin(U / ws) * 180 / Math.PI;
                if (U <= 0 && V < 0) {
                    wd = 180.0 - wd;
                } else if (U > 0 && V < 0) {
                    wd = 180.0 - wd;
                } else if (U < 0 && V > 0) {
                    wd = 360.0 + wd;
                }
                wd += 180;
                if (wd >= 360) {
                    wd -= 360;
                }
            }
            windDir.setDouble(i, wd);
        }

        return new Array[]{windDir, windSpeed};
    }

    /**
     * Get wind direction and wind speed from U/V
     *
     * @param u U component
     * @param v V component
     * @return Wind direction and wind speed
     */
    public static double[] uv2ds(double u, double v) {
        double ws = Math.sqrt(u * u + v * v);
        double wd;
        if (ws == 0) {
            wd = 0;
        } else {
            wd = Math.asin(u / ws) * 180 / Math.PI;
            if (u <= 0 && v < 0) {
                wd = 180.0 - wd;
            } else if (u > 0 && v < 0) {
                wd = 180.0 - wd;
            } else if (u < 0 && v > 0) {
                wd = 360.0 + wd;
            }
            wd += 180;
            if (wd >= 360) {
                wd -= 360;
            }
        }

        return new double[]{wd, ws};
    }

    /**
     * Get wind U/V components from wind direction and speed
     *
     * @param windDir Wind direction
     * @param windSpeed Wind speed
     * @return Wind U/V components
     */
    public static Array[] ds2uv(Array windDir, Array windSpeed) {
        Array U = Array.factory(DataType.DOUBLE, windDir.getShape());
        Array V = Array.factory(DataType.DOUBLE, windDir.getShape());
        double dir;
        for (int i = 0; i < U.getSize(); i++) {
            if (Double.isNaN(windDir.getDouble(i)) || Double.isNaN(windSpeed.getDouble(i))) {
                U.setDouble(i, Double.NaN);
                V.setDouble(i, Double.NaN);
            }
            dir = windDir.getDouble(i) + 180;
            if (dir > 360) {
                dir = dir - 360;
            }
            dir = dir * Math.PI / 180;
            U.setDouble(i, windSpeed.getDouble(i) * Math.sin(dir));
            V.setDouble(i, windSpeed.getDouble(i) * Math.cos(dir));
        }

        return new Array[]{U, V};
    }

    /**
     * Get wind U/V components from wind direction and speed
     *
     * @param windDir Wind direction
     * @param windSpeed Wind speed
     * @return Wind U/V components
     */
    public static double[] ds2uv(double windDir, double windSpeed) {
        double dir;
        dir = windDir + 180;
        if (dir > 360) {
            dir = dir - 360;
        }
        dir = dir * Math.PI / 180;
        double u = windSpeed * Math.sin(dir);
        double v = windSpeed * Math.cos(dir);

        return new double[]{u, v};
    }

    // </editor-fold>       
    // <editor-fold desc="Location">
    /**
     * In polygon function
     *
     * @param a Array a
     * @param x X dimension values
     * @param y Y dimension values
     * @param layer Polygon vector layer
     * @return Result array with cell values of 1 inside polygons and -1 outside
     * polygons
     */
    public static Array inPolygon(Array a, List<Number> x, List<Number> y, VectorLayer layer) {
        List<PolygonShape> polygons = (List<PolygonShape>) layer.getShapes();
        return ArrayMath.inPolygon(a, x, y, polygons);
    }

    /**
     * In polygon function
     *
     * @param a Array a
     * @param x X dimension values
     * @param y Y dimension values
     * @param ps Polygon shape
     * @return Result array with cell values of 1 inside polygons and -1 outside
     * polygons
     */
    public static Array inPolygon(Array a, List<Number> x, List<Number> y, PolygonShape ps) {
        List<PolygonShape> polygons = new ArrayList<>();
        polygons.add(ps);
        return ArrayMath.inPolygon(a, x, y, polygons);
    }

    /**
     * In polygon function
     *
     * @param a Array a
     * @param x X dimension values
     * @param y Y dimension values
     * @param polygons PolygonShape list
     * @return Result array with cell values of 1 inside polygons and -1 outside
     * polygons
     */
    public static Array inPolygon(Array a, List<Number> x, List<Number> y, List<PolygonShape> polygons) {
        if (a.getRank() == 2) {
            int xNum = x.size();
            int yNum = y.size();

            Array r = Array.factory(DataType.INT, a.getShape());
            for (int i = 0; i < yNum; i++) {
                for (int j = 0; j < xNum; j++) {
                    if (GeoComputation.pointInPolygons(polygons, new PointD(x.get(j).doubleValue(), y.get(i).doubleValue()))) {
                        r.setInt(i * xNum + j, 1);
                    } else {
                        r.setInt(i * xNum + j, -1);
                    }
                }
            }

            return r;
        } else if (a.getRank() == 1) {
            int n = x.size();
            Array r = Array.factory(DataType.INT, a.getShape());
            for (int i = 0; i < n; i++) {
                if (GeoComputation.pointInPolygons(polygons, new PointD(x.get(i).doubleValue(), y.get(i).doubleValue()))) {
                    r.setInt(i, 1);
                } else {
                    r.setInt(i, -1);
                }
            }

            return r;
        }

        return null;
    }

    /**
     * In polygon function
     *
     * @param x X coordinates
     * @param y Y coordinates
     * @param polygons PolygonShape list
     * @return Result boolean array
     */
    public static Array inPolygon(Array x, Array y, List<PolygonShape> polygons) {
        Array r = Array.factory(DataType.BOOLEAN, x.getShape());
        for (int i = 0; i < r.getSize(); i++) {
            if (GeoComputation.pointInPolygons(polygons, new PointD(x.getDouble(i), y.getDouble(i)))) {
                r.setBoolean(i, true);
            } else {
                r.setBoolean(i, false);
            }
        }

        return r;
    }

    /**
     * In polygon function
     *
     * @param a Array a
     * @param x X dimension values
     * @param y Y dimension values
     * @param x_p X coordinate of the polygon
     * @param y_p Y coordinate of the polygon
     * @return Result array with cell values of 1 inside polygons and -1 outside
     * polygons
     */
    public static Array inPolygon(Array a, List<Number> x, List<Number> y, List<Number> x_p, List<Number> y_p) {
        PolygonShape ps = new PolygonShape();
        List<PointD> points = new ArrayList<>();
        for (int i = 0; i < x_p.size(); i++) {
            points.add(new PointD(x_p.get(i).doubleValue(), y_p.get(i).doubleValue()));
        }
        ps.setPoints(points);
        List<PolygonShape> shapes = new ArrayList<>();
        shapes.add(ps);

        return inPolygon(a, x, y, shapes);
    }

    /**
     * In polygon function
     *
     * @param x X coordinates
     * @param y Y coordinates
     * @param x_p X coordinate of the polygon
     * @param y_p Y coordinate of the polygon
     * @return Result boolean array
     */
    public static Array inPolygon(Array x, Array y, Array x_p, Array y_p) {
        PolygonShape ps = new PolygonShape();
        List<PointD> points = new ArrayList<>();
        for (int i = 0; i < x_p.getSize(); i++) {
            points.add(new PointD(x_p.getDouble(i), y_p.getDouble(i)));
        }
        ps.setPoints(points);
        List<PolygonShape> shapes = new ArrayList<>();
        shapes.add(ps);

        return inPolygon(x, y, shapes);
    }

    /**
     * Maskout function
     *
     * @param a Array a
     * @param x X dimension values
     * @param y Y dimension values
     * @param layer VectorLayer
     * @param missingValue Missing value
     * @return Result array with cell values of missing outside polygons
     */
    public static Array maskout(Array a, List<Number> x, List<Number> y, VectorLayer layer, Number missingValue) {
        List<PolygonShape> polygons = (List<PolygonShape>) layer.getShapes();
        return ArrayMath.maskout(a, x, y, polygons, missingValue);
    }

    /**
     * Maskout function
     *
     * @param a Array a
     * @param x X dimension values
     * @param y Y dimension values
     * @param polygon Polygon shape
     * @param missingValue Missing value
     * @return Result array with cell values of missing outside polygons
     */
    public static Array maskout(Array a, List<Number> x, List<Number> y, PolygonShape polygon, Number missingValue) {
        List<PolygonShape> polygons = new ArrayList<>();
        polygons.add(polygon);
        return ArrayMath.maskout(a, x, y, polygons, missingValue);
    }

    /**
     * Maskout function
     *
     * @param a Array a
     * @param x X Array
     * @param y Y Array
     * @param polygons Polygons for maskout
     * @return Result array with cell values of missing outside polygons
     */
    public static Array maskout(Array a, Array x, Array y, List<PolygonShape> polygons) {
        Array r = Array.factory(a.getDataType(), a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (GeoComputation.pointInPolygons(polygons, new PointD(x.getDouble(i), y.getDouble(i)))) {
                r.setObject(i, a.getObject(i));
            } else {
                r.setObject(i, Double.NaN);
            }
        }
        return r;
    }

    /**
     * Maskin function
     *
     * @param a Array a
     * @param x X Array
     * @param y Y Array
     * @param polygons Polygons for maskin
     * @return Result array with cell values of missing inside polygons
     */
    public static Array maskin(Array a, Array x, Array y, List<PolygonShape> polygons) {
        Array r = Array.factory(a.getDataType(), a.getShape());
        for (int i = 0; i < a.getSize(); i++) {
            if (GeoComputation.pointInPolygons(polygons, new PointD(x.getDouble(i), y.getDouble(i)))) {
                r.setObject(i, Double.NaN);
            } else {
                r.setObject(i, a.getObject(i));
            }
        }
        return r;
    }

    /**
     * Maskout function
     *
     * @param a Array a
     * @param x X Array
     * @param y Y Array
     * @param polygons Polygons for maskout
     * @return Result arrays removing cells outside polygons
     */
    public static Array[] maskout_Remove(Array a, Array x, Array y, List<PolygonShape> polygons) {
        List<Object> rdata = new ArrayList<>();
        List<Double> rxdata = new ArrayList<>();
        List<Double> rydata = new ArrayList<>();
        for (int i = 0; i < a.getSize(); i++) {
            if (GeoComputation.pointInPolygons(polygons, new PointD(x.getDouble(i), y.getDouble(i)))) {
                rdata.add(a.getObject(i));
                rxdata.add(x.getDouble(i));
                rydata.add(y.getDouble(i));
            }
        }

        int n = rdata.size();
        int[] shape = new int[1];
        shape[0] = n;
        Array r = Array.factory(a.getDataType(), shape);
        Array rx = Array.factory(x.getDataType(), shape);
        Array ry = Array.factory(y.getDataType(), shape);
        for (int i = 0; i < n; i++) {
            r.setObject(i, rdata.get(i));
            rx.setDouble(i, rxdata.get(i));
            ry.setDouble(i, rydata.get(i));
        }

        return new Array[]{r, rx, ry};
    }

    /**
     * Maskin function
     *
     * @param a Array a
     * @param x X Array
     * @param y Y Array
     * @param polygons Polygons for maskin
     * @return Result arrays removing cells inside polygons
     */
    public static Array[] maskin_Remove(Array a, Array x, Array y, List<PolygonShape> polygons) {
        List<Object> rdata = new ArrayList<>();
        List<Double> rxdata = new ArrayList<>();
        List<Double> rydata = new ArrayList<>();
        for (int i = 0; i < a.getSize(); i++) {
            if (!GeoComputation.pointInPolygons(polygons, new PointD(x.getDouble(i), y.getDouble(i)))) {
                rdata.add(a.getObject(i));
                rxdata.add(x.getDouble(i));
                rydata.add(y.getDouble(i));
            }
        }

        int n = rdata.size();
        int[] shape = new int[1];
        shape[0] = n;
        Array r = Array.factory(a.getDataType(), shape);
        Array rx = Array.factory(x.getDataType(), shape);
        Array ry = Array.factory(y.getDataType(), shape);
        for (int i = 0; i < n; i++) {
            r.setObject(i, rdata.get(i));
            rx.setDouble(i, rxdata.get(i));
            ry.setDouble(i, rydata.get(i));
        }

        return new Array[]{r, rx, ry};
    }

    /**
     * Maskout function
     *
     * @param a Array a
     * @param x X dimension values
     * @param y Y dimension values
     * @param polygons PolygonShape list
     * @return Result array with cell values of missing outside polygons
     */
    public static Array maskout(Array a, List<Number> x, List<Number> y, List<PolygonShape> polygons) {
        return maskout(a, x, y, polygons, Double.NaN);
    }

    /**
     * Maskout function
     *
     * @param a Array a
     * @param x X dimension values
     * @param y Y dimension values
     * @param polygons PolygonShape list
     * @param missingValue Missing value
     * @return Result array with cell values of missing outside polygons
     */
    public static Array maskout(Array a, List<Number> x, List<Number> y, List<PolygonShape> polygons, Number missingValue) {
        int xNum = x.size();
        int yNum = y.size();

        Array r = Array.factory(a.getDataType(), a.getShape());
        if (a.getRank() == 1) {
            for (int i = 0; i < xNum; i++) {
                if (GeoComputation.pointInPolygons(polygons, new PointD(x.get(i).doubleValue(), y.get(i).doubleValue()))) {
                    r.setObject(i, a.getObject(i));
                } else {
                    r.setObject(i, missingValue);
                }
            }
        } else if (a.getRank() == 2) {
            int idx;
            for (int i = 0; i < yNum; i++) {
                for (int j = 0; j < xNum; j++) {
                    idx = i * xNum + j;
                    if (GeoComputation.pointInPolygons(polygons, new PointD(x.get(j).doubleValue(), y.get(i).doubleValue()))) {
                        r.setObject(idx, a.getObject(idx));
                    } else {
                        r.setObject(idx, missingValue);
                    }
                }
            }
        }

        return r;
    }

    /**
     * Maskout function
     *
     * @param a Array a
     * @param m Array mask
     * @param missingValue Missing value
     * @return Result array
     */
    public static Array maskout(Array a, Array m, Number missingValue) {
        Array r = Array.factory(a.getDataType(), a.getShape());
        int n = (int) a.getSize();
        for (int i = 0; i < n; i++) {
            if (m.getDouble(i) < 0) {
                r.setObject(i, missingValue);
            } else {
                r.setObject(i, a.getObject(i));
            }
        }

        return r;
    }

    /**
     * Maskout function
     *
     * @param a Array a
     * @param m Array mask
     * @return Result array
     */
    public static Array maskout(Array a, Array m) {
        Array r = Array.factory(a.getDataType(), a.getShape());
        int n = (int) a.getSize();
        for (int i = 0; i < n; i++) {
            if (m.getDouble(i) < 0) {
                r.setObject(i, Double.NaN);
            } else {
                r.setObject(i, a.getObject(i));
            }
        }

        return r;
    }

    /**
     * Maskin function
     *
     * @param a Array a
     * @param m Array mask
     * @return Result array
     */
    public static Array maskin(Array a, Array m) {
        Array r = Array.factory(a.getDataType(), a.getShape());
        int n = (int) a.getSize();
        for (int i = 0; i < n; i++) {
            if (m.getDouble(i) < 0) {
                r.setObject(i, a.getObject(i));
            } else {
                r.setObject(i, Double.NaN);
            }
        }

        return r;
    }

    // </editor-fold>
    // <editor-fold desc="Regress">
    /**
     * Get correlation coefficient How well did the forecast values correspond
     * to the observed values? Range: -1 to 1. Perfect score: 1.
     *
     * @param xData X data array
     * @param yData Y data array
     * @return Correlation coefficent
     */
    public static float getR(List<Number> xData, List<Number> yData) {
        int n = xData.size();
        double x_sum = 0;
        double y_sum = 0;
        for (int i = 0; i < n; i++) {
            x_sum += xData.get(i).doubleValue();
            y_sum += yData.get(i).doubleValue();
        }
        double sx_sum = 0.0;
        double sy_sum = 0.0;
        double xy_sum = 0.0;
        for (int i = 0; i < n; i++) {
            sx_sum += xData.get(i).doubleValue() * xData.get(i).doubleValue();
            sy_sum += yData.get(i).doubleValue() * yData.get(i).doubleValue();
            xy_sum += xData.get(i).doubleValue() * yData.get(i).doubleValue();
        }

        double r = (n * xy_sum - x_sum * y_sum) / (Math.sqrt(n * sx_sum - x_sum * x_sum) * Math.sqrt(n * sy_sum - y_sum * y_sum));
        return (float) r;
    }

    /**
     * Get correlation coefficient How well did the forecast values correspond
     * to the observed values? Range: -1 to 1. Perfect score: 1.
     *
     * @param xData X data array
     * @param yData Y data array
     * @return Correlation coefficent
     */
    public static float getR(Array xData, Array yData) {
        int n = (int) xData.getSize();
        double x_sum = 0;
        double y_sum = 0;
        double sx_sum = 0.0;
        double sy_sum = 0.0;
        double xy_sum = 0.0;
        int nn = 0;
        double x, y;
        for (int i = 0; i < n; i++) {
            x = xData.getDouble(i);
            y = yData.getDouble(i);
            if (Double.isNaN(x) || Double.isNaN(y)) {
                continue;
            }
            x_sum += x;
            y_sum += y;
            sx_sum += x * x;
            sy_sum += y * y;
            xy_sum += x * y;
            nn += 1;
        }

        double r = (nn * xy_sum - x_sum * y_sum) / (Math.sqrt(nn * sx_sum - x_sum * x_sum) * Math.sqrt(nn * sy_sum - y_sum * y_sum));
        return (float) r;
    }

    /**
     * Determine the least square trend equation - linear fitting
     *
     * @param xData X data array
     * @param yData Y data array
     * @return Result array - y intercept and slope
     */
    public static double[] leastSquareTrend(List<Number> xData, List<Number> yData) {
        int n = xData.size();
        double sumX = 0.0;
        double sumY = 0.0;
        double sumSquareX = 0.0;
        double sumXY = 0.0;
        for (int i = 0; i < n; i++) {
            sumX += xData.get(i).doubleValue();
            sumY += yData.get(i).doubleValue();
            sumSquareX += xData.get(i).doubleValue() * xData.get(i).doubleValue();
            sumXY += xData.get(i).doubleValue() * yData.get(i).doubleValue();
        }

        double a = (sumSquareX * sumY - sumX * sumXY) / (n * sumSquareX - sumX * sumX);
        double b = (n * sumXY - sumX * sumY) / (n * sumSquareX - sumX * sumX);

        return new double[]{a, b};
    }

    /**
     * Linear regress
     *
     * @param xData X data array
     * @param yData Y data array
     * @return Result array - y intercept, slope and correlation coefficent
     */
    public static double[] lineRegress(List<Number> xData, List<Number> yData) {
        int n = xData.size();
        double x_sum = 0;
        double y_sum = 0;
        double sx_sum = 0.0;
        double sy_sum = 0.0;
        double xy_sum = 0.0;
        for (int i = 0; i < n; i++) {
            x_sum += xData.get(i).doubleValue();
            y_sum += yData.get(i).doubleValue();
            sx_sum += xData.get(i).doubleValue() * xData.get(i).doubleValue();
            sy_sum += yData.get(i).doubleValue() * yData.get(i).doubleValue();
            xy_sum += xData.get(i).doubleValue() * yData.get(i).doubleValue();
        }

        double r = (n * xy_sum - x_sum * y_sum) / (Math.sqrt(n * sx_sum - x_sum * x_sum) * Math.sqrt(n * sy_sum - y_sum * y_sum));
        double a = (sx_sum * y_sum - x_sum * xy_sum) / (n * sx_sum - x_sum * x_sum);
        double b = (n * xy_sum - x_sum * y_sum) / (n * sx_sum - x_sum * x_sum);

        return new double[]{a, b, r};
    }

    /**
     * Linear regress
     *
     * @param xData X data array
     * @param yData Y data array
     * @return Slope, intercept, correlation coefficent, two-sided p-value, the
     * standard error of the estimate for the slope, valid data number
     */
    public static double[] lineRegress(Array xData, Array yData) {
        double x_sum = 0;
        double y_sum = 0;
        double sx_sum = 0.0;
        double sy_sum = 0.0;
        double xy_sum = 0.0;
        int n = 0;
        List<Double> xi = new ArrayList<>();
        List<Double> yi = new ArrayList<>();
        for (int i = 0; i < xData.getSize(); i++) {
            if (Double.isNaN(xData.getDouble(i))) {
                continue;
            }
            if (Double.isNaN(yData.getDouble(i))) {
                continue;
            }
            xi.add(xData.getDouble(i));
            yi.add(yData.getDouble(i));
            x_sum += xData.getDouble(i);
            y_sum += yData.getDouble(i);
            sx_sum += xData.getDouble(i) * xData.getDouble(i);
            sy_sum += yData.getDouble(i) * yData.getDouble(i);
            xy_sum += xData.getDouble(i) * yData.getDouble(i);
            n += 1;
        }

        double r = (n * xy_sum - x_sum * y_sum) / (Math.sqrt(n * sx_sum - x_sum * x_sum) * Math.sqrt(n * sy_sum - y_sum * y_sum));
        double intercept = (sx_sum * y_sum - x_sum * xy_sum) / (n * sx_sum - x_sum * x_sum);
        double slope = (n * xy_sum - x_sum * y_sum) / (n * sx_sum - x_sum * x_sum);
        int df = n - 2;    //degress of freedom
        double TINY = 1.0e-20;
        double t = r * Math.sqrt(df / ((1.0 - r + TINY) * (1.0 + r + TINY)));
        //two-sided p-value for a hypothesis test whose null hypothesis is that the slope is zero
        double p = studpval(t, df);

        // more statistical analysis
        double xbar = x_sum / n;
        double ybar = y_sum / n;
        double rss = 0.0;      // residual sum of squares
        double ssr = 0.0;      // regression sum of squares
        double fit;
        double xxbar = 0.0;
        for (int i = 0; i < n; i++) {
            fit = slope * xi.get(i) + intercept;
            rss += (fit - yi.get(i)) * (fit - yi.get(i));
            ssr += (fit - ybar) * (fit - ybar);
            xxbar += (xi.get(i) - xbar) * (xi.get(i) - xbar);
        }
        double svar = rss / df;
        double svar1 = svar / xxbar;
        double svar0 = svar / n + xbar * xbar * svar1;
        svar0 = Math.sqrt(svar0);    //the standard error of the estimate for the intercept
        svar1 = Math.sqrt(svar1);    //the standard error of the estimate for the slope

//        double xbar = x_sum / n;
//        double ybar = y_sum / n;
//        double bhat = 0.0;
//        double ssqx = 0.0;
//        for (int i = 0; i < n; i++) {
//            bhat = bhat + (yi.get(i) - ybar) * (xi.get(i) - xbar);
//            ssqx = ssqx + (xi.get(i) - xbar) * (xi.get(i) - xbar);
//        }
//        bhat = bhat / ssqx;
//        double ahat = ybar - bhat * xbar;
//        double sigmahat2 = 0.0;
//        double[] ri = new double[n];
//        for (int i = 0; i < n; i++) {
//            ri[i] = yi.get(i) - (ahat + bhat * xi.get(i));
//            sigmahat2 = sigmahat2 + ri[i] * ri[i];
//        }
//        sigmahat2 = sigmahat2 / (n * 1.0 - 2.0);
//        double seb = Math.sqrt(sigmahat2 / ssqx);
//        double sigmahat = Math.sqrt((seb * seb) * ssqx);
//        double sea = Math.sqrt(sigmahat * sigmahat * (1 / (n * 1.0) + xbar * xbar / ssqx));
//        double b0 = 0;
//        double Tb = (bhat - b0) / seb;
//        double a0 = 0;
//        double Ta = (ahat - a0) / sea;        
//        p = studpval(Ta, n);
        return new double[]{slope, intercept, r, p, svar1, n};
    }

    private static double statcom(double mq, int mi, int mj, double mb) {
        double zz = 1;
        double mz = zz;
        int mk = mi;
        while (mk <= mj) {
            zz = zz * mq * mk / (mk - mb);
            mz = mz + zz;
            mk = mk + 2;
        }
        return mz;
    }

    private static double studpval(double mt, int mn) {
        mt = Math.abs(mt);
        double mw = mt / Math.sqrt(mn);
        double th = Math.atan2(mw, 1);
        if (mn == 1) {
            return 1.0 - th / (Math.PI / 2.0);
        }
        double sth = Math.sin(th);
        double cth = Math.cos(th);
        if (mn % 2 == 1) {
            return 1.0 - (th + sth * cth * statcom(cth * cth, 2, mn - 3, -1)) / (Math.PI / 2.0);
        } else {
            return 1.0 - sth * statcom(cth * cth, 1, mn - 3, -1);
        }
    }

    /**
     * Evaluate a polynomial at specific values. If p is of length N, this
     * function returns the value: p[0]*x**(N-1) + p[1]*x**(N-2) + ... +
     * p[N-2]*x + p[N-1]
     *
     * @param p array_like or poly1d object
     * @param x array_like or poly1d object
     * @return ndarray or poly1d
     */
    public static Array polyVal(List<Number> p, Array x) {
        int n = p.size();
        Array r = Array.factory(DataType.DOUBLE, x.getShape());
        for (int i = 0; i < x.getSize(); i++) {
            double val = x.getDouble(i);
            double rval = 0.0;
            for (int j = 0; j < n; j++) {
                rval += p.get(j).doubleValue() * Math.pow(val, n - j - 1);
            }
            r.setDouble(i, rval);
        }

        return r;
    }

    // </editor-fold>    
    // <editor-fold desc="Meteo">
    /**
     * Performs a centered difference operation on a grid data along one
     * dimension direction
     *
     * @param data The grid data
     * @param dimIdx Direction dimension index
     * @return Result grid data
     */
    public static Array cdiff(Array data, int dimIdx) {
        Array r = Array.factory(DataType.DOUBLE, data.getShape());
        Index index = data.getIndex();
        Index indexr = r.getIndex();
        int[] shape = data.getShape();
        int[] current, cc;
        double a, b;
        for (int i = 0; i < r.getSize(); i++) {
            current = indexr.getCurrentCounter();
            if (current[dimIdx] == 0 || current[dimIdx] == shape[dimIdx] - 1) {
                r.setDouble(indexr, Double.NaN);
            } else {
                cc = Arrays.copyOf(current, current.length);
                cc[dimIdx] = cc[dimIdx] - 1;
                index.set(cc);
                a = data.getDouble(index);
                cc[dimIdx] = cc[dimIdx] + 2;
                index.set(cc);
                b = data.getDouble(index);
                if (Double.isNaN(a) || Double.isNaN(b)) {
                    r.setDouble(indexr, Double.NaN);
                } else {
                    r.setDouble(indexr, a - b);
                }
            }
            indexr.incr();
        }

        return r;
    }

    /**
     * Performs a centered difference operation on a grid data in the x or y
     * direction
     *
     * @param data The grid data
     * @param isX If is x direction
     * @return Result grid data
     */
    public static Array cdiff_bak(Array data, boolean isX) {
        if (data.getRank() == 2) {
            int xnum = data.getShape()[1];
            int ynum = data.getShape()[0];
            Array r = Array.factory(DataType.DOUBLE, data.getShape());
            for (int i = 0; i < ynum; i++) {
                for (int j = 0; j < xnum; j++) {
                    if (i == 0 || i == ynum - 1 || j == 0 || j == xnum - 1) {
                        r.setDouble(i * xnum + j, Double.NaN);
                    } else {
                        double a, b;
                        if (isX) {
                            a = data.getDouble(i * xnum + j + 1);
                            b = data.getDouble(i * xnum + j - 1);
                        } else {
                            a = data.getDouble((i + 1) * xnum + j);
                            b = data.getDouble((i - 1) * xnum + j);
                        }
                        if (Double.isNaN(a) || Double.isNaN(b)) {
                            r.setDouble(i * xnum + j, Double.NaN);
                        } else {
                            r.setDouble(i * xnum + j, a - b);
                        }
                    }
                }
            }

            return r;
        } else if (data.getRank() == 1) {
            int n = data.getShape()[0];
            Array r = Array.factory(DataType.DOUBLE, data.getShape());
            for (int i = 0; i < n; i++) {
                if (i == 0 || i == n - 1) {
                    r.setDouble(i, Double.NaN);
                } else {
                    double a, b;
                    a = data.getDouble(i + 1);
                    b = data.getDouble(i - 1);
                    if (Double.isNaN(a) || Double.isNaN(b)) {
                        r.setDouble(i, Double.NaN);
                    } else {
                        r.setDouble(i, a - b);
                    }
                }
            }

            return r;
        } else {
            System.out.println("Data dimension number must be 1 or 2!");
            return null;
        }
    }

    /**
     * Calculates the vertical component of the curl (ie, vorticity)
     *
     * @param uData U component
     * @param vData V component
     * @param xx X dimension value
     * @param yy Y dimension value
     * @return Curl
     */
    public static Array hcurl(Array uData, Array vData, List<Number> xx, List<Number> yy) {
        int rank = uData.getRank();
        int[] shape = uData.getShape();
        Array lonData = Array.factory(DataType.DOUBLE, shape);
        Array latData = Array.factory(DataType.DOUBLE, shape);
        Index index = lonData.getIndex();
        int[] current;
        for (int i = 0; i < lonData.getSize(); i++) {
            current = index.getCurrentCounter();
            lonData.setDouble(index, xx.get(current[rank - 1]).doubleValue());
            latData.setDouble(index, yy.get(current[rank - 2]).doubleValue());
            index.incr();
        }

        Array dv = cdiff(vData, rank - 1);
        Array dx = mul(cdiff(lonData, rank - 1), Math.PI / 180);
        Array du = cdiff(mul(uData, cos(mul(latData, Math.PI / 180))), rank - 2);
        Array dy = mul(cdiff(latData, rank - 2), Math.PI / 180);
        Array gData = div(sub(div(dv, dx), div(du, dy)), mul(cos(mul(latData, Math.PI / 180)), 6.37e6));

        return gData;
    }

    /**
     * Calculates the horizontal divergence using finite differencing
     *
     * @param uData U component
     * @param vData V component
     * @param xx X dimension value
     * @param yy Y dimension value
     * @return Divergence
     */
    public static Array hdivg(Array uData, Array vData, List<Number> xx, List<Number> yy) {
        int rank = uData.getRank();
        int[] shape = uData.getShape();
        Array lonData = Array.factory(DataType.DOUBLE, shape);
        Array latData = Array.factory(DataType.DOUBLE, shape);
        Index index = lonData.getIndex();
        int[] current;
        for (int i = 0; i < lonData.getSize(); i++) {
            current = index.getCurrentCounter();
            lonData.setDouble(index, xx.get(current[rank - 1]).doubleValue());
            latData.setDouble(index, yy.get(current[rank - 2]).doubleValue());
            index.incr();
        }

        Array du = cdiff(uData, rank - 1);
        Array dx = mul(cdiff(lonData, rank - 1), Math.PI / 180);
        Array dv = cdiff(mul(vData, cos(mul(latData, Math.PI / 180))), rank - 2);
        Array dy = mul(cdiff(latData, rank - 2), Math.PI / 180);
        Array gData = div(add(div(du, dx), div(dv, dy)), mul(cos(mul(latData, Math.PI / 180)), 6.37e6));

        return gData;
    }

    /**
     * Take magnitude value from U/V grid data
     *
     * @param uData U grid data
     * @param vData V grid data
     * @return Magnitude grid data
     */
    public static Array magnitude(Array uData, Array vData) {
        int[] shape = uData.getShape();
        int xNum = shape[1];
        int yNum = shape[0];
        int idx;

        Array r = Array.factory(DataType.DOUBLE, shape);
        for (int i = 0; i < yNum; i++) {
            for (int j = 0; j < xNum; j++) {
                idx = i * xNum + j;
                if (Double.isNaN(uData.getDouble(idx)) || Double.isNaN(vData.getDouble(idx))) {
                    r.setDouble(idx, Double.NaN);
                } else {
                    r.setDouble(idx, Math.sqrt(Math.pow(uData.getDouble(idx), 2) + Math.pow(vData.getDouble(idx), 2)));
                }
            }
        }

        return r;
    }

    /**
     * Calculate fahrenheit temperature from celsius temperature
     *
     * @param tc Celsius temperature
     * @return Fahrenheit temperature
     */
    public static Array tc2tf(Array tc) {
        Array r = Array.factory(tc.getDataType(), tc.getShape());
        for (int i = 0; i < r.getSize(); i++) {
            r.setDouble(i, MeteoMath.tc2tf(tc.getDouble(i)));
        }

        return r;
    }

    /**
     * Calculate celsius temperature from fahrenheit temperature
     *
     * @param tf Fahrenheit temperature
     * @return Celsius temperature
     */
    public static Array tf2tc(Array tf) {
        Array r = Array.factory(tf.getDataType(), tf.getShape());
        for (int i = 0; i < r.getSize(); i++) {
            r.setDouble(i, MeteoMath.tf2tc(tf.getDouble(i)));
        }

        return r;
    }

    /**
     * Calculate relative humidity from specific humidity
     *
     * @param qair Specific humidity, dimensionless (e.g. kg/kg) ratio of water
     * mass / total air mass
     * @param temp Temperature - degree c
     * @param press Pressure - hPa (mb)
     * @return Relative humidity as percent (i.e. 80%)
     */
    public static Array qair2rh(Array qair, Array temp, double press) {
        Array r = Array.factory(DataType.DOUBLE, qair.getShape());
        double rh;
        for (int i = 0; i < r.getSize(); i++) {
            rh = MeteoMath.qair2rh(qair.getDouble(i), temp.getDouble(i), press);
            r.setDouble(i, rh);
        }

        return r;
    }

    /**
     * Calculate relative humidity
     *
     * @param qair Specific humidity, dimensionless (e.g. kg/kg) ratio of water
     * mass / total air mass
     * @param temp Temperature - degree c
     * @param press Pressure - hPa (mb)
     * @return Relative humidity as percent (i.e. 80%)
     */
    public static Array qair2rh(Array qair, Array temp, Array press) {
        Array r = Array.factory(DataType.DOUBLE, qair.getShape());
        double rh;
        for (int i = 0; i < r.getSize(); i++) {
            rh = MeteoMath.qair2rh(qair.getDouble(i), temp.getDouble(i), press.getDouble(i));
            r.setDouble(i, rh);
        }

        return r;
    }

    /**
     * Calculate height from pressure
     *
     * @param press Pressure - hPa
     * @return Height - m
     */
    public static Array press2Height(Array press) {
        Array r = Array.factory(DataType.DOUBLE, press.getShape());
        double rh;
        for (int i = 0; i < r.getSize(); i++) {
            rh = MeteoMath.press2Height(press.getDouble(i));
            r.setDouble(i, rh);
        }

        return r;
    }

    /**
     * Calculate pressure from height
     *
     * @param height Height - m
     * @return Pressure - hPa
     */
    public static Array height2Press(Array height) {
        Array r = Array.factory(DataType.DOUBLE, height.getShape());
        double rh;
        for (int i = 0; i < r.getSize(); i++) {
            rh = MeteoMath.height2Press(height.getDouble(i));
            r.setDouble(i, rh);
        }

        return r;
    }
    // </editor-fold>
}
