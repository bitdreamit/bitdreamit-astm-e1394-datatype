package com.bitdreamit.connect.plugins.datatypes.astm.client;

import com.bitdreamit.connect.plugins.datatypes.astm.shared.ASTME1394Constants;
import com.bitdreamit.connect.plugins.datatypes.astm.shared.ASTME1394DataTypeDelegate;
import com.mirth.connect.model.datatype.DataTypeDelegate;
import com.mirth.connect.plugins.DataTypeCodeTemplatePlugin;

/**
 * Code-template plugin for the ASTM E1394 data type.
 *
 * <p>Mirrors {@code HL7v2DataTypeCodeTemplatePlugin} — provides the data-type
 * delegate and display name to Mirth's code-template framework so that
 * template variables like {@code ${message}} and {@code ${sourceMap}} are
 * typed correctly in the code-template editor.</p>
 *
 * <p>The parent {@link DataTypeCodeTemplatePlugin} declares a single
 * {@code DataTypeCodeTemplatePlugin(String name)} constructor — Mirth's
 * plugin loader passes the plugin point name from {@code plugin.xml} when
 * instantiating this class.</p>
 */
public class ASTME1394DataTypeCodeTemplatePlugin extends DataTypeCodeTemplatePlugin {

    private final DataTypeDelegate dataTypeDelegate = new ASTME1394DataTypeDelegate();

    /**
     * Construct the code-template plugin.
     *
     * @param name the plugin point name (supplied by the Mirth plugin loader
     *             from {@code plugin.xml})
     */
    public ASTME1394DataTypeCodeTemplatePlugin(String name) {
        super(name);
    }

    @Override
    protected DataTypeDelegate getDataTypeDelegate() {
        return dataTypeDelegate;
    }

    @Override
    protected String getDisplayName() {
        return ASTME1394Constants.PLUGIN_NAME;
    }
}
