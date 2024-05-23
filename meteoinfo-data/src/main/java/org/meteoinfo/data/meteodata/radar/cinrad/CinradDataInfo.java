package org.meteoinfo.data.meteodata.radar.cinrad;

import org.meteoinfo.data.GridArray;
import org.meteoinfo.data.GridData;
import org.meteoinfo.data.meteodata.DataInfo;
import org.meteoinfo.data.meteodata.IGridDataInfo;
import org.meteoinfo.ndarray.Array;

import java.io.IOException;
import java.io.RandomAccessFile;

public class CinradDataInfo extends DataInfo {

    private static final int MISSING_INT = -9999;
    private static final float MISSING_FLOAT = Float.NaN;
    public static boolean isSC = false;
    public static boolean isCC = false;
    public static boolean isCC20 = false;

    @Override
    public boolean isValidFile(RandomAccessFile raf) {
        return isCINRAD(raf);
    }

    public boolean isCINRAD(RandomAccessFile raf) {
        try {
            raf.seek(0);

            byte[] b128 = new byte[136];
            raf.read(b128);
            String radarT = new String(b128);

            if (radarT.contains("CINRAD/SC") || radarT.contains("CINRAD/CD")) {
                isSC = true;
                isCC = false;
                isCC20 = false;
                return true;
            } else if (radarT.contains("CINRADC")) {
                isCC = true;
                isSC = false;
                isCC20 = false;
                return true;
            } else if (!radarT.contains("CINRADC") && radarT.contains("CINRAD/CC")) {
                isCC20 = true;
                isSC = false;
                isCC = false;
                return true;
            } else {
                isSC = false;
                isCC = false;
                isCC20 = false;
                return false;
            }
        } catch (IOException ioe) {
            return false;
        }

    }

    @Override
    public void readDataInfo(String fileName) {

    }

    @Override
    public Array read(String varName) {
        return null;
    }

    @Override
    public Array read(String varName, int[] origin, int[] size, int[] stride) {
        return null;
    }

}
