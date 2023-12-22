package org.meteoinfo.ndarray.io.npy;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;

public class NpyCharArray extends AbstractNpyArray<char[]> {

    private NpyDataType type;

    public NpyCharArray(int[] shape, char[] data) {
        super(shape, data, false);
    }

    public NpyCharArray(int[] shape, char[] data, boolean fortranOrder) {
        super(shape, data, fortranOrder);
    }

    public static NpyCharArray of(String s) {
        char[] chars = s.toCharArray();
        return new NpyCharArray(new int[0], chars);
    }

    @Override
    public NpyDataType dataType() {
        if (type != null)
            return type;
        boolean isAscii = StandardCharsets.US_ASCII
                .newEncoder()
                .canEncode(CharBuffer.wrap(data));
        type = isAscii
                ? NpyDataType.S
                : NpyDataType.U;
        return type;
    }

    @Override
    public Object getElement(int i) {
        return data[i];
    }

    @Override
    public void writeElementTo(int i, ByteBuffer buffer) {
        if (dataType() == NpyDataType.S) {
            buffer.put((byte) data[i]);
        } else {
            buffer.putInt(data[i]);
        }
    }

    @Override
    public int size() {
        return data.length;
    }

    @Override
    public boolean isCharArray() {
        return true;
    }

    @Override
    public NpyBooleanArray asBooleanArray() {
        boolean[] booleans = new boolean[data.length];
        for (int i = 0; i < data.length; i++) {
            booleans[i] = data[i] != 0;
        }
        return new NpyBooleanArray(copyShape(), booleans, fortranOrder);
    }

    /**
     * Converts this character array into a byte array. If the characters in this
     * array can be encoded in ASCII, a NULL-terminated byte array will be
     * returned. Otherwise, an array with the 4-byte unicode code-points encoded
     * in little-endian order will be returned.
     *
     * @return the NPY byte-array representation of this character array
     */
    @Override
    public NpyByteArray asByteArray() {
        NpyDataType type = dataType();
        if (type == NpyDataType.S) {
            // write as NULL terminated string
            byte[] bytes = new byte[data.length + 1];
            for (int i = 0; i < data.length; i++) {
                bytes[i] = (byte) data[i];
            }
            return NpyByteArray.vectorOf(bytes);
        }

        // write unicode code points
        byte[] bytes = new byte[data.length * 4];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (char datum : data) {
            buffer.putInt(datum);
        }
        return NpyByteArray.vectorOf(bytes);
    }

    @Override
    public NpyCharArray asCharArray() {
        return this;
    }

    @Override
    public NpyDoubleArray asDoubleArray() {
        return asIntArray().asDoubleArray();
    }

    @Override
    public NpyFloatArray asFloatArray() {
        return asIntArray().asFloatArray();
    }

    @Override
    public NpyIntArray asIntArray() {
        IntBuffer buffer = IntBuffer.allocate(data.length);
        int pos = 0;
        while (pos < data.length) {
            int codePoint = Character.codePointAt(data, pos);
            buffer.put(codePoint);
            pos += Character.charCount(codePoint);
        }

        int[] ints;
        if (buffer.remaining() == 0) {
            ints = buffer.array();
        } else {
            buffer.flip();
            ints = new int[buffer.limit()];
            buffer.get(ints, 0, buffer.limit());
        }
        return new NpyIntArray(copyShape(), ints, fortranOrder);
    }

    @Override
    public NpyLongArray asLongArray() {
        return asIntArray().asLongArray();
    }

    @Override
    public NpyShortArray asShortArray() {
        return asIntArray().asShortArray();
    }

    @Override
    public String toString() {
        return String.valueOf(data);
    }
}
