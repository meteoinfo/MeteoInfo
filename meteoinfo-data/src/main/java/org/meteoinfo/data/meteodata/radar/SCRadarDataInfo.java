package org.meteoinfo.data.meteodata.radar;

import org.meteoinfo.data.meteodata.DataInfo;
import org.meteoinfo.ndarray.Array;

import java.io.RandomAccessFile;

public class SCRadarDataInfo extends DataInfo implements IRadarDataInfo {

    @Override
    public boolean isValidFile(RandomAccessFile raf) {
        return false;
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

    @Override
    public RadarDataType getRadarDataType() {
        return RadarDataType.SAB;
    }
}
