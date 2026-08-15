package com.bitdreamit.connect.plugins.datatypes.astm.client;

import com.mirth.connect.client.ui.AbstractSettingsPanel;
import com.mirth.connect.client.ui.components.MirthCheckBox;
import com.mirth.connect.client.ui.components.MirthTextField;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.prefs.Preferences;

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
 *
 * <p><b>Implementation note:</b> Uses only standard JDK Swing layout
 * ({@link GridBagLayout}) and {@link JOptionPane} for validation alerts so
 * the panel compiles cleanly against the Mirth client jar without
 * requiring third-party libraries (MigLayout, SwingX) on the compile
 * classpath.</p>
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
        setLayout(new BorderLayout(0, 0));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(204, 204, 204)),
            "ASTM E1394 Defaults", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
            new Font("Tahoma", Font.BOLD, 11)));

        GridBagConstraints labelC = new GridBagConstraints();
        labelC.anchor  = GridBagConstraints.EAST;
        labelC.fill    = GridBagConstraints.NONE;
        labelC.weightx = 0.0;
        labelC.insets  = new Insets(4, 8, 4, 4);

        GridBagConstraints fieldC = new GridBagConstraints();
        fieldC.anchor  = GridBagConstraints.WEST;
        fieldC.fill    = GridBagConstraints.HORIZONTAL;
        fieldC.weightx = 1.0;
        fieldC.insets  = new Insets(4, 4, 4, 8);

        addRow(panel, 0, "Field Delimiter:",       fieldDelimiterField,        labelC, fieldC, 50);
        addRow(panel, 1, "Repeat Delimiter:",      repeatDelimiterField,       labelC, fieldC, 50);
        addRow(panel, 2, "Component Delimiter:",   componentDelimiterField,    labelC, fieldC, 50);
        addRow(panel, 3, "Escape Character:",      escapeCharacterField,       labelC, fieldC, 50);
        addRow(panel, 4, "Derive from Header:",    deriveFromHeaderCheckBox,   labelC, fieldC, 0);
        addRow(panel, 5, "Strict Validation:",     strictValidationCheckBox,   labelC, fieldC, 0);
        addRow(panel, 6, "Strip ASTM E1381 Chars:",stripASTM1381CharsCheckBox, labelC, fieldC, 0);
        addRow(panel, 7, "Convert Line Breaks:",   convertLineBreaksCheckBox,  labelC, fieldC, 0);
        addRow(panel, 8, "Use Field Repetitions:", useFieldRepetitionsCheckBox,labelC, fieldC, 0);
        addRow(panel, 9, "Use Subcomponents:",     useSubcomponentsCheckBox,   labelC, fieldC, 0);

        // Filler row to absorb vertical slack.
        GridBagConstraints fillerC = new GridBagConstraints();
        fillerC.gridx   = 0;
        fillerC.gridy   = 10;
        fillerC.gridwidth = 2;
        fillerC.weighty = 1.0;
        fillerC.fill    = GridBagConstraints.VERTICAL;
        panel.add(Box.createVerticalGlue(), fillerC);

        add(panel, BorderLayout.CENTER);
    }

    private void addRow(JPanel panel, int row, String labelText, JComponent field,
                        GridBagConstraints labelC, GridBagConstraints fieldC, int preferredWidth) {
        labelC.gridx = 0; labelC.gridy = row;
        fieldC.gridx = 1; fieldC.gridy = row;
        panel.add(new JLabel(labelText), labelC);
        if (preferredWidth > 0) {
            field.setPreferredSize(new Dimension(preferredWidth, field.getPreferredSize().height));
            field.setMinimumSize  (new Dimension(preferredWidth, field.getPreferredSize().height));
            field.setMaximumSize  (new Dimension(preferredWidth, field.getPreferredSize().height));
        }
        panel.add(field, fieldC);
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
            alertError("Field delimiter must be exactly 1 character.");
            fieldDelimiterField.requestFocus();
            return false;
        }
        if (!isSingleChar(repeatDelimiterField.getText())) {
            alertError("Repeat delimiter must be exactly 1 character.");
            repeatDelimiterField.requestFocus();
            return false;
        }
        if (!isSingleChar(componentDelimiterField.getText())) {
            alertError("Component delimiter must be exactly 1 character.");
            componentDelimiterField.requestFocus();
            return false;
        }
        if (!isSingleChar(escapeCharacterField.getText())) {
            alertError("Escape character must be exactly 1 character.");
            escapeCharacterField.requestFocus();
            return false;
        }
        // Ensure all four delimiters are distinct.
        String fd = fieldDelimiterField.getText();
        String rd = repeatDelimiterField.getText();
        String cd = componentDelimiterField.getText();
        String ec = escapeCharacterField.getText();
        if (fd.equals(rd) || fd.equals(cd) || fd.equals(ec) || rd.equals(cd) || rd.equals(ec) || cd.equals(ec)) {
            alertError("Delimiters must all be distinct characters.");
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

    /**
     * Display a modal error dialog. Uses standard JDK {@link JOptionPane}
     * rather than {@code PlatformUI.MIRTH_FRAME.alertError(...)} so the
     * panel does not require the SwingX library ({@code JXFrame}) on the
     * compile classpath.
     */
    private void alertError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    private boolean isSingleChar(String s) {
        return s != null && s.length() == 1;
    }
}
