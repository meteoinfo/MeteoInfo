package org.meteoinfo.math.matrix;

import org.apache.commons.math4.core.jdkmath.AccurateMath;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.Complex;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.math.ArrayUtil;
import org.meteoinfo.math.blas.UPLO;
import org.meteoinfo.math.matrix.DMatrix;
import org.meteoinfo.math.matrix.SymmMatrix;

public class MatrixUtil {

    /** Exponent offset in IEEE754 representation. */
    private static final long EXPONENT_OFFSET = 1023L;
    public static final double EPSILON = Double.longBitsToDouble((EXPONENT_OFFSET - 53L) << 52);

    /**
     * Convert matrix to array
     * @param a Matrix
     * @return Array
     */
    public static Array matrixToArray(DMatrix a) {
        int nRows = a.nrows();
        int nCols = a.ncols();
        Array r = Array.factory(DataType.DOUBLE, new int[]{nRows, nCols});
        for (int i = 0; i < nRows; i++) {
            for (int j = 0; j < nCols; j++) {
                r.setDouble(i * nCols + j, a.get(i, j));
            }
        }

        return r;
    }

    /**
     * Convert matrix to array
     * @param a Matrix
     * @param uplo Upper or lower triangle matrix
     * @return Array
     */
    public static Array matrixToArray(DMatrix a, UPLO uplo) {
        int nRows = a.nrows();
        int nCols = a.ncols();
        Array r = Array.factory(DataType.DOUBLE, new int[]{nRows, nCols});
        if (uplo == UPLO.LOWER) {
            for (int i = 0; i < nRows; i++) {
                for (int j = 0; j < nCols; j++) {
                    if (j > i)
                        continue;
                    r.setDouble(i * nCols + j, a.get(i, j));
                }
            }
        } else {
            for (int i = 0; i < nRows; i++) {
                for (int j = 0; j < nCols; j++) {
                    if (i > j)
                        continue;
                    r.setDouble(i * nCols + j, a.get(i, j));
                }
            }
        }

        return r;
    }

    /**
     * Convert matrix to array
     * @param a Matrix
     * @param uplo Upper or lower triangle matrix
     * @param diagonalValue Diagonal value
     * @return Array
     */
    public static Array matrixToArray(DMatrix a, UPLO uplo, double diagonalValue) {
        int nRows = a.nrows();
        int nCols = a.ncols();
        Array r = Array.factory(DataType.DOUBLE, new int[]{nRows, nCols});
        if (uplo == UPLO.LOWER) {
            for (int i = 0; i < nRows; i++) {
                for (int j = 0; j < nCols; j++) {
                    if (j < i) {
                        r.setDouble(i * nCols + j, a.get(i, j));
                    } else if (j == i) {
                        r.setDouble(i * nCols + j, diagonalValue);
                    }
                }
            }
        } else {
            for (int i = 0; i < nRows; i++) {
                for (int j = 0; j < nCols; j++) {
                    if (i < j) {
                        r.setDouble(i * nCols + j, a.get(i, j));
                    } else if (i == j) {
                        r.setDouble(i * nCols + j, diagonalValue);
                    }
                }
            }
        }

        return r;
    }

    /**
     * Convert real and image one dimension array to complex array
     * @param real Real part array
     * @param image Image part array
     * @return Complex array
     */
    public static Array toArray(double[] real, double[] image) {
        Array a = Array.factory(DataType.COMPLEX, new int[]{real.length});
        for (int i = 0; i < a.getSize(); i++) {
            a.setComplex(i, new Complex(real[i], image[i]));
        }
        return a;
    }

    /**
     * Convert array to matrix
     * @param a Array
     * @return Matrix
     */
    public static Matrix arrayToMatrix(Array a) {
        Matrix ma;
        if (a.getRank() == 2)
            ma = new Matrix((double[][]) ArrayUtil.copyToNDJavaArray_Double(a));
        else
            ma = new Matrix((double[]) ArrayUtil.copyToNDJavaArray_Double(a));

        return ma;
    }

    /**
     * Convert array to symmetric matrix
     * @param a Array
     * @return Symmetric matrix
     */
    public static SymmMatrix arrayToSymmMatrix(Array a) {
        SymmMatrix ma = new SymmMatrix(UPLO.LOWER, (double[][]) ArrayUtil.copyToNDJavaArray_Double(a));

        return ma;
    }

    /**
     * Checks whether a matrix is symmetric, within a given relative tolerance.
     * @param matrix The matrix
     * @return If the matrix is symmetric
     */
    public static boolean isSymmetric(Matrix matrix) {
        final int rows = matrix.m;
        if (rows != matrix.n) {
            return false;
        }

        final double relativeTolerance = 10 * matrix.m * matrix.n * EPSILON;
        for (int i = 0; i < rows; i++) {
            for (int j = i + 1; j < rows; j++) {
                final double mij = matrix.get(i, j);
                final double mji = matrix.get(j, i);
                if (AccurateMath.abs(mij - mji) >
                        AccurateMath.max(AccurateMath.abs(mij), AccurateMath.abs(mji)) * relativeTolerance) {
                    return false;
                }
            }
        }
        return true;
    }
}
