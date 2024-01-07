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
}
