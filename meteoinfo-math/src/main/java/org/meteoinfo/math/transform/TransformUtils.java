package org.meteoinfo.math.transform;

import java.util.function.DoubleUnaryOperator;

import org.apache.commons.numbers.core.ArithmeticUtils;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.Complex;
import org.meteoinfo.ndarray.DataType;

/**
 * Useful functions for the implementation of various transforms.
 * Class is package-private (for internal use only).
 */
final class TransformUtils {
    /** Number of array slots: 1 for "real" parts 1 for "imaginary" parts. */
    private static final int NUM_PARTS = 2;

    /** Utility class. */
    private TransformUtils() {}

    /**
     * Multiply every component in the given real array by the
     * given real number. The change is made in place.
     *
     * @param f Array to be scaled.
     * @param d Scaling coefficient.
     * @return a reference to the scaled array.
     */
    static double[] scaleInPlace(double[] f, double d) {
        for (int i = 0; i < f.length; i++) {
            f[i] *= d;
        }
        return f;
    }

    /**
     * Multiply every component in the given complex array by the
     * given real number. The change is made in place.
     *
     * @param f Array to be scaled.
     * @param d Scaling coefficient.
     * @return the scaled array.
     */
    static Complex[] scaleInPlace(Complex[] f, double d) {
        for (int i = 0; i < f.length; i++) {
            f[i] = Complex.ofCartesian(d * f[i].getReal(), d * f[i].getImaginary());
        }
        return f;
    }


    /**
     * Builds a new two dimensional array of {@code double} filled with the real
     * and imaginary parts of the specified {@link Complex} numbers. In the
     * returned array {@code dataRI}, the data is laid out as follows
     * <ul>
     * <li>{@code dataRI[0][i] = dataC[i].getReal()},</li>
     * <li>{@code dataRI[1][i] = dataC[i].getImaginary()}.</li>
     * </ul>
     *
     * @param dataC Array of {@link Complex} data to be transformed.
     * @return a two dimensional array filled with the real and imaginary parts
     * of the specified complex input.
     */
    static double[][] createRealImaginary(final Complex[] dataC) {
        final double[][] dataRI = new double[2][dataC.length];
        final double[] dataR = dataRI[0];
        final double[] dataI = dataRI[1];
        for (int i = 0; i < dataC.length; i++) {
            final Complex c = dataC[i];
            dataR[i] = c.getReal();
            dataI[i] = c.getImaginary();
        }
        return dataRI;
    }

    /**
     * Builds a new two dimensional array of {@code double} filled with the real
     * and imaginary parts of the specified {@link Complex} numbers. In the
     * returned array {@code dataRI}, the data is laid out as follows
     * <ul>
     * <li>{@code dataRI[0][i] = dataC[i].getReal()},</li>
     * <li>{@code dataRI[1][i] = dataC[i].getImaginary()}.</li>
     * </ul>
     *
     * @param dataC Array of {@link Complex} data to be transformed.
     * @return a two dimensional array filled with the real and imaginary parts
     * of the specified complex input.
     */
    static double[][] createRealImaginary(final Array dataC) {
        dataC.copyIfView();

        final double[][] dataRI = new double[2][(int) dataC.getSize()];
        final double[] dataR = dataRI[0];
        final double[] dataI = dataRI[1];
        for (int i = 0; i < dataC.getSize(); i++) {
            final Complex c = dataC.getComplex(i);
            dataR[i] = c.getReal();
            dataI[i] = c.getImaginary();
        }
        return dataRI;
    }

