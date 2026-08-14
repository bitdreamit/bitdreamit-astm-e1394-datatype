package com.bitdreamit.connect.plugins.datatypes.astm.server;

import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptor;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptorFactory;
import com.mirth.connect.donkey.server.message.batch.BatchMessageSource;
import com.mirth.connect.model.datatype.SerializerProperties;

/**
 * Factory for {@link ASTME1394BatchAdaptor}. Instantiated once per channel
 * deployment by Mirth Connect's batch framework.
 */
public class ASTME1394BatchAdaptorFactory extends BatchAdaptorFactory {

    private final SerializerProperties properties;

    public ASTME1394BatchAdaptorFactory(SourceConnector sourceConnector, SerializerProperties properties) {
        super(sourceConnector);
        this.properties = properties;
    }

    @Override
    public BatchAdaptor createBatchAdaptor(BatchMessageSource batchMessageSource) {
        return new ASTME1394BatchAdaptor(sourceConnector, batchMessageSource, properties);
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
