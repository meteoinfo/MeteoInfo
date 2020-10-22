package org.meteoinfo.data.meteodata.grib.grib2;

import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import ucar.nc2.grib.grib2.Grib2Record;
import ucar.nc2.grib.grib2.Grib2RecordScanner;
import ucar.unidata.io.RandomAccessFile;

import java.io.IOException;

public class GRIB2RecordReader {

    private RandomAccessFile raf;
    private Grib2RecordScanner scanner;

    /**
     * Constructor
     */
    public GRIB2RecordReader() {

    }

    /**
     * Constructor
     * @param fileName Grib2 file name
     */
    public GRIB2RecordReader(String fileName) {
        this.open(fileName);
    }

    /**
     * Open Grib2 data file
     * @param fileName Grib2 data file name
     */
    public void open(String fileName) {
        try {
            raf = new RandomAccessFile(fileName, "r");
            scanner = new Grib2RecordScanner(raf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Close data file
     * @throws IOException
     */
    public void close() throws IOException {
        this.raf.close();
    }

    /**
     * Get if the scanner has next data record
     * @return Has next data record or not
     * @throws IOException
     */
    public boolean hasNext() throws IOException {
        return this.scanner.hasNext();
    }

    /**
     * Get next data record
     * @return Data record
     * @throws IOException
     */
    public Grib2Record next() throws IOException {
        return this.scanner.next();
    }

    /**
     * Get data record from start byte
     * @param position Start byte position
     * @return Data record
     * @throws IOException
     */
    public Grib2Record getDataRecord(long position) throws IOException {
        this.raf.seek(position);
        return this.scanner.next();
    }

    /**
     * Get current file position
     * @return Current file position
     */
    public long getCurrentPosition() {
        return this.raf.getFilePointer();
    }

    /**
     * Read data array
     * @param record Grib2 data record
     * @return Data array
     * @throws IOException
     */
    public Array readData(Grib2Record record) throws IOException {
        long position = this.raf.getFilePointer();
        float[] data = record.readData(this.raf);
        int ny = record.getGDS().getNy();
        int nx = record.getGDS().getNx();
        Array r = Array.factory(DataType.FLOAT, new int[]{ny, nx}, data);
        this.raf.seek(position);

        return r;
    }
}
