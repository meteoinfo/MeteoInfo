/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.math;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.meteoinfo.geoprocess.GeoComputation;
import org.meteoinfo.global.DataConvert;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointD;
import org.meteoinfo.global.util.BigDecimalUtil;
import org.meteoinfo.global.util.GlobalUtil;
import org.meteoinfo.global.util.JDateUtil;
import org.meteoinfo.io.EndianDataOutputStream;
import org.meteoinfo.math.spatial.KDTree;
import org.meteoinfo.math.spatial.KDTree.SearchResult;
import org.meteoinfo.ndarray.Complex;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.Index;
import org.meteoinfo.ndarray.Index2D;
import org.meteoinfo.ndarray.IndexIterator;
import org.meteoinfo.ndarray.InvalidRangeException;
import org.meteoinfo.ndarray.Range;
import org.meteoinfo.shape.PolygonShape;

/**
 *
 * @author yaqiang
 */
public class ArrayUtil {

    // <editor-fold desc="File">
    /**
     * Read ASCII data file to an array
     *
     * @param fileName File name
     * @param delimiter Delimiter
     * @param headerLines Headerline number
     * @param dataType Data type string
     * @param shape Shape
     * @param readFirstCol Read first column data or not
     * @return Result array
     * @throws UnsupportedEncodingException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static Array readASCIIFile(String fileName, String delimiter, int headerLines, String dataType,
            List<Integer> shape, boolean readFirstCol) throws UnsupportedEncodingException, FileNotFoundException, IOException {
        BufferedReader sr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
        if (headerLines > 0) {
            for (int i = 0; i < headerLines; i++) {
                sr.readLine();
            }
        }

        DataType dt = DataType.DOUBLE;
        if (dataType != null) {
            if (dataType.contains("%")) {
                dataType = dataType.split("%")[1];
            }
            dt = ArrayUtil.toDataType(dataType);
        }

        int i;
        int[] ss = new int[shape.size()];
        for (i = 0; i < shape.size(); i++) {
            ss[i] = shape.get(i);
        }
        Array a = Array.factory(dt, ss);

        String[] dataArray;
        i = 0;
        String line = sr.readLine();
        int sCol = 0;
        if (!readFirstCol) {
            sCol = 1;
        }

        if (delimiter == null || delimiter.equals(" ")) {
            delimiter = "\\s+";
        } else {
            delimiter = Pattern.quote(delimiter);
        }

        while (line != null) {
            line = line.trim();
            if (line.isEmpty()) {
                line = sr.readLine();
                continue;
            }
            dataArray = line.split(delimiter);
            for (int j = sCol; j < dataArray.length; j++) {
                a.setDouble(i, Double.parseDouble(dataArray[j]));
                i += 1;
                if (i >= a.getSize()) {
                    break;
                }
            }
            if (i >= a.getSize()) {
                break;
            }

            line = sr.readLine();
        }
        sr.close();

        return a;
    }

    /**
     * Get row number of a ASCII file
     *
     * @param fileName File name
     * @return Row number
     * @throws FileNotFoundException
     */
    public static int numASCIIRow(String fileName) throws FileNotFoundException {
        File f = new File(fileName);
        int lineNumber;
        String line;
        try (Scanner fileScanner = new Scanner(f)) {
            lineNumber = 0;
            while (fileScanner.hasNextLine()) {
                line = fileScanner.nextLine(); 
                if (!line.isEmpty())
                    lineNumber++;
            }
        }

        return lineNumber;
    }

