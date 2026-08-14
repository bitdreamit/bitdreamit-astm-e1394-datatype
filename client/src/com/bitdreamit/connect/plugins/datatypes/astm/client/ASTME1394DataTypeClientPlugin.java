package com.bitdreamit.connect.plugins.datatypes.astm.client;

import com.bitdreamit.connect.plugins.datatypes.astm.shared.ASTME1394Constants;
import com.mirth.connect.client.ui.AbstractSettingsPanel;
import com.mirth.connect.plugins.ClientPlugin;

/**
 * Client-side plugin entry point for the ASTM E1394 data type.
 *
 * <p>Registered in {@code client/resources/plugin.xml} and instantiated by
 * the Mirth Connect Administrator client. Provides the settings panel that
 * lets administrators edit the default delimiters and parser options.</p>
 */
public class ASTME1394DataTypeClientPlugin extends ClientPlugin {

    @Override
    public String getPluginPointName() {
        return ASTME1394Constants.PLUGIN_NAME;
    }

    @Override
    public void start() {
        // No client-side initialization required.
    }

    @Override
    public void stop() {
        // No client-side cleanup required.
    }

    @Override
    public void reset() {
        // No client-side state to reset.
    }

    @Override
    public AbstractSettingsPanel getSettingsPanel() {
        return new ASTME1394DataTypeSettingsPanel("ASTM E1394 Settings");
    }

    @Override
    public String getSettingsPanelName() {
        return "ASTM E1394";
    }
}
