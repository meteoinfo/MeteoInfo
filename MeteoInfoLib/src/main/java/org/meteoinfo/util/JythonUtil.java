package org.meteoinfo.util;

import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.Complex;
import org.meteoinfo.ndarray.DataType;
import org.python.core.PyComplex;

import java.util.List;

public class JythonUtil {

    /**
     * Convert PyComplex list to ArrayComplex
     * @param data PyComplex list
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
        if (data.get(0) instanceof PyComplex) {
            Array a = Array.factory(DataType.COMPLEX, new int[]{data.size()});
            PyComplex pd;
            for (int i = 0; i < data.size(); i++) {
                pd = (PyComplex)data.get(i);
                a.setObject(i, new Complex(pd.real, pd.imag));
            }
            return a;
        } else if (data.get(0) instanceof List) {
            int ndim = data.size();
            int len = ((List) data.get(0)).size();
            Array a = Array.factory(DataType.COMPLEX, new int[]{ndim, len});
            PyComplex pd;
            for (int i = 0; i < ndim; i++) {
                List<Object> d = (List) data.get(i);
                for (int j = 0; j < len; j++) {
                    pd = (PyComplex) d.get(j);
                    a.setObject(i * len + j, new Complex(pd.real, pd.imag));
                }
            }
            return a;
        }
        return null;
    }

}
