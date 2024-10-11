package org.meteoinfo.math.transform;

import org.apache.commons.numbers.core.ArithmeticUtils;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.ArrayComplex;
import org.meteoinfo.ndarray.Complex;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.math.ArrayMath;
import org.meteoinfo.ndarray.math.ArrayUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

/**
 * Implements the Fast Fourier Transform for transformation of one-dimensional
 * real or complex data sets. For reference, see <em>Applied Numerical Linear
 * Algebra</em>, ISBN 0898713897, chapter 6.
 * <p>
 * There are several variants of the discrete Fourier transform, with various
 * normalization conventions, which are specified by the parameter
 * {@link Norm}.
 * <p>
 * The current implementation of the discrete Fourier transform as a fast
 * Fourier transform requires the length of the data set to be a power of 2.
 * This greatly simplifies and speeds up the code. Users can pad the data with
 * zeros to meet this requirement. There are other flavors of FFT, for
 * reference, see S. Winograd,
 * <i>On computing the discrete Fourier transform</i>, Mathematics of
 * Computation, 32 (1978), 175 - 199.
 */
public class FastFourierTransform implements ComplexTransform {

    /** Number of array slots: 1 for "real" parts 1 for "imaginary" parts. */
    private static final int NUM_PARTS = 2;
    /** The IEEE 754 machine epsilon from Cephes: {@code (2^-53)} */
    private static final double MACH_EPS = 1.11022302462515654042e-16;
    static final double TOL = 5.0 * MACH_EPS;
    /**
     * {@code W_SUB_N_R[i]} is the real part of
     * {@code exp(- 2 * i * pi / n)}:
     * {@code W_SUB_N_R[i] = cos(2 * pi/ n)}, where {@code n = 2^i}.
     */
    private static final double[] W_SUB_N_R = {
            0x1.0p0, -0x1.0p0, 0x1.1a62633145c07p-54, 0x1.6a09e667f3bcdp-1,
            0x1.d906bcf328d46p-1, 0x1.f6297cff75cbp-1, 0x1.fd88da3d12526p-1, 0x1.ff621e3796d7ep-1,
            0x1.ffd886084cd0dp-1, 0x1.fff62169b92dbp-1, 0x1.fffd8858e8a92p-1, 0x1.ffff621621d02p-1,
            0x1.ffffd88586ee6p-1, 0x1.fffff62161a34p-1, 0x1.fffffd8858675p-1, 0x1.ffffff621619cp-1,
            0x1.ffffffd885867p-1, 0x1.fffffff62161ap-1, 0x1.fffffffd88586p-1, 0x1.ffffffff62162p-1,
            0x1.ffffffffd8858p-1, 0x1.fffffffff6216p-1, 0x1.fffffffffd886p-1, 0x1.ffffffffff621p-1,
            0x1.ffffffffffd88p-1, 0x1.fffffffffff62p-1, 0x1.fffffffffffd9p-1, 0x1.ffffffffffff6p-1,
            0x1.ffffffffffffep-1, 0x1.fffffffffffffp-1, 0x1.0p0, 0x1.0p0,
            0x1.0p0, 0x1.0p0, 0x1.0p0, 0x1.0p0,
            0x1.0p0, 0x1.0p0, 0x1.0p0, 0x1.0p0,
            0x1.0p0, 0x1.0p0, 0x1.0p0, 0x1.0p0,
            0x1.0p0, 0x1.0p0, 0x1.0p0, 0x1.0p0,
            0x1.0p0, 0x1.0p0, 0x1.0p0, 0x1.0p0,
            0x1.0p0, 0x1.0p0, 0x1.0p0, 0x1.0p0,
            0x1.0p0, 0x1.0p0, 0x1.0p0, 0x1.0p0,
            0x1.0p0, 0x1.0p0, 0x1.0p0 };

