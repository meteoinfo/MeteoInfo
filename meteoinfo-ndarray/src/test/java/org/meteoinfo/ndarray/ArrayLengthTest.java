package org.meteoinfo.ndarray;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArrayLengthTest {
    @Test
    public void testLength() {
        int n = 6296;
        long n1 = (long) n * (n - 1) / 2;
        int n2 = (int) n1;
        System.out.println(n2);
        double[] d = new double[n2];
        double[][] d1 = new double[n / 2][(n - 1)];
        System.out.println(d.length);
        assertEquals(d.length, d1.length * d1[0].length);
    }
}
