/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.math.linalg;

import org.apache.commons.math3.fitting.leastsquares.*;
import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.util.Pair;
import org.ejml.data.Complex_F64;
import org.ejml.simple.SimpleBase;
import org.ejml.simple.SimpleEVD;
import org.ejml.simple.SimpleMatrix;
import org.ejml.simple.SimpleSVD;
import org.meteoinfo.math.matrix.Matrix;
import org.meteoinfo.math.matrix.MatrixUtil;
import org.meteoinfo.ndarray.math.ArrayUtil;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.Complex;
import org.meteoinfo.ndarray.DataType;
import smile.math.blas.UPLO;

import java.util.List;

/**
 *
 * @author Yaqiang Wang
 */
public class LinalgUtil {

    /**
     * Matrix dot operator
     * @param a Matrix a
     * @param b Matrix b
     * @return Result matrix
     */
    public static Array dot(Array a, Array b) {
        Matrix ma = MatrixUtil.arrayToMatrix(a);
        Matrix mb = MatrixUtil.arrayToMatrix(b);
        Matrix mr = ma.mm(mb);

        return MatrixUtil.matrixToArray(mr);
    }

    /**
     * Solve a linear matrix equation, or system of linear scalar equations.
     *
     * @param a Coefficient matrix.
     * @param b Ordinate or “dependent variable” values.
     * @return Solution to the system a x = b. Returned shape is identical to b.
     */
    public static Array solve(Array a, Array b) {
        Matrix ma = MatrixUtil.arrayToMatrix(a);
        Matrix.LU lu = ma.lu();
        double[] bb = (double[]) ArrayUtil.copyToNDJavaArray_Double(b);
        double[] x = lu.solve(bb);
        Array r = Array.factory(DataType.DOUBLE, b.getShape(), x);

        return r;
    }

    /**
     * Calculates the Cholesky decomposition of a matrix. The Cholesky
     * decomposition of a real symmetric positive-definite matrix A consists of
     * a lower triangular matrix L with same size such that: A = LLT. In a
     * sense, this is the square root of A.
     *
     * @param a The given matrix.
     * @return Result array.
     */
    public static Array cholesky(Array a) {
        return cholesky(a, true);
    }

    /**
     * Calculates the Cholesky decomposition of a matrix. The Cholesky
     * decomposition of a real symmetric positive-definite matrix A consists of
     * a lower triangular matrix L with same size such that: A = LLT. In a
     * sense, this is the square root of A.
     *
     * @param a The given matrix.
     * @param lower Lower triangle or upper triangle matrix
     * @return Result array.
     */
    public static Array cholesky(Array a, boolean lower) {
        UPLO uplo = lower ? UPLO.LOWER : UPLO.UPPER;
        Matrix ma = MatrixUtil.arrayToMatrix(a);
        ma.uplo(uplo);
        Matrix.Cholesky cholesky = ma.cholesky();

        return MatrixUtil.matrixToArray(cholesky.lu, uplo);
    }

