/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.meteodata;

import java.nio.ByteBuffer;
import java.util.Formatter;
import java.util.List;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.ArrayChar;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.Index;

/**
 *
 * @author Yaqiang Wang
 */
public class Attribute {

    private String name;
    private DataType dataType;
    private String svalue;
    private int nelems; // can be 0 or greater
    private Array values;
    private boolean isUnsigned;

    /**
     * Constructor
     *
     * @param name Name
     */
    public Attribute(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Trying to set name to null on " + this);
        }
        this.name = name;
    }

    /**
     * Constructor
     *
     * @param name Name of attribute
     * @param values Values of attribute
     */
    public Attribute(String name, Array values) {
        this(name);
        setValues(values);
    }

    /**
     * Construct attribute with list of String or Number values.
     *
     * @param name name of attribute
     * @param values list of values. must be String or Number, must all be the
     * same type, and have at least 1 member
     */
    public Attribute(String name, List values) {
        this(name);
        int n = values.size();
        Object pa;

        Class c = values.get(0).getClass();
        if (c == String.class) {
            String[] va  = new String[n];
            pa = va;
            for (int i = 0; i < n; i++) {
                va[i] = (String) values.get(i);
            }

        } else if (c == Integer.class) {
            int[] va  = new int[n];
            pa = va;
            for (int i = 0; i < n; i++) {
                va[i] = (Integer) values.get(i);
            }

        } else if (c == Double.class) {
            double[] va  = new double[n];
            pa = va;
            for (int i = 0; i < n; i++) {
                va[i] = (Double) values.get(i);
            }

        } else if (c == Float.class) {
            float[] va  = new float[n];
            pa = va;
            for (int i = 0; i < n; i++) {
                va[i] = (Float) values.get(i);
            }

        } else if (c == Short.class) {
            short[] va  = new short[n];
            pa = va;
            for (int i = 0; i < n; i++) {
                va[i] = (Short) values.get(i);
            }

        } else if (c == Byte.class) {
            byte[] va  = new byte[n];
            pa = va;
            for (int i = 0; i < n; i++) {
                va[i] = (Byte) values.get(i);
            }

        } else if (c == Long.class) {
            long[] va  = new long[n];
            pa = va;
            for (int i = 0; i < n; i++) {
                va[i] = (Long) values.get(i);
            }

        } else {
            throw new IllegalArgumentException("unknown type for Attribute = " + c.getName());
        }

        setValues(Array.factory(c, new int[]{n}, pa));
    }

    /**
     * Create a scalar numeric-valued Attribute.
     *
     * @param name name of Attribute
     * @param val value of Attribute
     */
    public Attribute(String name, Number val) {
        this(name, val, false);
    }

    public Attribute(String name, Number val, boolean isUnsigned) {
        this(name);

        int[] shape = new int[1];
        shape[0] = 1;
        DataType dt = DataType.getType(val.getClass());
        Array vala = Array.factory(dt, shape);
        Index ima = vala.getIndex();
        vala.setObject(ima.set0(0), val);
        setValues(vala);
        this.isUnsigned = isUnsigned;
    }

    /**
     * Create a String-valued Attribute.
     *
     * @param name name of Attribute
     * @param val value of Attribute
     */
    public Attribute(String name, String val) {
        this(name);
        setStringValue(val);
    }

    /**
     * Get short name
     *
     * @return Short name
     */
    public String getShortName() {
        return this.name;
    }

    /**
     * Set short name
     *
     * @param value Short name
     */
    public void setShortName(String value) {
        this.name = value;
    }

    /**
     * Get name
     *
     * @return Name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set name
     *
     * @param value Name
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Get length
     *
     * @return Length
     */
    public int getLength() {
        return this.nelems;
    }

    /**
     * Get string value
     *
     * @return String value
     */
    public String getStringValue() {
        return this.svalue;
    }

    /**
     * Get string value
     *
     * @param index Index
     * @return String value
     */
    public String getStringValue(int index) {
        if (this.dataType != DataType.STRING) {
            return null;
        }
        if ((this.svalue != null) && (index == 0)) {
            return this.svalue;
        }
        return _getStringValue(index);
    }

    private String _getStringValue(int index) {
        if ((index < 0) || (index >= this.nelems)) {
            return null;
        }
        return (String) this.values.getObject(index);
    }

    /**
     * set the value as a String, trimming trailing zeroes
     *
     * @param val value of Attribute
     */
    public void setStringValue(String val) {
        if (val == null) {
            return;
        }

        // get rid of trailing nul characters
        int len = val.length();
        while ((len > 0) && (val.charAt(len - 1) == 0)) {
            len--;
        }
        if (len != val.length()) {
            val = val.substring(0, len);
        }

        this.svalue = val;
        this.nelems = 1;
        this.dataType = DataType.STRING;

        //values = Array.factory(String.class, new int[]{1});
        //values.setObject(values.getIndex(), val);
        //setValues(values);
    }

    /**
     * Get values
     *
     * @return Value array
     */
    public Array getValues() {
        if ((this.values == null) && (this.svalue != null)) {
            this.values = Array.factory(DataType.STRING, new int[]{1});
            this.values.setObject(this.values.getIndex(), this.svalue);
        }
        return this.values;
    }

    /**
     * Get value
     *
     * @param index Index
     * @return Value
     */
    public Object getValue(int index) {
        if (isString()) {
            return getStringValue(index);
        }
        return getNumericValue(index);
    }

    /**
     * The value is String or not
     *
     * @return Boolean
     */
    public boolean isString() {
        return (this.dataType == DataType.STRING) && (null != getStringValue());
    }

    /**
     * The value is Unsigned or not
     *
     * @return Boolean
     */
    public boolean isUnsigned() {
        return (this.isUnsigned) || ((this.values != null) && (this.values.isUnsigned()));
    }

    /**
     * Get numeric value
     *
     * @return Numeric value
     */
    public Number getNumericValue() {
        return getNumericValue(0);
    }

    /**
     * Get numeric value
     *
     * @param index Index
     * @return Numeric value
     */
    public Number getNumericValue(int index) {
        if ((index < 0) || (index >= this.nelems)) {
            return null;
        }
        if (this.dataType == DataType.STRING) {
            try {
                return Double.parseDouble(getStringValue(index));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        if (this.dataType == DataType.BYTE) {
            return this.values.getByte(index);
        }
        if (this.dataType == DataType.SHORT) {
            return this.values.getShort(index);
        }
        if (this.dataType == DataType.INT) {
            return this.values.getInt(index);
        }
        if (this.dataType == DataType.FLOAT) {
            return this.values.getFloat(index);
        }
        if (this.dataType == DataType.DOUBLE) {
            return this.values.getDouble(index);
        }
        if (this.dataType == DataType.LONG) {
            return this.values.getLong(index);
        }
        return null;
    }

    /**
     * set the values from an Array
     *
     * @param arr value of Attribute
     */
    public void setValues(Array arr) {
        if (arr == null) {
            dataType = DataType.STRING;
            return;
        }

        if (DataType.getType(arr.getElementType()) == null) {
            throw new IllegalArgumentException("Cant set Attribute with type " + arr.getElementType());
        }

        if (arr.getElementType() == char.class) { // turn CHAR into STRING
            ArrayChar carr = (ArrayChar) arr;

            if (carr.getRank()
                    == 1) { // common case
                svalue = carr.getString();
                this.nelems = 1;
                this.dataType = DataType.STRING;
                return;
            }
            // otherwise its an array of Strings
            arr = carr.make1DStringArray();
        }

// this should be a utility somewhere
        if (arr.getElementType() == ByteBuffer.class) { // turn OPAQUE into BYTE
            int totalLen = 0;

            arr.resetLocalIterator();

            while (arr.hasNext()) {
                ByteBuffer bb = (ByteBuffer) arr.next();
                totalLen += bb.limit();
            }
            byte[] ba = new byte[totalLen];
            int pos = 0;

            arr.resetLocalIterator();

            while (arr.hasNext()) {
                ByteBuffer bb = (ByteBuffer) arr.next();
                System.arraycopy(bb.array(), 0, ba, pos, bb.limit());
                pos += bb.limit();
            }
            arr = Array.factory(DataType.BYTE, new int[]{totalLen}, ba);
        }

        if (arr.getRank() > 1) {
            arr = arr.reshape(new int[]{(int) arr.getSize()}); // make sure 1D
        }
        this.values = arr;
        this.nelems = (int) arr.getSize();
        this.dataType = DataType.getType(arr.getElementType());
    }

    protected void writeCDL(Formatter f) {
        f.format("%s", new Object[]{getShortName()});
        if (isString()) {
            f.format(" = ", new Object[0]);
            for (int i = 0; i < getLength(); i++) {
                if (i != 0) {
                    f.format(", ", new Object[0]);
                }
                String val = getStringValue(i);
                if (val != null) {
                    f.format("\"%s\"", new Object[]{val});
                }
            }
        } else {
            f.format(" = ", new Object[0]);
            for (int i = 0; i < getLength(); i++) {
                if (i != 0) {
                    f.format(", ", new Object[0]);
                }
                f.format("%s", new Object[]{getNumericValue(i)});
                if (this.dataType == DataType.FLOAT) {
                    f.format("f", new Object[0]);
                } else if (this.dataType == DataType.SHORT) {
                    if (isUnsigned()) {
                        f.format("US", new Object[0]);
                    } else {
                        f.format("S", new Object[0]);
                    }
                } else if (this.dataType == DataType.BYTE) {
                    if (isUnsigned()) {
                        f.format("UB", new Object[0]);
                    } else {
                        f.format("B", new Object[0]);
                    }
                } else if (this.dataType == DataType.LONG) {
                    if (isUnsigned()) {
                        f.format("UL", new Object[0]);
                    } else {
                        f.format("L", new Object[0]);
                    }
                } else if ((this.dataType == DataType.INT)
                        && (isUnsigned())) {
                    f.format("U", new Object[0]);
                }
            }
        }
    }

    @Override
    public String toString() {
        Formatter f = new Formatter();
        writeCDL(f);
        return f.toString();
    }
}
