package com.bitdreamit.connect.plugins.datatypes.astm.server;

import com.mirth.connect.donkey.model.message.BatchRawMessage;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptor;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptorFactory;
import com.mirth.connect.model.datatype.SerializerProperties;

/**
 * Factory for {@link ASTME1394BatchAdaptor}. Instantiated once per channel
 * deployment by Mirth Connect's batch framework.
 *
 * <p>In Mirth Connect 4.x the {@link BatchAdaptorFactory} contract changed:
 * {@code createBatchAdaptor} now takes a {@link BatchRawMessage} instead of
 * the legacy {@code BatchMessageSource}. The {@link BatchRawMessage} wraps
 * both the message source and the partitioning metadata needed by the
 * newer batch pipeline, and is forwarded as-is to the
 * {@link ASTME1394BatchAdaptor} constructor.</p>
 */
public class ASTME1394BatchAdaptorFactory extends BatchAdaptorFactory {

    private final SerializerProperties properties;

    public ASTME1394BatchAdaptorFactory(SourceConnector sourceConnector, SerializerProperties properties) {
        super(sourceConnector);
        this.properties = properties;
    }

    @Override
    public BatchAdaptor createBatchAdaptor(BatchRawMessage batchRawMessage) {
        return new ASTME1394BatchAdaptor(this, sourceConnector, batchRawMessage, properties);
    }

    @Override
    public void onDeploy() throws DeployException {
        // No deployment-time initialization required.
    }

    @Override
    public void onUndeploy() throws UndeployException {
        // No undeploy-time cleanup required.
    }
}
