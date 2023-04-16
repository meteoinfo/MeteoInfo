package org.meteoinfo.math.matrix;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MatrixTest {

    double[][] A = {
            {0.9000, 0.4000, 0.0000f},
            {0.4000, 0.5000, 0.3000f},
            {0.0000, 0.3000, 0.8000f}
    };
    double[] b = {0.5, 0.5, 0.5f};
    double[][] C = {
            {0.97, 0.56, 0.12f},
            {0.56, 0.50, 0.39f},
            {0.12, 0.39, 0.73f}
    };

    Matrix matrix = new Matrix(A);

    @Test
    public void testMv() {
        matrix = new Matrix(A);
        double[] d = matrix.mv(b);
        assertEquals(0.65, d[0], 1E-7);
    }

    @Test
    public void testTv() {
        matrix = new Matrix(A);
        double[] d = matrix.tv(b);
        assertEquals(0.65, d[0], 1E-7);
    }

    @Test
    public void testSVDSolve() {
        matrix = new Matrix(A);
        Matrix.SVD svd = matrix.svd(true, true);
        double[] d = svd.solve(b);
        assertEquals(0.3642384179155737, d[0], 1E-7);
    }
}