    /**
     * {@code W_SUB_N_I[i]} is the imaginary part of
     * {@code exp(- 2 * i * pi / n)}:
     * {@code W_SUB_N_I[i] = -sin(2 * pi/ n)}, where {@code n = 2^i}.
     */
    private static final double[] W_SUB_N_I = {
            0x1.1a62633145c07p-52, -0x1.1a62633145c07p-53, -0x1.0p0, -0x1.6a09e667f3bccp-1,
            -0x1.87de2a6aea963p-2, -0x1.8f8b83c69a60ap-3, -0x1.917a6bc29b42cp-4, -0x1.91f65f10dd814p-5,
            -0x1.92155f7a3667ep-6, -0x1.921d1fcdec784p-7, -0x1.921f0fe670071p-8, -0x1.921f8becca4bap-9,
            -0x1.921faaee6472dp-10, -0x1.921fb2aecb36p-11, -0x1.921fb49ee4ea6p-12, -0x1.921fb51aeb57bp-13,
            -0x1.921fb539ecf31p-14, -0x1.921fb541ad59ep-15, -0x1.921fb5439d73ap-16, -0x1.921fb544197ap-17,
            -0x1.921fb544387bap-18, -0x1.921fb544403c1p-19, -0x1.921fb544422c2p-20, -0x1.921fb54442a83p-21,
            -0x1.921fb54442c73p-22, -0x1.921fb54442cefp-23, -0x1.921fb54442d0ep-24, -0x1.921fb54442d15p-25,
            -0x1.921fb54442d17p-26, -0x1.921fb54442d18p-27, -0x1.921fb54442d18p-28, -0x1.921fb54442d18p-29,
            -0x1.921fb54442d18p-30, -0x1.921fb54442d18p-31, -0x1.921fb54442d18p-32, -0x1.921fb54442d18p-33,
            -0x1.921fb54442d18p-34, -0x1.921fb54442d18p-35, -0x1.921fb54442d18p-36, -0x1.921fb54442d18p-37,
            -0x1.921fb54442d18p-38, -0x1.921fb54442d18p-39, -0x1.921fb54442d18p-40, -0x1.921fb54442d18p-41,
            -0x1.921fb54442d18p-42, -0x1.921fb54442d18p-43, -0x1.921fb54442d18p-44, -0x1.921fb54442d18p-45,
            -0x1.921fb54442d18p-46, -0x1.921fb54442d18p-47, -0x1.921fb54442d18p-48, -0x1.921fb54442d18p-49,
            -0x1.921fb54442d18p-50, -0x1.921fb54442d18p-51, -0x1.921fb54442d18p-52, -0x1.921fb54442d18p-53,
            -0x1.921fb54442d18p-54, -0x1.921fb54442d18p-55, -0x1.921fb54442d18p-56, -0x1.921fb54442d18p-57,
            -0x1.921fb54442d18p-58, -0x1.921fb54442d18p-59, -0x1.921fb54442d18p-60 };

    /** Type of DFT. */
    protected final Norm normalization;
    /** Inverse or forward. */
    protected final boolean inverse;

    /**
     * @param normalization Normalization to be applied to the
     * transformed data.
     * @param inverse Whether to perform the inverse transform.
     */
    public FastFourierTransform(final Norm normalization,
                                final boolean inverse) {
        this.normalization = normalization;
        this.inverse = inverse;
    }

    /**
     * @param normalization Normalization to be applied to the
     * transformed data.
     */
    public FastFourierTransform(final Norm normalization) {
        this(normalization, false);
    }

    /**
     * Constructor
     *
     * @param inverse Whether to perform the inverse transform.
     */
    public FastFourierTransform(final boolean inverse) {
        this(Norm.STD, inverse);
    }

    /**
     * Constructor
     */
    public FastFourierTransform() {
        this(Norm.STD, false);
    }