    /**
     * Builds a new array of {@link Complex} from the specified two dimensional
     * array of real and imaginary parts. In the returned array {@code dataC},
     * the data is laid out as follows
     * <ul>
     * <li>{@code dataC[i].getReal() = dataRI[0][i]},</li>
     * <li>{@code dataC[i].getImaginary() = dataRI[1][i]}.</li>
     * </ul>
     *
     * @param dataRI Array of real and imaginary parts to be transformed.
     * @return a {@link Complex} array.
     * @throws IllegalArgumentException if the number of rows of the specified
     * array is not two, or the array is not rectangular.
     */
    static Complex[] createComplex(final double[][] dataRI) {
        if (dataRI.length != NUM_PARTS) {
            throw new TransformException(TransformException.SIZE_MISMATCH,
                    dataRI.length, NUM_PARTS);
        }
        final double[] dataR = dataRI[0];
        final double[] dataI = dataRI[1];
        if (dataR.length != dataI.length) {
            throw new TransformException(TransformException.SIZE_MISMATCH,
                    dataI.length, dataR.length);
        }

        final int n = dataR.length;
        final Complex[] c = new Complex[n];
        for (int i = 0; i < n; i++) {
            c[i] = Complex.ofCartesian(dataR[i], dataI[i]);
        }
        return c;
    }

    /**
     * Builds a new array of {@link Complex} from the specified two dimensional
     * array of real and imaginary parts. In the returned array {@code dataC},
     * the data is laid out as follows
     * <ul>
     * <li>{@code dataC[i].getReal() = dataRI[0][i]},</li>
     * <li>{@code dataC[i].getImaginary() = dataRI[1][i]}.</li>
     * </ul>
     *
     * @param dataRI Array of real and imaginary parts to be transformed.
     * @return a {@link Complex} array.
     * @throws IllegalArgumentException if the number of rows of the specified
     * array is not two, or the array is not rectangular.
     */
    static Array createComplexArray(final double[][] dataRI) {
        if (dataRI.length != NUM_PARTS) {
            throw new TransformException(TransformException.SIZE_MISMATCH,
                    dataRI.length, NUM_PARTS);
        }
        final double[] dataR = dataRI[0];
        final double[] dataI = dataRI[1];
        if (dataR.length != dataI.length) {
            throw new TransformException(TransformException.SIZE_MISMATCH,
                    dataI.length, dataR.length);
        }

        final int n = dataR.length;
        final Array c = Array.factory(DataType.COMPLEX, new int[]{n});
        for (int i = 0; i < n; i++) {
            c.setComplex(i, Complex.ofCartesian(dataR[i], dataI[i]));
        }
        return c;
    }

    /**
     * Samples the specified univariate real function on the specified interval.
     * <p>
     * The interval is divided equally into {@code n} sections and sample points
     * are taken from {@code min} to {@code max - (max - min) / n}; therefore
     * {@code f} is not sampled at the upper bound {@code max}.</p>
     *
     * @param f Function to be sampled
     * @param min Lower bound of the interval (included).
     * @param max Upper bound of the interval (excluded).
     * @param n Number of sample points.
     * @return the array of samples.
     * @throws IllegalArgumentException if the lower bound {@code min} is
     * greater than, or equal to the upper bound {@code max}, if the number
     * of sample points {@code n} is negative.
     */
    static double[] sample(DoubleUnaryOperator f,
                           double min,
                           double max,
                           int n) {
        if (n <= 0) {
            throw new TransformException(TransformException.NOT_STRICTLY_POSITIVE,
                    Integer.valueOf(n));
        }
        if (min >= max) {
            throw new TransformException(TransformException.TOO_LARGE, min, max);
        }

        final double[] s = new double[n];
        final double h = (max - min) / n;
        for (int i = 0; i < n; i++) {
            s[i] = f.applyAsDouble(min + i * h);
        }
        return s;
    }

    /**
     * Check whether the number is a power of 2
     * @param number
     * @return Whether is a power of 2
     */
    public static boolean isPowerOfTwo(int n) {
        return n > 0L && (n & n - 1L) == 0L;
    }

    /**
     * Find next number which is a power of 2
     * @param number
     * @return The next number which is a power of 2
     */
    public static int nextPowerOfTwo(int number) {
        if (isPowerOfTwo(number)) {
            return number;
        }

        int x = number;
        x = x - 1;
        x |= x >> 1;
        x |= x >> 2;
        x |= x >> 4;
        x |= x >> 8;
        x |= x >> 16;

        return x + 1;
    }
}
