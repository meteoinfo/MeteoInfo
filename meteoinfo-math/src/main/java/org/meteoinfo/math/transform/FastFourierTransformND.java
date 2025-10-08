package org.meteoinfo.math.transform;

import org.meteoinfo.ndarray.*;
import org.meteoinfo.ndarray.math.ArrayMath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FastFourierTransformND extends FastFourierTransform {

    /**
     * Constructor
     */
    public FastFourierTransformND() {
        super();
    }

    /**
     * Constructor
     * @param inverse Whether is inverse transform
     */
    public FastFourierTransformND(boolean inverse) {
        super(inverse);
    }

    @Override
    public Array apply(Array f) {
        List<Integer> axes = new ArrayList<>();
        for (int i = f.getRank() - 1; i >= 0; i--) {
            axes.add(i);
        }

        return apply(f, axes);
    }

    /**
     * Apply
     * @param f Input array
     * @param axes The axes
     * @return Array after N-D FFT transformation
     */
    public Array apply(Array f, List<Integer> axes) {
        f = f.copyIfView();

        int[] shape = f.getShape();
        Array r = Array.factory(DataType.COMPLEX, shape);

        try {
            FastFourierTransform fastFourierTransform = new FastFourierTransform(this.normalization, this.inverse);

            int[] current;
            int axisIdx = 0;
            for (int axis : axes) {
                if (axis < 0) {
                    axis = shape.length + axis;
                }
                Index indexr = r.getIndex();
                for (int i = 0; i < r.getSize(); i++) {
                    current = indexr.getCurrentCounter();
                    if (current[axis] == 0) {
                        List<Range> ranges = new ArrayList<>();
                        for (int j = 0; j < shape.length; j++) {
                            if (j == axis) {
                                ranges.add(new Range(0, shape[j] - 1, 1));
                            } else {
                                ranges.add(new Range(current[j], current[j], 1));
                            }
                        }
                        Array data;
                        if (axisIdx == 0) {
                            data = ArrayMath.section(f, ranges).copy();
                        } else {
                            data = ArrayMath.section(r, ranges).copy();
                        }
                        data = fastFourierTransform.apply(data);
                        ArrayMath.setSection(r, ranges, data);
                    }
                    indexr.incr();
                }
                axisIdx += 1;
            }
        } catch (InvalidRangeException e) {
            e.printStackTrace();
        }

        return r;
    }

}
