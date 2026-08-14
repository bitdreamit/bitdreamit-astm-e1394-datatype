package com.bitdreamit.mirth.astm.e1394.server;

import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptor;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptorFactory;
import com.mirth.connect.donkey.server.message.batch.BatchMessageSource;
import com.mirth.connect.model.datatype.SerializerProperties;

public class ASTME1394BatchAdaptorFactory extends BatchAdaptorFactory {
    private SerializerProperties properties;
    public ASTME1394BatchAdaptorFactory(SourceConnector sourceConnector, SerializerProperties properties) {
        super(sourceConnector);
        this.properties = properties;
    }
    @Override public BatchAdaptor createBatchAdaptor(BatchMessageSource batchMessageSource) {
        return new ASTME1394BatchAdaptor(sourceConnector, batchMessageSource, properties);
    }
    @Override public void onDeploy() throws DeployException {}
    @Override public void onUndeploy() throws UndeployException {}
}
