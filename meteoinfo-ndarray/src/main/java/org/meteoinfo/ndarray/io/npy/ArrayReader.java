package org.meteoinfo.ndarray.io.npy;

import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.io.npy.dict.NpyHeaderDict;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

abstract class ArrayReader {

  protected final NpyHeaderDict dict;
  protected final int elementCount;
  private final int elementSize;
  private int pos;

  private ArrayReader(NpyHeaderDict dict) {
    this.dict = dict;
    NpyDataType type = dict.dataType();
    this.elementCount = type == NpyDataType.S || type == NpyDataType.U
      ? dict.typeSize()
      : dict.numberOfElements();
    this.elementSize = type == NpyDataType.S ? 1
      : type == NpyDataType.U ? 4 : type.size();
    this.pos = 0;
  }

  static ArrayReader of(NpyHeaderDict dict) throws NpyFormatException {
    switch (dict.dataType()) {
      case bool:
        return new BooleanBuilder(dict);
      case f2:
        return new FloatBuilder(dict, NpyUtil::f2ToFloat);
      case f4:
        return new FloatBuilder(dict, ByteBuffer::getFloat);
      case f8:
        return new DoubleBuilder(dict);
      case i1:
        return new ByteBuilder(dict);
      case i2:
        return new ShortBuilder(dict, ByteBuffer::getShort);
      case i4:
        return new IntBuilder(dict, ByteBuffer::getInt);
      case i8:
        return new LongBuilder(dict, ByteBuffer::getLong);
      case u1:
        return new ShortBuilder(dict, NpyUtil::u1ToShort);
      case u2:
        return new IntBuilder(dict, NpyUtil::u2ToInt);
      case u4:
        return new LongBuilder(dict, NpyUtil::u4ToLong);
      case u8:
        return new BigIntBuilder(dict);
      case S:
        return new AsciiBuilder(dict);
      case U:
        return new UnicodeBuilder(dict);
      default:
        throw new NpyFormatException(
          "unsupported data type: " + dict.dataType());
    }
  }

  final void readAllFrom(ByteBuffer buffer) {
    while (pos != elementCount && buffer.remaining() >= elementSize) {
      nextInto(buffer, pos);
      pos++;
    }
  }

  final void readNextFrom(ByteBuffer buffer) {
    nextInto(buffer, pos);
    pos++;
  }

  abstract void nextInto(ByteBuffer buffer, int pos);

  abstract Object getData();

  Array finish() {
    return Array.factory(NpyUtil.toMIDataType(dict.dataType()), dict.shape(), getData());
  }

  private static final class BooleanBuilder extends ArrayReader {

    private final boolean[] data;

    private BooleanBuilder(NpyHeaderDict dict) {
      super(dict);
      this.data = new boolean[elementCount];
    }

    @Override
    void nextInto(ByteBuffer buffer, int pos) {
      data[pos] = buffer.get() != 0;
    }

    @Override
    Object getData() {
      return data;
    }
  }

  private static final class ByteBuilder extends ArrayReader {

    private final byte[] data;

    private ByteBuilder(NpyHeaderDict dict) {
      super(dict);
      this.data = new byte[elementCount];
    }

    @Override
    void nextInto(ByteBuffer buffer, int pos) {
      data[pos] = buffer.get();
    }

    @Override
    Object getData() {
      return data;
    }
  }

  private static final class DoubleBuilder extends ArrayReader {

    private final double[] data;

    private DoubleBuilder(NpyHeaderDict dict) {
      super(dict);
      this.data = new double[elementCount];
    }

    @Override
    void nextInto(ByteBuffer buffer, int pos) {
      data[pos] = buffer.getDouble();
    }

    @Override
    Object getData() {
      return data;
    }
  }

  private static final class FloatBuilder extends ArrayReader {

    private final float[] data;
    private final ToFloatFunction<ByteBuffer> fn;

    private FloatBuilder(NpyHeaderDict dict, ToFloatFunction<ByteBuffer> fn) {
      super(dict);
      this.data = new float[elementCount];
      this.fn = fn;
    }

    @Override
    void nextInto(ByteBuffer buffer, int pos) {
      data[pos] = fn.applyAsFloat(buffer);
    }

    @Override
    Object getData() {
      return data;
    }
  }

  private static final class IntBuilder extends ArrayReader {

    private final int[] data;
    private final ToIntFunction<ByteBuffer> fn;

    private IntBuilder(NpyHeaderDict dict, ToIntFunction<ByteBuffer> fn) {
      super(dict);
      this.data = new int[elementCount];
      this.fn = fn;
    }

    @Override
    void nextInto(ByteBuffer buffer, int pos) {
      data[pos] = fn.applyAsInt(buffer);
    }

    @Override
    Object getData() {
      return data;
    }
  }

  private static final class ShortBuilder extends ArrayReader {

    private final short[] data;
    private final ToShortFunction<ByteBuffer> fn;

    private ShortBuilder(NpyHeaderDict dict, ToShortFunction<ByteBuffer> fn) {
      super(dict);
      this.data = new short[elementCount];
      this.fn = fn;
    }

    @Override
    void nextInto(ByteBuffer buffer, int pos) {
      data[pos] = fn.applyAsShort(buffer);
    }

    @Override
    Object getData() {
      return data;
    }
  }

  private static final class LongBuilder extends ArrayReader {

    private final long[] data;
    private final ToLongFunction<ByteBuffer> fn;

    private LongBuilder(NpyHeaderDict dict, ToLongFunction<ByteBuffer> fn) {
      super(dict);
      this.data = new long[elementCount];
      this.fn = fn;
    }

    @Override
    void nextInto(ByteBuffer buffer, int pos) {
      data[pos] = fn.applyAsLong(buffer);
    }

    @Override
    Object getData() {
      return data;
    }
  }

  private static final class BigIntBuilder extends ArrayReader {

    private final BigInteger[] data;

    private BigIntBuilder(NpyHeaderDict dict) {
      super(dict);
      this.data = new BigInteger[elementCount];
    }

    @Override
    void nextInto(ByteBuffer buffer, int pos) {
      data[pos] = NpyUtil.u8ToBigInteger(buffer);
    }

    @Override
    Object getData() {
      return data;
    }
  }

  private static final class AsciiBuilder extends ArrayReader {

    private final CharBuffer chars;
    private char[] data;
    private boolean terminated = false;

    private AsciiBuilder(NpyHeaderDict dict) {
      super(dict);
      this.chars = CharBuffer.allocate(elementCount);
    }

    @Override
    void nextInto(ByteBuffer buffer, int pos) {
      if (terminated)
        return;
      char next = (char)buffer.get();
      if (next == 0) {
        terminated = true;
        return;
      }
      chars.put(next);
    }

    @Override
    Object getData() {
      if (chars.remaining() == 0) {
        data = chars.array();
      } else {
        chars.flip();
        data = new char[chars.limit()];
        chars.get(data, 0, chars.limit());
      }

      return data;
    }
  }

  private static final class UnicodeBuilder extends ArrayReader {

    private final int[] data;

    private UnicodeBuilder(NpyHeaderDict dict) {
      super(dict);
      this.data = new int[elementCount];
    }

    @Override
    void nextInto(ByteBuffer buffer, int pos) {
      data[pos] = buffer.getInt();
    }

    @Override
    Object getData() {
      return data;
    }
  }
}
