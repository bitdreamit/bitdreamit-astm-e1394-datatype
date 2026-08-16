package com.bitdreamit.connect.plugins.datatypes.astm.shared;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.mirth.connect.donkey.server.message.batch.BatchStreamReader;

/**
 * ASTM E1394 batch stream reader.
 *
 * <p>Reads bytes from a {@code SourceConnector}'s input stream and detects
 * intermediate message boundaries. For ASTM E1394 the record delimiter is
 * CR (0x0D); the boundary therefore fires whenever a CR or LF is observed
 * while the buffer already contains a complete {@code H…L} session.</p>
 *
 * <p>This implementation defers final-boundary detection to the upstream
 * {@code BatchAdaptor} (which splits on {@code H…L} boundaries); the
 * intermediate-message check here only handles the simple line-terminator
 * case for compatibility with the Mirth Connect batch framework.</p>
 */
public class ASTME1394BatchStreamReader extends BatchStreamReader {

    public ASTME1394BatchStreamReader(InputStream inputStream) {
        super(inputStream);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public byte[] checkForIntermediateMessage(ByteArrayOutputStream capturedBytes, List endBytesBuffer, int lastByte) throws IOException {
        if (lastByte == '\r' || lastByte == '\n') {
            if (endBytesBuffer != null) {
                for (Object b : endBytesBuffer) {
                    capturedBytes.write(((Byte) b).byteValue());
                }
            }
            return capturedBytes.toByteArray();
        }
        return null;
    }
}
