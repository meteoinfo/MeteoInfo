package org.meteoinfo.ndarray.io.npy.dict;

import org.meteoinfo.ndarray.io.npy.NpyByteOrder;
import org.meteoinfo.ndarray.io.npy.NpyDataType;
import org.meteoinfo.ndarray.io.npy.NpyFormatException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Contains the values of the dictionary that is stored in the header of an NPY
 * file.
 */
public class NpyHeaderDict {

    private final NpyDataType dataType;
    private final NpyByteOrder byteOrder;
    private final boolean fortranOrder;
    private final int[] shape;
    private final Map<String, String> properties;
    private final int typeSize;

    private NpyHeaderDict(Builder builder) {
        this.dataType = Objects.requireNonNull(builder.dataType);
        this.fortranOrder = builder.fortranOrder;

        // shape
        this.shape = builder.shape == null
                ? new int[0]
                : Arrays.copyOf(builder.shape, builder.shape.length);

        // byte order
        this.byteOrder = builder.byteOrder == null
                ? NpyByteOrder.NOT_APPLICABLE
                : builder.byteOrder;

        // type size
        this.typeSize = dataType.size() != 0
                ? dataType.size()
                : builder.typeSize;

        // additional properties
        this.properties = builder.properties != null
                ? builder.properties
                : Collections.emptyMap();
    }

    public static Builder of(NpyDataType dataType) {
        return new Builder(dataType);
    }

    public NpyDataType dataType() {
        return dataType;
    }

    public NpyByteOrder byteOrder() {
        return byteOrder;
    }

    /**
     * Describes the size of the stored type. The meaning of this field depends
     * on the respective storage type. For numeric types it is in general the
     * number of bytes which are required to store a single value. For strings,
     * it is the number of characters of the string (note that for unicode strings
     * 4 bytes are used to store a single character in NPY and that ASCII strings
     * are stored with an additional null-termination byte).
     *
     * @return the size of the stored type.
     */
    public int typeSize() {
        return typeSize;
    }

    /**
     * Returns {@code true} when the array is stored in Fortran order.
     */
    public boolean hasFortranOrder() {
        return fortranOrder;
    }

    /**
     * Returns the number of dimensions of the array..
     */
    public int dimensions() {
        return shape.length;
    }

    /**
     * Returns the size if the ith dimension of the array.
     *
     * @param i the 0-based dimension for which the size is requested.
     * @return the size of the requested dimension
     * @throws IndexOutOfBoundsException if {@code i < 0 || i >= dimensions()}
     */
    public int sizeOfDimension(int i) {
        if (i < 0 || i >= shape.length)
            throw new IndexOutOfBoundsException(String.valueOf(i));
        return shape[i];
    }

    /**
     * Returns the size of the stored array in number of bytes. That is the
     * number of elements of the stored array times the size of the data type in
     * bytes.
     *
     * @return the size of the stored array in bytes
     */
    public long dataSize() {
        long elemCount = numberOfElements();
        NpyDataType type = dataType();
        if (type.size() != 0)
            return elemCount * typeSize();
        if (type == NpyDataType.U)
            return typeSize() * 4L;
        return elemCount * typeSize();
    }

    /**
     * Returns the number of elements that are stored in the array.
     *
     * @return the number of elements which is the product of all dimension sizes.
     */
    public int numberOfElements() {
        int count = 1;
        int n = dimensions();
        for (int i = 0; i < n; i++) {
            count *= sizeOfDimension(i);
        }
        return count;
    }

    /**
     * Returns the shape of the stored array. Note that this returns a new
     * allocated array each time you call this method.
     *
     * @return the shape of the stored array
     */
    public int[] shape() {
        int n = dimensions();
        int[] shape = new int[n];
        for (int i = 0; i < shape.length; i++) {
            shape[i] = sizeOfDimension(i);
        }
        return shape;
    }

    public String property(String key) {
        return properties.get(key);
    }


    public Map<String, String> otherProperties() {
        return properties.isEmpty()
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(properties);
    }

    public static NpyHeaderDict parse(String s) throws NpyFormatException {
        PyValue value = Parser.parse(s);
        if (value.isError())
            throw new NpyFormatException(
                    "invalid header dictionary: " + value.asError().message());
        if (!value.isDict())
            throw new NpyFormatException(
                    "invalid header dictionary; type is " + value.getClass());

        PyDict dict = value.asDict();

        // read the data type
        PyValue typeEntry = dict.get("descr");
        if (typeEntry.isNone())
            throw new NpyFormatException(
                    "invalid header dictionary; data type field 'descr' is missing");
        if (!typeEntry.isString())
            throw new NpyFormatException(
                    "invalid header dictionary; data type field " +
                            "'descr' is not a string but: " + typeEntry);
        String dtype = typeEntry.asString().value();
        NpyDataType dataType = NpyDataType.of(dtype);
        if (dataType == null)
            throw new NpyFormatException(
                    "unsupported data type: " + dtype);

        Builder builder = of(dataType)
                .withShape(getShape(dict))
                .withFortranOrder(getFortranOrder(dict))
                .withByteOrder(NpyDataType.byteOrderOf(dtype));

        // try to set the type size for string types
        if (dataType.size() == 0) {
            for (int i = 0; i < dtype.length(); i++) {
                if (!Character.isDigit(dtype.charAt(i)))
                    continue;
                try {
                    String lenStr = dtype.substring(i);
                    int typeSize = Integer.parseInt(lenStr);
                    builder.withTypeSize(typeSize);
                } catch (Exception ignored) {
                }
                break;
            }
        }

        // collect other string properties
        dict.forEach((key, val) -> {
            if (!val.isString())
                return;
            if (key.equals("descr")
                    || key.equals("shape")
                    || key.equals("fortran_order"))
                return;
            builder.withOtherProperty(
                    key, val.asString().value());
        });

        return builder.create();
    }

