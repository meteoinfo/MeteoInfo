package org.meteoinfo.ndarray.io.matlab;

import org.meteoinfo.ndarray.Array;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.types.MatFile;
import us.hebi.matlab.mat.types.Matrix;
import us.hebi.matlab.mat.types.Source;
import us.hebi.matlab.mat.types.Sources;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Mat {

    /**
     * Load data from MatLab data file
     *
     * @param fileName MatLab file name
     * @return Data map
     */
    public static Map<String, Array> load(String fileName) {
        try (Source source = Sources.openFile(fileName)) {
            MatFile mat = Mat5.newReader(source).readMat();
            Map<String, Array> map = new HashMap<>();
            for (MatFile.Entry entry : mat.getEntries()) {
                Array array = MatLabUtil.fromMatLabArray((Matrix) entry.getValue());
                map.put(entry.getName(), array);
            }

            return map;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
