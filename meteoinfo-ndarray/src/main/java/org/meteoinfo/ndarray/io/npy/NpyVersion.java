package org.meteoinfo.ndarray.io.npy;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Contains the version information of the first bytes of an NPY file:
 * <ul>
 *   <li>the first 6 bytes are the magic string '\x93'</li>
 *   <li>bytes 7 and 8 contain the major and minor version</li>
 * </ul>
 */
class NpyVersion {

  final int major;
  final int minor;

  private NpyVersion(int major, int minor) {
    this.major = major;
    this.minor = minor;

  }

  /**
   * Reads the format version from the first 8 bytes of an NPY file. It checks
   * that the array starts with the magic string {@code '\x93NUMPY'} and that
   * the version is in a supported range. If this is not the case, it throws
   * an {@code UnsupportedFormatException}.
   *
   * @param bytes at least, the first 8 bytes of an NPY file
   * @return the NPY version of that file
   */
  static NpyVersion of(byte[] bytes) throws NpyFormatException {
    if (bytes.length < 8)
      throw new NpyFormatException("invalid NPY header");
    if (NpyUtil.u1ToShort(bytes[0]) != 0x93)
      throw new NpyFormatException("invalid NPY header");
    String numpy = new String(bytes, 1, 5);
    if (!numpy.equals("NUMPY"))
      throw new NpyFormatException("invalid NPY header");

    int major = NpyUtil.u1ToShort(bytes[6]);
    int minor = NpyUtil.u1ToShort(bytes[7]);
    if (major != 1 && major != 2 && major != 3)
      throw new NpyFormatException(
        "unsupported NPY version: " + major);
    return new NpyVersion(major, minor);
  }

  Charset headerEncoding() {
    return major >= 3
      ? StandardCharsets.UTF_8
      : StandardCharsets.US_ASCII;
  }
}
