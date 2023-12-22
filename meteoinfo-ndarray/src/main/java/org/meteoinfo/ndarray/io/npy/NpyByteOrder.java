package org.meteoinfo.ndarray.io.npy;

import java.nio.ByteOrder;

public enum NpyByteOrder {

  HARDWARE_NATIVE('='),

  LITTLE_ENDIAN('<'),

  BIG_ENDIAN('>'),

  NOT_APPLICABLE('|');

  private final char symbol;

  NpyByteOrder(char symbol) {
    this.symbol = symbol;
  }

  public char symbol() {
    return symbol;
  }

  /**
   * Tries to identify the byte order from a data type description. It tries to
   * identify it from the first character:
   * <ul>
   *   <li>{@code =} hardware native</li>
   *   <li>{@code <} little-endian</li>
   *   <li>{@code >} big-endian</li>
   *   <li>{@code |} not applicable</li>
   * </ul>
   *
   * @param description the data type description, eg. {@code <i4}
   * @return the detected byte order, or {@link #HARDWARE_NATIVE} if it could
   * not detect it
   */
  public static NpyByteOrder of(String description) {
    if (description == null)
      return HARDWARE_NATIVE;
    String s = description.trim();
    if (s.length() == 0)
      return HARDWARE_NATIVE;
    char c = s.charAt(0);
    for (NpyByteOrder v : values()) {
      if (c == v.symbol)
        return v;
    }
    return HARDWARE_NATIVE;
  }

  /**
   * Returns the corresponding Java {@link ByteOrder}.
   */
  public ByteOrder toJava() {
    switch (this) {
      case BIG_ENDIAN:
        return ByteOrder.BIG_ENDIAN;
      case LITTLE_ENDIAN:
        return ByteOrder.LITTLE_ENDIAN;
      default:
        return ByteOrder.nativeOrder();
    }
  }

  @Override
  public String toString() {
    return String.valueOf(symbol);
  }
}
