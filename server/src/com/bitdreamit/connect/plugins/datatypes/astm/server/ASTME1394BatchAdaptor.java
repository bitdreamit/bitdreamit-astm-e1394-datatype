package com.bitdreamit.mirth.astm.e1394.server;

import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptor;
import com.mirth.connect.donkey.server.message.batch.BatchMessageException;
import com.mirth.connect.donkey.server.message.batch.BatchMessageSource;
import com.mirth.connect.model.datatype.SerializerProperties;

public class ASTME1394BatchAdaptor extends BatchAdaptor {
    private SerializerProperties properties;
    public ASTME1394BatchAdaptor(SourceConnector sourceConnector, BatchMessageSource batchMessageSource, SerializerProperties properties) {
        super(sourceConnector, batchMessageSource);
        this.properties = properties;
    }
    @Override public String getMessage() throws BatchMessageException {
        try {
            byte[] bytes = batchMessageSource.getNextMessage();
            if (bytes == null) return null;
            return new String(bytes, "UTF-8");
        } catch (Exception e) { throw new BatchMessageException("Failed to read ASTM batch message", e); }
    }
    @Override public void cleanup() {}
}