    /**
     * Get row number of a ASCII file
     *
     * @param fileName File name
     * @param delimiter
     * @param headerLines
     * @return Row number
     * @throws FileNotFoundException
     */
    public static int numASCIICol(String fileName, String delimiter, int headerLines) throws FileNotFoundException, IOException {
        String[] dataArray;
        if (delimiter == null){
            delimiter = "\\s+";
        } else {
            delimiter = Pattern.quote(delimiter);
        }
        try (BufferedReader sr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)))) {
            if (headerLines > 0) {
                for (int i = 0; i < headerLines; i++) {
                    sr.readLine();
                }
            }
            String line = sr.readLine().trim();
            dataArray = line.split(delimiter);
        }

        return dataArray.length;
    }

    /**
     * Create a binary file
     * @param fn The file path
     * @return EndianDataOutputStream
     */
    public static EndianDataOutputStream createBinFile(String fn) throws IOException {
        DataOutputStream out = new DataOutputStream(new FileOutputStream(new File(fn)));
        EndianDataOutputStream outs = new EndianDataOutputStream(out);
        return outs;
    }

    /**
     * Save an array data to a binary file
     *
     * @param outs EndianDataOutputStream
     * @param a Array
     * @param byteOrder Byte order
     * @param sequential If write as sequential binary file - Fortran
     */
    public static void writeBinFile(EndianDataOutputStream outs, Array a, String byteOrder,
                                   boolean sequential) {
        try {
            ByteBuffer bb = a.getDataAsByteBuffer();
            int n = (int) a.getSize();
            ByteOrder bOrder = ByteOrder.LITTLE_ENDIAN;
            if (byteOrder.equalsIgnoreCase("big_endian")) {
                bOrder = ByteOrder.BIG_ENDIAN;
            }

            if (sequential) {
                if (bOrder == ByteOrder.BIG_ENDIAN) {
                    outs.writeIntBE(n * 4);
                } else {
                    outs.writeIntLE(n * 4);
                }
            }

            if (bOrder == ByteOrder.BIG_ENDIAN) {
                outs.write(bb.array());
            } else if (a.getDataType() == DataType.BYTE) {
                outs.write(bb.array());
            } else {
                ByteBuffer nbb = ByteBuffer.allocate(bb.array().length);
                nbb.order(bOrder);
                switch (a.getDataType()) {
                    case INT:
                        for (int i = 0; i < a.getSize(); i++) {
                            nbb.putInt(i * 4, bb.getInt());
                            //nbb.putInt(a.getInt(i));
                        }
                        break;
                    case FLOAT:
                        for (int i = 0; i < a.getSize(); i++) {
                            nbb.putFloat(i * 4, bb.getFloat());
                        }
                        break;
                    case DOUBLE:
                        for (int i = 0; i < a.getSize(); i++) {
                            nbb.putDouble(i * 8, bb.getDouble());
                        }
                        break;
                    default:
                        nbb.put(bb);
                }
                outs.write(nbb.array());
            }

            if (sequential) {
                if (bOrder == ByteOrder.BIG_ENDIAN) {
                    outs.writeIntBE(n * 4);
                } else {
                    outs.writeIntLE(n * 4);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ArrayUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ArrayUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Save an array data to a binary file
     *
     * @param fn File path
     * @param a Array
     * @param byteOrder Byte order
     * @param append If append to existing file
     * @param sequential If write as sequential binary file - Fortran
     */
    public static void saveBinFile(String fn, Array a, String byteOrder, boolean append,
            boolean sequential) {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(new File(fn), append))) {
            EndianDataOutputStream outs = new EndianDataOutputStream(out);
            ByteBuffer bb = a.getDataAsByteBuffer();
            int n = (int) a.getSize();
            ByteOrder bOrder = ByteOrder.LITTLE_ENDIAN;
            if (byteOrder.equalsIgnoreCase("big_endian")) {
                bOrder = ByteOrder.BIG_ENDIAN;
            }

            if (sequential) {
                if (bOrder == ByteOrder.BIG_ENDIAN) {
                    outs.writeIntBE(n * 4);
                } else {
                    outs.writeIntLE(n * 4);
                }
            }

            if (bOrder == ByteOrder.BIG_ENDIAN) {
                outs.write(bb.array());
            } else if (a.getDataType() == DataType.BYTE) {
                outs.write(bb.array());
            } else {
                ByteBuffer nbb = ByteBuffer.allocate(bb.array().length);
                nbb.order(bOrder);
                switch (a.getDataType()) {
                    case INT:
                        for (int i = 0; i < a.getSize(); i++) {
                            nbb.putInt(i * 4, bb.getInt());
                            //nbb.putInt(a.getInt(i));
                        }
                        break;
                    case FLOAT:
                        for (int i = 0; i < a.getSize(); i++) {
                            nbb.putFloat(i * 4, bb.getFloat());
                        }
                        break;
                    case DOUBLE:
                        for (int i = 0; i < a.getSize(); i++) {
                            nbb.putDouble(i * 8, bb.getDouble());
                        }
                        break;
                    default:
                        nbb.put(bb);
                }
                outs.write(nbb.array());
            }

            if (sequential) {
                if (bOrder == ByteOrder.BIG_ENDIAN) {
                    outs.writeIntBE(n * 4);
                } else {
                    outs.writeIntLE(n * 4);
                }
            }

            outs.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ArrayUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ArrayUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Save an array data to a ASCII file
     *
     * @param fn File path
     * @param a Array
     * @param colNum Column number of each line
     * @param format String format
     * @param delimiter Delimiter
     * @throws java.io.IOException
     */
    public static void saveASCIIFile(String fn, Array a, int colNum,
            String format, String delimiter) throws IOException {
        BufferedWriter sw = new BufferedWriter(new FileWriter(new File(fn)));
        String line = "";
        int j = 0;
        IndexIterator iter = a.getIndexIterator();
        Object o;
        for (int i = 0; i < a.getSize(); i++) {
            j += 1;
            o = iter.getObjectNext();
            if (format == null) {
                line = line + o.toString();
            } else {
                line = line + String.format(format, o);
            }
            if (j < colNum && i < a.getSize() - 1) {
                if (delimiter == null) {
                    line = line + " ";
                } else {
                    line = line + delimiter;
                }
            } else {
                sw.write(line);
                sw.newLine();
                line = "";
                j = 0;
            }
        }
        sw.flush();
        sw.close();
    }

    /**
     * Read array from a binary file
     *
     * @param fn Binary file name
     * @param dims Dimensions
     * @param dataType Data type string
     * @param skip Skip bytes
     * @param byteOrder Byte order
     * @return Result array
     */
    public static Array readBinFile(String fn, List<Integer> dims, String dataType, int skip,
            String byteOrder) {
        DataType dt = DataType.DOUBLE;
        if (dataType != null) {
            if (dataType.contains("%")) {
                dataType = dataType.split("%")[1];
            }
            dt = ArrayUtil.toDataType(dataType);
        }
        DataType ndt = dt;
        if (dt == DataType.BYTE) {
            ndt = DataType.INT;
        }

        ByteOrder bOrder = ByteOrder.LITTLE_ENDIAN;
        if (byteOrder.equalsIgnoreCase("big_endian")) {
            bOrder = ByteOrder.BIG_ENDIAN;
        }

        int[] shape = new int[dims.size()];
        for (int i = 0; i < dims.size(); i++) {
            shape[i] = dims.get(i);
        }
        Array r = Array.factory(ndt, shape);
        IndexIterator iter = r.getIndexIterator();
        try {
            DataInputStream ins = new DataInputStream(new FileInputStream(fn));
            ins.skip(skip);
            byte[] bytes;
            byte[] db;
            int start = 0;
            switch (dt) {
                case BYTE:
                    bytes = new byte[(int) r.getSize()];
                    ins.read(bytes);
                    for (int i = 0; i < r.getSize(); i++) {
                        r.setInt(i, DataConvert.byte2Int(bytes[i]));
                    }
                    break;
                case SHORT:
                    bytes = new byte[(int) r.getSize() * 2];
                    db = new byte[2];
                    ins.read(bytes);
                    while (iter.hasNext()) {
                        System.arraycopy(bytes, start, db, 0, 2);
                        iter.setShortNext(DataConvert.bytes2Short(db, bOrder));
                        start += 2;
                    }
                    break;
                case INT:
                    bytes = new byte[(int) r.getSize() * 4];
                    db = new byte[4];
                    ins.read(bytes);
                    while (iter.hasNext()) {
                        System.arraycopy(bytes, start, db, 0, 4);
                        iter.setIntNext(DataConvert.bytes2Int(db, bOrder));
                        start += 4;
                    }
                    break;
                case FLOAT:
                    bytes = new byte[(int) r.getSize() * 4];
                    db = new byte[4];
                    ins.read(bytes);
                    while (iter.hasNext()) {
                        System.arraycopy(bytes, start, db, 0, 4);
                        iter.setFloatNext(DataConvert.bytes2Float(db, bOrder));
                        start += 4;
                    }
                    break;
                case DOUBLE:
                    bytes = new byte[(int) r.getSize() * 8];
                    db = new byte[8];
                    ins.read(bytes);
                    while (iter.hasNext()) {
                        System.arraycopy(bytes, start, db, 0, 8);
                        iter.setDoubleNext(DataConvert.bytes2Double(db, bOrder));
                        start += 8;
                    }
                    break;
            }
            ins.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ArrayUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ArrayUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        return r;
    }

    // </editor-fold>
    // <editor-fold desc="Create">
     /**
     * Create an array
     *
     * @param data Object
     * @return Array
     */
    public static Array array(Object data) {
        return array(data, null);
    }
    
    /**
     * Create an array
     *
     * @param data Object
     * @param dt Data type
     * @return Array
     */
    public static Array array(Object data, DataType dt) {
        if (data instanceof Array) {
            return (Array) data;
        } else if (data instanceof List) {
            return array_list((List) data, dt);
        } else if (data.getClass().isArray()) {
            return Array.factory(data);
        } else {
            if (dt == null)
                dt = ArrayMath.getDataType(data);
            Array a = Array.factory(dt, new int[]{1});
            if (data instanceof String) {
                a.setString(0, (String)data);
            } else {
                a.setObject(0, data);
            }
            return a;
        }
    }

//    /**
//     * Create an array
//     *
//     * @param data Array like data
//     * @param dt Data type
//     * @return Array
//     */
//    public static Array array(ArrayList data, DataType dt) {
//        return array((List<Object>) data, dt);
//    }

    /**
     * Create an array
     *
     * @param data Array like data
     * @param dt Data type
     * @return Array
     */
    public static Array array_list(List data, DataType dt) {
        if (data.isEmpty()) {
            if (dt == null) {
                dt = DataType.DOUBLE;
            }
            return Array.factory(dt, new int[]{0});
        }
        Object d0 = data.get(0);
        if (d0 instanceof Number) {
            if (dt == null)
                dt = ArrayUtil.objectsToType(data);
            Array a = Array.factory(dt, new int[]{data.size()});
            for (int i = 0; i < data.size(); i++) {
                a.setObject(i, data.get(i));
            }
            return a;
        } else if (d0 instanceof String) {
            if (dt == null)
                dt = DataType.STRING;
            Array a = Array.factory(dt, new int[]{data.size()});
            for (int i = 0; i < data.size(); i++) {
                a.setString(i, (String)data.get(i));
            }
            return a;
        } else if (d0 instanceof Boolean) {
            Array a = Array.factory(DataType.BOOLEAN, new int[]{data.size()});
            //Array a = new ArrayBoolean(new int[]{data.size()});
            for (int i = 0; i < data.size(); i++) {
                a.setObject(i, data.get(i));
            }
            return a;
        } else if (d0 instanceof Complex) {
            Array a = Array.factory(DataType.OBJECT, new int[]{data.size()});
            Complex d;
            for (int i = 0; i < data.size(); i++) {
                d = (Complex) data.get(i);
                a.setObject(i, d);
            }
            return a;
        } else if (d0 instanceof List) {
            List<Integer> dims = new ArrayList<>();
            dims.add(data.size());
            dims.add(((List) d0).size());
            Object v = ((List) d0).get(0);
            if (v instanceof List) {
                d0 = v;
            }
            while (v instanceof List) {
                dims.add(((List) v).size());
                v = ((List) v).get(0);
                if (v instanceof List) {
                    d0 = v;
                } else {
                    break;
                }
            }
            if (dt == null)
                dt = ArrayUtil.objectsToType((List) d0);
            int ndim = dims.size();
            int[] shape = dims.stream().mapToInt(Integer::valueOf).toArray();
            Array a = Array.factory(dt, shape);
            Index index = a.getIndex();
            int[] counter;
            List vList;
            for (int i = 0; i < a.getSize(); i++) {
                counter = index.getCurrentCounter();
                vList = (List) data.get(counter[0]);
                if (ndim > 2) {
                    for (int j = 1; j < ndim - 1; j++) {
                        vList = (List) vList.get(counter[j]);
                    }
                }
                a.setObject(index, vList.get(counter[ndim - 1]));
                index.incr();
            }
            return a;
        } else {
            Array a = Array.factory(DataType.OBJECT, new int[]{data.size()});
            for (int i = 0; i < data.size(); i++) {
                a.setObject(i, data.get(i));
            }
            return a;
        }
    }

    /**
     * New view of array with the same data.
     *
     * @param a Input array
     * @return View array
     */
    public static Array view(Array a) {
        return Array.factory(a.getElementType(), a.getIndex(), a.getStorage());
    }

    /**
     * Array range
     *
     * @param start Start value
     * @param stop Stop value
     * @param step Step value
     * @return Array
     */
    public static Array arrayRange_bak(Number start, Number stop, final Number step) {
        if (stop == null) {
            stop = start;
            start = 0;
        }
        DataType dataType = ArrayUtil.objectsToType(new Object[]{
            start,
            stop,
            step});
        double startv = start.doubleValue();
        double stopv = stop.doubleValue();
        double stepv = step.doubleValue();
        List<Object> data = new ArrayList<>();
        if (dataType == DataType.FLOAT || dataType == DataType.DOUBLE) {
            while (startv < stopv) {
                data.add(startv);
                startv = BigDecimalUtil.add(startv, stepv);
            }
        } else {
            while (startv < stopv) {
                data.add(startv);
                startv += stepv;
            }
        }
        int length = data.size();
        Array a = Array.factory(dataType, new int[]{length});
        for (int i = 0; i < length; i++) {
            a.setObject(i, data.get(i));
        }
        return a;
    }

    /**
     * Array range
     *
     * @param start Start value
     * @param stop Stop value
     * @param step Step value
     * @return Array
     */
    public static Array arrayRange(Number start, Number stop, final Number step) {
        if (stop == null) {
            stop = start;
            start = 0;
        }
        DataType dataType = ArrayUtil.objectsToType(new Object[]{
            start,
            stop,
            step});
        double startv = start.doubleValue();
        double stopv = stop.doubleValue();
        double stepv = step.doubleValue();
        final int length = Math.max(0, (int) Math.ceil((stopv
                - startv) / stepv));
        Array a = Array.factory(dataType, new int[]{length});
        if (dataType == DataType.FLOAT || dataType == DataType.DOUBLE) {
            for (int i = 0; i < length; i++) {
                a.setObject(i, BigDecimalUtil.add(BigDecimalUtil.mul(i, stepv), startv));
            }
        } else {
            for (int i = 0; i < length; i++) {
                a.setObject(i, i * stepv + startv);
            }
        }
        return a;
    }

    /**
     * Array range
     *
     * @param start Start value
     * @param length Length
     * @param step Step value
     * @return Array
     */
    public static Array arrayRange1(Number start, final int length, final Number step) {
        DataType dataType = ArrayUtil.objectsToType(new Object[]{
            start,
            step});
        double startv = start.doubleValue();
        double stepv = step.doubleValue();
        Array a = Array.factory(dataType, new int[]{length});
        if (dataType == DataType.FLOAT || dataType == DataType.DOUBLE) {
            for (int i = 0; i < length; i++) {
                a.setObject(i, BigDecimalUtil.add(BigDecimalUtil.mul(i, stepv), startv));
            }
        } else {
            for (int i = 0; i < length; i++) {
                a.setObject(i, i * stepv + startv);
            }
        }
        return a;
    }

    /**
     * Array line space
     *
     * @param start Start value
     * @param stop Stop value
     * @param n Number value
     * @param endpoint If stop is included
     * @return Array
     */
    public static Array lineSpace(Number start, Number stop, final int n, boolean endpoint) {
        if (stop == null) {
            stop = start;
            start = 0;
        }
        double startv = start.doubleValue();
        double stopv = stop.doubleValue();
        int div = endpoint ? (n - 1) : n;
        double stepv = (stopv - startv) / div;
        Array a = Array.factory(DataType.DOUBLE, new int[]{n});
        double v = startv;
        for (int i = 0; i < n; i++) {
            a.setDouble(i, v);
            v += stepv;
        }
        if (endpoint) {
            if (a.getDouble(n - 1) != stopv) {
                a.setDouble(n - 1, stopv);
            }
        }

        return a;
    }

    /**
     * Array line space
     *
     * @param start Start value
     * @param stop Stop value
     * @param n Number value
     * @param endpoint If stop is included
     * @return Array
     */
    public static Array lineSpace_bak(Number start, Number stop, final int n, boolean endpoint) {
        if (stop == null) {
            stop = start;
            start = 0;
        }
        double startv = start.doubleValue();
        double stopv = stop.doubleValue();
        double stepv = (stopv - startv) / (n - 1);
        double endv = n * stepv + startv;
        int nn = n;
        if (endpoint) {
            if (endv < stopv) {
                nn += 1;
            }
        } else if (endv >= stopv) {
            nn -= 1;
        }
        Array a = Array.factory(DataType.FLOAT, new int[]{nn});
        for (int i = 0; i < nn; i++) {
            a.setObject(i, BigDecimalUtil.add(BigDecimalUtil.mul(i, stepv), startv));
        }

        return a;
    }

    /**
     * Get zero array
     *
     * @param n Number
     * @return Array
     */
    public static Array zeros(int n) {
        Array a = Array.factory(DataType.FLOAT, new int[]{n});
        for (int i = 0; i < n; i++) {
            a.setFloat(i, 0);
        }

        return a;
    }

    /**
     * Get zero array
     *
     * @param shape Shape
     * @param dtype Data type
     * @return Array Result array
     */
    public static Array zeros(List<Integer> shape, String dtype) {
        DataType dt = toDataType(dtype);
        return zeros(shape, dt);
    }

    /**
     * Get zero array
     *
     * @param shape Shape
     * @param dtype Data type
     * @return Array Result array
     */
    public static Array zeros(List<Integer> shape, DataType dtype) {
        int[] ashape = new int[shape.size()];
        for (int i = 0; i < shape.size(); i++) {
            ashape[i] = shape.get(i);
        }
        Array a = Array.factory(dtype, ashape);

        return a;
    }

    /**
     * Get zero array
     *
     * @param shape Shape
     * @param dtype Data type
     * @return Array Result array
     */
    public static Array empty(List<Integer> shape, DataType dtype) {
        int[] ashape = new int[shape.size()];
        for (int i = 0; i < shape.size(); i++) {
            ashape[i] = shape.get(i);
        }
        Array a = Array.factory(dtype, ashape);

        return a;
    }

    /**
     * Return a new array of given shape and type, filled with fill value.
     *
     * @param shape Shape
     * @param fillValue Fill value
     * @param dtype Data type
     * @return Array Result array
     */
    public static Array full(List<Integer> shape, Object fillValue, DataType dtype) {
        int[] ashape = new int[shape.size()];
        for (int i = 0; i < shape.size(); i++) {
            ashape[i] = shape.get(i);
        }
        if (dtype == null) {
            dtype = ArrayMath.getDataType(fillValue);
        }
        Array a = Array.factory(dtype, ashape);

        for (int i = 0; i < a.getSize(); i++) {
            a.setObject(i, fillValue);
        }

        return a;
    }

    /**
     * Get ones array
     *
     * @param n Number
     * @return Array Result array
     */
    public static Array ones(int n) {
        Array a = Array.factory(DataType.FLOAT, new int[]{n});
        for (int i = 0; i < n; i++) {
            a.setFloat(i, 1);
        }

        return a;
    }

    /**
     * Get ones array
     *
     * @param shape Shape
     * @param dtype Data type
     * @return Array Result array
     */
    public static Array ones(List<Integer> shape, DataType dtype) {
        int[] ashape = new int[shape.size()];
        for (int i = 0; i < shape.size(); i++) {
            ashape[i] = shape.get(i);
        }
        Array a = Array.factory(dtype, ashape);
        for (int i = 0; i < a.getSize(); i++) {
            a.setObject(i, 1);
        }

        return a;
    }

    /**
     * Return the identity array - a square array with ones on the main
     * diagonal.
     *
     * @param n Number of rows (and columns) in n x n output.
     * @param dtype Data type
     * @return Identity array
     */
    public static Array identity(int n, DataType dtype) {
        int[] shape = new int[]{n, n};
        Array a = Array.factory(dtype, shape);
        IndexIterator index = a.getIndexIterator();
        int[] current;
        while (index.hasNext()) {
            index.next();
            current = index.getCurrentCounter();
            if (current[0] == current[1]) {
                index.setObjectCurrent(1);
            } else {
                index.setObjectCurrent(0);
            }
        }

        return a;
    }

    /**
     * Return a 2-D array with ones on the diagonal and zeros elsewhere.
     *
     * @param n Number of rows in the output.
     * @param m Number of columns in the output.
     * @param k Index of the diagonal: 0 (the default) refers to the main
     * diagonal, a positive value refers to an upper diagonal, and a negative
     * value to a lower diagonal.
     * @param dtype Data type
     * @return Created array
     */
    public static Array eye(int n, int m, int k, DataType dtype) {
        int[] shape = new int[]{n, m};
        Array a = Array.factory(dtype, shape);
        IndexIterator index = a.getIndexIterator();
        int[] current;
        int i, j;
        while (index.hasNext()) {
            index.next();
            current = index.getCurrentCounter();
            i = current[0];
            j = current[1] - k;
            if (i == j) {
                index.setObjectCurrent(1);
            } else {
                index.setObjectCurrent(0);
            }
        }

        return a;
    }

    /**
     * Extract a diagonal or construct a diagonal array.
     *
     * @param a If a is a 2-D array, return a copy of its k-th diagonal. If a is
     * a 1-D array, return a 2-D array with a on the k-th diagonal.
     * @param k Diagonal in question.
     * @return Diagonal array
     */
    public static Array diag(Array a, int k) {
        if (a.getRank() == 2) {
            int m = a.getShape()[0];
            int n = a.getShape()[1];
            int len = Math.min(m, n) - Math.abs(k);
            Array r = Array.factory(a.getDataType(), new int[]{len});
            IndexIterator index = a.getIndexIterator();
            int[] current;
            int idx = 0, i, j;
            while (index.hasNext()) {
                index.next();
                current = index.getCurrentCounter();
                i = current[0];
                j = current[1] - k;
                if (i == j) {
                    r.setObject(idx, index.getObjectCurrent());
                    idx += 1;
                    if (idx == len) {
                        break;
                    }
                }
            }
            return r;
        } else {
            int m = a.getShape()[0];
            Array r = Array.factory(a.getDataType(), new int[]{m, m});
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < m; j++) {
                    if (i == j - k) {
                        r.setObject(i * m + j, a.getObject(i));
                    } else {
                        r.setObject(i * m + j, 0);
                    }
                }
            }
            return r;
        }
    }

    /**
     * Repeat a value n times
     *
     * @param v The value
     * @param n N times
     * @return Repeated array
     */
    public static Array repeat(Number v, int n) {
        DataType dt = ArrayMath.getDataType(v);
        Array r = Array.factory(dt, new int[]{n});
        for (int i = 0; i < n; i++) {
            r.setObject(i, v);
        }

        return r;
    }

    /**
     * Repeat elements of an array.
     *
     * @param a The value
     * @param repeats The number of repetitions for each element
     * @return Repeated array
     */
    public static Array repeat(Array a, List<Integer> repeats) {
        Array r;
        IndexIterator iterA = a.getIndexIterator();
        Object o;
        if (repeats.size() == 1) {
            int n = repeats.get(0);
            r = Array.factory(a.getDataType(), new int[]{(int) a.getSize() * n});
            int i = 0;
            while(iterA.hasNext()) {
                for (int j = 0; j < n; j++) {
                    o = iterA.getObjectNext();
                    r.setObject(i * n + j, o);
                }
                i += 1;
            }
        } else {
            int n = 0;
            for (int i = 0; i < repeats.size(); i++) {
                n += repeats.get(i);
            }
            r = Array.factory(a.getDataType(), new int[]{n});
            int idx = 0;
            int i = 0;
            while(iterA.hasNext()) {
                o = iterA.getObjectNext();
                for (int j = 0; j < repeats.get(i); j++) {
                    r.setObject(idx, o);
                    idx += 1;
                }
                i += 1;
            }
        }

        return r;
    }

    /**
     * Repeat elements of an array.
     *
     * @param a The value
     * @param repeats The number of repetitions for each element
     * @param axis The axis
     * @return Repeated array
     */
    public static Array repeat(Array a, List<Integer> repeats, int axis) {
        Array r;
        if (repeats.size() == 1) {
            int n = repeats.get(0);
            int[] shape = a.getShape();
            shape[axis] = shape[axis] * n;
            r = Array.factory(a.getDataType(), shape);
            Index aindex = a.getIndex();
            Index index = r.getIndex();
            int[] current;
            for (int i = 0; i < r.getSize(); i++) {
                current = index.getCurrentCounter();
                current[axis] = current[axis] / n;
                aindex.set(current);
                r.setObject(index, a.getObject(aindex));
                index.incr();
            }
        } else {
            int n = 0;
            int[] rsum = new int[repeats.size()];
            for (int i = 0; i < repeats.size(); i++) {
                rsum[i] = n;
                n += repeats.get(i);
            }
            int[] shape = a.getShape();
            shape[axis] = n;
            r = Array.factory(a.getDataType(), shape);
            Index aindex = a.getIndex();
            Index index = r.getIndex();
            int[] current;
            int idx;
            for (int i = 0; i < a.getSize(); i++) {
                current = aindex.getCurrentCounter();
                idx = current[axis];
                for (int j = 0; j < repeats.get(idx); j++) {
                    current[axis] = rsum[idx] + j;
                    index.set(current);
                    r.setObject(index, a.getObject(aindex));
                }
                aindex.incr();
            }
        }

        return r;
    }

    /**
     * Repeat a value n times
     *
     * @param v The value
     * @param n N times
     * @return Repeated array
     */
    public static Array tile(Number v, int n) {
        DataType dt = ArrayMath.getDataType(v);
        Array r = Array.factory(dt, new int[]{n});
        for (int i = 0; i < n; i++) {
            r.setObject(i, v);
        }

        return r;
    }

    /**
     * Repeat a value n times
     *
     * @param v The value
     * @param repeats The number of repetitions for each element
     * @return Repeated array
     */
    public static Array tile(Number v, List<Integer> repeats) {
        int[] shape = new int[repeats.size()];
        for (int i = 0; i < repeats.size(); i++) {
            shape[i] = repeats.get(i);
        }
        DataType dt = ArrayMath.getDataType(v);
        Array r = Array.factory(dt, shape);
        for (int i = 0; i < r.getSize(); i++) {
            r.setObject(i, v);
        }

        return r;
    }

    /**
     * Repeat elements of an array.
     *
     * @param a The value
     * @param repeats The number of repetitions for each element
     * @return Repeated array
     */
    public static Array tile(Array a, List<Integer> repeats) {
        if (a.getRank() > repeats.size()) {
            int n = a.getRank() - repeats.size();
            for (int i = 0; i < n; i++) {
                repeats.add(0, 1);
            }
        } else if (a.getRank() < repeats.size()) {
            int[] shape = a.getShape();
            int[] nshape = new int[repeats.size()];
            int n = repeats.size() - shape.length;
            for (int i = 0; i < nshape.length; i++) {
                if (i < n) {
                    nshape[i] = 1;
                } else {
                    nshape[i] = shape[i - n];
                }
            }
            a = a.reshape(nshape);
        }
        int[] ashape = a.getShape();
        int[] shape = a.getShape();
        for (int i = 0; i < shape.length; i++) {
            shape[i] = shape[i] * repeats.get(i);
        }
        Array r = Array.factory(a.getDataType(), shape);
        Index index = r.getIndex();
        Index aindex = a.getIndex();
        int[] current;
        int idx;
        for (int i = 0; i < r.getSize(); i++) {
            current = index.getCurrentCounter();
            for (int j = 0; j < repeats.size(); j++) {
                idx = current[j];
                idx = idx % ashape[j];
                current[j] = idx;
            }
            aindex.set(current);
            r.setObject(index, a.getObject(aindex));
            index.incr();
        }

        return r;
    }

    /**
     * Get random value
     *
     * @return Random value
     */
    public static double rand() {
        Random r = new Random();
        return r.nextDouble();
    }

    /**
     * Get random array - one dimension
     *
     * @param n Array length
     * @return Result array
     */
    public static Array rand(int n) {
        Array r = Array.factory(DataType.DOUBLE, new int[]{n});
        Random rd = new Random();
        for (int i = 0; i < r.getSize(); i++) {
            r.setDouble(i, rd.nextDouble());
        }

        return r;
    }

    /**
     * Get random array
     *
     * @param shape Shape
     * @return Array Result array
     */
    public static Array rand(List<Integer> shape) {
        int[] ashape = new int[shape.size()];
        for (int i = 0; i < shape.size(); i++) {
            ashape[i] = shape.get(i);
        }
        Array a = Array.factory(DataType.DOUBLE, ashape);
        Random rd = new Random();
        for (int i = 0; i < a.getSize(); i++) {
            a.setDouble(i, rd.nextDouble());
        }

        return a;
    }

    private static DataType objectsToType(final Object[] objects) {
        if (objects.length == 0) {
            return DataType.INT;
        }
        short new_sz, sz = -1;
        DataType dataType = DataType.INT;
        for (final Object o : objects) {
            final DataType _type = ArrayMath.getDataType(o);
            new_sz = ArrayMath.typeToNBytes(_type);
            if (new_sz > sz) {
                dataType = _type;
                sz = new_sz;
            }
        }
        return dataType;
    }

    private static DataType objectsToType(final List<Object> objects) {
        if (objects.isEmpty()) {
            return DataType.INT;
        }
        short new_sz, sz = -1;
        DataType dataType = DataType.INT;
        for (final Object o : objects) {
            final DataType _type = ArrayMath.getDataType(o);
            new_sz = ArrayMath.typeToNBytes(_type);
            if (new_sz > sz) {
                dataType = _type;
                sz = new_sz;
            }
        }
        return dataType;
    }

    /**
     * Merge data type to one data type
     *
     * @param dt1 Data type 1
     * @param dt2 Data type 2
     * @return Merged data type
     */
    public static DataType mergeDataType(DataType dt1, DataType dt2) {
        if (dt1 == DataType.OBJECT || dt2 == DataType.OBJECT) {
            return DataType.OBJECT;
        } else if (dt1 == DataType.STRING || dt2 == DataType.STRING) {
            return DataType.STRING;
        } else if (dt1 == DataType.DOUBLE || dt2 == DataType.DOUBLE) {
            return DataType.DOUBLE;
        } else if (dt1 == DataType.FLOAT || dt2 == DataType.FLOAT) {
            return DataType.FLOAT;
        } else {
            return dt1;
        }
    }

    // </editor-fold>
    // <editor-fold desc="Output">
    /**
     * Array to string
     *
     * @param a Array a
     * @return String
     */
    public static String convertToString(Array a) {
        if (a.getDataType() == DataType.STRUCTURE)
            return a.toString();

        StringBuilder sbuff = new StringBuilder();
        sbuff.append("array(");
        int ndim = a.getRank();
        if (ndim > 1) {
            sbuff.append("[");
        }
        int i = 0, n = 0;
        IndexIterator ii = a.getIndexIterator();
        int shapeIdx = ndim - 1;
        if (shapeIdx < 0) {
            sbuff.append("[");
            sbuff.append(ii.getObjectNext());
            sbuff.append("])");
            return sbuff.toString();
        }

        int len = a.getShape()[shapeIdx];
        String dstr;
        while (ii.hasNext()) {
            if (i == 0) {
                if (n > 0) {
                    sbuff.append("\n      ");
                }
                sbuff.append("[");
            }
            dstr = ii.getStringNext();
            if (a.getDataType().isBoolean()) {
                dstr = GlobalUtil.capitalize(dstr);
            } 
            if (a.getDataType().isString()) {
                sbuff.append("'");
                sbuff.append(dstr);
                sbuff.append("'");
            } else
                sbuff.append(dstr);
            i += 1;
            if (i == len) {
                sbuff.append("]");
                len = a.getShape()[shapeIdx];
                i = 0;
            } else {
                sbuff.append(", ");
            }
            n += 1;
            if (n > 200) {
                sbuff.append("...]");
                break;
            }
        }
        if (ndim > 1) {
            sbuff.append("]");
        }
        sbuff.append(")");
        return sbuff.toString();
    }

    /**
     * Array to string
     *
     * @param a Array a
     * @return String
     */
    public static String toString_old(Array a) {
        StringBuilder sbuff = new StringBuilder();
        sbuff.append("array(");
        int ndim = a.getRank();
        if (ndim > 1) {
            sbuff.append("[");
        }
        int i = 0;
        int shapeIdx = ndim - 1;
        int len = a.getShape()[shapeIdx];
        IndexIterator ii = a.getIndexIterator();
        while (ii.hasNext()) {
            if (i == 0) {
                sbuff.append("[");
            }
            Object data = ii.getObjectNext();
            sbuff.append(data);
            i += 1;
            if (i == len) {
                sbuff.append("]");
                len = a.getShape()[shapeIdx];
                i = 0;
            } else {
                sbuff.append(", ");
            }
        }
        if (ndim > 1) {
            sbuff.append("]");
        }
        return sbuff.toString();
    }

    // </editor-fold>
    // <editor-fold desc="Convert/Sort">
    /**
     * Get data type string
     *
     * @param dt The data type
     * @return Data type string
     */
    public static String dataTypeString(DataType dt) {
        String str = "string";
        switch (dt) {
            case BYTE:
                str = "byte";
                break;
            case SHORT:
                str = "short";
                break;
            case INT:
                str = "int";
                break;
            case FLOAT:
                str = "float";
                break;
            case DOUBLE:
                str = "double";
                break;
        }

        return str;
    }

    /**
     * To data type - ucar.ma2
     *
     * @param dt Data type string
     * @return Data type
     */
    public static DataType toDataType(String dt) {
        if (dt.contains("%")) {
            dt = dt.split("%")[1];
        }
        switch (dt.toLowerCase()) {
            case "c":
            case "s":
            case "string":
                return DataType.STRING;
            case "b":
            case "byte":
                return DataType.BYTE;
            case "short":
                return DataType.SHORT;
            case "i":
            case "int":
                return DataType.INT;
            case "f":
            case "float":
                return DataType.FLOAT;
            case "d":
            case "double":
                return DataType.DOUBLE;
            case "bool":
            case "boolean":
                return DataType.BOOLEAN;
            default:
                return DataType.OBJECT;
        }
    }

    /**
     * Convert array to integer type
     *
     * @param a Array a
     * @return Result array
     */
    public static Array toInteger(Array a) {
        Array r = Array.factory(DataType.INT, a.getShape());
        IndexIterator iterA = a.getIndexIterator();
        IndexIterator iterR = r.getIndexIterator();
        if (a.getDataType().isNumeric()) {
            while (iterA.hasNext()) {
                iterR.setIntNext(iterA.getIntNext());
            }
        } else {
            if (a.getDataType() == DataType.BOOLEAN) {
                while(iterA.hasNext()) {
                    iterR.setIntNext(iterA.getBooleanNext() ? 1 : 0);
                }
            } else {
                while(iterA.hasNext()) {
                    iterR.setIntNext(Integer.valueOf(iterA.getObjectNext().toString().trim()));
                }
            }
        }

        return r;
    }

    /**
     * Convert array to float type
     *
     * @param a Array a
     * @return Result array
     */
    public static Array toFloat(Array a) {
        Array r = Array.factory(DataType.FLOAT, a.getShape());
        IndexIterator iterA = a.getIndexIterator();
        IndexIterator iterR = r.getIndexIterator();
        if (a.getDataType().isNumeric()) {
            while(iterA.hasNext()) {
                iterR.setFloatNext(iterA.getFloatNext());
            }
        } else {
            while(iterA.hasNext()) {
                iterR.setFloatNext(Float.valueOf(iterA.getObjectNext().toString()));
            }
        }

        return r;
    }

    /**
     * Convert array to double type
     *
     * @param a Array a
     * @return Result array
     */
    public static Array toDouble(Array a) {
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        IndexIterator iterA = a.getIndexIterator();
        IndexIterator iterR = r.getIndexIterator();
        if (a.getDataType().isNumeric()) {
            while(iterA.hasNext()) {
                iterR.setDoubleNext(iterA.getDoubleNext());
            }
        } else if (a.getDataType() == DataType.DATE) {
            while (iterA.hasNext()) {
                iterR.setDoubleNext(JDateUtil.toOADate(iterA.getDateNext()));
            }
        } else {
            while(iterA.hasNext()) {
                iterR.setDoubleNext(Double.valueOf(iterA.getObjectNext().toString()));
            }
        }

        return r;
    }

    /**
     * Convert array to boolean type
     *
     * @param a Array a
     * @return Result array
     */
    public static Array toBoolean(Array a) {
        Array r = Array.factory(DataType.BOOLEAN, a.getShape());
        IndexIterator iterA = a.getIndexIterator();
        IndexIterator iterR = r.getIndexIterator();
        while(iterA.hasNext()) {
            iterR.setBooleanNext(iterA.getDoubleNext() != 0);
        }

        return r;
    }

    /**
     * Convert array to date type
     * @param a Array a
     * @return Result array
     */
    public static Array toDate(Array a) {
        Array r = Array.factory(DataType.DATE, a.getShape());
        IndexIterator iterA = a.getIndexIterator();
        IndexIterator iterR = r.getIndexIterator();
        while(iterA.hasNext()) {
            iterR.setDateNext(JDateUtil.fromOADate(iterA.getDoubleNext()));
        }

        return r;
    }

    /**
     * Concatenate arrays to one array along a axis
     *
     * @param arrays Array list
     * @param axis The axis
     * @return Concatenated array
     * @throws org.meteoinfo.ndarray.InvalidRangeException
     */
    public static Array concatenate(List<Array> arrays, Integer axis) throws InvalidRangeException {
        int ndim = arrays.get(0).getRank();
        if (axis == -1) {
            axis = ndim - 1;
        }
        int len = 0;
        int[] lens = new int[arrays.size()];
        int i = 0;
        List<Index> indexList = new ArrayList<>();
        for (Array a : arrays) {
            len += a.getShape()[axis];
            lens[i] = len;
            indexList.add(a.getIndex());
            i += 1;
        }
        int[] shape = arrays.get(0).getShape();
        shape[axis] = len;
        Array r = Array.factory(arrays.get(0).getDataType(), shape);
        int[] current;
        IndexIterator ii = r.getIndexIterator();
        Index index;
        int idx = 0;
        while (ii.hasNext()) {
            ii.next();
            current = ii.getCurrentCounter();
            for (i = 0; i < lens.length; i++) {
                if (current[axis] < lens[i]) {
                    idx = i;
                    break;
                }
            }
            if (idx > 0) {
                current[axis] = current[axis] - lens[idx - 1];
            }
            index = indexList.get(idx);
            index.set(current);
            ii.setObjectCurrent(arrays.get(idx).getObject(index));
        }

        return r;
    }

    /**
     * Concatenate two arrays to one array along a axis
     *
     * @param a Array a
     * @param b Array b
     * @param axis The axis
     * @return Concatenated array
     * @throws org.meteoinfo.ndarray.InvalidRangeException
     */
    public static Array concatenate(Array a, Array b, Integer axis) throws InvalidRangeException {
        int n = a.getRank();
        int[] shape = a.getShape();
        if (axis == -1) {
            axis = n - 1;
        }
        int nn = shape[axis];
        int[] bshape = b.getShape();
        int[] nshape = new int[n];
        for (int i = 0; i < n; i++) {
            if (i == axis) {
                nshape[i] = shape[i] + bshape[i];
            } else {
                nshape[i] = shape[i];
            }
        }
        Array r = Array.factory(a.getDataType(), nshape);
        Index indexr = r.getIndex();
        Index indexa = a.getIndex();
        Index indexb = b.getIndex();
        int[] current;
        for (int i = 0; i < r.getSize(); i++) {
            current = indexr.getCurrentCounter();
            if (current[axis] < nn) {
                indexa.set(current);
                r.setObject(indexr, a.getObject(indexa));
            } else {
                current[axis] = current[axis - nn];
                indexb.set(current);
                r.setObject(indexr, b.getObject(indexb));
            }
            indexr.incr();
        }

        return r;
    }

    public static List<Array> arraySplit(Array a, int sections, int axis) {
        int[] shape = a.getShape();
        if (axis == -1) {
            axis = a.getRank() - 1;
        }
        int n = shape[axis];
        int sn = n / sections;
        List<Array> arrays = new ArrayList<>();


        return arrays;
    }

    /**
     * Sort array along an axis
     *
     * @param a Array a
     * @param axis The axis
     * @return Sorted array
     * @throws InvalidRangeException
     */
    public static Array sort(Array a, Integer axis) throws InvalidRangeException {
        int n = a.getRank();
        int[] shape = a.getShape();
        if (axis == null) {
            int[] nshape = new int[1];
            nshape[0] = (int) a.getSize();
            Array r = Array.factory(a.getDataType(), nshape);
            List tlist = new ArrayList();
            IndexIterator ii = a.getIndexIterator();
            while (ii.hasNext()) {
                tlist.add(ii.getObjectNext());
            }
            Collections.sort(tlist);
            for (int i = 0; i < r.getSize(); i++) {
                r.setObject(i, tlist.get(i));
            }

            return r;
        } else {
            if (axis == -1) {
                axis = n - 1;
            }
            int nn = shape[axis];
            Array r = Array.factory(a.getDataType(), shape);
            Index indexr = r.getIndex();
            int[] current;
            List<Range> ranges = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                if (i == axis) {
                    ranges.add(new Range(0, 0, 1));
                } else {
                    ranges.add(new Range(0, shape[i] - 1, 1));
                }
            }
            IndexIterator rii = r.sectionNoReduce(ranges).getIndexIterator();
            while (rii.hasNext()) {
                rii.next();
                current = rii.getCurrentCounter();
                ranges = new ArrayList<>();
                for (int j = 0; j < n; j++) {
                    if (j == axis) {
                        ranges.add(new Range(0, shape[j] - 1, 1));
                    } else {
                        ranges.add(new Range(current[j], current[j], 1));
                    }
                }
                List tlist = new ArrayList();
                IndexIterator ii = a.getRangeIterator(ranges);
                while (ii.hasNext()) {
                    tlist.add(ii.getObjectNext());
                }
                Collections.sort(tlist);
                for (int j = 0; j < nn; j++) {
                    indexr.set(current);
                    r.setObject(indexr, tlist.get(j));
                    current[axis] = current[axis] + 1;
                }
            }

            return r;
        }
    }

    /**
     * Get sorted array index along an axis
     *
     * @param a Array a
     * @param axis The axis
     * @return Index of sorted array
     * @throws InvalidRangeException
     */
    public static Array argSort(Array a, Integer axis) throws InvalidRangeException {
        int n = a.getRank();
        int[] shape = a.getShape();
        Object v;
        if (axis == null) {
            int[] nshape = new int[1];
            nshape[0] = (int) a.getSize();
            Array r = Array.factory(DataType.INT, nshape);
            List stlist = new ArrayList();
            IndexIterator ii = a.getIndexIterator();
            while (ii.hasNext()) {
                v = ii.getObjectNext();
                stlist.add(v);
            }
            //Collections.sort(stlist);
            ListIndexComparator comparator = new ListIndexComparator(stlist);
            Integer[] indexes = comparator.createIndexArray();
            Arrays.sort(indexes, comparator);
            for (int i = 0; i < r.getSize(); i++) {
                r.setInt(i, indexes[i]);
            }

            return r;
        } else {
            if (axis == -1) {
                axis = n - 1;
            }
            int nn = shape[axis];
            Array r = Array.factory(DataType.INT, shape);
            Index indexr = r.getIndex();
            int[] current;
            List<Range> ranges = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                if (i == axis) {
                    ranges.add(new Range(0, 0, 1));
                } else {
                    ranges.add(new Range(0, shape[i] - 1, 1));
                }
            }
            IndexIterator rii = r.sectionNoReduce(ranges).getIndexIterator();
            while (rii.hasNext()) {
                rii.next();
                current = rii.getCurrentCounter();
                ranges = new ArrayList<>();
                for (int j = 0; j < n; j++) {
                    if (j == axis) {
                        ranges.add(new Range(0, shape[j] - 1, 1));
                    } else {
                        ranges.add(new Range(current[j], current[j], 1));
                    }
                }
                List stlist = new ArrayList();
                IndexIterator ii = a.getRangeIterator(ranges);
                while (ii.hasNext()) {
                    v = ii.getObjectNext();
                    stlist.add(v);
                }
                //Collections.sort(stlist);
                ListIndexComparator comparator = new ListIndexComparator(stlist);
                Integer[] indexes = comparator.createIndexArray();
                Arrays.sort(indexes, comparator);
                for (int j = 0; j < nn; j++) {
                    indexr.set(current);
                    r.setObject(indexr, indexes[j]);
                    current[axis] = current[axis] + 1;
                }
            }

            return r;
        }
    }

    /**
     * Find the unique elements of an array.
     *
     * @param a Array a
     * @param axis The axis
     * @return  The sorted unique elements of an array
     * @throws InvalidRangeException
     */
    public static Array unique(Array a, Integer axis) throws InvalidRangeException {
        int n = a.getRank();
        int[] shape = a.getShape();
        if (axis == null) {
            List tList = new ArrayList();
            IndexIterator ii = a.getIndexIterator();
            Object o;
            while (ii.hasNext()) {
                o = ii.getObjectNext();
                if (!tList.contains(o))
                    tList.add(o);
            }
            Collections.sort(tList);
            int[] nShape = new int[]{tList.size()};
            Array r = Array.factory(a.getDataType(), nShape);
            for (int i = 0; i < r.getSize(); i++) {
                r.setObject(i, tList.get(i));
            }

            return r;
        } else {
            if (axis == -1) {
                axis = n - 1;
            }
            List<Array> tList = new ArrayList();
            Array ta;
            int nn = shape[axis];
            shape[axis] = 1;
            int[] origin = new int[shape.length];
            for (int i = 0; i < nn; i++) {
                origin[axis] = i;
                ta = a.section(origin, shape);
                if (!tList.contains(ta)) {
                    tList.add(ta);
                }
            }
            shape[axis] = tList.size();
            Array r = Array.factory(a.getDataType(), shape);
            List<Range> ranges = new ArrayList<>();
            for (int i = 0; i < shape.length; i++) {
                ranges.add(new Range(0, shape[i] - 1, 1));
            }
            Range range = ranges.get(axis);
            for (int i = 0; i < tList.size(); i++) {
                range = new Range(i, i, 1);
                ranges.set(i, range);
                ArrayMath.setSection(r, ranges, tList.get(i));
            }
            return r;
        }
    }

    /**
     * Convert array to N-Dimension double Java array
     *
     * @param a Array a
     * @param dtype Data type string
     * @return N-D Java array
     */
    public static Object copyToNDJavaArray(Array a, String dtype) {
        if (dtype == null) {
            return copyToNDJavaArray(a);
        }

        switch (dtype.toLowerCase()) {
            case "double":
                return copyToNDJavaArray_Double(a);
            case "long":
                return copyToNDJavaArray_Long(a);
            default:
                return copyToNDJavaArray(a);
        }
    }

    /**
     * Convert array to N-Dimension double Java array
     *
     * @param a Array a
     * @return N-D Java array
     */
    public static Object copyToNDJavaArray(Array a) {
        Object javaArray;
        try {
            javaArray = java.lang.reflect.Array.newInstance(a.getDataType().getPrimitiveClassType(), a.getShape());
        } catch (IllegalArgumentException | NegativeArraySizeException e) {
            throw new IllegalArgumentException(e);
        }
        IndexIterator iter = a.getIndexIterator();
        reflectArrayCopyOut(javaArray, a, iter);

        return javaArray;
    }

    /**
     * Convert array to N-Dimension double Java array
     *
     * @param a Array a
     * @return N-D Java array
     */
    public static Object copyToNDJavaArray_Long(Array a) {
        Object javaArray;
        try {
            javaArray = java.lang.reflect.Array.newInstance(Long.TYPE, a.getShape());
        } catch (IllegalArgumentException | NegativeArraySizeException e) {
            throw new IllegalArgumentException(e);
        }
        IndexIterator iter = a.getIndexIterator();
        reflectArrayCopyOut(javaArray, a, iter);

        return javaArray;
    }

    /**
     * Convert array to N-Dimension double Java array
     *
     * @param a Array a
     * @return N-D Java array
     */
    public static Object copyToNDJavaArray_Double(Array a) {
        Object javaArray;
        try {
            javaArray = java.lang.reflect.Array.newInstance(Double.TYPE, a.getShape());
        } catch (IllegalArgumentException | NegativeArraySizeException e) {
            throw new IllegalArgumentException(e);
        }
        IndexIterator iter = a.getIndexIterator();
        reflectArrayCopyOut(javaArray, a, iter);

        return javaArray;
    }

    private static void reflectArrayCopyOut(Object jArray, Array aa, IndexIterator aaIter) {
        Class cType = jArray.getClass().getComponentType();

        if (!cType.isArray()) {
            if (cType == long.class) {
                copyTo1DJavaArray_Long(aaIter, jArray);
            } else {
                copyTo1DJavaArray(aaIter, jArray);
            }
        } else {
            for (int i = 0; i < java.lang.reflect.Array.getLength(jArray); i++) {
                reflectArrayCopyOut(java.lang.reflect.Array.get(jArray, i), aa, aaIter);
            }
        }
    }

    protected static void copyTo1DJavaArray(IndexIterator iter, Object javaArray) {
        for (int i = 0; i < java.lang.reflect.Array.getLength(javaArray); i++) {
            java.lang.reflect.Array.set(javaArray, i, iter.getObjectNext());
        }
    }

    protected static void copyTo1DJavaArray_Long(IndexIterator iter, Object javaArray) {
        long[] ja = (long[]) javaArray;
        for (int i = 0; i < ja.length; i++) {
            ja[i] = iter.getLongNext();
        }
    }

    /**
     * Return a new array with sub-arrays along an axis deleted
     *
     * @param a Input array
     * @param idx Index
     * @param axis The axis
     * @return
     */
    public static Array delete(Array a, int idx, int axis) {
        int[] shape = a.getShape();
        int n = shape.length;
        int[] nshape = new int[n];
        for (int i = 0; i < n; i++) {
            if (i == axis) {
                nshape[i] = shape[i] - 1;
            } else {
                nshape[i] = shape[i];
            }
        }
        Array r = Array.factory(a.getDataType(), nshape);
        IndexIterator ii = a.getIndexIterator();
        int[] current;
        int i = 0;
        while (ii.hasNext()) {
            ii.next();
            current = ii.getCurrentCounter();
            if (current[axis] != idx) {
                r.setObject(i, ii.getObjectCurrent());
                i += 1;
            }
        }

        return r;
    }

    /**
     * Return a new array with sub-arrays along an axis deleted
     *
     * @param a Input array
     * @param idx Index
     * @param axis The axis
     * @return
     */
    public static Array delete(Array a, List<Integer> idx, int axis) {
        int[] shape = a.getShape();
        int n = shape.length;
        int[] nshape = new int[n];
        for (int i = 0; i < n; i++) {
            if (i == axis) {
                nshape[i] = shape[i] - idx.size();
            } else {
                nshape[i] = shape[i];
            }
        }
        Array r = Array.factory(a.getDataType(), nshape);
        IndexIterator ii = a.getIndexIterator();
        int[] current;
        int i = 0;
        while (ii.hasNext()) {
            ii.next();
            current = ii.getCurrentCounter();
            if (!idx.contains(current[axis])) {
                r.setObject(i, ii.getObjectCurrent());
                i += 1;
            }
        }

        return r;
    }

    // </editor-fold>
    // <editor-fold desc="Statistics">
    /**
     * Histogram x/y array
     *
     * @param a Data array
     * @param nbins bin number
     * @return X/Y arrays
     */
    public static List<Array> histogram(Array a, int nbins) {
        double min = ArrayMath.getMinimum(a);
        double max = ArrayMath.getMaximum(a);
        double interval = BigDecimalUtil.div(BigDecimalUtil.sub(max, min), nbins);
        double[] bins = new double[nbins + 1];
        for (int i = 0; i < nbins + 1; i++) {
            bins[i] = min;
            min = BigDecimalUtil.add(min, interval);
        }
        Array ba = Array.factory(DataType.DOUBLE, new int[]{bins.length}, bins);
        return histogram(a, ba);
    }

    /**
     * Histogram x/y array
     *
     * @param a Data array
     * @param bins bin edges
     * @return X/Y arrays
     */
    public static List<Array> histogram(Array a, Array bins) {
        int n = (int) bins.getSize();
        Array hist = Array.factory(DataType.INT, new int[]{n - 1});
        double v;
        IndexIterator iterA = a.getIndexIterator();
        while(iterA.hasNext()) {
            v = iterA.getDoubleNext();
            for (int j = 0; j < n - 1; j++) {
                if (j == n - 2) {
                    if (v >= bins.getDouble(j) && v <= bins.getDouble(j + 1)) {
                        hist.setInt(j, hist.getInt(j) + 1);
                        break;
                    }
                } else if (v >= bins.getDouble(j) && v < bins.getDouble(j + 1)) {
                    hist.setInt(j, hist.getInt(j) + 1);
                    break;
                }
            }
        }

        List<Array> r = new ArrayList<>();
        r.add(hist);
        r.add(bins);

        return r;
    }

    /**
     * Histogram x/y array
     *
     * @param a Data array
     * @param bins bin edges
     * @return X/Y arrays
     */
    public static List<Array> histogram(Array a, double[] bins) {
        int n = bins.length;
        double delta = bins[1] - bins[0];
        int[] count = new int[n + 1];
        double v;
        IndexIterator iterA = a.getIndexIterator();
        while(iterA.hasNext()) {
            v = iterA.getDoubleNext();
            if (v < bins[0]) {
                count[0] += 1;
            } else if (v > bins[n - 1]) {
                count[n] += 1;
            } else {
                for (int j = 0; j < n - 1; j++) {
                    if (v > bins[j] && v < bins[j + 1]) {
                        count[j + 1] += 1;
                        break;
                    }
                }
            }
        }

        Array x = Array.factory(DataType.DOUBLE, new int[]{count.length + 1});
        Array y = Array.factory(DataType.INT, new int[]{count.length});
        for (int i = 0; i < count.length; i++) {
            y.setInt(i, count[i]);
            if (i == 0) {
                x.setDouble(0, bins[0] - delta);
                x.setDouble(1, bins[0]);
            } else if (i == count.length - 1) {
                x.setDouble(i + 1, bins[i - 1] + delta);
            } else {
                x.setDouble(i + 1, bins[i]);
            }
        }
        List<Array> r = new ArrayList<>();
        r.add(y);
        r.add(x);

        return r;
    }

    // </editor-fold>
    // <editor-fold desc="Resample/Interpolate">
    /**
     * Broadcast array to a new shape
     *
     * @param a Array a
     * @param shape Shape
     * @return Result array
     */
    public static Array broadcast(Array a, int[] shape) {
        int[] bshape = a.getShape();
        if (bshape.length > shape.length) {
            return null;
        }

        if (bshape.length < shape.length) {
            int miss = shape.length - a.getRank();
            bshape = new int[shape.length];
            for (int i = 0; i < shape.length; i++) {
                if (i < miss) {
                    bshape[i] = 1;
                } else {
                    bshape[i] = a.getShape()[i - miss];
                }
            }
            a = a.reshape(bshape);
        }

        //Check
        boolean pass = true;
        for (int i = 0; i < shape.length; i++) {
            if (shape[i] != bshape[i] && bshape[i] != 1) {
                pass = false;
                break;
            }
        }
        if (!pass) {
            return null;
        }

        //Broadcast
        Index aindex = a.getIndex();
        Array r = Array.factory(a.getDataType(), shape);
        Index index = r.getIndex();
        int[] current;
        for (int i = 0; i < r.getSize(); i++) {
            current = index.getCurrentCounter();
            for (int j = 0; j < shape.length; j++) {
                if (bshape[j] == 1) {
                    aindex.setDim(j, 0);
                } else {
                    aindex.setDim(j, current[j]);
                }
            }
            r.setObject(index, a.getObject(aindex));
            index.incr();
        }

        return r;
    }

    /**
     * Broadcast array to a new shape
     *
     * @param a Array a
     * @param shape Shape
     * @return Result array
     */
    public static Array broadcast(Array a, List<Integer> shape) {
        int[] nshape = new int[shape.size()];
        for (int i = 0; i < shape.size(); i++) {
            nshape[i] = shape.get(i);
        }
        return broadcast(a, nshape);
    }

    /**
     * Mesh grid
     *
     * @param x X array - vector
     * @param y Y array - vector
     * @return Result arrays - matrix
     */
    public static Array[] meshgrid(Array x, Array y) {
        int xn = (int) x.getSize();
        int yn = (int) y.getSize();
        int[] shape = new int[]{yn, xn};
        Array rx = Array.factory(x.getDataType(), shape);
        Array ry = Array.factory(y.getDataType(), shape);        
        IndexIterator iterY = y.getIndexIterator();
        Object xv, yv;
        for (int i = 0; i < yn; i++) {
            yv = iterY.getObjectNext();
            IndexIterator iterX = x.getIndexIterator();
            for (int j = 0; j < xn; j++) {
                xv = iterX.getObjectNext();
                rx.setObject(i * xn + j, xv);
                ry.setObject(i * xn + j, yv);
            }
        }

        return new Array[]{rx, ry};
    }

    /**
     * Mesh grid
     *
     * @param xs X arrays
     * @return Result arrays - matrix
     */
    public static Array[] meshgrid(Array... xs) {
        int n = xs.length;
        int[] shape = new int[n];
        int i = 0;
        Array x;
        for (i = 0; i < n; i++) {
            x = xs[i];
            shape[n - i - 1] = (int) x.getSize();
        }

        Array[] rs = new Array[n];
        Array r;
        int idx;
        for (int s = 0; s < n; s++) {
            x = xs[s];
            r = Array.factory(xs[s].getDataType(), shape);
            Index index = r.getIndex();
            Index xIndex = x.getIndex();
            for (i = 0; i < r.getSize(); i++) {
                idx = index.getCurrentCounter()[n - s - 1];
                r.setObject(index, x.getObject(xIndex.set0(idx)));
                index.incr();
            }
            rs[s] = r;
        }

        return rs;
    }

    /**
     * Smooth with 5 points
     *
     * @param a Array
     * @param rowNum Row number
     * @param colNum Column number
     * @param unDefData Missing value
     * @return Result array
     */
    public static Array smooth5(Array a, int rowNum, int colNum, double unDefData) {
        Array r = Array.factory(a.getDataType(), a.getShape());
        double s = 0.5;
        if (!a.getIndexPrivate().isFastIterator()) {
            a = a.copy();
        }
        if (Double.isNaN(unDefData)) {
            for (int i = 0; i < rowNum; i++) {
                for (int j = 0; j < colNum; j++) {
                    if (i == 0 || i == rowNum - 1 || j == 0 || j == colNum - 1) {
                        r.setDouble(i * colNum + j, a.getDouble(i * colNum + j));
                    } else {
                        if (Double.isNaN(a.getDouble(i * colNum + j)) || Double.isNaN(a.getDouble((i + 1) * colNum + j)) || Double.isNaN(a.getDouble((i - 1) * colNum + j))
                                || Double.isNaN(a.getDouble(i * colNum + j + 1)) || Double.isNaN(a.getDouble(i * colNum + j - 1))) {
                            r.setDouble(i * colNum + j, a.getDouble(i * colNum + j));
                            continue;
                        }
                        r.setDouble(i * colNum + j, a.getDouble(i * colNum + j) + s / 4 * (a.getDouble((i + 1) * colNum + j) + a.getDouble((i - 1) * colNum + j) + a.getDouble(i * colNum + j + 1)
                                + a.getDouble(i * colNum + j - 1) - 4 * a.getDouble(i * colNum + j)));
                    }
                }
            }
        } else {
            for (int i = 0; i < rowNum; i++) {
                for (int j = 0; j < colNum; j++) {
                    if (i == 0 || i == rowNum - 1 || j == 0 || j == colNum - 1) {
                        r.setDouble(i * colNum + j, a.getDouble(i * colNum + j));
                    } else {
                        if (a.getDouble(i * colNum + j) == unDefData || a.getDouble((i + 1) * colNum + j) == unDefData || a.getDouble((i - 1) * colNum + j)
                                == unDefData || a.getDouble(i * colNum + j + 1) == unDefData || a.getDouble(i * colNum + j - 1) == unDefData) {
                            r.setDouble(i * colNum + j, a.getDouble(i * colNum + j));
                            continue;
                        }
                        r.setDouble(i * colNum + j, a.getDouble(i * colNum + j) + s / 4 * (a.getDouble((i + 1) * colNum + j) + a.getDouble((i - 1) * colNum + j) + a.getDouble(i * colNum + j + 1)
                                + a.getDouble(i * colNum + j - 1) - 4 * a.getDouble(i * colNum + j)));
                    }
                }
            }
        }

        return r;
    }

    /**
     * Smooth with 5 points
     *
     * @param a Array
     * @return Result array
     */
    public static Array smooth5(Array a) {
        int[] shape = a.getShape();
        int colNum = shape[1];
        int rowNum = shape[0];
        Array r = Array.factory(a.getDataType(), shape);
        if (!a.getIndexPrivate().isFastIterator()) {
            a = a.copy();
        }
        Index2D index = new Index2D(shape);
        double v, w;
        double sum, wsum;
        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                sum = 0;
                wsum = 0;
                for (int ii = i - 1; ii <= i + 1; ii++) {
                    if (ii < 0 || ii >= rowNum) {
                        continue;
                    }
                    for (int jj = j - 1; jj <= j + 1; jj++) {
                        if (jj < 0 || jj >= colNum) {
                            continue;
                        }
                        if ((ii == i - 1 || ii == i + 1) && jj != j) {
                            continue;
                        }
                        v = a.getDouble(index.set(ii, jj));
                        if (!Double.isNaN(v)) {
                            if (ii == i && jj == j) {
                                w = 1;
                            } else {
                                w = 0.5;
                            }
                            sum += v * w;
                            wsum += w;
                        }
                    }
                }
                index.set(i, j);
                if (wsum > 0) {
                    r.setDouble(index, sum / wsum);
                } else {
                    r.setDouble(index, Double.NaN);
                }
            }
        }

        return r;
    }

    /**
     * Smooth with 9 points
     *
     * @param a Array
     * @return Result array
     */
    public static Array smooth9(Array a) {
        int[] shape = a.getShape();
        int colNum = shape[1];
        int rowNum = shape[0];
        Array r = Array.factory(a.getDataType(), shape);
        if (!a.getIndexPrivate().isFastIterator()) {
            a = a.copy();
        }
        Index2D index = new Index2D(shape);
        double v, w;
        double sum, wsum;
        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                sum = 0;
                wsum = 0;
                for (int ii = i - 1; ii <= i + 1; ii++) {
                    if (ii < 0 || ii >= rowNum) {
                        continue;
                    }
                    for (int jj = j - 1; jj <= j + 1; jj++) {
                        if (jj < 0 || jj >= colNum) {
                            continue;
                        }
                        v = a.getDouble(index.set(ii, jj));
                        if (!Double.isNaN(v)) {
                            if (ii == i && jj == j) {
                                w = 1;
                            } else {
                                if (ii == i || jj == j) {
                                    w = 0.5;
                                } else {
                                    w = 0.3;
                                }
                            }
                            sum += v * w;
                            wsum += w;
                        }
                    }
                }
                index.set(i, j);
                if (wsum > 0) {
                    r.setDouble(index, sum / wsum);
                } else {
                    r.setDouble(index, Double.NaN);
                }
            }
        }

        return r;
    }

    /**
     * Interpolation with IDW radius method
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param a scatter value array
     * @param X grid X array
     * @param Y grid Y array
     * @param neededPointNum needed at least point number
     * @param radius search radius
     * @return interpolated grid data
     */
    public static Array interpolation_IDW_Radius(List<Number> x_s, List<Number> y_s, Array a,
            List<Number> X, List<Number> Y, int neededPointNum, double radius) {
        int rowNum, colNum, pNum;
        colNum = X.size();
        rowNum = Y.size();
        pNum = x_s.size();
        Array r = Array.factory(DataType.DOUBLE, new int[]{rowNum, colNum});
        int i, j;
        double w, gx, gy, v;
        boolean match;

        //Construct K-D tree
        KDTree.Euclidean<Double> kdTree = new KDTree.Euclidean<>(2);
        IndexIterator iter = a.getIndexIterator();
        for (i = 0; i < pNum; i++) {
            v = iter.getDoubleNext();
            if (!Double.isNaN(v)) {
                kdTree.addPoint(new double[]{x_s.get(i).doubleValue(), y_s.get(i).doubleValue()}, v);
            }
        }

        //---- Do interpolation
        for (i = 0; i < rowNum; i++) {
            gy = Y.get(i).doubleValue();
            for (j = 0; j < colNum; j++) {
                gx = X.get(j).doubleValue();
                List<SearchResult<Double>> srs = kdTree.ballSearch_distance(new double[]{gx, gy}, radius * radius);
                if (srs == null || srs.size() < neededPointNum) {
                    r.setDouble(i * colNum + j, Double.NaN);
                } else {
                    double v_sum = 0.0;
                    double weight_sum = 0.0;
                    match = false;
                    for (SearchResult sr : srs) {
                        v = (double) sr.payload;
                        if (sr.distance == 0) {
                            r.setDouble(i * colNum + j, v);
                            match = true;
                            break;
                        } else {
                            w = 1. / sr.distance;
                            weight_sum += w;
                            v_sum += v * w;
                        }
                    }
                    if (!match) {
                        r.setDouble(i * colNum + j, v_sum / weight_sum);
                    }
                }
            }
        }

        return r;
    }

    /**
     * Interpolation with IDW neighbor method
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param a scatter value array
     * @param X grid X array
     * @param Y grid Y array
     * @param points Number of points used for interpolation
     * @return interpolated grid data
     */
    public static Array interpolation_IDW_Neighbor(List<Number> x_s, List<Number> y_s, Array a,
            List<Number> X, List<Number> Y, Integer points) {
        int colNum = X.size();
        int rowNum = Y.size();
        int pNum = x_s.size();
        Array r = Array.factory(DataType.DOUBLE, new int[]{rowNum, colNum});
        int i, j;
        double w, v, gx, gy;
        boolean match;

        //Construct K-D tree
        int n = 0;
        IndexIterator iter = a.getIndexIterator();
        KDTree.Euclidean<Double> kdTree = new KDTree.Euclidean<>(2);
        for (i = 0; i < pNum; i++) {
            v = iter.getDoubleNext();
            if (!Double.isNaN(v)) {
                kdTree.addPoint(new double[]{x_s.get(i).doubleValue(), y_s.get(i).doubleValue()}, v);
                n += 1;
            }
        }
        if (points == null) {
            points = n;
        }

        //---- Do interpolation with IDW method
        for (i = 0; i < rowNum; i++) {
            gy = Y.get(i).doubleValue();
            for (j = 0; j < colNum; j++) {
                gx = X.get(j).doubleValue();
                List<SearchResult<Double>> srs = kdTree.nearestNeighbours(new double[]{gx, gy}, points);
                double v_sum = 0.0;
                double weight_sum = 0.0;
                match = false;
                for (SearchResult sr : srs) {
                    v = (double) sr.payload;
                    if (sr.distance == 0) {
                        r.setDouble(i * colNum + j, v);
                        match = true;
                        break;
                    } else {
                        w = 1. / sr.distance;
                        weight_sum += w;
                        v_sum += v * w;
                    }
                }
                if (!match) {
                    r.setDouble(i * colNum + j, v_sum / weight_sum);
                }
            }
        }

        return r;
    }

    /**
     * Interpolate with nearest method
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param a scatter value array
     * @param X x coordinate
     * @param Y y coordinate
     * @param radius Radius
     * @param fill_value undefine value
     * @return grid data
     */
    public static Array interpolation_Nearest_1(List<Number> x_s, List<Number> y_s, Array a, List<Number> X, List<Number> Y,
            double radius, double fill_value) {
        int rowNum, colNum, pNum;
        colNum = X.size();
        rowNum = Y.size();
        pNum = x_s.size();
        Array rdata = Array.factory(DataType.DOUBLE, new int[]{rowNum, colNum});
        double gx, gy;
        double x, y, r, v, minr;
        int rr = (int) Math.ceil(radius);

        List<int[]> pIJ = getPointsIJ(x_s, y_s, X, Y);

        if (!a.getIndexPrivate().isFastIterator()) {
            a = a.copy();
        }
        for (int i = 0; i < rowNum; i++) {
            gy = Y.get(i).doubleValue();
            for (int j = 0; j < colNum; j++) {
                rdata.setDouble(i * colNum + j, fill_value);
                gx = X.get(j).doubleValue();
                minr = Double.MAX_VALUE;
                List<Integer> pIdx = getPointsIdx(pIJ, i, j, rr);
                for (int p : pIdx) {
                    v = a.getDouble(p);
                    if (MIMath.doubleEquals(v, fill_value)) {
                        continue;
                    }

                    x = x_s.get(p).doubleValue();
                    y = y_s.get(p).doubleValue();
                    r = Math.sqrt((gx - x) * (gx - x) + (gy - y) * (gy - y));
                    if (r < minr) {
                        rdata.setDouble(i * colNum + j, v);
                        minr = r;
                    }
                }
            }
        }

        return rdata;
    }

    /**
     * Interpolate with nearest method
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param a scatter value array
     * @param X x coordinate
     * @param Y y coordinate
     * @param radius Radius
     * @return grid data
     */
    public static Array interpolation_Nearest_bak(List<Number> x_s, List<Number> y_s, Array a, List<Number> X, List<Number> Y,
            double radius) {
        int rowNum, colNum, pNum;
        colNum = X.size();
        rowNum = Y.size();
        pNum = x_s.size();
        Array rdata = Array.factory(DataType.DOUBLE, new int[]{rowNum, colNum});
        double gx, gy;
        double x, y, r, v, minr;

        if (!a.getIndexPrivate().isFastIterator()) {
            a = a.copy();
        }
        for (int i = 0; i < rowNum; i++) {
            gy = Y.get(i).doubleValue();
            for (int j = 0; j < colNum; j++) {
                rdata.setDouble(i * colNum + j, Double.NaN);
                gx = X.get(j).doubleValue();
                minr = Double.MAX_VALUE;
                for (int p = 0; p < pNum; p++) {
                    v = a.getDouble(p);
                    if (Double.isNaN(v)) {
                        continue;
                    }

                    x = x_s.get(p).doubleValue();
                    y = y_s.get(p).doubleValue();
                    if (Math.abs(gx - x) > radius || Math.abs(gy - y) > radius) {
                        continue;
                    }

                    r = Math.sqrt((gx - x) * (gx - x) + (gy - y) * (gy - y));
                    if (r < radius) {
                        if (r < minr) {
                            rdata.setDouble(i * colNum + j, v);
                            minr = r;
                        }
                    }
                }
            }
        }

        return rdata;
    }
    
    /**
     * Interpolate with nearest method
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param a scatter value array
     * @param X x coordinate
     * @param Y y coordinate
     * @param radius Radius
     * @return grid data
     */
    public static Array interpolation_Nearest(List<Number> x_s, List<Number> y_s, Array a, List<Number> X, List<Number> Y,
            double radius) {
        int rowNum, colNum, pNum;
        colNum = X.size();
        rowNum = Y.size();
        pNum = x_s.size();
        Array rdata = Array.factory(DataType.DOUBLE, new int[]{rowNum, colNum});
        double gx, gy;

        //Construct K-D tree
        KDTree.Euclidean<Double> kdTree = new KDTree.Euclidean<>(2);
        IndexIterator iter = a.getIndexIterator();
        double v;
        for (int i = 0; i < pNum; i++) {
            v = iter.getDoubleNext();
//            if (!Double.isNaN(v)) {
//                kdTree.addPoint(new double[]{x_s.get(i).doubleValue(), y_s.get(i).doubleValue()}, v);
//            }
            kdTree.addPoint(new double[]{x_s.get(i).doubleValue(), y_s.get(i).doubleValue()}, v);
        }

        //Loop
        if (radius == Double.POSITIVE_INFINITY) {
            for (int i = 0; i < rowNum; i++) {
                gy = Y.get(i).doubleValue();
                for (int j = 0; j < colNum; j++) {
                    gx = X.get(j).doubleValue();
                    SearchResult r = kdTree.nearestNeighbours(new double[]{gx, gy}, 1).get(0);
                    rdata.setDouble(i * colNum + j, ((double) r.payload));
                }
            }
        } else {
            for (int i = 0; i < rowNum; i++) {
                gy = Y.get(i).doubleValue();
                for (int j = 0; j < colNum; j++) {
                    gx = X.get(j).doubleValue();
                    SearchResult r = kdTree.nearestNeighbours(new double[]{gx, gy}, 1).get(0);
                    if (Math.sqrt(r.distance) <= radius) {
                        rdata.setDouble(i * colNum + j, ((double) r.payload));
                    } else {
                        rdata.setDouble(i * colNum + j, Double.NaN);
                    }
                }
            }
        }

        return rdata;
    }

    /**
     * Interpolate with nearest method
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param a scatter value array
     * @param X x coordinate
     * @param Y y coordinate
     * @param radius Radius
     * @return grid data
     */
    public static Array interpolation_Nearest(Array x_s, Array y_s, Array a, Array X, Array Y,
                                              double radius) {
        int rowNum, colNum, pNum;
        colNum = (int)X.getSize();
        rowNum = (int)Y.getSize();
        pNum = (int)x_s.getSize();
        Array rdata = Array.factory(DataType.DOUBLE, new int[]{rowNum, colNum});
        double gx, gy;

        //Construct K-D tree
        KDTree.Euclidean<Double> kdTree = new KDTree.Euclidean<>(2);
        IndexIterator iter = a.getIndexIterator();
        double v;
        for (int i = 0; i < pNum; i++) {
            v = iter.getDoubleNext();
//            if (!Double.isNaN(v)) {
//                kdTree.addPoint(new double[]{x_s.get(i).doubleValue(), y_s.get(i).doubleValue()}, v);
//            }
            kdTree.addPoint(new double[]{x_s.getDouble(i), y_s.getDouble(i)}, v);
        }

        //Loop
        if (radius == Double.POSITIVE_INFINITY) {
            for (int i = 0; i < rowNum; i++) {
                gy = Y.getDouble(i);
                for (int j = 0; j < colNum; j++) {
                    gx = X.getDouble(j);
                    SearchResult r = kdTree.nearestNeighbours(new double[]{gx, gy}, 1).get(0);
                    rdata.setDouble(i * colNum + j, ((double) r.payload));
                }
            }
        } else {
            for (int i = 0; i < rowNum; i++) {
                gy = Y.getDouble(i);
                for (int j = 0; j < colNum; j++) {
                    gx = X.getDouble(j);
                    SearchResult r = kdTree.nearestNeighbours(new double[]{gx, gy}, 1).get(0);
                    if (Math.sqrt(r.distance) <= radius) {
                        rdata.setDouble(i * colNum + j, ((double) r.payload));
                    } else {
                        rdata.setDouble(i * colNum + j, Double.NaN);
                    }
                }
            }
        }

        return rdata;
    }

    /**
     * Interpolate with surface method
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param a scatter value array
     * @param X x coordinate
     * @param Y y coordinate
     * @param unDefData undefine value
     * @return grid data
     */
    public static Array interpolation_Surface_1(Array x_s, Array y_s, Array a, Array X, Array Y,
                                                double unDefData) {
        x_s = x_s.copyIfView();
        y_s = y_s.copyIfView();
        a = a.copyIfView();
        X = X.copyIfView();
        Y = Y.copyIfView();

        int rowNum, colNum, xn, yn;
        int[] shape = x_s.getShape();
        colNum = shape[1];
        rowNum = shape[0];
        xn = (int) X.getSize();
        yn = (int) Y.getSize();
        Array r = Array.factory(DataType.DOUBLE, new int[]{yn, xn});
        double x, y;

        PolygonShape[][] polygons = new PolygonShape[rowNum - 1][colNum - 1];
        PolygonShape ps;
        for (int i = 0; i < rowNum - 1; i++) {
            for (int j = 0; j < colNum - 1; j++) {
                ps = new PolygonShape();
                List<PointD> points = new ArrayList<>();
                points.add(new PointD(x_s.getDouble(i * colNum + j), y_s.getDouble(i * colNum + j)));
                points.add(new PointD(x_s.getDouble((i + 1) * colNum + j), y_s.getDouble((i + 1) * colNum + j)));
                points.add(new PointD(x_s.getDouble((i + 1) * colNum + j + 1), y_s.getDouble((i + 1) * colNum + j + 1)));
                points.add(new PointD(x_s.getDouble(i * colNum + j + 1), y_s.getDouble(i * colNum + j + 1)));
                points.add((PointD) points.get(0).clone());
                ps.setPoints(points);
                polygons[i][j] = ps;
            }
        }

        for (int i = 0; i < yn; i++) {
            for (int j = 0; j < xn; j++) {
                r.setDouble(i * xn + j, unDefData);
            }
        }

        double v;
        for (int i = 0; i < rowNum - 1; i++) {
            for (int j = 0; j < colNum - 1; j++) {
                ps = polygons[i][j];
                v = a.getDouble(i * colNum + j);
                for (int ii = 0; ii < yn; ii++) {
                    y = Y.getDouble(ii);
                    for (int jj = 0; jj < xn; jj++) {
                        x = X.getDouble(jj);
                        if (Double.isNaN(r.getDouble(ii * xn + jj)) || r.getDouble(ii * xn + jj) == unDefData) {
                            if (GeoComputation.pointInPolygon(ps, x, y)) {
                                r.setDouble(ii * xn + jj, v);
                            }
                        }
                    }
                }
            }
        }

        return r;
    }

    /**
     * Interpolate with surface method
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param a scatter value array
     * @param X x coordinate
     * @param Y y coordinate
     * @return grid data
     */
    public static Array interpolation_Surface(Array x_s, Array y_s, Array a, Array X, Array Y) {
        x_s = x_s.copyIfView();
        y_s = y_s.copyIfView();
        a = a.copyIfView();
        X = X.copyIfView();
        Y = Y.copyIfView();

        int rowNum, colNum, xn, yn;
        int[] shape = x_s.getShape();
        colNum = shape[1];
        rowNum = shape[0];
        xn = (int) X.getSize();
        yn = (int) Y.getSize();
        Array r = Array.factory(DataType.DOUBLE, new int[]{yn, xn});
        for (int i = 0; i < r.getSize(); i++) {
            r.setDouble(i, Double.NaN);
        }

        double x, y;
        double v, xll, xtl, xtr, xlr, yll, ytl, ytr, ylr;
        double dX = X.getDouble(1) - X.getDouble(0);
        double dY = Y.getDouble(1) - Y.getDouble(0);
        int minxi, maxxi, minyi, maxyi;
        for (int i = 0; i < rowNum - 1; i++) {
            for (int j = 0; j < colNum - 1; j++) {
                v = a.getDouble(i * colNum + j);
                if (Double.isNaN(v)) {
                    continue;
                }
                xll = x_s.getDouble(i * colNum + j);
                xtl = x_s.getDouble((i + 1) * colNum + j);
                xtr = x_s.getDouble((i + 1) * colNum + j + 1);
                xlr = x_s.getDouble(i * colNum + j + 1);
                yll = y_s.getDouble(i * colNum + j);
                ytl = y_s.getDouble((i + 1) * colNum + j);
                ytr = y_s.getDouble((i + 1) * colNum + j + 1);
                ylr = y_s.getDouble(i * colNum + j + 1);
                if (Double.isNaN(xll) || Double.isNaN(xtl) || Double.isNaN(xtr) || Double.isNaN(xlr)
                        || Double.isNaN(yll) || Double.isNaN(ytl) || Double.isNaN(ytr) || Double.isNaN(ylr)) {
                    continue;
                }
                PolygonShape ps = new PolygonShape();
                List<PointD> points = new ArrayList<>();
                points.add(new PointD(xll, yll));
                points.add(new PointD(xtl, ytl));
                points.add(new PointD(xtr, ytr));
                points.add(new PointD(xlr, ylr));
                points.add((PointD) points.get(0).clone());
                ps.setPoints(points);
                minxi = (int) ((ps.getExtent().minX - X.getDouble(0)) / dX);
                maxxi = (int) ((ps.getExtent().maxX - X.getDouble(0)) / dX);
                minyi = (int) ((ps.getExtent().minY - Y.getDouble(0)) / dY);
                maxyi = (int) ((ps.getExtent().maxY - Y.getDouble(0)) / dY);
                maxxi += 1;
                maxyi += 1;
                if (maxxi < 0 || minxi >= xn) {
                    continue;
                }
                if (maxyi < 0 || minyi >= yn) {
                    continue;
                }
                if (minxi < 0) {
                    minxi = 0;
                }
                if (maxxi >= xn) {
                    maxxi = xn - 1;
                }
                if (maxyi >= yn) {
                    maxyi = yn - 1;
                }
                for (int m = minyi; m <= maxyi; m++) {
                    y = Y.getDouble(m);
                    for (int n = minxi; n <= maxxi; n++) {
                        x = X.getDouble(n);
                        if (GeoComputation.pointInPolygon(ps, x, y)) {
                            r.setDouble(m * xn + n, v);
                        }
                    }
                }
            }
        }

        return r;
    }

    /**
     * Extend the grid to half cell, so the grid points are the centers of the
     * cells
     *
     * @param x Input x coordinate
     * @param y Input y coordinate
     * @return Result x and y coordinates
     */
    public static Array[] extendHalfCell(Array x, Array y) {
        x = x.copyIfView();
        y = y.copyIfView();
        double dX = x.getDouble(1) - x.getDouble(0);
        double dY = y.getDouble(1) - y.getDouble(0);
        int nx = (int) x.getSize() + 1;
        int ny = (int) y.getSize() + 1;
        Array rx = Array.factory(DataType.DOUBLE, new int[]{nx});
        Array ry = Array.factory(DataType.DOUBLE, new int[]{ny});
        for (int i = 0; i < rx.getSize(); i++) {
            if (i == rx.getSize() - 1) {
                rx.setDouble(i, x.getDouble(i - 1) + dX * 0.5);
            } else {
                rx.setDouble(i, x.getDouble(i) - dX * 0.5);
            }
        }

        for (int i = 0; i < ry.getSize(); i++) {
            if (i == ry.getSize() - 1) {
                ry.setDouble(i, y.getDouble(i - 1) + dY * 0.5);
            } else {
                ry.setDouble(i, y.getDouble(i) - dY * 0.5);
            }
        }

        return new Array[]{rx, ry};
    }

    /**
     * Interpolate with inside method - The grid cell value is the average value
     * of the inside points or fill value if no inside point.
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param a scatter value array
     * @param X x coordinate
     * @param Y y coordinate
     * @return grid data
     */
    public static Array interpolation_Inside(List<Number> x_s, List<Number> y_s, Array a, List<Number> X, List<Number> Y) {
        int rowNum, colNum, pNum;
        colNum = X.size();
        rowNum = Y.size();
        pNum = x_s.size();
        Array r = Array.factory(DataType.DOUBLE, new int[]{rowNum, colNum});
        double dX = X.get(1).doubleValue() - X.get(0).doubleValue();
        double dY = Y.get(1).doubleValue() - Y.get(0).doubleValue();
        int[][] pNums = new int[rowNum][colNum];
        double x, y, v;

        a = a.copyIfView();
        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                pNums[i][j] = 0;
                r.setDouble(i * colNum + j, 0.0);
            }
        }

        for (int p = 0; p < pNum; p++) {
            v = a.getDouble(p);
            if (Double.isNaN(v)) {
                continue;
            }

            x = x_s.get(p).doubleValue();
            y = y_s.get(p).doubleValue();
            if (x < X.get(0).doubleValue() || x > X.get(colNum - 1).doubleValue()) {
                continue;
            }
            if (y < Y.get(0).doubleValue() || y > Y.get(rowNum - 1).doubleValue()) {
                continue;
            }

            int j = (int) ((x - X.get(0).doubleValue()) / dX);
            int i = (int) ((y - Y.get(0).doubleValue()) / dY);
            pNums[i][j] += 1;
            r.setDouble(i * colNum + j, r.getDouble(i * colNum + j) + v);
        }

        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                if (pNums[i][j] == 0) {
                    r.setDouble(i * colNum + j, Double.NaN);
                } else {
                    r.setDouble(i * colNum + j, r.getDouble(i * colNum + j) / pNums[i][j]);
                }
            }
        }

        return r;
    }

    /**
     * Interpolate with inside method - The grid cell value is the average value
     * of the inside points or fill value if no inside point.
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param a scatter value array
     * @param X x coordinate
     * @param Y y coordinate
     * @param center If the grid point is center or border
     * @return grid data
     */
    public static Array interpolation_Inside_Mean(Array x_s, Array y_s, Array a, Array X, Array Y, boolean center) {
        int rowNum, colNum, pNum;
        x_s = x_s.copyIfView();
        y_s = y_s.copyIfView();
        a = a.copyIfView();
        X = X.copyIfView();
        Y = Y.copyIfView();

        if (center) {
            Array[] xy = extendHalfCell(X, Y);
            X = xy[0];
            Y = xy[1];
        }

        colNum = (int) X.getSize();
        rowNum = (int) Y.getSize();
        if (center) {
            colNum -= 1;
            rowNum -= 1;
        }
        pNum = (int) x_s.getSize();
        Array r = Array.factory(DataType.DOUBLE, new int[]{rowNum, colNum});
        double dX = X.getDouble(1) - X.getDouble(0);
        double dY = Y.getDouble(1) - Y.getDouble(0);
        int[][] pNums = new int[rowNum][colNum];
        double x, y, v;

        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                pNums[i][j] = 0;
                r.setDouble(i * colNum + j, 0.0);
            }
        }

        for (int p = 0; p < pNum; p++) {
            v = a.getDouble(p);
            if (Double.isNaN(v)) {
                continue;
            }

            x = x_s.getDouble(p);
            y = y_s.getDouble(p);
            if (x < X.getDouble(0) || x > X.getDouble(colNum - 1)) {
                continue;
            }
            if (y < Y.getDouble(0) || y > Y.getDouble(rowNum - 1)) {
                continue;
            }

            int j = (int) ((x - X.getDouble(0)) / dX);
            int i = (int) ((y - Y.getDouble(0)) / dY);
            pNums[i][j] += 1;
            r.setDouble(i * colNum + j, r.getDouble(i * colNum + j) + v);
        }

        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                if (pNums[i][j] == 0) {
                    r.setDouble(i * colNum + j, Double.NaN);
                } else {
                    r.setDouble(i * colNum + j, r.getDouble(i * colNum + j) / pNums[i][j]);
                }
            }
        }

        return r;
    }

    /**
     * Interpolate with inside method - The grid cell value is the maximum value
     * of the inside points or fill value if no inside point.
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param a scatter value array
     * @param X x coordinate
     * @param Y y coordinate
     * @return grid data
     */
    public static Array interpolation_Inside_Max(List<Number> x_s, List<Number> y_s, Array a, List<Number> X, List<Number> Y) {
        int rowNum, colNum, pNum;
        colNum = X.size();
        rowNum = Y.size();
        pNum = x_s.size();
        Array r = Array.factory(DataType.DOUBLE, new int[]{rowNum, colNum});
        double dX = X.get(1).doubleValue() - X.get(0).doubleValue();
        double dY = Y.get(1).doubleValue() - Y.get(0).doubleValue();
        int[][] pNums = new int[rowNum][colNum];
        double x, y, v;
        double min = Double.NEGATIVE_INFINITY;

        a = a.copyIfView();
        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                pNums[i][j] = 0;
                r.setDouble(i * colNum + j, min);
            }
        }

        for (int p = 0; p < pNum; p++) {
            v = a.getDouble(p);
            if (Double.isNaN(v)) {
                continue;
            }

            x = x_s.get(p).doubleValue();
            y = y_s.get(p).doubleValue();
            if (x < X.get(0).doubleValue() || x > X.get(colNum - 1).doubleValue()) {
                continue;
            }
            if (y < Y.get(0).doubleValue() || y > Y.get(rowNum - 1).doubleValue()) {
                continue;
            }

            int j = (int) ((x - X.get(0).doubleValue()) / dX);
            int i = (int) ((y - Y.get(0).doubleValue()) / dY);
            pNums[i][j] += 1;
            r.setDouble(i * colNum + j, Math.max(r.getDouble(i * colNum + j), v));
        }

        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                if (pNums[i][j] == 0 || Double.isInfinite(r.getDouble(i * colNum + j))) {
                    r.setDouble(i * colNum + j, Double.NaN);
                }
            }
        }

        return r;
    }

    /**
     * Interpolate with inside method - The grid cell value is the minimum value
     * of the inside points or fill value if no inside point.
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param a scatter value array
     * @param X x coordinate
     * @param Y y coordinate
     * @return grid data
     */
    public static Array interpolation_Inside_Min(List<Number> x_s, List<Number> y_s, Array a, List<Number> X, List<Number> Y) {
        int rowNum, colNum, pNum;
        colNum = X.size();
        rowNum = Y.size();
        pNum = x_s.size();
        Array r = Array.factory(DataType.DOUBLE, new int[]{rowNum, colNum});
        double dX = X.get(1).doubleValue() - X.get(0).doubleValue();
        double dY = Y.get(1).doubleValue() - Y.get(0).doubleValue();
        int[][] pNums = new int[rowNum][colNum];
        double x, y, v;
        double max = Double.MAX_VALUE;

        a = a.copyIfView();
        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                pNums[i][j] = 0;
                r.setDouble(i * colNum + j, max);
            }
        }

        for (int p = 0; p < pNum; p++) {
            v = a.getDouble(p);
            if (Double.isNaN(v)) {
                continue;
            }

            x = x_s.get(p).doubleValue();
            y = y_s.get(p).doubleValue();
            if (x < X.get(0).doubleValue() || x > X.get(colNum - 1).doubleValue()) {
                continue;
            }
            if (y < Y.get(0).doubleValue() || y > Y.get(rowNum - 1).doubleValue()) {
                continue;
            }

            int j = (int) ((x - X.get(0).doubleValue()) / dX);
            int i = (int) ((y - Y.get(0).doubleValue()) / dY);
            pNums[i][j] += 1;
            r.setDouble(i * colNum + j, Math.min(r.getDouble(i * colNum + j), v));
        }

        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                if (pNums[i][j] == 0 || r.getDouble(i * colNum + j) == Double.MAX_VALUE) {
                    r.setDouble(i * colNum + j, Double.NaN);
                }
            }
        }

        return r;
    }

    /**
     * Interpolate with inside method - The grid cell value is the count number
     * of the inside points or fill value if no inside point.
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param X x coordinate
     * @param Y y coordinate
     * @param pointDensity If return point density value
     * @param centerPoint points locate at center or border of grid
     * @return grid data
     */
    public static Object interpolation_Inside_Count(List<Number> x_s, List<Number> y_s,
            List<Number> X, List<Number> Y, boolean pointDensity, boolean centerPoint) {
        int rowNum, colNum, pNum;
        colNum = X.size();
        rowNum = Y.size();
        pNum = x_s.size();
        Array r = Array.factory(DataType.INT, new int[]{rowNum, colNum});
        double dX = X.get(1).doubleValue() - X.get(0).doubleValue();
        double dY = Y.get(1).doubleValue() - Y.get(0).doubleValue();
        int[][] pNums = new int[rowNum][colNum];
        double x, y, sx, sy, ex, ey;
        if (centerPoint) {
            sx = X.get(0).doubleValue() - dX * 0.5;
            sy = Y.get(0).doubleValue() - dY * 0.5;
            ex = X.get(colNum - 1).doubleValue() + dX * 0.5;
            ey = Y.get(rowNum - 1).doubleValue() + dY * 0.5;
        } else {
            sx = X.get(0).doubleValue();
            sy = Y.get(0).doubleValue();
            ex = X.get(colNum - 1).doubleValue();
            ey = Y.get(rowNum - 1).doubleValue();
        }

        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                pNums[i][j] = 0;
                //r.setInt(i * colNum + j, 0);
            }
        }

        for (int p = 0; p < pNum; p++) {
            x = x_s.get(p).doubleValue();
            y = y_s.get(p).doubleValue();
            if (x < sx || x > ex) {
                continue;
            }
            if (y < sy || y > ey) {
                continue;
            }

            int j = (int) ((x - sx) / dX);
            int i = (int) ((y - sy) / dY);
            pNums[i][j] += 1;
            //r.setInt(i * colNum + j, r.getInt(i * colNum + j) + 1);
        }

        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                r.setInt(i * colNum + j, pNums[i][j]);
            }
        }

        if (pointDensity) {
            Array pds = Array.factory(DataType.INT, new int[]{pNum});
            for (int p = 0; p < pNum; p++) {
                x = x_s.get(p).doubleValue();
                y = y_s.get(p).doubleValue();
                if (x < sx || x > ex) {
                    continue;
                }
                if (y < sy || y > ey) {
                    continue;
                }

                int j = (int) ((x - sx) / dX);
                int i = (int) ((y - sy) / dY);
                pds.setInt(p, pNums[i][j]);
            }
            return new Array[]{r, pds};
        }

        return r;
    }

    private static List<int[]> getPointsIJ(List<Number> x_s, List<Number> y_s, List<Number> X, List<Number> Y) {
        int rowNum, colNum, pNum;
        colNum = X.size();
        rowNum = Y.size();
        pNum = x_s.size();
        double dX = X.get(1).doubleValue() - X.get(0).doubleValue();
        double dY = Y.get(1).doubleValue() - Y.get(0).doubleValue();
        List<int[]> pIndices = new ArrayList<>();
        double x, y;
        int i, j;
        for (int p = 0; p < pNum; p++) {
            x = x_s.get(p).doubleValue();
            y = y_s.get(p).doubleValue();
            if (x < X.get(0).doubleValue() || x > X.get(colNum - 1).doubleValue()) {
                continue;
            }
            if (y < Y.get(0).doubleValue() || y > Y.get(rowNum - 1).doubleValue()) {
                continue;
            }

            j = (int) ((x - X.get(0).doubleValue()) / dX);
            i = (int) ((y - Y.get(0).doubleValue()) / dY);
            pIndices.add(new int[]{i, j});
        }

        return pIndices;
    }

    private static List<Integer> getPointsIdx(List<int[]> pIJ, int ii, int jj, int radius) {
        List<Integer> pIdx = new ArrayList<>();
        int[] ij;
        int i, j;
        for (int p = 0; p < pIJ.size(); p++) {
            ij = pIJ.get(p);
            i = ij[0];
            j = ij[1];
            if (Math.abs(i - ii) <= radius && Math.abs(j - jj) <= radius) {
                pIdx.add(p);
            }
        }

        return pIdx;
    }

    /**
     * Cressman analysis
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param v_s scatter value array
     * @param X x array
     * @param Y y array
     * @param radList radii list
     * @return result grid data
     */
    public static Array cressman(List<Number> x_s, List<Number> y_s, Array v_s, List<Number> X, List<Number> Y,
            List<Number> radList) {
        v_s = v_s.copyIfView();

        int xNum = X.size();
        int yNum = Y.size();
        int pNum = x_s.size();
        //double[][] gridData = new double[yNum][xNum];
        Array r = Array.factory(DataType.DOUBLE, new int[]{yNum, xNum});
        int irad = radList.size();
        int i, j;

        //Loop through each stn report and convert stn lat/lon to grid coordinates
        double xMin = X.get(0).doubleValue();
        double xMax = X.get(xNum - 1).doubleValue();
        double yMin = Y.get(0).doubleValue();
        double yMax = Y.get(yNum - 1).doubleValue();
        double xDelt = X.get(1).doubleValue() - X.get(0).doubleValue();
        double yDelt = Y.get(1).doubleValue() - Y.get(0).doubleValue();
        double x, y;
        double sum;
        int stNum = 0;
        double[][] stationData = new double[pNum][3];
        for (i = 0; i < pNum; i++) {
            x = x_s.get(i).doubleValue();
            y = y_s.get(i).doubleValue();
            stationData[i][0] = (x - xMin) / xDelt;
            stationData[i][1] = (y - yMin) / yDelt;
            stationData[i][2] = v_s.getDouble(i);
            if (!Double.isNaN(stationData[i][2])) {
                //total += stationData[i][2];
                stNum += 1;
            }
        }
        //total = total / stNum;

        double HITOP = -999900000000000000000.0;
        double HIBOT = 999900000000000000000.0;
        double[][] TOP = new double[yNum][xNum];
        double[][] BOT = new double[yNum][xNum];
        for (i = 0; i < yNum; i++) {
            for (j = 0; j < xNum; j++) {
                TOP[i][j] = HITOP;
                BOT[i][j] = HIBOT;
            }
        }

        //Initial grid values are average of station reports within the first radius
        double val, sx, sy, sxi, syi;
        double rad;
        if (radList.size() > 0) {
            rad = radList.get(0).doubleValue();
        } else {
            rad = 4;
        }
        for (i = 0; i < yNum; i++) {
            y = Y.get(i).doubleValue();
            yMin = y - rad;
            yMax = y + rad;
            for (j = 0; j < xNum; j++) {
                x = X.get(j).doubleValue();
                xMin = x - rad;
                xMax = x + rad;
                stNum = 0;
                sum = 0;
                for (int s = 0; s < pNum; s++) {
                    val = stationData[s][2];
                    sx = x_s.get(s).doubleValue();
                    sy = y_s.get(s).doubleValue();
                    sxi = stationData[s][0];
                    syi = stationData[s][1];
                    if (Double.isNaN(val) || sx < xMin || sx > xMax || sy < yMin || sy > yMax) {
                        continue;
                    }

                    double dis = Math.sqrt(Math.pow(sx - x, 2) + Math.pow(sy - y, 2));
                    if (dis > rad) {
                        continue;
                    }

                    sum += val;
                    stNum += 1;
                    if (TOP[i][j] < val) {
                        TOP[i][j] = val;
                    }
                    if (BOT[i][j] > val) {
                        BOT[i][j] = val;
                    }
                }
                if (stNum == 0) {
                    r.setDouble(i * xNum + j, Double.NaN);
                } else {
                    r.setDouble(i * xNum + j, sum / stNum);
                }
            }
        }

        //Perform the objective analysis
        for (int p = 0; p < irad; p++) {
            rad = radList.get(p).doubleValue();
            for (i = 0; i < yNum; i++) {
                y = Y.get(i).doubleValue();
                yMin = y - rad;
                yMax = y + rad;
                for (j = 0; j < xNum; j++) {
                    if (Double.isNaN(r.getDouble(i * xNum + j))) {
                        continue;
                    }

                    x = X.get(j).doubleValue();
                    xMin = x - rad;
                    xMax = x + rad;
                    sum = 0;
                    double wSum = 0;
                    for (int s = 0; s < pNum; s++) {
                        val = stationData[s][2];
                        sx = x_s.get(s).doubleValue();
                        sy = y_s.get(s).doubleValue();
                        sxi = stationData[s][0];
                        syi = stationData[s][1];
                        if (Double.isNaN(val) || sx < xMin || sx > xMax || sy < yMin || sy > yMax) {
                            continue;
                        }

                        double dis = Math.sqrt(Math.pow(sx - x, 2) + Math.pow(sy - y, 2));
                        if (dis > rad) {
                            continue;
                        }

                        int i1 = (int) syi;
                        int j1 = (int) sxi;
                        int i2 = i1 + 1;
                        int j2 = j1 + 1;
                        double a = r.getDouble(i1 * xNum + j1);
                        double b = r.getDouble(i1 * xNum + j2);
                        double c = r.getDouble(i2 * xNum + j1);
                        double d = r.getDouble(i2 * xNum + j2);
                        List<Double> dList = new ArrayList<>();
                        if (!Double.isNaN(a)) {
                            dList.add(a);
                        }
                        if (!Double.isNaN(b)) {
                            dList.add(b);
                        }
                        if (!Double.isNaN(c)) {
                            dList.add(c);
                        }
                        if (Double.isNaN(d)) {
                            dList.add(d);
                        }

                        double calVal;
                        if (dList.isEmpty()) {
                            continue;
                        } else if (dList.size() == 1) {
                            calVal = dList.get(0);
                        } else if (dList.size() <= 3) {
                            double aSum = 0;
                            for (double dd : dList) {
                                aSum += dd;
                            }
                            calVal = aSum / dList.size();
                        } else {
                            double x1val = a + (c - a) * (syi - i1);
                            double x2val = b + (d - b) * (syi - i1);
                            calVal = x1val + (x2val - x1val) * (sxi - j1);
                        }
                        double eVal = val - calVal;
                        double w = (rad * rad - dis * dis) / (rad * rad + dis * dis);
                        sum += eVal * w;
                        wSum += w;
                    }
//                    if (wSum < 0.000001) {
//                        r.setDouble(i * xNum + j, Double.NaN);
//                    } else {
//                        double aData = r.getDouble(i * xNum + j) + sum / wSum;
//                        r.setDouble(i * xNum + j, Math.max(BOT[i][j], Math.min(TOP[i][j], aData)));
//                    }
                    if (wSum >= 0.000001) {
                        double aData = r.getDouble(i * xNum + j) + sum / wSum;
                        r.setDouble(i * xNum + j, Math.max(BOT[i][j], Math.min(TOP[i][j], aData)));
                    }
                }
            }
        }

        //Return
        return r;
    }

    /**
     * Cressman analysis
     *
     * @param x_s scatter X array
     * @param y_s scatter Y array
     * @param v_s scatter value array
     * @param X x array
     * @param Y y array
     * @param radList radii list
     * @return result grid data
     */
    public static Array cressman_bak(List<Number> x_s, List<Number> y_s, Array v_s, List<Number> X, List<Number> Y,
            List<Number> radList) {
        v_s = v_s.copyIfView();

        int xNum = X.size();
        int yNum = Y.size();
        int pNum = x_s.size();
        //double[][] gridData = new double[yNum][xNum];
        Array r = Array.factory(DataType.DOUBLE, new int[]{yNum, xNum});
        int irad = radList.size();
        int i, j;

        //Loop through each stn report and convert stn lat/lon to grid coordinates
        double xMin = X.get(0).doubleValue();
        double xMax;
        double yMin = Y.get(0).doubleValue();
        double yMax;
        double xDelt = X.get(1).doubleValue() - X.get(0).doubleValue();
        double yDelt = Y.get(1).doubleValue() - Y.get(0).doubleValue();
        double x, y;
        double sum;
        int stNum = 0;
        double[][] stationData = new double[pNum][3];
        for (i = 0; i < pNum; i++) {
            x = x_s.get(i).doubleValue();
            y = y_s.get(i).doubleValue();
            stationData[i][0] = (x - xMin) / xDelt;
            stationData[i][1] = (y - yMin) / yDelt;
            stationData[i][2] = v_s.getDouble(i);
            if (!Double.isNaN(stationData[i][2])) {
                //total += stationData[i][2];
                stNum += 1;
            }
        }
        //total = total / stNum;

        double HITOP = -999900000000000000000.0;
        double HIBOT = 999900000000000000000.0;
        double[][] TOP = new double[yNum][xNum];
        double[][] BOT = new double[yNum][xNum];
        for (i = 0; i < yNum; i++) {
            for (j = 0; j < xNum; j++) {
                TOP[i][j] = HITOP;
                BOT[i][j] = HIBOT;
            }
        }

        //Initial grid values are average of station reports within the first radius
        double rad;
        if (radList.size() > 0) {
            rad = radList.get(0).doubleValue();
        } else {
            rad = 4;
        }
        for (i = 0; i < yNum; i++) {
            y = (double) i;
            yMin = y - rad;
            yMax = y + rad;
            for (j = 0; j < xNum; j++) {
                x = (double) j;
                xMin = x - rad;
                xMax = x + rad;
                stNum = 0;
                sum = 0;
                for (int s = 0; s < pNum; s++) {
                    double val = stationData[s][2];
                    double sx = stationData[s][0];
                    double sy = stationData[s][1];
                    if (sx < 0 || sx >= xNum - 1 || sy < 0 || sy >= yNum - 1) {
                        continue;
                    }

                    if (Double.isNaN(val) || sx < xMin || sx > xMax || sy < yMin || sy > yMax) {
                        continue;
                    }

                    double dis = Math.sqrt(Math.pow(sx - x, 2) + Math.pow(sy - y, 2));
                    if (dis > rad) {
                        continue;
                    }

                    sum += val;
                    stNum += 1;
                    if (TOP[i][j] < val) {
                        TOP[i][j] = val;
                    }
                    if (BOT[i][j] > val) {
                        BOT[i][j] = val;
                    }
                }
                if (stNum == 0) {
                    r.setDouble(i * xNum + j, Double.NaN);
                } else {
                    r.setDouble(i * xNum + j, sum / stNum);
                }
            }
        }

        //Perform the objective analysis
        for (int p = 0; p < irad; p++) {
            rad = radList.get(p).doubleValue();
            for (i = 0; i < yNum; i++) {
                y = (double) i;
                yMin = y - rad;
                yMax = y + rad;
                for (j = 0; j < xNum; j++) {
                    if (Double.isNaN(r.getDouble(i * xNum + j))) {
                        continue;
                    }

                    x = (double) j;
                    xMin = x - rad;
                    xMax = x + rad;
                    sum = 0;
                    double wSum = 0;
                    for (int s = 0; s < pNum; s++) {
                        double val = stationData[s][2];
                        double sx = stationData[s][0];
                        double sy = stationData[s][1];
                        if (sx < 0 || sx >= xNum - 1 || sy < 0 || sy >= yNum - 1) {
                            continue;
                        }

                        if (Double.isNaN(val) || sx < xMin || sx > xMax || sy < yMin || sy > yMax) {
                            continue;
                        }

                        double dis = Math.sqrt(Math.pow(sx - x, 2) + Math.pow(sy - y, 2));
                        if (dis > rad) {
                            continue;
                        }

                        int i1 = (int) sy;
                        int j1 = (int) sx;
                        int i2 = i1 + 1;
                        int j2 = j1 + 1;
                        double a = r.getDouble(i1 * xNum + j1);
                        double b = r.getDouble(i1 * xNum + j2);
                        double c = r.getDouble(i2 * xNum + j1);
                        double d = r.getDouble(i2 * xNum + j2);
                        List<Double> dList = new ArrayList<>();
                        if (!Double.isNaN(a)) {
                            dList.add(a);
                        }
                        if (!Double.isNaN(b)) {
                            dList.add(b);
                        }
                        if (!Double.isNaN(c)) {
                            dList.add(c);
                        }
                        if (Double.isNaN(d)) {
                            dList.add(d);
                        }

                        double calVal;
                        if (dList.isEmpty()) {
                            continue;
                        } else if (dList.size() == 1) {
                            calVal = dList.get(0);
                        } else if (dList.size() <= 3) {
                            double aSum = 0;
                            for (double dd : dList) {
                                aSum += dd;
                            }
                            calVal = aSum / dList.size();
                        } else {
                            double x1val = a + (c - a) * (sy - i1);
                            double x2val = b + (d - b) * (sy - i1);
                            calVal = x1val + (x2val - x1val) * (sx - j1);
                        }
                        double eVal = val - calVal;
                        double w = (rad * rad - dis * dis) / (rad * rad + dis * dis);
                        sum += eVal * w;
                        wSum += w;
                    }
                    if (wSum < 0.000001) {
                        r.setDouble(i * xNum + j, Double.NaN);
                    } else {
                        double aData = r.getDouble(i * xNum + j) + sum / wSum;
                        r.setDouble(i * xNum + j, Math.max(BOT[i][j], Math.min(TOP[i][j], aData)));
                    }
//                    if (wSum >= 0.000001) {
//                        double aData = r.getDouble(i * xNum + j) + sum / wSum;
//                        r.setDouble(i * xNum + j, Math.max(BOT[i][j], Math.min(TOP[i][j], aData)));
//                    }
                }
            }
        }

        //Return
        return r;
    }

    /**
     * Interpolates from a rectilinear grid to another rectilinear grid using
     * bilinear interpolation.
     *
     * @param a The sample array
     * @param X X coordinate of the sample array
     * @param Y Y coordinate of the sample array
     * @param newX X coordinate of the query points
     * @param newY Y coordinate of the query points
     * @return Resampled array
     */
    public static Array linint2(Array a, Array X, Array Y, Array newX, Array newY) {
        int xn = (int) newX.getSize();
        int yn = (int) newY.getSize();
        int[] shape = a.getShape();
        int n = shape.length;
        shape[n - 1] = xn;
        shape[n - 2] = yn;
        double x, y, v;
        Array r = Array.factory(DataType.DOUBLE, shape);

        Index index = r.getIndex();
        int[] counter;
        int yi, xi;
        for (int k = 0; k < r.getSize(); k++) {
            counter = index.getCurrentCounter();
            yi = counter[n - 2];
            xi = counter[n - 1];
            y = newY.getDouble(yi);
            x = newX.getDouble(xi);
            v = bilinear(a, index, X, Y, x, y);
            r.setDouble(index, v);
            index.incr();
        }

        return r;
    }

    /**
     * Get slice array along an axis
     * @param a The original array
     * @param axis The axis
     * @param dim axis dimension array
     * @param v axis value of slice
     * @return Slice array
     * @throws InvalidRangeException
     */
    public static Array slice(Array a, int axis, Array dim, double v) throws InvalidRangeException {
        a = a.copyIfView();
        dim = dim.copyIfView();

        int ndim = a.getRank();
        int[] dShape = a.getShape();
        int n = (int) dim.getSize();
        int idx = getDimIndex(dim, v);
        if (idx == -1 || idx == -(n+1)) {
            return null;
        } else if (idx >= 0) {
            int[] origin = new int[ndim];
            int[] shape = new int[ndim];
            for (int i = 0; i < ndim; i++) {
                if (i == axis) {
                    origin[i] = idx;
                    shape[i] = 1;
                } else {
                    origin[i] = 0;
                    shape[i] = dShape[i];
                }
            }
            Array r = a.section(origin, shape);
            return r;
        } else {
            int[] origin = new int[ndim];
            int[] shape = new int[ndim];
            int si = -idx - 2;
            int ei = si + 1;
            for (int i = 0; i < ndim; i++) {
                if (i == axis) {
                    origin[i] = si;
                    shape[i] = 1;
                } else {
                    origin[i] = 0;
                    shape[i] = dShape[i];
                }
            }
            Array rs = a.section(origin, shape);
            origin[axis] = ei;
            Array re = a.section(origin, shape);
            double sv = dim.getDouble(si);
            double ev = dim.getDouble(ei);
            double ratio = (v - sv) / (ev - sv);
            Array r = Array.factory(a.getDataType(), rs.getShape());
            IndexIterator rIter = r.getIndexIterator();
            IndexIterator sIter = rs.getIndexIterator();
            IndexIterator eIter = re.getIndexIterator();
            while (rIter.hasNext()) {
                sv = sIter.getDoubleNext();
                ev = eIter.getDoubleNext();
                rIter.setDoubleNext(sv + (ev - sv) * ratio);
            }
            return r;
        }
    }

    /**
     * Resample grid array with bilinear method
     *
     * @param a The sample array
     * @param X X coordinate of the sample array
     * @param Y Y coordinate of the sample array
     * @param newX X coordinate of the query points
     * @param newY Y coordinate of the query points
     * @return Resampled array
     */
    public static Array resample_Bilinear(Array a, List<Number> X, List<Number> Y, List<Number> newX, List<Number> newY) {
        int i, j;
        int xn = newX.size();
        int yn = newY.size();
        double x, y, v;
        Array r = Array.factory(DataType.DOUBLE, new int[]{yn, xn});

        if (!a.getIndexPrivate().isFastIterator()) {
            a = a.copy();
        }
        for (i = 0; i < yn; i++) {
            y = newY.get(i).doubleValue();
            for (j = 0; j < xn; j++) {
                x = newX.get(j).doubleValue();
                if (x < X.get(0).doubleValue() || x > X.get(X.size() - 1).doubleValue()) {
                    r.setDouble(i * xn + j, Double.NaN);
                } else if (y < Y.get(0).doubleValue() || y > Y.get(Y.size() - 1).doubleValue()) {
                    r.setDouble(i * xn + j, Double.NaN);
                } else {
                    v = toStation(a, X, Y, x, y);
                    r.setDouble(i * xn + j, v);
                }
            }
        }

        return r;
    }

    /**
     * Resample grid array with bilinear method
     *
     * @param a The sample array
     * @param X X coordinate of the sample array
     * @param Y Y coordinate of the sample array
     * @param newX X coordinate of the query points
     * @param newY Y coordinate of the query points
     * @return Resampled array
     */
    public static Array resample_Bilinear(Array a, Array X, Array Y, Array newX, Array newY) {
        int i;
        int n = (int) newX.getSize();
        double x, y, v;
        Array r = Array.factory(DataType.DOUBLE, newX.getShape());

        for (i = 0; i < n; i++) {
            x = newX.getDouble(i);
            y = newY.getDouble(i);
            if (x < X.getDouble(0) || x > X.getDouble((int) X.getSize() - 1)) {
                r.setDouble(i, Double.NaN);
            } else if (y < Y.getDouble(0) || y > Y.getDouble((int) Y.getSize() - 1)) {
                r.setDouble(i, Double.NaN);
            } else {
                v = toStation(a, X, Y, x, y);
                r.setDouble(i, v);
            }
        }

        return r;
    }

    /**
     * Resample grid array with neighbor method
     *
     * @param a The sample array
     * @param X X coordinate of the sample array
     * @param Y Y coordinate of the sample array
     * @param newX X coordinate of the query points
     * @param newY Y coordinate of the query points
     * @return Resampled array
     */
    public static Array resample_Neighbor(Array a, Array X, Array Y, Array newX, Array newY) {
        int i;
        int n = (int) newX.getSize();
        double x, y, v;
        Array r = Array.factory(DataType.DOUBLE, newX.getShape());

        for (i = 0; i < n; i++) {
            x = newX.getDouble(i);
            y = newY.getDouble(i);
            if (x < X.getDouble(0) || x > X.getDouble((int) X.getSize() - 1)) {
                r.setDouble(i, Double.NaN);
            } else if (y < Y.getDouble(0) || y > Y.getDouble((int) Y.getSize() - 1)) {
                r.setDouble(i, Double.NaN);
            } else {
                v = toStation_Neighbor(a, X, Y, x, y);
                r.setDouble(i, v);
            }
        }

        return r;
    }

    /**
     * Interpolate array data
     *
     * @param a Array
     * @param X X coordinates
     * @param Y Y coordinates
     * @return Result array data
     */
    public Array interpolate(Array a, List<Number> X, List<Number> Y) {
        int nxNum = X.size() * 2 - 1;
        int nyNum = Y.size() * 2 - 1;
        List<Number> newX = new ArrayList<>();
        List<Number> newY = new ArrayList<>();
        int i;

        for (i = 0; i < nxNum; i++) {
            if (i % 2 == 0) {
                newX.add(X.get(i / 2).doubleValue());
            } else {
                newX.add((X.get((i - 1) / 2).doubleValue() + X.get((i - 1) / 2 + 1).doubleValue()) / 2);
            }
        }
        for (i = 0; i < nyNum; i++) {
            if (i % 2 == 0) {
                newY.add(Y.get(i / 2).doubleValue());
            } else {
                newY.add((Y.get((i - 1) / 2).doubleValue() + Y.get((i - 1) / 2 + 1).doubleValue()) / 2);
            }
        }

        return resample_Bilinear(a, X, Y, newX, newY);
    }

    /**
     * Multidimensional interpolation on regular grids.
     *
     * @param points The points defining the regular grid in n dimensions.
     * @param values The data on the regular grid in n dimensions.
     * @param xi The coordinates to sample the gridded data at
     * @return Interpolation value
     */
    public static double interpn_s(List<List<Number>> points, Array values, List<Number> xi) {
        Object[] r = findIndices(points, xi);
        boolean outBounds = (boolean) r[2];
        if (outBounds) {
            return Double.NaN;
        } else {
            double v, weight;
            Index index = values.getIndex();
            int[] indices = (int[]) r[0];
            double[] distances = (double[]) r[1];
            v = 0;
            List<Index> ii = new ArrayList<>();
            iterIndex(ii, index, indices, 0);
            int n = indices.length;
            for (Index idx : ii) {
                weight = 1;
                for (int i = 0; i < n; i++) {
                    weight *= idx.getCurrentCounter()[i] == indices[i] ? 1 - distances[i] : distances[i];
                }
                v += values.getDouble(idx) * weight;
            }

            return v;
        }
    }

    /**
     * Multidimensional interpolation on regular grids.
     *
     * @param points The points defining the regular grid in n dimensions.
     * @param values The data on the regular grid in n dimensions.
     * @param xi The coordinates to sample the gridded data at
     * @return Interpolation value
     */
    public static double interpn_s(List<Array> points, Array values, Array xi) {
        Object[] r = findIndices(points, xi);
        boolean outBounds = (boolean) r[2];
        if (outBounds) {
            return Double.NaN;
        } else {
            double v, weight;
            Index index = values.getIndex();
            int[] indices = (int[]) r[0];
            double[] distances = (double[]) r[1];
            v = 0;
            List<Index> ii = new ArrayList<>();
            iterIndex(ii, index, indices, 0);
            int n = indices.length;
            for (Index idx : ii) {
                weight = 1;
                for (int i = 0; i < n; i++) {
                    weight *= idx.getCurrentCounter()[i] == indices[i] ? 1 - distances[i] : distances[i];
                }
                v += values.getDouble(idx) * weight;
            }

            return v;
        }
    }

    /**
     * Multidimensional interpolation on regular grids.
     *
     * @param points The points defining the regular grid in n dimensions.
     * @param values The data on the regular grid in n dimensions.
     * @param xi The coordinates to sample the gridded data at - 2D
     * @return Interpolation value
     */
    public static Array interpn(List<Array> points, Array values, List<Array> xi) {
        int n = xi.size();
        Array r = Array.factory(DataType.DOUBLE, new int[]{n});
        for (int i = 0; i < n; i++) {
            Array x = xi.get(i);
            r.setDouble(i, interpn_s(points, values, x));
        }

        return r;
    }

    /**
     * Multidimensional interpolation on regular grids.
     *
     * @param points The points defining the regular grid in n dimensions.
     * @param values The data on the regular grid in n dimensions.
     * @param xi The coordinates to sample the gridded data at - 2D
     * @return Interpolation value
     * @throws ucar.ma2.InvalidRangeException
     */
    public static Object interpn(List<Array> points, Array values, Array xi) throws InvalidRangeException {
        if (xi.getRank() == 1) {
            return interpn_s(points, values, xi);
        }

        int n = xi.getShape()[0];
        int m = xi.getShape()[1];
        Array r = Array.factory(DataType.DOUBLE, new int[]{n});
        for (int i = 0; i < n; i++) {
            Array x = xi.section(new int[]{i, 0}, new int[]{1, m});
            r.setDouble(i, interpn_s(points, values, x));
        }

        return r;
    }

    private static void iterIndex(List<Index> ii, Index index, int[] indices, int idx) {
        int n = indices.length;
        if (idx < n - 1) {
            index.setDim(idx, indices[idx]);
            iterIndex(ii, index, indices, idx + 1);
            index.setDim(idx, indices[idx] + 1);
            iterIndex(ii, index, indices, idx + 1);
        } else {
            index.setDim(idx, indices[idx]);
            ii.add((Index) index.clone());
            //System.out.println(index);
            index.setDim(idx, indices[idx] + 1);
            ii.add((Index) index.clone());
            //System.out.println(index);
        }
    }

    /**
     * Find indices
     *
     * @param points The points defining the regular grid in n dimensions.
     * @param xi The coordinates to sample the gridded data at
     * @return Indices
     */
    public static Object[] findIndices(List<List<Number>> points, List<Number> xi) {
        int n = points.size();
        int[] indices = new int[n];
        double[] distances = new double[n];
        boolean outBounds = false;
        double x;
        List<Number> a;
        for (int i = 0; i < n; i++) {
            x = xi.get(i).doubleValue();
            a = points.get(i);
            int idx = searchSorted(a, x);
            if (idx < 0) {
                outBounds = true;
                idx = 0;
            }
            indices[i] = idx;
            distances[i] = (x - a.get(idx).doubleValue()) / (a.get(idx + 1).doubleValue() - a.get(idx).doubleValue());
        }

        return new Object[]{indices, distances, outBounds};
    }

    /**
     * Find indices
     *
     * @param points The points defining the regular grid in n dimensions.
     * @param xi The coordinates to sample the gridded data at
     * @return Indices
     */
    public static Object[] findIndices(List<Array> points, Array xi) {
        int n = points.size();
        int[] indices = new int[n];
        double[] distances = new double[n];
        boolean outBounds = false;
        double x;
        Array a;
        for (int i = 0; i < n; i++) {
            x = xi.getDouble(i);
            a = points.get(i);
            int idx = searchSorted(a, x);
            if (idx < 0) {
                outBounds = true;
                idx = 0;
            }
            indices[i] = idx;
            distances[i] = (x - a.getDouble(idx)) / (a.getDouble(idx + 1) - a.getDouble(idx));
        }

        return new Object[]{indices, distances, outBounds};
    }

    /**
     * Search sorted list index
     *
     * @param a Sorted list
     * @param v value
     * @return Index
     */
    public static int searchSorted(List<Number> a, double v) {
        int idx = -1;
        int n = a.size();
        if (a.get(1).doubleValue() > a.get(0).doubleValue()) {
            if (v < a.get(0).doubleValue()) {
                return idx;
            }

            if (v > a.get(n - 1).doubleValue()) {
                return idx;
            }

            for (int i = 1; i < n; i++) {
                if (v < a.get(i).doubleValue()) {
                    idx = i - 1;
                    break;
                }
            }
        } else {
            if (v > a.get(0).doubleValue()) {
                return idx;
            }

            if (v < a.get(n - 1).doubleValue()) {
                return idx;
            }

            for (int i = 1; i < n; i++) {
                if (v > a.get(i).doubleValue()) {
                    idx = i - 1;
                    break;
                }
            }
        }

        return idx;
    }

    /**
     * Search sorted list index
     *
     * @param a Sorted list
     * @param v value
     * @return Index
     */
    public static int searchSorted(Array a, double v) {
        a = a.copyIfView();
        int idx = -1;
        int n = (int) a.getSize();
        if (a.getDouble(1) > a.getDouble(0)) {
            if (v < a.getDouble(0)) {
                return idx;
            }

            if (v > a.getDouble(n - 1)) {
                return idx;
            }

            for (int i = 1; i < n; i++) {
                if (v < a.getDouble(i)) {
                    idx = i - 1;
                    break;
                }
            }
        } else {
            if (v > a.getDouble(0)) {
                return idx;
            }

            if (v < a.getDouble(n - 1)) {
                return idx;
            }

            for (int i = 1; i < n; i++) {
                if (v > a.getDouble(i)) {
                    idx = i - 1;
                    break;
                }
            }
        }

        return idx;
    }
    
    /**
     * Interpolates data with any shape over a specified axis.
     *
     * @param x Desired interpolated value
     * @param xp The x-coordinates of the data points.
     * @param a The data to be interpolated.
     * @param axis The axis to interpolate over.
     * @return Interpolated data
     * @throws InvalidRangeException
     */
    public static Array interpolate_1d(double x, Array xp, Array a, int axis) throws InvalidRangeException {
        int[] dataShape = xp.getShape();
        int[] shape = new int[dataShape.length - 1];
        for (int i = 0; i < dataShape.length; i++) {
            if (i < axis) {
                shape[i] = dataShape[i];
            } else if (i > axis) {
                shape[i - 1] = dataShape[i];
            }
        }

        Array r = Array.factory(a.getDataType(), shape);
        Index indexr = r.getIndex();
        Index indexa = a.getIndex();
        int[] current, dcurrent = new int[dataShape.length];
        int idx;
        Array tArray;
        double belowp, abovep, belowa, abovea;
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
                    dcurrent[j] = current[idx];
                }
            }
            tArray = ArrayMath.section(xp, ranges);
            idx = searchSorted(tArray, x);
            if (idx < 0) {
                r.setDouble(i, Double.NaN);
            } else if (idx == dataShape[axis] - 1) {
                dcurrent[axis] = idx;
                r.setObject(i, a.getObject(indexa.set(dcurrent)));
            } else {
                dcurrent[axis] = idx;
                indexa.set(dcurrent);
                belowp = xp.getDouble(indexa);
                belowa = a.getDouble(indexa);
                dcurrent[axis] = idx + 1;
                indexa.set(dcurrent);
                abovep = xp.getDouble(indexa);
                abovea = a.getDouble(indexa);
                r.setDouble(i, (x - belowp) / (abovep - belowp) * (abovea - belowa) + belowa);
            }
            indexr.incr();
        }

        return r;
    }

    /**
     * Interpolates data with any shape over a specified axis.
     *
     * @param xa Desired interpolated values
     * @param xp The x-coordinates of the data points.
     * @param a The data to be interpolated.
     * @param axis The axis to interpolate over.
     * @return Interpolated data
     * @throws InvalidRangeException
     */
    public static Array interpolate_1d(Array xa, Array xp, Array a, int axis) throws InvalidRangeException {
        int[] dataShape = xp.getShape();
        int[] shape = new int[dataShape.length];
        int[] tshape = new int[shape.length - 1];
        for (int i = 0; i < dataShape.length; i++) {
            if (i == axis) {
                shape[i] = xa.getShape()[0];
            } else {
                shape[i] = dataShape[i];
                if (i < axis) {
                    tshape[i] = dataShape[i];
                } else {
                    tshape[i - 1] = dataShape[i];
                }
            }
        }

        Array r = Array.factory(a.getDataType(), shape);
        Array rr = Array.factory(DataType.BYTE, tshape);
        Index indexrr = rr.getIndex();
        Index indexr = r.getIndex();
        Index indexa = a.getIndex();
        int[] currentrr, currenta = new int[dataShape.length], currentr = new int[shape.length];
        int idx;
        Array tArray;
        double belowp, abovep, belowa, abovea, x;
        for (int i = 0; i < rr.getSize(); i++) {
            currentrr = indexrr.getCurrentCounter();
            List<Range> ranges = new ArrayList<>();
            for (int j = 0; j < dataShape.length; j++) {
                if (j == axis) {
                    ranges.add(new Range(0, dataShape[j] - 1, 1));
                } else {
                    idx = j;
                    if (idx > axis) {
                        idx -= 1;
                    }
                    ranges.add(new Range(currentrr[idx], currentrr[idx], 1));
                    currenta[j] = currentrr[idx];
                    currentr[j] = currentrr[idx];
                }
            }
            tArray = ArrayMath.section(xp, ranges);
            for (int j = 0; j < xa.getSize(); j++) {
                x = xa.getDouble(j);
                idx = searchSorted(tArray, x);
                currentr[axis] = j;
                indexr.set(currentr);
                if (idx < 0) {
                    r.setDouble(indexr, Double.NaN);
                } else if (idx == dataShape[axis] - 1) {
                    currenta[axis] = idx;
                    r.setObject(indexr, a.getObject(indexa.set(currenta)));
                } else {
                    currenta[axis] = idx;
                    indexa.set(currenta);
                    belowp = xp.getDouble(indexa);
                    belowa = a.getDouble(indexa);
                    currenta[axis] = idx + 1;
                    indexa.set(currenta);
                    abovep = xp.getDouble(indexa);
                    abovea = a.getDouble(indexa);
                    r.setDouble(indexr, (x - belowp) / (abovep - belowp) * (abovea - belowa) + belowa);
                }
            }
            indexrr.incr();
        }

        return r;
    }

    /**
     * Interpolate data to a station point
     *
     * @param data Data array
     * @param xArray X array
     * @param yArray Y array
     * @param x X coordinate of the station
     * @param y Y coordinate of the station
     * @param missingValue Missing value
     * @return Interpolated value
     */
    public static double toStation(Array data, List<Number> xArray, List<Number> yArray, double x, double y,
            double missingValue) {
        double iValue = Double.NaN;
        int nx = xArray.size();
        int ny = yArray.size();
        if (x < xArray.get(0).doubleValue() || x > xArray.get(nx - 1).doubleValue()
                || y < yArray.get(0).doubleValue() || y > yArray.get(ny - 1).doubleValue()) {
            return iValue;
        }

        //Get x/y index
        int xIdx = 0, yIdx = 0;
        int i;
        boolean isIn = false;
        for (i = 1; i < nx; i++) {
            if (x < xArray.get(i).doubleValue()) {
                xIdx = i - 1;
                isIn = true;
                break;
            }
        }
        if (!isIn) {
            xIdx = nx - 2;
        }
        isIn = false;
        for (i = 1; i < ny; i++) {
            if (y < yArray.get(i).doubleValue()) {
                yIdx = i - 1;
                isIn = true;
                break;
            }
        }
        if (!isIn) {
            yIdx = ny - 2;
        }

        int i1 = yIdx;
        int j1 = xIdx;
        int i2 = i1 + 1;
        int j2 = j1 + 1;
        Index index = data.getIndex();
        double a = data.getDouble(index.set(i1, j1));
        double b = data.getDouble(index.set(i1, j2));
        double c = data.getDouble(index.set(i2, j1));
        double d = data.getDouble(index.set(i2, j2));
        List<java.lang.Double> dList = new ArrayList<>();
        if (!Double.isNaN(a) && !MIMath.doubleEquals(a, missingValue)) {
            dList.add(a);
        }
        if (!Double.isNaN(b) && !MIMath.doubleEquals(b, missingValue)) {
            dList.add(b);
        }
        if (!Double.isNaN(c) && !MIMath.doubleEquals(c, missingValue)) {
            dList.add(c);
        }
        if (!Double.isNaN(d) && !MIMath.doubleEquals(d, missingValue)) {
            dList.add(d);
        }

        if (dList.isEmpty()) {
            return iValue;
        } else if (dList.size() == 1) {
            iValue = dList.get(0);
        } else if (dList.size() <= 3) {
            double aSum = 0;
            for (double dd : dList) {
                aSum += dd;
            }
            iValue = aSum / dList.size();
        } else {
            double dx = xArray.get(xIdx + 1).doubleValue() - xArray.get(xIdx).doubleValue();
            double dy = yArray.get(yIdx + 1).doubleValue() - yArray.get(yIdx).doubleValue();
            double x1val = a + (c - a) * (y - yArray.get(i1).doubleValue()) / dy;
            double x2val = b + (d - b) * (y - yArray.get(i1).doubleValue()) / dy;
            iValue = x1val + (x2val - x1val) * (x - xArray.get(j1).doubleValue()) / dx;
        }

        return iValue;
    }

    /**
     * Interpolate data to a station point
     *
     * @param data Data array
     * @param xArray X array
     * @param yArray Y array
     * @param x X coordinate of the station
     * @param y Y coordinate of the station
     * @return Interpolated value
     */
    public static double toStation(Array data, List<Number> xArray, List<Number> yArray, double x, double y) {
        double iValue = Double.NaN;
        int nx = xArray.size();
        int ny = yArray.size();
        if (x < xArray.get(0).doubleValue() || x > xArray.get(nx - 1).doubleValue()
                || y < yArray.get(0).doubleValue() || y > yArray.get(ny - 1).doubleValue()) {
            return iValue;
        }

        //Get x/y index
        int xIdx = 0, yIdx = 0;
        int i;
        boolean isIn = false;
        for (i = 1; i < nx; i++) {
            if (x < xArray.get(i).doubleValue()) {
                xIdx = i - 1;
                isIn = true;
                break;
            }
        }
        if (!isIn) {
            xIdx = nx - 2;
        }
        isIn = false;
        for (i = 1; i < ny; i++) {
            if (y < yArray.get(i).doubleValue()) {
                yIdx = i - 1;
                isIn = true;
                break;
            }
        }
        if (!isIn) {
            yIdx = ny - 2;
        }

        int i1 = yIdx;
        int j1 = xIdx;
        int i2 = i1 + 1;
        int j2 = j1 + 1;
        Index index = data.getIndex();
        double a = data.getDouble(index.set(i1, j1));
        double b = data.getDouble(index.set(i1, j2));
        double c = data.getDouble(index.set(i2, j1));
        double d = data.getDouble(index.set(i2, j2));
        List<java.lang.Double> dList = new ArrayList<>();
        if (!Double.isNaN(a)) {
            dList.add(a);
        }
        if (!Double.isNaN(b)) {
            dList.add(b);
        }
        if (!Double.isNaN(c)) {
            dList.add(c);
        }
        if (!Double.isNaN(d)) {
            dList.add(d);
        }

        if (dList.isEmpty()) {
            return iValue;
        } else if (dList.size() == 1) {
            iValue = dList.get(0);
        } else if (dList.size() <= 3) {
            double aSum = 0;
            for (double dd : dList) {
                aSum += dd;
            }
            iValue = aSum / dList.size();
        } else {
            double dx = xArray.get(xIdx + 1).doubleValue() - xArray.get(xIdx).doubleValue();
            double dy = yArray.get(yIdx + 1).doubleValue() - yArray.get(yIdx).doubleValue();
            double x1val = a + (c - a) * (y - yArray.get(i1).doubleValue()) / dy;
            double x2val = b + (d - b) * (y - yArray.get(i1).doubleValue()) / dy;
            iValue = x1val + (x2val - x1val) * (x - xArray.get(j1).doubleValue()) / dx;
        }

        return iValue;
    }

    /**
     * Get value index in a dimension array
     *
     * @param dim Dimension array
     * @param v The value
     * @return value index
     */
    public static int getDimIndex(Array dim, Number v) {
        dim = dim.copyIfView();
        int idx;
        switch (dim.getDataType()) {
            case BYTE:
                return Arrays.binarySearch((byte[]) dim.getStorage(), v.byteValue());
            case INT:
                return Arrays.binarySearch((int[]) dim.getStorage(), v.intValue());
            case SHORT:
                return Arrays.binarySearch((short[]) dim.getStorage(), v.shortValue());
            case LONG:
                return Arrays.binarySearch((long[]) dim.getStorage(), v.longValue());
            case FLOAT:
                return Arrays.binarySearch((float[]) dim.getStorage(), v.floatValue());
            case DOUBLE:
                return Arrays.binarySearch((double[]) dim.getStorage(), v.doubleValue());
        }

        int n = (int) dim.getSize();
        if (v.doubleValue() < dim.getDouble(0) || v.doubleValue() > dim.getDouble(n - 1)) {
            return -1;
        }

        idx = n - 1;
        for (int i = 1; i < n; i++) {
            if (v.doubleValue() < dim.getDouble(i)) {
                idx = i - 1;
                break;
            }
        }
        return idx;
    }

    private static int[] gridIndex(Array xdim, Array ydim, double x, double y) {
        int xn = (int) xdim.getSize();
        int yn = (int) ydim.getSize();
        int xIdx = getDimIndex(xdim, x);
        if (xIdx == -1 || xIdx == - (xn + 1)) {
            return null;
        } else if (xIdx < 0) {
            xIdx = - xIdx - 2;
        }

        int yIdx = getDimIndex(ydim, y);
        if (yIdx == -1 || yIdx == -(yn + 1)) {
            return null;
        } else if (yIdx < 0) {
            yIdx = - yIdx - 2;
        }

        if (xIdx == xn - 1) {
            xIdx = xn - 2;
        }
        if (yIdx == yn - 1) {
            yIdx = yn - 2;
        }
        int i1 = yIdx;
        int j1 = xIdx;
        int i2 = i1 + 1;
        int j2 = j1 + 1;

        return new int[]{i1, j1, i2, j2};
    }

    private static double bilinear(Array data, Index dindex, Array xdim, Array ydim, double x, double y) {
        xdim = xdim.copyIfView();
        ydim = ydim.copyIfView();

        double iValue = Double.NaN;
        int[] xyIdx = gridIndex(xdim, ydim, x, y);
        if (xyIdx == null) {
            return iValue;
        }

        int i1 = xyIdx[0];
        int j1 = xyIdx[1];
        int i2 = xyIdx[2];
        int j2 = xyIdx[3];
        Index index = data.getIndex();
        int n = index.getRank();
        for (int i = 0; i < n - 2; i++) {
            index.setDim(i, dindex.getCurrentCounter()[i]);
        }
        index.setDim(n - 2, i1);
        index.setDim(n - 1, j1);
        double a = data.getDouble(index);
        index.setDim(n - 1, j2);
        double b = data.getDouble(index);
        index.setDim(n - 2, i2);
        index.setDim(n - 1, j1);
        double c = data.getDouble(index);
        index.setDim(n - 2, i2);
        index.setDim(n - 1, j2);
        double d = data.getDouble(index);
        List<java.lang.Double> dList = new ArrayList<>();
        if (!Double.isNaN(a)) {
            dList.add(a);
        }
        if (!Double.isNaN(b)) {
            dList.add(b);
        }
        if (!Double.isNaN(c)) {
            dList.add(c);
        }
        if (!Double.isNaN(d)) {
            dList.add(d);
        }

        if (dList.isEmpty()) {
            return iValue;
        } else if (dList.size() == 1) {
            iValue = dList.get(0);
        } else if (dList.size() <= 3) {
            double aSum = 0;
            for (double dd : dList) {
                aSum += dd;
            }
            iValue = aSum / dList.size();
        } else {
            double dx = xdim.getDouble(j1 + 1) - xdim.getDouble(j1);
            double dy = ydim.getDouble(i1 + 1) - ydim.getDouble(i1);
            double x1val = a + (c - a) * (y - ydim.getDouble(i1)) / dy;
            double x2val = b + (d - b) * (y - ydim.getDouble(i1)) / dy;
            iValue = x1val + (x2val - x1val) * (x - xdim.getDouble(j1)) / dx;
        }

        return iValue;
    }

    /**
     * Interpolate data to a station point
     *
     * @param data Data array
     * @param xArray X array
     * @param yArray Y array
     * @param x X coordinate of the station
     * @param y Y coordinate of the station
     * @return Interpolated value
     */
    public static double toStation(Array data, Array xArray, Array yArray, double x, double y) {
        xArray = xArray.copyIfView();
        yArray = yArray.copyIfView();

        double iValue = Double.NaN;
        int nx = (int) xArray.getSize();
        int ny = (int) yArray.getSize();
        if (x < xArray.getDouble(0) || x > xArray.getDouble(nx - 1)
                || y < yArray.getDouble(0) || y > yArray.getDouble(ny - 1)) {
            return iValue;
        }

        //Get x/y index
        int xIdx = 0, yIdx = 0;
        int i;
        boolean isIn = false;
        for (i = 1; i < nx; i++) {
            if (x < xArray.getDouble(i)) {
                xIdx = i - 1;
                isIn = true;
                break;
            }
        }
        if (!isIn) {
            xIdx = nx - 2;
        }
        isIn = false;
        for (i = 1; i < ny; i++) {
            if (y < yArray.getDouble(i)) {
                yIdx = i - 1;
                isIn = true;
                break;
            }
        }
        if (!isIn) {
            yIdx = ny - 2;
        }

        int i1 = yIdx;
        int j1 = xIdx;
        int i2 = i1 + 1;
        int j2 = j1 + 1;
        Index index = data.getIndex();
        double a = data.getDouble(index.set(i1, j1));
        double b = data.getDouble(index.set(i1, j2));
        double c = data.getDouble(index.set(i2, j1));
        double d = data.getDouble(index.set(i2, j2));
        List<java.lang.Double> dList = new ArrayList<>();
        if (!Double.isNaN(a)) {
            dList.add(a);
        }
        if (!Double.isNaN(b)) {
            dList.add(b);
        }
        if (!Double.isNaN(c)) {
            dList.add(c);
        }
        if (!Double.isNaN(d)) {
            dList.add(d);
        }

        if (dList.isEmpty()) {
            return iValue;
        } else if (dList.size() == 1) {
            iValue = dList.get(0);
        } else if (dList.size() <= 3) {
            double aSum = 0;
            for (double dd : dList) {
                aSum += dd;
            }
            iValue = aSum / dList.size();
        } else {
            double dx = xArray.getDouble(xIdx + 1) - xArray.getDouble(xIdx);
            double dy = yArray.getDouble(yIdx + 1) - yArray.getDouble(yIdx);
            double x1val = a + (c - a) * (y - yArray.getDouble(i1)) / dy;
            double x2val = b + (d - b) * (y - yArray.getDouble(i1)) / dy;
            iValue = x1val + (x2val - x1val) * (x - xArray.getDouble(j1)) / dx;
        }

        return iValue;
    }

    /**
     * Interpolate data to a station point
     *
     * @param data Data array
     * @param xArray X array
     * @param yArray Y array
     * @param x X coordinate of the station
     * @param y Y coordinate of the station
     * @param missingValue Missing value
     * @return Interpolated value
     */
    public static double toStation_Neighbor(Array data, List<Number> xArray, List<Number> yArray, double x, double y,
            double missingValue) {
        double iValue = Double.NaN;
        int nx = xArray.size();
        int ny = yArray.size();
        if (x < xArray.get(0).doubleValue() || x > xArray.get(nx - 1).doubleValue()
                || y < yArray.get(0).doubleValue() || y > yArray.get(ny - 1).doubleValue()) {
            return iValue;
        }

        //Get x/y index
        int xIdx = 0, yIdx = 0;
        int i;
        boolean isIn = false;
        for (i = 1; i < nx; i++) {
            if (x < xArray.get(i).doubleValue()) {
                xIdx = i - 1;
                isIn = true;
                break;
            }
        }
        if (!isIn) {
            xIdx = nx - 2;
        }
        isIn = false;
        for (i = 1; i < ny; i++) {
            if (y < yArray.get(i).doubleValue()) {
                yIdx = i - 1;
                isIn = true;
                break;
            }
        }
        if (!isIn) {
            yIdx = ny - 2;
        }

        int i1 = yIdx;
        int j1 = xIdx;
        int i2 = i1 + 1;
        int j2 = j1 + 1;
        Index index = data.getIndex();
        double a = data.getDouble(index.set(i1, j1));
        double b = data.getDouble(index.set(i1, j2));
        double c = data.getDouble(index.set(i2, j1));
        double d = data.getDouble(index.set(i2, j2));

        if (Math.abs(x - xArray.get(j1).doubleValue()) < Math.abs(xArray.get(j2).doubleValue() - x)) {
            if (Math.abs(y - yArray.get(i1).doubleValue()) < Math.abs(yArray.get(i2).doubleValue() - y)) {
                iValue = a;
            } else {
                iValue = c;
            }
        } else if (Math.abs(y - yArray.get(i1).doubleValue()) < Math.abs(yArray.get(i2).doubleValue() - y)) {
            iValue = b;
        } else {
            iValue = d;
        }

        return iValue;
    }

    /**
     * Interpolate data to a station point
     *
     * @param data Data array
     * @param xArray X array
     * @param yArray Y array
     * @param x X coordinate of the station
     * @param y Y coordinate of the station
     * @return Interpolated value
     */
    public static double toStation_Neighbor(Array data, List<Number> xArray, List<Number> yArray, double x, double y) {
        //ouble iValue = Double.NaN;
        int nx = xArray.size();
        int ny = yArray.size();
        if (x < xArray.get(0).doubleValue() || x > xArray.get(nx - 1).doubleValue()
                || y < yArray.get(0).doubleValue() || y > yArray.get(ny - 1).doubleValue()) {
            return Double.NaN;
        }

        //Get x/y index
        int xIdx = 0, yIdx = 0;
        int i;
        boolean isIn = false;
        for (i = 1; i < nx; i++) {
            if (x < xArray.get(i).doubleValue()) {
                xIdx = i - 1;
                isIn = true;
                break;
            }
        }
        if (!isIn) {
            xIdx = nx - 2;
        }
        isIn = false;
        for (i = 1; i < ny; i++) {
            if (y < yArray.get(i).doubleValue()) {
                yIdx = i - 1;
                isIn = true;
                break;
            }
        }
        if (!isIn) {
            yIdx = ny - 2;
        }

        int i1 = yIdx;
        int j1 = xIdx;
        int i2 = i1 + 1;
        int j2 = j1 + 1;
        Index index = data.getIndex();
        double a = data.getDouble(index.set(i1, j1));
        double b = data.getDouble(index.set(i1, j2));
        double c = data.getDouble(index.set(i2, j1));
        double d = data.getDouble(index.set(i2, j2));

        double iValue;
        if (Math.abs(x - xArray.get(j1).doubleValue()) < Math.abs(xArray.get(j2).doubleValue() - x)) {
            if (Math.abs(y - yArray.get(i1).doubleValue()) < Math.abs(yArray.get(i2).doubleValue() - y)) {
                iValue = a;
            } else {
                iValue = c;
            }
        } else if (Math.abs(y - yArray.get(i1).doubleValue()) < Math.abs(yArray.get(i2).doubleValue() - y)) {
            iValue = b;
        } else {
            iValue = d;
        }

        return iValue;
    }

    /**
     * Interpolate data to a station point
     *
     * @param data Data array
     * @param xArray X array
     * @param yArray Y array
     * @param x X coordinate of the station
     * @param y Y coordinate of the station
     * @return Interpolated value
     */
    public static double toStation_Neighbor(Array data, Array xArray, Array yArray, double x, double y) {
        xArray = xArray.copyIfView();
        yArray = yArray.copyIfView();

        //ouble iValue = Double.NaN;
        int nx = (int) xArray.getSize();
        int ny = (int) yArray.getSize();
        if (x < xArray.getDouble(0) || x > xArray.getDouble(nx - 1)
                || y < yArray.getDouble(0) || y > yArray.getDouble(ny - 1)) {
            return Double.NaN;
        }

        //Get x/y index
        int xIdx = 0, yIdx = 0;
        int i;
        boolean isIn = false;
        for (i = 1; i < nx; i++) {
            if (x < xArray.getDouble(i)) {
                xIdx = i - 1;
                isIn = true;
                break;
            }
        }
        if (!isIn) {
            xIdx = nx - 2;
        }
        isIn = false;
        for (i = 1; i < ny; i++) {
            if (y < yArray.getDouble(i)) {
                yIdx = i - 1;
                isIn = true;
                break;
            }
        }
        if (!isIn) {
            yIdx = ny - 2;
        }

        int i1 = yIdx;
        int j1 = xIdx;
        int i2 = i1 + 1;
        int j2 = j1 + 1;
        Index index = data.getIndex();
        double a = data.getDouble(index.set(i1, j1));
        double b = data.getDouble(index.set(i1, j2));
        double c = data.getDouble(index.set(i2, j1));
        double d = data.getDouble(index.set(i2, j2));

        double iValue;
        if (Math.abs(x - xArray.getDouble(j1)) < Math.abs(xArray.getDouble(j2) - x)) {
            if (Math.abs(y - yArray.getDouble(i1)) < Math.abs(yArray.getDouble(i2) - y)) {
                iValue = a;
            } else {
                iValue = c;
            }
        } else if (Math.abs(y - yArray.getDouble(i1)) < Math.abs(yArray.getDouble(i2) - y)) {
            iValue = b;
        } else {
            iValue = d;
        }

        return iValue;
    }

    // </editor-fold>
    // <editor-fold desc="Time average">
    // </editor-fold>
}
