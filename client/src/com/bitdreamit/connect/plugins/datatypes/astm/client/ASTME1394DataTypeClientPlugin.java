package com.bitdreamit.connect.plugins.datatypes.astm.client;

import com.bitdreamit.connect.plugins.datatypes.astm.shared.ASTME1394Constants;
import com.bitdreamit.connect.plugins.datatypes.astm.shared.ASTME1394DataTypeDelegate;
import com.mirth.connect.model.attachments.AttachmentHandlerType;
import com.mirth.connect.model.datatype.DataTypeDelegate;
import com.mirth.connect.plugins.DataTypeClientPlugin;
import org.syntax.jedit.tokenmarker.TokenMarker;

/**
 * Client-side plugin entry point for the ASTM E1394 data type.
 *
 * <p>Mirrors the structure of {@code HL7v2DataTypeClientPlugin} from Mirth
 * Connect 4.5.x — implements exactly the abstract methods declared by
 * {@link DataTypeClientPlugin} with matching signatures (raw types where
 * the parent uses raw types, protected visibility where the parent
 * declares protected, etc.).</p>
 */
public class ASTME1394DataTypeClientPlugin extends DataTypeClientPlugin {

    public ASTME1394DataTypeClientPlugin(String name) {
        super(name);
    }

    @Override
    public String getPluginPointName() {
        return ASTME1394Constants.PLUGIN_NAME;
    }

    @Override
    public String getDisplayName() {
        return ASTME1394Constants.PLUGIN_NAME;
    }

    @Override
    public AttachmentHandlerType getDefaultAttachmentHandlerType() {
        return null;
    }

    @Override
    public TokenMarker getTokenMarker() {
        return null;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class getVocabulary() {
        return null;
    }

    @Override
    public String getTemplateString(byte[] content) {
        return "";
    }

    @Override
    public int getMinTreeLevel() {
        return 0;
    }

    @Override
    protected DataTypeDelegate getDataTypeDelegate() {
        return new ASTME1394DataTypeDelegate();
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void reset() {
    }
}
