/*
 * BitDreamIT Mirth Lab Extensions
 * Copyright (c) 2026 Kimi AI (Moonshot AI) — MIT License
 */
package com.bitdreamit.mirth.labextensions.astme1394datatype;

import com.mirth.connect.client.ui.AbstractConnectorSettingsPanel;
import com.mirth.connect.client.ui.DataTypeClientPlugin;
import com.mirth.connect.model.datatype.DataTypeProperties;

public class AstmE1394DataTypeClientPlugin extends DataTypeClientPlugin {

    public AstmE1394DataTypeClientPlugin(String name) {
        super(name);
    }

    @Override
    public String getPluginPointName() { return "ASTM E1394"; }

    @Override
    public AbstractConnectorSettingsPanel getSerializationSettingsPanel() {
        return new AstmE1394SettingsPanel();
    }

    @Override
    public AbstractConnectorSettingsPanel getDeserializationSettingsPanel() {
        return new AstmE1394SettingsPanel();
    }

    @Override
    public AbstractConnectorSettingsPanel getBatchSettingsPanel() {
        return new AstmE1394SettingsPanel();
    }

    @Override
    public DataTypeProperties getDefaultProperties() {
        return new AstmE1394DataTypeProperties();
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void reset() {}
}