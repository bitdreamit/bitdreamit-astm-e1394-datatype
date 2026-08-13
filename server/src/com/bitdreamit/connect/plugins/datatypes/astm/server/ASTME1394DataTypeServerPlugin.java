package com.bitdreamit.connect.plugins.datatypes.astm.server;

import com.bitdreamit.connect.plugins.datatypes.astm.shared.ASTME1394Constants;
import com.mirth.connect.model.ExtensionPermission;
import com.mirth.connect.plugins.DataTypeServerPlugin;
import com.mirth.connect.model.datatype.DataTypeProperties;
import com.mirth.connect.model.datatype.SerializationProperties;
import com.mirth.connect.model.datatype.DeserializationProperties;
import com.mirth.connect.model.datatype.BatchProperties;
import com.mirth.connect.model.datatype.BatchAdaptorFactory;
import com.mirth.connect.model.converters.IMessageSerializer;
import java.util.Map;
import java.util.Properties;

public class ASTME1394DataTypeServerPlugin extends DataTypeServerPlugin {

    @Override
    public void init(Properties properties) {}

    @Override
    public void update(Properties properties) {}

    @Override
    public Properties getDefaultProperties() {
        return new Properties();
    }

    @Override
    public ExtensionPermission[] getExtensionPermissions() {
        return new ExtensionPermission[0];
    }

    @Override
    public Map<String, Object> getObjectsForSwaggerExamples() {
        return null;
    }

    @Override
    public String getPluginPointName() {
        return ASTME1394Constants.PLUGIN_POINT_NAME;
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public IMessageSerializer getSerializer(SerializationProperties serializationProperties,
                                             DeserializationProperties deserializationProperties) {
        return new ASTME1394MessageSerializer(serializationProperties, deserializationProperties);
    }

    @Override
    public BatchAdaptorFactory getBatchAdaptorFactory() {
        return new ASTME1394BatchAdaptorFactory();
    }

    @Override
    public BatchProperties getDefaultBatchProperties() {
        return new ASTME1394BatchProperties();
    }

    @Override
    public DataTypeProperties getDefaultDataTypeProperties() {
        return new ASTME1394DataTypeProperties();
    }
}
