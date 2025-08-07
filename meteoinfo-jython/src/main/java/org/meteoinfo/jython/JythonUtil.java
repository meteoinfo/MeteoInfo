package org.meteoinfo.jython;

import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.Complex;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.IndexIterator;
import org.python.core.PyComplex;
import org.python.core.PyObject;
import org.python.modules.time.PyTimeTuple;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class JythonUtil {

    /**
     * Convert jython complex to java complex
     * @param v Jython complex
     * @return Java complex
     */
    public static Complex toComplex(PyComplex v) {
        return new Complex(v.real, v.imag);
    }

    /**
     * Convert java complex to jython complex
     * @param v Java complex
     * @return Jython complex
     */
    public static PyComplex toComplex(Complex v) {
        return new PyComplex(v.real(), v.imag());
    }

    /**
     * Convert PyComplex value to ArrayComplex
     * @param data PyComplex value
     * @return ArrayComplex
     */
    public static Array toComplexArray(PyComplex data) {
        Array a = Array.factory(DataType.COMPLEX, new int[]{1});
        Complex d = new Complex(data.real, data.imag);
        a.setComplex(0, d);
        return a;
    }

    /**
     * Convert PyComplex list to ArrayComplex
     * @param data PyComplex list
     * @return ArrayComplex
     */
    public static Array toComplexArray(List<Object> data) {
        if (data.get(0) instanceof List) {
            int ndim = data.size();
            int len = ((List) data.get(0)).size();
            Array a = Array.factory(DataType.COMPLEX, new int[]{ndim, len});
            PyComplex pd;
            for (int i = 0; i < ndim; i++) {
                List<Object> d = (List) data.get(i);
                for (int j = 0; j < len; j++) {
                    if (d.get(j) instanceof PyComplex) {
                        pd = (PyComplex) d.get(j);
                        a.setComplex(i * len + j, new Complex(pd.real, pd.imag));
                    } else {
                        a.setComplex(i * len + j, new Complex((double) d.get(j), 0));
                    }
                }
            }
            return a;
        } else {
            Array a = Array.factory(DataType.COMPLEX, new int[]{data.size()});
            PyComplex pd;
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i) instanceof PyComplex) {
                    pd = (PyComplex) data.get(i);
                    a.setComplex(i, new Complex(pd.real, pd.imag));
                } else {
                    a.setComplex(i, new Complex((double) data.get(i), 0));
                }
            }
            return a;
        }
    }

}
