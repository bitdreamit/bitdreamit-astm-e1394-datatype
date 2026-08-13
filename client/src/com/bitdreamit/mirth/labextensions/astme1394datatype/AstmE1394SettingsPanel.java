/*
 * BitDreamIT Mirth Lab Extensions
 * Copyright (c) 2026 Kimi AI (Moonshot AI) — MIT License
 */
package com.bitdreamit.mirth.labextensions.astme1394datatype;

import com.mirth.connect.client.ui.AbstractConnectorSettingsPanel;
import com.mirth.connect.client.ui.components.MirthCheckBox;
import com.mirth.connect.client.ui.components.MirthTextField;
import com.mirth.connect.model.datatype.DataTypeProperties;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class AstmE1394SettingsPanel extends AbstractConnectorSettingsPanel {

    // General tab
    private MirthCheckBox strictBox;
    private MirthCheckBox fieldValidationBox;
    private MirthCheckBox recordTypeBox;
    private MirthCheckBox fieldCountBox;
    private MirthCheckBox responseBox;
    private JLabel responseStatusLabel;
    private JComboBox<String> responseStatusBox;
    private MirthCheckBox trackControlIdBox;
    private JLabel maxHistoryLabel;
    private MirthTextField maxHistoryField;

    // Delimiters tab
    private JLabel segDelimLabel;
    private MirthTextField segDelimField;
    private JLabel fieldDelimLabel;
    private MirthTextField fieldDelimField;
    private JLabel compDelimLabel;
    private MirthTextField compDelimField;
    private JLabel repeatDelimLabel;
    private MirthTextField repeatDelimField;
    private JLabel escapeDelimLabel;
    private MirthTextField escapeDelimField;

    // Advanced tab
    private MirthCheckBox repeatFieldsBox;
    private MirthCheckBox subcompBox;
    private MirthCheckBox manufacturerBox;
    private MirthCheckBox stripEmptyBox;
    private MirthCheckBox includeEmptyBox;
    private MirthCheckBox prettyPrintBox;
    private MirthCheckBox templateBox;
    private JLabel templateLabel;
    private JTextArea templateArea;
    private MirthCheckBox batchScriptBox;
    private JLabel batchLabel;
    private JTextArea batchArea;

    public AstmE1394SettingsPanel() {
        initComponents();
    }

    private void initComponents() {
        setBackground(Color.WHITE);
        setLayout(new MigLayout("insets 8, novisualpadding, hidemode 3, fill, gap 4", "[grow]", ""));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(Color.WHITE);

        // ===== GENERAL TAB =====
        JPanel generalPanel = new JPanel(new MigLayout("insets 8, gap 4", "[][grow]", ""));
        generalPanel.setBackground(Color.WHITE);

        strictBox = new MirthCheckBox("Use Strict Validation");
        strictBox.setSelected(true);
        strictBox.setBackground(Color.WHITE);
        fieldValidationBox = new MirthCheckBox("Field-Level Data Type Validation");
        fieldValidationBox.setSelected(true);
        fieldValidationBox.setBackground(Color.WHITE);
        recordTypeBox = new MirthCheckBox("Validate Record Types (H/P/O/R/C/L only)");
        recordTypeBox.setSelected(true);
        recordTypeBox.setBackground(Color.WHITE);
        fieldCountBox = new MirthCheckBox("Validate Minimum Field Counts");
        fieldCountBox.setSelected(true);
        fieldCountBox.setBackground(Color.WHITE);
        responseBox = new MirthCheckBox("Generate Response Messages (ACK/NAK)");
        responseBox.setSelected(true);
        responseBox.setBackground(Color.WHITE);
        responseStatusLabel = new JLabel("Default Response Status:");
        responseStatusBox = new JComboBox<>(new String[]{"AA (Accepted)", "AE (Application Error)", "AR (Rejected)"});
        trackControlIdBox = new MirthCheckBox("Track Control IDs for Deduplication");
        trackControlIdBox.setSelected(true);
        trackControlIdBox.setBackground(Color.WHITE);
        maxHistoryLabel = new JLabel("Max History Size:");
        maxHistoryField = new MirthTextField();
        maxHistoryField.setText("10000");

        generalPanel.add(strictBox, "span 2, wrap");
        generalPanel.add(fieldValidationBox, "span 2, wrap");
        generalPanel.add(recordTypeBox, "span 2, wrap");
        generalPanel.add(fieldCountBox, "span 2, wrap");
        generalPanel.add(responseBox, "span 2, wrap");
        generalPanel.add(responseStatusLabel, "right");
        generalPanel.add(responseStatusBox, "w 180!, wrap");
        generalPanel.add(trackControlIdBox, "span 2, wrap");
        generalPanel.add(maxHistoryLabel, "right");
        generalPanel.add(maxHistoryField, "w 100!, wrap");
        tabs.addTab("General", generalPanel);

        // ===== DELIMITERS TAB =====
        JPanel delimPanel = new JPanel(new MigLayout("insets 8, gap 4", "[][grow]", ""));
        delimPanel.setBackground(Color.WHITE);
        delimPanel.setBorder(new TitledBorder("ASTM Delimiters"));

        segDelimLabel = new JLabel("Segment Delimiter (\r):");
        segDelimField = new MirthTextField(); segDelimField.setText("\r");
        fieldDelimLabel = new JLabel("Field Delimiter (|):");
        fieldDelimField = new MirthTextField(); fieldDelimField.setText("|");
        compDelimLabel = new JLabel("Component Delimiter (^):");
        compDelimField = new MirthTextField(); compDelimField.setText("^");
        repeatDelimLabel = new JLabel("Repeat Delimiter (\):");
        repeatDelimField = new MirthTextField(); repeatDelimField.setText("\\");
        escapeDelimLabel = new JLabel("Escape Delimiter (&):");
        escapeDelimField = new MirthTextField(); escapeDelimField.setText("&");

        delimPanel.add(segDelimLabel, "right");
        delimPanel.add(segDelimField, "w 100!, wrap");
        delimPanel.add(fieldDelimLabel, "right");
        delimPanel.add(fieldDelimField, "w 100!, wrap");
        delimPanel.add(compDelimLabel, "right");
        delimPanel.add(compDelimField, "w 100!, wrap");
        delimPanel.add(repeatDelimLabel, "right");
        delimPanel.add(repeatDelimField, "w 100!, wrap");
        delimPanel.add(escapeDelimLabel, "right");
        delimPanel.add(escapeDelimField, "w 100!, wrap");
        tabs.addTab("Delimiters", delimPanel);

        // ===== ADVANCED TAB =====
        JPanel advancedPanel = new JPanel(new MigLayout("insets 8, gap 4", "[][grow]", ""));
        advancedPanel.setBackground(Color.WHITE);

        repeatFieldsBox = new MirthCheckBox("Handle Repeat Fields");
        repeatFieldsBox.setSelected(true);
        repeatFieldsBox.setBackground(Color.WHITE);
        subcompBox = new MirthCheckBox("Handle Subcomponents");
        subcompBox.setSelected(true);
        subcompBox.setBackground(Color.WHITE);
        manufacturerBox = new MirthCheckBox("Allow Manufacturer Extensions");
        manufacturerBox.setBackground(Color.WHITE);
        stripEmptyBox = new MirthCheckBox("Strip Empty Fields on Parse");
        stripEmptyBox.setBackground(Color.WHITE);
        includeEmptyBox = new MirthCheckBox("Include Empty Fields on Serialize");
        includeEmptyBox.setSelected(true);
        includeEmptyBox.setBackground(Color.WHITE);
        prettyPrintBox = new MirthCheckBox("Pretty-Print XML Output");
        prettyPrintBox.setBackground(Color.WHITE);

        templateBox = new MirthCheckBox("Enable Template Engine (Extra Feature)");
        templateBox.setBackground(Color.WHITE);
        templateLabel = new JLabel("Template:");
        templateArea = new JTextArea(3, 40);
        templateArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane templateScroll = new JScrollPane(templateArea);

        batchScriptBox = new MirthCheckBox("Enable Batch Scripting (Extra Feature)");
        batchScriptBox.setBackground(Color.WHITE);
        batchLabel = new JLabel("Batch Script (Groovy/JS):");
        batchArea = new JTextArea(3, 40);
        batchArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane batchScroll = new JScrollPane(batchArea);

        advancedPanel.add(repeatFieldsBox, "span 2, wrap");
        advancedPanel.add(subcompBox, "span 2, wrap");
        advancedPanel.add(manufacturerBox, "span 2, wrap");
        advancedPanel.add(stripEmptyBox, "span 2, wrap");
        advancedPanel.add(includeEmptyBox, "span 2, wrap");
        advancedPanel.add(prettyPrintBox, "span 2, wrap");
        advancedPanel.add(templateBox, "span 2, wrap");
        advancedPanel.add(templateLabel, "right, top");
        advancedPanel.add(templateScroll, "growx, h 60!, wrap");
        advancedPanel.add(batchScriptBox, "span 2, wrap");
        advancedPanel.add(batchLabel, "right, top");
        advancedPanel.add(batchScroll, "growx, h 60!, wrap");
        tabs.addTab("Advanced", advancedPanel);

        add(tabs, "growx, wrap");
    }

    @Override
    public DataTypeProperties getProperties() {
        AstmE1394DataTypeProperties p = new AstmE1394DataTypeProperties();
        p.setUseStrictValidation(strictBox.isSelected());
        p.setFieldLevelValidation(fieldValidationBox.isSelected());
        p.setValidateRecordTypes(recordTypeBox.isSelected());
        p.setValidateFieldCounts(fieldCountBox.isSelected());
        p.setGenerateResponses(responseBox.isSelected());
        String rs = (String) responseStatusBox.getSelectedItem();
        p.setResponseStatus(rs != null ? rs.substring(0, 2) : "AA");
        p.setTrackControlIds(trackControlIdBox.isSelected());
        try { p.setMaxControlIdHistory(Integer.parseInt(maxHistoryField.getText())); } catch (Exception ignored) {}

        p.setSegmentDelimiter(segDelimField.getText());
        p.setFieldDelimiter(fieldDelimField.getText());
        p.setComponentDelimiter(compDelimField.getText());
        p.setRepeatDelimiter(repeatDelimField.getText());
        p.setEscapeDelimiter(escapeDelimField.getText());

        p.setHandleRepeatFields(repeatFieldsBox.isSelected());
        p.setHandleSubcomponents(subcompBox.isSelected());
        p.setHandleManufacturerExtensions(manufacturerBox.isSelected());
        p.setStripEmptyFields(stripEmptyBox.isSelected());
        p.setIncludeEmptyFields(includeEmptyBox.isSelected());
        p.setPrettyPrintXml(prettyPrintBox.isSelected());
        p.setEnableTemplateEngine(templateBox.isSelected());
        p.setSerializationTemplate(templateArea.getText());
        p.setEnableBatchScripting(batchScriptBox.isSelected());
        p.setBatchScript(batchArea.getText());
        return p;
    }

    @Override
    public void setProperties(DataTypeProperties properties) {
        if (properties instanceof AstmE1394DataTypeProperties) {
            AstmE1394DataTypeProperties p = (AstmE1394DataTypeProperties) properties;
            strictBox.setSelected(p.isUseStrictValidation());
            fieldValidationBox.setSelected(p.isFieldLevelValidation());
            recordTypeBox.setSelected(p.isValidateRecordTypes());
            fieldCountBox.setSelected(p.isValidateFieldCounts());
            responseBox.setSelected(p.isGenerateResponses());
            String rs = p.getResponseStatus();
            if ("AA".equals(rs)) responseStatusBox.setSelectedIndex(0);
            else if ("AE".equals(rs)) responseStatusBox.setSelectedIndex(1);
            else if ("AR".equals(rs)) responseStatusBox.setSelectedIndex(2);
            trackControlIdBox.setSelected(p.isTrackControlIds());
            maxHistoryField.setText(String.valueOf(p.getMaxControlIdHistory()));

            segDelimField.setText(p.getSegmentDelimiter());
            fieldDelimField.setText(p.getFieldDelimiter());
            compDelimField.setText(p.getComponentDelimiter());
            repeatDelimField.setText(p.getRepeatDelimiter());
            escapeDelimField.setText(p.getEscapeDelimiter());

            repeatFieldsBox.setSelected(p.isHandleRepeatFields());
            subcompBox.setSelected(p.isHandleSubcomponents());
            manufacturerBox.setSelected(p.isHandleManufacturerExtensions());
            stripEmptyBox.setSelected(p.isStripEmptyFields());
            includeEmptyBox.setSelected(p.isIncludeEmptyFields());
            prettyPrintBox.setSelected(p.isPrettyPrintXml());
            templateBox.setSelected(p.isEnableTemplateEngine());
            templateArea.setText(p.getSerializationTemplate());
            batchScriptBox.setSelected(p.isEnableBatchScripting());
            batchArea.setText(p.getBatchScript());
        }
    }

    @Override
    public DataTypeProperties getDefaults() {
        return new AstmE1394DataTypeProperties();
    }

    @Override
    public boolean checkProperties(DataTypeProperties properties, boolean highlight) {
        return true;
    }

    @Override
    public void resetInvalidProperties() {}
}