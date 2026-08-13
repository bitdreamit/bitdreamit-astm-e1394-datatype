package com.bitdreamit.connect.plugins.datatypes.astm.server;

import com.mirth.connect.model.datatype.BatchAdaptor;
import com.mirth.connect.model.datatype.BatchAdaptorFactory;
import com.mirth.connect.model.datatype.DataTypeProperties;
import java.util.ArrayList;
import java.util.List;

public class ASTME1394BatchAdaptor implements BatchAdaptor {

    private ASTME1394BatchProperties props;

    public ASTME1394BatchAdaptor(ASTME1394BatchProperties props) {
        this.props = props;
    }

    @Override
    public List<String> getMessages(String source) throws Exception {
        List<String> messages = new ArrayList<>();
        if ("H_L_BOUNDARY".equals(props.getBatchSplitType())) {
            StringBuilder current = new StringBuilder();
            String[] lines = source.split("\r\n|\r|\n");
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                current.append(line).append("\r");
                if (line.startsWith("L|")) {
                    messages.add(current.toString());
                    current = new StringBuilder();
                }
            }
            if (current.length() > 0) {
                messages.add(current.toString());
            }
        } else {
            messages.add(source);
        }
        return messages;
    }
}
