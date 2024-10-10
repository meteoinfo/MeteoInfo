package org.meteoinfo.math.transform;

import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.Complex;

import java.util.function.DoubleUnaryOperator;
import java.util.function.UnaryOperator;

/**
 * {@link Complex} transform.
 * <p>
 * Such transforms include {@link FastSineTransform sine transform},
 * {@link FastCosineTransform cosine transform} or {@link
 * FastHadamardTransform Hadamard transform}.
 */
public interface ComplexTransform extends UnaryOperator<Array> {
    /**
     * Returns the transform of the specified data set.
     *
     * @param f the data array to be transformed (signal).
     * @return the transformed array (spectrum).
     * @throws IllegalArgumentException if the transform cannot be performed.
     */
    Array apply(Array f);

    /**
     * Returns the transform of the specified data set.
     *
     * @param f the data array to be transformed (signal).
     * @return the transformed array (spectrum).
     * @throws IllegalArgumentException if the transform cannot be performed.
     */
    Array apply(double[] f);

    /**
     * Returns the transform of the specified function.
     *
     * @param f   Function to be sampled and transformed.
     * @param min Lower bound (inclusive) of the interval.
     * @param max Upper bound (exclusive) of the interval.
     * @param n   Number of sample points.
     * @return the result.
     * @throws IllegalArgumentException if the transform cannot be performed.
     */
    default Array apply(DoubleUnaryOperator f,
                            double min,
                            double max,
                            int n) {
        return apply(TransformUtils.sample(f, min, max, n));
    }
}
