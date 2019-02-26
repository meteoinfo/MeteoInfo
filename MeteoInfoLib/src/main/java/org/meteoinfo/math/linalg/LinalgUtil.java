/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.math.linalg;

import java.util.List;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.fitting.leastsquares.MultivariateJacobianFunction;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.CholeskyDecomposition;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.apache.commons.math3.util.Pair;
import org.ejml.data.Complex_F64;
import org.ejml.simple.SimpleBase;
import org.ejml.simple.SimpleEVD;
import org.ejml.simple.SimpleMatrix;
import org.ejml.simple.SimpleSVD;
import org.meteoinfo.data.ArrayUtil;
import org.meteoinfo.math.Complex;
import ucar.ma2.Array;
import ucar.ma2.DataType;

/**
 *
 * @author Yaqiang Wang
 */
public class LinalgUtil {

    /**
     * Solve a linear matrix equation, or system of linear scalar equations.
     *
     * @param a Coefficient matrix.
     * @param b Ordinate or “dependent variable” values.
     * @return Solution to the system a x = b. Returned shape is identical to b.
     */
    public static Array solve(Array a, Array b) {
        Array r = Array.factory(DataType.DOUBLE, b.getShape());
        double[][] aa = (double[][]) ArrayUtil.copyToNDJavaArray(a);
        RealMatrix coefficients = new Array2DRowRealMatrix(aa, false);
        DecompositionSolver solver = new LUDecomposition(coefficients).getSolver();
        double[] bb = (double[]) ArrayUtil.copyToNDJavaArray(b);
        RealVector constants = new ArrayRealVector(bb, false);
        RealVector solution = solver.solve(constants);
        for (int i = 0; i < r.getSize(); i++) {
            r.setDouble(i, solution.getEntry(i));
        }

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
        Array r = Array.factory(DataType.DOUBLE, a.getShape());
        double[][] aa = (double[][]) ArrayUtil.copyToNDJavaArray(a);
        RealMatrix matrix = new Array2DRowRealMatrix(aa, false);
        CholeskyDecomposition decomposition = new CholeskyDecomposition(matrix);
        RealMatrix L = decomposition.getL();
        int n = L.getColumnDimension();
        int m = L.getRowDimension();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                r.setDouble(i * n + j, L.getEntry(i, j));
            }
        }

        return r;
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
        Array Pa = Array.factory(DataType.DOUBLE, a.getShape());
        Array La = Array.factory(DataType.DOUBLE, a.getShape());
        Array Ua = Array.factory(DataType.DOUBLE, a.getShape());
        double[][] aa = (double[][]) ArrayUtil.copyToNDJavaArray(a);
        RealMatrix matrix = new Array2DRowRealMatrix(aa, false);
        LUDecomposition decomposition = new LUDecomposition(matrix);
        RealMatrix P = decomposition.getP();
        RealMatrix L = decomposition.getL();
        RealMatrix U = decomposition.getU();
        int n = L.getColumnDimension();
        int m = L.getRowDimension();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                Pa.setDouble(i * n + j, P.getEntry(i, j));
                La.setDouble(i * n + j, L.getEntry(i, j));
                Ua.setDouble(i * n + j, U.getEntry(i, j));
            }
        }

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
        int m = a.getShape()[0];
        int n = a.getShape()[1];
        Array Qa = Array.factory(DataType.DOUBLE, new int[]{m, m});
        Array Ra = Array.factory(DataType.DOUBLE, a.getShape());
        double[][] aa = (double[][]) ArrayUtil.copyToNDJavaArray(a);
        RealMatrix matrix = new Array2DRowRealMatrix(aa, false);
        QRDecomposition decomposition = new QRDecomposition(matrix);
        RealMatrix Q = decomposition.getQ();
        RealMatrix R = decomposition.getR();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                Qa.setDouble(i * m + j, Q.getEntry(i, j));
            }
        }
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                Ra.setDouble(i * n + j, R.getEntry(i, j));
            }
        }

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
        int m = a.getShape()[0];
        int n = a.getShape()[1];
        int k = Math.min(m, n);
        Array Ua = Array.factory(DataType.DOUBLE, new int[]{m, k});
        Array Va = Array.factory(DataType.DOUBLE, new int[]{k, n});
        Array Sa = Array.factory(DataType.DOUBLE, new int[]{k});
        double[][] aa = (double[][]) ArrayUtil.copyToNDJavaArray(a);
        RealMatrix matrix = new Array2DRowRealMatrix(aa, false);
        SingularValueDecomposition decomposition = new SingularValueDecomposition(matrix);
        RealMatrix U = decomposition.getU();
        RealMatrix V = decomposition.getVT();
        double[] sv = decomposition.getSingularValues();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < k; j++) {
                Ua.setDouble(i * k + j, U.getEntry(i, j));
            }
        }
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < n; j++) {
                Va.setDouble(i * n + j, V.getEntry(i, j));
            }
        }
        for (int i = 0; i < k; i++) {
            Sa.setDouble(i, sv[i]);
        }

        return new Array[]{Ua, Sa, Va};
    }