    /**
     * Computes the standard transform of the data.
     * Computation is done in place.
     * Assumed layout of the input data:
     * <ul>
     *   <li>{@code dataRI[0][i]}: Real part of the {@code i}-th data point,</li>
     *   <li>{@code dataRI[1][i]}: Imaginary part of the {@code i}-th data point.</li>
     * </ul>
     *
     * @param dataRI Two-dimensional array of real and imaginary parts of the data.
     * @throws IllegalArgumentException if the number of data points is not
     * a power of two, if the number of rows of the specified array is not two,
     * or the array is not rectangular.
     */
    public void transformInPlace(final double[][] dataRI) {
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
        if (!ArithmeticUtils.isPowerOfTwo(n)) {
            throw new TransformException(TransformException.NOT_POWER_OF_TWO,
                    Integer.valueOf(n));
        }

        if (n == 1) {
            return;
        } else if (n == 2) {
            final double srcR0 = dataR[0];
            final double srcI0 = dataI[0];
            final double srcR1 = dataR[1];
            final double srcI1 = dataI[1];

            // X_0 = x_0 + x_1
            dataR[0] = srcR0 + srcR1;
            dataI[0] = srcI0 + srcI1;
            // X_1 = x_0 - x_1
            dataR[1] = srcR0 - srcR1;
            dataI[1] = srcI0 - srcI1;

            normalizeTransformedData(dataRI);
            return;
        }

        bitReversalShuffle2(dataR, dataI);

        // Do 4-term DFT.
        if (inverse) {
            for (int i0 = 0; i0 < n; i0 += 4) {
                final int i1 = i0 + 1;
                final int i2 = i0 + 2;
                final int i3 = i0 + 3;

                final double srcR0 = dataR[i0];
                final double srcI0 = dataI[i0];
                final double srcR1 = dataR[i2];
                final double srcI1 = dataI[i2];
                final double srcR2 = dataR[i1];
                final double srcI2 = dataI[i1];
                final double srcR3 = dataR[i3];
                final double srcI3 = dataI[i3];

                // 4-term DFT
                // X_0 = x_0 + x_1 + x_2 + x_3
                dataR[i0] = srcR0 + srcR1 + srcR2 + srcR3;
                dataI[i0] = srcI0 + srcI1 + srcI2 + srcI3;
                // X_1 = x_0 - x_2 + j * (x_3 - x_1)
                dataR[i1] = srcR0 - srcR2 + (srcI3 - srcI1);
                dataI[i1] = srcI0 - srcI2 + (srcR1 - srcR3);
                // X_2 = x_0 - x_1 + x_2 - x_3
                dataR[i2] = srcR0 - srcR1 + srcR2 - srcR3;
                dataI[i2] = srcI0 - srcI1 + srcI2 - srcI3;
                // X_3 = x_0 - x_2 + j * (x_1 - x_3)
                dataR[i3] = srcR0 - srcR2 + (srcI1 - srcI3);
                dataI[i3] = srcI0 - srcI2 + (srcR3 - srcR1);
            }
        } else {
            for (int i0 = 0; i0 < n; i0 += 4) {
                final int i1 = i0 + 1;
                final int i2 = i0 + 2;
                final int i3 = i0 + 3;

                final double srcR0 = dataR[i0];
                final double srcI0 = dataI[i0];
                final double srcR1 = dataR[i2];
                final double srcI1 = dataI[i2];
                final double srcR2 = dataR[i1];
                final double srcI2 = dataI[i1];
                final double srcR3 = dataR[i3];
                final double srcI3 = dataI[i3];

                // 4-term DFT
                // X_0 = x_0 + x_1 + x_2 + x_3
                dataR[i0] = srcR0 + srcR1 + srcR2 + srcR3;
                dataI[i0] = srcI0 + srcI1 + srcI2 + srcI3;
                // X_1 = x_0 - x_2 + j * (x_3 - x_1)
                dataR[i1] = srcR0 - srcR2 + (srcI1 - srcI3);
                dataI[i1] = srcI0 - srcI2 + (srcR3 - srcR1);
                // X_2 = x_0 - x_1 + x_2 - x_3
                dataR[i2] = srcR0 - srcR1 + srcR2 - srcR3;
                dataI[i2] = srcI0 - srcI1 + srcI2 - srcI3;
                // X_3 = x_0 - x_2 + j * (x_1 - x_3)
                dataR[i3] = srcR0 - srcR2 + (srcI3 - srcI1);
                dataI[i3] = srcI0 - srcI2 + (srcR1 - srcR3);
            }
        }

        int lastN0 = 4;
        int lastLogN0 = 2;
        while (lastN0 < n) {
            final int n0 = lastN0 << 1;
            final int logN0 = lastLogN0 + 1;
            final double wSubN0R = W_SUB_N_R[logN0];
            double wSubN0I = W_SUB_N_I[logN0];
            if (inverse) {
                wSubN0I = -wSubN0I;
            }

            // Combine even/odd transforms of size lastN0 into a transform of size N0 (lastN0 * 2).
            for (int destEvenStartIndex = 0; destEvenStartIndex < n; destEvenStartIndex += n0) {
                final int destOddStartIndex = destEvenStartIndex + lastN0;

                double wSubN0ToRR = 1;
                double wSubN0ToRI = 0;

                for (int r = 0; r < lastN0; r++) {
                    final int destEvenStartIndexPlusR = destEvenStartIndex + r;
                    final int destOddStartIndexPlusR = destOddStartIndex + r;

                    final double grR = dataR[destEvenStartIndexPlusR];
                    final double grI = dataI[destEvenStartIndexPlusR];
                    final double hrR = dataR[destOddStartIndexPlusR];
                    final double hrI = dataI[destOddStartIndexPlusR];

                    final double a = wSubN0ToRR * hrR - wSubN0ToRI * hrI;
                    final double b = wSubN0ToRR * hrI + wSubN0ToRI * hrR;
                    // dest[destEvenStartIndex + r] = Gr + WsubN0ToR * Hr
                    dataR[destEvenStartIndexPlusR] = grR + a;
                    dataI[destEvenStartIndexPlusR] = grI + b;
                    // dest[destOddStartIndex + r] = Gr - WsubN0ToR * Hr
                    dataR[destOddStartIndexPlusR] = grR - a;
                    dataI[destOddStartIndexPlusR] = grI - b;

                    // WsubN0ToR *= WsubN0R
                    final double nextWsubN0ToRR = wSubN0ToRR * wSubN0R - wSubN0ToRI * wSubN0I;
                    final double nextWsubN0ToRI = wSubN0ToRR * wSubN0I + wSubN0ToRI * wSubN0R;
                    wSubN0ToRR = nextWsubN0ToRR;
                    wSubN0ToRI = nextWsubN0ToRI;
                }
            }

            lastN0 = n0;
            lastLogN0 = logN0;
        }

        normalizeTransformedData(dataRI);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if the length of the data array is not a power of two.
     */
    @Override
    public Array apply(final double[] f) {
        final double[][] dataRI = {
                Arrays.copyOf(f, f.length),
                new double[f.length]
        };
        transformInPlace(dataRI);
        return TransformUtils.createComplexArray(dataRI);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if the number of sample points
     * {@code n} is not a power of two, if the lower bound is greater than,
     * or equal to the upper bound, if the number of sample points {@code n}
     * is negative
     */
    @Override
    public Array apply(final DoubleUnaryOperator f,
                           final double min,
                           final double max,
                           final int n) {
        return apply(TransformUtils.sample(f, min, max, n));
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if the length of the data array is
     * not a power of two.
     */
    @Override
    public Array apply(Array f) {
        if (!ArithmeticUtils.isPowerOfTwo(f.getSize())) {
            //f = pad(f);
            if (inverse) {
                return ifftBluestein(f);
            } else {
                return fftBluestein(f);
            }
        }

        final double[][] dataRI = TransformUtils.createRealImaginary(f);
        transformInPlace(dataRI);
        tolTransformedData(dataRI);
        return TransformUtils.createComplexArray(dataRI);
    }

    /**
     * Fast Fourier Transform using Bluestein method
     * @param a Input data array
     * @return FFT data array
     */
    public Array fftBluestein(Array a) {
        // find a power of 2 convolution length m such that m >= n * 2 + 1
        int n = (int) a.getSize();
        if (n >= 0x20000000) {
            throw new IllegalArgumentException("array too large: " + n);
        }

        double[] cos = new double[n];
        double[] sin = new double[n];
        for (int i = 0; i < n; ++i) {
            int j = (int) ((long) i * i % (n * 2));
            double angle = Math.PI * j / n;
            cos[i] = Math.cos(angle);
            sin[i] = Math.sin(angle);
        }

        int m = Integer.highestOneBit(n) * 4;

        // temporary arrays
        double[] a_re = new double[m];
        double[] a_im = new double[m];
        double[] b_re = new double[m];
        double[] b_im = new double[m];

        b_re[0] = cos[0];
        b_im[0] = sin[0];

        Complex c;
        for (int i = 0; i < n; ++i) {
            double sin_i = sin[i];
            double cos_i = cos[i];
            c = a.getComplex(i);
            double re_i = c.real();
            double im_i = c.imag();
            a_re[i] = re_i * cos_i + im_i * sin_i;
            a_im[i] = -re_i * sin_i + im_i * cos_i;
            if (i != 0) {
                b_re[i] = b_re[m - i] = cos_i;
                b_im[i] = b_im[m - i] = sin_i;
            }
        }

        // convolution
        Array conv = convolve(ArrayUtil.arrayComplex(a_re, a_im), ArrayUtil.arrayComplex(b_re, b_im));
        List<double[]> re_im = ((ArrayComplex) conv).getRealImage();
        double[] c_re = re_im.get(0);
        double[] c_im = re_im.get(1);

        // result
        double[] re = new double[n];
        double[] im = new double[n];

        // postprocessing
        Array r = Array.factory(DataType.COMPLEX, new int[]{n});
        for (int i = 0; i < n; ++i) {
            double sin_i = sin[i];
            double cos_i = cos[i];
            double c_re_i = c_re[i];
            double c_im_i = c_im[i];
            double re_i = c_re_i * cos_i + c_im_i * sin_i;
            double im_i = -c_re_i * sin_i + c_im_i * cos_i;
            c = new Complex((Math.abs(re_i) <= TOL) ? 0.0 : re_i,
                (Math.abs(im_i) <= TOL) ? 0.0 : im_i);
            r.setComplex(i, c);
        }

        return r;
    }

    /**
     * Fast Fourier Inverse Transform using Bluestein method
     * @param a Input data array
     * @return Inverse FFT data array
     */
    public Array ifftBluestein(Array freqs) {
        Array inv = fftBluestein(freqs);

        List<double[]> re_im = ((ArrayComplex) inv).getRealImage();
        double[] re = re_im.get(0);
        double[] im = re_im.get(1);
        final int n = re.length;
        for (int i = 0; i < n; ++i) {
            double re_i = re[i] / n;
            double im_i = im[i] / n;
            re[i] = (Math.abs(re_i) <= TOL) ? 0.0 : re_i;
            im[i] = (Math.abs(im_i) <= TOL) ? 0.0 : im_i;
        }
        for (int i = 1; i <= n / 2; ++i) {
            double re_tmp = re[n - i];
            double im_tmp = im[n - i];
            re[n - i] = re[i];
            re[i] = re_tmp;
            im[n - i] = im[i];
            im[i] = im_tmp;
        }

        //return inv;
        return new ArrayComplex(re, im);
    }

    private Array convolve(Array x, Array y) {
        FastFourierTransform fft = new FastFourierTransform(this.normalization, false);
        x = fft.apply(x);
        y = fft.apply(y);

        Array x_re = ArrayMath.getReal(x);
        Array x_im = ArrayMath.getImage(x);
        Array y_re = ArrayMath.getReal(y);
        Array y_im = ArrayMath.getImage(y);
        Array temp = Array.factory(DataType.COMPLEX, x.getShape());

        Complex c;
        for (int i = 0; i < x_re.getSize(); ++i) {
            double x_re_i = x_re.getDouble(i);
            double y_re_i = y_re.getDouble(i);
            double x_im_i = x_im.getDouble(i);
            double y_im_i = y_im.getDouble(i);
            c = new Complex(x_re_i * y_re_i - x_im_i * y_im_i,
                    x_im_i * y_re_i + x_re_i * y_im_i);
            temp.setComplex(i, c);
        }

        fft = new FastFourierTransform(this.normalization, true);
        return fft.apply(temp);
    }

    /**
     * Applies normalization to the transformed data.
     *
     * @param dataRI Unscaled transformed data.
     */
    private void normalizeTransformedData(final double[][] dataRI) {
        if (normalization == Norm.NONE) {
            return;
        }

        final double[] dataR = dataRI[0];
        final double[] dataI = dataRI[1];
        final int n = dataR.length;

        switch (normalization) {
            case STD:
                if (inverse) {
                    final double scaleFactor = 1d / n;
                    for (int i = 0; i < n; i++) {
                        dataR[i] *= scaleFactor;
                        dataI[i] *= scaleFactor;
                    }
                }

                break;

            case UNIT:
                final double scaleFactor = 1d / Math.sqrt(n);
                for (int i = 0; i < n; i++) {
                    dataR[i] *= scaleFactor;
                    dataI[i] *= scaleFactor;
                }

                break;

            default:
                throw new IllegalStateException(); // Should never happen.
        }
    }

    private void tolTransformedData(final double[][] dataRI) {
        final double[] dataR = dataRI[0];
        final double[] dataI = dataRI[1];
        final int n = dataR.length;

        for (int i = 0; i < n; i++) {
            if (Math.abs(dataR[i]) <= TOL) {
                dataR[i] = 0;
            }
            if (Math.abs(dataI[i]) <= TOL) {
                dataI[i] = 0;
            }
        }
    }

    /**
     * Performs identical index bit reversal shuffles on two arrays of
     * identical size.
     * Each element in the array is swapped with another element based
     * on the bit-reversal of the index.
     * For example, in an array with length 16, item at binary index 0011
     * (decimal 3) would be swapped with the item at binary index 1100
     * (decimal 12).
     *
     * @param a Array to be shuffled.
     * @param b Array to be shuffled.
     */
    private static void bitReversalShuffle2(double[] a,
                                            double[] b) {
        final int n = a.length;
        final int halfOfN = n >> 1;

        int j = 0;
        for (int i = 0; i < n; i++) {
            if (i < j) {
                // swap indices i & j
                double temp = a[i];
                a[i] = a[j];
                a[j] = temp;

                temp = b[i];
                b[i] = b[j];
                b[j] = temp;
            }

            int k = halfOfN;
            while (k <= j && k > 0) {
                j -= k;
                k >>= 1;
            }
            j += k;
        }
    }

    /**
     * Pads data so that its length is a power of 2
     *
     * @param data the 1D array data
     * @return the padded 1D array data
     */
    private Array pad(Array data) {
        int length = (int) data.getSize();
        if (ArithmeticUtils.isPowerOfTwo(length)) {
            return data;
        }

        int pLength = TransformUtils.nextPowerOfTwo(length);
        Array padded = Array.factory(data.getDataType(), new int[]{pLength});
        for (int i = 0; i < length; i++) {
            padded.setObject(i, data.getObject(i));
        }

        return padded;
    }

    /**
     * Normalization types.
     */
    public enum Norm {
        /**
         * Should be passed to the constructor of {@link FastFourierTransform}
         * to use the <em>standard</em> normalization convention. This normalization
         * convention is defined as follows
         * <ul>
         * <li>forward transform: \( y_n = \sum_{k = 0}^{N - 1} x_k e^{-2 \pi i n k / N} \),</li>
         * <li>inverse transform: \( x_k = \frac{1}{N} \sum_{n = 0}^{N - 1} y_n e^{2 \pi i n k / N} \),</li>
         * </ul>
         * where \( N \) is the size of the data sample.
         */
        STD,

        /**
         * Should be passed to the constructor of {@link FastFourierTransform}
         * to use the <em>unitary</em> normalization convention. This normalization
         * convention is defined as follows
         * <ul>
         * <li>forward transform: \( y_n = \frac{1}{\sqrt{N}} \sum_{k = 0}^{N - 1} x_k e^{-2 \pi i n k / N} \),</li>
         * <li>inverse transform: \( x_k = \frac{1}{\sqrt{N}} \sum_{n = 0}^{N - 1} y_n e^{2 \pi i n k / N} \),</li>
         * </ul>
         * where \( N \) is the size of the data sample.
         */
        UNIT,

        /**
         * Not do normalization
         */
        NONE;
    }
}
