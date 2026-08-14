package com.bitdreamit.mirth.astm.e1394.server;

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

public class ASTME1394DataTypeServerPlugin extends DataTypeServerPlugin {
    private DataTypeDelegate dataTypeDelegate = new ASTME1394DataTypeDelegate();

    @Override public String getPluginPointName() { return dataTypeDelegate.getName(); }
    @Override public void start() {}
    @Override public void stop() {}
    @Override protected DataTypeDelegate getDataTypeDelegate() { return dataTypeDelegate; }

    @Override public AutoResponder getAutoResponder(SerializationProperties sp, ResponseGenerationProperties gp) {
        return new ASTME1394AutoResponder(sp, gp);
    }
    @Override public ResponseValidator getResponseValidator(SerializationProperties sp, ResponseValidationProperties vp) {
        return new ASTME1394ResponseValidator(sp, vp);
    }
    @Override public BatchAdaptorFactory getBatchAdaptorFactory(SourceConnector sc, SerializerProperties p) {
        return new ASTME1394BatchAdaptorFactory(sc, p);
    }
    @Override public BatchStreamReader getBatchStreamReader(InputStream is, TransmissionModeProperties p) {
        return new ASTME1394BatchStreamReader(is);
    }
}
