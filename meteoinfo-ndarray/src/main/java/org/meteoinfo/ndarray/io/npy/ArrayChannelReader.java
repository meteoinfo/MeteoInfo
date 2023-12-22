package org.meteoinfo.ndarray.io.npy;

import org.meteoinfo.ndarray.Array;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;


class ArrayChannelReader {

    private static final int MAX_BUFFER_SIZE = 8 * 1024;

    private final ReadableByteChannel channel;
    private final NpyHeader header;

    private ArrayChannelReader(ReadableByteChannel channel, NpyHeader header) {
        this.channel = channel;
        this.header = header;
    }

    static Array read(ReadableByteChannel channel, NpyHeader header)
            throws IOException, NpyFormatException {
        return new ArrayChannelReader(channel, header).read();
    }

    private Array read() throws IOException, NpyFormatException {
        long totalBytes = header.dict().dataSize();
        int bufferSize = totalBytes > 0 && totalBytes < ((long) MAX_BUFFER_SIZE)
                ? (int) totalBytes
                : MAX_BUFFER_SIZE;

        ArrayReader builder = ArrayReader.of(header.dict());

        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        buffer.order(header.byteOrder());
        long readBytes = 0;
        while (readBytes < totalBytes) {
            int n = channel.read(buffer);
            if (n <= 0)
                break;
            buffer.flip();
            builder.readAllFrom(buffer);
            buffer.clear();
            readBytes += n;
        }
        return builder.finish();
    }
}
