/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.meteoinfo.ndarray.Complex;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.Index;
import org.meteoinfo.ndarray.IndexIterator;
import org.meteoinfo.ndarray.InvalidRangeException;
import org.meteoinfo.ndarray.MAMath;
import org.meteoinfo.ndarray.Range;

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

        if (aType == DataType.COMPLEX || bType == DataType.COMPLEX) {
            return DataType.COMPLEX;
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
        return a.getDataType() == DataType.COMPLEX;
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
            case COMPLEX:
                if (a.getDataType() == DataType.COMPLEX) {
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

    private static Array addInt(Array a, Array b) {
        int broadcast = broadcastCheck(a, b);
        switch (broadcast) {
            case 0:
                Array r = Array.factory(DataType.INT, a.getShape());
                if (a.getIndexPrivate().isFastIterator() && b.getIndexPrivate().isFastIterator()) {
                    int va,
                     vb;
                    for (int i = 0; i < r.getSize(); i++) {
                        va  = a.getInt(i);
                        vb = b.getInt(i);
                        if (va  == Integer.MIN_VALUE || vb == Integer.MIN_VALUE) {
                            r.setInt(i, Integer.MIN_VALUE);
                        } else {
                            r.setInt(i, va  + vb);
                        }
                    }
                } else {
                    IndexIterator iterR = r.getIndexIterator();
                    IndexIterator iterA = a.getIndexIterator();
                    IndexIterator iterB = b.getIndexIterator();
                    int va,
                     vb;
                    while (iterA.hasNext()) {
                        va  = iterA.getIntNext();
                        vb = iterB.getIntNext();
                        if (va  == Integer.MIN_VALUE || vb == Integer.MIN_VALUE) {
                            iterR.setIntNext(Integer.MIN_VALUE);
                        } else {
                            iterR.setIntNext(va  + vb);
                        }
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
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < r.getSize(); i++) {
                r.setInt(i, a.getInt(i) + b);
            }
        } else {
            IndexIterator iterR = r.getIndexIterator();
            IndexIterator iterA = a.getIndexIterator();
            int v;
            while (iterA.hasNext()) {
                v = iterA.getIntNext();
                if (v == Integer.MIN_VALUE) {
                    iterR.setIntNext(Integer.MIN_VALUE);
                } else {
                    iterR.setIntNext(v + b);
                }
            }
        }

        return r;
    }

    private static Array addFloat(Array a, Array b) {
        int broadcast = broadcastCheck(a, b);
        switch (broadcast) {
            case 0:
                Array r = Array.factory(DataType.FLOAT, a.getShape());
                if (a.getIndexPrivate().isFastIterator() && b.getIndexPrivate().isFastIterator()) {
                    float va,
                     vb;
                    for (int i = 0; i < r.getSize(); i++) {
                        va  = a.getFloat(i);
                        vb = b.getFloat(i);
                        if (Float.isNaN(va) || Float.isNaN(vb)) {
                            r.setFloat(i, Float.NaN);
                        } else {
                            r.setFloat(i, va  + vb);
                        }
                    }
                } else {
                    IndexIterator iterR = r.getIndexIterator();
                    IndexIterator iterA = a.getIndexIterator();
                    IndexIterator iterB = b.getIndexIterator();
                    float va,
                     vb;
                    while (iterA.hasNext()) {
                        va  = iterA.getFloatNext();
                        vb = iterB.getFloatNext();
                        if (Float.isNaN(va) || Float.isNaN(vb)) {
                            iterR.setFloatNext(Float.NaN);
                        } else {
                            iterR.setFloatNext(va  + vb);
                        }
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
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < r.getSize(); i++) {
                r.setFloat(i, a.getFloat(i) + b);
            }
        } else {
            IndexIterator iterR = r.getIndexIterator();
            IndexIterator iterA = a.getIndexIterator();
            float v;
            while (iterA.hasNext()) {
                v = iterA.getFloatNext();
                if (Float.isNaN(v)) {
                    iterR.setFloatNext(v);
                } else {
                    iterR.setFloatNext(v + b);
                }
            }
        }

        return r;
    }

    private static Array addDouble(Array a, Array b) {
        int broadcast = broadcastCheck(a, b);
        switch (broadcast) {
            case 0:
                Array r = Array.factory(DataType.DOUBLE, a.getShape());
                if (a.getIndexPrivate().isFastIterator() && b.getIndexPrivate().isFastIterator()) {
                    double va,
                     vb;
                    for (int i = 0; i < r.getSize(); i++) {
                        va  = a.getDouble(i);
                        vb = b.getDouble(i);
                        if (Double.isNaN(va) || Double.isNaN(vb)) {
                            r.setDouble(i, Double.NaN);
                        } else {
                            r.setDouble(i, va  + vb);
                        }
                    }
                } else {
                    IndexIterator iterR = r.getIndexIterator();
                    IndexIterator iterA = a.getIndexIterator();
                    IndexIterator iterB = b.getIndexIterator();
                    double va,
                     vb;
                    while (iterA.hasNext()) {
                        va  = iterA.getDoubleNext();
                        vb = iterB.getDoubleNext();
                        if (Double.isNaN(va) || Double.isNaN(vb)) {
                            iterR.setDoubleNext(Double.NaN);
                        } else {
                            iterR.setDoubleNext(va  + vb);
                        }
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
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < r.getSize(); i++) {
                r.setDouble(i, a.getDouble(i) + b);
            }
        } else {
            IndexIterator iterR = r.getIndexIterator();
            IndexIterator iterA = a.getIndexIterator();
            double v;
            while (iterA.hasNext()) {
                v = iterA.getDoubleNext();
                if (Double.isNaN(v)) {
                    iterR.setDoubleNext(Double.NaN);
                } else {
                    iterR.setDoubleNext(v + b);
                }
            }
        }

        return r;
    }

    private static Array addComplex(Array a, Array b) {
        int broadcast = broadcastCheck(a, b);
        switch (broadcast) {
            case 0:
                Array r = Array.factory(DataType.COMPLEX, a.getShape());
                if (a.getIndexPrivate().isFastIterator() && b.getIndexPrivate().isFastIterator()) {
                    for (int i = 0; i < r.getSize(); i++) {
                        r.setComplex(i, a.getComplex(i).add(b.getComplex(i)));
                    }
                } else {
                    IndexIterator iterR = r.getIndexIterator();
                    IndexIterator iterA = a.getIndexIterator();
                    IndexIterator iterB = b.getIndexIterator();
                    while (iterA.hasNext()) {
                        iterR.setComplexNext(iterA.getComplexNext().add(iterB.getComplexNext()));
                    }
                }
                return r;
            case 1:
                int[] shape = broadcast(a, b);
                r = Array.factory(DataType.COMPLEX, shape);
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
                    r.setComplex(i, a.getComplex(aindex).add(b.getComplex(bindex)));
                    index.incr();
                }
                return r;
            default:
                return null;
        }
    }

    private static Array addComplex(Array a, double b) {
        Array r = Array.factory(DataType.OBJECT, a.getShape());
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < r.getSize(); i++) {
                r.setComplex(i, a.getComplex(i).add(b));
            }
        } else {
            IndexIterator iterR = r.getIndexIterator();
            IndexIterator iterA = a.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setComplexNext(iterA.getComplexNext().add(b));
            }
        }

        return r;
    }

    private static Array addComplex(Array a, Complex b) {
        Array r = Array.factory(DataType.COMPLEX, a.getShape());
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < r.getSize(); i++) {
                r.setComplex(i, a.getComplex(i).add(b));
            }
        } else {
            IndexIterator iterR = r.getIndexIterator();
            IndexIterator iterA = a.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setComplexNext(iterA.getComplexNext().add(b));
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

    private static Array subInt(Array a, Array b) {
        int broadcast = broadcastCheck(a, b);
        switch (broadcast) {
            case 0:
                Array r = Array.factory(DataType.INT, a.getShape());
                if (a.getIndexPrivate().isFastIterator() && b.getIndexPrivate().isFastIterator()) {
                    for (int i = 0; i < a.getSize(); i++) {
                        r.setInt(i, a.getInt(i) - b.getInt(i));
                    }
                } else {
                    IndexIterator iterA = a.getIndexIterator();
                    IndexIterator iterB = b.getIndexIterator();
                    IndexIterator iterR = r.getIndexIterator();
                    while (iterA.hasNext()) {
                        iterR.setIntNext(iterA.getIntNext() - iterB.getIntNext());
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
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < a.getSize(); i++) {
                r.setInt(i, a.getInt(i) - b);
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setIntNext(iterA.getIntNext() - b);
            }
        }

        return r;
    }

    private static Array subInt(int b, Array a) {
        Array r = Array.factory(DataType.INT, a.getShape());
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < a.getSize(); i++) {
                r.setInt(i, b - a.getInt(i));
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setIntNext(b - iterA.getIntNext());
            }
        }

        return r;
    }

    private static Array subFloat(Array a, Array b) {
        int broadcast = broadcastCheck(a, b);
        switch (broadcast) {
            case 0:
                Array r = Array.factory(DataType.FLOAT, a.getShape());
                if (a.getIndexPrivate().isFastIterator() && b.getIndexPrivate().isFastIterator()) {
                    for (int i = 0; i < a.getSize(); i++) {
                        if (Float.isNaN(a.getFloat(i)) || Float.isNaN(b.getFloat(i))) {
                            r.setFloat(i, Float.NaN);
                        } else {
                            r.setFloat(i, a.getFloat(i) - b.getFloat(i));
                        }
                    }
                } else {
                    IndexIterator iterA = a.getIndexIterator();
                    IndexIterator iterB = b.getIndexIterator();
                    IndexIterator iterR = r.getIndexIterator();
                    while (iterA.hasNext()) {
                        iterR.setFloatNext(iterA.getFloatNext() - iterB.getFloatNext());
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
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < a.getSize(); i++) {
                r.setFloat(i, a.getFloat(i) - b);
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setFloatNext(iterA.getFloatNext() - b);
            }
        }

        return r;
    }

    private static Array subFloat(float b, Array a) {
        Array r = Array.factory(DataType.FLOAT, a.getShape());
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < a.getSize(); i++) {
                r.setFloat(i, b - a.getFloat(i));
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setFloatNext(b - iterA.getFloatNext());
            }
        }

        return r;
    }

    private static Array subDouble(Array a, Array b) {
        int broadcast = broadcastCheck(a, b);
        switch (broadcast) {
            case 0:
                Array r = Array.factory(DataType.DOUBLE, a.getShape());
                if (a.getIndexPrivate().isFastIterator() && b.getIndexPrivate().isFastIterator()) {
                    for (int i = 0; i < a.getSize(); i++) {
                        if (Double.isNaN(a.getDouble(i)) || Double.isNaN(b.getDouble(i))) {
                            r.setDouble(i, Double.NaN);
                        } else {
                            r.setDouble(i, a.getDouble(i) - b.getDouble(i));
                        }
                    }
                } else {
                    IndexIterator iterA = a.getIndexIterator();
                    IndexIterator iterB = b.getIndexIterator();
                    IndexIterator iterR = r.getIndexIterator();
                    while (iterA.hasNext()) {
                        iterR.setDoubleNext(iterA.getDoubleNext() - iterB.getDoubleNext());
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
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < a.getSize(); i++) {
                r.setDouble(i, a.getDouble(i) - b);
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setDoubleNext(iterA.getDoubleNext() - b);
            }
        }

        return r;
    }

    private static Array subDouble(double b, Array a) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < a.getSize(); i++) {
                r.setDouble(i, b - a.getDouble(i));
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setDoubleNext(b - iterA.getDoubleNext());
            }
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
            case COMPLEX:
                return ArrayMath.mulComplex(a, b);
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
            case COMPLEX:
                return ArrayMath.mulComplex(a, b.doubleValue());
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

    private static Array mulInt(Array a, Array b) {
        int broadcast = broadcastCheck(a, b);
        switch (broadcast) {
            case 0:
                Array r = Array.factory(DataType.INT, a.getShape());
                if (a.getIndexPrivate().isFastIterator() && b.getIndexPrivate().isFastIterator()) {
                    int va,
                     vb;
                    for (int i = 0; i < r.getSize(); i++) {
                        va  = a.getInt(i);
                        vb = b.getInt(i);
                        if (va  == Integer.MIN_VALUE || vb == Integer.MIN_VALUE) {
                            r.setInt(i, Integer.MIN_VALUE);
                        } else {
                            r.setInt(i, va  * vb);
                        }
                    }
                } else {
                    IndexIterator iterR = r.getIndexIterator();
                    IndexIterator iterA = a.getIndexIterator();
                    IndexIterator iterB = b.getIndexIterator();
                    int va,
                     vb;
                    while (iterA.hasNext()) {
                        va  = iterA.getIntNext();
                        vb = iterB.getIntNext();
                        if (va  == Integer.MIN_VALUE || vb == Integer.MIN_VALUE) {
                            iterR.setIntNext(Integer.MIN_VALUE);
                        } else {
                            iterR.setIntNext(va  * vb);
                        }
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
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < r.getSize(); i++) {
                r.setInt(i, a.getInt(i) * b);
            }
        } else {
            IndexIterator iterR = r.getIndexIterator();
            IndexIterator iterA = a.getIndexIterator();
            int v;
            while (iterA.hasNext()) {
                v = iterA.getIntNext();
                if (v == Integer.MIN_VALUE) {
                    iterR.setIntNext(Integer.MIN_VALUE);
                } else {
                    iterR.setIntNext(v * b);
                }
            }
        }

        return r;
    }

    private static Array mulFloat(Array a, Array b) {
        int broadcast = broadcastCheck(a, b);
        switch (broadcast) {
            case 0:
                Array r = Array.factory(DataType.FLOAT, a.getShape());
                if (a.getIndexPrivate().isFastIterator() && b.getIndexPrivate().isFastIterator()) {
                    float va,
                     vb;
                    for (int i = 0; i < a.getSize(); i++) {
                        va  = a.getFloat(i);
                        vb = b.getFloat(i);
                        if (Float.isNaN(va) || Float.isNaN(vb)) {
                            r.setFloat(i, Float.NaN);
                        } else {
                            r.setFloat(i, a.getFloat(i) * b.getFloat(i));
                        }
                    }
                } else {
                    IndexIterator iterR = r.getIndexIterator();
                    IndexIterator iterA = a.getIndexIterator();
                    IndexIterator iterB = b.getIndexIterator();
                    float va,
                     vb;
                    while (iterA.hasNext()) {
                        va  = iterA.getFloatNext();
                        vb = iterB.getFloatNext();
                        if (Float.isNaN(va) || Float.isNaN(vb)) {
                            iterR.setFloatNext(Float.NaN);
                        } else {
                            iterR.setFloatNext(va  * vb);
                        }
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
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < r.getSize(); i++) {
                r.setFloat(i, a.getFloat(i) * b);
            }
        } else {
            IndexIterator iterR = r.getIndexIterator();
            IndexIterator iterA = a.getIndexIterator();
            float v;
            while (iterA.hasNext()) {
                v = iterA.getFloatNext();
                if (Float.isNaN(v)) {
                    iterR.setFloatNext(Float.NaN);
                } else {
                    iterR.setFloatNext(v * b);
                }
            }
        }

        return r;
    }

    private static Array mulDouble(Array a, Array b) {
        int broadcast = broadcastCheck(a, b);
        switch (broadcast) {
            case 0:
                Array r = Array.factory(DataType.DOUBLE, a.getShape());
                if (a.getIndexPrivate().isFastIterator() && b.getIndexPrivate().isFastIterator()) {
                    double va,
                     vb;
                    for (int i = 0; i < a.getSize(); i++) {
                        va  = a.getDouble(i);
                        vb = b.getDouble(i);
                        if (Double.isNaN(va) || Double.isNaN(vb)) {
                            r.setDouble(i, Double.NaN);
                        } else {
                            r.setDouble(i, a.getDouble(i) * b.getDouble(i));
                        }
                    }
                } else {
                    IndexIterator iterR = r.getIndexIterator();
                    IndexIterator iterA = a.getIndexIterator();
                    IndexIterator iterB = b.getIndexIterator();
                    double va,
                     vb;
                    while (iterA.hasNext()) {
                        va  = iterA.getDoubleNext();
                        vb = iterB.getDoubleNext();
                        if (Double.isNaN(va) || Double.isNaN(vb)) {
                            iterR.setDoubleNext(Double.NaN);
                        } else {
                            iterR.setDoubleNext(va  * vb);
                        }
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
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < r.getSize(); i++) {
                r.setDouble(i, a.getDouble(i) * b);
            }
        } else {
            IndexIterator iterR = r.getIndexIterator();
            IndexIterator iterA = a.getIndexIterator();
            double v;
            while (iterA.hasNext()) {
                v = iterA.getDoubleNext();
                if (Double.isNaN(v)) {
                    iterR.setDoubleNext(Double.NaN);
                } else {
                    iterR.setDoubleNext(v * b);
                }
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
            case COMPLEX:
                return ArrayMath.divComplex(a, b);
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
            case COMPLEX:
                return ArrayMath.divComplex(a, b.doubleValue());
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
            case COMPLEX:
                return ArrayMath.divComplex(b.doubleValue(), a);
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

    private static Array divInt(Array a, Array b) {
        int broadcast = broadcastCheck(a, b);
        switch (broadcast) {
            case 0:
                Array r = Array.factory(DataType.INT, a.getShape());
                if (a.getIndexPrivate().isFastIterator() && b.getIndexPrivate().isFastIterator()) {
                    int va,
                     vb;
                    for (int i = 0; i < r.getSize(); i++) {
                        va  = a.getInt(i);
                        vb = b.getInt(i);
                        if (va  == Integer.MIN_VALUE || vb == Integer.MIN_VALUE) {
                            r.setInt(i, Integer.MIN_VALUE);
                        } else {
                            r.setInt(i, va  / vb);
                        }
                    }
                } else {
                    IndexIterator iterR = r.getIndexIterator();
                    IndexIterator iterA = a.getIndexIterator();
                    IndexIterator iterB = b.getIndexIterator();
                    int va,
                     vb;
                    while (iterA.hasNext()) {
                        va  = iterA.getIntNext();
                        vb = iterB.getIntNext();
                        if (va  == Integer.MIN_VALUE || vb == Integer.MIN_VALUE) {
                            iterR.setIntNext(Integer.MIN_VALUE);
                        } else {
                            iterR.setIntNext(va  / vb);
                        }
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
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < r.getSize(); i++) {
                r.setInt(i, a.getInt(i) / b);
            }
        } else {
            IndexIterator iterR = r.getIndexIterator();
            IndexIterator iterA = a.getIndexIterator();
            int v;
            while (iterA.hasNext()) {
                v = iterA.getIntNext();
                if (v == Integer.MIN_VALUE) {
                    iterR.setIntNext(Integer.MIN_VALUE);
                } else {
                    iterR.setIntNext(v / b);
                }
            }
        }

        return r;
    }

    private static Array divInt(int b, Array a) {
        Array r = Array.factory(DataType.INT, a.getShape());
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < r.getSize(); i++) {
                r.setInt(i, b / a.getInt(i));
            }
        } else {
            IndexIterator iterR = r.getIndexIterator();
            IndexIterator iterA = a.getIndexIterator();
            int v;
            while (iterA.hasNext()) {
                v = iterA.getIntNext();
                if (v == Integer.MIN_VALUE) {
                    iterR.setIntNext(Integer.MIN_VALUE);
                } else {
                    iterR.setIntNext(b / v);
                }
            }
        }

        return r;
    }

    private static Array divFloat(Array a, Array b) {
        int broadcast = broadcastCheck(a, b);
        switch (broadcast) {
            case 0:
                Array r = Array.factory(DataType.FLOAT, a.getShape());
                if (a.getIndexPrivate().isFastIterator() && b.getIndexPrivate().isFastIterator()) {
                    float va,
                     vb;
                    for (int i = 0; i < r.getSize(); i++) {
                        va  = a.getFloat(i);
                        vb = b.getFloat(i);
                        if (Float.isNaN(va) || Float.isNaN(vb)) {
                            r.setFloat(i, Float.NaN);
                        } else {
                            r.setFloat(i, va  / vb);
                        }
                    }
                } else {
                    IndexIterator iterR = r.getIndexIterator();
                    IndexIterator iterA = a.getIndexIterator();
                    IndexIterator iterB = b.getIndexIterator();
                    float va,
                     vb;
                    while (iterA.hasNext()) {
                        va  = iterA.getFloatNext();
                        vb = iterB.getFloatNext();
                        if (Float.isNaN(va) || Float.isNaN(vb)) {
                            iterR.setFloatNext(Float.NaN);
                        } else {
                            iterR.setFloatNext(va  / vb);
                        }
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
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < r.getSize(); i++) {
                r.setFloat(i, a.getFloat(i) / b);
            }
        } else {
            IndexIterator iterR = r.getIndexIterator();
            IndexIterator iterA = a.getIndexIterator();
            float v;
            while (iterA.hasNext()) {
                v = iterA.getFloatNext();
                if (Float.isNaN(v)) {
                    iterR.setFloatNext(v);
                } else {
                    iterR.setFloatNext(v / b);
                }
            }
        }

        return r;
    }

    private static Array divFloat(float b, Array a) {
        Array r = Array.factory(DataType.FLOAT, a.getShape());
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < r.getSize(); i++) {
                r.setFloat(i, b / a.getFloat(i));
            }
        } else {
            IndexIterator iterR = r.getIndexIterator();
            IndexIterator iterA = a.getIndexIterator();
            float v;
            while (iterA.hasNext()) {
                v = iterA.getFloatNext();
                if (Float.isNaN(v)) {
                    iterR.setFloatNext(v);
                } else {
                    iterR.setFloatNext(b / v);
                }
            }
        }

        return r;
    }

    private static Array divDouble(Array a, Array b) {
        int broadcast = broadcastCheck(a, b);
        switch (broadcast) {
            case 0:
                Array r = Array.factory(DataType.DOUBLE, a.getShape());
                if (a.getIndexPrivate().isFastIterator() && b.getIndexPrivate().isFastIterator()) {
                    double va,
                     vb;
                    for (int i = 0; i < r.getSize(); i++) {
                        va  = a.getDouble(i);
                        vb = b.getDouble(i);
                        if (Double.isNaN(va) || Double.isNaN(vb)) {
                            r.setDouble(i, Double.NaN);
                        } else {
                            r.setDouble(i, va  / vb);
                        }
                    }
                } else {
                    IndexIterator iterR = r.getIndexIterator();
                    IndexIterator iterA = a.getIndexIterator();
                    IndexIterator iterB = b.getIndexIterator();
                    double va,
                     vb;
                    while (iterA.hasNext()) {
                        va  = iterA.getDoubleNext();
                        vb = iterB.getDoubleNext();
                        if (Double.isNaN(va) || Double.isNaN(vb)) {
                            iterR.setDoubleNext(Double.NaN);
                        } else {
                            iterR.setDoubleNext(va  / vb);
                        }
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
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < r.getSize(); i++) {
                r.setDouble(i, a.getDouble(i) / b);
            }
        } else {
            IndexIterator iterR = r.getIndexIterator();
            IndexIterator iterA = a.getIndexIterator();
            double v;
            while (iterA.hasNext()) {
                v = iterA.getDoubleNext();
                if (Double.isNaN(v)) {
                    iterR.setDoubleNext(Double.NaN);
                } else {
                    iterR.setDoubleNext(v / b);
                }
            }
        }

        return r;
    }

    private static Array divDouble(double b, Array a) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < r.getSize(); i++) {
                r.setDouble(i, b / a.getDouble(i));
            }
        } else {
            IndexIterator iterR = r.getIndexIterator();
            IndexIterator iterA = a.getIndexIterator();
            double v;
            while (iterA.hasNext()) {
                v = iterA.getDoubleNext();
                if (Double.isNaN(v)) {
                    iterR.setDoubleNext(Double.NaN);
                } else {
                    iterR.setDoubleNext(b / v);
                }
            }
        }

        return r;
    }

    private static Array divComplex(Array a, Array b) {
        int broadcast = broadcastCheck(a, b);
        switch (broadcast) {
            case 0:
                Array r = Array.factory(DataType.COMPLEX, a.getShape());
                if (a.getIndexPrivate().isFastIterator() && b.getIndexPrivate().isFastIterator()) {
                    for (int i = 0; i < r.getSize(); i++) {
                        r.setComplex(i, a.getComplex(i).divide(b.getComplex(i)));
                    }
                } else {
                    IndexIterator iterR = r.getIndexIterator();
                    IndexIterator iterA = a.getIndexIterator();
                    IndexIterator iterB = b.getIndexIterator();
                    while (iterA.hasNext()) {
                        iterR.setComplexNext(iterA.getComplexNext().divide(iterB.getComplexNext()));
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
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < r.getSize(); i++) {
                r.setComplex(i, a.getComplex(i).divide(b));
            }
        } else {
            IndexIterator iterR = r.getIndexIterator();
            IndexIterator iterA = a.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setComplexNext(iterA.getComplexNext().divide(b));
            }
        }

        return r;
    }

    private static Array divComplex(Array a, Complex b) {
        Array r = Array.factory(DataType.OBJECT, a.getShape());
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < r.getSize(); i++) {
                r.setComplex(i, a.getComplex(i).divide(b));
            }
        } else {
            IndexIterator iterR = r.getIndexIterator();
            IndexIterator iterA = a.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setComplexNext(iterA.getComplexNext().divide(b));
            }
        }

        return r;
    }

    private static Array divComplex(double b, Array a) {
        Array r = Array.factory(DataType.OBJECT, a.getShape());
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < r.getSize(); i++) {
                r.setComplex(i, a.getComplex(i).rDivide(b));
            }
        } else {
            IndexIterator iterR = r.getIndexIterator();
            IndexIterator iterA = a.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setComplexNext(iterA.getComplexNext().rDivide(b));
            }
        }

        return r;
    }

    private static Array divComplex(Complex b, Array a) {
        Array r = Array.factory(DataType.OBJECT, a.getShape());
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < r.getSize(); i++) {
                r.setComplex(i, b.divide(a.getComplex(i)));
            }
        } else {
            IndexIterator iterR = r.getIndexIterator();
            IndexIterator iterA = a.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setComplexNext(b.divide(iterA.getComplexNext()));
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
            case COMPLEX:
                return ArrayMath.powComplex(a, b.doubleValue());
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
            case COMPLEX:
                return ArrayMath.powComplex(a.doubleValue(), b);
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
            case COMPLEX:
                return ArrayMath.powComplex(a, b);
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
        IndexIterator iterA = a.getIndexIterator();
        if (a.getDataType() == DataType.COMPLEX) {
            r = Array.factory(DataType.DOUBLE, a.getShape());
            if (a.getIndexPrivate().isFastIterator()) {
                for (int i = 0; i < r.getSize(); i++) {
                    r.setDouble(i, a.getComplex(i).abs());
                }
            } else {
                IndexIterator iterR = r.getIndexIterator();
                while (iterA.hasNext()) {
                    iterR.setDoubleNext(iterA.getComplexNext().abs());
                }
            }
        } else {
            r = Array.factory(a.getDataType(), a.getShape());
            if (a.getIndexPrivate().isFastIterator()) {
                for (int i = 0; i < r.getSize(); i++) {
                    r.setDouble(i, Math.abs(a.getDouble(i)));
                }
            } else {
                IndexIterator iterR = r.getIndexIterator();
                while (iterA.hasNext()) {
                    iterR.setObjectNext(Math.abs(iterA.getDoubleNext()));
                }
            }
        }

        return r;
    }

    /**
     * Return the ceiling of the input, element-wise.
     *
     * @param a Array a
     * @return Result array
     */
    public static Array ceil(Array a) {
        Array r;
        IndexIterator iterA = a.getIndexIterator();
        r = Array.factory(a.getDataType(), a.getShape());
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < r.getSize(); i++) {
                r.setDouble(i, Math.ceil(a.getDouble(i)));
            }
        } else {
            IndexIterator iterR = r.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setDoubleNext(Math.ceil(iterA.getDoubleNext()));
            }
        }

        return r;
    }

    /**
     * Return the ceiling of the input, element-wise.
     *
     * @param a Array a
     * @return Result array
     */
    public static Array floor(Array a) {
        Array r;
        IndexIterator iterA = a.getIndexIterator();
        r = Array.factory(a.getDataType(), a.getShape());
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < r.getSize(); i++) {
                r.setDouble(i, Math.floor(a.getDouble(i)));
            }
        } else {
            IndexIterator iterR = r.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setDoubleNext(Math.floor(iterA.getDoubleNext()));
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
        Array r = Array.factory(DataType.BOOLEAN, a.getShape());
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < r.getSize(); i++) {
                r.setBoolean(i, a.getDouble(i) == b.getDouble(i));
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterB = b.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setBooleanNext(iterA.getDoubleNext() == iterB.getDoubleNext());
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
        Array r = Array.factory(DataType.BOOLEAN, a.getShape());
        double v = b.doubleValue();
        if (Double.isNaN(v)) {
            if (a.getIndexPrivate().isFastIterator()) {
                for (int i = 0; i < a.getSize(); i++) {
                    r.setBoolean(i, Double.isNaN(a.getDouble(i)));
                }
            } else {
                IndexIterator iterA = a.getIndexIterator();
                IndexIterator iterR = r.getIndexIterator();
                while (iterA.hasNext()) {
                    iterR.setBooleanNext(Double.isNaN(iterA.getDoubleNext()));
                }
            }
        } else {
            if (a.getIndexPrivate().isFastIterator()) {
                for (int i = 0; i < a.getSize(); i++) {
                    r.setBoolean(i, a.getDouble(i) == v);
                }
            } else {
                IndexIterator iterA = a.getIndexIterator();
                IndexIterator iterR = r.getIndexIterator();
                while (iterA.hasNext()) {
                    iterR.setBooleanNext(iterA.getDoubleNext() == v);
                }
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
    public static Array equal(Array a, String b) {
        Array r = Array.factory(DataType.BOOLEAN, a.getShape());
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < a.getSize(); i++) {
                r.setBoolean(i, a.getString(i).equals(b));
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setBooleanNext(iterA.getStringNext().equals(b));
            }
        }

        return r;
    }

    /**
     * Test element-wise for positive or negative infinity.
     *
     * @param a Array a
     * @return Result array
     */
    public static Array isInfinite(Array a) {
        Array r = Array.factory(DataType.BOOLEAN, a.getShape());
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < a.getSize(); i++) {
                r.setBoolean(i, Double.isInfinite(a.getDouble(i)));
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setBooleanNext(Double.isInfinite(iterA.getDoubleNext()));
            }
        }

        return r;
    }

    /**
     * Test element-wise for finiteness (not infinity or not Not a Number).
     *
     * @param a Array a
     * @return Result array
     */
    public static Array isFinite(Array a) {
        Array r = Array.factory(DataType.BOOLEAN, a.getShape());
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < a.getSize(); i++) {
                r.setBoolean(i, Double.isFinite(a.getDouble(i)));
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setBooleanNext(Double.isFinite(iterA.getDoubleNext()));
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
        Array r = Array.factory(DataType.BOOLEAN, a.getShape());
        if (a.getIndexPrivate().isFastIterator() && b.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < a.getSize(); i++) {
                r.setBoolean(i, a.getDouble(i) < b.getDouble(i));
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            IndexIterator iterB = b.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setBooleanNext(iterA.getDoubleNext() < iterB.getDoubleNext());
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
        Array r = Array.factory(DataType.BOOLEAN, a.getShape());
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < a.getSize(); i++) {
                r.setBoolean(i, a.getDouble(i) < b.doubleValue());
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setBooleanNext(iterA.getDoubleNext() < b.doubleValue());
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
        Array r = Array.factory(DataType.BOOLEAN, a.getShape());
        if (a.getIndexPrivate().isFastIterator() && b.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < a.getSize(); i++) {
                r.setBoolean(i, a.getDouble(i) <= b.getDouble(i));
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            IndexIterator iterB = b.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setBooleanNext(iterA.getDoubleNext() <= iterB.getDoubleNext());
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
        Array r = Array.factory(DataType.BOOLEAN, a.getShape());
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < a.getSize(); i++) {
                r.setBoolean(i, a.getDouble(i) <= b.doubleValue());
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setBooleanNext(iterA.getDoubleNext() <= b.doubleValue());
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
        Array r = Array.factory(DataType.BOOLEAN, a.getShape());
        if (a.getIndexPrivate().isFastIterator() && b.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < a.getSize(); i++) {
                r.setBoolean(i, a.getDouble(i) > b.getDouble(i));
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            IndexIterator iterB = b.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setBooleanNext(iterA.getDoubleNext() > iterB.getDoubleNext());
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
        Array r = Array.factory(DataType.BOOLEAN, a.getShape());
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < a.getSize(); i++) {
                r.setBoolean(i, a.getDouble(i) > b.doubleValue());
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setBooleanNext(iterA.getDoubleNext() > b.doubleValue());
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
        Array r = Array.factory(DataType.BOOLEAN, a.getShape());
        if (a.getIndexPrivate().isFastIterator() && b.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < a.getSize(); i++) {
                r.setBoolean(i, a.getDouble(i) >= b.getDouble(i));
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            IndexIterator iterB = b.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setBooleanNext(iterA.getDoubleNext() >= iterB.getDoubleNext());
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
        Array r = Array.factory(DataType.BOOLEAN, a.getShape());
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < a.getSize(); i++) {
                r.setBoolean(i, a.getDouble(i) >= b.doubleValue());
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setBooleanNext(iterA.getDoubleNext() >= b.doubleValue());
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
        Array r = Array.factory(DataType.BOOLEAN, a.getShape());
        if (a.getIndexPrivate().isFastIterator() && b.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < a.getSize(); i++) {
                r.setBoolean(i, a.getDouble(i) != b.getDouble(i));
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            IndexIterator iterB = b.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setBooleanNext(iterA.getDoubleNext() != iterB.getDoubleNext());
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
        Array r = Array.factory(DataType.BOOLEAN, a.getShape());
        double v = b.doubleValue();
        if (Double.isNaN(v)) {
            if (a.getIndexPrivate().isFastIterator()) {
                for (int i = 0; i < a.getSize(); i++) {
                    r.setBoolean(i, !Double.isNaN(a.getDouble(i)));
                }
            } else {
                IndexIterator iterA = a.getIndexIterator();
                IndexIterator iterR = r.getIndexIterator();
                while (iterA.hasNext()) {
                    iterR.setBooleanNext(!Double.isNaN(iterA.getDoubleNext()));
                }
            }
        } else {
            if (a.getIndexPrivate().isFastIterator()) {
                for (int i = 0; i < a.getSize(); i++) {
                    r.setBoolean(i, a.getDouble(i) != v);
                }
            } else {
                IndexIterator iterA = a.getIndexIterator();
                IndexIterator iterR = r.getIndexIterator();
                while (iterA.hasNext()) {
                    iterR.setBooleanNext(iterA.getDoubleNext() != v);
                }
            }
        }

        return r;
    }

    /**
     * Test whether any array element evaluates to True.
     *
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
     * @throws org.meteoinfo.ndarray.InvalidRangeException
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
        Array r = Array.factory(DataType.BOOLEAN, shape);
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
     *
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
     * @throws org.meteoinfo.ndarray.InvalidRangeException
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
        Array r = Array.factory(DataType.BOOLEAN, shape);
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
        Array r = Array.factory(DataType.BOOLEAN, a.getShape());
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < a.getSize(); i++) {
                r.setBoolean(i, b.contains(a.getObject(i)));
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setBooleanNext(b.contains(iterA.getObjectNext()));
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
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < a.getSize(); i++) {
                if (Double.isNaN(a.getDouble(i))) {
                    return true;
                }
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            while (iterA.hasNext()) {
                if (Double.isNaN(iterA.getDoubleNext())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Remove NaN values in an array
     *
     * @param a The array
     * @return The array withou NaN values
     */
    public static Array removeNaN(Array a) {
        List d = new ArrayList<>();
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < a.getSize(); i++) {
                if (!Double.isNaN(a.getDouble(i))) {
                    d.add(a.getObject(i));
                }
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            double v;
            while (iterA.hasNext()) {
                v = iterA.getDoubleNext();
                if (!Double.isNaN(v)) {
                    d.add(v);
                }
            }
        }

        if (d.isEmpty()) {
            return null;
        }

        Array r = Array.factory(a.getDataType(), new int[]{d.size()});
        for (int i = 0; i < d.size(); i++) {
            r.setObject(i, d.get(i));
        }

        return r;
    }

    /**
     * Remove NaN values in arrays
     *
     * @param a The arrays
     * @return The array withou NaN values
     */
    public static Array[] removeNaN(Array... a) {
        if (a.length == 1) {
            Array r0 = removeNaN(a[0]);
            return r0 == null ? null : new Array[]{removeNaN(a[0])};
        }

        List d = new ArrayList<>();
        int n = (int) a[0].getSize();
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
                for (Array aa : a) {
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
            r[i] = Array.factory(a[i].getDataType(), new int[]{len});
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
        IndexIterator iterA = a.getIndexIterator();
        int[] counter;
        double v;
        while (iterA.hasNext()) {
            v = iterA.getDoubleNext();
            if (!Double.isNaN(v) && v != 0) {
                counter = index.getCurrentCounter();
                for (int j = 0; j < ndim; j++) {
                    r.get(j).add(counter[j]);
                }
            }
            index.incr();
            //iterA.next();
        }

        if (r.get(0).isEmpty()) {
            return null;
        }

        List<Array> ra = new ArrayList<>();
        for (int i = 0; i < ndim; i++) {
            ra.add(ArrayUtil.array(r.get(i), null));
        }
        return ra;
    }

    /**
     * Bit and operation
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array bitAnd(Array a, Number b) {
        Array r = Array.factory(a.getDataType(), a.getShape());
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < a.getSize(); i++) {
                r.setInt(i, a.getInt(i) & b.intValue());
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setIntNext(iterA.getIntNext() & b.intValue());
            }
        }

        return r;
    }

    /**
     * Bit and operation
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array bitAnd(Array a, Array b) {
        Array r = Array.factory(a.getDataType(), a.getShape());
        if (a.getIndexPrivate().isFastIterator() && b.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < a.getSize(); i++) {
                r.setInt(i, a.getInt(i) & b.getInt(i));
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterB = b.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setIntNext(iterA.getIntNext() & iterB.getIntNext());
            }
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
        Array r = Array.factory(a.getDataType(), a.getShape());
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < a.getSize(); i++) {
                r.setInt(i, a.getInt(i) | b.intValue());
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setIntNext(iterA.getIntNext() | b.intValue());
            }
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
        Array r = Array.factory(a.getDataType(), a.getShape());
        if (a.getIndexPrivate().isFastIterator() && b.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < a.getSize(); i++) {
                r.setInt(i, a.getInt(i) | b.getInt(i));
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterB = b.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setIntNext(iterA.getIntNext() | iterB.getIntNext());
            }
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
        Array r = Array.factory(a.getDataType(), a.getShape());
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < a.getSize(); i++) {
                r.setInt(i, a.getInt(i) ^ b.intValue());
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setIntNext(iterA.getIntNext() ^ b.intValue());
            }
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
        Array r = Array.factory(a.getDataType(), a.getShape());
        if (a.getIndexPrivate().isFastIterator() && b.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < a.getSize(); i++) {
                r.setInt(i, a.getInt(i) ^ b.getInt(i));
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterB = b.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setIntNext(iterA.getIntNext() ^ iterB.getIntNext());
            }
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
        Array r = Array.factory(a.getDataType(), a.getShape());
        if (a.getDataType() == DataType.BOOLEAN) {
            if (a.getIndexPrivate().isFastIterator()) {
                for (int i = 0; i < a.getSize(); i++) {
                    r.setBoolean(i, !a.getBoolean(i));
                }
            } else {
                IndexIterator iterA = a.getIndexIterator();
                IndexIterator iterR = r.getIndexIterator();
                while (iterA.hasNext()) {
                    iterR.setBooleanNext(!iterA.getBooleanNext());
                }
            }
        } else {
            if (a.getIndexPrivate().isFastIterator()) {
                for (int i = 0; i < a.getSize(); i++) {
                    r.setObject(i, ~a.getInt(i));
                }
            } else {
                IndexIterator iterA = a.getIndexIterator();
                IndexIterator iterR = r.getIndexIterator();
                while (iterA.hasNext()) {
                    iterR.setObjectNext(~iterA.getIntNext());
                }
            }
        }

        return r;
    }

    /**
     * Logical not
     *
     * @param a Array a
     * @return Result array
     */
    public static Array logicalNot(Array a) {
        Array r = Array.factory(DataType.BOOLEAN, a.getShape());
        if (a.getDataType() == DataType.BOOLEAN) {
            if (a.getIndexPrivate().isFastIterator()) {
                for (int i = 0; i < a.getSize(); i++) {
                    r.setBoolean(i, !a.getBoolean(i));
                }
            } else {
                IndexIterator iterA = a.getIndexIterator();
                IndexIterator iterR = r.getIndexIterator();
                while (iterA.hasNext()) {
                    iterR.setBooleanNext(!iterA.getBooleanNext());
                }
            }
        } else {
            if (a.getIndexPrivate().isFastIterator()) {
                for (int i = 0; i < a.getSize(); i++) {
                    r.setObject(i, a.getInt(i) == 0);
                }
            } else {
                IndexIterator iterA = a.getIndexIterator();
                IndexIterator iterR = r.getIndexIterator();
                while (iterA.hasNext()) {
                    iterR.setObjectNext(iterA.getIntNext() == 0);
                }
            }
        }

        return r;
    }

    /**
     * Bit left shift operation
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array leftShift(Array a, Number b) {
        Array r = Array.factory(a.getDataType(), a.getShape());
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < a.getSize(); i++) {
                r.setObject(i, a.getInt(i) << b.intValue());
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setObjectNext(iterA.getIntNext() << b.intValue());
            }
        }

        return r;
    }

    /**
     * Bit left shift operation
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array leftShift(Array a, Array b) {
        Array r = Array.factory(a.getDataType(), a.getShape());
        if (a.getIndexPrivate().isFastIterator() && b.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < a.getSize(); i++) {
                r.setObject(i, a.getInt(i) << b.getInt(i));
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterB = b.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setObjectNext(iterA.getIntNext() << iterB.getIntNext());
            }
        }

        return r;
    }

    /**
     * Bit right shift operation
     *
     * @param a Array a
     * @param b Number b
     * @return Result array
     */
    public static Array rightShift(Array a, Number b) {
        Array r = Array.factory(a.getDataType(), a.getShape());
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < a.getSize(); i++) {
                r.setObject(i, a.getInt(i) >> b.intValue());
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setObjectNext(iterA.getIntNext() >> b.intValue());
            }
        }

        return r;
    }

    /**
     * Bit right shift operation
     *
     * @param a Array a
     * @param b Array b
     * @return Result array
     */
    public static Array rightShift(Array a, Array b) {
        Array r = Array.factory(a.getDataType(), a.getShape());
        if (a.getIndexPrivate().isFastIterator() && b.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < a.getSize(); i++) {
                r.setObject(i, a.getInt(i) >> b.getInt(i));
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterB = b.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setObjectNext(iterA.getIntNext() >> iterB.getIntNext());
            }
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
        IndexIterator iterY = y.getIndexIterator();
        while (iterY.hasNext()) {
            v = iterY.getDoubleNext();
            if (Double.isNaN(v)) {
                continue;
            }
            r += v;
            if (nn > 0 && nn < n) {
                r += v;
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
     * @throws org.meteoinfo.ndarray.InvalidRangeException
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
        IndexIterator iterY = y.getIndexIterator();
        IndexIterator iterX = x.getIndexIterator();
        double x1, x2, y1, y2;
        x1 = iterX.getDoubleNext();
        y1 = iterY.getDoubleNext();
        while (iterY.hasNext()) {
            x2 = iterX.getDoubleNext();
            y2 = iterY.getDoubleNext();
            if (Double.isNaN(x2) || Double.isNaN(y2)) {
                continue;
            }
            r += (x2 - x1) * (y2 + y1);
            if (Double.isNaN(r))
                r = 0;
            x1 = x2;
            y1 = y2;
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
     * @param ranges Ranges
     * @return Definite integral as approximated by trapezoidal rule
     * @throws org.meteoinfo.ndarray.InvalidRangeException
     */
    public static double trapz(Array y, Array x, List<Range> ranges) throws InvalidRangeException {
        double r = 0;
        double v, x1, x2;
        double v0 = Double.NEGATIVE_INFINITY;
        IndexIterator iterY = y.getRangeIterator(ranges);
        IndexIterator iterX = x.getIndexIterator();
        x1 = iterX.getDoubleNext();
        int n = 0;
        while (iterY.hasNext()) {
            v = iterY.getDoubleNext();
            if (Double.isNaN(v)) {
                continue;
            }
            if (Double.isInfinite(v0)) {
                v0 = v;
                continue;
            }
            x2 = iterX.getDoubleNext();
            r += (x2 - x1) * (v + v0);
            v0 = v;
            n += 1;
            x1 = x2;
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
     * @throws org.meteoinfo.ndarray.InvalidRangeException
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
     * @throws org.meteoinfo.ndarray.InvalidRangeException
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
     * Returns an element-wise indication of the sign of a number. The sign
     * function returns -1 if x less than 0, 0 if x==0, 1 if x bigger than 0.
     * nan is returned for nan inputs.
     *
     * @param x Input array
     * @return The sign of x array
     */
    public static Array sign(Array x) {
        Array r = Array.factory(DataType.FLOAT, x.getShape());
        if (x.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < r.getSize(); i++) {
                r.setFloat(i, Math.signum(x.getFloat(i)));
            }
        } else {
            IndexIterator iterX = x.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            while (iterX.hasNext()) {
                iterR.setFloatNext(Math.signum(iterX.getFloatNext()));
            }
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
            Index aIndex = a.getIndex();
            Index bIndex = b.getIndex();
            for (int i = 0; i < shape[0]; i++) {
                v = 0;
                for (int j = 0; j < n; j++) {
                    v += a.getDouble(aIndex.set0(j)) * b.getDouble(bIndex.set(j, i));
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
        if (a.getIndexPrivate().isFastIterator() && b.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < a.getSize(); i++) {
                r += a.getDouble(i) * b.getDouble(i);
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterB = a.getIndexIterator();
            while (iterA.hasNext()) {
                r += iterA.getDoubleNext() * iterB.getDoubleNext();
            }
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
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < a.getSize(); i++) {
                r.setDouble(i, Math.toDegrees(a.getDouble(i)));
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setDoubleNext(Math.toDegrees(iterA.getDoubleNext()));
            }
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
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < a.getSize(); i++) {
                r.setDouble(i, Math.toRadians(a.getDouble(i)));
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setDoubleNext(Math.toRadians(iterA.getDoubleNext()));
            }
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
        if (a.getDataType() == DataType.COMPLEX) {
            r = Array.factory(DataType.COMPLEX, a.getShape());
            if (a.getIndexPrivate().isFastIterator()) {
                for (int i = 0; i < a.getSize(); i++) {
                    r.setComplex(i, a.getComplex(i).sin());
                }
            } else {
                IndexIterator iterA = a.getIndexIterator();
                IndexIterator iterR = r.getIndexIterator();
                while (iterA.hasNext()) {
                    iterR.setComplexNext(iterA.getComplexNext().sin());
                }
            }
        } else {
            r = Array.factory(a.getDataType() == DataType.DOUBLE ? DataType.DOUBLE : DataType.FLOAT, a.getShape());
            if (a.getIndexPrivate().isFastIterator()) {
                for (int i = 0; i < a.getSize(); i++) {
                    r.setDouble(i, Math.sin(a.getDouble(i)));
                }
            } else {
                IndexIterator iterA = a.getIndexIterator();
                IndexIterator iterR = r.getIndexIterator();
                while (iterA.hasNext()) {
                    iterR.setDoubleNext(Math.sin(iterA.getDoubleNext()));
                }
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
        if (a.getDataType() == DataType.COMPLEX) {
            r = Array.factory(DataType.COMPLEX, a.getShape());
            if (a.getIndexPrivate().isFastIterator()) {
                for (int i = 0; i < a.getSize(); i++) {
                    r.setComplex(i, a.getComplex(i).cos());
                }
            } else {
                IndexIterator iterA = a.getIndexIterator();
                IndexIterator iterR = r.getIndexIterator();
                while (iterA.hasNext()) {
                    iterR.setComplexNext(iterA.getComplexNext().cos());
                }
            }
        } else {
            r = Array.factory(a.getDataType() == DataType.DOUBLE ? DataType.DOUBLE : DataType.FLOAT, a.getShape());
            if (a.getIndexPrivate().isFastIterator()) {
                for (int i = 0; i < a.getSize(); i++) {
                    r.setDouble(i, Math.cos(a.getDouble(i)));
                }
            } else {
                IndexIterator iterA = a.getIndexIterator();
                IndexIterator iterR = r.getIndexIterator();
                while (iterA.hasNext()) {
                    iterR.setDoubleNext(Math.cos(iterA.getDoubleNext()));
                }
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
        if (a.getDataType() == DataType.COMPLEX) {
            r = Array.factory(DataType.COMPLEX, a.getShape());
            if (a.getIndexPrivate().isFastIterator()) {
                for (int i = 0; i < a.getSize(); i++) {
                    r.setComplex(i, a.getComplex(i).tan());
                }
            } else {
                IndexIterator iterA = a.getIndexIterator();
                IndexIterator iterR = r.getIndexIterator();
                while (iterA.hasNext()) {
                    iterR.setComplexNext(iterA.getComplexNext().tan());
                }
            }
        } else {
            r = Array.factory(a.getDataType() == DataType.DOUBLE ? DataType.DOUBLE : DataType.FLOAT, a.getShape());
            if (a.getIndexPrivate().isFastIterator()) {
                for (int i = 0; i < a.getSize(); i++) {
                    r.setDouble(i, Math.tan(a.getDouble(i)));
                }
            } else {
                IndexIterator iterA = a.getIndexIterator();
                IndexIterator iterR = r.getIndexIterator();
                while (iterA.hasNext()) {
                    iterR.setDoubleNext(Math.tan(iterA.getDoubleNext()));
                }
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
        if (a.getDataType() == DataType.COMPLEX) {
            r = Array.factory(DataType.COMPLEX, a.getShape());
            if (a.getIndexPrivate().isFastIterator()) {
                for (int i = 0; i < a.getSize(); i++) {
                    r.setComplex(i, a.getComplex(i).asin());
                }
            } else {
                IndexIterator iterA = a.getIndexIterator();
                IndexIterator iterR = r.getIndexIterator();
                while (iterA.hasNext()) {
                    iterR.setComplexNext(iterA.getComplexNext().asin());
                }
            }
        } else {
            r = Array.factory(a.getDataType() == DataType.DOUBLE ? DataType.DOUBLE : DataType.FLOAT, a.getShape());
            if (a.getIndexPrivate().isFastIterator()) {
                for (int i = 0; i < a.getSize(); i++) {
                    r.setDouble(i, Math.asin(a.getDouble(i)));
                }
            } else {
                IndexIterator iterA = a.getIndexIterator();
                IndexIterator iterR = r.getIndexIterator();
                while (iterA.hasNext()) {
                    iterR.setDoubleNext(Math.asin(iterA.getDoubleNext()));
                }
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
        if (a.getDataType() == DataType.COMPLEX) {
            r = Array.factory(DataType.COMPLEX, a.getShape());
            if (a.getIndexPrivate().isFastIterator()) {
                for (int i = 0; i < r.getSize(); i++) {
                    r.setComplex(i, a.getComplex(i).acos());
                }
            } else {
                IndexIterator iterA = a.getIndexIterator();
                IndexIterator iterR = r.getIndexIterator();
                while (iterA.hasNext()) {
                    iterR.setComplexNext(iterA.getComplexNext().acos());
                }
            }
        } else {
            r = Array.factory(a.getDataType() == DataType.DOUBLE ? DataType.DOUBLE : DataType.FLOAT, a.getShape());
            if (a.getIndexPrivate().isFastIterator()) {
                for (int i = 0; i < a.getSize(); i++) {
                    r.setDouble(i, Math.acos(a.getDouble(i)));
                }
            } else {
                IndexIterator iterA = a.getIndexIterator();
                IndexIterator iterR = r.getIndexIterator();
                while (iterA.hasNext()) {
                    iterR.setDoubleNext(Math.acos(iterA.getDoubleNext()));
                }
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
        if (a.getDataType() == DataType.COMPLEX) {
            r = Array.factory(DataType.COMPLEX, a.getShape());
            if (a.getIndexPrivate().isFastIterator()) {
                for (int i = 0; i < a.getSize(); i++) {
                    r.setComplex(i, a.getComplex(i).atan());
                }
            } else {
                IndexIterator iterA = a.getIndexIterator();
                IndexIterator iterR = r.getIndexIterator();
                while (iterA.hasNext()) {
                    iterR.setComplexNext(iterA.getComplexNext().atan());
                }
            }
        } else {
            r = Array.factory(a.getDataType() == DataType.DOUBLE ? DataType.DOUBLE : DataType.FLOAT, a.getShape());
            if (a.getIndexPrivate().isFastIterator()) {
                for (int i = 0; i < a.getSize(); i++) {
                    r.setDouble(i, Math.atan(a.getDouble(i)));
                }
            } else {
                IndexIterator iterA = a.getIndexIterator();
                IndexIterator iterR = r.getIndexIterator();
                while (iterA.hasNext()) {
                    iterR.setDoubleNext(Math.atan(iterA.getDoubleNext()));
                }
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
        if (a.getIndexPrivate().isFastIterator() && b.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < a.getSize(); i++) {
                r.setDouble(i, Math.atan2(a.getDouble(i), b.getDouble(i)));
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterB = b.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            while (iterA.hasNext()) {
                iterR.setDoubleNext(Math.atan2(iterA.getDoubleNext(), iterB.getDoubleNext()));
            }
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
        if (r.getIndexPrivate().isFastIterator() && y.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < x.getSize(); i++) {
                rr = cartesianToPolar(x.getDouble(i), y.getDouble(i));
                r.setDouble(i, rr[1]);
                B.setDouble(i, rr[0]);
            }
        } else {
            IndexIterator iterX = x.getIndexIterator();
            IndexIterator iterY = y.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            IndexIterator iterB = B.getIndexIterator();
            while (iterX.hasNext()) {
                rr = cartesianToPolar(iterX.getDoubleNext(), iterY.getDoubleNext());
                iterR.setDoubleNext(rr[1]);
                iterB.setDoubleNext(rr[0]);
            }
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
        if (r.getIndexPrivate().isFastIterator() && y.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < x.getSize(); i++) {
                rr = polarToCartesian(B.getDouble(i), r.getDouble(i));
                x.setDouble(i, rr[0]);
                y.setDouble(i, rr[1]);
            }
        } else {
            IndexIterator iterX = x.getIndexIterator();
            IndexIterator iterY = y.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            IndexIterator iterB = B.getIndexIterator();
            while (iterX.hasNext()) {
                rr = polarToCartesian(iterB.getDoubleNext(), iterR.getDoubleNext());
                iterX.setDoubleNext(rr[0]);
                iterY.setDoubleNext(rr[1]);
            }
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
        //Array rr = Array.factory(r.getDataType(), r.getShape());
        //MAMath.copy(rr, r);
        return r;
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
        //Array rr = Array.factory(r.getDataType(), r.getShape());
        //MAMath.copy(rr, r);
        return r;
    }

    /**
     * Take elements from an array along an axis.
     *
     * @param a The array
     * @param indices The indices of the values to extract.
     * @param axis The axis over which to select values.
     * @return The returned array has the same type as a.
     */
    public static Array take(Array a, Array indices, Integer axis) throws InvalidRangeException {
        int nIdx = (int)indices.getSize();
        if (axis == null) {
            int[] shape = a.getShape();
            Array r = Array.factory(a.getDataType(), new int[]{nIdx});
            for (int i = 0; i < nIdx; i++) {
                r.setObject(i, a.getObject(indices.getInt(i)));
            }
            return r;
        } else {
            int[] shape = a.getShape();
            int[] nshape = new int[shape.length];
            List<Range> ranges = new ArrayList<>();
            for (int i = 0; i < shape.length; i++) {
                if (i == axis) {
                    nshape[i] = nIdx;
                } else {
                    nshape[i] = shape[i];
                }
                ranges.add(new Range(0, shape[i] - 1));
            }
            Array r = Array.factory(a.getDataType(), nshape);
            IndexIterator iter = a.getIndexIterator();
            int idx;
            if (axis == 0) {
                IndexIterator riter = r.getIndexIterator();
                for (int i = 0; i < indices.getSize(); i++) {
                    idx = indices.getInt(i);
                    ranges.set(axis, new Range(idx, idx));
                    Array temp = a.section(ranges);
                    IndexIterator titer = temp.getIndexIterator();
                    while (titer.hasNext()) {
                        riter.setObjectNext(titer.getObjectNext());
                    }
                }
            } else {
                Index rindex = r.getIndex();
                for (int i = 0; i < indices.getSize(); i++) {
                    idx = indices.getInt(i);
                    ranges.set(axis, new Range(idx, idx));
                    Array temp = a.sectionNoReduce(ranges);
                    IndexIterator titer = temp.getIndexIterator();
                    int[] current;
                    while (titer.hasNext()) {
                        titer.next();
                        current = titer.getCurrentCounter();
                        current[axis] = i;
                        rindex.set(current);
                        r.setObject(rindex, titer.getObjectCurrent());
                    }
                }
            }

            return r;
        }
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
        r = Array.factory(a.getDataType(), a.getShape(), r.getStorage());
        return r;
    }

    /**
     * Set section
     *
     * @param a Array a
     * @param ranges Ranges
     * @param v Object value
     * @return Result array
     * @throws InvalidRangeException
     */
    public static Array setSection(Array a, List<Range> ranges, Object v) throws InvalidRangeException {
        Array r = a.section(ranges);
        IndexIterator iter = r.getIndexIterator();
        if (a.getDataType() == DataType.BOOLEAN) {
            boolean b = true;
            if (v instanceof Number) {
                if (((Number)v).doubleValue() == 0) {
                    b = false;
                }
            } else if (v instanceof Boolean) {
                if (!((Boolean)v)) {
                    b = false;
                }
            } else {
                return a;
            }
            while (iter.hasNext()) {
                iter.setObjectNext(b);
            }
        } else {
            while (iter.hasNext()) {
                iter.setObjectNext(v);
            }
        }
        r = Array.factory(a.getDataType(), a.getShape(), r.getStorage());
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
        r = Array.factory(a.getDataType(), a.getShape(), r.getStorage());
        return r;
    }

    /**
     * Set section
     *
     * @param a Array a
     * @param origin Origin
     * @param shape Shape
     * @param v Array value
     * @return Result array
     * @throws InvalidRangeException
     */
    public static Array setSection(Array a, int[] origin, int[] shape, Array v) throws InvalidRangeException {
        Array r = a.section(origin, shape);
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
        r = Array.factory(a.getDataType(), a.getShape(), r.getStorage());
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
        Array r = Array.factory(a.getDataType(), a.getShape());
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
        Index index = r.getIndex();
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
            if (val != missingv) {
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
            if (val != missingv) {
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
    public static Number min(Array a) {
        double min = Double.MAX_VALUE;
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
        if (min == Double.MAX_VALUE) {
            return Double.NaN;
        } else {
            return doubleToNumber(min, a.getDataType());
        }
    }

    /**
     * Compute minimum value of an array along an axis (dimension)
     *
     * @param a Array a
     * @param axis Axis
     * @return Minimum value array
     * @throws org.meteoinfo.ndarray.InvalidRangeException
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
     * @throws org.meteoinfo.ndarray.InvalidRangeException
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
    public static Number max(Array a) {
        double max = -Double.MAX_VALUE;
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
        if (max == Double.MIN_VALUE) {
            return Double.NaN;
        } else {
            return doubleToNumber(max, a.getDataType());
        }
    }

    /**
     * Compute maximum value of an array along an axis (dimension)
     *
     * @param a Array a
     * @param axis Axis
     * @return Maximum value array
     * @throws org.meteoinfo.ndarray.InvalidRangeException
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
     * @throws org.meteoinfo.ndarray.InvalidRangeException
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
     * @throws org.meteoinfo.ndarray.InvalidRangeException
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
     * @throws org.meteoinfo.ndarray.InvalidRangeException
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
        List<IndexIterator> iters = new ArrayList<>();
        for (Array a : alist) {
            iters.add(a.getIndexIterator());
        }
        for (int i = 0; i < r.getSize(); i++) {
            sum = 0.0;
            n = 0;
            for (IndexIterator iter : iters) {
                v = iter.getDoubleNext();
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
     * Return the cumulative sum of the elements along a given axis.
     *
     * @param a Array a
     * @param axis Axis
     * @return Sum value array
     * @throws InvalidRangeException
     */
    public static Array cumsum(Array a, int axis) throws InvalidRangeException {
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
        Array r = Array.factory(a.getDataType(), shape);
        Array rr = Array.factory(a.getDataType(), dataShape);
        List<Double> s;
        Index indexr = r.getIndex();
        Index indexrr = rr.getIndex();
        int[] current, currentrr = indexrr.getCurrentCounter();
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
                    currentrr[j] = current[idx];
                }
            }
            s = cumsum(a, ranges);
            for (int j = 0; j < s.size(); j++) {
                currentrr[axis] = j;
                rr.setDouble(indexrr.set(currentrr), s.get(j));
            }
            indexr.incr();
        }
        r = null;

        return rr;
    }

    /**
     * Compute cumulative sum value of an array
     *
     * @param a Array a
     * @param ranges Range list
     * @return Sum value
     * @throws InvalidRangeException
     */
    public static List<Double> cumsum(Array a, List<Range> ranges) throws InvalidRangeException {
        double s = 0.0, v;
        IndexIterator ii = a.getRangeIterator(ranges);
        List<Double> r = new ArrayList<>();
        while (ii.hasNext()) {
            v = ii.getDoubleNext();
            s += v;
            r.add(s);
        }

        return r;
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
     * @throws org.meteoinfo.ndarray.InvalidRangeException
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
     * @throws org.meteoinfo.ndarray.InvalidRangeException
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
     * @throws org.meteoinfo.ndarray.InvalidRangeException
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
     * @throws org.meteoinfo.ndarray.InvalidRangeException
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
     * @throws org.meteoinfo.ndarray.InvalidRangeException
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
        IndexIterator iterA = a.getIndexIterator();
        while (iterA.hasNext()) {
            v = iterA.getDoubleNext();
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
        List<IndexIterator> iters = new ArrayList<>();
        for (Array a : alist) {
            iters.add(a.getIndexIterator());
        }
        for (int i = 0; i < r.getSize(); i++) {
            sum = 0.0;
            n = 0;
            for (IndexIterator iter : iters) {
                v = iter.getDoubleNext();
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
     * @throws org.meteoinfo.ndarray.InvalidRangeException
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
     * @param ddof Means delta degree of freedom
     * @return Standard deviation value array
     * @throws org.meteoinfo.ndarray.InvalidRangeException
     */
    public static Array std(Array a, int axis, int ddof) throws InvalidRangeException {
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
            mean = std(a, ranges, ddof);
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
     * @param ddof Means delta degree of freedom
     * @return Standard deviation value
     * @throws org.meteoinfo.ndarray.InvalidRangeException
     */
    public static double std(Array a, List<Range> ranges, int ddof) throws InvalidRangeException {
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
        sum = Math.sqrt(sum / (n - ddof));

        return sum;
    }

    /**
     * Compute standard deviation value of an array
     *
     * @param a Array a
     * @return Standard deviation value
     * @throws org.meteoinfo.ndarray.InvalidRangeException
     */
    public static double std(Array a) throws InvalidRangeException {
        return std(a, 0);
    }

    /**
     * Compute standard deviation value of an array
     *
     * @param a Array a
     * @param ddof Means delta degree of freedom
     * @return Standard deviation value
     * @throws org.meteoinfo.ndarray.InvalidRangeException
     */
    public static double std(Array a, int ddof) throws InvalidRangeException {
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
        sum = Math.sqrt(sum / (n - ddof));

        return sum;
    }

    /**
     * Compute variance value of an array
     *
     * @param a Array a
     * @param ddof Means delta degree of freedom
     * @return Variance value
     * @throws org.meteoinfo.ndarray.InvalidRangeException
     */
    public static double var(Array a, int ddof) throws InvalidRangeException {
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
        sum = sum / (n - ddof);

        return sum;
    }

    /**
     * Compute variance value of an array along an axis (dimension)
     *
     * @param a Array a
     * @param axis Axis
     * @param ddof Means delta degree of freedom
     * @return Variance value array
     * @throws org.meteoinfo.ndarray.InvalidRangeException
     */
    public static Array var(Array a, int axis, int ddof) throws InvalidRangeException {
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
            mean = var(a, ranges, ddof);
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
     * @param ddof Means delta degree of freedom
     * @return Variance value
     * @throws org.meteoinfo.ndarray.InvalidRangeException
     */
    public static double var(Array a, List<Range> ranges, int ddof) throws InvalidRangeException {
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
        sum = sum / (n - ddof);

        return sum;
    }

    /**
     * Compute median value of an array along an axis (dimension)
     *
     * @param a Array a
     * @param axis Axis
     * @return Median value array
     * @throws org.meteoinfo.ndarray.InvalidRangeException
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
     * Median funtion
     *
     * @param aDataList The data list
     * @return Median
     */
    public static double median(List<Double> aDataList) {
        Collections.sort(aDataList);
        if (aDataList.size() % 2 == 0) {
            return (aDataList.get(aDataList.size() / 2) + aDataList.get(aDataList.size() / 2 - 1)) / 2.0;
        } else {
            return aDataList.get(aDataList.size() / 2);
        }
    }

    /**
     * Minimum function
     *
     * @param aDataList The data list
     * @return Minimum
     */
    public static double minimum(List<Double> aDataList) {
        double aMin;

        aMin = aDataList.get(0);
        for (int i = 1; i < aDataList.size(); i++) {
            aMin = Math.min(aMin, aDataList.get(i));
        }

        return aMin;
    }

    /**
     * Maximum function
     *
     * @param aDataList The data list
     * @return Maximum
     */
    public static double maximum(List<Double> aDataList) {
        double aMax;

        aMax = aDataList.get(0);
        for (int i = 1; i < aDataList.size(); i++) {
            aMax = Math.max(aMax, aDataList.get(i));
        }

        return aMax;
    }

    /**
     * Quantile function
     *
     * @param aDataList The data list
     * @param aNum Quantile index
     * @return Quantile value
     */
    public static double quantile(List<Double> aDataList, int aNum) {
        Collections.sort(aDataList);
        double aData = 0;
        switch (aNum) {
            case 0:
                aData = minimum(aDataList);
                break;
            case 1:
                if ((aDataList.size() + 1) % 4 == 0) {
                    aData = aDataList.get((aDataList.size() + 1) / 4 - 1);
                } else {
                    aData = aDataList.get((aDataList.size() + 1) / 4 - 1) + 0.75 * (aDataList.get((aDataList.size() + 1) / 4)
                            - aDataList.get((aDataList.size() + 1) / 4 - 1));
                }
                break;
            case 2:
                aData = median(aDataList);
                break;
            case 3:
                if ((aDataList.size() + 1) % 4 == 0) {
                    aData = aDataList.get((aDataList.size() + 1) * 3 / 4 - 1);
                } else {
                    aData = aDataList.get((aDataList.size() + 1) * 3 / 4 - 1) + 0.25 * (aDataList.get((aDataList.size() + 1) * 3 / 4)
                            - aDataList.get((aDataList.size() + 1) * 3 / 4 - 1));
                }
                break;
            case 4:
                aData = maximum(aDataList);
                break;
        }

        return aData;
    }

    /**
     * Quantile function
     *
     * @param a The data array
     * @param aNum Quantile index
     * @return Quantile value
     */
    public static double quantile(Array a, int aNum) {
        List<Double> dlist = new ArrayList<>();
        IndexIterator ii = a.getIndexIterator();
        double v;
        while (ii.hasNext()) {
            v = ii.getDoubleNext();
            if (!Double.isNaN(v)) {
                dlist.add(v);
            }
        }
        if (dlist.size() <= 3) {
            return Double.NaN;
        } else {
            return quantile(dlist, aNum);
        }
    }

    /**
     * Compute median value of an array
     *
     * @param a Array a
     * @param ranges Range list
     * @return Median value
     * @throws org.meteoinfo.ndarray.InvalidRangeException
     */
    public static double median(Array a, List<Range> ranges) throws InvalidRangeException {
        Array b = a.section(ranges);
        double median = quantile(b, 2);
        return median;
    }

    /**
     * Compute median value of an array
     *
     * @param a Array a
     * @return Median value
     */
    public static double median(Array a) {
        return quantile(a, 2);
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
        if (x1.getIndexPrivate().isFastIterator() && x2.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < r.getSize(); i++) {
                r.setObject(i, Math.max(x1.getDouble(i), x2.getDouble(i)));
            }
        } else {
            IndexIterator iterX1 = x1.getIndexIterator();
            IndexIterator iterX2 = x2.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            while (iterX1.hasNext()) {
                iterR.setObjectNext(Math.max(iterX1.getDoubleNext(), iterX2.getDoubleNext()));
            }
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
        if (x1.getIndexPrivate().isFastIterator() && x2.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < r.getSize(); i++) {
                if (Double.isNaN(x1.getDouble(i))) {
                    r.setObject(i, x2.getDouble(i));
                } else if (Double.isNaN(x2.getDouble(i))) {
                    r.setObject(i, x1.getDouble(i));
                } else {
                    r.setObject(i, Math.max(x1.getDouble(i), x2.getDouble(i)));
                }
            }
        } else {
            IndexIterator iterX1 = x1.getIndexIterator();
            IndexIterator iterX2 = x2.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            double v1, v2;
            while (iterX1.hasNext()) {
                v1 = iterX1.getDoubleNext();
                v2 = iterX2.getDoubleNext();
                if (Double.isNaN(v1)) {
                    iterR.setObjectNext(v2);
                } else if (Double.isNaN(v2)) {
                    iterR.setObjectNext(v1);
                } else {
                    iterR.setObjectNext(Math.max(v1, v2));
                }
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
        if (x1.getIndexPrivate().isFastIterator() && x2.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < r.getSize(); i++) {
                r.setObject(i, Math.min(x1.getDouble(i), x2.getDouble(i)));
            }
        } else {
            IndexIterator iterX1 = x1.getIndexIterator();
            IndexIterator iterX2 = x2.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            while (iterX1.hasNext()) {
                iterR.setObjectNext(Math.min(iterX1.getDoubleNext(), iterX2.getDoubleNext()));
            }
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
        if (x1.getIndexPrivate().isFastIterator() && x2.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < r.getSize(); i++) {
                if (Double.isNaN(x1.getDouble(i))) {
                    r.setObject(i, x2.getDouble(i));
                } else if (Double.isNaN(x2.getDouble(i))) {
                    r.setObject(i, x1.getDouble(i));
                } else {
                    r.setObject(i, Math.min(x1.getDouble(i), x2.getDouble(i)));
                }
            }
        } else {
            IndexIterator iterX1 = x1.getIndexIterator();
            IndexIterator iterX2 = x2.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            double v1, v2;
            while (iterX1.hasNext()) {
                v1 = iterX1.getDoubleNext();
                v2 = iterX2.getDoubleNext();
                if (Double.isNaN(v1)) {
                    iterR.setObjectNext(v2);
                } else if (Double.isNaN(v2)) {
                    iterR.setObjectNext(v1);
                } else {
                    iterR.setObjectNext(Math.min(v1, v2));
                }
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
            Index indexX = x.getIndex();
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
                    vv = x.getDouble(indexX.set0(idx));
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
            Index indexX = x.getIndex();
            for (int i = 0; i < n; i++) {
                v = 0;
                dn = 0;
                for (int j = 0; j < window; j++) {
                    if (i - j < 0) {
                        break;
                    }
                    vv = x.getDouble(indexX.set0(i - j));
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
                    if (val == missingv.doubleValue()) {
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
        if (Arrays.equals(a.getShape(), b.getShape())) {
            if (b.getDataType() == DataType.BOOLEAN) {
                if (a.getIndexPrivate().isFastIterator() && b.getIndexPrivate().isFastIterator()) {
                    for (int i = 0; i < a.getSize(); i++) {
                        if (b.getBoolean(i)) {
                            a.setObject(i, value);
                        }
                    }
                } else {
                    IndexIterator iterA = a.getIndexIterator();
                    IndexIterator iterB = b.getIndexIterator();
                    while (iterA.hasNext()) {
                        if (iterB.getBooleanNext()) {
                            iterA.setObjectNext(value);
                        } else {
                            iterA.next();
                        }
                    }
                }
            } else {
                if (a.getIndexPrivate().isFastIterator() && b.getIndexPrivate().isFastIterator()) {
                    for (int i = 0; i < a.getSize(); i++) {
                        if (b.getInt(i) == 1) {
                            a.setObject(i, value);
                        }
                    }
                } else {
                    IndexIterator iterA = a.getIndexIterator();
                    IndexIterator iterB = b.getIndexIterator();
                    while (iterA.hasNext()) {
                        if (iterB.getIntNext() == 1) {
                            iterA.setObjectNext(value);
                        } else {
                            iterA.next();
                        }
                    }
                }
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterB = b.getIndexIterator();
            int v;
            while(iterB.hasNext()) {
                v = iterB.getIntNext();
                a.setObject(v, value);
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
            if (a.getIndexPrivate().isFastIterator() && b.getIndexPrivate().isFastIterator()
                    && value.getIndexPrivate().isFastIterator()) {
                for (int i = 0; i < a.getSize(); i++) {
                    if (b.getBoolean(i)) {
                        a.setObject(i, value.getObject(i));
                    }
                }
            } else {
                IndexIterator iterA = a.getIndexIterator();
                IndexIterator iterB = b.getIndexIterator();
                IndexIterator iterV = value.getIndexIterator();
                while (iterA.hasNext()) {
                    if (iterB.getBooleanNext()) {
                        iterA.setObjectNext(iterV.getObjectNext());
                    }
                }
            }
        } else {
            if (a.getIndexPrivate().isFastIterator() && b.getIndexPrivate().isFastIterator()
                    && value.getIndexPrivate().isFastIterator()) {
                for (int i = 0; i < a.getSize(); i++) {
                    if (b.getInt(i) == 1) {
                        a.setObject(i, value.getObject(i));
                    }
                }
            } else {
                IndexIterator iterA = a.getIndexIterator();
                IndexIterator iterB = b.getIndexIterator();
                IndexIterator iterV = value.getIndexIterator();
                while (iterA.hasNext()) {
                    if (iterB.getIntNext() == 1) {
                        iterA.setObjectNext(iterV.getObjectNext());
                    }
                }
            }
        }
    }

    /**
     * Replace value
     *
     * @param a Array a
     * @param oValue Replaced value
     * @param value New value
     */
    public static void replaceValue(Array a, Object oValue, Object value) {
        if (a.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < a.getSize(); i++) {
                if (a.getObject(i) == oValue) {
                    a.setObject(i, value);
                }
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            while (iterA.hasNext()) {
                if (iterA.getObjectNext() == oValue) {
                    iterA.setObjectCurrent(value);
                } else {
                    iterA.next();
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
            case STRING:
                while (iterA.hasNext()) {
                    r.add(iterA.getStringNext());
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
     * Convert double to number by data type
     * @param v Double value
     * @param dt Data type
     * @return Number
     */
    public static Number doubleToNumber(double v, DataType dt) {
        Number n = new Double(v);
        switch (dt) {
            case INT:
                return n.intValue();
            case FLOAT:
                return n.floatValue();
            case SHORT:
                return n.shortValue();
            case BYTE:
                return n.byteValue();
        }
        return n;
    }

    // </editor-fold>       
    // <editor-fold desc="Location">
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
        if (a.getIndexPrivate().isFastIterator() && m.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < n; i++) {
                if (m.getDouble(i) < 0) {
                    r.setObject(i, missingValue);
                } else {
                    r.setObject(i, a.getObject(i));
                }
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterM = m.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            while (iterA.hasNext()) {
                if (iterM.getDoubleNext() < 0) {
                    iterR.setObjectNext(missingValue);
                } else {
                    iterR.setObjectNext(iterA.getObjectNext());
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
     * @return Result array
     */
    public static Array maskout(Array a, Array m) {
        Array r = Array.factory(a.getDataType(), a.getShape());
        int n = (int) a.getSize();
        if (a.getIndexPrivate().isFastIterator() && m.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < n; i++) {
                if (m.getDouble(i) < 0) {
                    r.setObject(i, Double.NaN);
                } else {
                    r.setObject(i, a.getObject(i));
                }
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterM = m.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            while (iterA.hasNext()) {
                if (iterM.getDoubleNext() < 0) {
                    iterR.setObjectNext(Double.NaN);
                } else {
                    iterR.setObjectNext(iterA.getObjectNext());
                }
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
        if (a.getIndexPrivate().isFastIterator() && m.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < n; i++) {
                if (m.getDouble(i) >= 0) {
                    r.setObject(i, Double.NaN);
                } else {
                    r.setObject(i, a.getObject(i));
                }
            }
        } else {
            IndexIterator iterA = a.getIndexIterator();
            IndexIterator iterM = m.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            while (iterA.hasNext()) {
                if (iterM.getDoubleNext() >= 0) {
                    iterR.setObjectNext(Double.NaN);
                } else {
                    iterR.setObjectNext(iterA.getObjectNext());
                }
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
        IndexIterator iterX = xData.getIndexIterator();
        IndexIterator iterY = yData.getIndexIterator();
        while (iterX.hasNext()) {
            x = iterX.getDoubleNext();
            y = iterY.getDoubleNext();
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
        IndexIterator xIter = xData.getIndexIterator();
        IndexIterator yIter = yData.getIndexIterator();
        double xv, yv;
        while (xIter.hasNext()) {
            xv = xIter.getDoubleNext();
            yv = yIter.getDoubleNext();
            if (Double.isNaN(xv))
                continue;
            if (Double.isNaN(yv))
                continue;
            xi.add(xv);
            yi.add(yv);
            x_sum += xv;
            y_sum += yv;
            sx_sum += xv * xv;
            sy_sum += yv * yv;
            xy_sum += xv * yv;
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
        if (x.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < x.getSize(); i++) {
                double val = x.getDouble(i);
                double rval = 0.0;
                for (int j = 0; j < n; j++) {
                    rval += p.get(j).doubleValue() * Math.pow(val, n - j - 1);
                }
                r.setDouble(i, rval);
            }
        } else {
            IndexIterator iterX = x.getIndexIterator();
            IndexIterator iterR = r.getIndexIterator();
            double val, rval;
            while (iterX.hasNext()) {
                val = iterX.getDoubleNext();
                rval = 0.0;
                for (int j = 0; j < n; j++) {
                    rval += p.get(j).doubleValue() * Math.pow(val, n - j - 1);
                }
                iterR.setDoubleNext(rval);
            }
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
        Array r = Array.factory(DataType.DOUBLE, shape);
        IndexIterator iterU = uData.getIndexIterator();
        IndexIterator iterV = vData.getIndexIterator();
        IndexIterator iterR = r.getIndexIterator();
        double u, v;
        while (iterU.hasNext()) {
            u = iterU.getDoubleNext();
            v = iterV.getDoubleNext();
            if (Double.isNaN(u) || Double.isNaN(v)) {
                iterR.setDoubleNext(Double.NaN);
            } else {
                iterR.setDoubleNext(Math.sqrt(Math.pow(u, 2) + Math.pow(v, 2)));
            }
        }

        return r;
    }

    // </editor-fold>
}
