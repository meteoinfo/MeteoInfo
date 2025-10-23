package org.meteoinfo.jython;

import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.Complex;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.IndexIterator;
import org.python.core.Py;
import org.python.core.PyComplex;
import org.python.core.PyObject;
import org.python.modules.time.PyTimeTuple;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
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

    /**
     * Convert Jython datetime to Java LocalDateTime
     *
     * @param dt Jython datetime object
     * @return Java LocalDateTime object
     */
    public static LocalDateTime toDateTime(PyObject dt) {
        Calendar calendar = (Calendar) dt.__tojava__(Calendar.class);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(calendar.toInstant(), calendar.getTimeZone().toZoneId());

        return localDateTime;
    }

    /**
     * Convert Java LocalDateTime object to Jython datetime object
     *
     * @param dt Java LocalDatetime object
     * @return Jython datetime object
     */
    public static PyObject toDateTime(LocalDateTime dt) {
        Timestamp timestamp = Timestamp.valueOf(dt);

        return Py.newDatetime(timestamp);
    }

    /**
     * Convert Java LocalDateTime array to Jython datetime array or verse vise
     *
     * @param a Java LocalDateTime array or Jython datetime array
     * @return Jython datatime array or Java LocalDateTime array
     */
    public static Array toDateTime(Array a) {
        IndexIterator iterA = a.getIndexIterator();
        if (a.getDataType() == DataType.DATE) {
            Array r = Array.factory(DataType.OBJECT, a.getShape());
            IndexIterator interR = r.getIndexIterator();
            while (interR.hasNext()) {
                interR.setObjectNext(toDateTime(iterA.getDateNext()));
            }

            return r;
        } else {
            Array r = Array.factory(DataType.DATE, a.getShape());
            IndexIterator interR = r.getIndexIterator();
            while (interR.hasNext()) {
                interR.setDateNext(toDateTime((PyObject) iterA.getObjectNext()));
            }

            return r;
        }
    }


}