//    /**
//     * Calculates the compact Singular Value Decomposition of a matrix.
//     * The Singular Value Decomposition of matrix A is a set of three matrices: U, Σ and V 
//     * such that A = U × Σ × VT. Let A be a m × n matrix, then U is a m × p orthogonal 
//     * matrix, Σ is a p × p diagonal matrix with positive or null elements, V is a p × n 
//     * orthogonal matrix (hence VT is also orthogonal) where p=min(m,n).
//     * @param a Given matrix.
//     * @return Result U/S/V arrays.
//     */
//    public static Array[] svd_JAMA(Array a){
//        int m = a.getShape()[0];        
//        int n = a.getShape()[1];
//        int k = Math.min(m, n);
//        double[][] aa = (double[][])ArrayUtil.copyToNDJavaArray(a);
//        Matrix M = new Matrix(aa);
//        Jama.SingularValueDecomposition svd = M.svd();
//        Array Ua = Array.factory(DataType.DOUBLE, new int[]{m, k});
//        Array Va = Array.factory(DataType.DOUBLE, new int[]{n, n});
//        Array Sa = Array.factory(DataType.DOUBLE, new int[]{k});
//        Matrix U = svd.getU();
//        Matrix V = svd.getV();     
//        double[] sv = svd.getSingularValues();
//        for (int i = 0; i < m; i++){
//            for (int j = 0; j < k; j++){
//                Ua.setDouble(i * k + j, U.get(i, j));
//            }
//        }
//        for (int i = 0; i < n; i++){
//            for (int j = 0; j < n; j++){
//                Va.setDouble(i * n + j, V.get(i, j));
//            }
//        }
//        for (int i = 0; i < k; i++){
//            Sa.setDouble(i, sv[i]);
//        }
//        
//        return new Array[]{Ua, Sa, Va};
//    }
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
    public static Array[] svd_EJML(Array a) {
        int m = a.getShape()[0];
        int n = a.getShape()[1];
        int k = Math.min(m, n);
        double[][] aa = (double[][]) ArrayUtil.copyToNDJavaArray(a);
        SimpleMatrix M = new SimpleMatrix(aa);
        SimpleSVD svd = M.svd(false);
        Array Ua = Array.factory(DataType.DOUBLE, new int[]{m, m});
        Array Va = Array.factory(DataType.DOUBLE, new int[]{n, n});
        Array Sa = Array.factory(DataType.DOUBLE, new int[]{k});
        SimpleBase U = svd.getU();
        SimpleBase V = svd.getV();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                Ua.setDouble(i * m + j, U.get(i, j));
            }
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Va.setDouble(j * n + i, V.get(i, j));
            }
        }
        for (int i = 0; i < k; i++) {
            //Sa.setDouble(i, sv[i]);
            Sa.setDouble(i, svd.getSingleValue(i));
        }

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
    public static Array[] eigen_bak(Array a) {
        int m = a.getShape()[0];
        Array Wa;
        Array Va = Array.factory(DataType.DOUBLE, new int[]{m, m});
        double[][] aa = (double[][]) ArrayUtil.copyToNDJavaArray(a);
        RealMatrix matrix = new Array2DRowRealMatrix(aa, false);
        EigenDecomposition decomposition = new EigenDecomposition(matrix);
        if (decomposition.hasComplexEigenvalues()) {
            Wa = Array.factory(DataType.OBJECT, new int[]{m});
            double[] rev = decomposition.getRealEigenvalues();
            double[] iev = decomposition.getImagEigenvalues();
            for (int i = 0; i < m; i++) {
                Wa.setObject(i, new Complex(rev[i], iev[i]));
                RealVector v = decomposition.getEigenvector(i);
                for (int j = 0; j < v.getDimension(); j++) {
                    Va.setDouble(j * m + i, v.getEntry(j));
                }
            }
        } else {
            RealMatrix V = decomposition.getV();
            RealMatrix D = decomposition.getD();
            Wa = Array.factory(DataType.DOUBLE, new int[]{m});
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < m; j++) {
                    Va.setDouble(i * m + (m - j - 1), V.getEntry(i, j));
                    if (i == j) {
                        Wa.setDouble(m - i - 1, D.getEntry(i, j));
                    }
                }
            }
        }

        return new Array[]{Wa, Va};
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
        int m = a.getShape()[0];
        Array Wa;
        Array Va = Array.factory(DataType.DOUBLE, new int[]{m, m});
        double[][] aa = (double[][]) ArrayUtil.copyToNDJavaArray(a);
        RealMatrix matrix = new Array2DRowRealMatrix(aa, false);
        EigenDecomposition decomposition = new EigenDecomposition(matrix);
        double[] rev = decomposition.getRealEigenvalues();
        double[] iev = decomposition.getImagEigenvalues();
        if (decomposition.hasComplexEigenvalues()) {
            Wa = Array.factory(DataType.OBJECT, new int[]{m});
            for (int i = 0; i < m; i++) {
                Wa.setObject(i, new Complex(rev[i], iev[i]));
                RealVector v = decomposition.getEigenvector(i);
                for (int j = 0; j < v.getDimension(); j++) {
                    Va.setDouble(j * m + i, v.getEntry(j));
                }
            }
        } else {
            Wa = Array.factory(DataType.DOUBLE, new int[]{m});
            for (int i = 0; i < m; i++) {
                Wa.setDouble(i, rev[m - i - 1]);
                RealVector v = decomposition.getEigenvector(m - i - 1);
                for (int j = 0; j < v.getDimension(); j++) {
                    Va.setDouble(j * m + i, v.getEntry(j));
                }
            }
        }

        return new Array[]{Wa, Va};
    }

    /**
     * Calculates the eigen decomposition of a real matrix. The eigen
     * decomposition of matrix A is a set of two matrices: V and D such that A =
     * V × D × VT. A, V and D are all m × m matrices.
     *
     * @param a Given matrix.
     * @return Result W/V arrays.
     */
    public static Array[] eigen_EJML(Array a) {
        int m = a.getShape()[0];
        Array Wa;
        Array Va = Array.factory(DataType.DOUBLE, new int[]{m, m});
        double[][] aa = (double[][]) ArrayUtil.copyToNDJavaArray(a);
        SimpleMatrix M = new SimpleMatrix(aa);
        SimpleEVD evd = M.eig();
        List<Complex_F64> evs = evd.getEigenvalues();
        boolean isComplex = evd.getEigenVector(0) == null;
        if (isComplex) {
            Wa = Array.factory(DataType.OBJECT, new int[]{m});
        } else {
            Wa = Array.factory(DataType.DOUBLE, new int[]{m});
        }
        for (int i = 0; i < m; i++) {
            if (isComplex) {
                Wa.setObject(i, new Complex(evs.get(i).real, evs.get(i).imaginary));
            } else {
                Wa.setDouble(i, evs.get(m - i - 1).real);
                SimpleBase v = evd.getEigenVector(m - i - 1);
                for (int j = 0; j < v.getNumElements(); j++) {
                    Va.setDouble(j * m + i, v.get(j));
                }
            }
        }

        return new Array[]{Wa, Va};
    }

    /**
     * Calculate inverse matrix
     *
     * @param a The matrix
     * @return Inverse matrix array
     */
    public static Array inv(Array a) {
        double[][] aa = (double[][]) ArrayUtil.copyToNDJavaArray(a);
        RealMatrix matrix = new Array2DRowRealMatrix(aa, false);
        RealMatrix invm = MatrixUtils.inverse(matrix);
        if (invm == null) {
            return null;
        }

        int m = invm.getRowDimension();
        int n = invm.getColumnDimension();
        Array r = Array.factory(DataType.DOUBLE, new int[]{m, n});
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                r.setDouble(i * n + j, invm.getEntry(i, j));
            }
        }

        return r;
    }

    /**
     * Not correct at present !!!
     * @param a
     * @param b
     * @return 
     */
    public static Array lstsq(Array a, Array b) {
        final double[][] aa = (double[][]) ArrayUtil.copyToNDJavaArray(a);
        final double[] bb = (double[]) ArrayUtil.copyToNDJavaArray(b);

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
}
