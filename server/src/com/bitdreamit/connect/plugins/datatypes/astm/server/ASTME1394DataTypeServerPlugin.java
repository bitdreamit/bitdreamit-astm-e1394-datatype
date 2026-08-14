package com.bitdreamit.connect.plugins.datatypes.astm.server;

import java.io.InputStream;

import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.message.AutoResponder;
import com.mirth.connect.donkey.server.message.ResponseValidator;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptorFactory;
import com.mirth.connect.donkey.server.message.batch.BatchStreamReader;
import com.mirth.connect.model.datatype.DataTypeDelegate;
import com.mirth.connect.model.datatype.ResponseGenerationProperties;
import com.mirth.connect.model.datatype.ResponseValidationProperties;
import com.mirth.connect.model.datatype.SerializationProperties;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.model.transmission.TransmissionModeProperties;
import com.mirth.connect.plugins.DataTypeServerPlugin;

/**
 * Server-side plugin entry point for the ASTM E1394 data type.
 *
 * <p>Wires together the serializer delegate, the auto-responder, the response
 * validator, the batch adaptor factory, and the batch stream reader. Mirth
 * Connect's plugin loader instantiates this class via the {@code serverClass}
 * declaration in {@code server/resources/plugin.xml}.</p>
 */
public class ASTME1394DataTypeServerPlugin extends DataTypeServerPlugin {

    private final DataTypeDelegate dataTypeDelegate = new ASTME1394DataTypeDelegate();

    @Override
    public String getPluginPointName() {
        return dataTypeDelegate.getName();
    }

    @Override
    public void start() {
        // No background threads or external resources to initialize.
    }

    @Override
    public void stop() {
        // No background threads or external resources to tear down.
    }

    @Override
    protected DataTypeDelegate getDataTypeDelegate() {
        return dataTypeDelegate;
    }

    @Override
    public AutoResponder getAutoResponder(SerializationProperties serializationProperties,
                                          ResponseGenerationProperties responseGenerationProperties) {
        return new ASTME1394AutoResponder(serializationProperties, responseGenerationProperties);
    }

    @Override
    public ResponseValidator getResponseValidator(SerializationProperties serializationProperties,
                                                  ResponseValidationProperties responseValidationProperties) {
        return new ASTME1394ResponseValidator(serializationProperties, responseValidationProperties);
    }

    @Override
    public BatchAdaptorFactory getBatchAdaptorFactory(SourceConnector sourceConnector,
                                                       SerializerProperties properties) {
        return new ASTME1394BatchAdaptorFactory(sourceConnector, properties);
    }

    @Override
    public BatchStreamReader getBatchStreamReader(InputStream inputStream, TransmissionModeProperties properties) {
        return new ASTME1394BatchStreamReader(inputStream);
    }
}
