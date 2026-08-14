package com.bitdreamit.mirth.astm.e1394.client;

import com.bitdreamit.mirth.astm.e1394.shared.ASTME1394Constants;
import com.mirth.connect.client.ui.AbstractSettingsPanel;
import com.mirth.connect.plugins.ClientPlugin;

public class ASTME1394DataTypeClientPlugin extends ClientPlugin {
    @Override public String getPluginPointName() { return ASTME1394Constants.PLUGIN_NAME; }
    @Override public void start() {}
    @Override public void stop() {}
    @Override public void reset() {}
    @Override public AbstractSettingsPanel getSettingsPanel() { return new ASTME1394DataTypeSettingsPanel("ASTM E1394 Settings"); }
    @Override public String getSettingsPanelName() { return "ASTM E1394"; }
}
