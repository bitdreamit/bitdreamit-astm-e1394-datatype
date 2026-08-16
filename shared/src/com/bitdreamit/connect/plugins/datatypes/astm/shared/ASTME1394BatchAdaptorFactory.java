package com.bitdreamit.connect.plugins.datatypes.astm.shared;

import com.mirth.connect.donkey.model.message.BatchRawMessage;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptor;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.server.message.DebuggableBatchAdaptorFactory;

/**
 * Factory for {@link ASTME1394BatchAdaptor}.
 *
 * <p>Mirrors {@code ER7BatchAdaptorFactory} from Mirth's HL7v2 plugin —
 * extends {@link DebuggableBatchAdaptorFactory} and calls the parent's
 * two-arg constructor {@code super(sourceConnector, properties)}.</p>
 */
public class ASTME1394BatchAdaptorFactory extends DebuggableBatchAdaptorFactory {

    private final SerializerProperties properties;

    public ASTME1394BatchAdaptorFactory(SourceConnector sourceConnector, SerializerProperties properties) {
        super(sourceConnector, properties);
        this.properties = properties;
    }

    @Override
    public BatchAdaptor createBatchAdaptor(BatchRawMessage batchRawMessage) {
        return new ASTME1394BatchAdaptor(this, sourceConnector, batchRawMessage, properties);
    }
}
