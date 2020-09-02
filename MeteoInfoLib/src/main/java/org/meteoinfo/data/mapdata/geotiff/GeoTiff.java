/* Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 */
package org.meteoinfo.data.mapdata.geotiff;

import org.meteoinfo.data.GridData;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.meteoinfo.math.ArrayMath;
import org.meteoinfo.data.GridArray;
import org.meteoinfo.data.mapdata.geotiff.compression.CompressionDecoder;
import org.meteoinfo.data.mapdata.geotiff.compression.DeflateCompression;
import org.meteoinfo.data.mapdata.geotiff.compression.LZWCompression;
import org.meteoinfo.global.DataConvert;
import org.meteoinfo.global.util.BigDecimalUtil;
import org.meteoinfo.projection.KnownCoordinateSystems;
import org.meteoinfo.projection.info.ProjectionInfo;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.Index;

/**
 *
 * @author yaqiang
 */
public class GeoTiff {
    // <editor-fold desc="Variables">

    private String filename;
    private RandomAccessFile file;
    private FileChannel channel;
    private List<IFDEntry> tags = new ArrayList();
    private ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
    private boolean readonly;
    private boolean showBytes = false;
    private boolean debugRead = false;
    private boolean debugReadGeoKey = false;
    private boolean showHeaderBytes = false;
    private int headerSize = 8;
    private int firstIFD = 0;
    private int lastIFD = 0;
    private int startOverflowData = 0;
    private int nextOverflowData = 0;
    private List<GeoKey> geokeys = new ArrayList();
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     *
     * @param filename File name
     */
    public GeoTiff(String filename) {
        this.filename = filename;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Close
     *
     * @throws IOException IOException
     */
    public void close()
            throws IOException {
        if (this.channel != null) {
            if (!this.readonly) {
                this.channel.force(true);
                this.channel.truncate(this.nextOverflowData);
            }
            this.channel.close();
        }
        if (this.file != null) {
            this.file.close();
        }
    }

    /**
     * Add tag
     *
     * @param ifd IFDEntry
     */
    void addTag(IFDEntry ifd) {
        this.tags.add(ifd);
    }

    /**
     * Delete tag
     *
     * @param ifd IFDEntry
     */
    void deleteTag(IFDEntry ifd) {
        this.tags.remove(ifd);
    }

    /**
     * Set transform
     *
     * @param xStart X start
     * @param yStart Y start
     * @param xInc X inc
     * @param yInc Y inc
     */
    void setTransform(double xStart, double yStart, double xInc, double yInc) {
        addTag(new IFDEntry(Tag.ModelTiepointTag, FieldType.DOUBLE).setValue(new double[]{0.0D, 0.0D, 0.0D, xStart, yStart, 0.0D}));

        addTag(new IFDEntry(Tag.ModelPixelScaleTag, FieldType.DOUBLE).setValue(new double[]{xInc, yInc, 0.0D}));
    }

    /**
     * Add geo key
     *
     * @param geokey Geo key
     */
    void addGeoKey(GeoKey geokey) {
        this.geokeys.add(geokey);
    }

    /**
     * Write geo keys
     */
    private void writeGeoKeys() {
        if (this.geokeys.isEmpty()) {
            return;
        }

        int extra_chars = 0;
        int extra_ints = 0;
        int extra_doubles = 0;
        for (GeoKey geokey : this.geokeys) {
            if (geokey.isDouble) {
                extra_doubles += geokey.count();
            } else if (geokey.isString) {
                extra_chars += geokey.valueString().length() + 1;
            } else if (geokey.count() > 1) {
                extra_ints += geokey.count();
            }
        }
        int n = (this.geokeys.size() + 1) * 4;
        int[] values = new int[n + extra_ints];
        double[] dvalues = new double[extra_doubles];
        char[] cvalues = new char[extra_chars];
        int icounter = n;
        int dcounter = 0;
        int ccounter = 0;

        values[0] = 1;
        values[1] = 1;
        values[2] = 0;
        values[3] = this.geokeys.size();
        int count = 4;
        for (GeoKey geokey : this.geokeys) {
            values[(count++)] = geokey.tagCode();

            if (geokey.isDouble) {
                values[(count++)] = Tag.GeoDoubleParamsTag.getCode();
                values[(count++)] = geokey.count();
                values[(count++)] = dcounter;
                for (int k = 0; k < geokey.count(); k++) {
                    dvalues[(dcounter++)] = geokey.valueD(k);
                }
            } else if (geokey.isString) {
                String s = geokey.valueString();
                values[(count++)] = Tag.GeoAsciiParamsTag.getCode();
                values[(count++)] = s.length();
                values[(count++)] = ccounter;
                for (int k = 0; k < s.length(); k++) {
                    cvalues[(ccounter++)] = s.charAt(k);
                }
                cvalues[(ccounter++)] = '\000';
            } else if (geokey.count() > 1) {
                values[(count++)] = Tag.GeoKeyDirectoryTag.getCode();
                values[(count++)] = geokey.count();
                values[(count++)] = icounter;
                for (int k = 0; k < geokey.count(); k++) {
                    values[(icounter++)] = geokey.value(k);
                }
            } else {
                values[(count++)] = 0;
                values[(count++)] = 1;
                values[(count++)] = geokey.value();
            }
        }

        addTag(new IFDEntry(Tag.GeoKeyDirectoryTag, FieldType.SHORT).setValue(values));
        if (extra_doubles > 0) {
            addTag(new IFDEntry(Tag.GeoDoubleParamsTag, FieldType.DOUBLE).setValue(dvalues));
        }
        if (extra_chars > 0) {
            addTag(new IFDEntry(Tag.GeoAsciiParamsTag, FieldType.ASCII).setValue(new String(cvalues)));
        }
    }

    /**
     * Write data
     *
     * @param data Data array
     * @param imageNumber Image number
     * @return Int
     * @throws IOException
     */
    int writeData(byte[] data, int imageNumber) throws IOException {
        if (this.file == null) {
            init();
        }
        if (imageNumber == 1) {
            this.channel.position(this.headerSize);
        } else {
            this.channel.position(this.nextOverflowData);
        }
        ByteBuffer buffer = ByteBuffer.wrap(data);
        this.channel.write(buffer);

        if (imageNumber == 1) {
            this.firstIFD = (this.headerSize + data.length);
        } else {
            this.firstIFD = (data.length + this.nextOverflowData);
        }
        return this.nextOverflowData;
    }

    /**
     * Write data
     *
     * @param data Float data array
     * @param imageNumber Image number
     * @return Int
     * @throws IOException
     */
    int writeData(float[] data, int imageNumber) throws IOException {
        if (this.file == null) {
            init();
        }
        if (imageNumber == 1) {
            this.channel.position(this.headerSize);
        } else {
            this.channel.position(this.nextOverflowData);
        }

        ByteBuffer direct = ByteBuffer.allocateDirect(4 * data.length);
        FloatBuffer buffer = direct.asFloatBuffer();
        buffer.put(data);

        this.channel.write(direct);

        if (imageNumber == 1) {
            this.firstIFD = (this.headerSize + 4 * data.length);
        } else {
            this.firstIFD = (4 * data.length + this.nextOverflowData);
        }
        return this.nextOverflowData;
    }

    /**
     * Write meta data
     *
     * @param imageNumber Image number
     * @throws IOException
     */
    void writeMetadata(int imageNumber) throws IOException {
        if (this.file == null) {
            init();
        }

        writeGeoKeys();

        Collections.sort(this.tags);
        int start = 0;
        if (imageNumber == 1) {
            start = writeHeader(this.channel);
        } else {
            this.channel.position(this.lastIFD);
            ByteBuffer buffer = ByteBuffer.allocate(4);
            if (this.debugRead) {
                System.out.println("position before writing nextIFD= " + this.channel.position() + " IFD is " + this.firstIFD);
            }
            buffer.putInt(this.firstIFD);
            ((Buffer)buffer).flip();
            this.channel.write(buffer);
        }
        writeIFD(this.channel, this.firstIFD);
    }

    /**
     * Write header
     *
     * @param channel File channel
     * @return Int
     * @throws IOException
     */
    private int writeHeader(FileChannel channel) throws IOException {
        channel.position(0L);

        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put((byte) 77);
        buffer.put((byte) 77);
        buffer.putShort((short) 42);
        buffer.putInt(this.firstIFD);

        ((Buffer)buffer).flip();
        channel.write(buffer);

        return this.firstIFD;
    }

    /**
     * Init tags
     *
     * @throws IOException
     */
    public void initTags() throws IOException {
        this.tags = new ArrayList();
        this.geokeys = new ArrayList();
    }

    /**
     * Init
     *
     * @throws IOException
     */
    private void init() throws IOException {
        this.file = new RandomAccessFile(this.filename, "rw");
        this.channel = this.file.getChannel();
        if (this.debugRead) {
            System.out.println("Opened file to write: '" + this.filename + "', size=" + this.channel.size());
        }
        this.readonly = false;
    }

    /**
     * Write IFD
     *
     * @param channel File channel
     * @param start Start
     * @throws IOException
     */
    private void writeIFD(FileChannel channel, int start) throws IOException {
        channel.position(start);

        ByteBuffer buffer = ByteBuffer.allocate(2);
        int n = this.tags.size();
        buffer.putShort((short) n);
        ((Buffer)buffer).flip();
        channel.write(buffer);

        start += 2;
        this.startOverflowData = (start + 12 * this.tags.size() + 4);
        this.nextOverflowData = this.startOverflowData;

        for (IFDEntry elem : this.tags) {
            writeIFDEntry(channel, elem, start);
            start += 12;
        }

        channel.position(this.startOverflowData - 4);
        this.lastIFD = (this.startOverflowData - 4);
        if (this.debugRead) {
            System.out.println("pos before writing nextIFD= " + channel.position());
        }
        buffer = ByteBuffer.allocate(4);
        buffer.putInt(0);
        ((Buffer)buffer).flip();
        channel.write(buffer);
    }

    /**
     * Write IFDEntry
     *
     * @param channel File channel
     * @param ifd IFDEntry
     * @param start Start
     * @throws IOException
     */
    private void writeIFDEntry(FileChannel channel, IFDEntry ifd, int start) throws IOException {
        channel.position(start);
        ByteBuffer buffer = ByteBuffer.allocate(12);

        buffer.putShort((short) ifd.tag.getCode());
        buffer.putShort((short) ifd.type.code);
        buffer.putInt(ifd.count);

        int size = ifd.count * ifd.type.size;
        if (size <= 4) {
            int done = writeValues(buffer, ifd);
            for (int k = 0; k < 4 - done; k++) {
                buffer.put((byte) 0);
            }
            ((Buffer)buffer).flip();
            channel.write(buffer);
        } else {
            buffer.putInt(this.nextOverflowData);
            ((Buffer)buffer).flip();
            channel.write(buffer);

            channel.position(this.nextOverflowData);

            ByteBuffer vbuffer = ByteBuffer.allocate(size);
            writeValues(vbuffer, ifd);
            ((Buffer)vbuffer).flip();
            channel.write(vbuffer);
            this.nextOverflowData += size;
        }
    }

    /**
     * Write values
     *
     * @param buffer ByteBuffer
     * @param ifd IFDEntry
     * @return Int
     */
    private int writeValues(ByteBuffer buffer, IFDEntry ifd) {
        int done = 0;

        if (ifd.type == FieldType.ASCII) {
            return writeSValue(buffer, ifd);
        }
        if (ifd.type == FieldType.RATIONAL) {
            for (int i = 0; i < ifd.count * 2; i++) {
                done += writeIntValue(buffer, ifd, ifd.value[i]);
            }
        } else if (ifd.type == FieldType.FLOAT) {
            for (int i = 0; i < ifd.count; i++) {
                buffer.putFloat((float) ifd.valueD[i]);
            }
            done += ifd.count * 4;
        } else if (ifd.type == FieldType.DOUBLE) {
            for (int i = 0; i < ifd.count; i++) {
                buffer.putDouble(ifd.valueD[i]);
            }
            done += ifd.count * 8;
        } else {
            for (int i = 0; i < ifd.count; i++) {
                done += writeIntValue(buffer, ifd, ifd.value[i]);
            }
        }
        return done;
    }

    /**
     * Write value
     *
     * @param buffer ByteBuffer
     * @param ifd IFDEntry
     * @param v Int value
     * @return Int
     */
    private int writeIntValue(ByteBuffer buffer, IFDEntry ifd, int v) {
        switch (ifd.type.code) {
            case 1:
                buffer.put((byte) v);
                return 1;
            case 3:
                buffer.putShort((short) v);
                return 2;
            case 4:
                buffer.putInt(v);
                return 4;
            case 5:
                buffer.putInt(v);
                return 4;
            case 2:
        }
        return 0;
    }

    /**
     * Write size value
     *
     * @param buffer ByteBuffer
     * @param ifd IFDEntry
     * @return Int
     */
    private int writeSValue(ByteBuffer buffer, IFDEntry ifd) {
        buffer.put(ifd.valueS.getBytes());
        int size = ifd.valueS.length();
        if (size % 2 == 1) {
            size++;
        }
        return size;
    }

    /**
     * Read file
     *
     * @throws IOException
     */
    public void read()
            throws IOException {
        this.file = new RandomAccessFile(this.filename, "r");
        this.channel = this.file.getChannel();
        if (this.debugRead) {
            System.out.println("Opened file to read:'" + this.filename + "', size=" + this.channel.size());
        }
        this.readonly = true;

        int nextOffset = readHeader(this.channel);
        while (nextOffset > 0) {
            nextOffset = readIFD(this.channel, nextOffset);
            parseGeoInfo();
        }
    }

    /**
     * Find tag
     *
     * @param tag Tag
     * @return IFDEntry
     */
    IFDEntry findTag(Tag tag) {
        if (tag == null) {
            return null;
        }
        for (IFDEntry ifd : this.tags) {
            if (ifd.tag == tag) {
                return ifd;
            }
        }
        return null;
    }

    /**
     * Read X/Y coordinates
     *
     * @return X/Y coordinates
     */
    public List<double[]> readXY() {
        IFDEntry widthIFD = this.findTag(Tag.ImageWidth);
        IFDEntry heightIFD = this.findTag(Tag.ImageLength);
        int width = widthIFD.value[0];
        int height = heightIFD.value[0];
        double[] X = new double[width];
        double[] Y = new double[height];
        double minLon, maxLat, xdelta, ydelta;
        IFDEntry modelTransformationTag = this.findTag(Tag.ModelTransformationTag);
        if (modelTransformationTag != null){
            minLon = modelTransformationTag.valueD[3];
            maxLat = modelTransformationTag.valueD[7];
            xdelta = modelTransformationTag.valueD[0];
            ydelta = -modelTransformationTag.valueD[5];
        } else {
            IFDEntry modelTiePointTag = findTag(Tag.ModelTiepointTag);
            IFDEntry modelPixelScaleTag = findTag(Tag.ModelPixelScaleTag);
            minLon = modelTiePointTag.valueD[3];
            maxLat = modelTiePointTag.valueD[4];
            xdelta = modelPixelScaleTag.valueD[0];
            ydelta = modelPixelScaleTag.valueD[1];
        }
        for (int i = 0; i < width; i++) {
            X[i] = BigDecimalUtil.add(minLon, BigDecimalUtil.mul(xdelta, i));
        }
        for (int i = 0; i < height; i++) {
            Y[height - i - 1] = BigDecimalUtil.sub(maxLat, BigDecimalUtil.mul(ydelta, i));
        }

        List<double[]> xy = new ArrayList<>();
        xy.add(X);
        xy.add(Y);

        return xy;
    }

    /**
     * Get grid data
     *
     * @return Grid data
     */
    public int[][] readData() {
        try {
            //Grid data values
            IFDEntry widthIFD = this.findTag(Tag.ImageWidth);
            IFDEntry heightIFD = this.findTag(Tag.ImageLength);
            int width = widthIFD.value[0];
            int height = heightIFD.value[0];
            int[] values1d = readData(width, height);
            int[][] values = new int[height][width];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    values[height - i - 1][j] = values1d[i * width + j];
                }
            }
            values1d = null;

            return values;
        } catch (IOException ex) {
            Logger.getLogger(GeoTiff.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Get grid data
     *
     * @return Grid data
     */
    public GridArray getGridArray() {
        try {
            //Grid data values
            IFDEntry widthIFD = this.findTag(Tag.ImageWidth);
            IFDEntry heightIFD = this.findTag(Tag.ImageLength);
            int width = widthIFD.value[0];
            int height = heightIFD.value[0];
            GridArray gData = new GridArray();
            gData.setData(readArray());

            //Grid data coordinate
            double[] X = new double[width];
            double[] Y = new double[height];
            IFDEntry modelTiePointTag = findTag(Tag.ModelTiepointTag);
            IFDEntry modelPixelScaleTag = findTag(Tag.ModelPixelScaleTag);
            double minLon = modelTiePointTag.valueD[3];
            double maxLat = modelTiePointTag.valueD[4];
            double xdelt = modelPixelScaleTag.valueD[0];
            double ydelt = modelPixelScaleTag.valueD[1];
            for (int i = 0; i < width; i++) {
                X[i] = minLon + xdelt * i;
            }
            for (int i = 0; i < height; i++) {
                Y[height - i - 1] = maxLat - ydelt * i;
            }

            //gData.data = values;
            gData.xArray = X;
            gData.yArray = Y;

            //Get missing value
            IFDEntry noDataTag = findTag(Tag.GDALNoData);
            if (noDataTag != null) {
                double missingValue = Double.parseDouble(noDataTag.valueS);
                gData.missingValue = missingValue;
            }

            //Projection
            String projStr = getProjection();
            if (projStr != null) {
                gData.projInfo = ProjectionInfo.factory(projStr);
            } else {
                gData.projInfo = KnownCoordinateSystems.geographic.world.WGS1984;
            }

            return gData;
        } catch (IOException ex) {
            Logger.getLogger(GeoTiff.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Get grid data
     *
     * @return Grid data
     */
    public GridData getGridData() {
        try {
            //Grid data values
            IFDEntry widthIFD = this.findTag(Tag.ImageWidth);
            IFDEntry heightIFD = this.findTag(Tag.ImageLength);
            int width = widthIFD.value[0];
            int height = heightIFD.value[0];
            GridData.Integer gData = new GridData.Integer(height, width);
            int[] values1d = readData(width, height);
            //int[][] values = new int[height][width];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    //values[height - i - 1][j] = values1d[i * width + j];
                    gData.setValue(height - i - 1, j, values1d[i * width + j]);
                }
            }
            values1d = null;

            //Grid data coordinate
            double[] X = new double[width];
            double[] Y = new double[height];
            IFDEntry modelTiePointTag = findTag(Tag.ModelTiepointTag);
            IFDEntry modelPixelScaleTag = findTag(Tag.ModelPixelScaleTag);
            double minLon = modelTiePointTag.valueD[3];
            double maxLat = modelTiePointTag.valueD[4];
            double xdelt = modelPixelScaleTag.valueD[0];
            double ydelt = modelPixelScaleTag.valueD[1];
            for (int i = 0; i < width; i++) {
                X[i] = minLon + xdelt * i;
            }
            for (int i = 0; i < height; i++) {
                Y[height - i - 1] = maxLat - ydelt * i;
            }

            //gData.data = values;
            gData.xArray = X;
            gData.yArray = Y;

            //Projection
            String projStr = getProjection();
            if (projStr != null) {
                gData.projInfo = ProjectionInfo.factory(projStr);
            } else {
                gData.projInfo = KnownCoordinateSystems.geographic.world.WGS1984;
            }

            return gData;
        } catch (IOException ex) {
            Logger.getLogger(GeoTiff.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Get grid data
     *
     * @return Grid data
     */
    public GridData getGridData_Value() {
        try {
            //Grid data values
            IFDEntry widthIFD = this.findTag(Tag.ImageWidth);
            IFDEntry heightIFD = this.findTag(Tag.ImageLength);
            int width = widthIFD.value[0];
            int height = heightIFD.value[0];
            GridData.Integer gData = new GridData.Integer(height, width);
            int[] values1d = readData(width, height);
            //int[][] values = new int[height][width];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    //values[height - i - 1][j] = values1d[i * width + j];
                    gData.setValue(height - i - 1, j, values1d[i * width + j]);
                }
            }
            values1d = null;

            return gData;
        } catch (IOException ex) {
            Logger.getLogger(GeoTiff.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Read projection info
     *
     * @return Projection info
     */
    public ProjectionInfo readProj() {
        String projStr = getProjection();
        if (projStr != null) {
            return ProjectionInfo.factory(projStr);
        } else {
            return KnownCoordinateSystems.geographic.world.WGS1984;
        }
    }

    private String getProjection() {
        String projStr = null;
        IFDEntry geoKeyDirectoryTag = findTag(Tag.GeoKeyDirectoryTag);
        if (geoKeyDirectoryTag != null) {
            GeoKey gtModelTypeGeoKey = geoKeyDirectoryTag.findGeoKey(GeoKey.Tag.GTModelTypeGeoKey);
            if (gtModelTypeGeoKey.value() == 1) {
                GeoKey projCoordTrans = geoKeyDirectoryTag.findGeoKey(GeoKey.Tag.GeoKey_ProjCoordTrans);
                GeoKey projStdParallel1 = geoKeyDirectoryTag.findGeoKey(GeoKey.Tag.GeoKey_ProjStdParallel1);
                double lat_1 = projStdParallel1 == null ? 0 : projStdParallel1.valueD(0);
                GeoKey projStdParallel2 = geoKeyDirectoryTag.findGeoKey(GeoKey.Tag.GeoKey_ProjStdParallel2);
                double lat_2 = projStdParallel2 == null ? 0 :projStdParallel2.valueD(0);
                GeoKey projCenterLong = geoKeyDirectoryTag.findGeoKey(GeoKey.Tag.GeoKey_ProjNatOriginLong);
                if (projCenterLong == null){
                    projCenterLong = geoKeyDirectoryTag.findGeoKey(GeoKey.Tag.GeoKey_ProjCenterLong);
                }
                double lon_0 = projCenterLong == null ? 0 : projCenterLong.valueD(0);
                GeoKey projNatOriginLat = geoKeyDirectoryTag.findGeoKey(GeoKey.Tag.GeoKey_ProjNatOriginLat);
                double lat_0 = projNatOriginLat == null ? 0 : projNatOriginLat.valueD(0);
                GeoKey projFalseEasting = geoKeyDirectoryTag.findGeoKey(GeoKey.Tag.GeoKey_ProjFalseEasting);
                double x_0 = projFalseEasting == null ? 0 : projFalseEasting.valueD(0);
                GeoKey projFalseNorthing = geoKeyDirectoryTag.findGeoKey(GeoKey.Tag.GeoKey_ProjFalseNorthing);
                double y_0 = projFalseNorthing == null ? 0 : projFalseNorthing.valueD(0);
                GeoKey projScaleAtNatOrigin = geoKeyDirectoryTag.findGeoKey(GeoKey.Tag.ProjScaleAtNatOriginGeoKey);
                double k_0 = projScaleAtNatOrigin == null ? 1 : projScaleAtNatOrigin.valueD(0);
                switch (projCoordTrans.value()) {
                    case 1:    //Transverse Mercator
                        projStr = "+proj=tmerc"
                                + "+lat_0=" + String.valueOf(lat_0)
                                + "+lon_0=" + String.valueOf(lon_0)
                                + "+k_0=" + String.valueOf(k_0)
                                + "+x_0=" + String.valueOf(x_0)
                                + "+y_0=" + String.valueOf(y_0);
                        break;
                    case 11:    //AlbersEqualArea
                        projStr = "+proj=aea"
                                + "+lat_1=" + String.valueOf(lat_1)
                                + "+lat_2=" + String.valueOf(lat_2)
                                + "+lon_0=" + String.valueOf(lon_0)
                                + "+lat_0=" + String.valueOf(lat_0)
                                + "+x_0=" + String.valueOf(x_0)
                                + "+y_0=" + String.valueOf(y_0);
                        break;
                }
            }
        }

        return projStr;
    }

    /**
     * Test read data
     *
     * @param width Width
     * @param height Height
     * @return Data
     * @throws IOException
     */
    public int[] readData(int width, int height) throws IOException {
        int[] values = new int[width * height];
        IFDEntry bitsPerSampleTag = findTag(Tag.BitsPerSample);
        int bitsPerSample = bitsPerSampleTag.value[0];
        IFDEntry tileOffsetTag = findTag(Tag.TileOffsets);
        ByteBuffer buffer;
        if (tileOffsetTag != null) {
            int tileOffset = tileOffsetTag.value[0];
            IFDEntry tileSizeTag = findTag(Tag.TileByteCounts);
            IFDEntry tileLengthTag = findTag(Tag.TileLength);
            IFDEntry tileWidthTag = findTag(Tag.TileWidth);
            int tileWidth = tileWidthTag.value[0];
            int tileHeight = tileLengthTag.value[0];
            int hTileNum = (width + tileWidth - 1) / tileWidth;
            int vTileNum = (height + tileHeight - 1) / tileHeight;
            int tileSize = tileSizeTag.value[0];
            System.out.println("tileOffset =" + tileOffset + " tileSize=" + tileSize);
            int idx;
            int tileIdx, vIdx, hIdx;
            if (bitsPerSample == 8) {
                for (int i = 0; i < vTileNum; i++) {
                    for (int j = 0; j < hTileNum; j++) {
                        tileIdx = i * hTileNum + j;
                        tileOffset = tileOffsetTag.value[tileIdx];
                        tileSize = tileSizeTag.value[tileIdx];
                        buffer = testReadData(tileOffset, tileSize);
                        for (int h = 0; h < tileHeight; h++) {
                            vIdx = i * tileHeight + h;
                            if (vIdx == height) {
                                break;
                            }
                            for (int w = 0; w < tileWidth; w++) {
                                hIdx = j * tileWidth + w;
                                if (hIdx == width) {
                                    break;
                                }
                                idx = vIdx * width + hIdx;
                                values[idx] = buffer.get();
                            }
                        }
                    }
                }
            } else if (bitsPerSample == 16) {
                for (int i = 0; i < vTileNum; i++) {
                    for (int j = 0; j < hTileNum; j++) {
                        tileIdx = i * hTileNum + j;
                        tileOffset = tileOffsetTag.value[tileIdx];
                        tileSize = tileSizeTag.value[tileIdx];
                        buffer = testReadData(tileOffset, tileSize);
                        for (int h = 0; h < tileHeight; h++) {
                            vIdx = i * tileHeight + h;
                            if (vIdx == height) {
                                break;
                            }
                            for (int w = 0; w < tileWidth; w++) {
                                hIdx = j * tileWidth + w;
                                if (hIdx == width) {
                                    break;
                                }
                                idx = vIdx * width + hIdx;
                                values[idx] = buffer.getShort();
                            }
                        }
                    }
                }
            }
        } else {
            IFDEntry stripOffsetTag = findTag(Tag.StripOffsets);
            if (stripOffsetTag != null) {
                int stripNum = stripOffsetTag.count;
                int stripOffset;
                IFDEntry stripSizeTag = findTag(Tag.StripByteCounts);
                int stripSize = stripSizeTag.value[0];
                IFDEntry rowsPerStripTag = findTag(Tag.RowsPerStrip);
                int rowNum = rowsPerStripTag.value[0];
                //System.out.println("stripOffset =" + stripOffset + " stripSize=" + stripSize);
                int n = 0;
                for (int i = 0; i < stripNum; i++) {
                    stripOffset = stripOffsetTag.value[i];
                    buffer = testReadData(stripOffset, stripSize);
                    for (int j = 0; j < width * rowNum; j++) {
                        values[n] = buffer.getShort();
                        n += 1;
                    }
                }
            }
        }

        return values;
    }

    /**
     * Get band number
     *
     * @return Band number
     */
    public int getBandNum() {
        IFDEntry samplesPerPixelTag = findTag(Tag.SamplesPerPixel);
        int samplesPerPixel = samplesPerPixelTag.value[0];    //Number of bands
        return samplesPerPixel;
    }

    /**
     * Test read data
     *
     * @return Data
     * @throws IOException
     */
    public Array readArray() throws IOException {
        IFDEntry widthIFD = this.findTag(Tag.ImageWidth);
        IFDEntry heightIFD = this.findTag(Tag.ImageLength);
        int width = widthIFD.value[0];
        int height = heightIFD.value[0];
        IFDEntry samplesPerPixelTag = findTag(Tag.SamplesPerPixel);
        int samplesPerPixel = samplesPerPixelTag.value[0];    //Number of bands
        //int[] values = new int[width * height];
        IFDEntry bitsPerSampleTag = findTag(Tag.BitsPerSample);
        int bitsPerSample = bitsPerSampleTag.value[0];
        int[] shape;
        if (samplesPerPixel == 1) {
            shape = new int[]{height, width};
        } else {
            shape = new int[]{height, width, samplesPerPixel};
        }
//        if (samplesPerPixel == 1) {
//            shape = new int[]{width, height};
//        } else {
//            shape = new int[]{width, height, samplesPerPixel};
//        }
        DataType dataType = DataType.INT;
        IFDEntry sampleFormatTag = findTag(Tag.SampleFormat);
        int sampleFormat = 0;
        if (sampleFormatTag != null) {
            sampleFormat = sampleFormatTag.value[0];
        }
        switch (bitsPerSample) {
            case 32:
                switch (sampleFormat) {
                    case 3:
                        dataType = DataType.FLOAT;
                        break;
                }
                break;
        }
        Array r = Array.factory(dataType, shape);
        IFDEntry compressionTag = findTag(Tag.Compression);
        CompressionDecoder cDecoder = null;
        if (compressionTag != null){
            int compression = compressionTag.value[0];
            if (compression > 1){
                switch (compression) {
                    case 5:
                        cDecoder = new LZWCompression();
                        break;
                    case 8:
                        cDecoder = new DeflateCompression();
                        break;
                }
            }
        }
        IFDEntry tileOffsetTag = findTag(Tag.TileOffsets);
        ByteBuffer buffer;
        if (tileOffsetTag != null) {
            Index index = r.getIndex();
            int tileOffset;
            IFDEntry tileSizeTag = findTag(Tag.TileByteCounts);
            IFDEntry tileLengthTag = findTag(Tag.TileLength);
            IFDEntry tileWidthTag = findTag(Tag.TileWidth);
            int tileWidth = tileWidthTag.value[0];
            int tileHeight = tileLengthTag.value[0];
            int hTileNum = (width + tileWidth - 1) / tileWidth;
            int vTileNum = (height + tileHeight - 1) / tileHeight;
            int tileSize;
            //System.out.println("tileOffset =" + tileOffset + " tileSize=" + tileSize);
            int tileIdx, vIdx, hIdx;
            switch (bitsPerSample) {
                case 8:
                    for (int i = 0; i < vTileNum; i++) {
                        for (int j = 0; j < hTileNum; j++) {
                            tileIdx = i * hTileNum + j;
                            tileOffset = tileOffsetTag.value[tileIdx];
                            tileSize = tileSizeTag.value[tileIdx];
                            buffer = testReadData(tileOffset, tileSize);
                            for (int h = 0; h < tileHeight; h++) {
                                vIdx = i * tileHeight + h;
                                if (vIdx == height) {
                                    break;
                                }
                                for (int w = 0; w < tileWidth; w++) {
                                    hIdx = j * tileWidth + w;
                                    if (hIdx == width) {
                                        break;
                                    }
                                    index.set0(vIdx);
                                    index.set1(hIdx);
                                    if (samplesPerPixel == 1) {
                                        r.setInt(index, DataConvert.byte2Int(buffer.get()));
                                    } else {
                                        for (int k = 0; k < samplesPerPixel; k++) {
                                            index.set2(k);
                                            r.setInt(index, DataConvert.byte2Int(buffer.get()));
                                        }
                                    }
                                }
                            }
                        }
                    }
                    break;
                case 16:
                    for (int i = 0; i < vTileNum; i++) {
                        for (int j = 0; j < hTileNum; j++) {
                            tileIdx = i * hTileNum + j;
                            tileOffset = tileOffsetTag.value[tileIdx];
                            tileSize = tileSizeTag.value[tileIdx];
                            buffer = testReadData(tileOffset, tileSize);
                            for (int h = 0; h < tileHeight; h++) {
                                vIdx = i * tileHeight + h;
                                if (vIdx == height) {
                                    break;
                                }
                                for (int w = 0; w < tileWidth; w++) {
                                    hIdx = j * tileWidth + w;
                                    if (hIdx == width) {
                                        break;
                                    }
                                    index.set0(vIdx);
                                    index.set1(hIdx);
                                    if (samplesPerPixel == 1) {
                                        r.setInt(index, buffer.getShort());
                                    } else {
                                        for (int k = 0; k < samplesPerPixel; k++) {
                                            index.set2(k);
                                            r.setInt(index, buffer.getShort());
                                        }
                                    }
                                }
                            }
                        }
                    }
                    break;
                case 32:
                    int size = tileHeight * tileWidth * 4;
                    for (int i = 0; i < vTileNum; i++) {
                        for (int j = 0; j < hTileNum; j++) {
                            tileIdx = i * hTileNum + j;
                            tileOffset = tileOffsetTag.value[tileIdx];
                            tileSize = tileSizeTag.value[tileIdx];
                            if (tileSize == 0)
                                continue;

                            buffer = testReadData(tileOffset, tileSize);
                            if (cDecoder != null){
                                buffer = ByteBuffer.wrap(cDecoder.decode(buffer.array(), byteOrder));
                                if (buffer.limit() < size){
                                    ByteBuffer nbuffer = ByteBuffer.allocate(size);
                                    nbuffer.put(buffer.array());
                                    buffer = nbuffer;
                                    ((Buffer)buffer).position(0);
                                }
                                buffer.order(byteOrder);
                            }
                            for (int h = 0; h < tileHeight; h++) {
                                vIdx = i * tileHeight + h;
                                if (vIdx == height) {
                                    break;
                                }
                                index.set0(vIdx);
                                for (int w = 0; w < tileWidth; w++) {
                                    hIdx = j * tileWidth + w;
                                    if (hIdx == width) {
                                        buffer.get(new byte[(tileWidth - w) * 4]);
                                        break;
                                    }
                                    index.set1(hIdx);
                                    if (samplesPerPixel == 1) {
                                        if (dataType == DataType.FLOAT) {
                                            r.setFloat(index, buffer.getFloat());
                                        } else {
                                            r.setInt(index, buffer.getInt());
                                        }
                                    } else {
                                        for (int k = 0; k < samplesPerPixel; k++) {
                                            index.set2(k);
                                            if (dataType == DataType.FLOAT) {
                                                r.setFloat(index, buffer.getFloat());
                                            } else {
                                                r.setInt(index, buffer.getInt());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        } else {
            IFDEntry stripOffsetTag = findTag(Tag.StripOffsets);
            if (stripOffsetTag != null) {
                int stripNum = stripOffsetTag.count;
                int stripOffset;
                IFDEntry stripSizeTag = findTag(Tag.StripByteCounts);
                int stripSize = stripSizeTag.value[0];
                IFDEntry rowsPerStripTag = findTag(Tag.RowsPerStrip);
                int rowNum = rowsPerStripTag.value[0];
                //System.out.println("stripOffset =" + stripOffset + " stripSize=" + stripSize);
                int idx = 0;
                switch (bitsPerSample) {
                    case 8:
                        for (int i = 0; i < stripNum; i++) {
                            stripOffset = stripOffsetTag.value[i];
                            buffer = testReadData(stripOffset, stripSize);
                            for (int j = 0; j < width * rowNum; j++) {
                                for (int k = 0; k < samplesPerPixel; k++) {
                                    r.setInt(idx, DataConvert.byte2Int(buffer.get()));
                                    idx += 1;
                                }
                            }
                        }
                        break;
                    case 16:
                        for (int i = 0; i < stripNum; i++) {
                            stripOffset = stripOffsetTag.value[i];
                            buffer = testReadData(stripOffset, stripSize);
                            for (int j = 0; j < width * rowNum; j++) {
                                for (int k = 0; k < samplesPerPixel; k++) {
                                    if (dataType == DataType.FLOAT) {
                                        r.setFloat(idx, buffer.getShort());
                                    } else {
                                        r.setInt(idx, buffer.getShort());
                                    }
                                    idx += 1;
                                }
                            }
                        }
                        break;
                    case 32:
                        for (int i = 0; i < stripNum; i++) {
                            stripOffset = stripOffsetTag.value[i];
                            stripSize = stripSizeTag.value[i];
                            buffer = testReadData(stripOffset, stripSize);
                            if (cDecoder != null){
                                buffer = ByteBuffer.wrap(cDecoder.decode(buffer.array(), byteOrder));
                                buffer.order(byteOrder);
                            }
                            for (int j = 0; j < width * rowNum; j++) {
                                for (int k = 0; k < samplesPerPixel; k++) {
                                    r.setFloat(idx, buffer.getFloat());
                                    idx += 1;
                                }
                            }
                        }
                        break;
                }
            }
        }

        r = ArrayMath.flip(r, 0);
        return r;
    }

    /**
     * Test read data
     *
     * @param offset Offset
     * @param size Size
     * @throws IOException
     */
    private ByteBuffer testReadData(int offset, int size) throws IOException {
        this.channel.position(offset);
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.order(this.byteOrder);

        this.channel.read(buffer);
        ((Buffer)buffer).flip();

//        for (int i = 0; i < size / 4; i++) {
//            System.out.println(i + ": " + buffer.getFloat());
//        }
        return buffer;
    }

    /**
     * Read header
     *
     * @param channel File channel
     * @return Int
     * @throws IOException
     */
    private int readHeader(FileChannel channel) throws IOException {
        channel.position(0L);

        ByteBuffer buffer = ByteBuffer.allocate(8);
        channel.read(buffer);
        ((Buffer)buffer).flip();
        if (this.showHeaderBytes) {
            printBytes(System.out, "header", buffer, 4);
            buffer.rewind();
        }

        byte b = buffer.get();
        if (b == 73) {
            this.byteOrder = ByteOrder.LITTLE_ENDIAN;
        }
        buffer.order(this.byteOrder);
        ((Buffer)buffer).position(4);
        int firstIFD = buffer.getInt();
        if (this.debugRead) {
            System.out.println(" firstIFD == " + firstIFD);
        }

        return firstIFD;
    }

    /**
     * Read IFD
     *
     * @param channel File channel
     * @param start Start
     * @return Int
     * @throws IOException
     */
    private int readIFD(FileChannel channel, int start) throws IOException {
        channel.position(start);

        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.order(this.byteOrder);

        int n = channel.read(buffer);
        ((Buffer)buffer).flip();
        if (this.showBytes) {
            printBytes(System.out, "IFD", buffer, 2);
            buffer.rewind();
        }
        short nentries = buffer.getShort();
        if (this.debugRead) {
            System.out.println(" nentries = " + nentries);
        }

        start += 2;
        for (int i = 0; i < nentries; i++) {
            IFDEntry ifd = readIFDEntry(channel, start);
            if (this.debugRead) {
                System.out.println(i + " == " + ifd);
            }

            this.tags.add(ifd);
            start += 12;
        }

        if (this.debugRead) {
            System.out.println(" looking for nextIFD at pos == " + channel.position() + " start = " + start);
        }
        channel.position(start);
        buffer = ByteBuffer.allocate(4);
        buffer.order(this.byteOrder);
        n = channel.read(buffer);
        ((Buffer)buffer).flip();
        int nextIFD = buffer.getInt();
        if (this.debugRead) {
            System.out.println(" nextIFD == " + nextIFD);
        }
        return nextIFD;
    }

    /**
     * Read IFDEntry
     *
     * @param channel File channel
     * @param start Start
     * @return IFDEntry
     * @throws IOException
     */
    private IFDEntry readIFDEntry(FileChannel channel, int start) throws IOException {
        if (this.debugRead) {
            System.out.println("readIFDEntry starting position to " + start);
        }

        channel.position(start);
        ByteBuffer buffer = ByteBuffer.allocate(12);
        buffer.order(this.byteOrder);
        channel.read(buffer);
        ((Buffer)buffer).flip();
        if (this.showBytes) {
            printBytes(System.out, "IFDEntry bytes", buffer, 12);
        }

        ((Buffer)buffer).position(0);
        int code = readUShortValue(buffer);
        Tag tag = Tag.get(code);
        if (tag == null) {
            tag = new Tag(code);
        }
        FieldType type = FieldType.get(readUShortValue(buffer));
        int count = buffer.getInt();

        IFDEntry ifd = new IFDEntry(tag, type, count);

        if (ifd.count * ifd.type.size <= 4) {
            readValues(buffer, ifd);
        } else {
            int offset = buffer.getInt();
            if (this.debugRead) {
                System.out.println("position to " + offset);
            }
            channel.position(offset);
            ByteBuffer vbuffer = ByteBuffer.allocate(ifd.count * ifd.type.size);
            vbuffer.order(this.byteOrder);
            channel.read(vbuffer);
            ((Buffer)vbuffer).flip();
            readValues(vbuffer, ifd);
        }

        return ifd;
    }

    /**
     * Read values
     *
     * @param buffer ByteBuffer
     * @param ifd IFDEntry
     */
    private void readValues(ByteBuffer buffer, IFDEntry ifd) {
        if (ifd.type == FieldType.ASCII) {
            ifd.valueS = readSValue(buffer, ifd);
        } else if (ifd.type == FieldType.RATIONAL) {
            ifd.value = new int[ifd.count * 2];
            for (int i = 0; i < ifd.count * 2; i++) {
                ifd.value[i] = readIntValue(buffer, ifd);
            }
        } else if (ifd.type == FieldType.FLOAT) {
            ifd.valueD = new double[ifd.count];
            for (int i = 0; i < ifd.count; i++) {
                ifd.valueD[i] = buffer.getFloat();
            }
        } else if (ifd.type == FieldType.DOUBLE) {
            ifd.valueD = new double[ifd.count];
            for (int i = 0; i < ifd.count; i++) {
                ifd.valueD[i] = buffer.getDouble();
            }
        } else {
            ifd.value = new int[ifd.count];
            for (int i = 0; i < ifd.count; i++) {
                ifd.value[i] = readIntValue(buffer, ifd);
            }
        }
    }

    /**
     * Read int vlaue
     *
     * @param buffer ByteBuffer
     * @param ifd IFDEntry
     * @return Int value
     */
    private int readIntValue(ByteBuffer buffer, IFDEntry ifd) {
        switch (ifd.type.code) {
            case 1:
                return buffer.get();
            case 2:
                return buffer.get();
            case 3:
                return readUShortValue(buffer);
            case 4:
                return buffer.getInt();
            case 5:
                return buffer.getInt();
        }
        return 0;
    }

    /**
     * Read short vlaue
     *
     * @param buffer ByteBuffer
     * @return Short value
     */
    private int readUShortValue(ByteBuffer buffer) {
        return buffer.getShort() & 0xFFFF;
    }

    /**
     * Read string vlaue
     *
     * @param buffer ByteBuffer
     * @param ifd IFDEntry
     * @return String value
     */
    private String readSValue(ByteBuffer buffer, IFDEntry ifd) {
        byte[] dst = new byte[ifd.count];
        buffer.get(dst);
        return new String(dst);
    }

    /**
     * Print bytes
     *
     * @param ps PrintStream
     * @param head Head string
     * @param buffer ByteBuffer
     * @param n N
     */
    private void printBytes(PrintStream ps, String head, ByteBuffer buffer, int n) {
        ps.print(head + " == ");
        for (int i = 0; i < n; i++) {
            byte b = buffer.get();
            int ub = b < 0 ? b + 256 : b;
            ps.print(ub + "(");
            ps.write(b);
            ps.print(") ");
        }
        ps.println();
    }

    private void parseGeoInfo() {
        IFDEntry keyDir = findTag(Tag.GeoKeyDirectoryTag);
        IFDEntry dparms = findTag(Tag.GeoDoubleParamsTag);
        IFDEntry aparams = findTag(Tag.GeoAsciiParamsTag);

        if (null == keyDir) {
            return;
        }

        int nkeys = keyDir.value[3];
        if (this.debugReadGeoKey) {
            System.out.println("parseGeoInfo nkeys = " + nkeys + " keyDir= " + keyDir);
        }
        int pos = 4;

        for (int i = 0; i < nkeys; i++) {
            int id = keyDir.value[(pos++)];
            int location = keyDir.value[(pos++)];
            int vcount = keyDir.value[(pos++)];
            int offset = keyDir.value[(pos++)];

            GeoKey.Tag tag = GeoKey.Tag.getOrMake(id);

            GeoKey key = null;
            if (location == 0) {
                key = new GeoKey(id, offset);
            } else {
                IFDEntry data = findTag(Tag.get(location));
                if (data == null) {
                    System.out.println("********ERROR parseGeoInfo: cant find Tag code = " + location);
                } else if (data.tag == Tag.GeoDoubleParamsTag) {
                    double[] dvalue = new double[vcount];
                    for (int k = 0; k < vcount; k++) {
                        dvalue[k] = data.valueD[(offset + k)];
                    }
                    key = new GeoKey(tag, dvalue);
                } else if (data.tag == Tag.GeoKeyDirectoryTag) {
                    int[] value = new int[vcount];
                    for (int k = 0; k < vcount; k++) {
                        value[k] = data.value[(offset + k)];
                    }
                    key = new GeoKey(tag, value);
                } else if (data.tag == Tag.GeoAsciiParamsTag) {
                    String value = data.valueS.substring(offset, offset + vcount);
                    key = new GeoKey(tag, value);
                }

            }

            if (key != null) {
                keyDir.addGeoKey(key);
                if (this.debugReadGeoKey) {
                    System.out.println(" yyy  add geokey=" + key);
                }
            }
        }
    }

    /**
     * Show Info
     *
     * @param out PrintStream
     */
    public void showInfo(PrintStream out) {
        out.println("Geotiff file= " + this.filename);
        for (int i = 0; i < this.tags.size(); i++) {
            IFDEntry ifd = (IFDEntry) this.tags.get(i);
            out.println(i + " IFDEntry == " + ifd);
        }
    }

    /**
     * Show info
     *
     * @return Infostring
     */
    public String showInfo() {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(10000);
        showInfo(new PrintStream(bout));
        return bout.toString();
    }

    // </editor-fold>
}
