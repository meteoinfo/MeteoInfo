package org.meteoinfo.ndarray.io.npy;

import java.nio.ByteBuffer;

public final class NpyFloatArray extends AbstractNpyArray<float[]> {

    public NpyFloatArray(int[] shape, float[] data, boolean fortranOrder) {
        super(shape, data, fortranOrder);
    }

    public static NpyFloatArray vectorOf(float[] data) {
        return new NpyFloatArray(new int[]{data.length}, data, false);
    }

    /**
     * Wraps the given data in a 2-dimensional array in row-major order (C order).
     *
     * @param data the data of the array
     * @param rows the number of rows of the array
     * @param cols the number of columns of the array
     * @return a 2d array of the given shape
     */
    public static NpyFloatArray rowOrderOf(float[] data, int rows, int cols) {
        return new NpyFloatArray(new int[]{rows, cols}, data, false);
    }

    /**
     * Wraps the given data in a 2-dimensional array in column-major order (
     * Fortran order).
     *
     * @param data the data of the array
     * @param rows the number of rows of the array
     * @param cols the number of columns of the array
     * @return a 2d array of the given shape
     */
    public static NpyFloatArray columnOrderOf(float[] data, int rows, int cols) {
        return new NpyFloatArray(new int[]{rows, cols}, data, true);
    }

    @Override
    public NpyDataType dataType() {
        return NpyDataType.f4;
    }

    @Override
    public int size() {
        return data.length;
    }

    @Override
    public Object getElement(int i) {
        return data[i];
    }

    @Override
    public void writeElementTo(int i, ByteBuffer buffer) {
        buffer.putFloat(data[i]);
    }

    @Override
    public boolean isFloatArray() {
        return true;
    }

    @Override
    public NpyBooleanArray asBooleanArray() {
        boolean[] booleans = new boolean[data.length];
        for (int i = 0; i < data.length; i++) {
            booleans[i] = i != 0;
        }
        return new NpyBooleanArray(copyShape(), booleans, fortranOrder);
    }

    @Override
    public NpyByteArray asByteArray() {
        byte[] bytes = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            bytes[i] = (byte) data[i];
        }
        return new NpyByteArray(copyShape(), bytes, fortranOrder);
    }

    @Override
    public NpyDoubleArray asDoubleArray() {
        double[] doubles = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            doubles[i] = data[i];
        }
        return new NpyDoubleArray(copyShape(), doubles, fortranOrder);
    }

    @Override
    public NpyFloatArray asFloatArray() {
        return this;
    }

    @Override
    public NpyIntArray asIntArray() {
        int[] ints = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            ints[i] = (int) data[i];
        }
        return new NpyIntArray(copyShape(), ints, fortranOrder);
    }

    @Override
    public NpyLongArray asLongArray() {
        long[] longs = new long[data.length];
        for (int i = 0; i < data.length; i++) {
            longs[i] = (long) data[i];
        }
        return new NpyLongArray(copyShape(), longs, fortranOrder);
    }

    @Override
    public NpyShortArray asShortArray() {
        short[] shorts = new short[data.length];
        for (int i = 0; i < data.length; i++) {
            shorts[i] = (short) data[i];
        }
        return new NpyShortArray(copyShape(), shorts, fortranOrder);
    }
}
