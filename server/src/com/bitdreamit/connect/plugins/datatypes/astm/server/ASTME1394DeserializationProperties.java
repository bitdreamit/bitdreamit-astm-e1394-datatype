package com.bitdreamit.connect.plugins.datatypes.astm.server;

import com.mirth.connect.model.datatype.DeserializationProperties;
import com.mirth.connect.donkey.util.DonkeyElement;
import com.bitdreamit.connect.plugins.datatypes.astm.shared.ASTME1394Constants;

public class ASTME1394DeserializationProperties extends DeserializationProperties {
    private char fieldDelimiter = ASTME1394Constants.DEFAULT_FIELD_DELIMITER;
    private char repeatDelimiter = ASTME1394Constants.DEFAULT_REPEAT_DELIMITER;
    private char componentDelimiter = ASTME1394Constants.DEFAULT_COMPONENT_DELIMITER;
    private char escapeCharacter = ASTME1394Constants.DEFAULT_ESCAPE_CHARACTER;
    private boolean deriveDelimitersFromHeader = true;

    public char getFieldDelimiter() { return fieldDelimiter; }
    public void setFieldDelimiter(char v) { this.fieldDelimiter = v; }
    public char getRepeatDelimiter() { return repeatDelimiter; }
    public void setRepeatDelimiter(char v) { this.repeatDelimiter = v; }
    public char getComponentDelimiter() { return componentDelimiter; }
    public void setComponentDelimiter(char v) { this.componentDelimiter = v; }
    public char getEscapeCharacter() { return escapeCharacter; }
    public void setEscapeCharacter(char v) { this.escapeCharacter = v; }
    public boolean isDeriveDelimitersFromHeader() { return deriveDelimitersFromHeader; }
    public void setDeriveDelimitersFromHeader(boolean v) { this.deriveDelimitersFromHeader = v; }

    @Override
    public DonkeyElement toDonkeyElement() {
        DonkeyElement element = new DonkeyElement("deserializationProperties");
        element.addChildElement("fieldDelimiter", String.valueOf(fieldDelimiter));
        element.addChildElement("repeatDelimiter", String.valueOf(repeatDelimiter));
        element.addChildElement("componentDelimiter", String.valueOf(componentDelimiter));
        element.addChildElement("escapeCharacter", String.valueOf(escapeCharacter));
        element.addChildElement("deriveDelimitersFromHeader", String.valueOf(deriveDelimitersFromHeader));
        return element;
    }
}
