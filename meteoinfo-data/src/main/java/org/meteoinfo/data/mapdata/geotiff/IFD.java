package org.meteoinfo.data.mapdata.geotiff;

import org.meteoinfo.common.DataConvert;
import org.meteoinfo.data.mapdata.geotiff.compression.CompressionDecoder;
import org.meteoinfo.data.mapdata.geotiff.compression.DeflateCompression;
import org.meteoinfo.data.mapdata.geotiff.compression.LZWCompression;
import org.meteoinfo.ndarray.*;
import org.meteoinfo.ndarray.math.ArrayMath;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class IFD {
    private List<IFDEntry> tags = new ArrayList<>();
    private FileChannel channel;
    private ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;

    /**
     * Constructor
     * @param fc File channel
     */
    public IFD(FileChannel fc, ByteOrder byteOrder) {
        this.channel = fc;
        this.byteOrder = byteOrder;
    }

    /**
     * Get tags
     * @return Tags
     */
    public List<IFDEntry> getTags() {return this.tags;}

    /**
     * Set tags
     * @param value Tags
     */
    public void setTags(List<IFDEntry> value) {
        this.tags = value;
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
     * Add a tag
     * @param tag The tag
     */
    public void addTag(IFDEntry tag) {
        this.tags.add(tag);
    }

    /**
     * Delete tag
     *
     * @param ifd IFDEntry
     */
    public void deleteTag(IFDEntry ifd) {
        this.tags.remove(ifd);
    }

    /**
     * Get tag by index
     * @param i Index
     * @return Tag
     */
    public IFDEntry getTag(int i) {
        return this.tags.get(i);
    }

    /**
     * Get tag number
     * @return Tag number
     */
    public int getTagNum() {
        return this.tags.size();
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
            long tileOffset;
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
                            tileOffset = tileOffsetTag.valueL[tileIdx];
                            tileSize = tileSizeTag.value[tileIdx];
                            buffer = testReadData(tileOffset, tileSize, cDecoder);
                            for (int h = 0; h < tileHeight; h++) {
                                vIdx = i * tileHeight + h;
                                if (vIdx == height) {
                                    break;
                                }
                                for (int w = 0; w < tileWidth; w++) {
                                    hIdx = j * tileWidth + w;
                                    if (hIdx == width) {
                                        buffer.get(new byte[tileWidth - w]);
                                        break;
                                    }
                                    index.set0(vIdx);
                                    index.set1(hIdx);
                                    if (samplesPerPixel == 1) {
                                        if (sampleFormat == 2)
                                            r.setInt(index, buffer.get());
                                        else
                                            r.setInt(index, Byte.toUnsignedInt(buffer.get()));
                                    } else {
                                        for (int k = 0; k < samplesPerPixel; k++) {
                                            index.set2(k);
                                            if (sampleFormat == 2)
                                                r.setInt(index, buffer.get());
                                            else
                                                r.setInt(index, Byte.toUnsignedInt(buffer.get()));
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
                            tileOffset = tileOffsetTag.valueL[tileIdx];
                            tileSize = tileSizeTag.value[tileIdx];
                            buffer = testReadData(tileOffset, tileSize, cDecoder);
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
                            tileOffset = tileOffsetTag.valueL[tileIdx];
                            tileSize = tileSizeTag.value[tileIdx];
                            if (tileSize == 0)
                                continue;

                            buffer = testReadData(tileOffset, tileSize, cDecoder);
                            if (buffer.limit() < size){
                                ByteBuffer nbuffer = ByteBuffer.allocate(size);
                                nbuffer.put(buffer.array());
                                buffer = nbuffer;
                                ((Buffer)buffer).position(0);
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
                int stripNum = (int)stripOffsetTag.count;
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
                            stripSize = stripSizeTag.value[i];
                            buffer = testReadData(stripOffset, stripSize, cDecoder);
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
                            stripSize = stripSizeTag.value[i];
                            buffer = testReadData(stripOffset, stripSize, cDecoder);
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
                            buffer = testReadData(stripOffset, stripSize, cDecoder);
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
     * @param yRange Y range
     * @param xRange X range
     * @return Data
     * @throws IOException
     */
    public Array readArray(Range yRange, Range xRange) throws IOException, InvalidRangeException {
        IFDEntry widthIFD = this.findTag(Tag.ImageWidth);
        IFDEntry heightIFD = this.findTag(Tag.ImageLength);
        int width = widthIFD.value[0];
        int height = heightIFD.value[0];
        int nx = xRange.length();
        int ny = yRange.length();
        if (width == nx && height == ny) {
            return readArray();
        }

        IFDEntry samplesPerPixelTag = findTag(Tag.SamplesPerPixel);
        int samplesPerPixel = samplesPerPixelTag.value[0];    //Number of bands
        IFDEntry bitsPerSampleTag = findTag(Tag.BitsPerSample);
        int bitsPerSample = bitsPerSampleTag.value[0];
        int[] shape;
        if (samplesPerPixel == 1) {
            shape = new int[]{ny, nx};
        } else {
            shape = new int[]{ny, nx, samplesPerPixel};
        }
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
            long tileOffset;
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
                            tileOffset = tileOffsetTag.valueL[tileIdx];
                            tileSize = tileSizeTag.value[tileIdx];
                            buffer = testReadData(tileOffset, tileSize, cDecoder);
                            for (int h = 0; h < tileHeight; h++) {
                                vIdx = i * tileHeight + h;
                                if (vIdx == height) {
                                    break;
                                }
                                if (!yRange.contains(vIdx)) {
                                    buffer.position(buffer.position() + tileWidth * samplesPerPixel);
                                    continue;
                                }
                                vIdx = yRange.index(vIdx);
                                index.set0(vIdx);
                                for (int w = 0; w < tileWidth; w++) {
                                    hIdx = j * tileWidth + w;
                                    if (hIdx == width) {
                                        buffer.get(new byte[tileWidth - w]);
                                        break;
                                    }
                                    if (!xRange.contains(hIdx)) {
                                        buffer.position(buffer.position() + samplesPerPixel);
                                        continue;
                                    }
                                    hIdx = xRange.index(hIdx);
                                    index.set1(hIdx);
                                    if (samplesPerPixel == 1) {
                                        if (sampleFormat == 2)
                                            r.setInt(index, buffer.get());
                                        else
                                            r.setInt(index, Byte.toUnsignedInt(buffer.get()));
                                    } else {
                                        for (int k = 0; k < samplesPerPixel; k++) {
                                            index.set2(k);
                                            if (sampleFormat == 2)
                                                r.setInt(index, buffer.get());
                                            else
                                                r.setInt(index, Byte.toUnsignedInt(buffer.get()));
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
                            tileOffset = tileOffsetTag.valueL[tileIdx];
                            tileSize = tileSizeTag.value[tileIdx];
                            buffer = testReadData(tileOffset, tileSize, cDecoder);
                            for (int h = 0; h < tileHeight; h++) {
                                vIdx = i * tileHeight + h;
                                if (vIdx == height) {
                                    break;
                                }
                                if (!yRange.contains(vIdx)) {
                                    buffer.position(buffer.position() + tileWidth * samplesPerPixel * 2);
                                    continue;
                                }
                                vIdx = yRange.index(vIdx);
                                index.set0(vIdx);
                                for (int w = 0; w < tileWidth; w++) {
                                    hIdx = j * tileWidth + w;
                                    if (hIdx == width) {
                                        break;
                                    }
                                    if (!xRange.contains(hIdx)) {
                                        buffer.position(buffer.position() + samplesPerPixel * 2);
                                        continue;
                                    }
                                    hIdx = xRange.index(hIdx);
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
                            tileOffset = tileOffsetTag.valueL[tileIdx];
                            tileSize = tileSizeTag.value[tileIdx];
                            if (tileSize == 0)
                                continue;

                            buffer = testReadData(tileOffset, tileSize, cDecoder);
                            if (buffer.limit() < size){
                                ByteBuffer nbuffer = ByteBuffer.allocate(size);
                                nbuffer.put(buffer.array());
                                buffer = nbuffer;
                                ((Buffer)buffer).position(0);
                            }
                            for (int h = 0; h < tileHeight; h++) {
                                vIdx = i * tileHeight + h;
                                if (vIdx == height) {
                                    break;
                                }
                                if (!yRange.contains(vIdx)) {
                                    buffer.position(buffer.position() + tileWidth * samplesPerPixel * 4);
                                    continue;
                                }
                                vIdx = yRange.index(vIdx);
                                index.set0(vIdx);
                                for (int w = 0; w < tileWidth; w++) {
                                    hIdx = j * tileWidth + w;
                                    if (hIdx == width) {
                                        buffer.get(new byte[(tileWidth - w) * 4]);
                                        break;
                                    }
                                    if (!xRange.contains(hIdx)) {
                                        buffer.position(buffer.position() + samplesPerPixel * 40);
                                        continue;
                                    }
                                    hIdx = xRange.index(hIdx);
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
                IndexIterator iter = r.getIndexIterator();
                Index index = Index.factory(new int[]{height, width});
                int stripNum = (int)stripOffsetTag.count;
                int stripOffset;
                IFDEntry stripSizeTag = findTag(Tag.StripByteCounts);
                int stripSize = stripSizeTag.value[0];
                IFDEntry rowsPerStripTag = findTag(Tag.RowsPerStrip);
                int rowNum = rowsPerStripTag.value[0];
                //System.out.println("stripOffset =" + stripOffset + " stripSize=" + stripSize);
                int[] counter;
                switch (bitsPerSample) {
                    case 8:
                        for (int i = 0; i < stripNum; i++) {
                            stripOffset = stripOffsetTag.value[i];
                            stripSize = stripSizeTag.value[i];
                            buffer = testReadData(stripOffset, stripSize, cDecoder);
                            for (int j = 0; j < width * rowNum; j++) {
                                counter = index.getCurrentCounter();
                                if (yRange.contains(counter[0]) && xRange.contains(counter[1])) {
                                    for (int k = 0; k < samplesPerPixel; k++) {
                                        iter.setIntNext(DataConvert.byte2Int(buffer.get()));
                                    }
                                } else {
                                    buffer.position(buffer.position() + samplesPerPixel);
                                }
                                index.incr();
                            }
                        }
                        break;
                    case 16:
                        for (int i = 0; i < stripNum; i++) {
                            stripOffset = stripOffsetTag.value[i];
                            stripSize = stripSizeTag.value[i];
                            buffer = testReadData(stripOffset, stripSize, cDecoder);
                            for (int j = 0; j < width * rowNum; j++) {
                                counter = index.getCurrentCounter();
                                if (yRange.contains(counter[0]) && xRange.contains(counter[1])) {
                                    for (int k = 0; k < samplesPerPixel; k++) {
                                        if (dataType == DataType.FLOAT) {
                                            iter.setFloatNext(buffer.getShort());
                                        } else {
                                            iter.setIntNext(buffer.getShort());
                                        }
                                    }
                                } else {
                                    buffer.position(buffer.position() + samplesPerPixel * 2);
                                }
                                index.incr();
                            }
                        }
                        break;
                    case 32:
                        for (int i = 0; i < stripNum; i++) {
                            stripOffset = stripOffsetTag.value[i];
                            stripSize = stripSizeTag.value[i];
                            buffer = testReadData(stripOffset, stripSize, cDecoder);
                            for (int j = 0; j < width * rowNum; j++) {
                                counter = index.getCurrentCounter();
                                if (yRange.contains(counter[0]) && xRange.contains(counter[1])) {
                                    for (int k = 0; k < samplesPerPixel; k++) {
                                        iter.setFloatNext(buffer.getFloat());
                                    }
                                } else {
                                    buffer.position(buffer.position() + samplesPerPixel * 4);
                                }
                                index.incr();
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
     * @param cDecoder Compression decoder
     * @throws IOException
     */
    private ByteBuffer testReadData(long offset, int size, CompressionDecoder cDecoder) throws IOException {
        this.channel.position(offset);
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.order(this.byteOrder);

        this.channel.read(buffer);
        ((Buffer)buffer).flip();

        if (cDecoder != null){
            buffer = ByteBuffer.wrap(cDecoder.decode(buffer.array(), byteOrder));
            buffer.order(byteOrder);
        }

        return buffer;
    }
}
