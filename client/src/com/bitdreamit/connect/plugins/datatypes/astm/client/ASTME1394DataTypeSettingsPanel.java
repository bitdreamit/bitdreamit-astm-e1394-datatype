package com.bitdreamit.connect.plugins.datatypes.astm.client;

import com.mirth.connect.client.ui.AbstractSettingsPanel;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.components.MirthCheckBox;
import com.mirth.connect.client.ui.components.MirthTextField;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.prefs.Preferences;

import net.miginfocom.swing.MigLayout;

/**
 * Administrator UI settings panel for the ASTM E1394 data type.
 *
 * <p>Lets administrators edit the default delimiter set and parser options
 * that are applied to newly created channels. Settings are persisted via
 * {@link Preferences} so they survive Administrator restarts.</p>
 *
 * <p>Validation enforces single-character inputs for all four delimiter
 * fields — multi-character delimiters are not supported by the E1394 spec
 * and would corrupt message round-tripping.</p>
 */
public class ASTME1394DataTypeSettingsPanel extends AbstractSettingsPanel {

    private static final String PREFIX = "com.bitdreamit.connect.plugins.datatypes.astm.";

    private MirthTextField fieldDelimiterField;
    private MirthTextField repeatDelimiterField;
    private MirthTextField componentDelimiterField;
    private MirthTextField escapeCharacterField;
    private MirthCheckBox   deriveFromHeaderCheckBox;
    private MirthCheckBox   strictValidationCheckBox;
    private MirthCheckBox   stripASTM1381CharsCheckBox;
    private MirthCheckBox   convertLineBreaksCheckBox;
    private MirthCheckBox   useFieldRepetitionsCheckBox;
    private MirthCheckBox   useSubcomponentsCheckBox;

    public ASTME1394DataTypeSettingsPanel(String tabName) {
        super(tabName);
        initComponents();
        initLayout();
        doRefresh();
    }

    private void initComponents() {
        fieldDelimiterField       = new MirthTextField();
        repeatDelimiterField      = new MirthTextField();
        componentDelimiterField  = new MirthTextField();
        escapeCharacterField     = new MirthTextField();
        deriveFromHeaderCheckBox        = new MirthCheckBox();
        strictValidationCheckBox        = new MirthCheckBox();
        stripASTM1381CharsCheckBox      = new MirthCheckBox();
        convertLineBreaksCheckBox        = new MirthCheckBox();
        useFieldRepetitionsCheckBox      = new MirthCheckBox();
        useSubcomponentsCheckBox          = new MirthCheckBox();

        // Defaults match ASTME1394Constants.
        fieldDelimiterField.setText("|");
        repeatDelimiterField.setText("\\");
        componentDelimiterField.setText("^");
        escapeCharacterField.setText("&");
        stripASTM1381CharsCheckBox.setSelected(true);
        convertLineBreaksCheckBox.setSelected(true);
        useFieldRepetitionsCheckBox.setSelected(true);
        useSubcomponentsCheckBox.setSelected(true);
    }

