package com.bitdreamit.connect.plugins.datatypes.astm.server;

import com.mirth.connect.model.datatype.BatchAdaptor;
import com.mirth.connect.model.datatype.BatchAdaptorFactory;
import com.mirth.connect.model.datatype.DataTypeProperties;

public class ASTME1394BatchAdaptorFactory implements BatchAdaptorFactory {

    @Override
    public BatchAdaptor getBatchAdaptor(DataTypeProperties dataTypeProperties) {
        return new ASTME1394BatchAdaptor((ASTME1394BatchProperties) dataTypeProperties.getBatchProperties());
    }
}
