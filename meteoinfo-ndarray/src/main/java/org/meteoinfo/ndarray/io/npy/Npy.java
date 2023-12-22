package org.meteoinfo.ndarray.io.npy;

import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.io.npy.dict.NpyHeaderDict;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.function.BiConsumer;

public class Npy {

    /**
     * Load the content of the given file into NPY array instance.
     *
     * @param file the NPY file to read
     * @return the mapped NPY array
     * @throws NpyFormatException if the NPY format is invalid or unsupported
     * @throws RuntimeException   IO exceptions are wrapped in runtime exceptions
     */
    public static Array load(String fileName) {
        File file = new File(fileName);
        return load(file);
    }

    /**
     * Load the content of the given file into NPY array instance.
     *
     * @param file the NPY file to read
     * @return the mapped NPY array
     * @throws NpyFormatException if the NPY format is invalid or unsupported
     * @throws RuntimeException   IO exceptions are wrapped in runtime exceptions
     */
    public static NpyArray<?> read(File file) {
        try (RandomAccessFile f = new RandomAccessFile(file, "r");
             FileChannel channel = f.getChannel()) {
            NpyHeader header = NpyHeader.read(channel);
            return ChannelReader.read(channel, header);
        } catch (IOException e) {
            throw new RuntimeException("failed to read file: " + file, e);
        }
    }

    /**
     * Load the content of the given file into NPY array instance.
     *
     * @param file the NPY file to read
     * @return the mapped NPY array
     * @throws NpyFormatException if the NPY format is invalid or unsupported
     * @throws RuntimeException   IO exceptions are wrapped in runtime exceptions
     */
    public static Array load(File file) {
        try (RandomAccessFile f = new RandomAccessFile(file, "r");
             FileChannel channel = f.getChannel()) {
            NpyHeader header = NpyHeader.read(channel);
            return ArrayChannelReader.read(channel, header);
        } catch (IOException e) {
            throw new RuntimeException("failed to read file: " + file, e);
        }
    }

    /**
     * Load the content of the given file into NPY array instance.
     *
     * @param channel the NPY file to read
     * @return the mapped NPY array
     * @throws NpyFormatException if the NPY format is invalid or unsupported
     * @throws RuntimeException   IO exceptions are wrapped in runtime exceptions
     */
    public static Array load(ReadableByteChannel channel) {
        try {
            NpyHeader header = NpyHeader.read(channel);
            return ArrayChannelReader.read(channel, header);
        } catch (IOException e) {
            throw new RuntimeException("failed to read NPY array from channel", e);
        }
    }

    public static NpyArray<?> read(ReadableByteChannel channel) {
        try {
            NpyHeader header = NpyHeader.read(channel);
            return ChannelReader.read(channel, header);
        } catch (IOException e) {
            throw new RuntimeException("failed to read NPY array from channel", e);
        }
    }

