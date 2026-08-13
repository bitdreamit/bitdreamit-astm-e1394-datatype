package com.bitdreamit.connect.plugins.datatypes.astm.client;

import com.bitdreamit.connect.plugins.datatypes.astm.shared.ASTME1394Constants;
import com.mirth.connect.client.ui.AbstractSettingsPanel;
import com.mirth.connect.plugins.DataTypeClientPlugin;

public class ASTME1394DataTypeClientPlugin extends DataTypeClientPlugin {

    public ASTME1394DataTypeClientPlugin(String name) {
        super(name);
    }

    @Override
    public AbstractSettingsPanel getSettingsPanel() {
        return new ASTME1394DataTypeSettingsPanel("ASTM E1394");
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
    public void reset() {}
}
