package org.meteoinfo.math.transform;

import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.Complex;
import org.meteoinfo.ndarray.DataType;

public class FFT {

    // compute the fast fourier transform of x[], assuming its length is a power of 2
    public static Complex[] fft(Complex[] x) {
        int N = x.length;

        // base case
        if (N == 1) return new Complex[] { x[0] };

        // radix 2 Cooley-Tukey FFT
        if (N % 2 != 0) { throw new RuntimeException("N is not a power of 2"); }

        // fft of even terms
        Complex[] even = new Complex[N/2];
        for (int k = 0; k < N/2; k++) {
            even[k] = x[2*k];
        }
        Complex[] q = fft(even);

        // fft of odd terms
        Complex[] odd  = even;  // reuse the array
        for (int k = 0; k < N/2; k++) {
            odd[k] = x[2*k + 1];
        }
        Complex[] r = fft(odd);

        // combine
        Complex[] y = new Complex[N];
        for (int k = 0; k < N/2; k++) {
            double kth = -2 * k * Math.PI / N;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            y[k] = q[k].add(wk.multiply(r[k]));
            y[k + N/2] = q[k].subtract(wk.multiply(r[k]));
        }
        return y;
    }

    // compute the FFT of x[], assuming its length is a power of 2
    public static Array fft(Array x) {
        x = x.copyIfView();

        int N = (int) x.getSize();

        // base case
        if (N == 1) return x.copy();

        // radix 2 Cooley-Tukey FFT
        if (N % 2 != 0) { throw new RuntimeException("N is not a power of 2"); }

        // fft of even terms
        Complex[] even = new Complex[N / 2];
        for (int k = 0; k < N / 2; k++) {
            even[k] = x.getComplex(2 * k);
        }
        Complex[] q = fft(even);

        // fft of odd terms
        Complex[] odd  = even;  // reuse the array
        for (int k = 0; k < N/2; k++) {
            odd[k] = x.getComplex(2 * k + 1);
        }
        Complex[] r = fft(odd);

        // combine
        Array y = Array.factory(DataType.COMPLEX, new int[]{N});
        for (int k = 0; k < N / 2; k++) {
            double kth = -2 * k * Math.PI / N;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            y.setComplex(k, q[k].add(wk.multiply(r[k])));
            y.setComplex(k + N / 2, q[k].subtract(wk.multiply(r[k])));
        }
        return y;
    }

    // compute the inverse FFT of x[], assuming its length is a power of 2
    public static Complex[] ifft(Complex[] x) {
        int N = x.length;
        Complex[] y = new Complex[N];

        // take conjugate
        for (int i = 0; i < N; i++) {
            y[i] = x[i].conj();
        }

        // compute forward FFT
        y = fft(y);

        // take conjugate again
        for (int i = 0; i < N; i++) {
            y[i] = y[i].conj();
        }

        // divide by N
        for (int i = 0; i < N; i++) {
            y[i] = y[i].multiply(1.0 / N);
        }

        return y;
    }

    // compute the inverse FFT of x[], assuming its length is a power of 2
    public static Array ifft(Array x) {
        x = x.copyIfView();

        int N = (int) x.getSize();
        Array y = Array.factory(DataType.COMPLEX, new int[]{N});

        // take conjugate
        for (int i = 0; i < N; i++) {
            y.setComplex(i, x.getComplex(i).conj());
        }

        // compute forward FFT
        y = fft(y);

        // take conjugate again
        for (int i = 0; i < N; i++) {
            y.setComplex(i, y.getComplex(i).conj());
        }

        // divide by N
        for (int i = 0; i < N; i++) {
            y.setComplex(i, y.getComplex(i).multiply(1.0 / N));
        }

        return y;
    }

    // compute the circular convolution of x and y
    public static Complex[] cConvolve(Complex[] x, Complex[] y) {

        // should probably pad x and y with 0s so that they have same length
        // and are powers of 2
        if (x.length != y.length) { throw new RuntimeException("Dimensions don't agree"); }

        int N = x.length;

        // compute FFT of each sequence
        Complex[] a = fft(x);
        Complex[] b = fft(y);

        // point-wise multiply
        Complex[] c = new Complex[N];
        for (int i = 0; i < N; i++) {
            c[i] = a[i].multiply(b[i]);
        }

        // compute inverse FFT
        return ifft(c);
    }


    // compute the linear convolution of x and y
    public static Complex[] convolve(Complex[] x, Complex[] y) {
        Complex ZERO = new Complex(0, 0);

        Complex[] a = new Complex[2*x.length];
        for (int i = 0;        i <   x.length; i++) a[i] = x[i];
        for (int i = x.length; i < 2*x.length; i++) a[i] = ZERO;

        Complex[] b = new Complex[2*y.length];
        for (int i = 0;        i <   y.length; i++) b[i] = y[i];
        for (int i = y.length; i < 2*y.length; i++) b[i] = ZERO;

        return cConvolve(a, b);
    }

    // display an array of Complex numbers to standard output
    public static void show(Complex[] x, String title) {
        System.out.println(title);
        System.out.println("-------------------");
        for (int i = 0; i < x.length; i++) {
            System.out.println(x[i]);
        }
        System.out.println();
    }


    public static Complex[] toComplex(double[] SZ)
    {
        int count = SZ.length;
        Complex[] C_SZ = new Complex[count];
        for (int i = 0; i < count; i++)
        {
            Complex d = new Complex(SZ[i],0);
            C_SZ[i] = d;
        }
        return C_SZ;
    }
}
