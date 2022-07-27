package org.meteoinfo.data.meteodata.util;

import org.meteoinfo.data.dimarray.DimArray;
import org.meteoinfo.data.dimarray.Dimension;
import org.meteoinfo.data.meteodata.DataInfo;
import org.meteoinfo.data.meteodata.Variable;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.InvalidRangeException;
import org.meteoinfo.ndarray.Range;
import org.meteoinfo.ndarray.math.ArrayMath;

import java.util.ArrayList;
import java.util.List;

public class WRFUtil {
    /**
     * Un-Stagger a dimension array
     * @param array The dimension array
     * @param axis The axis
     * @return Un-Staggered dimension array
     */
    public static DimArray deStagger(DimArray array, int axis) {
        try {
            Dimension sDim = array.getDimension(axis);
            Range range1 = new Range(0, sDim.getLength() - 2);
            Range range2 = new Range(1, sDim.getLength() - 1);
            Array sDimValue = sDim.getDimValue();
            Array sDimValue1 = Array.factory(DataType.DOUBLE, new int[]{sDim.getLength() - 1});
            for (int i = 0; i < sDimValue1.getSize(); i++) {
                sDimValue1.setDouble(i, 0.5 * (sDimValue.getDouble(i) + sDimValue.getDouble(i + 1)));
            }
            Dimension sDim1 = new Dimension(sDim);
            sDim1.setDimValue(sDimValue1);
            sDim1.setStagger(false);

            List<Range> rangeList1 = new ArrayList<>();
            List<Range> rangeList2 = new ArrayList<>();
            List<Dimension> dimensions = new ArrayList<>();
            for (int i = 0; i < array.getDimensions().size(); i++) {
                if (i == axis) {
                    rangeList1.add(range1);
                    rangeList2.add(range2);
                    dimensions.add(sDim1);
                } else {
                    Dimension dimension = array.getDimension(i);
                    rangeList1.add(new Range(dimension.getLength()));
                    rangeList2.add(new Range(dimension.getLength()));
                    dimensions.add(dimension);
                }
            }
            Array array1 = array.getArray().section(rangeList1);
            Array array2 = array.getArray().section(rangeList2);
            Array r = ArrayMath.mul(ArrayMath.add(array1, array2), 0.5);

            return new DimArray(r, dimensions);
        } catch (InvalidRangeException e) {
            e.printStackTrace();
            return array;
        }
    }

    /**
     * Un-Stagger a dimension array
     * @param array The dimension array
     * @return Un-Staggered dimension array
     */
    public static DimArray deStagger(DimArray array) {
        int idx = array.getStaggerDimIndex();
        if (idx >= 0) {
            return deStagger(array, idx);
        } else {
            //System.out.println("The dimension array has no stagger dimension!");
            return array;
        }
    }

    /**
     * Get geopotential height array - meter
     * @param dataInfo The WRF data info
     * @return Geopotential height
     */
    public static DimArray getGPM(DataInfo dataInfo) {
        DimArray ph = dataInfo.readDimArray("PH");
        ph = deStagger(ph);
        DimArray phb = dataInfo.readDimArray("PHB");
        phb = deStagger(phb);
        Array gpm = ArrayMath.div(ArrayMath.add(ph.getArray(), phb.getArray()), 9.81);

        return new DimArray(gpm, ph.getDimensions());
    }

    /**
     * Get geopotential 1-D height array - meter
     * @param dataInfo The WRF data info
     * @return Geopotential height
     */
    public static DimArray getGPM1D(DataInfo dataInfo) {
        Variable variable = dataInfo.getVariable("PH");
        List<Range> ranges = new ArrayList<>();
        for (Dimension dimension : variable.getDimensions()) {
            if (dimension.isStagger()) {
                ranges.add(new Range(dimension.getLength()));
            } else {
                ranges.add(new Range(1));
            }
        }
        DimArray ph = dataInfo.readDimArray("PH", ranges);
        ph = deStagger(ph);
        DimArray phb = dataInfo.readDimArray("PHB", ranges);
        phb = deStagger(phb);
        Array gpm = ArrayMath.div(ArrayMath.add(ph.getArray(), phb.getArray()), 9.81);

        return new DimArray(gpm, ph.getDimensions());
    }
}