    private void initLayout() {
        setBackground(Color.WHITE);
        setLayout(new MigLayout("insets 12, fillx, wrap 2", "[right][left,grow]", ""));

        JPanel panel = new JPanel(new MigLayout("insets 12, fillx, wrap 2", "[right][left,grow]"));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(204, 204, 204)),
            "ASTM E1394 Defaults", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
            new Font("Tahoma", Font.BOLD, 11)));

        panel.add(new JLabel("Field Delimiter:"));        panel.add(fieldDelimiterField,       "w 50!");
        panel.add(new JLabel("Repeat Delimiter:"));       panel.add(repeatDelimiterField,      "w 50!");
        panel.add(new JLabel("Component Delimiter:"));    panel.add(componentDelimiterField,  "w 50!");
        panel.add(new JLabel("Escape Character:"));       panel.add(escapeCharacterField,      "w 50!");
        panel.add(new JLabel("Derive from Header:"));      panel.add(deriveFromHeaderCheckBox);
        panel.add(new JLabel("Strict Validation:"));      panel.add(strictValidationCheckBox);
        panel.add(new JLabel("Strip ASTM E1381 Chars:")); panel.add(stripASTM1381CharsCheckBox);
        panel.add(new JLabel("Convert Line Breaks:"));     panel.add(convertLineBreaksCheckBox);
        panel.add(new JLabel("Use Field Repetitions:"));  panel.add(useFieldRepetitionsCheckBox);
        panel.add(new JLabel("Use Subcomponents:"));       panel.add(useSubcomponentsCheckBox);

        add(panel, "growx");
    }

    @Override
    public void doRefresh() {
        Preferences p = Preferences.userNodeForPackage(this.getClass());
        fieldDelimiterField.setText(p.get(PREFIX + "fieldDelimiter", "|"));
        repeatDelimiterField.setText(p.get(PREFIX + "repeatDelimiter", "\\"));
        componentDelimiterField.setText(p.get(PREFIX + "componentDelimiter", "^"));
        escapeCharacterField.setText(p.get(PREFIX + "escapeCharacter", "&"));
        deriveFromHeaderCheckBox.setSelected(p.getBoolean(PREFIX + "deriveFromHeader", false));
        strictValidationCheckBox.setSelected(p.getBoolean(PREFIX + "strictValidation", false));
        stripASTM1381CharsCheckBox.setSelected(p.getBoolean(PREFIX + "stripASTM1381Chars", true));
        convertLineBreaksCheckBox.setSelected(p.getBoolean(PREFIX + "convertLineBreaks", true));
        useFieldRepetitionsCheckBox.setSelected(p.getBoolean(PREFIX + "useFieldRepetitions", true));
        useSubcomponentsCheckBox.setSelected(p.getBoolean(PREFIX + "useSubcomponents", true));
    }

    @Override
    public boolean doSave() {
        if (!isSingleChar(fieldDelimiterField.getText())) {
            PlatformUI.MIRTH_FRAME.alertError(this, "Field delimiter must be exactly 1 character.");
            fieldDelimiterField.requestFocus();
            return false;
        }
        if (!isSingleChar(repeatDelimiterField.getText())) {
            PlatformUI.MIRTH_FRAME.alertError(this, "Repeat delimiter must be exactly 1 character.");
            repeatDelimiterField.requestFocus();
            return false;
        }
        if (!isSingleChar(componentDelimiterField.getText())) {
            PlatformUI.MIRTH_FRAME.alertError(this, "Component delimiter must be exactly 1 character.");
            componentDelimiterField.requestFocus();
            return false;
        }
        if (!isSingleChar(escapeCharacterField.getText())) {
            PlatformUI.MIRTH_FRAME.alertError(this, "Escape character must be exactly 1 character.");
            escapeCharacterField.requestFocus();
            return false;
        }
        // Ensure all four delimiters are distinct.
        String fd = fieldDelimiterField.getText();
        String rd = repeatDelimiterField.getText();
        String cd = componentDelimiterField.getText();
        String ec = escapeCharacterField.getText();
        if (fd.equals(rd) || fd.equals(cd) || fd.equals(ec) || rd.equals(cd) || rd.equals(ec) || cd.equals(ec)) {
            PlatformUI.MIRTH_FRAME.alertError(this, "Delimiters must all be distinct characters.");
            return false;
        }

        Preferences p = Preferences.userNodeForPackage(this.getClass());
        p.put(PREFIX + "fieldDelimiter",     fd);
        p.put(PREFIX + "repeatDelimiter",    rd);
        p.put(PREFIX + "componentDelimiter", cd);
        p.put(PREFIX + "escapeCharacter",    ec);
        p.putBoolean(PREFIX + "deriveFromHeader",      deriveFromHeaderCheckBox.isSelected());
        p.putBoolean(PREFIX + "strictValidation",      strictValidationCheckBox.isSelected());
        p.putBoolean(PREFIX + "stripASTM1381Chars",    stripASTM1381CharsCheckBox.isSelected());
        p.putBoolean(PREFIX + "convertLineBreaks",     convertLineBreaksCheckBox.isSelected());
        p.putBoolean(PREFIX + "useFieldRepetitions",   useFieldRepetitionsCheckBox.isSelected());
        p.putBoolean(PREFIX + "useSubcomponents",     useSubcomponentsCheckBox.isSelected());
        return true;
    }

    private boolean isSingleChar(String s) {
        return s != null && s.length() == 1;
    }
}
