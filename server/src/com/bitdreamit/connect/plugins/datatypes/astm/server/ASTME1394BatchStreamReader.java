package com.bitdreamit.mirth.astm.e1394.server;

import com.mirth.connect.donkey.server.message.batch.BatchStreamReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ASTME1394BatchStreamReader extends BatchStreamReader {
    public ASTME1394BatchStreamReader(InputStream inputStream) { super(inputStream); }
    @Override
    public byte[] checkForIntermediateMessage(ByteArrayOutputStream capturedBytes, List endBytesBuffer, int lastByte) throws IOException {
        if (lastByte == '\r' || lastByte == '\n') {
            if (endBytesBuffer != null) {
                for (Object b : endBytesBuffer) capturedBytes.write((Byte) b);
            }
            return capturedBytes.toByteArray();
        }
        return null;
    }
}
