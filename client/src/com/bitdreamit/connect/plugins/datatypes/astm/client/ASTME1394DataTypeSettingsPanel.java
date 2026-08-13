package com.bitdreamit.connect.plugins.datatypes.astm.client;

import com.mirth.connect.client.ui.AbstractSettingsPanel;
import com.mirth.connect.client.ui.components.MirthTextField;
import com.mirth.connect.client.ui.components.MirthCheckBox;
import com.mirth.connect.model.datatype.DataTypeProperties;
import com.bitdreamit.connect.plugins.datatypes.astm.server.*;
import javax.swing.*;
import java.awt.*;

public class ASTME1394DataTypeSettingsPanel extends AbstractSettingsPanel {

    private MirthTextField fieldDelimiterField;
    private MirthTextField repeatDelimiterField;
    private MirthTextField componentDelimiterField;
    private MirthTextField escapeCharacterField;
    private MirthCheckBox deriveFromHeaderCheckBox;
    private MirthCheckBox strictValidationCheckBox;

    public ASTME1394DataTypeSettingsPanel(String tabName) {
        super(tabName);
        initComponents();
        initLayout();
    }

    private void initComponents() {
        fieldDelimiterField = new MirthTextField();
        fieldDelimiterField.setToolTipText("Field delimiter (default: |)");
        repeatDelimiterField = new MirthTextField();
        repeatDelimiterField.setToolTipText("Repeat delimiter (default: \\)");
        componentDelimiterField = new MirthTextField();
        componentDelimiterField.setToolTipText("Component delimiter (default: ^)");
        escapeCharacterField = new MirthTextField();
        escapeCharacterField.setToolTipText("Escape character (default: &)");
        deriveFromHeaderCheckBox = new MirthCheckBox("Derive delimiters from Header record (H|field2)");
        deriveFromHeaderCheckBox.setToolTipText("Read delimiter definitions from the message Header record at runtime");
        strictValidationCheckBox = new MirthCheckBox("Use strict validation");
        strictValidationCheckBox.setToolTipText("Fail on malformed records; unchecked = log and continue");
    }

    private void initLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Field Delimiter:"), gbc);
        gbc.gridx = 1;
        add(fieldDelimiterField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Repeat Delimiter:"), gbc);
        gbc.gridx = 1;
        add(repeatDelimiterField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Component Delimiter:"), gbc);
        gbc.gridx = 1;
        add(componentDelimiterField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        add(new JLabel("Escape Character:"), gbc);
        gbc.gridx = 1;
        add(escapeCharacterField, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        add(deriveFromHeaderCheckBox, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        add(strictValidationCheckBox, gbc);

        gbc.gridx = 0; gbc.gridy = 6; gbc.weighty = 1.0;
        add(Box.createVerticalGlue(), gbc);
    }

    @Override
    public void setProperties(DataTypeProperties properties) {
        ASTME1394SerializationProperties ser = (ASTME1394SerializationProperties) properties.getSerializationProperties();
        ASTME1394DeserializationProperties des = (ASTME1394DeserializationProperties) properties.getDeserializationProperties();
        fieldDelimiterField.setText(String.valueOf(ser.getFieldDelimiter()));
        repeatDelimiterField.setText(String.valueOf(ser.getRepeatDelimiter()));
        componentDelimiterField.setText(String.valueOf(ser.getComponentDelimiter()));
        escapeCharacterField.setText(String.valueOf(ser.getEscapeCharacter()));
        deriveFromHeaderCheckBox.setSelected(des.isDeriveDelimitersFromHeader());
        strictValidationCheckBox.setSelected(ser.isUseStrictValidation());
    }

    @Override
    public DataTypeProperties getProperties() {
        ASTME1394DataTypeProperties props = new ASTME1394DataTypeProperties();
        ASTME1394SerializationProperties ser = (ASTME1394SerializationProperties) props.getSerializationProperties();
        ASTME1394DeserializationProperties des = (ASTME1394DeserializationProperties) props.getDeserializationProperties();
        String fd = fieldDelimiterField.getText();
        if (fd != null && fd.length() > 0) ser.setFieldDelimiter(fd.charAt(0));
        String rd = repeatDelimiterField.getText();
        if (rd != null && rd.length() > 0) ser.setRepeatDelimiter(rd.charAt(0));
        String cd = componentDelimiterField.getText();
        if (cd != null && cd.length() > 0) ser.setComponentDelimiter(cd.charAt(0));
        String ec = escapeCharacterField.getText();
        if (ec != null && ec.length() > 0) ser.setEscapeCharacter(ec.charAt(0));
        des.setFieldDelimiter(ser.getFieldDelimiter());
        des.setRepeatDelimiter(ser.getRepeatDelimiter());
        des.setComponentDelimiter(ser.getComponentDelimiter());
        des.setEscapeCharacter(ser.getEscapeCharacter());
        des.setDeriveDelimitersFromHeader(deriveFromHeaderCheckBox.isSelected());
        ser.setUseStrictValidation(strictValidationCheckBox.isSelected());
        return props;
    }
}
