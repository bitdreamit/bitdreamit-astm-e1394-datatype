package com.bitdreamit.connect.plugins.datatypes.astm.test;

import com.bitdreamit.connect.plugins.datatypes.astm.server.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class ASTME1394RoundTripTest {

    private static final String SAMPLE =
        "H|\^&|||BitDreamLIS^1.0|||||HOST||P|1|20260813120000\r" +
        "P|1||MRN000123||DOE^JANE||19900101|F\r" +
        "O|1|SPEC001||^^^GLU|R||20260813120000|||||A||||1\r" +
        "R|1|^^^GLU|98|mg/dL|70-99||N||F||ANALYZER01|20260813121500\r" +
        "L|1|N\r";

    @Test
    public void testRoundTrip() throws Exception {
        ASTME1394DeserializationProperties desProps = new ASTME1394DeserializationProperties();
        ASTME1394SerializationProperties serProps = new ASTME1394SerializationProperties();

        ASTME1394Deserializer deserializer = new ASTME1394Deserializer(desProps);
        String xml = deserializer.toXML(SAMPLE);
        assertTrue(xml.contains("<ASTM>"));
        assertTrue(xml.contains("<H>"));
        assertTrue(xml.contains("<P>"));
        assertTrue(xml.contains("<O>"));
        assertTrue(xml.contains("<R>"));
        assertTrue(xml.contains("<L>"));

        org.w3c.dom.Document doc = com.mirth.connect.util.XmlUtil.parse(xml);
        ASTME1394Serializer serializer = new ASTME1394Serializer(serProps);
        String reconstructed = serializer.fromXML(doc);

        // Normalize for comparison
        String normOriginal = SAMPLE.replace("\r", "\r").trim();
        String normReconstructed = reconstructed.trim();
        assertEquals(normOriginal, normReconstructed);
    }

    @Test
    public void testHeaderDelimiterDerivation() throws Exception {
        String msg = "H|#@!|||TestLIS|||||HOST||P|1|20260101\rP|1||ID1\rL|1|N\r";
        ASTME1394DeserializationProperties props = new ASTME1394DeserializationProperties();
        props.setDeriveDelimitersFromHeader(true);
        ASTME1394Deserializer des = new ASTME1394Deserializer(props);
        String xml = des.toXML(msg);
        assertTrue(xml.contains("<H>"));
    }

    @Test
    public void testEscapeRoundTrip() throws Exception {
        String msg = "H|\^&|||Test\F\S\R\E\H0A\rP|1||ID1\rL|1|N\r";
        ASTME1394DeserializationProperties desProps = new ASTME1394DeserializationProperties();
        ASTME1394SerializationProperties serProps = new ASTME1394SerializationProperties();
        ASTME1394Deserializer des = new ASTME1394Deserializer(desProps);
        String xml = des.toXML(msg);
        org.w3c.dom.Document doc = com.mirth.connect.util.XmlUtil.parse(xml);
        ASTME1394Serializer ser = new ASTME1394Serializer(serProps);
        String out = ser.fromXML(doc);
        assertTrue(out.contains("\F"));
        assertTrue(out.contains("\S"));
        assertTrue(out.contains("\R"));
        assertTrue(out.contains("\E"));
    }

    @Test
    public void testBatchSplit() throws Exception {
        String batch =
            "H|\^&|||A\rP|1||ID1\rL|1|N\r" +
            "H|\^&|||B\rP|1||ID2\rL|1|N\r";
        ASTME1394BatchProperties props = new ASTME1394BatchProperties();
        props.setBatchSplitType("H_L_BOUNDARY");
        ASTME1394BatchAdaptor adaptor = new ASTME1394BatchAdaptor(props);
        java.util.List<String> msgs = adaptor.getMessages(batch);
        assertEquals(2, msgs.size());
        assertTrue(msgs.get(0).contains("A"));
        assertTrue(msgs.get(1).contains("B"));
    }
}
