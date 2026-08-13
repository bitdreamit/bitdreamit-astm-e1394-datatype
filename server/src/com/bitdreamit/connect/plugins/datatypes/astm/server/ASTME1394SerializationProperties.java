package com.bitdreamit.connect.plugins.datatypes.astm.server;

import com.mirth.connect.model.datatype.SerializationProperties;
import com.mirth.connect.donkey.util.DonkeyElement;
import com.bitdreamit.connect.plugins.datatypes.astm.shared.ASTME1394Constants;

public class ASTME1394SerializationProperties extends SerializationProperties {
    private char fieldDelimiter = ASTME1394Constants.DEFAULT_FIELD_DELIMITER;
    private char repeatDelimiter = ASTME1394Constants.DEFAULT_REPEAT_DELIMITER;
    private char componentDelimiter = ASTME1394Constants.DEFAULT_COMPONENT_DELIMITER;
    private char escapeCharacter = ASTME1394Constants.DEFAULT_ESCAPE_CHARACTER;
    private boolean useStrictValidation = false;

    public char getFieldDelimiter() { return fieldDelimiter; }
    public void setFieldDelimiter(char v) { this.fieldDelimiter = v; }
    public char getRepeatDelimiter() { return repeatDelimiter; }
    public void setRepeatDelimiter(char v) { this.repeatDelimiter = v; }
    public char getComponentDelimiter() { return componentDelimiter; }
    public void setComponentDelimiter(char v) { this.componentDelimiter = v; }
    public char getEscapeCharacter() { return escapeCharacter; }
    public void setEscapeCharacter(char v) { this.escapeCharacter = v; }
    public boolean isUseStrictValidation() { return useStrictValidation; }
    public void setUseStrictValidation(boolean v) { this.useStrictValidation = v; }

    @Override
    public DonkeyElement toDonkeyElement() {
        DonkeyElement element = new DonkeyElement("serializationProperties");
        element.addChildElement("fieldDelimiter", String.valueOf(fieldDelimiter));
        element.addChildElement("repeatDelimiter", String.valueOf(repeatDelimiter));
        element.addChildElement("componentDelimiter", String.valueOf(componentDelimiter));
        element.addChildElement("escapeCharacter", String.valueOf(escapeCharacter));
        element.addChildElement("useStrictValidation", String.valueOf(useStrictValidation));
        return element;
    }
}
