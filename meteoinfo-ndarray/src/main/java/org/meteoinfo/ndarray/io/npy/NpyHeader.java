package org.meteoinfo.ndarray.io.npy;

import org.meteoinfo.ndarray.io.npy.dict.NpyHeaderDict;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.util.Objects;

/**
 * The NPY header contains the metadata of the stored array and the NPY file.
 */
public final class NpyHeader {

  private final long dataOffset;
  private final NpyHeaderDict dict;

  private NpyHeader(long dataOffset, NpyHeaderDict dict) {
    this.dataOffset = dataOffset;
    this.dict = Objects.requireNonNull(dict);
  }

  @Override
  public String toString() {
    return dict.toString();
  }

  /**
   * Returns the dictionary entries of this header.
   */
  public NpyHeaderDict dict() {
    return dict;
  }

  /**
   * Returns the 0-based position from where the data start in the NPY file.
   */
  public long dataOffset() {
    return dataOffset;
  }

  public ByteOrder byteOrder() {
    return dict.byteOrder() == null
      ? ByteOrder.nativeOrder()
      : dict.byteOrder().toJava();
  }

  public static NpyHeader read(InputStream in)
    throws IOException, NpyFormatException {

    // read the version
    byte[] bytes = new byte[8];
    int n = in.read(bytes);
    if (n != 8)
      throw new NpyFormatException("invalid NPY header");
    NpyVersion version = NpyVersion.of(bytes);

    // read the header length; 2 bytes for version 1; 4 bytes for versions > 1
    int headerLength;
    long dataOffset;
    if (version.major == 1) {
      bytes = new byte[2];
      n = in.read(bytes);
      if (n != 2)
        throw new NpyFormatException("invalid NPY header");
      headerLength = NpyUtil.u2ToInt(bytes, ByteOrder.LITTLE_ENDIAN);
      dataOffset = 10 + headerLength;
    } else {
      bytes = new byte[4];
      n = in.read(bytes);
      if (n != 4)
        throw new NpyFormatException("invalid NPY header");
      long len = NpyUtil.u4ToLong(bytes, ByteOrder.LITTLE_ENDIAN);
      dataOffset = 12 + len;
      headerLength = (int) len;
    }

    // read the header string
    bytes = new byte[headerLength];
    if (in.read(bytes) != headerLength)
      throw new NpyFormatException("invalid NPY file");
    String header = new String(bytes, version.headerEncoding());
    return new NpyHeader(dataOffset, NpyHeaderDict.parse(header));
  }

  public static NpyHeader read(ReadableByteChannel channel) throws IOException {

    // read the version
    ByteBuffer buffer = ByteBuffer.allocate(8)
      .order(ByteOrder.LITTLE_ENDIAN);
    if (channel.read(buffer) < 8) {
      throw new NpyFormatException("invalid NPY header");
    }
    buffer.flip();
    NpyVersion version = NpyVersion.of(buffer.array());

    int headerLength;
    long dataOffset;
    buffer.position(0);
    if (version.major == 1) {
      buffer.limit(2);
      if (channel.read(buffer) != 2)
        throw new NpyFormatException("invalid NPY header");
      buffer.flip();
      headerLength = NpyUtil.u2ToInt(buffer);
      dataOffset = 10 + headerLength;
    } else {
      buffer.limit(4);
      if (channel.read(buffer) != 4)
        throw new NpyFormatException("invalid NPY header");
      long len = NpyUtil.u4ToLong(buffer);
      dataOffset = 12 + len;
      headerLength = (int) len;
    }

    // read and parse the header
    buffer = ByteBuffer.allocate(headerLength);
    if (channel.read(buffer) != headerLength)
      throw new NpyFormatException("invalid NPY file");
    String header = new String(buffer.array(), version.headerEncoding());
    return new NpyHeader(dataOffset, NpyHeaderDict.parse(header));
  }

}
