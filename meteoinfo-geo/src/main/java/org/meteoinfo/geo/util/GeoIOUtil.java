package org.meteoinfo.geo.util;

import org.meteoinfo.data.GridData;
import org.meteoinfo.geo.mapdata.MapDataManage;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.Index;
import org.meteoinfo.ndarray.Index2D;
import org.meteoinfo.projection.ProjectionInfo;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GeoIOUtil {

    /**
     * Save 2D array data to bil file
     * @param fileName The bil file name
     * @param data The 2D array data
     * @param xArray X coordinate array - 1D
     * @param yArray Y coordinate array - 1D
     * @param projInfo The projection
     * @throws IOException
     */
    public static void saveAsBILFile(String fileName, Array data, Array xArray, Array yArray,
                                     ProjectionInfo projInfo) throws IOException {
        data = data.copyIfView();
        xArray = xArray.copyIfView();
        yArray = yArray.copyIfView();

        try {
            //Save data file
            DataOutputStream outs = new DataOutputStream(new FileOutputStream(fileName));
            int xn = (int) xArray.getSize();
            int yn = (int) yArray.getSize();
            Index2D index = (Index2D) data.getIndex();
            for (int i = 0; i < yn; i++) {
                for (int j = 0; j < xn; j++) {
                    index.set(yn - i - 1, j);
                    outs.writeFloat(data.getFloat(index));
                }
            }
            outs.close();

            //Save header file
            String hfn = fileName.replace(".bil", ".hdr");
            BufferedWriter sw = new BufferedWriter(new FileWriter(new File(hfn)));
            sw.write("nrows " + String.valueOf(yn));
            sw.newLine();
            sw.write("ncols " + String.valueOf(xn));
            sw.newLine();
            sw.write("nbands 1");
            sw.newLine();
            sw.write("nbits 32");
            sw.newLine();
            sw.write("pixeltype float");
            sw.newLine();
            sw.write("byteorder M");
            sw.newLine();
            sw.write("layout bil");
            sw.newLine();
            sw.write("ulxmap " + String.valueOf(xArray.getDouble(0)));
            sw.newLine();
            sw.write("ulymap " + String.valueOf(yArray.getDouble(yn - 1)));
            sw.newLine();
            sw.write("xdim " + String.valueOf(xArray.getDouble(1) - xArray.getDouble(0)));
            sw.newLine();
            sw.write("ydim " + String.valueOf(yArray.getDouble(1) - yArray.getDouble(0)));
            sw.newLine();

            sw.flush();
            sw.close();

            if (!projInfo.isLonLat()) {
                String projFn = fileName.substring(0, fileName.length() - 3) + "prj";
                MapDataManage.writeProjFile(projFn, projInfo);
            }
        } catch (IOException ex) {
            Logger.getLogger(GridData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