    /**
     * Opens the given file as a random access file and reads the NPY header. It
     * calls the given consumer with the opened file and header and closes the
     * file when the consumer returns. This is useful when you want to do multiple
     * operations on an NPY file, e.g. read multiple columns.
     *
     * @param file the NPY file
     * @param fn   a consumer of the opened random access file and NPY header
     */
    public static void use(File file, BiConsumer<RandomAccessFile, NpyHeader> fn) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r");
             FileChannel channel = raf.getChannel()) {
            NpyHeader header = NpyHeader.read(channel);
            fn.accept(raf, header);
        } catch (IOException e) {
            throw new RuntimeException("failed to use NPY file: " + file, e);
        }
    }

    /**
     * Reads a range of {@code n} elements from an array in an NPY file. Say you
     * have an NPY file with an array {@code [1, 2, 3, 4]}, then
     * {@code readRange(file, 2, 1)} would read {@code 2} elements starting from
     * an offset of {@code 1} and thus would return {@code [2, 3]}.
     *
     * @param file   a NPY file
     * @param n      the number of elements that should be read from the file
     * @param offset the 0-based position of the first element of the range
     * @return a one-dimensional array with {@code n} elements
     */
    public static NpyArray<?> readRange(File file, int n, int offset) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r");
             FileChannel channel = raf.getChannel()) {
            NpyHeader header = NpyHeader.read(channel);
            return readRange(raf, header, n, offset);
        } catch (IOException e) {
            throw new RuntimeException(
                    "failed to read a range of " +
                            n + " elements from NPY file " + file, e);
        }
    }

    /**
     * Same as {@link #readRange(File, int, int)} but with an opened NPY file.
     *
     * @param file   the opened NPY file
     * @param header the NPY header of the file
     * @param n      the number of elements that should be read from the file
     * @param offset the 0-based position of the first element of the range
     * @return a one-dimensional array with {@code n} elements
     */
    public static NpyArray<?> readRange(
            RandomAccessFile file, NpyHeader header, int n, int offset) {

        NpyHeaderDict dict = header.dict();
        int elemSize = dict.dataType() == NpyDataType.U
                ? 4
                : Math.max(dict.dataType().size(), 1);

        try {

            // seek to the reading position and read the data
            long start = header.dataOffset();
            if (offset > 0) {
                start += (long) elemSize * (long) offset;
            }
            file.seek(start);
            int byteCount = n * elemSize;
            ByteBuffer buffer = ByteBuffer.allocate(byteCount)
                    .order(dict.byteOrder().toJava());
            if (file.getChannel().read(buffer) < byteCount) {
                throw new IndexOutOfBoundsException(
                        "failed to read " + n + " elements from file");
            }
            buffer.flip();

            // read the range into an array
            NpyHeaderDict rangeDict = shape1d(dict, n);
            NpyArrayReader reader = NpyArrayReader.of(rangeDict);
            reader.readAllFrom(buffer);
            return reader.finish();

        } catch (IOException e) {
            throw new RuntimeException(
                    "failed to read range from NPY file: " + file, e);
        }
    }

    public static NpyArray<?> readElements(File file, int n, int offset, int inc) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r");
             FileChannel channel = raf.getChannel()) {
            NpyHeader header = NpyHeader.read(channel);
            return readElements(raf, header, n, offset, inc);
        } catch (IOException e) {
            throw new RuntimeException(
                    "failed to read " + n + " elements from NPY file " + file, e);
        }
    }

    public static NpyArray<?> readElements(
            RandomAccessFile file, NpyHeader header, int n, int offset, int inc) {

        if (inc == 1)
            return readRange(file, header, n, offset);

        NpyHeaderDict dict = header.dict();
        int elemSize = dict.dataType() == NpyDataType.U
                ? 4
                : Math.max(dict.dataType().size(), 1);

        try {

            // read the data
            ByteBuffer buffer = ByteBuffer.allocate(n * elemSize)
                    .order(dict.byteOrder().toJava());
            long fileOffset = header.dataOffset();
            if (offset > 0) {
                fileOffset += (long) elemSize * (long) offset;
            }
            FileChannel channel = file.getChannel();
            for (int i = 0; i < n; i++) {
                file.seek(fileOffset + (long) i * inc * elemSize);
                buffer.limit(buffer.position() + elemSize);
                if (channel.read(buffer) < elemSize) {
                    throw new IndexOutOfBoundsException(
                            "failed to read " + n + " elements from file");
                }
            }
            buffer.flip();

            // read the range into an array
            NpyHeaderDict rangeDict = shape1d(dict, n);
            NpyArrayReader reader = NpyArrayReader.of(rangeDict);
            reader.readAllFrom(buffer);
            return reader.finish();
        } catch (IOException e) {
            throw new RuntimeException(
                    "failed to read elements from NPY file: " + file, e);
        }
    }

    static NpyHeaderDict shape1d(NpyHeaderDict dict, int n) {
        return NpyHeaderDict.of(dict.dataType())
                .withTypeSize(dict.typeSize())
                .withByteOrder(dict.byteOrder())
                .withFortranOrder(dict.hasFortranOrder())
                .withShape(new int[]{n})
                .create();
    }

    /**
     * Save array data to a npy file
     *
     * @param fileName The file path
     * @param array The data array
     */
    public static void save(String fileName, Array array) {
        File file = new File(fileName);
        save(file, array);
    }

    /**
     * Save array data to a npy file
     *
     * @param file The file
     * @param array The data array
     */
    public static void save(File file, Array array) {
        try (RandomAccessFile f = new RandomAccessFile(file, "rw");
             FileChannel channel = f.getChannel()) {
            save(channel, array);
        } catch (IOException e) {
            throw new RuntimeException("failed to write array to file " + file, e);
        }
    }

    /**
     * Save array data to a npy file
     *
     * @param channel The file channel
     * @param array The data array
     */
    public static void save(WritableByteChannel channel, Array array) {
        try {

            NpyDataType dataType = NpyUtil.toNpyDataType(array.getDataType());

            // write the header
            NpyHeaderDict dict = NpyHeaderDict.of(dataType)
                    .withShape(array.getShape())
                    .withFortranOrder(false)
                    .withByteOrder(NpyByteOrder.LITTLE_ENDIAN)
                    .create();
            channel.write(ByteBuffer.wrap(dict.toNpyHeader()));

            // allocate a buffer
            ByteBuffer buffer = array.getDataAsByteBuffer(ByteOrder.LITTLE_ENDIAN);
            channel.write(buffer);
        } catch (IOException e) {
            throw new RuntimeException("failed to write NPY array to channel", e);
        }
    }

    public static void save(OutputStream outputStream, Array array) {
        WritableByteChannel channel = Channels.newChannel(outputStream);
        save(channel, array);
    }

    public static void write(File file, NpyHeaderDict dict, byte[] data) {
        try (RandomAccessFile f = new RandomAccessFile(file, "rw");
             FileChannel channel = f.getChannel()) {
            write(channel, dict, data);
        } catch (IOException e) {
            throw new RuntimeException("failed to write npy data to file " + file, e);
        }
    }

    private static void write(
            WritableByteChannel channel, NpyHeaderDict dict, byte[] data) {
        try {
            byte[] header = dict.toNpyHeader();
            channel.write(ByteBuffer.wrap(header));
            channel.write(ByteBuffer.wrap(data));
        } catch (Exception e) {
            throw new RuntimeException("failed to write npy data", e);
        }
    }

    public static void write(File file, NpyArray<?> array) {
        try (RandomAccessFile f = new RandomAccessFile(file, "rw");
             FileChannel channel = f.getChannel()) {
            write(channel, array);
        } catch (IOException e) {
            throw new RuntimeException("failed to write array to file " + file, e);
        }
    }

    public static void write(WritableByteChannel channel, NpyArray<?> array) {
        try {

            NpyDataType dataType = array.dataType();

            // handle strings
            if (array.isCharArray()) {
                NpyCharArray charArray = array.asCharArray();
                NpyHeaderDict dict = NpyHeaderDict.of(dataType)
                        .withByteOrder(dataType == NpyDataType.S
                                ? NpyByteOrder.NOT_APPLICABLE
                                : NpyByteOrder.LITTLE_ENDIAN)
                        .withTypeSize(charArray.size())
                        .create();
                byte[] bytes = charArray.asByteArray().data();
                Npy.write(channel, dict, bytes);
                return;
            }

            // write the header
            NpyHeaderDict dict = NpyHeaderDict.of(dataType)
                    .withShape(array.shape())
                    .withFortranOrder(array.hasColumnOrder())
                    .withByteOrder(NpyByteOrder.LITTLE_ENDIAN)
                    .create();
            channel.write(ByteBuffer.wrap(dict.toNpyHeader()));

            // allocate a buffer
            long totalBytes = dict.dataSize();
            int maxBufferSize = 8 * 1024;
            int bufferSize = totalBytes < maxBufferSize
                    ? (int) totalBytes
                    : maxBufferSize;
            ByteBuffer buffer = ByteBuffer.allocate(bufferSize)
                    .order(ByteOrder.LITTLE_ENDIAN);

            // write data to the channel
            for (int i = 0; i < array.size(); i++) {
                array.writeElementTo(i, buffer);
                if (!buffer.hasRemaining()) {
                    buffer.flip();
                    channel.write(buffer);
                    buffer.clear();
                }
            }
            if (buffer.position() > 0) {
                buffer.flip();
                channel.write(buffer);
            }

        } catch (IOException e) {
            throw new RuntimeException("failed to write NPY array to channel", e);
        }
    }

    public static void write(OutputStream stream, NpyArray<?> array) {
        // do not close the channel here because it would
        // close the underlying output stream which is
        // not the idea of this function.
        WritableByteChannel channel = Channels.newChannel(stream);
        write(channel, array);
    }

    public static NpyArray<?> memmap(File file) {
        try (RandomAccessFile f = new RandomAccessFile(file, "r");
             FileChannel channel = f.getChannel()) {
            NpyHeader header = NpyHeader.read(channel);
            long dataSize = header.dict().dataSize();

            // only a buffer of size < Integer.MAX_VALUE can be mapped
            // into memory. if the size of the stored array is larger
            // we take the normal reader currently
            long max = Integer.MAX_VALUE;
            if (dataSize >= max)
                return ChannelReader.read(channel, header);

            java.nio.MappedByteBuffer buffer = channel.map(
                    FileChannel.MapMode.READ_ONLY, header.dataOffset(), dataSize);
            buffer.order(header.byteOrder());
            NpyArrayReader builder = NpyArrayReader.of(header.dict());
            builder.readAllFrom(buffer);
            return builder.finish();
        } catch (IOException e) {
            throw new RuntimeException("failed to memmap NPY file: " + file, e);
        }
    }

}
