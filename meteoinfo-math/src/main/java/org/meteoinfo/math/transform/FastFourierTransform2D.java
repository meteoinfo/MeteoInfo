package org.meteoinfo.math.transform;

import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.InvalidRangeException;
import org.meteoinfo.ndarray.Range;
import org.meteoinfo.ndarray.math.ArrayMath;
import org.meteoinfo.ndarray.math.ArrayUtil;

import java.util.Arrays;
import java.util.List;

public class FastFourierTransform2D extends FastFourierTransform {

    /**
     * Constructor
     */
    public FastFourierTransform2D() {
        super();
    }

    /**
     * Constructor
     * @param inverse Whether is inverse transform
     */
    public FastFourierTransform2D(boolean inverse) {
        super(inverse);
    }

    @Override
    public Array apply(Array f) {
        f = f.copyIfView();

        int[] shape = f.getShape();
        int nRow = shape[0];
        int nCol = shape[1];
        Array r = Array.factory(DataType.COMPLEX, shape);

        try {
            FastFourierTransform fastFourierTransform = new FastFourierTransform(this.normalization, this.inverse);

            //Transform rows
            Range xRange = new Range(0, nCol - 1, 1);
            Range yRange;
            List<Range> ranges;
            for (int i = 0; i < nRow; i++) {
                yRange = new Range(i, i);
                ranges = Arrays.asList(yRange, xRange);
                Array data = ArrayMath.section(f, ranges).copy();
                data = fastFourierTransform.apply(data);
                ArrayMath.setSection(r, ranges, data);
            }

            //Transform cols
            yRange = new Range(0, nRow - 1, 1);
            for (int i = 0; i < nCol; i++) {
                xRange = new Range(i, i);
                ranges = Arrays.asList(yRange, xRange);
                Array data = ArrayMath.section(r, ranges).copy();
                data = fastFourierTransform.apply(data);
                ArrayMath.setSection(r, ranges, data);
            }
        } catch (InvalidRangeException e) {
            e.printStackTrace();
        }

        return r;
    }

}