    private static boolean getFortranOrder(PyDict dict)
            throws NpyFormatException {
        PyValue entry = dict.get("fortran_order");
        if (entry.isNone())
            return false;
        if (!entry.isIdentifier())
            throw new NpyFormatException(
                    "invalid header dictionary: fortran_order must be " +
                            "True or False but was '" + entry + "'");
        String value = entry.asIdentifier().value();
        switch (value) {
            case "True":
                return true;
            case "False":
                return false;
            default:
                throw new NpyFormatException(
                        "invalid header dictionary: fortran_order must be " +
                                "True or False but was '" + value + "'");
        }
    }

    private static int[] getShape(PyDict dict) throws NpyFormatException {
        PyValue entry = dict.get("shape");
        if (entry.isNone()) {
            throw new NpyFormatException(
                    "invalid header dictionary: property 'shape' is missing");
        }
        if (!entry.isTuple()) {
            throw new NpyFormatException(
                    "invalid header dictionary: property 'shape' is not a tuple");
        }

        PyTuple tuple = entry.asTuple();
        int[] shape = new int[tuple.size()];
        for (int i = 0; i < tuple.size(); i++) {
            PyValue value = tuple.at(i);
            if (!value.isInt()) {
                throw new NpyFormatException(
                        "invalid header dictionary: argument "
                                + i + " of tuple 'shape' is not an integer");
            }
            shape[i] = (int) value.asInt().value();
        }
        return shape;
    }

    @Override
    public String toString() {

        // data type
        StringBuilder buffer = new StringBuilder("{'descr': '");
        if (dataType != null) {
            if (dataType.size() != 1) {
                buffer.append(byteOrder.symbol());
            }
            buffer.append(dataType.symbol());
            if (dataType.size() == 0) {
                buffer.append(typeSize);
            }
        }

        // fortran order
        buffer.append("', 'fortran_order': ");
        if (fortranOrder) {
            buffer.append("True");
        } else {
            buffer.append("False");
        }

        // shape
        buffer.append(", 'shape': (");
        if (shape != null) {
            for (int i = 0; i < shape.length; i++) {
                if (i > 0) {
                    buffer.append(' ');
                }
                buffer.append(shape[i]).append(',');
            }
        }
        buffer.append(")");

        // other properties
        for (Map.Entry<String, String> prop : properties.entrySet()) {
            String key = prop.getKey();
            String val = prop.getValue();
            if (key == null || val == null
                    || "descr".equals(key)
                    || "shape".equals(key)
                    || "fortran_order".equals(key))
                continue;
            buffer.append(", '")
                    .append(key.replace('\'', '"'))
                    .append("': '")
                    .append(val.replace('\'', '"'))
                    .append('\'');
        }

        buffer.append('}');
        return buffer.toString();

    }

    public byte[] toNpyHeader() {

        int version = 1;

        // dictionary bytes
        String s = toString();
        boolean allAscii = StandardCharsets.US_ASCII.newEncoder().canEncode(s);
        if (!allAscii) {
            version = 3;
        }
        byte[] dictBytes = allAscii
                ? s.getBytes(StandardCharsets.US_ASCII)
                : s.getBytes(StandardCharsets.UTF_8);


        // calculate the length and padding
        int filled = version == 1
                ? 11 + dictBytes.length
                : 13 + dictBytes.length;
        int padding = 64 - (filled % 64);
        int totalLen = filled + padding;

        if (version == 1 && totalLen > 65535) {
            version = 2;
            filled = 13 + dictBytes.length;
            padding = 64 - (filled % 64);
            totalLen = filled + padding;
        }

        ByteBuffer buf = ByteBuffer.allocate(totalLen);
        buf.order(ByteOrder.LITTLE_ENDIAN);

        // magic
        buf.put((byte) 0x93);
        buf.put("NUMPY".getBytes());

        // version
        buf.put((byte) version);
        buf.put((byte) 0);

        // header length
        if (version == 1) {
            buf.putShort((short) (totalLen - 10));
        } else {
            buf.putInt(totalLen - 12);
        }

        // write the padding
        buf.put(dictBytes);
        for (int i = 0; i < padding; i++) {
            buf.put((byte) ' ');
        }
        buf.put((byte) '\n');
        return buf.array();
    }

    public static class Builder {

        private final NpyDataType dataType;
        private int[] shape;
        private NpyByteOrder byteOrder;
        private boolean fortranOrder;
        private Map<String, String> properties;
        private int typeSize;

        private Builder(NpyDataType dataType) {
            this.dataType = Objects.requireNonNull(dataType);
        }

        public Builder withShape(int[] shape) {
            this.shape = shape;
            return this;
        }

        public Builder withByteOrder(NpyByteOrder byteOrder) {
            this.byteOrder = byteOrder;
            return this;
        }

        public Builder withFortranOrder(boolean b) {
            this.fortranOrder = b;
            return this;
        }

        /**
         * Set the size of the stored type. This field must be set when the stored
         * data type is a string. In this case the size of the type is the number
         * of characters of the string.
         *
         * @param typeSize the size of the stored type
         * @return this builder
         */
        public Builder withTypeSize(int typeSize) {
            this.typeSize = typeSize;
            return this;
        }

        public Builder withOtherProperty(String key, String value) {
            if (key == null || value == null)
                return this;
            if (properties == null) {
                properties = new HashMap<>();
            }
            properties.put(key, value);
            return this;
        }

        public NpyHeaderDict create() {
            return new NpyHeaderDict(this);
        }
    }
}
