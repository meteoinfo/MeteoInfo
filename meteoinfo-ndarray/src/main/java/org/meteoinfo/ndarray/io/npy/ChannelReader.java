package org.meteoinfo.ndarray.io.npy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;


class ChannelReader {

    private static final int MAX_BUFFER_SIZE = 8 * 1024;

    private final ReadableByteChannel channel;
    private final NpyHeader header;

    private ChannelReader(ReadableByteChannel channel, NpyHeader header) {
        this.channel = channel;
        this.header = header;
    }

    static NpyArray<?> read(ReadableByteChannel channel, NpyHeader header)
            throws IOException, NpyFormatException {
        return new ChannelReader(channel, header).read();
    }

    private NpyArray<?> read() throws IOException, NpyFormatException {
        long totalBytes = header.dict().dataSize();
        int bufferSize = totalBytes > 0 && totalBytes < ((long) MAX_BUFFER_SIZE)
                ? (int) totalBytes
                : MAX_BUFFER_SIZE;

        NpyArrayReader builder = NpyArrayReader.of(header.dict());

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