    /**
     * Calculates the LUP-decomposition of a square matrix. The
     * LUP-decomposition of a matrix A consists of three matrices L, U and P
     * that satisfy: P×A = L×U. L is lower triangular (with unit diagonal
     * terms), U is upper triangular and P is a permutation matrix. All matrices
     * are m×m.
     *
     * @param a Given matrix.
     * @return Result P/L/U arrays.
     */
    public static Array[] lu(Array a) {
        Matrix ma = new Matrix((double[][]) ArrayUtil.copyToNDJavaArray_Double(a));
        Matrix.LU lu = ma.lu();
        Array La = MatrixUtil.matrixToArray(lu.lu, UPLO.LOWER, 1.0);
        Array Ua = MatrixUtil.matrixToArray(lu.lu, UPLO.UPPER);
        int m = lu.ipiv.length;
        Matrix P = new Matrix(m, m);
        for (int i = 0; i < m; i++) {
            P.set(i, i, 1.0);
        }
        double temp;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                temp = P.get(i, j);
                P.set(i, j, P.get(lu.ipiv[i] - 1, j));
                P.set(lu.ipiv[i] - 1, j, temp);
            }
        }
        P = P.transpose();
        Array Pa = MatrixUtil.matrixToArray(P);

        return new Array[]{Pa, La, Ua};
    }

    /**
     * Calculates the QR-decomposition of a matrix. The QR-decomposition of a
     * matrix A consists of two matrices Q and R that satisfy: A = QR, Q is
     * orthogonal (QTQ = I), and R is upper triangular. If A is m×n, Q is m×m
     * and R m×n.
     *
     * @param a Given matrix.
     * @return Result Q/R arrays.
     */
    public static Array[] qr(Array a) {
        Matrix ma = new Matrix((double[][]) ArrayUtil.copyToNDJavaArray_Double(a));
        Matrix.QR qr = ma.qr();
        Array Qa = MatrixUtil.matrixToArray(qr.Q());
        Array Ra = MatrixUtil.matrixToArray(qr.R());

        return new Array[]{Qa, Ra};
    }

    /**
     * Calculates the compact Singular Value Decomposition of a matrix. The
     * Singular Value Decomposition of matrix A is a set of three matrices: U, Σ
     * and V such that A = U × Σ × VT. Let A be a m × n matrix, then U is a m ×
     * p orthogonal matrix, Σ is a p × p diagonal matrix with positive or null
     * elements, V is a p × n orthogonal matrix (hence VT is also orthogonal)
     * where p=min(m,n).
     *
     * @param a Given matrix.
     * @return Result U/S/V arrays.
     */
    public static Array[] svd(Array a) {
        Matrix ma = MatrixUtil.arrayToMatrix(a);
        Matrix.SVD svd = ma.svd();

        Array Ua = MatrixUtil.matrixToArray(svd.U);
        Array Va = MatrixUtil.matrixToArray(svd.V);
        Array Sa = Array.factory(DataType.DOUBLE, new int[]{svd.s.length}, svd.s);

        return new Array[]{Ua, Sa, Va};
    }

    /**
     * Calculates the eigen decomposition of a real matrix. The eigen
     * decomposition of matrix A is a set of two matrices: V and D such that A =
     * V × D × VT. A, V and D are all m × m matrices.
     *
     * @param a Given matrix.
     * @return Result W/V arrays.
     */
    public static Array[] eigen(Array a) {
        Matrix ma = MatrixUtil.arrayToMatrix(a);
        boolean isSymmetric = MatrixUtil.isSymmetric(ma);
        if (isSymmetric) {
            ma.uplo(UPLO.LOWER);
        }
        Matrix.EVD evd = ma.eigen(false, true, false);
        Array Wa;
        if (evd.wi == null) {
            Wa = Array.factory(DataType.DOUBLE, new int[]{evd.wr.length}, evd.wr);
        } else {
            boolean isComplex = false;
            for (int i = 0; i < evd.wi.length; i++) {
                if (evd.wi[i] != 0) {
                    isComplex = true;
                    break;
                }
            }
            if (isComplex)
                Wa = MatrixUtil.toArray(evd.wr, evd.wi);
            else
                Wa = Array.factory(DataType.DOUBLE, new int[]{evd.wr.length}, evd.wr);
        }
        Array Va = MatrixUtil.matrixToArray(evd.Vr);

        return new Array[]{Wa, Va};
    }

    /**
     * Calculate inverse matrix
     *
     * @param a The matrix
     * @return Inverse matrix array
     */
    public static Array inv(Array a) {
        Matrix ma = MatrixUtil.arrayToMatrix(a);
        Matrix r = ma.inverse();

        return r == null ? null : MatrixUtil.matrixToArray(r);
    }

    /**
     * Not correct at present !!!
     *
     * @param a
     * @param b
     * @return
     */
    public static Array lstsq(Array a, Array b) {
        final double[][] aa = (double[][]) ArrayUtil.copyToNDJavaArray_Double(a);
        final double[] bb = (double[]) ArrayUtil.copyToNDJavaArray_Double(b);

        // the model function 
        MultivariateJacobianFunction function = new MultivariateJacobianFunction() {
            @Override
            public Pair<RealVector, RealMatrix> value(final RealVector point) {
                RealVector value = new ArrayRealVector(bb.length);
                RealMatrix jacobian = new Array2DRowRealMatrix(aa, false);
                for (int i = 0; i < bb.length; ++i) {

                }
                return new Pair<>(value, jacobian);

            }
        };

        // least squares problem to solve
        LeastSquaresProblem problem = new LeastSquaresBuilder().
                //start(new double[]{100.0, 50.0}).
                model(function).
                target(bb).
                lazyEvaluation(false).
                maxEvaluations(1000).
                maxIterations(1000).
                build();
        LeastSquaresOptimizer.Optimum optimum = new LevenbergMarquardtOptimizer().optimize(problem);

        RealVector r = optimum.getPoint();
        int n = r.getDimension();
        Array x = Array.factory(DataType.DOUBLE, new int[]{n});
        for (int i = 0; i < n; i++) {
            x.setDouble(i, r.getEntry(i));
        }

        return x;
    }

    // Function to get cofactor of  
    // mat[p][q] in temp[][]. n is  
    // current dimension of mat[][] 
    public static void getCofactor(double mat[][],
            double temp[][], int p, int q, int n) {
        int i = 0, j = 0;

        // Looping for each element of  
        // the matrix 
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {

                // Copying into temporary matrix  
                // only those element which are  
                // not in given row and column 
                if (row != p && col != q) {
                    temp[i][j++] = mat[row][col];

                    // Row is filled, so increase  
                    // row index and reset col  
                    //index 
                    if (j == n - 1) {
                        j = 0;
                        i++;
                    }
                }
            }
        }
    }

    /* Recursive function for finding determinant 
    of matrix. n is current dimension of mat[][]. */
    public static double determinantOfMatrix(double mat[][], int n, int N) {
        int D = 0; // Initialize result 

        // Base case : if matrix contains single 
        // element 
        if (n == 1) {
            return mat[0][0];
        }

        // To store cofactors 
        double temp[][] = new double[N][N];

        // To store sign multiplier 
        int sign = 1;

        // Iterate for each element of first row 
        for (int f = 0; f < n; f++) {
            // Getting Cofactor of mat[0][f] 
            getCofactor(mat, temp, 0, f, n);
            D += sign * mat[0][f]
                    * determinantOfMatrix(temp, n - 1, N);

            // terms are to be added with  
            // alternate sign 
            sign = -sign;
        }

        return D;
    }

    /**
     * Calculate determinant of a matrix array
     *
     * @param mat Input array
     * @return Determinant
     */
    public static double determinantOfMatrix(Array mat) {
        int n = mat.getShape()[0];
        double[][] a = (double[][]) ArrayUtil.copyToNDJavaArray_Double(mat);
        return determinantOfMatrix(a, n, n);
    }
}
